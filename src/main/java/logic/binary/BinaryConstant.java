package logic.binary;

import logic.sat.Atom;
import logic.sat.ClauseCollection;
import java.util.ArrayList;

/** A binary constant represents a constant integer, with fixed atoms for the bits. */
public class BinaryConstant implements BinaryInteger {
  private int _value;
  private ArrayList<Atom> _parts;
  private Atom _negativeBit;

  public BinaryConstant(int value, Atom truth) {
    _value = value;
    _parts = new ArrayList<Atom>();

    // for negative values, the binary representation is that of NOT (-value-1); hence, we
    // construct the binary representation of -value-1 but with swapped atoms
    Atom one, zero;
    if (_value >= 0) {
      one = truth;
      zero = truth.negate();
    }
    else {
      one = truth.negate();
      zero = truth;
      value = -value-1;
    }

    while (value > 0) {
      if (value % 2 == 1) _parts.add(one);
      else _parts.add(zero);
      value /= 2;
    }
    _negativeBit = zero;
  }


  public int queryMinimum() {
    return _value;
  }

  public int queryMaximum() {
    return _value;
  }

  public int length() {
    return _parts.size();
  }

  public BinaryConstant setPracticalBounds(int newmin, int newmax) {
    return this;
  }

  public Atom queryBit(int i) {
    if (i < _parts.size()) return _parts.get(i);
    return _negativeBit;
  }

  public Atom queryNegativeBit() {
    return _negativeBit;
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    // nothing to add
  }

  public String toString() {
    return "" + _value;
  }
}

