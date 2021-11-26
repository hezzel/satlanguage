package logic.sat;

import java.lang.Comparable;

/** An Atom is a boolean variable or negated boolean variable. Atoms are immutable objects. */
public class Atom implements Comparable<Atom> {
  private Variable _x;
  private boolean _negated;

  public Atom(Variable x, boolean positive) {
    _x = x;
    _negated = !positive;
  }

  public int compareTo(Atom a) {
    int c = _x.compareTo(a._x);
    if (c != 0) return c;
    if (a._negated == _negated) return 0;
    if (a._negated) return -1;
    return 1;
  }

  /** Returns the corresponding variable. */
  public Variable queryVariable() {
    return _x;
  }

  /** Returns the index of the corresponding variable. */
  public int queryIndex() {
    return _x.queryIndex();
  }

  /** Returns whether the variable is negated or not. */
  public boolean queryNegative() {
    return _negated;
  }

  /** Returns the negated form of this atom, without altering the current one. */
  public Atom negate() {
    return new Atom(_x, _negated);
  }

  /** Returns a description of the atom as should be sent to a SAT solver. */
  public String getSatDescription() {
    return (_negated ? "-" : "") + _x.queryIndex();
  }

  /** Returns a human-readable description of the atom. */
  public String toString() {
    return (_negated ? "¬" : "") + _x.toString();
  }

  /** Returns an equality check corresponding to compareTo. */
  public boolean equals(Atom other) {
    return compareTo(other) == 0;
  }
}

