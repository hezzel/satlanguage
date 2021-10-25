package logic.range;

import logic.sat.*;
import logic.parameter.Assignment;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * An integer expression of the form a + b, but with bounds: if the true sum is bigger than the
 * given maximum, then the value of the expresison is set to the maximum; and similar if the true
 * sum is smaller than the given minimum.
 */
public class RangePlus implements RangeInteger {
  private RangeInteger _left;
  private RangeInteger _right;
  private int _minimum;
  private int _maximum;
  private TreeMap<Integer,Variable> _vars;

  /**
   * a + b where the given minimum and maximum are exactly the true minimum and maximum of a + b.
   */
  public RangePlus(RangeInteger left, RangeInteger right) {
    if (left.toString().compareTo(right.toString()) < 0) {
      _left = left;
      _right = right;
    }
    else {
      _left = right;
      _right = left;
    }
    _minimum = _left.queryMinimum() + _right.queryMinimum();
    _maximum = _left.queryMaximum() + _right.queryMaximum();
    _vars = null;
  }

  /** a + b where the minimum and maximum are bounded by the given values. */
  public RangePlus(RangeInteger left, RangeInteger right, int min, int max) {
    if (left.toString().compareTo(right.toString()) < 0) {
      _left = left;
      _right = right;
    }
    else {
      _left = right;
      _right = left;
    }
    _minimum = _left.queryMinimum() + _right.queryMinimum();
    if (min > _minimum) _minimum = min;
    _maximum = _left.queryMaximum() + _right.queryMaximum();
    if (max < _maximum) _maximum = max;
    _vars = null;
  }

  private void setupVars() {
    _vars = new TreeMap<Integer,Variable>();
    for (int i = _maximum; i > _minimum; i--) {
      _vars.put(i, new Variable(_left.toString() + "⊕" + _right.toString() + "≥" + i));
    }
  }

  public int queryMinimum() {
    return _minimum;
  }

  public int queryMaximum() {
    return _maximum;
  }

  public Variable queryGeqVariable(int i) {
    if (i <= _minimum) return _left.queryGeqVariable(_left.queryMinimum());     // TRUE
    if (i > _maximum) return _right.queryGeqVariable(_right.queryMaximum()+1);  // FALSE
    if (_vars == null) setupVars();
    return _vars.get(i);
  }

  /**
   * Add clauses indicating that the geq variables truly represent the (bounded) sum of _left and
   * _right.
   */
  public void addWelldefinednessClauses(ClauseCollection col) {
    if (col.isInMemory("sum: " + toString())) return;
    col.addToMemory("sum: " + toString());

    _left.addWelldefinednessClauses(col);
    _right.addWelldefinednessClauses(col);
    if (_vars == null) setupVars();
    ArrayList<Atom> parts;

    // for all i, j: left ≥ i ∧ right ≥ j → sum ≥ i + j, so bsum ≥ MIN(i+j, _maximum)
    for (int i = _left.queryMinimum(); i <= _left.queryMaximum(); i++) {
      for (int j = _right.queryMinimum(); j <= _right.queryMaximum(); j++) {
        if (i + j <= _minimum) continue;    // in this case sum ≥ i + j is satisfied anyway
        parts = new ArrayList<Atom>();
        if (i > _left.queryMinimum()) parts.add(new Atom(_left.queryGeqVariable(i), false));
        if (j > _right.queryMinimum()) parts.add(new Atom(_right.queryGeqVariable(j), false));
        if (i + j > _maximum) parts.add(new Atom(queryGeqVariable(_maximum), true));
        else parts.add(new Atom(queryGeqVariable(i + j), true));
        col.addClause(new Clause(parts));
      }
    }

    // for all i, j: left < i+1 ∧ right < j+1 → sum < i + j + 1, so bsum < MAX(i+j, _minimum)+1
    for (int i = _left.queryMinimum(); i <= _left.queryMaximum(); i++) {
      for (int j = _right.queryMinimum(); j <= _right.queryMaximum(); j++) {
        if (i + j >= _maximum) continue;    // busm <= _maximum <= i+j is satisfied anyway
        parts = new ArrayList<Atom>();
        if (i < _left.queryMaximum()) parts.add(new Atom(_left.queryGeqVariable(i+1), true));
        if (j < _right.queryMaximum()) parts.add(new Atom(_right.queryGeqVariable(j+1), true));
        if (i + j < _minimum) parts.add(new Atom(queryGeqVariable(_minimum+1), false));
        else parts.add(new Atom(queryGeqVariable(i + j + 1), false));
        col.addClause(new Clause(parts));
      }
    }
  }

  public String toString() {
    return "bplus(" + _minimum + ", " + _maximum + ", " + _left.toString() + " ⊕ " +
           _right.toString() + ")";
  }
}

