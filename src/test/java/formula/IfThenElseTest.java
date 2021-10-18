import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.formula.*;
import java.util.TreeMap;

public class IfThenElseTest {
  private Atom make(String name, boolean value) {
    return new Atom(new Variable(name), value);
  }

  private Formula makef(String name, boolean value) {
    return new AtomicFormula(make(name, value));
  }

  @Test
  public void testToString() {
    Formula formula = new IfThenElse(new Iff(makef("x", false), makef("y", true)),
                                     new Or(makef("x", true), makef("y", false), makef("z", true)),
                                     new And(makef("a", true), makef("b", false)));
    //assertTrue(formula.toString().equals("(¬x ↔ y) ? x ∨ ¬y ∨ z : a ∧ ¬b"));
    assertTrue(formula.toString().equals("ite(¬x ↔ y, x ∨ ¬y ∨ z, a ∧ ¬b)"));
    formula = new IfThenElse(makef("x", false),
                             new IfThenElse(makef("y", true), makef("z", false), makef("a", true)),
                             makef("b", true));
    //assertTrue(formula.toString().equals("¬x ? (y ? ¬z : a) : b"));
    assertTrue(formula.toString().equals("ite(¬x, ite(y, ¬z, a), b)"));
  }

  @Test
  public void testConditionAtomClauses() {
    Variable.reset();
    // ite(x, y /\ -z, y \/ -z)
    Formula formula = new IfThenElse(makef("x", true),
                                     new And(makef("y", true), makef("z", false)),
                                     new Or(makef("y", true), makef("z", false)));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 3);
    assertTrue(coll.contains("¬x ∨ y"));
    assertTrue(coll.contains("¬x ∨ ¬z"));
    assertTrue(coll.contains("x ∨ y ∨ ¬z"));
  }

  @Test
  public void testConditionComplexClauses() {
    Variable.reset();
    // ite(x /\ y, z, -z)
    Formula x = makef("x", true);
    Formula formula = new IfThenElse(new And(makef("x", true), makef("y", true)),
                                     makef("z", true), makef("z", false));
    ClauseCollector coll = new ClauseCollector();
    formula.addClauses(coll);
    assertTrue(coll.size() == 5);
    assertTrue(coll.contains("x ∨ ¬⟦x ∧ y⟧"));
    assertTrue(coll.contains("y ∨ ¬⟦x ∧ y⟧"));
    assertTrue(coll.contains("¬x ∨ ¬y ∨ ⟦x ∧ y⟧"));
    assertTrue(coll.contains("z ∨ ¬⟦x ∧ y⟧"));
    assertTrue(coll.contains("¬z ∨ ⟦x ∧ y⟧"));
  }

  @Test
  public void testClausesImpliedBy() {
    Variable.reset();
    new Variable("x");
    // -a -> ite(y, x, z)
    Formula formula = new IfThenElse(makef("y", true), makef("x", true), makef("z", true));
    Atom nota = make("a", false);
    ClauseCollector coll = new ClauseCollector();
    formula.addClausesIfThisIsImpliedBy(nota, coll);
    assertTrue(coll.size() == 2);
    // nota -> (y -> x)
    assertTrue(coll.contains("x ∨ ¬y ∨ a"));
    // nota /\ (-y -> z)
    assertTrue(coll.contains("y ∨ z ∨ a"));
  }

  @Test
  public void testClausesImplying() {
    Variable.reset();
    // ite(x /\ y, z, -z) -> a
    Formula formula = new IfThenElse(new And(makef("x", true), makef("y", true)),
                                     makef("z", true), makef("z", false));
    Atom a = make("a", true);
    ClauseCollector coll = new ClauseCollector();
    formula.addClausesIfThisImplies(a, coll);
    assertTrue(coll.size() == 5);
    assertTrue(coll.contains("x ∨ ¬⟦x ∧ y⟧"));       // [x/\y] -> x
    assertTrue(coll.contains("y ∨ ¬⟦x ∧ y⟧"));       // [x/\y] -> y
    assertTrue(coll.contains("¬x ∨ ¬y ∨ ⟦x ∧ y⟧"));  // x /\ y -> [x/\y]
    assertTrue(coll.contains("¬z ∨ a ∨ ¬⟦x ∧ y⟧"));  // [x/\y] /\ z -> a
    assertTrue(coll.contains("z ∨ a ∨ ⟦x ∧ y⟧"));    // -[x/\y] /\ -z -> a
  }

  @Test
  public void testClausesIff() {
    Variable.reset();
    // ite(x /\ y, z, -z) <-> a
    Formula formula = new IfThenElse(new And(makef("x", true), makef("y", true)),
                                     makef("z", true), makef("z", false));
    Atom a = make("a", true);
    ClauseCollector coll = new ClauseCollector();
    formula.addClausesDef(a, coll);
    assertTrue(coll.size() == 7);
    assertTrue(coll.contains("z ∨ ¬a ∨ ¬⟦x ∧ y⟧"));  // a /\ new -> z
    assertTrue(coll.contains("¬z ∨ ¬a ∨ ⟦x ∧ y⟧"));  // a /\ -new -> -z
    assertTrue(coll.contains("¬z ∨ a ∨ ¬⟦x ∧ y⟧"));  // -a /\ new -> -z
    assertTrue(coll.contains("z ∨ a ∨ ⟦x ∧ y⟧"));    // -a /\ -new -> z
    assertTrue(coll.contains("x ∨ ¬⟦x ∧ y⟧"));       // new -> x
    assertTrue(coll.contains("y ∨ ¬⟦x ∧ y⟧"));       // new -> y
    assertTrue(coll.contains("¬x ∨ ¬y ∨ ⟦x ∧ y⟧"));  // x /\ y -> new
  }
}

