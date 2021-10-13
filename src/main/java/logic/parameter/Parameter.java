package logic.parameter;

/**
 * A Parameter describes an integer variable for bounded quantification.
 * Parameters are immutable.
 */
public class Parameter {
  private String _name;
  private PExpression _min;
  private PExpression _max;
  private PConstraint _restriction;

  /**
   * Create a parameter by the given name, which ranges from min to max (inclusive).
   * The parameter does not come with a restriction.
   */
  public Parameter(String name, int min, int max) {
    _name = name;
    _min = new ConstantExpression(min);
    _max = new ConstantExpression(max);
    _restriction = new TrueConstraint();
  }

  /**
   * Create a parameter by the given name, which ranges from min to max (inclusive).
   * The given restriction is not allowed to be null.
   */
  public Parameter(String name, int min, int max, PConstraint restriction) {
    _name = name;
    _min = new ConstantExpression(min);
    _max = new ConstantExpression(max);
    if (restriction == null) throw new Error("Parameter created with null-constraint.");
    else _restriction = restriction;
  }

  /**
   * Create a parameter by the given name, which ranges from min to max (inclusive).
   * The given restriction and expressions are not allowed to be null.
   */
  public Parameter(String name, PExpression min, PExpression max, PConstraint restriction) {
    _name = name;
    _min = min;
    _max = max;
    _restriction = restriction;
    if (min == null || max == null || restriction == null) {
      throw new Error("Parameter created with null argument.");
    }
  }

  public String queryName() { return _name; }
  public PExpression queryMinimum() { return _min; }
  public PExpression queryMaximum() { return _max; }
  public PConstraint queryRestriction() { return _restriction; }

  public String toString() {
    if (_restriction.isTop()) return _name + " ∈ {" + _min + ".." + _max + "}";
    else return _name + " ∈ {" + _min + ".." + _max + "} with " + _restriction;
  }
}

