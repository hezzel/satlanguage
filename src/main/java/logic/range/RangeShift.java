package logic.range;

import logic.sat.*;
import logic.parameter.Assignment;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A RangeShift is an integer expression of the form a + i, where i is a constant integer and a is
 * a RangeInteger.
 */
public class RangeShift implements RangeInteger {
  private RangeInteger _ri;
  private int _shift;

  public RangeShift(RangeInteger ri, int shift) {
    _ri = ri;
    _shift = shift;
  }

  public int queryMinimum() {
    return _ri.queryMinimum() + _shift;
  }

  public int queryMaximum() {
    return _ri.queryMaximum() + _shift;
  }

  public Variable queryGeqVariable(int i) {
    return _ri.queryGeqVariable(i - _shift);
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    _ri.addWelldefinednessClauses(col);
  }

  public String toString() {
    return "(" + _ri + " ⊕" + _shift + ")";
  }
}

