package language.execution;

import java.util.TreeSet;

public interface Statement {
  public static final int PRINT = 0;
  public static final int LET = 1;
  public static final int IF = 2;
  public static final int FOR = 3;
  public static final int BLOCK = 4;

  /**
   * Executes the statement in the given program state.  This may cause the state to be updated.
   */
  public void execute(ProgramState state);

  /** Returns the kind of statement this is (PRINT/IF/FOR/BLOCK). */
  public int queryKind();

  /**
   * Returns a string representation of the statement, taking indentation into account (but only
   * after a newline is printed).
   */
  public String toString(String indent);
}

