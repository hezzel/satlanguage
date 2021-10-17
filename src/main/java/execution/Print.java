package language.execution;

import java.util.ArrayList;

public class Print implements Statement {
  private ArrayList<StringExpression> _txt;

  public Print(StringExpression str) {
    _txt = new ArrayList<StringExpression>();
    _txt.add(str);
  }

  public Print(ArrayList<StringExpression> parts) {
    _txt = new ArrayList<StringExpression>(parts);
  }

  /** Handles the actual printing of a given string. */
  protected void output(String str) {
    System.out.print(str);
  }

  public void execute(ProgramState state) {
    for (int i = 0; i < _txt.size(); i++) {
      output(_txt.get(i).evaluate(state));
    }
  }

  public int queryKind() {
    return Statement.PRINT;
  }

  public String toString(String indent) {
    String ret = "print(";
    for (int i = 0; i < _txt.size(); i++) {
      if (i > 0) ret += ", ";
      ret += _txt.get(i).toString();
    }
    return ret + ")";
  }

  public String toString() { return toString(""); }
}

