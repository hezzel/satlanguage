package logic.number;

import logic.sat.Atom;
import logic.number.range.RangeInteger;
import logic.number.range.RangeConstant;
import logic.number.range.RangePlus;
import logic.number.range.RangeShift;
import logic.number.binary.BinaryInteger;
import logic.number.binary.BinaryConstant;
import logic.number.binary.BinaryPlus;

public class PlusInteger implements ClosedInteger {
  private ClosedInteger _left;
  private ClosedInteger _right;
  private int _kind;
  Atom _truth;

  public PlusInteger(ClosedInteger left, ClosedInteger right, int kind, Atom truth) {
    _kind = kind;
    _left = left;
    _right = right;
    _truth = truth;
    if ( (kind == ClosedInteger.BOTH && _left.queryKind() != ClosedInteger.BOTH) ||
         (kind == ClosedInteger.BOTH && _right.queryKind() != ClosedInteger.BOTH) ||
         (kind == ClosedInteger.RANGE && _left.queryKind() == ClosedInteger.BINARY) ||
         (kind == ClosedInteger.RANGE && _right.queryKind() == ClosedInteger.BINARY) ||
         (kind == ClosedInteger.BINARY && _left.queryKind() == ClosedInteger.RANGE) ||
         (kind == ClosedInteger.BINARY && _right.queryKind() == ClosedInteger.RANGE) ) {
      throw new Error("Cannot add " + _left.toString() + " to " + _right.toString() + " with " +
        "kind " + kind + ": children have kinds " + _left.queryKind() + " and " +
        _right.queryKind() + ".");
    }
  }

  public int queryMinimum() {
    return _left.queryMinimum() + _right.queryMinimum();
  }

  public int queryMaximum() {
    return _left.queryMaximum() + _right.queryMaximum();
  }

  public int queryKind() {
    return _kind;
  }

  public RangeInteger getRange() {
    if (_kind == ClosedInteger.BINARY) return null;
    if (_left.queryMinimum() == _left.queryMaximum()) {
      if (_right.queryMinimum() == _right.queryMaximum()) {
        return new RangeConstant(_left.queryMinimum() + _right.queryMinimum(), _truth);
      }
      return new RangeShift(_right.getRange(), _left.queryMinimum());
    }
    else if (_right.queryMinimum() == _right.queryMaximum()) {
      return new RangeShift(_left.getRange(), _right.queryMinimum());
    }
    return new RangePlus(_left.getRange(), _right.getRange());
  }

  public BinaryInteger getBinary() {
    if (_kind == ClosedInteger.RANGE) return null;
    if (_left.queryMinimum() == _left.queryMaximum() &&
        _right.queryMinimum() == _right.queryMaximum()) {
      return new BinaryConstant(_left.queryMinimum() + _right.queryMinimum(), _truth);
    }
    return new BinaryPlus(_left.getBinary(), _right.getBinary(), _truth);
  }

  public String toString() {
    if (_kind == ClosedInteger.RANGE) return _left.toString() + " ⊕ " + _right.toString();
    if (_kind == ClosedInteger.BINARY) return _left.toString() + " ⊞ " + _right.toString();
    return _left.toString() + " + " + _right.toString();
  }
}

