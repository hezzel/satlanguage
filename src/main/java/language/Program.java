package language;

import logic.sat.Solution;
import logic.VariableList;
import logic.RequirementsList;
import language.parser.ParserException;
import language.parser.InputReader;
import language.execution.ProgramState;
import language.execution.Statement;

/**
 * A Program is the combination of a requirements list and an output statement, which can easily be
 * set up from inside Java.
 */
public class Program {
  private VariableList _vars;
  private RequirementsList _reqs;
  private Statement _statement;

  public Program() {
    _vars = new VariableList();
    _reqs = new RequirementsList(_vars);
    _statement = null;
  }

  public void declare(String declaration) {
    try { InputReader.declare(declaration, _vars); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void require(String formula) {
    try { _reqs.add(InputReader.readClosedFormulaFromString(formula, _vars)); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void setOutput(String program) {
    try { _statement = InputReader.readStatementFromString("{" + program + "}", _vars); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void execute() {
    Solution sol = _reqs.solve();
    if (sol == null) System.out.println("Could not determine whether the problem is solvable.");
    else if (!sol.querySatisfiable()) System.out.println("The problem is not solvable.");
    else if (_statement == null) System.out.println("The problem is solvable.");
    else _statement.execute(new ProgramState(sol));
  }

  public void debugOutput() {
    String db = _reqs.queryDebugOutput();
    if (db == null) {
      System.out.println("You cannot print debug output until the program has been executed.");
    }
    else System.out.println(db);
  }
}
