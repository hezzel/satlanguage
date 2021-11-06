package logic.number;

import logic.number.range.RangeVariable;
import logic.number.binary.BinaryVariable;

public class VariableInteger implements ClosedInteger {
  private RangeVariable _r;
  private BinaryVariable _b;

  public VariableInteger(RangeVariable ri) {
    _r = ri;
    _b = null;
  }

  public VariableInteger(BinaryVariable bi) {
    _r = null;
    _b = bi;
  }

  public int queryMinimum() {
    return _r == null ? _b.queryMinimum() : _r.queryMinimum();
  }

  public int queryMaximum() {
    return _r == null ? _b.queryMaximum() : _r.queryMaximum();
  }

  public int queryKind() {
    return _r == null ? ClosedInteger.BINARY : ClosedInteger.RANGE;
  }

  public RangeVariable getRange() {
    return _r;
  }

  public BinaryVariable getBinary() {
    return _b;
  }

  public String toString() {
    return _r == null ? _b.toString() : _r.toString();
  }
}

