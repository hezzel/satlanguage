package language.parser;

import logic.sat.Variable;
import logic.parameter.*;
import logic.range.*;
import logic.formula.*;
import logic.VariableList;
import logic.RequirementsList;
import language.execution.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Vocabulary;

public class InputReader {
  private Vocabulary _vocabulary;
  private String[] _ruleNames;
  private DefinitionData _defs;

  public InputReader() {
    _vocabulary = LogicParser.VOCABULARY;
    _ruleNames = LogicParser.ruleNames;
    _defs = new DefinitionData();
  }

  /** ===== Generic functions: usable for all Antlr readers ===== */

  /** If the current tree is a terminal, returns its display name; otherwise returns null. */
  protected String getTerminalNodeName(ParseTree tree) {
    if (!(tree instanceof TerminalNode)) return null;
    Token token = ((TerminalNode)tree).getSymbol();
    return _vocabulary.getSymbolicName(token.getType());
  }

  /** If the current tree is a rule context, returns the corresponding rulename; otherwise null. */
  protected String getRuleName(ParseTree tree) {
    if (!(tree instanceof ParserRuleContext)) return null;
    ParserRuleContext cxt = (ParserRuleContext)tree;
    return _ruleNames[cxt.getRuleIndex()];
  }

  /** Gets the first Token of the given parse tree, for use in ParseExceptions and ParseErrors. */
  protected Token firstToken(ParseTree tree) {
    if (tree instanceof TerminalNode) return ((TerminalNode)tree).getSymbol();
    if (tree instanceof ParserRuleContext) return ((ParserRuleContext)tree).getStart();
    return null;
  }

  /** Builds an Error to indicate that there is a problem with the given parse tree. */
  protected ParserError buildError(ParseTree tree, String message) {
    Token start = firstToken(tree);
    String text = tree.getText();
    if (start != null) return new ParserError(start, text, message);
    return new ParserError(0, 0, text,
                           message + " [Also: parse tree does not include any tokens.]");
  }

  /** This returns a description (including "token" or "rule") of the given child of tree. */
  protected String checkChild(ParseTree tree, int childindex) {
    int childcount = tree.getChildCount();
    if (childcount <= childindex) return "<empty>";
    ParseTree child = tree.getChild(childindex);
    if (child == null) return "<null>";
    String ret = getTerminalNodeName(child);
    if (ret != null) return "token " + ret;
    ret = getRuleName(child);
    if (ret != null) return "rule " + ret;
    return "unexpected tree [" + child.getText() + "]";
  }

  /**
   * This function checks that the given child of tree is a rule with the given name.
   * If that is not the case, then a ParserError is thrown.
   */
  protected void verifyChildIsRule(ParseTree tree, int childindex, String rulename, String desc) {
    String actual = checkChild(tree, childindex);
    if (!actual.equals("rule " + rulename)) {
      throw buildError(tree, "encountered " + actual + "; expected " + desc + ".");
    }
  }

  /**
   * This function checks that the given child of tree is a token with the given name.
   * If that is not the case, then a ParserError is thrown.
   */
  protected void verifyChildIsToken(ParseTree tree, int childindex, String tokenname, String desc) {
    String actual = checkChild(tree, childindex);
    if (!actual.equals("token " + tokenname)) {
      throw buildError(tree, "encountered " + actual + "; expected " + desc + ".");
    }
  }

  /** ===== Reading Variables ===== */

  /**
   * Returns the variable represented by the identifier in the current tree, or throws a
   * ParserException if there is no boolean variable declared by the given name.
   */
  private Variable readBooleanVariable(ParseTree tree, VariableList lst) throws ParserException {
    String name = tree.getText();
    Variable v = lst.queryBooleanVariable(name);
    if (v != null) return v;
    if (lst.isDeclared(name)) {
      throw new ParserException(firstToken(tree), "Illegal use of variable " + name + ": used " +
        "as a stand-alone boolean variable but was not declared as such.");
    }
    else throw new ParserException(firstToken(tree), "Undeclared boolean variable: " + name);
  }

  /**
   * Reads a "paramvar" of the form x[i1,...,in] where each ij is the name of parameter i.  The
   * parameters should already be given in the parameter list, and it will be checked that they are
   * given correctly in the paramvar, and in the right order.  It is also checked that the variable
   * name is not yet declared.  If all is good, the name of the variable is returned.
   */
  private String readParamVarForDeclaration(ParseTree tree, ParameterList params,
                                            VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "an identifer (variable name)");
    verifyChildIsToken(tree, 1, "SBRACKETOPEN", "square opening bracket [");
    verifyChildIsRule(tree, 2, "pexprlist", "a list of parameter names");
    verifyChildIsToken(tree, 3, "SBRACKETCLOSE", "square closing bracket ]");
    
    // find the name and check that it's allowed
    String name = tree.getChild(0).getText();
    if (lst.isDeclared(name)) throw new ParserException(firstToken(tree),
      "declaring (parametrised) variable " + name + " which was previously declared!");

    // check that the parameters are declared in the same order as in params
    ParseTree exprlist = tree.getChild(2);
    if (exprlist.getChildCount() != 2 * params.size() - 1) {
      throw new ParserException(firstToken(tree), "parameters to the declared variable should be" +
        " the same as the ones after the 'for' keyword");
    }
    for (int i = 0; i < params.size(); i++) {
      if (!exprlist.getChild(2*i).getText().equals(params.get(i).queryName())) {
        throw new ParserException(firstToken(exprlist), "parameters to the declared variable " +
          "should be the same (and in the same order) as the ones after the 'for' keyword " +
          "(mismatch: " + params.get(i).queryName() + ")");
      }
    }

