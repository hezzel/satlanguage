package logic.parameter;

import java.util.TreeSet;

/** The constraint that never holds. */
public class FalseConstraint implements PConstraint {
  public FalseConstraint() {}
  public boolean evaluate(Assignment assignment) { return false; }
  public boolean isTop() { return false; }
  public int queryKind() { return PConstraint.CONSTANT; }
  public PConstraint substitute(Substitution substitution) { return this; }
  public PConstraint negate() { return new TrueConstraint(); }
  public TreeSet<String> queryParameters() { return new TreeSet<String>(); }
  public String toString() { return "⊥"; }
}
