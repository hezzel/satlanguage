package logic.range;

import logic.sat.*;
import logic.parameter.Parameter;
import logic.parameter.Assignment;

import java.util.TreeMap;

public class RangeVariable implements RangeInteger {
  private String _name;
  private int _minimum;
  private int _maximum;
  private Variable _truevar;
  private TreeMap<Integer,Variable> _variables;
  private String _rangeDesc;

  public RangeVariable(Parameter range, Variable truevar) {
    _name = range.queryName();
    _truevar = truevar;
    _minimum = range.queryMinimum().evaluate(null);
    _maximum = range.queryMaximum().evaluate(null);
    _variables = new TreeMap<Integer,Variable>();

    // find true minimum and maximum
    Assignment ass = new Assignment();
    for (; _maximum >= _minimum; _maximum--) {
      ass.put(_name, _maximum);
      if (range.queryRestriction().evaluate(ass)) break;
    }
    for (; _minimum <= _maximum; _minimum++) {
      ass.put(_name, _minimum);
      if (range.queryRestriction().evaluate(ass)) break;
    }
    if (_minimum > _maximum) {
      throw new Error("Trying to declare range integer variable " + _name + " with empty range.");
    }

    // determine range description
    _rangeDesc = "{" + _minimum + ".." + _maximum + "}";
    if (!range.queryRestriction().isTop()) _rangeDesc += " with " + range.queryRestriction();

    // create variables
    Variable lastinrange = null;
    for (int i = _maximum; i > _minimum; i--) {
      ass.put(_name, i);
      if (range.queryRestriction().evaluate(ass)) lastinrange = new Variable(_name + "≥" + i);
      _variables.put(i, lastinrange);
    }
  }

  public RangeVariable(String name, int minimum, int maximum, Variable truevar) {
    _name = name;
    _minimum = minimum;
    _maximum = maximum;
    _truevar = truevar;
    _rangeDesc = "{" + minimum + ".." + maximum + "}";
    _variables = new TreeMap<Integer,Variable>();
    for (int i = maximum; i > _minimum; i--) {
      _variables.put(i, new Variable(name + "≥" + i));
    }
  }

  public int queryMinimum() {
    return _minimum;
  }

  public int queryMaximum() {
    return _maximum;
  }

  public RangeInteger setPracticalBounds(int newmin, int newmax) {
    return this;
  }

  public Atom queryGeqAtom(int i) {
    if (_minimum >= i) return new Atom(_truevar, true);
    if (i > _maximum) return new Atom(_truevar, false);
    return new Atom(_variables.get(i), true);
  }

  /** Add clauses indicating that x ≥ i → x ≥ i-1 where necessary. */
  public void addWelldefinednessClauses(ClauseCollection col) {
    // avoid adding the same clauses twice
    if (col.isInMemory("rangevar " + _name)) return;
    col.addToMemory("rangevar " + _name);
    // add the clauses!
    for (int i = _maximum; i > _minimum+1; i--) {
      Variable xi = _variables.get(i), xj = _variables.get(i-1);
      if (xi != xj) col.addClause(new Clause(new Atom(xi, false), new Atom(xj, true)));
    }
  }

  /**
   * Returns the value of the current integer variable under the given solution.
   * The solution is assumed to satisfy the well-definedness clauses.
   */
  public int getValue(Solution solution) {
    for (int i = _minimum + 1; i <= _maximum; i++) {
      if (!solution.check(_variables.get(i))) return i - 1;
    }
    return _maximum;
  }

  /** Returns a string representation of the range this variable occupies. */
  public String queryRangeDescription() {
    return _rangeDesc;
  }

  /** Returns the name of the variable. */
  public String toString() {
    return _name;
  }
}

