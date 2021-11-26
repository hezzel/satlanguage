package logic;

import logic.sat.*;
import logic.parameter.PExpression;
import logic.parameter.Parameter;
import logic.parameter.ParameterList;
import logic.parameter.Substitution;
import logic.parameter.ParamBoolVar;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The VariableList keeps track of all the user-declared variables, to prevent any duplicates that
 * would make our formulas untrue.
 * Program-declared variables are NOT in this list, and should make sure to violate the
 * checkAcceptableName function so a user cannot declare the same variable as the program does
 * internally.
 */
public class VariableList {
  private TreeSet<String> _usednames;
  private TreeMap<String,Variable> _boolvars;
  private TreeMap<String,RangeVariable> _rangevars;
  private TreeMap<String,BinaryVariable> _binaryvars;
  private TreeMap<String,ParamBoolVar> _paramboolvars;
  private TreeMap<String,ParamRangeVar> _paramrangevars;
  private TreeMap<String,ParamBinaryVar> _parambinaryvars;

  public VariableList() {
    _usednames = new TreeSet<String>();
    _boolvars = new TreeMap<String,Variable>();
    _rangevars = new TreeMap<String,RangeVariable>();
    _binaryvars = new TreeMap<String,BinaryVariable>();
    _paramboolvars = new TreeMap<String,ParamBoolVar>();
    _paramrangevars = new TreeMap<String,ParamRangeVar>();
    _parambinaryvars = new TreeMap<String,ParamBinaryVar>();

    _boolvars.put("FALSE", new Variable("FALSE"));
    _boolvars.put("TRUE", new Variable("TRUE"));
    _usednames.add("FALSE");
    _usednames.add("TRUE");
  }

  private void checkAcceptableName(String name) {
    if (name.indexOf('[') != -1 || name.indexOf('⟦') != -1) {
      throw new Error("User-declared variables are not allowed to have '[' or '⟦' in their " +
        "name (problem: " + name + ").");
    }
    if (_usednames.contains(name)) {
      throw new Error("Trying to register " + name + " which is already in use.");
    }
  }

  private Atom truth() {
    return new Atom(queryTrueVariable(), true);
  }

