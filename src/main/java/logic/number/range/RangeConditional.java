package logic.number.range;

import logic.sat.*;
import logic.number.general.ClauseAdder;
import java.util.TreeMap;

/**
 * An expression of the form if <atom> then <value> else 0
 * The expression can also optionally be given bounds, so that it is increased to the given minimum
 * if necessary, and lowered to the given maximum.
 */
public class RangeConditional implements RangeInteger {
  private Atom _condition;
  private RangeInteger _value;
  private Atom _truth;
  private int _minimum;
  private int _maximum;
  private TreeMap<Integer,Variable> _variables;
  private ClauseAdder _adder;

  /** A shared helper for all the constructors. */
  private void setup(Atom cond, RangeInteger value, Atom truth, ClauseAdder adder) {
    _condition = cond;
    _value = value;
    _truth = truth;
    _minimum = 0 < value.queryMinimum() ? 0 : value.queryMinimum();
    _maximum = 0 < value.queryMaximum() ? value.queryMaximum() : 0;
    _adder = adder;
    _variables = null;
  }

  /**
   * Creates the expression: if cond then value else 0.
   * The ClauseAdder is essentially a function pointer that is called when the well-definedness
   * clauses have to be generated.  This should be used in the event that well-definedness clauses
   * should be added for cond.  (We use a function pointer here mostly to avoid a dependency on
   * Formula, which occurs in a different package).  If adder == null, then no additional
   * well-definedness clauses will be added on top of those defining the RangeConditional.
   */
  public RangeConditional(Atom cond, RangeInteger value, Atom truth, ClauseAdder adder) {
    setup(cond, value, truth, adder);
  }

  public RangeConditional(Atom cond, RangeInteger value, Atom truth, int min, int max,
                          ClauseAdder adder) {
    setup(cond, value, truth, adder);
    if (min > _minimum) _minimum = min;
    if (max < _maximum) _maximum = max;
    if (_minimum > _maximum) _minimum = _maximum;
  }

  public int queryMinimum() {
    return _minimum;
  }

  public int queryMaximum() {
    return _maximum;
  }

  public RangeInteger setPracticalBounds(int newmin, int newmax) {
    if (newmin <= _minimum && newmax >= _maximum) return this;
    return new RangeConditional(_condition, _value, _truth, newmin, newmax, _adder);
  }

  private void makeVariables() {
    _variables = new TreeMap<Integer,Variable>();
    // we create one variable for each i in {minvalue+1..maxvalue}
    int minval = _value.queryMinimum();
    int maxval = _value.queryMaximum();
    if (minval < _minimum) minval = _minimum;
    if (maxval > _maximum) maxval = _maximum;
    for (int i = minval+1; i <= maxval; i++) {
      _variables.put(i, new Variable(_condition.toString() + "?" + _value.toString() + "≥" + i));
    }
  }

  public Atom queryGeqAtom(int i) {
    if (i <= _minimum) return _truth;
    if (i > _maximum) return _truth.negate();

    // if 0 < i ≤ minvalue, then x?value ≥ i if and only if x holds (since 0 ≥ i does not hold)
    if (0 < i && i <= _value.queryMinimum()) return _condition;
    // if maxvalue < i <= 0, then x?value ≥ i if and only if x does not hold (since 0 ≥ i holds)
    if (_value.queryMaximum() < i && i <= 0) return _condition.negate();
    
    // otherwise, we use one of the dedicated variables
    if (_variables == null) makeVariables();
    return new Atom(_variables.get(i), true);
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    if (col.isInMemory("conditional: " + toString())) return;
    col.addToMemory("conditional: " + toString());

    if (_adder != null) _adder.add(col);

    if (_variables == null) makeVariables();
    int minval = _value.queryMinimum();
    int maxval = _value.queryMaximum();
    if (minval < _minimum) minval = _minimum;
    if (maxval > _maximum) maxval = _maximum;
    for (int i = minval + 1; i <= maxval; i++) {
      // if i > 0 we have: x?value ≥ i <-> x ∧ value ≥ i
      Atom geqi = new Atom(_variables.get(i), true);
      if (i > 0) {
        col.addClause(new Clause(geqi.negate(), _condition));
        col.addClause(new Clause(geqi.negate(), _value.queryGeqAtom(i)));
        col.addClause(new Clause(_condition.negate(), _value.queryGeqAtom(i).negate(), geqi));
      }
      // if i <= 0 we have: x?value ≥ i <-> ¬x ∨ value ≥ i
      if (i <= 0) {
        col.addClause(new Clause(geqi.negate(), _condition.negate(), _value.queryGeqAtom(i)));
        col.addClause(new Clause(_condition, geqi));
        col.addClause(new Clause(_value.queryGeqAtom(i).negate(), geqi));
      }
    }
  }

  public String toString() {
    if (_minimum > 0 || _minimum > _value.queryMinimum() ||
        _maximum < 0 || _maximum < _value.queryMaximum()) {
      return "cond(" + _minimum + ", " + _maximum + ", " + _condition.toString() + "?" +
             _value.toString() + ")";
    }
    return _condition.toString() + "?" + _value.toString();
  }
}

