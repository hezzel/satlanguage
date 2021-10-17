package language.execution;

import logic.sat.Variable;
import logic.sat.Solution;
import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import logic.parameter.Assignment;
import logic.parameter.ParameterList;
import logic.parameter.ParamBoolVar;

import java.util.Set;
import java.util.ArrayList;

public class ProgramState extends Assignment {
  private Solution _solution;

  public ProgramState(Solution sol) {
    _solution = sol;
  }

  public boolean queryValue(Variable x) {
    return _solution.check(x);
  }

  public boolean queryValue(ParamBoolVar x, ArrayList<PExpression> values) {
    ParameterList params = x.queryParameters();
    Assignment ass = new Assignment();
    for (int i = 0; i < params.size(); i++) {
      ass.put(params.get(i).queryName(), evaluate(values.get(i)));
    }
    return _solution.check(x.queryVar(ass));
  }

  /**
   * Throws an appropriate error message when trying to evaluate an expression that still has
   * uninitialised parameters in it.
   */
  public int evaluate(PExpression expr) {
    Set<String> params = expr.queryParameters();
    for (String p : params) {
      if (!defines(p)) {
        throw new UndeclaredVariableError("Expression " + expr + " uses unitialised parameter " +
                                          p);
      }
    }
    return expr.evaluate(this);
  }

  public boolean evaluate(PConstraint constr) {
    Set<String> params = constr.queryParameters();
    for (String p : params) {
      if (!defines(p)) {
        throw new UndeclaredVariableError("Constraint " + constr + " uses unitialised parameter " +
                                          p);
      }
    }
    return constr.evaluate(this);
  }
}
