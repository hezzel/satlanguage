package language.execution;

import logic.parameter.PExpression;
import java.util.ArrayList;

public class StringExpression {
  private String _a;
  private PExpression _b;
  private StringFunction _func;
  private ArrayList<PExpression> _funcArgs;

  public StringExpression(String str) {
    _a = str;
    _b = null;
    _func = null;
  }

  public StringExpression(PExpression expr) {
    _a = null;
    _b = expr;
    _func = null;
  }

  public StringExpression(StringFunction func, ArrayList<PExpression> funcArgs) {
    _a = null;
    _b = null;
    _func = func;
    _funcArgs = new ArrayList<PExpression>(funcArgs);
  }

  /** Returns the value of the string expression in the given program state. */
  public String evaluate(ProgramState state) {
    if (_a != null) return _a.replace("\\n", "\n");
    if (_b != null) return "" + state.evaluate(_b);
    ArrayList<Integer> parts = new ArrayList<Integer>();
    for (int i = 0; i < _funcArgs.size(); i++) parts.add(state.evaluate(_funcArgs.get(i)));
    return _func.lookup(parts);
  }

  public String toString() {
    if (_a != null) return "\"" + _a + "\"";
    if (_b != null) return _b.toString();
    String ret = _func.queryName() + "(";
    for (int i = 0; i < _funcArgs.size(); i++) {
      if (i > 0) ret += ",";
      ret += _funcArgs.get(i).toString();
    }
    return ret + ")";
  }
}

