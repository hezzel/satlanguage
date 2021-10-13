package logic.parameter;

import java.util.Set;

public interface PExpression {
  static final int CONSTANT  = 0;
  static final int PARAMETER = 1;
  static final int PRODUCT   = 2;
  static final int SUM       = 3;

  /**
   * Given a complete assignment of all parameters in the current expression, this returns the
   * value of the expression under that assignment.
   * In the case of an expession without any parameters, assignment is allowed to be null; but if
   * there are parameters this will yield an Error.
   */
  public int evaluate(Assignment assignment);

  /**
   * Given a assignment of zero or more of the parameters in the current expression, this
   * evaluates the assignment as far as possible and returns the result.
   */
  public PExpression substitute(Assignment assignment);

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

  /** This returns the names of the parameters that are used in the current expression. */
  public Set<String> queryParameters();

  /** This does a semantic equality check. */
  public boolean equals(PExpression expr);

  /** This returns the kind of expression (CONSTANT, PARAMETER, SUM, PRODUCT). */
  public int queryKind();

  /** If this is a sum or product, this returns the left child. */
  public PExpression queryLeft();

  /** If this is a sum or product, this returns the right child. */
  public PExpression queryRight();
}

