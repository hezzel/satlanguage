package language;

import logic.sat.Solution;
import logic.VariableList;
import logic.RequirementsList;
import language.parser.ParserException;
import language.parser.InputReader;
import language.parser.DefinitionData;
import language.execution.ProgramState;
import language.execution.Statement;

/**
 * A Program is the combination of a requirements list and an output statement, which can easily be
 * set up from inside Java, or be read from an input file.
 */
public class Program {
  private VariableList _vars;
  private RequirementsList _reqs;
  private Statement _statement;
  private DefinitionData _defs;

  public Program() {
    _vars = new VariableList();
    _reqs = new RequirementsList(_vars);
    _defs = new DefinitionData();
    _statement = null;
  }

  public void readFromFile(String filename) {
    try { _statement = InputReader.readProgramFromFile(filename, _reqs, _defs); }
    catch (Exception e) { throw new Error(e); }
  }

  public void addMacro(String name, int value) {
    try { InputReader.readMacroFromString("define " + name + " " + value, _defs); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void addMapping(String name, String description) {
    try { InputReader.readFunctionFromString("function " + name + " " + description, _defs); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void declare(String declaration) {
    try { InputReader.declare(declaration, _vars, _defs); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void require(String formula) {
    try { _reqs.add(InputReader.readClosedFormulaFromString(formula, _vars, _defs)); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void setOutput(String program) {
    try { _statement = InputReader.readStatementFromString("{" + program + "}", _vars, _defs); }
    catch (ParserException e) { throw new Error(e); }
  }

  public void execute(boolean debug) {
    Solution sol = _reqs.solve(debug);
    if (sol == null) System.out.println("Could not determine whether the problem is solvable.");
    else if (!sol.querySatisfiable()) System.out.println("The problem is not solvable.");
    else if (_statement == null) System.out.println("The problem is solvable.");
    else _statement.execute(new ProgramState(sol));
  }
}

