package logic.number;

import logic.sat.Atom;
import logic.number.range.RangeConstant;
import logic.number.binary.BinaryConstant;

public class ConstantInteger implements ClosedInteger {
  private int _value;
  private Atom _truth;

  public ConstantInteger(int value, Atom truth) {
    _value = value;
    _truth = truth;
  }

  public int queryMinimum() {
    return _value;
  }

  public int queryMaximum() {
    return _value;
  }

  public int queryKind() {
    return ClosedInteger.BOTH;
  }

  public RangeConstant getRange() {
    return new RangeConstant(_value, _truth);
  }

  public BinaryConstant getBinary() {
    return new BinaryConstant(_value, _truth);
  }

  public String toString() {
    return "" + _value;
  }
}

