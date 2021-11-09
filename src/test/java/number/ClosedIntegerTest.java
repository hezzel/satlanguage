import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.ClauseCollection;
import logic.number.general.ClauseAdder;
import logic.number.range.*;
import logic.number.binary.*;
import logic.number.ClosedInteger;
import logic.number.ConstantInteger;
import logic.number.VariableInteger;
import logic.number.ConditionalInteger;
import logic.number.PlusInteger;

public class ClosedIntegerTest {
  Atom truth() { return new Atom(new Variable("TRUE"), true); }

  @Test
  public void testConstantInteger() {
    ConstantInteger c = new ConstantInteger(-5, truth());
    assertTrue(c.queryMinimum() == -5);
    assertTrue(c.queryMaximum() == -5);
    assertTrue(c.queryKind() == ClosedInteger.BOTH);
    assertTrue(c.toString().equals("-5"));
    // does it make the right range integer?
    assertTrue(c.getRange().queryGeqAtom(-6).toString().equals("TRUE"));
    assertTrue(c.getRange().queryGeqAtom(-5).toString().equals("TRUE"));
    assertTrue(c.getRange().queryGeqAtom(-4).toString().equals("¬TRUE"));
    // does it make the right binary integer?
    assertTrue(c.getBinary().length() == 3);
    assertTrue(c.getBinary().queryNegativeBit().toString().equals("TRUE"));
  }

  @Test
  public void testWrappedRangeVariable() {
    RangeVariable x = new RangeVariable("x", 3, 7, truth());
    ClosedInteger w = new VariableInteger(x);
    assertTrue(w.queryMinimum() == 3);
    assertTrue(w.queryMaximum() == 7);
    assertTrue(w.queryKind() == ClosedInteger.RANGE);
    assertTrue(w.toString().equals("x"));
    assertTrue(w.getRange() == x);
    assertTrue(w.getBinary() == null);
  }

  @Test
  public void testWrappedBinaryVariable() {
    BinaryVariable x = new BinaryVariable("x", -8, 6, truth());
    ClosedInteger w = new VariableInteger(x);
    assertTrue(w.queryMinimum() == -8);
    assertTrue(w.queryMaximum() == 6);
    assertTrue(w.queryKind() == ClosedInteger.BINARY);
    assertTrue(w.toString().equals("x"));
    assertTrue(w.getRange() == null);
    assertTrue(w.getBinary() == x);
  }

  @Test
  public void testConditionalIntegerWithBinaryVariable() {
    BinaryVariable v = new BinaryVariable("var", 4, true, truth());
    ClosedInteger w = new VariableInteger(v);
    Atom myatom = new Atom(new Variable("x"), true);
    ClauseAdder adder = new ClauseAdder() { public void add(ClauseCollection col) { } };
    ConditionalInteger ci = new ConditionalInteger(myatom, w, truth(), adder);
    assertTrue(ci.queryMinimum() == -16);
    assertTrue(ci.queryMaximum() == 15);
    assertTrue(ci.queryKind() == ClosedInteger.BINARY);
    assertTrue(ci.getRange() == null);
    assertTrue(ci.getBinary().toString().equals("x?var"));
    assertTrue(ci.toString().equals("x?var"));
  }

  @Test
  public void testConditionalIntegerWithConstant() {
    ConstantInteger w = new ConstantInteger(7, truth());
    Atom myatom = new Atom(new Variable("x"), true);
    ClauseAdder adder = new ClauseAdder() { public void add(ClauseCollection col) { } };
    ConditionalInteger ci = new ConditionalInteger(myatom, w, truth(), adder);
    assertTrue(ci.queryMinimum() == 0);
    assertTrue(ci.queryMaximum() == 7);
    assertTrue(ci.queryKind() == ClosedInteger.BOTH);
    assertTrue(ci.getRange().queryGeqAtom(3).equals(myatom));
    assertTrue(ci.getBinary().queryBit(2).equals(myatom));
    assertTrue(ci.toString().equals("x?7"));
  }