    return name;
  }

  /**
   * Reads a paramvar declaration, and (a) adds all the arguments to args, and (b) returns the name
   * of the responsible variable.  If the given VariableList is null, then the arguments should be
   * true PExpressions; otherwise, they should be extended PExpressions, so they are allowed to
   * contain integer variables.  The name of the current variable is not looked up in the list.
   */
  private String splitParamVar(ParseTree tree, ArrayList<PExpression> args, VariableList lst)
                                                                           throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "the name of a ParamBoolVar");
    verifyChildIsToken(tree, 1, "SBRACKETOPEN", "indexing opening bracket [");
    verifyChildIsRule(tree, 2, "pexprlist", "a list of PExpressions");
    verifyChildIsToken(tree, 3, "SBRACKETCLOSE", "indexing closing bracket ]");
    String name = tree.getChild(0).getText();
    ParseTree pexprs = tree.getChild(2);
    for (int i = 0; i < pexprs.getChildCount(); i += 2) {
      args.add(readPExpression(pexprs.getChild(i), lst));
    }
    return name;
  }

  /**
   * Returns the parametrised boolean variable represented by the identifier starting the tree,
   * and updates the given arguments list to add all the pexpressions in the arguments list to the
   * variable.  (The args list is expected to be empty before the call.)
   * If there is no ParamBoolVar declared with that name, or if the length of the arguments list
   * does not match the expected number of parameters, a ParserException is thrown instead.  If
   * ivarsInParams is false, then the parameters of the bool var should be pure PExpressions; that
   * is, they are not allowed to contain integer variables.  If ivarsInParams is true, then we
   * should read the arguments as extended PExpressions.
   */
  private ParamBoolVar readQuantifiedBooleanVariable(ParseTree tree, VariableList lst,
                       ArrayList<PExpression> args, boolean ivarsInParams) throws ParserException {
    String name = splitParamVar(tree, args, ivarsInParams ? lst : null);
    ParamBoolVar x = lst.queryParametrisedBooleanVariable(name);
    if (x == null) {
      if (lst.isDeclared(name)) {
        throw new ParserException(firstToken(tree), "Illegal use of variable " + name + ": used " +
          "as a parametrised boolean variable but was not declared as such.");
      }
      else {
        throw new ParserException(firstToken(tree), "Encountered undeclared variable " + name);
      }
    }
    ParameterList expected = x.queryParameters();
    if (expected.size() != args.size()) {
      throw new ParserException(firstToken(tree), "Illegal use of variable " + name + " declared " +
        "with " + expected.size() + " parameters, but used with " + args.size() + " parameters.");
    }
    return x;
  }

  /**
   * Returns the parametrised range variable represented by the identifier starting the tree, and
   * updates the given arguments list to add all the pexpressions in the arguments list to the
   * variable.  (The args list is expected to be empty before the call.)
   * If there is no ParamRangeVar declared with that name, or if the length of the arguments list
   * does not match the expected number of parameters, a ParserException is thrown instead.  If
   * ivarsInParams is false, then the parameters of the range var should be pure PExpressions; that
   * is, they are not allowed to contain integer variables.  If ivarsInParams is true, then we
   * should read the arguments as extended PExpressions.
   */
  private ParamRangeVar readQuantifiedRangeVariable(ParseTree tree, VariableList lst,
                        ArrayList<PExpression> args, boolean ivarsInParams) throws ParserException {
    String name = splitParamVar(tree, args, ivarsInParams ? lst : null);
    ParamRangeVar x = lst.queryParametrisedRangeVariable(name);
    if (x == null) {
      if (lst.isDeclared(name)) {
        throw new ParserException(firstToken(tree), "Illegal use of variable " + name + ": used " +
          "as a parametrised range variable but was not declared as such.");
      }
      else {
        throw new ParserException(firstToken(tree), "Encountered undeclared variable " + name);
      }
    }
    ParameterList expected = x.queryParameters();
    if (expected.size() != args.size()) {
      throw new ParserException(firstToken(tree), "Illegal use of variable " + name + " declared " +
        "with " + expected.size() + " parameters, but used with " + args.size() + " parameters.");
    }
    return x;
  }

  /** ===== Reading PExpressions ===== */

  /**
   * If the variable list is given, then we are reading an *extended* PExpression, and can use the
   * list to look up variables occurring in the PExpression.
   * If not, then this is a normal PExpression (as occurs in the requirements part).
   */
  private PExpression readPExpression(ParseTree tree, VariableList lst) throws ParserException {
    if (tree.getChildCount() == 1) {
      verifyChildIsRule(tree, 0, "pexpressionminus", "a pexpression without plus");
      return readPExpressionMinus(tree.getChild(0), lst);
    }
    else {
      verifyChildIsRule(tree, 0, "pexpression", "a pexpression");
      verifyChildIsToken(tree, 1, "PLUS", "addition operator +");
      verifyChildIsRule(tree, 2, "pexpressionminus", "a pexpression without plus");
      PExpression left = readPExpression(tree.getChild(0), lst);
      PExpression right = readPExpressionMinus(tree.getChild(2), lst);
      return new SumExpression(left, right);
    }
  }

  private PExpression readPExpressionMinus(ParseTree tree, VariableList lst)
                                                                           throws ParserException {
    if (tree.getChildCount() == 1) {
      verifyChildIsRule(tree, 0, "pexpressiontimes", "a pexpression without plus or minus");
      return readPExpressionTimes(tree.getChild(0), lst);
    }
    else {
      verifyChildIsRule(tree, 0, "pexpressionminus", "a pexpression without plus");
      verifyChildIsToken(tree, 1, "MINUS", "subtraction operator -");
      verifyChildIsRule(tree, 2, "pexpressiontimes", "a pexpression without plus or minus");
      PExpression left = readPExpressionMinus(tree.getChild(0), lst);
      PExpression right = readPExpressionTimes(tree.getChild(2), lst);
      if (right.queryConstant()) right = new ConstantExpression(0 - right.evaluate(null));
      else right = new ProductExpression(new ConstantExpression(-1), right);
      return new SumExpression(left, right);
    }
  }

  private PExpression readPExpressionTimes(ParseTree tree, VariableList lst)
                                                                        throws ParserException {
    if (tree.getChildCount() == 1) {
      verifyChildIsRule(tree, 0, "pexpressionunit", "a parameter or integer");
      return readPExpressionUnit(tree.getChild(0), lst);
    }
    verifyChildIsRule(tree, 0, "pexpressiontimes", "a pexpression without plus or minus");
    verifyChildIsRule(tree, 2, "pexpressionunit", "a unit pexpression");
    PExpression part1 = readPExpressionTimes(tree.getChild(0), lst);
    PExpression part2 = readPExpressionUnit(tree.getChild(2), lst);
    String kind = checkChild(tree, 1);
    if (kind.equals("token TIMES")) return new ProductExpression(part1, part2);
    if (kind.equals("token DIV")) return new DivExpression(part1, part2);
    if (kind.equals("token MOD")) return new ModExpression(part1, part2);
    throw buildError(tree, "unexpected " + kind + " in pexpressiontimes");
  }

  private int readInteger(ParseTree tree) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token INTEGER")) {
      try { return Integer.parseInt(tree.getText()); }
      catch (NumberFormatException exc) { throw buildError(tree, "could not parse integer"); }
    }
    if (kind.equals("token MINUS")) {
      verifyChildIsToken(tree, 1, "INTEGER", "a positive integer");
      try { return Integer.parseInt(tree.getText()); }
      catch (NumberFormatException exc) { throw buildError(tree, "could not parse integer"); }
    }
    if (kind.equals("token DEFINITION")) {
      String name = tree.getChild(0).getText();
      if (!_defs._macros.containsKey(name)) {
        throw new ParserException(firstToken(tree), "Encountered undefined macro " + name);
      }
      return _defs._macros.get(name);
    }
    throw buildError(tree, "unexpected " + kind + " in integer");
  }

  private PExpression readPExpressionUnit(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token IDENTIFIER")) {
      String name = tree.getText();
      if (lst == null) return new ParameterExpression(name);
      RangeVariable x = lst.queryRangeVariable(name);
      if (x != null) return new VariableExpression(x);
      else return new ParameterExpression(tree.getText());
    }
    if (kind.equals("token MIN") || kind.equals("token MAX")) {
      verifyChildIsToken(tree, 1, "BRACKETOPEN", "opening bracket (");
      verifyChildIsRule(tree, 2, "pexpression", "pexpression");
      verifyChildIsToken(tree, 3, "COMMA", "a comma");
      verifyChildIsRule(tree, 4, "pexpression", "pexpression");
      verifyChildIsToken(tree, 5, "BRACKETCLOSE", "closing bracket )");
      PExpression a = readPExpression(tree.getChild(2), lst);
      PExpression b = readPExpression(tree.getChild(4), lst);
      if (kind.equals("token MIN")) return new MinExpression(a, b);
      else return new MaxExpression(a, b);
    }
    if (kind.equals("token BRACKETOPEN")) {
      verifyChildIsRule(tree, 1, "pexpression", "a parameter expression");
      verifyChildIsToken(tree, 2, "BRACKETCLOSE", "a closing bracket");
      return readPExpression(tree.getChild(1), lst);
    }
    if (kind.equals("token DEFINITION")) {
      verifyChildIsToken(tree, 1, "BRACKETOPEN", "opening bracket (");
      verifyChildIsRule(tree, 2, "pexpression", "a pexpression");
      verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "closing bracket )");
      String name = tree.getChild(0).getText();
      if (!_defs._functions.containsKey(name)) {
        throw new ParserException(firstToken(tree), "Definition " + name + " is not a function.");
      }
      ArrayList<PExpression> parts = new ArrayList<PExpression>();
      for (int i = 2; i < tree.getChildCount(); i += 2) {
        parts.add(readPExpression(tree.getChild(i), lst));
      }
      return new FunctionExpression(_defs._functions.get(name), parts);
    }
    if (kind.equals("rule integer")) {
      return new ConstantExpression(readInteger(tree.getChild(0)));
    }
    verifyChildIsRule(tree, 0, "paramvar", "an identifier, integer opening bracket or paramvar");
    return readPExpressionParamvar(tree.getChild(0), lst);
  }

  private PExpression readPExpressionParamvar(ParseTree tree, VariableList lst)
                                                                           throws ParserException {
    if (lst == null) {
      throw new ParserException(firstToken(tree), "Encountered variable " + tree.getText() +
        " which is not allowed as part of a parameter expression in the requirements part of " +
        "the program.");
    }
    ArrayList<PExpression> exprs = new ArrayList<PExpression>();
    ParamRangeVar x = readQuantifiedRangeVariable(tree, lst, exprs, true);
    return new ParamRangeVarExpression(x, exprs);
  }

  private PExpression readFullPExpression(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "pexpression", "a parameter expression");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readPExpression(tree.getChild(0), lst);
  }

  /** ===== Reading PConstraints ===== */

  /**
   * If the variable list is given, then we are reading an *extended* PConstraint, and can use the
   * list to look up variables occurring in the PConstraint.
   * If not, then this is a normal PConstraint (as occurs in the requirements part).
   */
  private PConstraint readPConstraint(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "pconstraintunit", "a basic pconstraint");
    if (tree.getChildCount() == 1)
      return readPConstraintUnit(tree.getChild(0), lst);
    ArrayList<PConstraint> parts = new ArrayList<PConstraint>();
    parts.add(readPConstraintUnit(tree.getChild(0), lst));
    for (int i = 1; i < tree.getChildCount(); i++) {
      ParseTree child = tree.getChild(i);
      verifyChildIsRule(child, 1, "pconstraintunit", "a basic pconstraint");
      parts.add(readPConstraintUnit(child.getChild(1), lst));
    }
    PConstraint ret = parts.get(parts.size()-1);
    if (checkChild(tree, 1).equals("rule pconstraintand")) {
      for (int i = parts.size()-2; i >= 0; i--) {
        ret = new AndConstraint(parts.get(i), ret);
      }
    }
    else {
      for (int i = parts.size()-2; i >= 0; i--) {
        ret = new OrConstraint(parts.get(i), ret);
      }
    }
    return ret;
  }

  private PConstraint readPConstraintUnit(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token TOP")) return new TrueConstraint();
    if (kind.equals("token BOTTOM")) return new FalseConstraint();
    if (kind.equals("token BRACKETOPEN")) {
      verifyChildIsRule(tree, 1, "pconstraint", "a PConstraint");
      verifyChildIsToken(tree, 2, "BRACKETCLOSE", "closing bracket ')'");
      return readPConstraint(tree.getChild(1), lst);
    }
    if (kind.equals("token NOT") || kind.equals("token MINUS")) {
      verifyChildIsRule(tree, 1, "pconstraintunit", "a basic pconstraint");
      return readPConstraintUnit(tree.getChild(1), lst).negate();
    }
    if (kind.equals("rule pconstraintrelation")) {
      return readPConstraintRelation(tree.getChild(0), lst);
    }
    verifyChildIsRule(tree, 0, "variable", "a boolean variable");
    return readPConstraintVariable(tree.getChild(0), lst);
  }

  public PConstraint readPConstraintVariable(ParseTree tree, VariableList lst)
                                                                          throws ParserException {
    if (lst == null) {
      throw new ParserException(firstToken(tree), "Encountered variable " + tree.getText() +
        " which is not allowed as part of a constraint in the requirements part of the program.");
    }
    String kind = checkChild(tree, 0);
    if (kind.equals("token IDENTIFIER")) {
      return new VariableConstraint(readBooleanVariable(tree.getChild(0), lst), true);
    }
    else {
      verifyChildIsRule(tree, 0, "paramvar", "a boolean variable or ranged variable");
      ArrayList<PExpression> exprs = new ArrayList<PExpression>();
      ParamBoolVar x = readQuantifiedBooleanVariable(tree.getChild(0), lst, exprs, true);
      return new ParamBoolVarConstraint(x, exprs, true);
    }
  }

  private PConstraint readPConstraintRelation(ParseTree tree, VariableList lst)
                                                                           throws ParserException {
    verifyChildIsRule(tree, 0, "pexpression", "a parameter expression");
    verifyChildIsRule(tree, 2, "pexpression", "a parameter expression");
    PExpression left = readPExpression(tree.getChild(0), lst);
    PExpression right = readPExpression(tree.getChild(2), lst);
    String kind = checkChild(tree, 1);
    if (kind.equals("token SMALLER")) return new SmallerConstraint(left, right);
    if (kind.equals("token GREATER")) return new SmallerConstraint(right, left);
    if (kind.equals("token LEQ")) return new SmallerConstraint(left, right.add(1));
    if (kind.equals("token GEQ")) return new SmallerConstraint(right, left.add(1));
    if (kind.equals("token EQUALS")) return new EqualConstraint(left, right);
    verifyChildIsToken(tree, 1, "NEQ", "comparison or (in)equality symbol");
    return new NeqConstraint(left, right);
  }

  private PConstraint readFullPConstraint(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "pconstraint", "a parameter expression");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readPConstraint(tree.getChild(0), lst);
  }
  
  /** ===== Reading Parameters ===== */

  private Parameter readParameter(ParseTree tree) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "a parameter name");
    String name = tree.getChild(0).getText();
    verifyChildIsToken(tree, 1, "IN", "set inclusion symbol ∈");
    verifyChildIsRule(tree, 2, "range", "a range { min .. max}");
    return readParameterRange(name, tree.getChild(2));
  }

  private Parameter readParameterRange(String paramname, ParseTree tree) throws ParserException {
    verifyChildIsToken(tree, 0, "BRACEOPEN", "set opening brace {");
    verifyChildIsRule(tree, 1, "pexpression", "a parameter expression");
    PExpression minimum = readPExpression(tree.getChild(1), null);
    verifyChildIsToken(tree, 2, "DOTS", "two dots ..");
    verifyChildIsRule(tree, 3, "pexpression", "a parameter expression");
    PExpression maximum = readPExpression(tree.getChild(3), null);
    verifyChildIsToken(tree, 4, "BRACECLOSE", "set closing brace }");
    PConstraint constraint;
    if (tree.getChildCount() > 5) {
      verifyChildIsToken(tree, 5, "WITH", "keyword 'with'");
      verifyChildIsRule(tree, 6, "pconstraint", "a parameter constraint");
      constraint = readPConstraint(tree.getChild(6), null);
    }
    else constraint = new TrueConstraint();
    return new Parameter(paramname, minimum, maximum, constraint);
  }

  private Parameter readFullParameter(ParseTree tree) throws ParserException {
    verifyChildIsRule(tree, 0, "parameter", "a parameter");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readParameter(tree.getChild(0));
  }

  /** Reads a ParameterList without checking that no fresh parameters are used in it. */
  private ArrayList<Parameter> readOpenParameterList(ParseTree tree) throws ParserException {
    ArrayList<Parameter> ret = new ArrayList<Parameter>();
    verifyChildIsRule(tree, 0, "parameter", "a parameter");
    ret.add(readParameter(tree.getChild(0)));
    for (int i = 1; i < tree.getChildCount(); i += 2) {
      verifyChildIsToken(tree, i, "COMMA", "a comma");
      verifyChildIsRule(tree, i+1, "parameter", "a parameter");
      ret.add(readParameter(tree.getChild(i+1)));
    }
    return ret;
  }

  private ParameterList readParameterList(ParseTree tree) throws ParserException {
    ArrayList<Parameter> pars = readOpenParameterList(tree);
    try {
      ParameterList ret = new ParameterList(pars);
      return ret;
    }
    catch (Error err) {
      throw new ParserException(firstToken(tree),err.getMessage());
    }
  }

  /** ===== Reading a variable declaration ===== */

  private void readDeclaration(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "DECLARE", "declare keyword");
    String kind = checkChild(tree, 1);
    if (kind.equals("rule boolvardec")) readBoolVarDec(tree.getChild(1), lst);
    else if (kind.equals("rule intvardec")) readIntVarDec(tree.getChild(1), lst);
    else if (kind.equals("rule paramboolvardec")) readParamBoolVarDec(tree.getChild(1), lst);
    else if (kind.equals("rule paramintvardec")) readParamIntVarDec(tree.getChild(1), lst);
    else throw buildError(tree, "encountered " + kind + ", expected a kind of dec.");
  }

  private void readBoolVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "an identifier (variable name)");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "BOOLTYPE", "Bool");
    String name = tree.getChild(0).getText();
    if (lst.isDeclared(name)) throw new ParserException(firstToken(tree),
      "declaring boolean variable " + name + " which was previously declared!");
    lst.registerBooleanVariable(name);
  }

  /**
   * Helper function: checks that the only parameters in prange are those defined in the allowed
   * list.  If allowed == null, then no parameters are allowed to occur in prange.
   */
  private void verifyRangeUsesOnlyAllowedParameters(Parameter prange, ParameterList allowed,
                                                    ParseTree tree) throws ParserException {
    Set<String> rangeParams = prange.queryRestriction().queryParameters();
    rangeParams.remove(prange.queryName());
    rangeParams.addAll(prange.queryMinimum().queryParameters());
    rangeParams.addAll(prange.queryMaximum().queryParameters());
    if (allowed != null) {
      for (int i = 0; i < allowed.size(); i++) rangeParams.remove(allowed.get(i).queryName());
    }
    if (rangeParams.size() > 0) {
      throw new ParserException(firstToken(tree), "range for integer variable " +
        prange.queryName() + " uses fresh parameters: " + rangeParams);
    }
  }

  private void readIntVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "an identifier (variable name)");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "RANGETYPE", "Int");
    verifyChildIsToken(tree, 3, "IN", "∈");
    verifyChildIsRule(tree, 4, "range", "a range");
    String name = tree.getChild(0).getText();
    if (lst.isDeclared(name)) throw new ParserException(firstToken(tree),
      "declaring range variable " + name + " which was previously declared!");
    Parameter param = readParameterRange(name, tree.getChild(4));
    verifyRangeUsesOnlyAllowedParameters(param, null, tree);
    lst.registerRangeVariable(param);
  }

  private void readParamBoolVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "paramvar", "a parametrised variable x[i1,...,in]");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "BOOLTYPE", "Bool");
    verifyChildIsToken(tree, 3, "FOR", "keyword 'for'");
    verifyChildIsRule(tree, 4, "parameterlist", "a list of parameters");
    ParameterList params = readParameterList(tree.getChild(4));
    String name = readParamVarForDeclaration(tree.getChild(0), params, lst);
    lst.registerParametrisedBooleanVariable(name, params);
  }

  private void readParamIntVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "paramvar", "a parametrised variable x[i1,...,in]");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "RANGETYPE", "Int");
    verifyChildIsToken(tree, 3, "IN", "∈");
    verifyChildIsRule(tree, 4, "range", "a range");
    verifyChildIsToken(tree, 5, "FOR", "keyword 'for'");
    verifyChildIsRule(tree, 6, "parameterlist", "a list of parameters");
    ParameterList params = readParameterList(tree.getChild(6));
    String name = readParamVarForDeclaration(tree.getChild(0), params, lst);
    Parameter range = readParameterRange(name, tree.getChild(4));
    verifyRangeUsesOnlyAllowedParameters(range, params, tree);
    lst.registerParametrisedRangeVariable(range, params);
  }

  /**
   * Meant for internal use in the program: reading a declaration from string.  This means the
   * declare keyword is omitted, and the input ends after the declaration.
   */
  private void readInternalDeclaration(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    String kind = checkChild(tree, 0);
    if (kind.equals("rule boolvardec")) readBoolVarDec(tree.getChild(0), lst);
    else if (kind.equals("rule intvardec")) readIntVarDec(tree.getChild(0), lst);
    else if (kind.equals("rule paramboolvardec")) readParamBoolVarDec(tree.getChild(0), lst);
    else if (kind.equals("rule paramintvardec")) readParamIntVarDec(tree.getChild(0), lst);
    else throw buildError(tree, "encountered " + kind +
      ", expected rule boolvardec or rule paramboolvardec");
  }

  /** ===== Reading a Formula ===== */

  private Formula readFormula(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("rule smallformula")) return readSmallFormula(tree.getChild(0), lst);
    if (kind.equals("rule junction")) return readJunction(tree.getChild(0), lst);
    if (kind.equals("rule arrow")) return readArrow(tree.getChild(0), lst);
    if (kind.equals("rule quantification")) return readQuantification(tree.getChild(0), lst);
    throw buildError(tree, "not sure what this formula kind is supposed to be: " + kind);
  }

  private Formula readSmallFormula(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token BRACKETOPEN")) {
      verifyChildIsRule(tree, 1, "formula", "a formula");
      verifyChildIsToken(tree, 2, "BRACKETCLOSE", "closing bracket )");
      return readFormula(tree.getChild(1), lst);
    }
    if (kind.equals("token NOT") || kind.equals("token MINUS")) {
      verifyChildIsRule(tree, 1, "smallformula", "opening bracket or variable");
      return readSmallFormula(tree.getChild(1), lst).negate();
    }
    if (kind.equals("token ITE")) {
      verifyChildIsToken(tree, 1, "BRACKETOPEN", "opening bracket");
      verifyChildIsToken(tree, 3, "COMMA", "a comma");
      verifyChildIsToken(tree, 5, "COMMA", "a comma");
      verifyChildIsToken(tree, 7, "BRACKETCLOSE", "closing bracket");
      verifyChildIsRule(tree, 2, "formula", "a formula");
      verifyChildIsRule(tree, 4, "formula", "a formula");
      verifyChildIsRule(tree, 6, "formula", "a formula");
      return new IfThenElse(readFormula(tree.getChild(2), lst), readFormula(tree.getChild(4), lst),
                            readFormula(tree.getChild(6), lst));
    }
    if (kind.equals("rule intcomparison")) {
      return readIntegerComparison(tree.getChild(0), lst);
    }
    verifyChildIsRule(tree, 0, "variable", "a variable");
    return readVariableFormula(tree.getChild(0), lst);
  }

  private Formula readVariableFormula(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token IDENTIFIER")) {
      return new AtomicFormula(readBooleanVariable(tree, lst), true);
    }
    else {
      verifyChildIsRule(tree, 0, "paramvar", "a boolean variable or ranged variable");
      return readQuantifiedBoolVar(tree.getChild(0), lst);
    }
  }

  private Formula readQuantifiedBoolVar(ParseTree tree, VariableList lst) throws ParserException {
    ArrayList<PExpression> given = new ArrayList<PExpression>();
    ParamBoolVar x = readQuantifiedBooleanVariable(tree, lst, given, false);
    return new QuantifiedAtom(x, true, given);
  }

  private Formula readIntegerComparison(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "intexpression", "an integer expression");
    verifyChildIsRule(tree, 2, "intexpression", "an integer expression");
    QuantifiedRangeInteger left = readIntegerExpression(tree.getChild(0), lst);
    QuantifiedRangeInteger right = readIntegerExpression(tree.getChild(2), lst);
    String kind = checkChild(tree, 1);
    if (kind.equals("token GEQ")) return new Geq(left, right, true);
    if (kind.equals("token LEQ")) return new Geq(right, left, true);
    if (kind.equals("token SMALLER")) return new Geq(left, right, false);
    if (kind.equals("token GREATER")) return new Geq(right, left, false);
    if (kind.equals("token EQUALS")) return new Equals(left, right, true);
    if (kind.equals("token NEQ")) return new Equals(left, right, false);
    throw buildError(tree, "expected (in)equality token");
  }

  private QuantifiedRangeInteger readIntegerExpression(ParseTree tree, VariableList lst)
                                                                          throws ParserException {
    QuantifiedRangeInteger expr = getConstantPart(tree, lst);
    QuantifiedRangeInteger ret = getNonConstantPart(tree, lst);
    if (expr == null && ret == null) {
      throw buildError(tree, "Encountered QuantifiedRangeInteger with no substantive parts: " +
                       tree.getText());
    }
    if (expr == null) return ret;
    if (ret == null) return expr;
    return new QuantifiedRangePlus(ret, expr);
  }

  /**
   * This filters all the pexpressions out of an integer expression, and combines them into a
   * single constant.  If there are no pexpressions in there, null is returned.
   */
  private QuantifiedRangeConstant getConstantPart(ParseTree tree, VariableList lst)
                                                                          throws ParserException {
    PExpression expr = null;
    for (int i = 0; i < tree.getChildCount(); i += 2) {
      ParseTree child = tree.getChild(i);
      String kind = checkChild(child, 0);
      if (kind.equals("rule pexpressionminus")) {
        PExpression e = readPExpressionMinus(child.getChild(0), lst);
        if (expr == null) expr = e; else expr = new SumExpression(expr, e);
      }
      else if (kind.equals("token IDENTIFIER") &&
               lst.queryRangeVariable(child.getText()) == null) {
        PExpression e = new ParameterExpression(child.getText());
        if (expr == null) expr = e; else expr = new SumExpression(expr, e);
      }
    }
    if (expr == null) return null;
    return new QuantifiedRangeConstant(expr, lst.queryTrueVariable());
  }

  /**
   * This filters all the non-pexpressions out of an integer expression, and combines them into a
   * single QuantifiedRangeInteger.  If there are only pexpressions in there, null is returned.
   */
  private QuantifiedRangeInteger getNonConstantPart(ParseTree tree, VariableList lst)
                                                                          throws ParserException {
    ArrayList<QuantifiedRangeInteger> ret = new ArrayList<QuantifiedRangeInteger>();
    for (int i = 0; i < tree.getChildCount(); i += 2) {
      ParseTree child = tree.getChild(i);
      String kind = checkChild(child, 0);
      QuantifiedRangeInteger found = null;
      if (kind.equals("token IDENTIFIER")) {
        String name = child.getText();
        RangeVariable x = lst.queryRangeVariable(name);
        if (x != null) ret.add(new QuantifiedRangeWrapper(x));
      }
      if (kind.equals("token BRACKETOPEN")) {
        verifyChildIsRule(child, 1, "intexpression", "an integer expression");
        verifyChildIsToken(child, 2, "BRACKETCLOSE", "closing bracket )");
        ret.add(readIntegerExpression(child.getChild(1), lst));
      }
      if (kind.equals("rule condition")) {
        verifyChildIsToken(child, 1, "QUESTION", "a question mark");
        verifyChildIsRule(child, 2, "intexpression", "an integer expression");
        QuantifiedRangeInteger expr = readIntegerExpression(child.getChild(2), lst);
        Formula form = readCondition(child.getChild(0), lst);
        return new QuantifiedConditionalRangeInteger(form, expr, lst.queryTrueVariable());
      }
      if (kind.equals("token SUM")) {
        verifyChildIsToken(child, 1, "BRACEOPEN", "opening brace {");
        verifyChildIsRule(child, 2, "intexpression", "integer expression");
        verifyChildIsToken(child, 3, "MID", "|");
        verifyChildIsRule(child, 4, "parameterlist", "a parameter list");
        QuantifiedRangeInteger expr = readIntegerExpression(child.getChild(2), lst);
        ArrayList<Parameter> params = readOpenParameterList(child.getChild(4));
        if (child.getChildCount() == 6) {
          verifyChildIsToken(child, 5, "BRACECLOSE", "closing brace }");
          return new QuantifiedRangeSum(params, expr, lst.queryTrueVariable());
        }
        verifyChildIsToken(child, 5, "MID", "|");
        verifyChildIsRule(child, 6, "formula", "a formula");
        verifyChildIsToken(child, 7, "BRACECLOSE", "closing brace }");
        Formula formula = readFormula(child.getChild(6), lst);
        expr = new QuantifiedConditionalRangeInteger(formula, expr, lst.queryTrueVariable());
        return new QuantifiedRangeSum(params, expr, lst.queryTrueVariable());
      }
      if (kind.equals("rule paramvar")) {
        ArrayList<PExpression> args = new ArrayList<PExpression>();
        ParamRangeVar x = readQuantifiedRangeVariable(child.getChild(0), lst, args, false);
        ret.add(new QuantifiedRangeVariable(x, args));
      }
    }
    if (ret.size() == 0) return null;
    if (ret.size() == 1) return ret.get(0);
    return new QuantifiedRangePlus(ret);
  }

  private Formula readCondition(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token BRACKETOPEN")) {
      verifyChildIsRule(tree, 1, "formula", "a formula");
      return readFormula(tree.getChild(1), lst);
    }
    if (kind.equals("token NOT")) {
      verifyChildIsRule(tree, 1, "condition", "a condition");
      return readCondition(tree.getChild(1), lst).negate();
    }
    verifyChildIsRule(tree, 0, "variable", "a variable");
    return readVariableFormula(tree.getChild(0), lst);
  }

  private String getRootOperator(ParseTree tree) {
    String kind = checkChild(tree, 0);
    if (kind.equals("rule smallformula") || kind.equals("rule quantification")) {
      return checkChild(tree.getChild(0), 0);
    }
    return checkChild(tree.getChild(0), 1);
  }

  private Formula readJunction(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "smallformula", "a basic formula (brackets or no operators)");
    verifyChildIsRule(tree, 2, "formula", "a formula");
    Formula left = readSmallFormula(tree.getChild(0), lst);
    Formula right = readFormula(tree.getChild(2), lst);
    boolean isAnd = checkChild(tree, 1).equals("token AND");
    String childkind = getRootOperator(tree.getChild(2));
    if ( (childkind.equals("token AND") && !isAnd) || (childkind.equals("token OR") && isAnd) ) {
      throw new ParserException(firstToken(tree), "No precedence is defined between AND and OR; " +
        "please use brackets.");
    }
    if (isAnd) return new And(left, right);
    else return new Or(left, right);
  }

  private Formula readArrow(ParseTree tree, VariableList lst) throws ParserException {
    Formula left, right;

    verifyChildIsRule(tree, 2, "formula", "a formula");
    right = readFormula(tree.getChild(2), lst);
    String childkind = getRootOperator(tree.getChild(2));
    if (childkind.equals("token IMPLIES") || childkind.equals("token IFF")) {
      throw new ParserException(firstToken(tree), "No associativity is defined for implication " +
        "and if-and-only-if; please use brackets.");
    }
    
    String kind = checkChild(tree, 0);
    if (kind.equals("rule junction")) left = readJunction(tree.getChild(0), lst);
    else {
      verifyChildIsRule(tree, 0, "smallformula", "a basic formula (brackets or no operators)");
      left = readSmallFormula(tree.getChild(0), lst);
    }

    if (checkChild(tree, 1).equals("token IMPLIES")) return new Implication(left, right);
    else return new Iff(left, right);
  }

  private Formula readQuantification(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("token NOT") || kind.equals("token MINUS")) {
    verifyChildIsRule(tree, 1, "quantification", "a quantification");
      return readQuantification(tree.getChild(1), lst).negate();
    }
    verifyChildIsRule(tree, 1, "parameter", "a parameter");
    verifyChildIsToken(tree, 2, "DOT", "a dot");
    verifyChildIsRule(tree, 3, "formula", "a formula");
    Parameter p = readParameter(tree.getChild(1));
    Formula form = readFormula(tree.getChild(3), lst);
    if (kind.equals("token FORALL")) return new Forall(p, form);
    if (kind.equals("token EXISTS")) return new Exists(p, form);
    throw buildError(tree, "Expected token FORALL or EXISTS");
  }

  private Formula readClosedFormula(ParseTree tree, VariableList lst) throws ParserException {
    Formula ret = readFormula(tree, lst);
    if (!ret.queryClosed()) {
      throw new ParserException(firstToken(tree),
        "Formula " + ret.toString() + " has unbound parameters.");
    }
    return ret;
  }

  /** ===== Reading a String expression (for the execution language) ===== */

  private Statement readStatement(ParseTree tree, VariableList lst) throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("rule printstatement")) return readPrint(tree.getChild(0), lst);
    if (kind.equals("rule ifstatement")) return readIf(tree.getChild(0), lst);
    if (kind.equals("rule forstatement")) return readFor(tree.getChild(0), lst);
    verifyChildIsRule(tree, 0, "block", "a block");
    return readBlock(tree.getChild(0), lst);
  }

  private StringExpression readStringExpression(ParseTree tree, VariableList lst)
                                                                    throws ParserException {
    String kind = checkChild(tree, 0);
    if (kind.equals("rule pexpression")) {
      return new StringExpression(readPExpression(tree.getChild(0), lst));
    }
    verifyChildIsToken(tree, 0, "STRING", "a string");
    String s = tree.getText();
    return new StringExpression(s.substring(1,s.length()-1));
  }

  private Statement readPrint(ParseTree tree, VariableList lst) throws ParserException {
    ArrayList<StringExpression> parts = new ArrayList<StringExpression>();
    verifyChildIsToken(tree, 1, "BRACKETOPEN", "opening bracket (");
    verifyChildIsToken(tree, tree.getChildCount()-1, "BRACKETCLOSE", "closing bracket )");
    for (int i = 2; i < tree.getChildCount()-1; i += 2) {
      verifyChildIsRule(tree, i, "stringexpr", "a string or pexpression");
      parts.add(readStringExpression(tree.getChild(i), lst));
    }
    if (checkChild(tree, 0).equals("token PRINTLN")) parts.add(new StringExpression("\\n"));
    return new Print(parts);
  }

  private Statement readIf(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "IF", "if keyword");
    verifyChildIsRule(tree, 1, "pconstraint", "pconstraint");
    verifyChildIsToken(tree, 2, "THEN", "then keyword");
    verifyChildIsRule(tree, 3, "statement", "a statement");
    PConstraint constr = readPConstraint(tree.getChild(1), lst);
    Statement th = readStatement(tree.getChild(3), lst);
    Statement el = null;
    if (tree.getChildCount() == 6) {
      verifyChildIsToken(tree, 4, "ELSE", "else keyword");
      verifyChildIsRule(tree, 5, "statement", "a statement");
      el = readStatement(tree.getChild(5), lst);
    }
    return new If(constr, th, el);
  }

  private Statement readFor(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "FOR", "for keyword");
    verifyChildIsToken(tree, 1, "IDENTIFIER", "an identifier (the counter)");
    verifyChildIsToken(tree, 2, "INITIATE", "intiation token :=");
    verifyChildIsRule(tree, 3, "pexpression", "a pexression (the minimum)");
    verifyChildIsToken(tree, 4, "TO", "to keyword");
    verifyChildIsRule(tree, 5, "pexpression", "a pexression (the maximum)");
    verifyChildIsToken(tree, 6, "DO", "do keyword");
    verifyChildIsRule(tree, 7, "statement", "a statement");
    String name = tree.getChild(1).getText();
    if (lst.isDeclared(name)) {
      throw new ParserException(firstToken(tree), "Please do not use '" + name + "' as a loop " +
        "counter: it is also declared as a variable.");
    }
    PExpression minimum = readPExpression(tree.getChild(3), lst);
    PExpression maximum = readPExpression(tree.getChild(5), lst);
    Statement statement = readStatement(tree.getChild(7), lst);
    return new For(name, minimum, maximum, statement);
  }

  private Statement readBlock(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "BRACEOPEN", "opening brace {");
    verifyChildIsToken(tree, tree.getChildCount()-1, "BRACECLOSE", "closing brace }");
    ArrayList<Statement> parts = new ArrayList<Statement>();
    for (int i = 1; i < tree.getChildCount()-1; i++) {
      parts.add(readStatement(tree.getChild(i), lst));
    }
    return new Block(parts);
  }

  /** ===== A full program ===== */

  private int readMacro(ParseTree tree) throws ParserException {
    verifyChildIsToken(tree, 0, "DEFINE", "keyword define");
    verifyChildIsToken(tree, 1, "DEFINITION", "a macro");
    verifyChildIsRule(tree, 2, "pexpression", "a pexpression");
    String name = tree.getChild(1).getText();
    if (_defs._macros.containsKey(name)) {
      throw new ParserException(firstToken(tree), "Redefining previously declared macro " + name);
    }
    if (_defs._functions.containsKey(name)) {
      throw new ParserException(firstToken(tree), "Redefining macro previously defined as " +
        "function: " + name);
    }
    PExpression expr = readPExpression(tree.getChild(2), null);
    if (expr.queryParameters().size() != 0) {
      throw new ParserException(firstToken(tree), "Macro definition " + name +
        " contains parameters: " + expr.queryParameters());
    }
    int k = expr.evaluate(null);
    _defs._macros.put(name, k);
    return k;
  }

  private Function readFunction(ParseTree tree) throws ParserException {
    verifyChildIsToken(tree, 0, "FUNCTION", "keyword function");
    verifyChildIsToken(tree, 1, "DEFINITION", "a definition");
    String name = tree.getChild(1).getText();
    verifyChildIsToken(tree, 2, "BRACKETOPEN", "opening bracket (");
    int i = 3;
    ArrayList<String> args = new ArrayList<String>();
    for (; i < tree.getChildCount(); i++) {
      String kind = checkChild(tree, i);
      if (kind.equals("token COMMA")) continue;
      if (kind.equals("token BRACKETCLOSE")) break;
      args.add(tree.getChild(i).getText());
    }
    Function func = new Function(name, args);
    verifyChildIsToken(tree, i+1, "BRACEOPEN", "opening brace {");
    for (i = i + 2; i < tree.getChildCount(); i++) {
      String kind = checkChild(tree, i);
      if (kind.equals("token SEMICOLON")) continue;
      if (kind.equals("token BRACECLOSE")) break;
      verifyChildIsRule(tree, i, "mappingentry", "a mapping entry");
      readMappingEntry(tree.getChild(i), args, func);
    }
    if (_defs._functions.containsKey(name)) {
      throw new ParserException(firstToken(tree), "Defining function " + name + " which already " +
        "exists.");
    }
    if (_defs._macros.containsKey(name)) {
      throw new ParserException(firstToken(tree), "Defining function " + name + " when a macro " +
        "by that name already exists.");
    }
    _defs._functions.put(name, func);
    return func;
  }

  private void readMappingEntry(ParseTree tree, ArrayList<String> argNames, Function func)
                                                                           throws ParserException {
    verifyChildIsToken(tree, tree.getChildCount()-2, "FUNCARROW", "function arrow ⇒");
    verifyChildIsRule(tree, tree.getChildCount()-1, "pexpression", "a parameter expression");
    PExpression result = readPExpression(tree.getChild(tree.getChildCount()-1), null);
    ArrayList<Integer> values = new ArrayList<Integer>();

    Set<String> params = result.queryParameters();
    for (int i = 0; i < argNames.size(); i++) params.remove(argNames.get(i));
    if (params.size() > 0) {
      throw new ParserException(firstToken(tree), "Expression " + result.toString() +
        " in function " + func.queryName() + " uses unexpected parameters " + params);
    }
    
    String kind = checkChild(tree, 0);
    
    if (kind.equals("rule optionalinteger")) {
      ParseTree child = tree.getChild(0);
      // _ ⇒ expr may be used regardless of how many parameters the function has
      if (checkChild(child, 0).equals("token UNDERSCORE")) {
        for (int i = 0; i < func.arity(); i++) values.add(null);
      }
      else {
        // i ⇒ expr may only be used if the function has exactly one parameter
        verifyChildIsRule(child, 0, "integer", "an integer");
        int k = readInteger(child.getChild(0));
        if (func.arity() != 1) {
          throw new ParserException(firstToken(tree), "function entry should be a tuple with " +
            func.arity() + " arguments, as this is how the function is declared!");
        }
        values.add(k);
      }
    }
    else {
      verifyChildIsToken(tree, 0, "BRACKETOPEN", "opening bracket (");
      int i = 1;
      for (; checkChild(tree, i).equals("rule optionalinteger"); i += 2) {
        ParseTree child = tree.getChild(i);
        if (checkChild(child, 0).equals("token UNDERSCORE")) values.add(null);
        else values.add(readInteger(child.getChild(0)));
      }
    }

    func.setValue(new Match(values), result);
  }

  private Statement readProgram(ParseTree tree, RequirementsList lst) throws ParserException {
    int i = 0;
    VariableList vars = lst.queryVariables();
    // read declarations and requirements
    for (; i < tree.getChildCount(); i++) {
      String kind = checkChild(tree, i);
      if (kind.equals("token SEPARATOR")) break;
      if (kind.equals("rule statement")) {
        throw new ParserException(firstToken(tree.getChild(i)),
          "Encountered a statement before the program separator ===.");
      }
      if (kind.equals("rule macro")) readMacro(tree.getChild(i));
      else if (kind.equals("rule function")) readFunction(tree.getChild(i));
      else if (kind.equals("rule declaration")) readDeclaration(tree.getChild(i), vars);
      else if (kind.equals("rule formula")) lst.add(readClosedFormula(tree.getChild(i), vars));
      else throw buildError(tree.getChild(i), "unexpected: " + kind);
    }
    // read statements
    ArrayList<Statement> stats = new ArrayList<Statement>();
    for (i++; i < tree.getChildCount()-1; i++) {
      verifyChildIsRule(tree, i, "statement", "a statement");
      stats.add(readStatement(tree.getChild(i), vars));
    }
    if (stats.size() == 1) return stats.get(0);
    return new Block(stats);
  }

  /** ===== Static access functions ===== */

  private static LogicParser createParserFromString(String str, ErrorCollector collector) {
    LogicLexer lexer = new LogicLexer(CharStreams.fromString(str));
    lexer.removeErrorListeners();
    lexer.addErrorListener(collector);
    LogicParser parser = new LogicParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(collector);
    return parser;
  }

  /** Yields a PExpression without variables in it. */
  public static PExpression readPExpressionFromString(String str, DefinitionData defs)
                                                                          throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.onlypexpression();
    collector.throwCollectedExceptions();
    return reader.readFullPExpression(tree, null);
  }

  /** Only really used for more convenient unit testing. */
  public static PExpression readPExpressionFromString(String str) throws ParserException{
    return readPExpressionFromString(str, null);
  }

  /** Yields a PExpression which may have variables in it. */
  public static PExpression readExtendedPExpressionFromString(String str, VariableList vars)
                                                                          throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlypexpression();
    collector.throwCollectedExceptions();
    return reader.readFullPExpression(tree, vars);
  }

  /** Yields a PConstraint without variables in it. */
  public static PConstraint readPConstraintFromString(String str) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlypconstraint();
    collector.throwCollectedExceptions();
    return reader.readFullPConstraint(tree, null);
  }

  /** Yields a PConstraint which may have variables in it. */
  public static PConstraint readExtendedPConstraintFromString(String str, VariableList vars)
                                                                          throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlypconstraint();
    collector.throwCollectedExceptions();
    return reader.readFullPConstraint(tree, vars);
  }

  public static Parameter readParameterFromString(String str) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlyparameter();
    collector.throwCollectedExceptions();
    return reader.readFullParameter(tree);
  }

  /**
   * This method reads a variable declaration from string (without the "declare" keyword) and
   * stores it in the given variable list.
   * Note that this will also give errors if the declaration is well-formed, but the variable was
   * previously declared.
   */
  public static void declare(String str, VariableList lst, DefinitionData defs)
                                                                           throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.internaldeclaration();
    collector.throwCollectedExceptions();
    reader.readInternalDeclaration(tree, lst);
  }

  /** Only really used for more convenient unit testing. */
  public static void declare(String str, VariableList lst) throws ParserException {
    declare(str, lst, null);
  }

  /**
   * This method reads a formula from string and returns it.
   * The formula is allowed to have free parameters.  However, all variables that are used are
   * required to be in vs, otherwise a ParserException will be thrown.
   */
  public static Formula readFormulaFromString(String str, VariableList vs) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlyformula();
    collector.throwCollectedExceptions();
    return reader.readFormula(tree.getChild(0), vs);
  }

  /**
   * This method reads a formula from string and returns it.
   * The formula is not allowed to have free parameters.  All variables that are used are required
   * to be in vs, otherwise a ParserException will be thrown.
   */
  public static Formula readClosedFormulaFromString(String str, VariableList vs, DefinitionData ds)
                                                                          throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    reader._defs = ds;
    ParseTree tree = parser.onlyformula();
    collector.throwCollectedExceptions();
    return reader.readClosedFormula(tree.getChild(0), vs);
  }

  public static Statement readStatementFromString(String str, VariableList vs, DefinitionData defs)
                                                                          throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.statement();
    collector.throwCollectedExceptions();
    return reader.readStatement(tree, vs);
  }

  /** Only really used for more convenient unit testing. */
  public static Statement readStatementFromString(String s, VariableList v) throws ParserException {
    return readStatementFromString(s, v, null);
  }

  public static int readMacroFromString(String str, DefinitionData defs) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.macro();
    collector.throwCollectedExceptions();
    return reader.readMacro(tree);
  }

  public static Function readFunctionFromString(String str, DefinitionData defs)
                                                                        throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.function();
    collector.throwCollectedExceptions();
    return reader.readFunction(tree);
  }

  /** Sets up a (lexer and) parser from the given file, using the given error collector. */
  public static Statement readProgramFromFile(String filename, RequirementsList lst, DefinitionData
                                              defs) throws IOException, ParserException {
    ErrorCollector collector = new ErrorCollector();
    ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(filename));
    LogicLexer lexer = new LogicLexer(input);
    lexer.removeErrorListeners();
    lexer.addErrorListener(collector);
    LogicParser parser = new LogicParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(collector);
    InputReader reader = new InputReader();
    if (defs != null) reader._defs = defs;
    ParseTree tree = parser.program();
    collector.throwCollectedExceptions();
    return reader.readProgram(tree, lst);
  }
}

