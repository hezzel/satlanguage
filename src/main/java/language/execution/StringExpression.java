package language.execution;

import logic.parameter.PExpression;
import java.util.ArrayList;

public class StringExpression {
  private String _a;
  private PExpression _b;

  public StringExpression(String str) {
    _a = str;
    _b = null;
  }

  public StringExpression(PExpression expr) {
    _a = null;
    _b = expr;
  }

  /** Returns the value of the string expression in the given program state. */
  public String evaluate(ProgramState state) {
    if (_a != null) return _a.replace("\\n", "\n");
    return "" + state.evaluate(_b);
  }

  public String toString() {
    if (_a != null) return "\"" + _a + "\"";
    else return _b.toString();
  }
}

