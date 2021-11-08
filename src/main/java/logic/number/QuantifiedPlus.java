package logic.number;

import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import java.util.Set;
import java.util.ArrayList;

/** A QuantifiedPlus represents the addition of two or more QuantifiedIntegers. */
public class QuantifiedPlus implements QuantifiedInteger {
  private ArrayList<QuantifiedInteger> _parts;
  int _kind;
  Atom _truth;

  public QuantifiedPlus(QuantifiedInteger l, QuantifiedInteger r, int kind, Atom truth) {
    _parts = new ArrayList<QuantifiedInteger>();
    _parts.add(l);
    _parts.add(r);
    _kind = kind;
    _truth = truth;
  }

  public QuantifiedPlus(ArrayList<QuantifiedInteger> parts, int kind, Atom truth) {
    _parts = new ArrayList<QuantifiedInteger>(parts);
    if (_parts.size() < 2) {
      throw new Error("Creating quantified plus with only " + _parts.size() + " parts.");
    }
    _kind = kind;
    _truth = truth;
  }

  public Set<String> queryParameters() {
    Set<String> ret = _parts.get(0).queryParameters();
    for (int i = 1; i < _parts.size(); i++) ret.addAll(_parts.get(i).queryParameters());
    return ret;
  }

  public boolean queryClosed() {
    for (int i = 0; i < _parts.size(); i++) {
      if (!_parts.get(i).queryClosed()) return false;
    }
    return true;
  }

  public QuantifiedPlus substitute(Substitution subst) {
    ArrayList<QuantifiedInteger> newparts = new ArrayList<QuantifiedInteger>();
    for (int i = 0; i < _parts.size(); i++) {
      newparts.add(_parts.get(i).substitute(subst));
    }
    return new QuantifiedPlus(newparts, _kind, _truth);
  }

  private PlusInteger makePlus(ClosedInteger left, ClosedInteger right) {
    return new PlusInteger(left, right, _kind, _truth);
  }

  /** Given that start ≤ end, this returns the sum of parts[start..end]. */
  private ClosedInteger createAddition(ArrayList<ClosedInteger> parts, int start, int end) {
    if (start == end) return parts.get(start);
    if (start + 1 == end) return new PlusInteger(parts.get(start), parts.get(end), _kind, _truth);
    int middle = (start + end) / 2;
    return makePlus(createAddition(parts, start, middle), createAddition(parts, middle + 1, end));
  }

  public ClosedInteger instantiate(Assignment ass) {
    int constant = 0;
    ArrayList<ClosedInteger> evalparts = new ArrayList<ClosedInteger>();

    for (int i = 0; i < _parts.size(); i++) {
      ClosedInteger ri = _parts.get(i).instantiate(ass);
      if (ri.queryMinimum() == ri.queryMaximum()) constant += ri.queryMinimum();
      else evalparts.add(ri);
    }

    // we only got constants!
    if (evalparts.size() == 0) return new ConstantInteger(constant, _truth);

    // just one non-constant
    if (evalparts.size() == 1) {
      if (constant == 0) return evalparts.get(0);
      return makePlus(evalparts.get(0), new ConstantInteger(constant, _truth));
    }

    // at least two non-constants; we will have to combine them
    ClosedInteger sum = createAddition(evalparts, 0, evalparts.size()-1);
    if (constant == 0) return sum;
    else return makePlus(sum, new ConstantInteger(constant, _truth));
  }

  public String toString() {
    String ret = _parts.get(0).toString();
    String symbol = _kind == ClosedInteger.RANGE ? " ⊕ " :
                    _kind == ClosedInteger.BINARY ? " ⊞ " : " + ";
    for (int i = 1; i < _parts.size(); i++) ret += symbol + _parts.get(i).toString();
    return ret;
  }
}

