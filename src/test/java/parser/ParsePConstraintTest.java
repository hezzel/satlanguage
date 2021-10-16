package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import logic.parameter.PConstraint;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParsePConstraintTest {
  @Test
  public void testReadTop() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("\\top");
      assertTrue(c.isTop());
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadBottom() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("⊥");
      assertTrue(c.queryKind() == PConstraint.CONSTANT);
      assertFalse(c.isTop());
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadSmaller() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("i < j");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("i < j"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadLarger() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("i > j");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("j < i"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadLeq() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("i <= 3");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("i < 4"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadGeq() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("(i ≥ 2)");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("2 < i+1"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadNeq() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("2 != 3");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("2 ≠ 3"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadEqual() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("i = j + 1");
      assertTrue(c.queryKind() == PConstraint.RELATION);
      assertTrue(c.toString().equals("i = j+1"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  public void testReadNegation() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("¬(a = b ∧ j >= i)");
      assertTrue(c.toString().equals("a ≠ b ∨ j < i"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
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
  public void testReadNestedConjunctionAndDisjunction() {
    try {
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
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  public void testMixedDisjunctionAndConjunctionNotAllowed() {
    try {
      PConstraint c = InputReader.readPConstraintFromString("a < b /\\ c > d \\/ \\bot");
    }
    catch (ParserException exc) {
    }
    assertTrue("expected to encounter an error on parsing mixed ∧ and ∨ without brackets", false);
  }
}

