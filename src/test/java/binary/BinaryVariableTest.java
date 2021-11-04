import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.binary.BinaryVariable;

public class BinaryVariableTest {
  @Test
  public void testStandardNegativeBinaryVariable() {
    BinaryVariable x = new BinaryVariable("x", 4, true, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == -16);
    assertTrue(x.queryMaximum() == 15);
    assertTrue(x.length() == 4);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(1).toString().equals("x⟨1⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(3).toString().equals("x⟨3⟩"));
    assertTrue(x.queryBit(4).toString().equals("x⟨-⟩"));
    assertTrue(x.queryBit(5).toString().equals("x⟨-⟩"));
    assertTrue(x.queryNegativeBit().toString().equals("x⟨-⟩"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testNonNegativeBinaryVariable() {
    BinaryVariable x = new BinaryVariable("x", 4, false, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == 0);
    assertTrue(x.queryMaximum() == 15);
    assertTrue(x.length() == 4);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(1).toString().equals("x⟨1⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(3).toString().equals("x⟨3⟩"));
    assertTrue(x.queryBit(4).toString().equals("¬TRUE"));
    assertTrue(x.queryBit(5).toString().equals("¬TRUE"));
    assertTrue(x.queryNegativeBit().toString().equals("¬TRUE"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testMixedMinimumMaximumExactBoundaries() {
    BinaryVariable x = new BinaryVariable("x", -16, 15, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == -16);
    assertTrue(x.queryMaximum() == 15);
    assertTrue(x.length() == 4);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(1).toString().equals("x⟨1⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(3).toString().equals("x⟨3⟩"));
    assertTrue(x.queryBit(4).toString().equals("x⟨-⟩"));
    assertTrue(x.queryBit(5).toString().equals("x⟨-⟩"));
    assertTrue(x.queryNegativeBit().toString().equals("x⟨-⟩"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testMinimumMaximumMixedBoundariesPositiveStronger() {
    BinaryVariable x = new BinaryVariable("x", -3, 6, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == -3);
    assertTrue(x.queryMaximum() == 6);
    assertTrue(x.length() == 3);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(1).toString().equals("x⟨1⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(3).toString().equals("x⟨-⟩"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    // 6 = 110, so 6 ≥ x if x != 111
    assertTrue(col.contains("¬x⟨0⟩ ∨ ¬x⟨1⟩ ∨ ¬x⟨2⟩ ∨ x⟨-⟩"));
    // -3 = NEG 101, so x ≥ -3 if x is not negative or x ∈ {-3..-1}, so if x is not negative or
    // (x⟨2⟩ = 1 and not (x⟨1⟩ and x⟨0⟩ both false))
    System.out.println(col);
    assertTrue(col.contains("x⟨2⟩ ∨ ¬x⟨-⟩"));
    assertTrue(col.contains("x⟨0⟩ ∨ x⟨1⟩ ∨ ¬x⟨-⟩"));
    assertTrue(col.size() == 3);
  }

  @Test
  public void testMinimumMaximumMixedBoundariesNegativeStronger() {
    BinaryVariable x = new BinaryVariable("x", -8, 2, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == -8);
    assertTrue(x.queryMaximum() == 2);
    assertTrue(x.length() == 3);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(3).toString().equals("x⟨-⟩"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    // 2 = 10, so 2 ≥ x if x is negative or (x⟨2⟩ does not hold and (¬x⟨1⟩ or ¬x⟨0⟩))
    assertTrue(col.contains("¬x⟨2⟩ ∨ x⟨-⟩"));
    assertTrue(col.contains("¬x⟨0⟩ ∨ ¬x⟨1⟩ ∨ x⟨-⟩"));
    // -8 = 1...1000, which is the smallest integer of length 3 we can make; so no requirements are
    // added for x ≥ -8
    assertTrue(col.size() == 2);
  }

  @Test
  public void testMinimumMaximumPositiveBoundaries() {
    BinaryVariable x = new BinaryVariable("x", 1, 12, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == 1);
    assertTrue(x.queryMaximum() == 12);
    assertTrue(x.length() == 4);
    assertTrue(x.queryBit(0).toString().equals("x⟨0⟩"));
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(5).toString().equals("¬TRUE"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    // 12 = 1100, so 12 ≥ x if ¬x⟨3⟩ ∨ ¬x⟨2⟩ ∨ (¬x⟨1⟩ ∧ ¬x⟨0⟩)
    assertTrue(col.contains("¬x⟨0⟩ ∨ ¬x⟨2⟩ ∨ ¬x⟨3⟩"));
    assertTrue(col.contains("¬x⟨1⟩ ∨ ¬x⟨2⟩ ∨ ¬x⟨3⟩"));
    // x ≥ 1 if any of the bits are true
    assertTrue(col.contains("x⟨0⟩ ∨ x⟨1⟩ ∨ x⟨2⟩ ∨ x⟨3⟩"));
    assertTrue(col.size() == 3);
  }

  @Test
  public void testMinimumMaximumNegativeBoundaries() {
    BinaryVariable x = new BinaryVariable("x", -7, -3, new Atom(new Variable("TRUE"), true));
    assertTrue(x.queryMinimum() == -7);
    assertTrue(x.queryMaximum() == -3);
    assertTrue(x.length() == 3);
    assertTrue(x.queryBit(2).toString().equals("x⟨2⟩"));
    assertTrue(x.queryBit(4).toString().equals("TRUE"));
    ClauseCollector col = new ClauseCollector();
    x.addWelldefinednessClauses(col);
    // -3 = 1..1101, so -3 ≥ x if ¬x⟨2⟩ or ¬x⟨1⟩
    assertTrue(col.contains("¬x⟨1⟩ ∨ ¬x⟨2⟩"));
    // -7 = 1...1001, so x ≥ -7 if at least one x⟨i⟩ holds
    assertTrue(col.contains("x⟨0⟩ ∨ x⟨1⟩ ∨ x⟨2⟩"));
    assertTrue(col.size() == 2);
  }
}

