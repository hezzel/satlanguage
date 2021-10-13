package language.parser;

import logic.parameter.*;

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

  private PConstraint readPConstraint(ParseTree tree) {
    verifyChildIsRule(tree, 0, "pconstraintunit", "a basic pconstraint");
    if (tree.getChildCount() == 1)
      return readPConstraintUnit(tree.getChild(0));
    ArrayList<PConstraint> parts = new ArrayList<PConstraint>();
    parts.add(readPConstraintUnit(tree.getChild(0)));
    for (int i = 1; i < tree.getChildCount(); i++) {
      ParseTree child = tree.getChild(i);
      verifyChildIsRule(child, 1, "pconstraintunit", "a basic pconstraint");
      parts.add(readPConstraintUnit(child.getChild(1)));
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

  private PConstraint readPConstraintUnit(ParseTree tree) {
    String kind = checkChild(tree, 0);
    if (kind.equals("token TOP")) return new TrueConstraint();
    if (kind.equals("token BOTTOM")) return new FalseConstraint();
    if (kind.equals("token BRACKETOPEN")) {
      verifyChildIsRule(tree, 1, "pconstraint", "a PConstraint");
      verifyChildIsToken(tree, 2, "BRACKETCLOSE", "closing bracket ')'");
      return readPConstraint(tree.getChild(1));
    }
    verifyChildIsRule(tree, 0, "pconstraintrelation", "a relation");
    return readPConstraintRelation(tree.getChild(0));
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
    verifyChildIsToken(tree, 1, "NEQ", "comparison or inequality symbol");
    return new NeqConstraint(left, right);
  }

  private PConstraint readFullPConstraint(ParseTree tree) {
    verifyChildIsRule(tree, 0, "pconstraint", "a parameter expression");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readPConstraint(tree.getChild(0));
  }
  
  /** ===== Reading Parameters ===== */

  private Parameter readParameter(ParseTree tree) {
    verifyChildIsToken(tree, 0, "IDENTIFIER", "a parameter name");
    String name = tree.getChild(0).getText();
    verifyChildIsToken(tree, 1, "IN", "set inclusion symbol ∈");
    verifyChildIsToken(tree, 2, "BRACEOPEN", "set opening brace {");
    verifyChildIsRule(tree, 3, "pexpression", "a parameter expression");
    PExpression minimum = readPExpression(tree.getChild(3));
    verifyChildIsToken(tree, 4, "DOTS", "two dots ..");
    verifyChildIsRule(tree, 5, "pexpression", "a parameter expression");
    PExpression maximum = readPExpression(tree.getChild(5));
    verifyChildIsToken(tree, 6, "BRACECLOSE", "set closing brace }");
    PConstraint constraint;
    if (tree.getChildCount() > 7) {
      verifyChildIsToken(tree, 7, "WITH", "keyword 'with'");
      verifyChildIsRule(tree, 8, "pconstraint", "a parameter constraint");
      constraint = readPConstraint(tree.getChild(8));
    }
    else constraint = new TrueConstraint();
    return new Parameter(name, minimum, maximum, constraint);
  }

  private Parameter readFullParameter(ParseTree tree) {
    verifyChildIsRule(tree, 0, "parameter", "a parameter");
    verifyChildIsToken(tree, 1, "EOF", "end of input");
    return readParameter(tree.getChild(0));
  }

  private ParameterList readParameterList(ParseTree tree) {
    ArrayList<Parameter> pars = new ArrayList<Parameter>();
    verifyChildIsRule(tree, 0, "parameter", "a parameter");
    pars.add(readParameter(tree.getChild(0)));
    for (int i = 1; i < tree.getChildCount(); i += 2) {
      verifyChildIsToken(tree, i, "COMMA", "a comma");
      verifyChildIsRule(tree, i+1, "parameter", "a parameter");
      pars.add(readParameter(tree.getChild(i+1)));
    }
    return new ParameterList(pars);
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

  public static PConstraint readPConstraintFromString(String str) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlypconstraint();
    collector.throwCollectedExceptions();
    return reader.readFullPConstraint(tree);
  }

  public static Parameter readParameterFromString(String str) throws ParserException {
    ErrorCollector collector = new ErrorCollector();
    LogicParser parser = createParserFromString(str, collector);
    InputReader reader = new InputReader();
    ParseTree tree = parser.onlyparameter();
    collector.throwCollectedExceptions();
    return reader.readFullParameter(tree);
  }
}

