import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.number.binary.BinaryConstant;
import logic.number.binary.BinaryVariable;
import logic.number.binary.BinaryPlus;

public class BinaryPlusTest {
  private void setIntCol(ClauseCollector col, String name, int len, int value) {
    BinaryConstant c = new BinaryConstant(value, new Atom(new Variable("TRUE"), true));
    for (int i = 0; i < len; i++) {
      col.force(name + "⟨" + i + "⟩", c.queryBit(i).toString().equals("TRUE"));
    }
    col.force(name + "⟨-⟩", c.queryNegativeBit().toString().equals("TRUE"));
  }

  private void checkSolution(ClauseCollector col, BinaryPlus p, int value) {
    BinaryConstant c = new BinaryConstant(value, new Atom(new Variable("TRUE"), true));
    assertTrue(col._solution != null);
    for (int i = 0; i < p.length(); i++) {
      String expected = p.toString() + "⟨" + i + "⟩";
      if (!c.queryBit(i).toString().equals("TRUE")) expected = "¬" + expected;
      assertTrue("Expecting " + p.toString() + " to be " + value + ", but " + expected +
        " not in solution: " + col._solution, col._solution.contains(expected));
    }
    if (p.queryMinimum() < 0 && p.queryMaximum() >= 0) {
      String expected = p.toString() + "⟨-⟩";
      if (!c.queryNegativeBit().toString().equals("TRUE")) expected = "¬" + expected;
      assertTrue("Expecting " + p.toString() + " to be " + value + ", but " + expected +
      " not in solution: " + col._solution, col._solution.contains(expected));
    }
  }

  @Test
  public void testAddTwoVariablesOfEqualLength() {
    Atom t = new Atom(new Variable("TRUE"), true);
    BinaryVariable x = new BinaryVariable("x", 4, true, t);
    BinaryVariable y = new BinaryVariable("y", 4, true, t);
    BinaryPlus p = new BinaryPlus(x, y, t);
    assertTrue(p.queryMinimum() == -32);
    assertTrue(p.queryMaximum() == 30);
    assertTrue(p.length() == 5);
    assertTrue(p.queryBit(0).toString().equals("(x⊞y)⟨0⟩"));
    assertTrue(p.queryBit(4).toString().equals("(x⊞y)⟨4⟩"));
    assertTrue(p.queryBit(5).toString().equals("(x⊞y)⟨-⟩"));
    assertTrue(p.queryNegativeBit().toString().equals("(x⊞y)⟨-⟩"));
    ClauseCollector col = new ClauseCollector();
    p.addWelldefinednessClauses(col);
    // test 3 - 7 = -4
    col.force("TRUE", true);
    setIntCol(col, "x", 4, 3);
    setIntCol(col, "y", 4, -7);
    assertTrue(col.unitPropagate());
    checkSolution(col, p, -4);
  }

  @Test
  public void testAddTwoPositiveVariables() {
    Atom t = new Atom(new Variable("TRUE"), true);
    BinaryVariable x = new BinaryVariable("x", 4, false, t);
    BinaryVariable y = new BinaryVariable("y", 2, false, t);
    BinaryPlus p = new BinaryPlus(x, y, t);
    assertTrue(p.queryMinimum() == 0);
    assertTrue(p.queryMaximum() == 18);
    assertTrue(p.length() == 5);
    assertTrue(p.queryNegativeBit().toString().equals("¬TRUE"));
    assertTrue(p.queryBit(0).toString().equals("(x⊞y)⟨0⟩"));
    assertTrue(p.queryBit(4).toString().equals("(x⊞y)⟨4⟩"));
    assertTrue(p.queryBit(5).toString().equals("¬TRUE"));
    ClauseCollector col = new ClauseCollector();
    p.addWelldefinednessClauses(col);
    // test 13 + 2 = 19
    col.force("TRUE", true);
    setIntCol(col, "x", 4, 13);
    setIntCol(col, "y", 2, 2);
    assertTrue(col.unitPropagate());
    checkSolution(col, p, 15);
  }

  @Test
  public void testAddTwoVariablesWithRange() {
    Atom t = new Atom(new Variable("TRUE"), true);
    BinaryVariable x = new BinaryVariable("x", -7, 16, t);
    BinaryVariable y = new BinaryVariable("y", 7, 9, t);
    BinaryPlus p = new BinaryPlus(x, y, t);
    assertTrue(p.queryMinimum() == 0);
    assertTrue(p.queryMaximum() == 25);
    assertTrue(p.length() == 5);
    assertTrue(p.queryBit(5).toString().equals("¬TRUE"));
    ClauseCollector col = new ClauseCollector();
    p.addWelldefinednessClauses(col);
    // test -3 + 9 = 6
    col.force("TRUE", true);
    setIntCol(col, "x", x.length(), -3);
    setIntCol(col, "y", y.length(), 9);
    assertTrue(col.unitPropagate());
    checkSolution(col, p, 6);
  }

  @Test
  public void testAddVariableToConstantEndingNegative() {
    Atom t = new Atom(new Variable("TRUE"), true);
    BinaryVariable x = new BinaryVariable("x", 14, 16, t);
    BinaryConstant y = new BinaryConstant(-17, t);
    BinaryPlus p = new BinaryPlus(x, y, t);
    assertTrue(p.queryMinimum() == -3);
    assertTrue(p.queryMaximum() == -1);
    assertTrue(p.length() == 2);
    assertTrue(p.queryNegativeBit().toString().equals("TRUE"));
    assertTrue(p.queryBit(1).toString().equals("(x⊞-17)⟨1⟩"));
    ClauseCollector col = new ClauseCollector();
    p.addWelldefinednessClauses(col);
    // test 15 + 17 = -2
    col.force("TRUE", true);
    setIntCol(col, "x", x.length(), 15);
    assertTrue(col.unitPropagate());
    checkSolution(col, p, -2);
  }

  @Test
  public void testAddThreeVariablesOfDifferentLengths() {
    Atom t = new Atom(new Variable("TRUE"), true);
    BinaryVariable x = new BinaryVariable("x", 4, true, t);
    BinaryVariable y = new BinaryVariable("y", 2, false, t);
    BinaryVariable z = new BinaryVariable("z", 3, true, t);
    BinaryPlus p = new BinaryPlus(new BinaryPlus(x, y, t), z, t);
    assertTrue(p.queryMinimum() == -24);
    assertTrue(p.queryMaximum() == 25);
    assertTrue(p.length() == 5);
    assertTrue(p.queryBit(0).toString().equals("((x⊞y)⊞z)⟨0⟩"));
    assertTrue(p.queryBit(4).toString().equals("((x⊞y)⊞z)⟨4⟩"));
    assertTrue(p.queryBit(5).toString().equals("((x⊞y)⊞z)⟨-⟩"));
    assertTrue(p.queryNegativeBit().toString().equals("((x⊞y)⊞z)⟨-⟩"));
  }
}

