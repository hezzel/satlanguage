package language.execution;

import logic.parameter.PExpression;
import logic.parameter.TrueConstraint;
import logic.parameter.Parameter;
import java.util.ArrayList;

public class Let implements Statement {
  private String _parameter;
  private PExpression _value;

  public Let(String parameter, PExpression value) {
    _parameter = parameter;
    _value = value;
  }

  public void execute(ProgramState state) {
    int value = state.evaluate(_value);
    state.put(_parameter, value);
  }

  public int queryKind() {
    return Statement.LET;
  }

  public String toString(String indent) {
    return indent + _parameter + " := " + _value.toString();
  }

  public String toString() { return toString(""); }
}

