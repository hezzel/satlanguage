package logic.number;

import logic.sat.Atom;
import logic.number.general.ClauseAdder;
import logic.number.range.RangeConditional;
import logic.number.binary.BinaryConditional;

public class ConditionalInteger implements ClosedInteger {
  private Atom _condition;
  private ClosedInteger _value;
  private ClauseAdder _adder;
  private Atom _truth;

  public ConditionalInteger(Atom cond, ClosedInteger value, Atom truth, ClauseAdder adder) {
    _condition = cond;
    _value = value;
    _adder = adder;
    _truth = truth;
  }

  public int queryMinimum() {
    return 0 < _value.queryMinimum() ? 0 : _value.queryMinimum();
  }

  public int queryMaximum() {
    return 0 > _value.queryMaximum() ? 0 : _value.queryMaximum();
  }

  public int queryKind() {
    return _value.queryKind();
  }

  public RangeConditional getRange() {
    if (_value.queryKind() == ClosedInteger.BINARY) return null;
    return new RangeConditional(_condition, _value.getRange(), _truth, _adder);
  }

  public BinaryConditional getBinary() {
    if (_value.queryKind() == ClosedInteger.RANGE) return null;
    return new BinaryConditional(_condition, _value.getBinary(), _truth, _adder);
  }

  public String toString() {
    return _condition.toString() + "?" + _value.toString();
  }
}

