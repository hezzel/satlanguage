package logic.parameter;

import java.util.Set;

/**
 * A PExpression defines an integer function based on a set of integer parameters, as used by
 * quantified formulas.  PExpressions are immutable.
 */
public interface PExpression {
  static final int CONSTANT  = 0;
  static final int PARAMETER = 1;
  static final int MINIMUM   = 2;
  static final int MAXIMUM   = 3;
  static final int FUNCTION  = 4;
  static final int PRODUCT   = 5;
  static final int DIVISION  = 6;
  static final int MODULO    = 7;
  static final int SUM       = 8;

  /**
   * Given a complete assignment of all parameters in the current expression, this returns the
   * value of the expression under that assignment.
   * In the case of an expession without any parameters, assignment is allowed to be null; but if
   * there are parameters this will yield an Error.
   */
  public int evaluate(Assignment assignment);

  /**
   * Given a substitution, this replaces the given parameters by the substituted ones and returns
   * the result.
   */
  public PExpression substitute(Substitution substitution);

  /**
   * Returns true if the current expression is an integer constant, false otherwise.
   * Note that in this case, evaluate(null) will return the value of the constant.
   */
  public boolean queryConstant();

  /**
   * Returns a PExpression representing the addition of the present expression to constant.
   */
  public PExpression add(int constant);

  /**
   * Returns a PExpression representing the multiplication of the present expression with constant.
   */
  public PExpression multiply(int constant);

  /** This returns the names of the parameters that are used in the current expression. */
  public Set<String> queryParameters();

  /** This does a semantic equality check. */
  public boolean equals(PExpression expr);

  /** This returns the kind of expression (CONSTANT, PARAMETER, SUM, PRODUCT, ...). */
  public int queryKind();

  /** If this is a sum or product, this returns the left child. */
  public PExpression queryLeft();

  /** If this is a sum or product, this returns the right child. */
  public PExpression queryRight();
}

