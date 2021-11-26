package language.execution;

import java.util.ArrayList;

public class Block implements Statement {
  private ArrayList<Statement> _statements;

  public Block(ArrayList<Statement> statements) {
    _statements = new ArrayList<Statement>(statements);
  }

  public void execute(ProgramState state) {
    for (int i = 0; i < _statements.size(); i++) {
      _statements.get(i).execute(state);
    }
  }

  public int queryKind() {
    return Statement.BLOCK;
  }

  public String toString(String indent) {
    String ret = "{\n";
    String newindent = indent + "  ";
    for (int i = 0; i < _statements.size(); i++) {
      ret += newindent + _statements.get(i).toString(newindent) + "\n";
    }
    return ret + indent + "}";
  }

  public String toString() { return toString(""); }
}

