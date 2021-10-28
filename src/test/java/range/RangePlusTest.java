import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Solution;
import logic.parameter.Parameter;
import logic.range.RangeInteger;
import logic.range.RangeConstant;
import logic.range.RangeVariable;
import logic.range.RangePlus;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.TreeSet;

public class RangePlusTest {
  private RangePlus createUnboundedPlus() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    RangeVariable y = new RangeVariable("y", 3, 7, t);
    return new RangePlus(x, y);
  }

  private RangePlus createBoundedPlus() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    RangeVariable y = new RangeVariable("y", 3, 7, t);
    return new RangePlus(x, y, 5, 10);
  }

  @Test
  public void testUnboundedRPBasics() {
    RangePlus rp = createUnboundedPlus();
    assertTrue(rp.queryMinimum() == 4);
    assertTrue(rp.queryMaximum() == 12);
    assertTrue(rp.toString().equals("bplus(4, 12, x ⊕ y)"));
    assertTrue(rp.queryGeqAtom(2).equals(new Atom(new Variable("TRUE"), true)));
    assertTrue(rp.queryGeqAtom(4).toString().equals("TRUE"));
    assertTrue(rp.queryGeqAtom(6).toString().equals("x⊕y≥6"));
    assertTrue(rp.queryGeqAtom(12).toString().equals("x⊕y≥12"));
    assertTrue(rp.queryGeqAtom(13).toString().equals("¬TRUE"));
  }

  @Test
  public void testboundedRPBasics() {
    RangePlus rp = createBoundedPlus();
    assertTrue(rp.queryMinimum() == 5);
    assertTrue(rp.queryMaximum() == 10);
    assertTrue(rp.toString().equals("bplus(5, 10, x ⊕ y)"));
    assertTrue(rp.queryGeqAtom(2).toString().equals("TRUE"));
    assertTrue(rp.queryGeqAtom(5).toString().equals("TRUE"));
    assertTrue(rp.queryGeqAtom(6).toString().equals("x⊕y≥6"));
    assertTrue(rp.queryGeqAtom(10).toString().equals("x⊕y≥10"));
    assertTrue(rp.queryGeqAtom(11).toString().equals("¬TRUE"));
  }

  @Test
  public void testRedundantBounds() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 2, t);
    RangeVariable y = new RangeVariable("y", 1, 2, t);
    RangePlus p = new RangePlus(x, y, 1, 4);
    assertTrue(p.queryMinimum() == 2);
    assertTrue(p.queryMaximum() == 4);
    assertTrue(p.toString().equals("bplus(2, 4, x ⊕ y)"));
    p = new RangePlus(x, y, 1, 3);
    assertTrue(p.toString().equals("bplus(2, 3, x ⊕ y)"));
  }

  @Test
  public void setMiddleBoundsInBigSum() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 1, 10, t);
    RangeVariable b = new RangeVariable("b", 0, 10, t);
    RangeVariable c = new RangeVariable("c", 0, 10, t);
    RangeVariable d = new RangeVariable("d", 1, 10, t);
    RangePlus abcd = new RangePlus(new RangePlus(a,b), new RangePlus(c,d));
    RangeInteger bounded = abcd.setPracticalBounds(4,7);
    assertTrue(bounded.toString().equals(
      "bplus(4, 7, bplus(1, 6, a ⊕ b) ⊕ bplus(1, 6, c ⊕ d))"
    ));
  }

  @Test
  public void setZeroBoundsInPlusMinusSum() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", -10, 5, t);
    RangeVariable b = new RangeVariable("b", -10, 5, t);
    RangeVariable c = new RangeVariable("c", 0, 3, t);
    RangePlus abcd = new RangePlus(new RangePlus(a,b), c);
    RangeInteger bounded = abcd.setPracticalBounds(-1,1);
    assertTrue(bounded.toString().equals(
      "bplus(-1, 1, bplus(-4, 1, a ⊕ b) ⊕ c)"
    ));
  }

  @Test
  public void setLowBounds() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 1, 10, t);
    RangeVariable b = new RangeVariable("b", 0, 8, t);
    RangeVariable c = new RangeVariable("c", -4, 2, t);
    RangeVariable d = new RangeVariable("d", 1, 7, t);
    RangePlus abcd = new RangePlus(new RangePlus(a,b), new RangePlus(c,d));
    RangeInteger bounded = abcd.setPracticalBounds(-5,0);
    assertTrue(bounded.toString().equals(
      "bplus(-2, 0, bplus(-3, -1, c ⊕ d) ⊕ bplus(1, 3, a ⊕ b))"));
  }

  @Test
  public void setHighBounds() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 1, 10, t);
    RangeVariable b = new RangeVariable("b", 0, 8, t);
    RangeVariable c = new RangeVariable("c", -4, 2, t);
    RangePlus abc = new RangePlus(a, new RangePlus(b, c));
    RangeInteger bounded = abc.setPracticalBounds(18, 23);
    assertTrue(bounded.toString().equals("bplus(18, 20, a ⊕ bplus(8, 10, b ⊕ c))"));
  }

  @Test
  public void setUnreasonableBounds() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 0, 8, t);
    RangeVariable b = new RangeVariable("b", 0, 8, t);
    RangePlus ab = new RangePlus(a, b);
    RangeInteger bounded = ab.setPracticalBounds(-5, -3);
    assertTrue(bounded.toString().equals("bplus(0, 0, a ⊕ b)"));
  }

  @Test
  public void setConflictingBounds() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 0, 8, t);
    RangeVariable b = new RangeVariable("b", 0, 8, t);
    RangeVariable c = new RangeVariable("c", 0, 8, t);
    RangeVariable d = new RangeVariable("d", 0, 8, t);
    RangePlus abcd = new RangePlus(new RangePlus(a, b), new RangePlus(c, d));
    RangeInteger bounded = abcd.setPracticalBounds(5, 4);
    assertTrue(bounded.toString().equals("bplus(4, 4, bplus(0, 4, a ⊕ b) ⊕ bplus(0, 4, c ⊕ d))"));
  }

  @Test
  public void testVariablePlusConstantClauses() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    RangeConstant two = new RangeConstant(2, t);
    RangePlus p = new RangePlus(x, two);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    p.addWelldefinednessClauses(col);
    // col should contain x ≥ i → x + 2 ≥ i + 2 for i in {2..5}
    // and x < i → x + 2 < i + 2 for i in {2..5}
    assertTrue(col.size() == 8);
    assertTrue(col.contains("¬x≥5 ∨ 2⊕x≥7"));
    assertTrue(col.contains("x≥5 ∨ ¬2⊕x≥7"));
    assertTrue(col.contains("¬x≥2 ∨ 2⊕x≥4"));
    assertTrue(col.contains("x≥2 ∨ ¬2⊕x≥4"));
  }

  @Test
  public void testVariablePlusConstantBoundedClauses() {
    Variable t = new Variable("TRUE");
    RangeVariable x = new RangeVariable("x", 1, 5, t);
    RangeConstant two = new RangeConstant(2, t);
    RangePlus p = new RangePlus(two, x, 4, 6);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    p.addWelldefinednessClauses(col);
    // col should roughly contain x ≥ i → x + 2 ≥ MIN(i + 2, 6) for i in {2..5}
    // and x < i → x + 2 < MAX(i + 2, 5) for i in {2..5}

    assertTrue(col.contains("¬x≥3 ∨ 2⊕x≥5"));   // x≥3 → 2⊕x≥5
    assertTrue(col.contains("¬x≥4 ∨ 2⊕x≥6"));   // x≥4 → 2⊕≥6
    assertTrue(col.contains("x≥3 ∨ ¬2⊕x≥5"));   // 2⊕x≥5 → x≥3
    assertTrue(col.contains("x≥4 ∨ ¬2⊕x≥6"));   // 2⊕x≥6 → x≥4
    // x ≥ 2 → x+2 ≥ 4 omitted, since x+2 ≥ 4 holds regardless with minimum 4
    // x < 5 → x+2 < 7 omitted, since x+2 < 7 holds regardless with maximum 6
    // x≥5 → 2⊕x≥6 is omitted because it is implied by x≥ 4 → 2⊕x≥6
    // 2⊕x≥5 → x≥2 is omitted because it is implied by 2⊕x≥5 → x≥3
    assertTrue(col.size() == 4);
  }

  @Test
  public void testUnboundedClauses() {
    RangePlus rp = createUnboundedPlus();
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    rp.addWelldefinednessClauses(col);
    // col should contain:
    // for i in {1..5}, for j in {3..7}: x ≥ i ∧ y ≥ j → x+y ≥ i+j (EXCEPT for i=1, j=3)
    // for i in {1..5}, for j in {3..7}: x ≤ i ∧ y ≤ j → x+y ≤ i+j (EXCEPT for i=5, j=7)
    // total: 5*5-1 + 5*5-1 = 48
    assertTrue(col.size() == 48);
    assertTrue(col.contains("¬x≥4 ∨ ¬y≥4 ∨ x⊕y≥8"));
    assertTrue(col.contains("x≥5 ∨ y≥5 ∨ ¬x⊕y≥9"));
    assertTrue(col.contains("¬x≥4 ∨ x⊕y≥7"));   // x ≥ 4 ∧ y ≥ ymin → x+y ≥ 4+yman = y
  }

  @Test
  public void testBoundedClauses() {
    RangePlus rp = createBoundedPlus();
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    rp.addWelldefinednessClauses(col);
    // of the 48 clauses, we omit x ≥ i ∧ y ≥ j → x+y ≥ i+j when i + j ∈ {4,5} (so 0 for 4 and 2
    // for 5) and when i + j ∈ {11,12} (so 2 for 11 and 1 for 12); the former because x+y ≥ 5
    // holds regardless, the latter because in all those cases we have a requirement x+y ≥ 10 if
    // (x,y) ≥ (i',j') for smaller values than (i,j);
    // we also omit x ≤ i ∧ y ≤ j → x+y ≤ i+j when i+j in {4,10,11,12} (so 1 for 4, 3 for 10, 2 for
    // 11 and 0 for 12); here, x ≤ 1 ∧ y ≤ 3 → x+y ≤ 4 is omitted because we already have x ≤ 1 ∧
    // y ≤ 4 → x+y ≤ 5, which implies it
    // hence, in total 11 are omitted, leaving 37 clauses
    assertTrue(col.size() == 37);
    assertTrue(col.contains("¬x≥4 ∨ ¬y≥4 ∨ x⊕y≥8"));  // x ≥ 4 ∧ y ≥ 4 → x+y ≥ 8
    assertTrue(col.contains("¬x≥4 ∨ x⊕y≥7"));         // x ≥ 4 ∧ y ≥ ymin → x+y ≥ 4+ymin = 7
    assertTrue(col.contains("x≥5 ∨ y≥5 ∨ ¬x⊕y≥9"));   // x ≤ 4 ∧ y ≤ 4 → x+y ≤ 8
    assertTrue(col.contains("x≥3 ∨ y≥4 ∨ ¬x⊕y≥6"));   // x ≤ 2 ∧ y ≤ 3 → x+y ≤ 5
    assertFalse(col.contains("x≥2 ∨ y≥4 ∨ ¬x⊕y≥5"));  // x ≤ 1 ∧ y ≤ 3 → x+y ≤ 4 is unnecessary
    assertFalse(col.contains("¬x≥5 ∨ ¬y≥7 ∨ x⊕y≥12"));
    assertFalse(col.contains("¬x≥5 ∨ ¬y≥7 ∨ x⊕y≥10"));
  }

  @Test
  public void testNestedPlusClausesExpectedCount() {
    Variable t = new Variable("TRUE");
    RangeVariable a = new RangeVariable("a", 1, 5, t);
    RangeVariable b = new RangeVariable("b", 1, 5, t);
    RangeVariable c = new RangeVariable("c", 1, 5, t);
    RangeVariable d = new RangeVariable("d", 1, 5, t);
    RangePlus ab = new RangePlus(a, b);
    RangePlus cd = new RangePlus(c, d);
    RangePlus rp = new RangePlus(ab, cd, 19, 20);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar a");
    col.addToMemory("rangevar b");
    col.addToMemory("rangevar c");
    col.addToMemory("rangevar d");
    rp.addWelldefinednessClauses(col);
    // a + b ≥ 10 <-> a ≥ 5 ∧ b ≥ 5
    assertTrue(col.contains("a≥5 ∨ ¬a⊕b≥10"));
    assertTrue(col.contains("b≥5 ∨ ¬a⊕b≥10"));
    assertTrue(col.contains("¬a≥5 ∨ ¬b≥5 ∨ a⊕b≥10"));
    // c + d ≥ 10 <-> c ≥ 5 ∧ d ≥ 5
    assertTrue(col.contains("c≥5 ∨ ¬c⊕d≥10"));
    assertTrue(col.contains("d≥5 ∨ ¬c⊕d≥10"));
    assertTrue(col.contains("¬c≥5 ∨ ¬d≥5 ∨ c⊕d≥10"));
    // (a + b) + (c + d) ≥ 20 <-> a + b ≥ 10 ∧ c + d ≥ 10
    assertTrue(col.contains("a⊕b≥10 ∨ ¬bplus(9, 10, a ⊕ b)⊕bplus(9, 10, c ⊕ d)≥20"));
    assertTrue(col.contains("c⊕d≥10 ∨ ¬bplus(9, 10, a ⊕ b)⊕bplus(9, 10, c ⊕ d)≥20"));
    assertTrue(col.contains("¬a⊕b≥10 ∨ ¬c⊕d≥10 ∨ bplus(9, 10, a ⊕ b)⊕bplus(9, 10, c ⊕ d)≥20"));
    assertTrue(col.size() == 9);
  }
}

