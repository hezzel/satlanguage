package logic.number.binary;

import logic.sat.*;
import logic.sat.ClauseCollection;
import logic.number.general.ClauseAdder;
import java.util.ArrayList;

/**
 * A binary conditional represents an expression x?value where x is an Atom and value is a binary
 * integer.
 */
public class BinaryConditional implements BinaryInteger {
  private Atom _condition;
  private Atom _truth;
  private BinaryInteger _value;
  private ArrayList<Atom> _parts;
  private Atom _negativeBit;
  private ClauseAdder _adder;

  public BinaryConditional(Atom atom, BinaryInteger value, Atom truth, ClauseAdder adder) {
    _condition = atom;
    _truth = truth;
    _value = value;
    _parts = new ArrayList<Atom>();
    _adder = adder;

    Atom falsehood = truth.negate();
    for (int i = 0; i < value.length(); i++) {
      if (value.queryBit(i).equals(truth)) _parts.add(atom);
      else if (value.queryBit(i).equals(falsehood)) _parts.add(falsehood);
      else _parts.add(new Atom(new Variable(toString() + "⟨" + i + "⟩"), true));
    }
    if (_value.queryNegativeBit().equals(truth)) _negativeBit = atom;
    else if (_value.queryNegativeBit().equals(falsehood)) _negativeBit = falsehood;
    else _negativeBit = new Atom(new Variable(toString() + "⟨-⟩"), true);
  }

  public int queryMinimum() {
    return _value.queryMinimum() < 0 ? _value.queryMinimum() : 0;
  }

  public int queryMaximum() {
    return _value.queryMaximum() > 0 ? _value.queryMaximum() : 0;
  }

  public int length() {
    return _parts.size();
  }

  public Atom queryBit(int i) {
    if (i < _parts.size()) return _parts.get(i);
    return _negativeBit;
  }

  public Atom queryNegativeBit() {
    return _negativeBit;
  }

  /* if b != TRUE and b != FALSE, this adds clauses for a <-> b /\ c to col */
  private void requireEquivalence(Atom a, Atom b, Atom c, ClauseCollection col) {
    if (b.equals(_truth)) return;
    if (b.equals(_truth.negate())) return;
    col.addClause(new Clause(a.negate(), b));
    col.addClause(new Clause(a.negate(), c));
    col.addClause(new Clause(b.negate(), c.negate(), a));
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    if (col.isInMemory("bconditional: " + toString())) return;
    col.addToMemory("bconditional: " + toString());

    _adder.add(col);

    Atom falsehood = _truth.negate();
    for (int i = 0; i < length(); i++) {
      requireEquivalence(_parts.get(i), _value.queryBit(i), _condition, col);
    }
    requireEquivalence(_negativeBit, _value.queryNegativeBit(), _condition, col);
  }

  public String toString() {
    return _condition.toString() + "?" + _value;
  }
}

