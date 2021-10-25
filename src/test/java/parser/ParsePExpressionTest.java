package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.PExpression;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParsePExpressionTest {
  @Test
  public void testReadPositiveInteger() {
    try {
      PExpression e = InputReader.readPExpressionFromString("39");
      assertTrue(e.queryConstant());
      assertTrue(e.evaluate(null) == 39);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadNegativeInteger() {
    try {
      PExpression e = InputReader.readPExpressionFromString("-71341");
      assertTrue(e.queryConstant());
      assertTrue(e.evaluate(null) == -71341);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadBadInteger() {
    try {
      PExpression e = InputReader.readPExpressionFromString("01");
    }
    catch (ParserException exc) {
      return;
    }
    assertTrue("Expected exception when reading 01", false);
  }

  @Test
  public void testReadShortParam() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a");
      assertTrue(e.queryKind() == PExpression.PARAMETER);
      assertTrue(e.toString().equals("a"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadLongParam() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a9TT_0b");
      assertTrue(e.queryKind() == PExpression.PARAMETER);
      assertTrue(e.toString().equals("a9TT_0b"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testReadParamWithIllegalCharacters() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a7@b");
    }
    catch (ParserException exc) {
      return;
    }
    assertTrue("Expected exception when reading a7@b", false);
  }

  @Test
  public void testReadParamWithIllegalStart() {
    try {
      PExpression e = InputReader.readPExpressionFromString("Hello");
    }
    catch (ParserException exc) {
      return;
    }
    assertTrue("Expected exception when reading Hello as parameter", false);
  }

  @Test
  public void testSimpleSum() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a+0+d");
      assertTrue(e.queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().queryKind() == PExpression.SUM);
      assertTrue(e.queryRight().queryKind() == PExpression.PARAMETER);
      assertTrue(e.queryLeft().queryLeft().toString().equals("a"));
      assertTrue(e.queryLeft().queryRight().evaluate(null) == 0);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testSimpleProduct() {
    try {
      PExpression e = InputReader.readPExpressionFromString("abc*3*bc");
      assertTrue(e.queryKind() == PExpression.PRODUCT);
      assertTrue(e.queryRight().queryKind() == PExpression.PARAMETER);
      assertTrue(e.queryRight().toString().equals("bc"));
      assertTrue(e.queryLeft().queryKind() == PExpression.PRODUCT);
      assertTrue(e.queryLeft().queryRight().evaluate(null) == 3);
      assertTrue(e.queryLeft().queryLeft().toString().equals("abc"));
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testProductSumLeft() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a3*bI+12");
      assertTrue(e.queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().queryKind() == PExpression.PRODUCT);
      assertTrue(e.queryLeft().queryLeft().queryKind() == PExpression.PARAMETER);
      assertTrue(e.queryLeft().queryLeft().toString().equals("a3"));
      assertTrue(e.queryLeft().queryRight().toString().equals("bI"));
      assertTrue(e.queryRight().evaluate(null) == 12);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testProductSumRight() {
    try {
      PExpression e = InputReader.readPExpressionFromString("a3+bI*12");
      assertTrue(e.queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().queryKind() == PExpression.PARAMETER);
      assertTrue(e.queryLeft().toString().equals("a3"));
      assertTrue(e.queryRight().queryKind() == PExpression.PRODUCT);
      assertTrue(e.queryRight().queryLeft().toString().equals("bI"));
      assertTrue(e.queryRight().queryRight().evaluate(null) == 12);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
  }

  @Test
  public void testDoubleMinus() throws ParserException {
    PExpression e = InputReader.readPExpressionFromString("a - b - c + d - e");
    assertTrue(e.toString().equals("a+-1*b+-1*c+d+-1*e"));
    assertTrue(e.queryKind() == PExpression.SUM);
    PExpression l = e.queryLeft();
    PExpression r = e.queryRight();
    assertTrue(l.toString().equals("a+-1*b+-1*c"));
    assertTrue(r.toString().equals("d+-1*e"));
    assertTrue(l.queryKind() == PExpression.SUM);
    assertTrue(l.queryRight().toString().equals("-1*c"));
    assertTrue(l.queryLeft().queryLeft().toString().equals("a"));
  }

  @Test
  public void testPExpressionWithBrackets() {
    try {
      PExpression e = InputReader.readPExpressionFromString("(1+b)+-3");
      assertTrue(e.queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().queryKind() == PExpression.SUM);
      assertTrue(e.queryLeft().queryLeft().evaluate(null) == 1);
      assertTrue(e.queryLeft().queryRight().toString().equals("b"));
      assertTrue(e.queryRight().evaluate(null) == -3);
    }
    catch (ParserException exc) {
      assertTrue(exc.toString(), false);
    }
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
    assertTrue(e.toString().equals("a+-1*b+3*a"));
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
    assertTrue(b12c.toString().equals("b*(12+-1*c)"));
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

