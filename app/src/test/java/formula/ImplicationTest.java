import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.formula.Formula;
import logic.formula.AtomicFormula;
import logic.formula.And;
import logic.formula.Or;
import logic.formula.Implication;
import java.util.TreeMap;

public class ImplicationTest {
  private Atom make(String name, boolean value) {
    return new Atom(new Variable(name), value);
  }

  private Formula makef(String name, boolean value) {
    return new AtomicFormula(make(name, value));
  }

  @Test
  public void testToString() {
    Formula formula = new Implication(new Or(makef("x", true), makef("y", true), makef("z", true)),
                                      new And(makef("a", true), makef("b", false)));
    assertTrue(formula.toString().equals("x ∨ y ∨ z → a ∧ ¬b"));
    formula = new Implication(makef("x", false),
                              new Implication(makef("y", true), makef("z", false)));
    assertTrue(formula.toString().equals("¬x → (y → ¬z)"));
    formula = new Implication(new Implication(makef("y", true), makef("z", false)),
                              makef("x", false));
    assertTrue(formula.toString().equals("(y → ¬z) → ¬x"));
  }

  @Test
  public void testLeftAtomClauses() {
    // x -> y /\ -z
    Formula form = new Implication(makef("x", true), new And(makef("y", true), makef("z", false)));
    ClauseCollector coll = new ClauseCollector();
    form.addClauses(coll);
    assertTrue(coll.size() == 2);
    assertTrue(coll.contains("¬x ∨ y"));
    assertTrue(coll.contains("¬x ∨ ¬z"));
  }

  @Test
  public void testRightAtomClauses() {
    // y \/ -z -> x
    Formula x = makef("x", true);
    Formula formula = new Implication(new Or(makef("y", true), makef("z", false)), x);
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 2);
    assertTrue(coll.contains("x ∨ ¬y"));
    assertTrue(coll.contains("x ∨ z"));
  }

  @Test
  public void testLeftRightAtomClausesDisjunction() {
    // x /\ -y -> a \/ -b
    Formula formula = new Implication(new And(makef("x", true), makef("y", false)),
                                      new Or(makef("a", true), makef("b", false)));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 1);
    assertTrue(coll.contains("¬x ∨ y ∨ a ∨ ¬b"));
  }

  @Test
  public void testLeftRightAtomClausesNoDisjunction() {
    Variable.reset();
    // x \/ -y -> a /\ -b
    Formula formula = new Implication(new Or(makef("x", true), makef("y", false)),
                                      new And(makef("a", true), makef("b", false)));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 7);
    assertTrue(coll.contains("⟦¬x ∧ y⟧ ∨ ⟦a ∧ ¬b⟧"));
    assertTrue(coll.contains("¬x ∨ ¬⟦¬x ∧ y⟧"));
    assertTrue(coll.contains("y ∨ ¬⟦¬x ∧ y⟧"));
    assertTrue(coll.contains("x ∨ ¬y ∨ ⟦¬x ∧ y⟧"));
    assertTrue(coll.contains("¬a ∨ b ∨ ⟦a ∧ ¬b⟧"));
    assertTrue(coll.contains("a ∨ ¬⟦a ∧ ¬b⟧"));
    assertTrue(coll.contains("¬b ∨ ¬⟦a ∧ ¬b⟧"));
  }
}

