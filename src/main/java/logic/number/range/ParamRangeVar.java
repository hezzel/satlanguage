package logic.number.range;

import logic.sat.Atom;
import logic.sat.ClauseCollection;
import logic.parameter.*;

/**
 * A ParamRangeVar -- or Parametrised Range IntegerVariable -- is a set of RangeVariables,
 * indexed with one or more named integers parameters which are allowed to occupy a fixed range.
 */
public class ParamRangeVar extends ParametrisedObject<RangeVariable> {
  private String _rangeDesc;

  /**
   * Creates a parametrised range variable with a given list of parameters.
   * The truth atom should be such that it represents "true" (so should be forced to hold in the
   * SAT requirements).
   */
  public ParamRangeVar(String name, ParameterList params, int minimum, int maximum, Atom truth) {
    super(name, params, makeHelper(new Parameter(name, minimum, maximum), params, truth));
    _rangeDesc = "{" + minimum + ".." + maximum + "}";
  }

  /**
   * Creates a parametrised range variable with a given list of parameters. The "count" parameter
   * defines the requirements for the IntegerVariable: its name, minimum, maximum and perhaps
   * range restriction.
   */
  public ParamRangeVar(Parameter count, ParameterList params, Atom truth) {
    super(count.queryName(), params, makeHelper(count, params, truth));
    _rangeDesc = "{" + count.queryMinimum() + ".." + count.queryMaximum() + "}";
    if (!count.queryRestriction().isTop()) _rangeDesc += " with " + count.queryRestriction();
  }

  /**
   * Helper function for the constructor: generates the helper object which the ParamTree requires
   * to construct itself.
   */
  private static ParamTree.ConstructorHelper<RangeVariable> makeHelper(Parameter count,
                                                                         ParameterList params,
                                                                         Atom truth) {
    return new ParamTree.ConstructorHelper<RangeVariable>() {
      /** Creates a variable for a suitable, complete combination of parameters. */
      public RangeVariable generate(Assignment args) {
        Substitution subst = new Substitution(args);
        String varname = queryObjectName(count.queryName(), params, subst);
        PExpression minimum = count.queryMinimum().substitute(subst);
        PExpression maximum = count.queryMaximum().substitute(subst);
        subst.put(count.queryName(), new ParameterExpression(varname));
        PConstraint restriction = count.queryRestriction().substitute(subst);
        Parameter p = new Parameter(varname, minimum, maximum, restriction);
        return new RangeVariable(p, truth);
      }
    };
  }

  /**
   * This returns the variable integer corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper variable), or not
   * all the required parameters are provided, then an Error is thrown instead.
   */
  public RangeVariable queryVar(Assignment values) {
    RangeVariable ret = queryObject(values);
    if (ret == null) {
      throw new Error("Instantiation of range integer variable " + toString() + " with " + values +
        " violates restrictions: parameter values are out of range.");
    }
    return ret;
  }

  public String queryRangeDescription() {
    return _rangeDesc;
  }

  private void addWelldefinednessClauses(int i, Assignment sigma, ClauseCollection col) {
    if (i >= _parameters.size()) queryVar(sigma).addWelldefinednessClauses(col);
    else {
      int min = _parameters.get(i).queryMinimum().evaluate(sigma);
      int max = _parameters.get(i).queryMaximum().evaluate(sigma);
      for (int k = min; k <= max; k++) {
        sigma.put(_parameters.get(i).queryName(), k);
        if (_parameters.get(i).queryRestriction().evaluate(sigma)) {
          addWelldefinednessClauses(i+1, sigma, col);
        }
        sigma.remove(_parameters.get(i).queryName());
      }
    }
  }

  /** Adds clauses to col indicating that all instances are well-defined variables. */
  public void addWelldefinednessClauses(ClauseCollection col) {
    addWelldefinednessClauses(0, new Assignment(), col);
  }
}

