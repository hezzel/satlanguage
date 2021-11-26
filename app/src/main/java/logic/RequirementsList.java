package logic;

import logic.sat.SatProblem;
import logic.sat.Solution;
import logic.formula.Formula;
import java.util.ArrayList;

/**
 * The RequirementsList keeps track of both the user-declared variables in a program, and the
 * (closed!) formulas that are required to hold.
 * It can translate these into a SatProblem to be solved.
 */
public class RequirementsList {
  private VariableList _variables;
  private ArrayList<Formula> _formulas;

  public RequirementsList(VariableList vars) {
    _variables = vars;
    _formulas = new ArrayList<Formula>();
  }

  public VariableList queryVariables() {
    return _variables;
  }

  public void add(Formula formula) {
    if (!formula.queryClosed()) {
      throw new Error("Adding an open formula to the requirements list!");
    }
    _formulas.add(formula);
  }

   /** 
   * This method turns the requirements list into a SatProblem, so that the one is satisfiable if
   * and only if the other is.
   */
  public SatProblem createSat() {
    System.err.println("Generating SAT problem...");
    SatProblem problem = new SatProblem();
    _variables.addWelldefinednessClauses(problem);
    for (int i = 0; i < _formulas.size(); i++) {
      System.err.println(_formulas.get(i).toString());
      _formulas.get(i).addClauses(problem);
    }
    return problem;
  }

  /** 
   * This method finds a satisfying assignment for all the requirements in the current list, if one
   * exists.  If not, the negative Solution is returned.
   * If the sat solver cannot decide the problem or a file issue occurs, null is returned instead.
   */
  public Solution solve(boolean debug) {
    SatProblem problem = createSat();
    if (debug) {
      System.err.println("Creating debug output...");
      String debugOutput = problem.toString();
      System.out.println(debugOutput);
    }
    System.err.println("Sending problem to SAT solver...");
    return problem.solve();
  }

  /** This returns a human-readable presentation of the requirements list. */
  public String toString() {
    String ret = _variables.toString();
    for (int i = 0; i < _formulas.size(); i++) {
      ret += _formulas.get(i).toString() + "\n";
    }   
    return ret;
  }
}

