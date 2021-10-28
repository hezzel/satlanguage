package logic.range;

import logic.sat.Atom;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import java.util.Set;
import java.util.ArrayList;

/** A QuantifiedRangePlus represents the addition of two or more QuantifiedRangeIntegers. */
public class QuantifiedRangePlus implements QuantifiedRangeInteger {
  private ArrayList<QuantifiedRangeInteger> _parts;

  public QuantifiedRangePlus(QuantifiedRangeInteger l, QuantifiedRangeInteger r) {
    _parts = new ArrayList<QuantifiedRangeInteger>();
    _parts.add(l);
    _parts.add(r);
  }

  public QuantifiedRangePlus(ArrayList<QuantifiedRangeInteger> parts) {
    _parts = new ArrayList<QuantifiedRangeInteger>(parts);
    if (_parts.size() < 2) {
      throw new Error("Creating quantified range plus with only " + _parts.size() + " parts.");
    }
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

  public QuantifiedRangePlus substitute(Substitution subst) {
    ArrayList<QuantifiedRangeInteger> newparts = new ArrayList<QuantifiedRangeInteger>();
    for (int i = 0; i < _parts.size(); i++) {
      newparts.add(_parts.get(i).substitute(subst));
    }
    return new QuantifiedRangePlus(newparts);
  }

  /** Given that start ≤ end, this returns the sum of parts[start..end]. */
  private RangeInteger createAddition(ArrayList<RangeInteger> parts, int start, int end) {
    if (start == end) return parts.get(start);
    if (start + 1 == end) return new RangePlus(parts.get(start), parts.get(end));
    int middle = (start + end) / 2;
    return new RangePlus(createAddition(parts, start, middle),
                         createAddition(parts, middle + 1, end));
  }

  public RangeInteger instantiate(Assignment ass) {
    Atom truth = null;
    int constant = 0;
    ArrayList<RangeInteger> evalparts = new ArrayList<RangeInteger>();

    for (int i = 0; i < _parts.size(); i++) {
      RangeInteger ri = _parts.get(i).instantiate(ass);
      if (ri.queryMinimum() == ri.queryMaximum()) {
        constant += ri.queryMinimum();
        truth = ri.queryGeqAtom(ri.queryMinimum());     // only needed if there is ≥ 1 constant
      }
      else evalparts.add(ri);
    }

    // we only got constants!
    if (evalparts.size() == 0) return new RangeConstant(constant, truth);

    // just one non-constant
    if (evalparts.size() == 1) {
      if (constant == 0) return evalparts.get(0);
      return new RangeShift(evalparts.get(0), constant);
    }

    // at least two non-constants; we will need a RangePlus
    RangeInteger sum = createAddition(evalparts, 0, evalparts.size()-1);
    if (constant == 0) return sum;
    else return new RangeShift(sum, constant);
  }

  public String toString() {
    String ret = _parts.get(0).toString();
    for (int i = 1; i < _parts.size(); i++) ret += " ⊕ " + _parts.get(i).toString();
    return ret;
  }
}

