package logic.parameter;

import java.util.Set;

public interface PConstraint {
  static final int CONSTANT = 0;
  static final int RELATION = 1;
  static final int AND      = 2;
  static final int OR       = 3;

  /**
   * Given a complete assignment of all parameters in the current expression, this returns the
   * value of the constraint under that assignment.
   */
  public boolean evaluate(Assignment assignment);

  /** Returns true if and only if this is the always-true constraint. */
  public boolean isTop();

  /** This returns the kind of constraint (CONSTANT, RELATION, AND, OR). */
  public int queryKind();

  /** 
   * Given a substitution, this replaces the given parameters by the substituted ones and returns
   * the result.
   */
  public PConstraint substitute(Substitution substitution);

   /** This returns the names of the parameters that are used in the current expression. */
  public Set<String> queryParameters();
}
