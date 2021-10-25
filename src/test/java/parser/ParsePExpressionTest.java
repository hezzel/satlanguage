package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParsePExpressionTest {
  @Test
  public void testReadPositiveInteger() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("39");
    assertTrue(e.queryConstant());
    assertTrue(e.evaluate(null) == 39);
  }

  @Test
  public void testReadNegativeInteger() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("-71341");
    assertTrue(e.queryConstant());
    assertTrue(e.evaluate(null) == -71341);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadBadInteger() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("01");
  }

  @Test
  public void testReadShortParam() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a");
    assertTrue(e.queryKind() == PExpression.PARAMETER);
    assertTrue(e.toString().equals("a"));
  }

  @Test
  public void testReadLongParam() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a9TT_0b");
    assertTrue(e.queryKind() == PExpression.PARAMETER);
    assertTrue(e.toString().equals("a9TT_0b"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadParamWithIllegalCharacters() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a7@b");
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadParamWithIllegalStart() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("Hello");
  }

  @Test
  public void testSimpleSum() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a+0+d");
    assertTrue(e.queryKind() == PExpression.SUM);
    assertTrue(e.queryLeft().queryKind() == PExpression.SUM);
    assertTrue(e.queryRight().queryKind() == PExpression.PARAMETER);
    assertTrue(e.queryLeft().queryLeft().toString().equals("a"));
    assertTrue(e.queryLeft().queryRight().evaluate(null) == 0);
  }

  @Test
  public void testSimpleProduct() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("abc*3*bc");
    assertTrue(e.queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryRight().queryKind() == PExpression.PARAMETER);
    assertTrue(e.queryRight().toString().equals("bc"));
    assertTrue(e.queryLeft().queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryLeft().queryRight().evaluate(null) == 3);
    assertTrue(e.queryLeft().queryLeft().toString().equals("abc"));
  }

  @Test
  public void testProductSumLeft() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a3*bI+12");
    assertTrue(e.queryKind() == PExpression.SUM);
    assertTrue(e.queryLeft().queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryLeft().queryLeft().queryKind() == PExpression.PARAMETER);
    assertTrue(e.queryLeft().queryLeft().toString().equals("a3"));
    assertTrue(e.queryLeft().queryRight().toString().equals("bI"));
    assertTrue(e.queryRight().evaluate(null) == 12);
  }

  @Test
  public void testProductSumRight() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a3+bI*12");
    assertTrue(e.queryKind() == PExpression.SUM);
    assertTrue(e.queryLeft().queryKind() == PExpression.PARAMETER);
    assertTrue(e.queryLeft().toString().equals("a3"));
    assertTrue(e.queryRight().queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryRight().queryLeft().toString().equals("bI"));
    assertTrue(e.queryRight().queryRight().evaluate(null) == 12);
  }

  @Test
  public void testSimpleDivision() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a/d");
    assertTrue(e.queryKind() == PExpression.DIVISION);
    assertTrue(e.queryLeft().toString().equals("a"));
    assertTrue(e.queryRight().toString().equals("d"));
  }

  @Test
  public void testSimpleModulo() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a%2");
    assertTrue(e.queryKind() == PExpression.MODULO);
    assertTrue(e.queryLeft().toString().equals("a"));
    assertTrue(e.queryRight().evaluate(null) == 2);
  }

  @Test
  public void testDivModMulCombination() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a*b*c/d*e%f");
    assertTrue(e.queryKind() == PExpression.MODULO);
    assertTrue(e.queryRight().toString().equals("f"));
    e = e.queryLeft();
    assertTrue(e.queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryRight().toString().equals("e"));
    e = e.queryLeft();
    assertTrue(e.queryKind() == PExpression.DIVISION);
    assertTrue(e.queryRight().toString().equals("d"));
    e = e.queryLeft();
    assertTrue(e.queryKind() == PExpression.PRODUCT);
    assertTrue(e.queryRight().toString().equals("c"));
    e = e.queryLeft();
    assertTrue(e.toString().equals("a*b"));
  }

  @Test
  public void testSimpleMin() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("min(a*b,c)");
    assertTrue(e.queryKind() == PExpression.MINIMUM);
    assertTrue(e.queryLeft().toString().equals("a*b"));
    assertTrue(e.queryRight().equals(new ParameterExpression("c")));
  }

  @Test
  public void testSimpleMax() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("max(-2, a + b - c)");
    assertTrue(e.queryKind() == PExpression.MAXIMUM);
    assertTrue(e.queryLeft().evaluate(null) == -2);
    assertTrue(e.queryRight().toString().equals("a+b-c"));
  }

  @Test
  public void testMultipleMinus() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a - b - c + d - e");
    assertTrue(e.toString().equals("a-b-c+d-e"));
    assertTrue(e.queryKind() == PExpression.SUM);
    PExpression l = e.queryLeft();
    PExpression r = e.queryRight();
    assertTrue(l.toString().equals("a-b-c"));
    assertTrue(r.toString().equals("d-e"));
    assertTrue(l.queryKind() == PExpression.SUM);
    assertTrue(l.queryRight().toString().equals("-c"));
    assertTrue(l.queryLeft().queryLeft().toString().equals("a"));
  }

  @Test
  public void testPExpressionWithBrackets() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("(1+b)-3");
    assertTrue(e.queryKind() == PExpression.SUM);
    assertTrue(e.queryLeft().queryKind() == PExpression.SUM);
    assertTrue(e.queryLeft().queryLeft().evaluate(null) == 1);
    assertTrue(e.queryLeft().queryRight().toString().equals("b"));
    assertTrue(e.queryRight().evaluate(null) == -3);
  }

  @Test
  public void testPExpressionMinus() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a-3");
      assertTrue(e.queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().toString().equals("a"));
      assertTrue(e.queryRight().evaluate(null) == -3);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testPExpressionWithMinusInMiddle() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a - b + 3 * a");
    assertTrue(e.toString().equals("a-b+3*a"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testPExpressionWithParamvar() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a - b + x[1] * a");
  }

  @Test
  public void testComplicatedPExpression() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a+b*( 12-c) + 13*-7 + (0 + (i+1) * j)");
    assertTrue(e.queryKind() == PExpression.SUM);

    // split in its parts
    PExpression last = e.queryRight();
    PExpression i1j = last.queryRight();
    PExpression zero = last.queryLeft();
    e = e.queryLeft();
    PExpression intexp = e.queryRight();
    e = e.queryLeft();
    PExpression b12c = e.queryRight();
    PExpression a = e.queryLeft();

    // a
    assertTrue(a.queryKind() == PExpression.PARAMETER);
    assertTrue(a.toString().equals("a"));
    // b * (12 - c)
    assertTrue(b12c.queryKind() == PExpression.PRODUCT);
    assertTrue(b12c.queryRight().queryKind() == PExpression.SUM);
    assertTrue(b12c.toString().equals("b*(12-c)"));
    // 13 * -7
    assertTrue(intexp.queryKind() == PExpression.PRODUCT);
    assertTrue(intexp.evaluate(null) == -91);
    // 0
    assertTrue(zero.queryKind() == PExpression.CONSTANT);
    assertTrue(zero.evaluate(null) == 0);
    // (i + 1) * j
    assertTrue(i1j.queryKind() == PExpression.PRODUCT);
    assertTrue(i1j.queryRight().queryKind() == PExpression.PARAMETER);
    assertTrue(i1j.queryLeft().queryKind() == PExpression.SUM);
    assertTrue(i1j.queryLeft().toString().equals("i+1"));
  }
}

