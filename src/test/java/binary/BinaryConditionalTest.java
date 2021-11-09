import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.number.general.ClauseAdder;
import logic.number.binary.BinaryConstant;
import logic.number.binary.BinaryVariable;
import logic.number.binary.BinaryConditional;
import java.util.ArrayList;

public class BinaryConditionalTest {
  private Atom makeAtom(String varname, boolean value) {
    return new Atom(new Variable(varname), value);
  }

  private Atom truth() {
    return makeAtom("TRUE", true);
  }

  private ClauseAdder emptyAdder() {
    return new ClauseAdder() {
      public void add(ClauseCollection col) {
      }
    };
  }

  @Test
  public void testPositiveConstant() {
    Atom na = makeAtom("a", false);
    BinaryConstant num = new BinaryConstant(5, truth());
    BinaryConditional bcc = new BinaryConditional(na, num, truth(), emptyAdder());
    assertTrue(bcc.toString().equals("¬a?5"));
    assertTrue(bcc.queryMinimum() == 0);
    assertTrue(bcc.queryMaximum() == 5);
    assertTrue(bcc.length() == 3);
    Atom falsehood = truth().negate();
    // 5: 101
    assertTrue(bcc.queryNegativeBit().equals(falsehood));
    assertTrue(bcc.queryBit(0).equals(na));
    assertTrue(bcc.queryBit(1).equals(falsehood));
    assertTrue(bcc.queryBit(2).equals(na));
    ClauseCollector col = new ClauseCollector();
    bcc.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testNegativeConstant() {
    Atom a = makeAtom("a", true);
    BinaryConstant num = new BinaryConstant(-7, truth());
    BinaryConditional bcc = new BinaryConditional(a, num, truth(), emptyAdder());
    assertTrue(bcc.toString().equals("a?-7"));
    assertTrue(bcc.queryMinimum() == -7);
    assertTrue(bcc.queryMaximum() == 0);
    assertTrue(bcc.length() == 3);
    Atom falsehood = truth().negate();
    // -7: 001
    assertTrue(bcc.queryNegativeBit().equals(a));
    assertTrue(bcc.queryBit(0).equals(a));
    assertTrue(bcc.queryBit(1).equals(falsehood));
    assertTrue(bcc.queryBit(2).equals(falsehood));
    ClauseCollector col = new ClauseCollector();
    bcc.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testShortConstant() {
    Atom na = makeAtom("a", false);
    Atom b = makeAtom("b", true);
    ClauseAdder ca = new ClauseAdder() {
      public void add(ClauseCollection col) {
        col.addClause(new Clause(b, na));
      }
    };
    BinaryConstant num = new BinaryConstant(-1, truth());
    BinaryConditional bcc = new BinaryConditional(b, num, truth(), ca);
    assertTrue(bcc.length() == 0);
    assertTrue(bcc.queryNegativeBit().equals(b));
    assertTrue(bcc.queryBit(0).equals(b));
    assertTrue(bcc.queryBit(1).equals(b));
    ClauseCollector col = new ClauseCollector();
    bcc.addWelldefinednessClauses(col);
    assertTrue(col.size() == 1);
    assertTrue(col.contains("¬a ∨ b"));
  }

  @Test
  public void testVariable() {
    Atom a = makeAtom("a", true);
    ClauseAdder ca = new ClauseAdder() {
      public void add(ClauseCollection col) {
        col.addClause(new Clause(makeAtom("x", true), a));
      }
    };
    BinaryVariable v = new BinaryVariable("v", -5, 9, truth());
    BinaryConditional c = new BinaryConditional(a, v, truth(), ca);
    assertTrue(c.length() == 4);
    assertTrue(c.queryNegativeBit().toString().equals("a?v⟨-⟩"));
    assertTrue(c.queryBit(0).toString().equals("a?v⟨0⟩"));
    assertTrue(c.queryBit(1).toString().equals("a?v⟨1⟩"));
    assertTrue(c.queryBit(2).toString().equals("a?v⟨2⟩"));
    assertTrue(c.queryBit(3).toString().equals("a?v⟨3⟩"));
    assertTrue(c.queryMinimum() == -5);
    assertTrue(c.queryMaximum() == 9);
    ClauseCollector col = new ClauseCollector();
    c.addWelldefinednessClauses(col);
    assertTrue(col.size() == 16); // 3 clauses per bit, and one for the clause adder
    c.addWelldefinednessClauses(col);
    assertTrue(col.size() == 16); // re-adding won't do anything
    // definition of a?v⟨0⟩
    assertTrue(col.contains("¬a ∨ ¬v⟨0⟩ ∨ a?v⟨0⟩"));
    assertTrue(col.contains("a ∨ ¬a?v⟨0⟩"));
    assertTrue(col.contains("v⟨0⟩ ∨ ¬a?v⟨0⟩"));
    // also check that we have clauses for a?v⟨3⟩ and a?v⟨-⟩
    assertTrue(col.contains("v⟨3⟩ ∨ ¬a?v⟨3⟩"));
    assertTrue(col.contains("¬a ∨ ¬v⟨-⟩ ∨ a?v⟨-⟩"));
  }
}

