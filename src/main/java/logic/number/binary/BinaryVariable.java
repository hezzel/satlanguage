package logic.number.binary;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import java.util.ArrayList;

/**
 * A binary variable consists of a number of bits (Atoms) together representing the binary
 * representation of a number.
 * A binary integer may be limited to a specific range, which leads to well-definedness clauses
 * that guarantee that the variables are indeed within that range.
 */
public class BinaryVariable implements BinaryInteger {
  private String _name;
  private int _minimum;
  private int _maximum;
  private ArrayList<Atom> _parts;
  private Atom _negativeBit;

  /**
   * Generates a binary variable ranging either between -2^{length}..2^{length}-1 (if allowNegative
   * is true), or between 0..2^{length}-1 (if allowNegative is false)
   */
  public BinaryVariable(String name, int length, boolean allowNegative, Atom truth) {
    _name = name;
    _parts = new ArrayList<Atom>();
    for (int i = 0; i < length; i++) {
      _parts.add(new Atom(new Variable(name + "⟨" + i + "⟩"), true));
    }
    _maximum = (1 << length) - 1;
    if (allowNegative) {
      _minimum = -1-_maximum;
      _negativeBit = new Atom(new Variable(name + "⟨-⟩"), true);
    }
    else {
      _minimum = 0;
      _negativeBit = truth.negate();
    }
  }

  /** Generates a binary variable that ranges between minimum and maximum */
  public BinaryVariable(String name, int minimum, int maximum, Atom truth) {
    _name = name;
    _parts = new ArrayList<Atom>();
    for (int i = 0, k = 1; k <= maximum || -k > minimum; i++, k *= 2) {
      _parts.add(new Atom(new Variable(name + "⟨" + i + "⟩"), true));
    }
    _minimum = minimum;
    _maximum = maximum;
    if (maximum < 0) _negativeBit = truth;
    else if (minimum >= 0) _negativeBit = truth.negate();
    else _negativeBit = new Atom(new Variable(name + "⟨-⟩"), true);
  }

  public int queryMinimum() {
    return _minimum;
  }

  public int queryMaximum() {
    return _maximum;
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

  public void addWelldefinednessClauses(ClauseCollection col) {
    if (col.isInMemory("binaryvar " + _name)) return;
    col.addToMemory("binaryvar " + _name);
    addMaxClauses(col);
    addMinClauses(col);
  }

  private void addMaxClauses(ClauseCollection col) {
    Atom truth = new Atom(new Variable("TRUE"), true);
    BinaryConstant max = new BinaryConstant(_maximum, truth);

    int n = length()-1;
    if (max.length() > n) n = max.length()-1;

    ArrayList<Atom> clause = new ArrayList<Atom>();
    if (_minimum < 0 && _maximum >= 0) clause.add(_negativeBit);
    for (; n >= 0; n--) {
      clause.add(queryBit(n).negate());
      if (!max.queryBit(n).equals(truth)) {
        col.addClause(new Clause(clause));
        clause.remove(clause.size()-1);
      }
    }
  }

  private void addMinClauses(ClauseCollection col) {
    Atom truth = new Atom(new Variable("TRUE"), true);
    BinaryConstant min = new BinaryConstant(_minimum, truth);

    int n = length()-1;
    if (min.length() > n) n = min.length()-1;

    ArrayList<Atom> clause = new ArrayList<Atom>();
    if (_minimum < 0 && _maximum >= 0) clause.add(_negativeBit.negate());
    for (; n >= 0; n--) {
      clause.add(queryBit(n));
      if (min.queryBit(n).equals(truth)) {
        col.addClause(new Clause(clause));
        clause.remove(clause.size()-1);
      }
    }
  }

  public String toString() {
    return _name;
  }
}

