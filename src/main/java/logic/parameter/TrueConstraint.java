package logic.parameter;

import java.util.TreeSet;

/** The constraint that always holds. */
public class TrueConstraint implements PConstraint {
  public TrueConstraint() {}
  public boolean evaluate(Assignment assignment) { return true; }
  public boolean isTop() { return true; }
  public int queryKind() { return PConstraint.CONSTANT; }
  public PConstraint substitute(Substitution substitution) { return this; }
  public TreeSet<String> queryParameters() { return new TreeSet<String>(); }
  public String toString() { return "⊤"; }
}
