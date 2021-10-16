package language.parser;

import logic.sat.Variable;
import logic.parameter.*;
import logic.formula.*;
import logic.VariableList;
import language.execution.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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

  public InputReader() {
    _vocabulary = LogicParser.VOCABULARY;
    _ruleNames = LogicParser.ruleNames;
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
   * Returns the parametrised boolean variable represented by the identifier starting the tree,
   * and updates the given arguments list to add all the pexpressions in the arguments list to the
   * variable.  If there is no ParamBoolVar declared with that name, or if the length of the
   * arguments list does not match the expected number of parameters, a ParserException is thrown
   * instead.
   */
  private ParamBoolVar readQuantifiedBooleanVariable(ParseTree tree, VariableList lst,
                                           ArrayList<PExpression> args) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "the name of a ParamBoolVar");
    verifyChildIsToken(tree, 1, "SBRACKETOPEN", "indexing opening bracket [");
    verifyChildIsRule(tree, 2, "pexprlist", "a list of PExpressions");
    verifyChildIsToken(tree, 3, "SBRACKETCLOSE", "indexing closing bracket ]");
    String name = tree.getChild(0).getText();
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
    ParseTree pexprs = tree.getChild(2);
    if (pexprs.getChildCount() != 2 * expected.size() - 1) {
      throw new ParserException(firstToken(tree), "Illegal use of variable " + name + " declared " +
        "with " + expected.size() + " parameters, but used with " + ((pexprs.getChildCount()-1)/2)
        + " parameters.");
    }
    for (int i = 0; i < expected.size(); i++) args.add(readPExpression(pexprs.getChild(2*i)));
    return x;
  }

  /** ===== Reading PExpressions ===== */

  private PExpression readPExpression(ParseTree tree) {
    if (tree.getChildCount() != 1) {
      throw buildError(tree, "pexpression has unexpected shape (" + tree.getChildCount() +
        " children)");
    }
    String kind = checkChild(tree, 0);
    if (kind.equals("rule pexpressiontimes")) return readPExpressionTimes(tree.getChild(0));
    if (kind.equals("rule pexpressionplus")) return readPExpressionPlus(tree.getChild(0));
    throw buildError(tree, "pexpression has unexpected kind (" + kind + ")");
  }

  private PExpression readPExpressionTimes(ParseTree tree) {
    if (tree.getChildCount() == 1) {
      verifyChildIsRule(tree, 0, "pexpressionunit", "a parameter or integer");
      return readPExpressionUnit(tree.getChild(0));
    }
    verifyChildIsRule(tree, 0, "pexpressionunit", "a parameter or integer");
    verifyChildIsToken(tree, 1, "TIMES", "TIMES (*)");
    verifyChildIsRule(tree, 2, "pexpressiontimes", "a pexpression without addition");
    PExpression part1 = readPExpressionUnit(tree.getChild(0));
    PExpression part2 = readPExpressionTimes(tree.getChild(2));
    return new ProductExpression(part1, part2);
  }

  private PExpression readPExpressionUnit(ParseTree tree) {
    String kind = checkChild(tree, 0);
    if (kind.equals("token IDENTIFIER")) return new ParameterExpression(tree.getText());
    if (kind.equals("token INTEGER")) {
      try { return new ConstantExpression(Integer.parseInt(tree.getText())); }
      catch (NumberFormatException exc) { throw buildError(tree, "could not parse integer"); }
    }
    if (kind.equals("token MINUS")) {
      verifyChildIsToken(tree, 1, "INTEGER", "a positive integer");
      try { return new ConstantExpression(Integer.parseInt(tree.getText())); }
      catch (NumberFormatException exc) { throw buildError(tree, "could not parse integer"); }
    }
    verifyChildIsToken(tree, 0, "BRACKETOPEN", "an identifier, integer or opening bracket");
    verifyChildIsRule(tree, 1, "pexpression", "a parameter expression");
    verifyChildIsToken(tree, 2, "BRACKETCLOSE", "a closing bracket");
    return readPExpression(tree.getChild(1));
  }

  private PExpression readPExpressionPlus(ParseTree tree) {
    verifyChildIsRule(tree, 0, "pexpressiontimes", "a pexpression without addition");
    verifyChildIsRule(tree, 2, "pexpression", "a parameter expression");
    PExpression part1 = readPExpressionTimes(tree.getChild(0));
    PExpression part2 = readPExpression(tree.getChild(2));
    String kind = checkChild(tree, 1);
    if (kind.equals("token PLUS")) return new SumExpression(part1, part2);
    else if (part2.queryConstant()) {
      part2 = new ConstantExpression(0 - part2.evaluate(null));
      return new SumExpression(part1, part2);
    }
    else return new SumExpression(part1, new ProductExpression(new ConstantExpression(-1), part2));
  }

  private PExpression readFullPExpression(ParseTree tree) {
    verifyChildIsRule(tree, 0, "pexpression", "a parameter expression");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readPExpression(tree.getChild(0));
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
      return readPConstraintRelation(tree.getChild(0));
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
      ParamBoolVar x = readQuantifiedBooleanVariable(tree.getChild(0), lst, exprs);
      return new ParamBoolVarConstraint(x, exprs, true);
    }
  }

  private PConstraint readPConstraintRelation(ParseTree tree) {
    verifyChildIsRule(tree, 0, "pexpression", "a parameter expression");
    verifyChildIsRule(tree, 2, "pexpression", "a parameter expression");
    PExpression left = readPExpression(tree.getChild(0));
    PExpression right = readPExpression(tree.getChild(2));
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
    PExpression minimum = readPExpression(tree.getChild(1));
    verifyChildIsToken(tree, 2, "DOTS", "two dots ..");
    verifyChildIsRule(tree, 3, "pexpression", "a parameter expression");
    PExpression maximum = readPExpression(tree.getChild(3));
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

  private ParameterList readParameterList(ParseTree tree) throws ParserException {
    ArrayList<Parameter> pars = new ArrayList<Parameter>();
    verifyChildIsRule(tree, 0, "parameter", "a parameter");
    pars.add(readParameter(tree.getChild(0)));
    for (int i = 1; i < tree.getChildCount(); i += 2) {
      verifyChildIsToken(tree, i, "COMMA", "a comma");
      verifyChildIsRule(tree, i+1, "parameter", "a parameter");
      pars.add(readParameter(tree.getChild(i+1)));
    }
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
    verifyChildIsRule(tree, 2, "end", "end of line (or end of input)");
    String kind = checkChild(tree, 1);
    if (kind.equals("rule boolvardec")) readBoolVarDec(tree.getChild(1), lst);
    else if (kind.equals("rule paramboolvardec")) readParamBoolVarDec(tree.getChild(1), lst);
    else throw buildError(tree, "encountered " + kind +
      ", expected rule boolvardec or rule paramboolvardec");
  }

  private void readBoolVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "an identifier (variable name)");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "BOOLTYPE", "Bool");
    String name = tree.getChild(0).getText();
    if (lst.isDeclared(name)) throw new ParserException(firstToken(tree),
      "declaring variable " + name + " which was previously declared!");
    lst.registerBooleanVariable(name);
  }

  private void readParamBoolVarDec(ParseTree tree, VariableList lst) throws ParserException {
    verifyChildIsRule(tree, 0, "paramvar", "a parametrised variable x[i1,...,in]");
    verifyChildIsToken(tree, 1, "TYPEOF", "typeof symbol ::");
    verifyChildIsToken(tree, 2, "BOOLTYPE", "Bool");
    verifyChildIsToken(tree, 3, "FOR", "keyword 'for'");
    verifyChildIsRule(tree, 4, "parameterlist", "a list of parameters");
    ParameterList params = readParameterList(tree.getChild(4));
    readParamBoolVarForDeclaration(tree.getChild(0), params, lst);
  }

  private void readParamBoolVarForDeclaration(ParseTree tree, ParameterList params,
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

    lst.registerParametrisedBooleanVariable(name, params);
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
    ParamBoolVar x = readQuantifiedBooleanVariable(tree, lst, given);
    return new QuantifiedAtom(x, true, given);
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

  public static PExpression readPExpressionFromString(String str) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlypexpression();
    collector.throwCollectedExceptions();
    return reader.readFullPExpression(tree);
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
   * This method reads a variable declaration from string and stores it in the given variable list.
   * Note that this will also give errors if the declaration is well-formed, but the variable was
   * previously declared.
   */
  public static void readDeclarationFromString(String str, VariableList lst)
                                                                      throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.declaration();
    collector.throwCollectedExceptions();
    reader.readDeclaration(tree, lst);
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
    ParseTree tree = parser.requirement();
    collector.throwCollectedExceptions();
    return reader.readFormula(tree.getChild(0), vs);
  }
}

