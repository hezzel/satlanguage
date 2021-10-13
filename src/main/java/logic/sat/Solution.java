package logic.sat;

import java.util.TreeSet;

/**
 * A Solution represents a YES or NO answer from the SAT Solver.  In the case of YES, it also
 * indicates truth values for all variables.
 * A Solution is an immutable object.
 */
public class Solution {
  private TreeSet<Integer> _vars; // the set of true-valued variables

  /**
   * Creates a Solution with an assignment for a solvable SAT problem (with that assignment), or
   * null if the problem is not solvable.
   */
  public Solution(TreeSet<Integer> trueVariables) {
    if (trueVariables == null) _vars = null;
    else _vars = new TreeSet(trueVariables);
  }

  /**
   * Returns true if this is a solution with an assignment, false if it is a null-solution (so for
   * an unsatisfiable SAT problem).
   */
  public boolean querySatisfiable() {
    return _vars != null;
  }

  /** Returns true if the given variable is satisfied under this assignment, false otherwise. */
  public boolean check(Variable x) {
    return _vars != null && _vars.contains(x.queryIndex());
  }

  /**
   * Returns true if variables with the given index are satisfied under the assignment,
   * false otherwise.
   */
  public boolean check(int index) {
    return _vars != null && _vars.contains(index);
  }

  /** Returns true if the given atom is satisfied under this assignment, false otherwise. */
  public boolean check(Atom a) {
    return _vars != null && _vars.contains(a.queryIndex()) != a.queryNegative();
  }
}

