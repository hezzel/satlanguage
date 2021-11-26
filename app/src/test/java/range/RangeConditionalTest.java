import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.number.general.ClauseAdder;
import logic.number.range.*;
import java.util.ArrayList;

public class RangeConditionalTest {
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
  public void testUnboundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    RangeInteger cri = new RangeConditional(makeAtom("z", true), a, truth(), emptyAdder());
    assertTrue(cri.toString().equals("z?a"));
  }

  @Test
  public void testSemiBoundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    Atom z = makeAtom("z", true);
    RangeInteger cri = new RangeConditional(z, a, truth(), 0, 6, emptyAdder());
    assertTrue(cri.toString().equals("z?a"));
  }

  @Test
  public void testBoundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    Atom z = makeAtom("z", true);
    RangeInteger cri = new RangeConditional(z, a, truth(), 0, 4, emptyAdder());
    assertTrue(cri.toString().equals("cond(0, 4, z?a)"));
  }

  @Test
  public void testPositiveConstant() {
    RangeInteger n = new RangeConstant(4, truth());
    Atom x = makeAtom("x", true);
    RangeInteger cri = new RangeConditional(x, n, truth(), emptyAdder());
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testNegativeConstant() {
    RangeInteger n = new RangeConstant(-4, truth());
    Atom x = makeAtom("x", false);
    RangeInteger cri = new RangeConditional(x, n, truth(), emptyAdder());
    assertTrue(cri.queryGeqAtom(1).toString().equals("¬TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(-3).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(-4).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-5).toString().equals("TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testPositiveRange() {
    Atom x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", 2, 5, truth());
    RangeInteger cri = new RangeConditional(x, y, truth(), emptyAdder());
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(2).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(3).toString().equals("x?y≥3"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x?y≥4"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("x?y≥5"));
    assertTrue(cri.queryGeqAtom(6).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("x ∨ ¬x?y≥4"));         // x?y≥4 <-> x /\ y≥4
    assertTrue(col.contains("y≥4 ∨ ¬x?y≥4"));
    assertTrue(col.contains("¬x ∨ ¬y≥4 ∨ x?y≥4"));
  }

  @Test
  public void testNegativeRange() {
    Variable.reset();
    Atom x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", -3, -1, truth());
    RangeInteger cri = new RangeConditional(x, y, truth(), emptyAdder());
    assertTrue(cri.queryGeqAtom(1).toString().equals("¬TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("¬x"));
    assertTrue(cri.queryGeqAtom(-1).toString().equals("x?y≥-1"));
    assertTrue(cri.queryGeqAtom(-2).toString().equals("x?y≥-2"));
    assertTrue(cri.queryGeqAtom(-3).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-4).toString().equals("TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 6);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("¬x ∨ y≥-2 ∨ ¬x?y≥-2"));     // x?y≥-2 <-> ¬x \/ y≥-2
    assertTrue(col.contains("x ∨ x?y≥-2"));
    assertTrue(col.contains("¬y≥-2 ∨ x?y≥-2"));
  }

  @Test
  public void testNegativePositiveRange() {
    Variable.reset();
    Atom x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", -3, 4, truth());
    RangeInteger cri = new RangeConditional(x, y, truth(), emptyAdder());
    assertTrue(cri.queryGeqAtom(-3).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-2).toString().equals("x?y≥-2"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("x?y≥0"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x?y≥4"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 21);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("¬x ∨ y≥0 ∨ ¬x?y≥0"));  // x?y≥0 <-> ¬x \/ y≥0
    assertTrue(col.contains("x ∨ x?y≥0"));
    assertTrue(col.contains("¬y≥0 ∨ x?y≥0"));
    assertTrue(col.contains("x ∨ ¬x?y≥1"));         // x?y≥1 <-> x /\ y≥1
    assertTrue(col.contains("y≥1 ∨ ¬x?y≥1"));
    assertTrue(col.contains("¬x ∨ ¬y≥1 ∨ x?y≥1"));
  }

  @Test
  public void testRangeWithBounds() {
    RangeInteger a = new RangeVariable("a", -10, 10, truth());
    Atom z = makeAtom("z", true);
    RangeInteger cri = new RangeConditional(z, a, truth(), 2, 5, emptyAdder());
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(3).toString().equals("z?a≥3"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("z?a≥5"));
    assertTrue(cri.queryGeqAtom(6).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);
  }

  @Test
  public void testAvoidDuplicateWelldefinednessClauses() {
    RangeInteger a = new RangeVariable("a", -10, 10, truth());
    Atom z = makeAtom("z", true);
    RangeInteger cri = new RangeConditional(z, a, truth(), 2, 5, emptyAdder());
    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);   // this is the usual number
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);   // nothing should have been added
  }

  @Test
  public void testExtraClausesAreAdded() {
    ClauseAdder ca = new ClauseAdder() {
      public void add(ClauseCollection col) {
        col.addClause(new Clause(makeAtom("BING", false)));
      }
    };
    Atom x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", 2, 5, truth());
    RangeInteger cri = new RangeConditional(x, y, truth(), ca);
    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 10);
    assertTrue(col.contains("¬BING"));
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 10);
  }
}

