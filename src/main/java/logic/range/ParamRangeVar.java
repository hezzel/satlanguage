package logic.range;

import logic.sat.Variable;
import logic.parameter.*;

/**
 * A ParamRangeVar -- or Parametrised Range IntegerVariable -- is a set of VariableIntegers,
 * indexed with one or more named integers parameters which are allowed to occupy a fixed range.
 */
public class ParamRangeVar extends ParametrisedObject<VariableInteger> {
  /**
   * Creates a parametrised range variable with a given list of parameters.
   * The falsehood variable should be such that it represents "false", and the truth variable
   * should represent "true".
   */
  public ParamRangeVar(String name, ParameterList params, int minimum, int maximum,
                       Variable falsehood, Variable truth) {
    super(name, params,
          makeHelper(new Parameter(name, minimum, maximum), params, falsehood, truth));
  }

  /**
   * Creates a parametrised range variable with a given list of parameters. The "count" parameter
   * defines the requirements for the IntegerVariable: its name, minimum, maximum and perhaps
   * range restriction.
   */
  public ParamRangeVar(Parameter count, ParameterList params, Variable falsehood, Variable truth) {
    super(count.queryName(), params, makeHelper(count, params, falsehood, truth));
  }

  /**
   * Helper function for the constructor: generates the helper object which the ParamTree requires
   * to construct itself.
   */
  private static ParamTree.ConstructorHelper<VariableInteger> makeHelper(Parameter count,
                                                                         ParameterList params,
                                                                         Variable falsehood,
                                                                         Variable truth) {
    return new ParamTree.ConstructorHelper<VariableInteger>() {
      /** Creates a variable for a suitable, complete combination of parameters. */
      public VariableInteger generate(Assignment args) {
        Substitution subst = new Substitution(args);
        String varname = queryObjectName(count.queryName(), params, subst);
        PExpression minimum = count.queryMinimum().substitute(subst);
        PExpression maximum = count.queryMaximum().substitute(subst);
        subst.put(count.queryName(), new ParameterExpression(varname));
        PConstraint restriction = count.queryRestriction().substitute(subst);
        Parameter p = new Parameter(varname, minimum, maximum, restriction);
        return new VariableInteger(p, falsehood, truth);
      }
    };
  }

  /**
   * This returns the variable integer corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper variable), or not
   * all the required parameters are provided, then an Error is thrown instead.
   */
  public VariableInteger queryVar(Assignment values) {
    VariableInteger ret = queryObject(values);
    if (ret == null) {
      throw new Error("Instantiation of range integer variable " + toString() + " with " + values +
        " violates restrictions: parameter values are out of range.");
    }
    return ret;
  }
}

