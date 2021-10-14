import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.formula.Formula;
import logic.formula.AtomicFormula;
import logic.formula.And;
import logic.formula.Or;
import java.util.ArrayList;

public class OrTest {
  private Or sampleOr() {
    AtomicFormula a1 = new AtomicFormula(new Atom(new Variable("x1"), true));
    AtomicFormula a2 = new AtomicFormula(new Atom(new Variable("x2"), true));
    AtomicFormula a3 = new AtomicFormula(new Atom(new Variable("x3"), false));
    AtomicFormula a4 = new AtomicFormula(new Atom(new Variable("x4"), true));
    AtomicFormula a5 = new AtomicFormula(new Atom(new Variable("x5"), false));
    ArrayList<Formula> parts = new ArrayList<Formula>();
    parts.add(a1);
    parts.add(new Or(a2, a3));
    parts.add(new And(a4, a2));
    parts.add(a5);
    return new Or(parts);
  }

  @Test
  public void testSampleString() {
    String str = sampleOr().toString();
    assertTrue(str.equals("x1 ∨ x2 ∨ ¬x3 ∨ (x4 ∧ x2) ∨ ¬x5"));
  }

  /*
  @Test
  public void testIffString() {
    Formula x = new AtomicFormula(new Atom(new Variable("x"), true));
    Formula y = new AtomicFormula(new Atom(new Variable("y"), true));
    Formula z = new AtomicFormula(new Atom(new Variable("z"), true));
    Formula form = new Or(x, new Iff(y, z));
    assertTrue(form.toString().equals("x ∨ (y ↔ z)"));
  }
  */

  @Test
  public void testNegation() {
    String str = sampleOr().negate().toString();
    assertTrue(str.equals("¬x1 ∧ ¬x2 ∧ x3 ∧ (¬x4 ∨ ¬x2) ∧ x5"));
  }

  @Test
  public void testAtom() {
    assertTrue(sampleOr().queryAtom() == null);
    ArrayList<Formula> parts = new ArrayList<Formula>();
    parts.add(new AtomicFormula(new Variable("x"), true));
    Formula a = new Or(parts);
    assertTrue(a.queryAtom() != null);
    parts = new ArrayList<Formula>();
    parts.add(new And(new AtomicFormula(new Atom(new Variable("x"), true)),
                      new AtomicFormula(new Atom(new Variable("y"), true))));
    a = new Or(parts);
    assertTrue(a.queryAtom() == null);
  }

  @Test
  public void testAddClauses() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    sampleOr().addClauses(coll);
    assertTrue(coll.size() == 4);
    assertTrue(coll.contains("x1 ∨ x2 ∨ ¬x3 ∨ ¬x5 ∨ ⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x2 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x4 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("¬x2 ∨ ¬x4 ∨ ⟦x4 ∧ x2⟧"));
  }

  @Test
  public void testAddClausesImpliedBy() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleOr();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesIfThisIsImpliedBy(myatom, coll);
    assertTrue(coll.size() == 4); 
    assertTrue(coll.contains("x1 ∨ x2 ∨ ¬x3 ∨ ¬x5 ∨ ¬myvar ∨ ⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x2 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x4 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("¬x2 ∨ ¬x4 ∨ ⟦x4 ∧ x2⟧"));
  }

  @Test
  public void testAddClausesImplies() {
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleOr();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesIfThisImplies(myatom, coll);
    assertTrue(coll.size() == 5);
    assertTrue(coll.contains("¬x1 ∨ myvar"));
    assertTrue(coll.contains("¬x2 ∨ myvar"));
    assertTrue(coll.contains("x3 ∨ myvar"));
    assertTrue(coll.contains("¬x2 ∨ ¬x4 ∨ myvar"));
    assertTrue(coll.contains("x5 ∨ myvar"));
  }

  @Test
  public void testAddClausesDef() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleOr();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesDef(myatom, coll);
    assertTrue(coll.size() == 9);
    assertTrue(coll.contains("¬x1 ∨ myvar"));
    assertTrue(coll.contains("¬x2 ∨ myvar"));
    assertTrue(coll.contains("x3 ∨ myvar"));
    assertTrue(coll.contains("myvar ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x5 ∨ myvar"));
    assertTrue(coll.contains("x1 ∨ x2 ∨ ¬x3 ∨ ¬x5 ∨ ¬myvar ∨ ⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x2 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("x4 ∨ ¬⟦x4 ∧ x2⟧"));
    assertTrue(coll.contains("¬x2 ∨ ¬x4 ∨ ⟦x4 ∧ x2⟧"));
  }

  @Test
  public void reuseExistingVariable() {
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleOr();
    Atom myatom = new Atom(new Variable("myvar"), true);
    new Variable("⟦x4 ∧ x2⟧");
    sample.addClausesDef(myatom, coll);
    assertTrue(coll.size() == 6);
    assertFalse(coll.contains("x4 ∨ ¬⟦x4 ∧ x2⟧"));
  }
}

