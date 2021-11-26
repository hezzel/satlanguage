import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.formula.*;

public class IffTest {
  private Atom make(String name, boolean value) {
    return new Atom(new Variable(name), value);
  }

  private Formula makef(String name, boolean value) {
    return new AtomicFormula(make(name, value));
  }

  @Test
  public void testToString() {
    Formula formula = new Iff(new Or(makef("x", true), makef("y", false), makef("z", true)),
                              new And(makef("a", true), makef("b", false)));
    assertTrue(formula.toString().equals("x ∨ ¬y ∨ z ↔ a ∧ ¬b"));
    formula = new Iff(makef("x", false), new Iff(makef("y", true), makef("z", false)));
    assertTrue(formula.toString().equals("¬x ↔ (y ↔ ¬z)"));
    formula = new Iff(new Iff(makef("y", true), makef("z", false)), makef("x", false));
    assertTrue(formula.toString().equals("(y ↔ ¬z) ↔ ¬x"));
  }

  @Test
  public void testLeftAtomClauses() {
    // x <-> y /\ -z
    Formula formula = new Iff(makef("x", true), new And(makef("y", true), makef("z", false)));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 3);
    assertTrue(coll.contains("¬x ∨ y"));
    assertTrue(coll.contains("¬x ∨ ¬z"));
    assertTrue(coll.contains("x ∨ ¬y ∨ z"));
  }

  @Test
  public void testRightAtomClauses() {
    // y /\ -z <-> x
    Formula x = makef("x", true);
    Formula formula = new Iff(new And(makef("y", true), makef("z", false)), x);
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 3);
    assertTrue(coll.contains("¬x ∨ y"));
    assertTrue(coll.contains("¬x ∨ ¬z"));
    assertTrue(coll.contains("x ∨ ¬y ∨ z"));
  }

  @Test
  public void testLeftRightAtomClauses() {
    Variable.reset();
    // x \/ -y \/ z <-> a /\ -b
    Formula formula = new Iff(new Or(makef("x", true), makef("y", false), makef("z", true)),
                              new And(makef("a", true), makef("b", false)));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 7);
    // new -> x \/ -y \/ z
    assertTrue(coll.contains("x ∨ ¬y ∨ z ∨ ¬⟦x ∨ ¬y ∨ z⟧"));
    // x \/ -y \/ z -> new
    assertTrue(coll.contains("¬x ∨ ⟦x ∨ ¬y ∨ z⟧"));
    assertTrue(coll.contains("y ∨ ⟦x ∨ ¬y ∨ z⟧"));
    assertTrue(coll.contains("¬z ∨ ⟦x ∨ ¬y ∨ z⟧"));
    // new -> a /\ -b
    assertTrue(coll.contains("a ∨ ¬⟦x ∨ ¬y ∨ z⟧"));
    assertTrue(coll.contains("¬b ∨ ¬⟦x ∨ ¬y ∨ z⟧"));
    // a /\ -b -> new
    assertTrue(coll.contains("¬a ∨ b ∨ ⟦x ∨ ¬y ∨ z⟧"));
  }

  @Test
  public void testClausesImpliedBy() {
    Variable.reset();
    // x <-> y \/ z
    Formula formula = new Iff(makef("x", true), new Or(makef("y", true), makef("z", true)));
    Atom nota = make("a", false);
    ClauseCollector coll = new ClauseCollector();
    formula.addClausesIfThisIsImpliedBy(nota, coll);
    assertTrue(coll.size() == 5);
    // nota /\ x -> [y \/ z]
    assertTrue(coll.contains("¬x ∨ a ∨ ⟦y ∨ z⟧"));
    // nota /\ -x -> -[y \/ z]
    assertTrue(coll.contains("x ∨ a ∨ ¬⟦y ∨ z⟧"));
    // [y \/ z] -> y \/ z
    assertTrue(coll.contains("y ∨ z ∨ ¬⟦y ∨ z⟧"));
    // y \/ z -> [y \/ z]
    assertTrue(coll.contains("¬y ∨ ⟦y ∨ z⟧"));
    assertTrue(coll.contains("¬z ∨ ⟦y ∨ z⟧"));
  }

  @Test
  public void testClausesImplying() {
    Variable.reset();
    // (x /\ y) <-> z
    Formula formula = new Iff(new And(makef("x", true), makef("y", true)), makef("z", true));
    Atom a = make("a", true);
    ClauseCollector coll = new ClauseCollector();
    coll.addToMemory("⟦x ∧ y⟧");
    formula.addClausesIfThisImplies(a, coll);
    assertTrue(coll.size() == 2);
    assertTrue(coll.contains("¬z ∨ a ∨ ¬⟦x ∧ y⟧"));  // [x/\y] /\ z -> a
    assertTrue(coll.contains("z ∨ a ∨ ⟦x ∧ y⟧"));    // -[x/\y] /\ -z -> a
  }

  @Test
  public void testClausesIff() {
    Variable.reset();
    // (x /\ y) <-> z
    Formula formula = new Iff(new And(makef("x", true), makef("y", true)), makef("z", true));
    Atom a = make("a", true);
    ClauseCollector coll = new ClauseCollector();
    coll.addToMemory("⟦x ∧ y⟧");
    formula.addClausesDef(a, coll);
    assertTrue(coll.size() == 4);
    assertTrue(coll.contains("z ∨ ¬a ∨ ¬⟦x ∧ y⟧"));  // a /\ [x/\y] -> z
    assertTrue(coll.contains("¬z ∨ ¬a ∨ ⟦x ∧ y⟧"));  // a /\ -[x/\y] -> -z
    assertTrue(coll.contains("¬z ∨ a ∨ ¬⟦x ∧ y⟧"));  // -a /\ [x/\y] -> -z
    assertTrue(coll.contains("z ∨ a ∨ ⟦x ∧ y⟧"));    // -a /\ -[x/\y] -> z
  }
}

