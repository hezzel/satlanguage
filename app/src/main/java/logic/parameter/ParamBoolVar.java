package logic.parameter;

import logic.sat.Variable;

/**
 * A ParamBoolVar -- or Parametrised Boolean Variable -- is actually a set of variables, indexed
 * with one or more named integers parameters which are allowed to occupy a fixed range.
 */
public class ParamBoolVar extends ParametrisedObject<Variable> {
  protected Variable _falsehood;

  /**
   * Creates a parametrised boolean variable with a given list of parameters.
   * The falsehood variable should be such that it represents "false".
   */
  public ParamBoolVar(String name, ParameterList params, Variable falsehood) {
    super(name, params, makeHelper(name, params, falsehood));
    _falsehood = falsehood;
  }

  /**
   * Helper function for the constructor: generates the helper object which the ParamTree requires
   * to construct itself.
   */
  private static ParamTree.ConstructorHelper<Variable> makeHelper(String name,
                                                                  ParameterList params,
                                                                  Variable falsehood) {
    return new ParamTree.ConstructorHelper<Variable>() {
      /** Creates a variable for a suitable, complete combination of parameters. */
      public Variable generate(Assignment args) {
        Substitution subst = new Substitution(args);
        String varname = queryObjectName(name, params, subst);
        return new Variable(varname);
      }
    };
  }

  /**
   * This returns the variable corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper variable), then
   * this will instead return a variable representing FALSE.
   * If not all required parameters are provided, then an Error is thrown instead.
   */
  public Variable queryVar(Assignment values) {
    Variable ret = queryObject(values);
    if (ret == null) ret = _falsehood;
    return ret;
  }
}

