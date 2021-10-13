package logic.parameter;

import logic.sat.Variable;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A ParamBoolVar -- or Parametrised Boolean Variable -- is actually a set of variables, indexed
 * with one or more named integers parameters which are allowed to occupy a fixed range.
 */
public class ParamBoolVar {
  protected String _varname;
  protected ParameterList _parameters;
  protected ParamTree<Variable> _variables;
  protected Variable _falsehood;

  /**
   * Creates a parametrised boolean variable with a given list of parameters.
   * Note that if there is already a boolean variable with any of the names name[par1,...,parn],
   * then an Error will be thrown (since in that case it is not possible to create the
   * corresponding boolean variables).
   * The falsehood variable should be such that it represents "false".
   */
  public ParamBoolVar(String name, Variable falsehood, ParameterList params) {
    _varname = name;
    _parameters = params;
    ParamTree.ConstructorHelper<Variable> helper = new ParamTree.ConstructorHelper<Variable>() {
      public Variable generate(Assignment args) {
        return generateVariable(args);
      }
    };
    _variables = new ParamTree<Variable>(params, helper);
    _falsehood = falsehood;
  }

  /**
   * Returns the variable name where, in place of each parameter, the substituted expression is
   * printed.  If a parameter is not in the substitution, then its name is printed directly.
   */
  protected String queryVariableName(Substitution substitution) {
    String name = _varname + "[";
    for (int i = 0; i < _parameters.size(); i++) {
      PExpression expr = substitution.get(_parameters.get(i).queryName());
      String arg;
      if (expr == null) arg = _parameters.get(i).queryName();
      else arg = expr.toString();
      if (i > 0) name += "," + arg;
      else name += arg;
    }
    return name + "]";
  }

  /** Creates a variable for a suitable, complete combination of parameters. */
  private Variable generateVariable(Assignment args) {
    Substitution subst = new Substitution(args);
    String name = queryVariableName(subst);
    return new Variable(name);
  }

  /** Returns the parameter list for this variable. */
  public ParameterList queryParameters() {
    return _parameters;
  }

  /**
   * This returns the variable corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper variable), then
   * this will instead return a variable representing FALSE.
   * If not all required parameters are provided, then an Error is thrown instead.
   */
  public Variable queryVar(Assignment values) {
    Variable ret = _variables.lookup(values);
    if (ret == null) ret = _falsehood;
    return ret;
  }

  /**
   * Returns a string representation where the given values are printed in place of the parameter
   * positions of the variable; if a parameter value is not given, then the name of the parameter
   * is printed instead.
   */
  public String toString(ArrayList<PExpression> parameterValues) {
    Substitution subst = new Substitution();
    for (int i = 0; i < _parameters.size(); i++) {
      subst.put(_parameters.get(i).queryName(), parameterValues.get(i));
    }
    return queryVariableName(subst);
  }

  /**
   * Returns a string representation where the parameters are replaced by their substituted
   * expression.
   */
  public String toString(Substitution subst) {
    return queryVariableName(subst);
  }

  public String toString() {
    return queryVariableName(new Substitution());
  }
}

