package language.execution;

import logic.parameter.PExpression;
import logic.parameter.TrueConstraint;
import logic.parameter.Parameter;
import java.util.ArrayList;

public class For implements Statement {
  private Parameter _parameter;
  private Statement _statement;

  public For(String counter, PExpression minimum, PExpression maximum, Statement statement) {
    _parameter = new Parameter(counter, minimum, maximum, new TrueConstraint());
    _statement = statement;
  }

  public void execute(ProgramState state) {
    int min = state.evaluate(_parameter.queryMinimum());
    int max = state.evaluate(_parameter.queryMaximum());
    for (int i = min; i <= max; i++) {
      state.put(_parameter.queryName(), i);
      boolean constr = state.evaluate(_parameter.queryRestriction());
      if (constr) _statement.execute(state);
    }
  }

  public int queryKind() {
    return Statement.FOR;
  }

  public String toString(String indent) {
    String ret = "for " + _parameter.queryName() + " := " + _parameter.queryMinimum().toString() +
      " to " + _parameter.queryMaximum().toString() + " do";
    if (_statement.queryKind() == Statement.IF || _statement.queryKind() == Statement.FOR) {
      ret += "\n  " + indent + _statement.toString(indent + "  ");
    }
    else ret += " " + _statement.toString(indent);
    return ret;
  }

  public String toString() { return toString(""); }
}