  @Test
  public void testAddingTwoRangeVariables() {
    ClosedInteger x = new VariableInteger(new RangeVariable("x", 0, 10, truth()));
    ClosedInteger y = new VariableInteger(new RangeVariable("y", -10, 0, truth()));
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.RANGE, truth());
    assertTrue(p.queryMinimum() == -10);
    assertTrue(p.queryMaximum() == 10);
    assertTrue(p.queryKind() == ClosedInteger.RANGE);
    assertTrue(p.toString().equals("(x ⊕ y)"));
    assertTrue(p.getBinary() == null);
    assertTrue(p.getRange() instanceof RangePlus);
    assertTrue(p.getRange().toString().equals("bplus(-10, 10, x ⊕ y)"));
  }

  @Test
  public void testAddingTwoBinaryVariables() {
    ClosedInteger x = new VariableInteger(new BinaryVariable("x", 0, 10, truth()));
    ClosedInteger y = new VariableInteger(new BinaryVariable("y", 3, false, truth()));
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.BINARY, truth());
    assertTrue(p.queryMinimum() == 0);
    assertTrue(p.queryMaximum() == 17);
    assertTrue(p.queryKind() == ClosedInteger.BINARY);
    assertTrue(p.toString().equals("(x ⊞ y)"));
    assertTrue(p.getRange() == null);
    assertTrue(p.getBinary() instanceof BinaryPlus);
    assertTrue(p.getBinary().toString().equals("(x⊞y)"));
  }

  @Test
  public void testAddingConstantToConstant() {
    ClosedInteger x = new ConstantInteger(-12, truth());
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.BOTH, truth());
    assertTrue(p.getRange() instanceof RangeConstant);
    assertTrue(p.getRange().queryMinimum() == -4);
    assertTrue(p.getBinary() instanceof BinaryConstant);
    assertTrue(p.getBinary().queryMinimum() == -4);
  }

  @Test
  public void testAddingConstantToConstantWithLimitedKind() {
    ClosedInteger x = new ConstantInteger(12, truth());
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.BINARY, truth());
    assertTrue(p.getRange() == null);
    assertTrue(p.getBinary() instanceof BinaryConstant);
    assertTrue(p.getBinary().queryMinimum() == 20);
  }

  @Test
  public void testAddingIntegerToRange() {
    ClosedInteger x = new VariableInteger(new RangeVariable("x", 0, 10, truth()));
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.RANGE, truth());
    assertTrue(p.getRange() instanceof RangeInteger);
    assertTrue(p.getRange().toString().equals("(x ⊕8)"));
  }

  @Test
  public void testAddingRangeToInteger() {
    ClosedInteger x = new VariableInteger(new RangeVariable("x", 0, 10, truth()));
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(y, x, ClosedInteger.RANGE, truth());
    assertTrue(p.getRange() instanceof RangeInteger);
    assertTrue(p.getRange().toString().equals("(x ⊕8)"));
  }

  @Test
  public void testAddingBinaryToInteger() {
    ClosedInteger x = new VariableInteger(new BinaryVariable("x", 0, 10, truth()));
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.BINARY, truth());
    assertTrue(p.queryKind() == ClosedInteger.BINARY);
    assertTrue(p.getBinary().toString().equals("(x⊞8)"));
  }

  @Test(expected = java.lang.Error.class)
  public void testRangeAdditionWithBothKind() {
    ClosedInteger x = new VariableInteger(new RangeVariable("x", 0, 10, truth()));
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(y, x, ClosedInteger.BOTH, truth());
  }

  @Test(expected = java.lang.Error.class)
  public void testBinaryAdditionWithRangeKind() {
    ClosedInteger x = new VariableInteger(new BinaryVariable("x", 0, 10, truth()));
    ClosedInteger y = new ConstantInteger(8, truth());
    ClosedInteger p = new PlusInteger(x, y, ClosedInteger.RANGE, truth());
  }
}

