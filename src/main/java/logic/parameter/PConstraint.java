package logic.parameter;

import java.util.Set;

/**
 * A PConstraint describes a restriction on parameters as used by quantified formulas.
 * PConstraints are immutable.
 */
public interface PConstraint {
  static final int CONSTANT = 0;
  static final int NOT      = 1;
  static final int RELATION = 2;
  static final int AND      = 3;
  static final int OR       = 4;
  static final int OTHER    = 5;

  /**
   * Given a complete assignment of all parameters in the current expression, this returns the
   * value of the constraint under that assignment.
   */
  public boolean evaluate(Assignment assignment);

  /** Returns the negation of this constraint. */
  public PConstraint negate();

  /** Returns true if and only if this is the always-true constraint. */
  public boolean isTop();

  /** This returns the kind of constraint (CONSTANT, RELATION, AND, OR, OTHER). */
  public int queryKind();

  /** 
   * Given a substitution, this replaces the given parameters by the substituted ones and returns
   * the result.
   */
  public PConstraint substitute(Substitution substitution);

   /** This returns the names of the parameters that are used in the current expression. */
  public Set<String> queryParameters();
}
