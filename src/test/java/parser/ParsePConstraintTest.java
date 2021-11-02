package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import language.parser.InputReader;
import language.parser.ParserException;
import language.parser.DefinitionData;

public class ParsePConstraintTest {
  @Test
  public void testReadTop() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("\\top");
    assertTrue(c.isTop());
  }

  @Test
  public void testReadBottom() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("⊥");
    assertTrue(c.queryKind() == PConstraint.CONSTANT);
    assertFalse(c.isTop());
  }

  @Test
  public void testReadSmaller() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("i < j");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("i < j"));
  }

  @Test
  public void testReadLarger() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("i > j");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("j < i"));
  }

  @Test
  public void testReadLeq() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("i <= 3");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("i < 4"));
  }

  @Test
  public void testReadGeq() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("(i ≥ 2)");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("2 < i+1"));
  }

  @Test
  public void testReadNeq() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("2 != 3");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("2 ≠ 3"));
  }

  @Test
  public void testReadEqual() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("i = j + 1");
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("i = j+1"));
  }

  @Test
  public void testReadNegation() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("¬(a = b ∧ j >= i)");
    assertTrue(c.toString().equals("a ≠ b ∨ j < i"));
  }

  @Test
  public void testReadProperty() throws ParserException {
    DefinitionData dd = new DefinitionData();
    InputReader.readPropertyFromString("property P { (1,2) ; (3,7,_) }", dd);
    PConstraint c = InputReader.readPConstraintFromString("P(2)", dd);
    assertTrue(c.queryKind() == PConstraint.RELATION);
    assertTrue(c.toString().equals("P(2)"));
    c = InputReader.readPConstraintFromString("P(i, 7)", dd);
    assertTrue(c.toString().equals("P(i,7)"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testUseVariable() throws ParserException {
    InputReader.readPConstraintFromString("a = b ∧ c");
  }

  @Test(expected = language.parser.ParserException.class)
  public void testUseParamBoolVar() throws ParserException {
    InputReader.readPConstraintFromString("a < 0 ∨ x[3]");
  }

  @Test
  public void testReadNestedConjunctionAndDisjunction() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("a < b ∧ b != 3 \\and (a ≥ 4 ∨ ⊤)");
    assertTrue(c instanceof AndConstraint);
    AndConstraint a = (AndConstraint)c;
    assertTrue(a.queryLeft().toString().equals("a < b"));
    assertTrue(a.queryRight() instanceof AndConstraint);
    AndConstraint b = (AndConstraint)a.queryRight();
    assertTrue(b.queryLeft().toString().equals("b ≠ 3"));
    assertTrue(b.queryRight() instanceof OrConstraint);
    assertTrue(b.queryRight().toString().equals("4 < a+1 ∨ ⊤"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMixedDisjunctionAndConjunctionNotAllowed() throws ParserException {
    PConstraint c = InputReader.readPConstraintFromString("a < b /\\ c > d \\/ \\bot");
  }
}

