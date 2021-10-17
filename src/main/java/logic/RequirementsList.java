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
  private String _debugOutput;

  public RequirementsList(VariableList vars) {
    _variables = vars;
    _formulas = new ArrayList<Formula>();
    _debugOutput = null;
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
    SatProblem problem = new SatProblem();
    for (int i = 0; i < _formulas.size(); i++) _formulas.get(i).addClauses(problem);
    return problem;
  }

  /** 
   * This method finds a satisfying assignment for all the requirements in the current list, if one
   * exists.  If not, the negative Solution is returned.
   * If the sat solver cannot decide the problem or a file issue occurs, null is returned instead.
   */
  public Solution solve() {
    SatProblem problem = createSat();
    _debugOutput = problem.toString();
    return problem.solve();
  }

  /**
   * Returns a string description of the SAT problem corresponding to this requirements list, as it
   * was created when the program was executed.
   */
  public String queryDebugOutput() {
    return _debugOutput;
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

