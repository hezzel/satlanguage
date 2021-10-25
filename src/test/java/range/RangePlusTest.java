import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Solution;
import logic.parameter.Parameter;
import logic.range.RangeConstant;
import logic.range.RangeVariable;
import logic.range.RangePlus;
import language.parser.InputReader;
import language.parser.ParserException;
import java.util.TreeSet;

public class RangePlusTest {
  private RangePlus createUnboundedPlus() {
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
    RangeVariable y = new RangeVariable("y", 3, 7, f, t);
    return new RangePlus(x, y);
  }

  private RangePlus createBoundedPlus() {
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
    RangeVariable y = new RangeVariable("y", 3, 7, f, t);
    return new RangePlus(x, y, 5, 10);
  }

  @Test
  public void testUnboundedRPBasics() {
    RangePlus rp = createUnboundedPlus();
    assertTrue(rp.queryMinimum() == 4);
    assertTrue(rp.queryMaximum() == 12);
    assertTrue(rp.toString().equals("bplus(4, 12, x ⊕ y)"));
    assertTrue(rp.queryGeqVariable(2).equals(new Variable("TRUE")));
    assertTrue(rp.queryGeqVariable(4).toString().equals("TRUE"));
    assertTrue(rp.queryGeqVariable(6).toString().equals("x⊕y≥6"));
    assertTrue(rp.queryGeqVariable(12).toString().equals("x⊕y≥12"));
    assertTrue(rp.queryGeqVariable(13).equals(new Variable("FALSE")));
  }

  @Test
  public void testboundedRPBasics() {
    RangePlus rp = createBoundedPlus();
    assertTrue(rp.queryMinimum() == 5);
    assertTrue(rp.queryMaximum() == 10);
    assertTrue(rp.toString().equals("bplus(5, 10, x ⊕ y)"));
    assertTrue(rp.queryGeqVariable(2).toString().equals("TRUE"));
    assertTrue(rp.queryGeqVariable(5).toString().equals("TRUE"));
    assertTrue(rp.queryGeqVariable(6).toString().equals("x⊕y≥6"));
    assertTrue(rp.queryGeqVariable(10).toString().equals("x⊕y≥10"));
    assertTrue(rp.queryGeqVariable(11).toString().equals("FALSE"));
  }

  @Test
  public void testRedundantBounds() {
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 2, f, t);
    RangeVariable y = new RangeVariable("y", 1, 2, f, t);
    RangePlus p = new RangePlus(x, y, 1, 4);
    assertTrue(p.queryMinimum() == 2);
    assertTrue(p.queryMaximum() == 4);
    assertTrue(p.toString().equals("bplus(2, 4, x ⊕ y)"));
    p = new RangePlus(x, y, 1, 3);
    assertTrue(p.toString().equals("bplus(2, 3, x ⊕ y)"));
  }

  @Test
  public void testVariablePlusConstantClauses() {
    Variable t = new Variable("TRUE");
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
    RangeConstant two = new RangeConstant(2, f, t);
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
    Variable f = new Variable("FALSE");
    RangeVariable x = new RangeVariable("x", 1, 5, f, t);
    RangeConstant two = new RangeConstant(2, f, t);
    RangePlus p = new RangePlus(two, x, 4, 6);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    p.addWelldefinednessClauses(col);
    // col should contain x ≥ i → x + 2 ≥ MIN(i + 2, 6) for i in {2..5}
    // and x < i → x + 2 < MAX(i + 2, 5) for i in {2..5}
    assertTrue(col.size() == 6);
    // x ≥ 2 → x+2 ≥ 4 omitted, since x+2 ≥ 4 holds regardless
    assertTrue(col.contains("¬x≥3 ∨ 2⊕x≥5"));
    assertTrue(col.contains("¬x≥5 ∨ 2⊕x≥6"));
    assertTrue(col.contains("¬x≥5 ∨ 2⊕x≥6"));
    // x < 5 → x+2 < 7 omitted, since x+2 < 7 holds regardless
    assertTrue(col.contains("x≥2 ∨ ¬2⊕x≥5"));
    assertTrue(col.contains("x≥3 ∨ ¬2⊕x≥5"));
    assertTrue(col.contains("x≥4 ∨ ¬2⊕x≥6"));
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
    // of the 48 clauses, we omit x ≥ i ∧ y ≥ j → x+y ≥ i+j when i+j = 5 (so 2)
    // and x ≤ i ∧ y ≤ j → x+y ≤ i+j when i+j in {10,11} (so 3 for 10, 2 for 11)
    // hence, in total we omit 7, so should have 41 constraints
    assertTrue(col.size() == 41);
    assertTrue(col.contains("¬x≥4 ∨ ¬y≥4 ∨ x⊕y≥8"));
    assertTrue(col.contains("x≥5 ∨ y≥5 ∨ ¬x⊕y≥9"));
    assertTrue(col.contains("¬x≥4 ∨ x⊕y≥7"));   // x ≥ 4 ∧ y ≥ ymin → x+y ≥ 4+yman = y
    assertTrue(col.contains("¬x≥5 ∨ ¬y≥7 ∨ x⊕y≥10"));
    assertTrue(col.contains("x≥2 ∨ y≥4 ∨ ¬x⊕y≥6"));
  }
}

