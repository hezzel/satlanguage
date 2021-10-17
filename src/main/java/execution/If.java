package language.execution;

import logic.parameter.PExpression;
import logic.parameter.PConstraint;

public class If implements Statement {
  private PConstraint _constraint;
  private Statement _thenstatement;
  private Statement _elsestatement;

  /** Only the else statement (e) is allowed to be null. */
  public If(PConstraint c, Statement t, Statement e) {
    _constraint = c;
    _thenstatement = t;
    _elsestatement = e;
  }

  public void execute(ProgramState state) {
    if (state.evaluate(_constraint)) _thenstatement.execute(state);
    else {
      if (_elsestatement != null) _elsestatement.execute(state);
    }
  }

  public int queryKind() {
    return Statement.IF;
  }

  public String toString(String indent) {
    String ind = indent + "  ";
    String ret = "if " + _constraint + " then";
    if (_thenstatement.queryKind() == Statement.IF ||
        _thenstatement.queryKind() == Statement.FOR) {
      ret += "\n" + ind + _thenstatement.toString(ind);
    }
    else ret += " " + _thenstatement.toString(indent);
    if (_elsestatement != null) {
      ret += "\n" + indent + "else";
      if (_elsestatement.queryKind() == Statement.IF ||
          _elsestatement.queryKind() == Statement.FOR) {
        ret += "\n" + ind + _elsestatement.toString(ind);
      }
      else ret += " " + _elsestatement.toString(indent);
    }
    return ret;
  }

  public String toString() { return toString(""); }

  // for unit testing purposes
  public Statement queryThenStatement() { return _thenstatement; }
  public Statement queryElseStatement() { return _elsestatement; }
}