  public Variable registerBooleanVariable(String name) {
    checkAcceptableName(name);
    Variable v = new Variable(name);
    _boolvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public RangeVariable registerRangeVariable(Parameter param) {
    String name = param.queryName();
    checkAcceptableName(name);
    RangeVariable v = new RangeVariable(param, truth());
    _rangevars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public BinaryVariable registerBinaryVariable(String name, int length, boolean allowNegative) {
    checkAcceptableName(name);
    BinaryVariable v = new BinaryVariable(name, length, allowNegative, truth());
    _binaryvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public BinaryVariable registerBinaryVariable(String name, int min, int max) {
    checkAcceptableName(name);
    BinaryVariable v = new BinaryVariable(name, min, max, truth());
    _binaryvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public ParamBoolVar registerParametrisedBooleanVariable(String name, ParameterList params) {
    checkAcceptableName(name);
    ParamBoolVar v = new ParamBoolVar(name, params, queryFalseVariable());
    _paramboolvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public ParamRangeVar registerParametrisedRangeVariable(Parameter count, ParameterList params) {
    String name = count.queryName();
    checkAcceptableName(name);
    ParamRangeVar v = new ParamRangeVar(count, params, new Atom(queryTrueVariable(), true));
    _paramrangevars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public ParamBinaryVar registerParametrisedBinaryVariable(String name, ParameterList params,
                                                           int length, boolean allowNegative) {
    checkAcceptableName(name);
    ParamBinaryVar v = new ParamBinaryVar(name, params, length, allowNegative, truth());
    _parambinaryvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public ParamBinaryVar registerParametrisedBinaryVariable(String name, ParameterList params,
                                                           PExpression min, PExpression max) {
    checkAcceptableName(name);
    ParamBinaryVar v = new ParamBinaryVar(name, params, min, max, truth());
    _parambinaryvars.put(name, v);
    _usednames.add(name);
    return v;
  }

  public boolean isDeclared(String name) {
    return _usednames.contains(name);
  }

  public Variable queryBooleanVariable(String name) {
    return _boolvars.get(name);
  }

  public RangeVariable queryRangeVariable(String name) {
    return _rangevars.get(name);
  }

  public BinaryVariable queryBinaryVariable(String name) {
    return _binaryvars.get(name);
  }

  public ParamBoolVar queryParametrisedBooleanVariable(String name) {
    return _paramboolvars.get(name);
  }

  public ParamRangeVar queryParametrisedRangeVariable(String name) {
    return _paramrangevars.get(name);
  }

  public ParamBinaryVar queryParametrisedBinaryVariable(String name) {
    return _parambinaryvars.get(name);
  }

  public Variable queryFalseVariable() {
    return _boolvars.get("FALSE");
  }

  public Variable queryTrueVariable() {
    return _boolvars.get("TRUE");
  }

  /** Adds clauses to col which necessitate that every integer variable is really an integer. */
  public void addWelldefinednessClauses(ClauseCollection col) {
    col.addClause(new Clause(new Atom(queryTrueVariable(), true)));
    col.addClause(new Clause(new Atom(queryFalseVariable(), false)));
    for (RangeVariable x : _rangevars.values()) x.addWelldefinednessClauses(col);
    for (ParamRangeVar y : _paramrangevars.values()) y.addWelldefinednessClauses(col);
    for (BinaryVariable x : _binaryvars.values()) x.addWelldefinednessClauses(col);
    for (ParamBinaryVar y : _parambinaryvars.values()) y.addWelldefinednessClauses(col);
  }

  /** This returns a human-readable presentation of the declared boolean variables. */
  private String printBasicBooleanVariableDeclarations() {
    String ret = "";
    for (String name : _boolvars.keySet()) {
      if (name.equals("FALSE") || name.equals("TRUE")) continue;
      ret += "declare " + name + " :: Bool\n";
    }
    return ret;
  }

  /** This returns a human-readable presentation of the declared range variables. */
  private String printBasicRangeDeclarations() {
    String ret = "";
    for (RangeVariable x : _rangevars.values()) {
      ret += "declare " + x.toString() + " :: Number ∈ " + x.queryRangeDescription() + "\n";
    }
    return ret;
  }

  /** This returns a human-readable presenttion of the declared binary variables. */
  private String printBasicBinaryDeclarations() {
    String ret = "";
    for (BinaryVariable x : _binaryvars.values()) {
      ret += "declare " + x.toString() + " :: Int? ∈ { " + x.queryMinimum() + ".." +
             x.queryMaximum() + " }\n";
    }
    return ret;
  }

  /** Helper for the two printVariableDeclaration method. */
  private String printParameterList(ParameterList lst) {
    if (lst.size() == 0) return ""; 
    String ret = " for ";
    for (int j = 0; j < lst.size(); j++) {
      if (j > 0) ret += ", ";
      ret += lst.get(j).toString();
    }   
    return ret;
  }

  /** This returns a human-readable presentation of the declared parametrised boolean variables. */
  private String printParametrisedBooleanVariableDeclarations() {
    String ret = ""; 
    Substitution subst = new Substitution();
    for (ParamBoolVar x : _paramboolvars.values()) {
      ret += "declare " + x.toString(subst) + " :: Bool" +
        printParameterList(x.queryParameters()) + "\n";
    }
    return ret;
  }

  /** This returns a human-readable presentation of the declared parametrised range variables. */
  private String printParametrisedRangeDeclarations() {
    String ret = "";
    for (ParamRangeVar x : _paramrangevars.values()) {
      ret += "declare " + x.toString() + " :: Number ∈ " + x.queryRangeDescription() +
        printParameterList(x.queryParameters()) + "\n";
    }
    return ret;
  }

  /** This returns a human-readable presentation of the declared parametrised binary variables. */
  private String printParametrisedBinaryDeclarations() {
    String ret = "";
    for (ParamBinaryVar x : _parambinaryvars.values()) {
      ret += "declare " + x.toString() + " :: " + x.queryDeclarationDescription() +
        printParameterList(x.queryParameters()) + "\n";
    }
    return ret;
  }

  /** This returns a human-readable presentation of the variable list. */
  public String toString() {
    String ret = printBasicBooleanVariableDeclarations() +
                 printParametrisedBooleanVariableDeclarations() +
                 printBasicRangeDeclarations() +
                 printParametrisedRangeDeclarations() +
                 printBasicBinaryDeclarations() +
                 printParametrisedBinaryDeclarations();
    return ret;
  }
}

