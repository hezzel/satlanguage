import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.parameter.Assignment;
import logic.formula.Formula;
import logic.formula.AtomicFormula;
import logic.formula.And;
import logic.formula.Or;
import logic.VariableList;
import language.parser.InputReader;
import java.util.ArrayList;

public class AndTest {
  private And sampleAnd() {
    AtomicFormula a1 = new AtomicFormula(new Atom(new Variable("x1"), true));
    AtomicFormula a2 = new AtomicFormula(new Atom(new Variable("x2"), true));
    AtomicFormula a3 = new AtomicFormula(new Atom(new Variable("x3"), false));
    AtomicFormula a4 = new AtomicFormula(new Atom(new Variable("x4"), true));
    AtomicFormula a5 = new AtomicFormula(new Atom(new Variable("x5"), false));
    ArrayList<Formula> parts = new ArrayList<Formula>();
    parts.add(a1);
    parts.add(new And(a2, a3));
    parts.add(new Or(a4, a2));
    parts.add(a5);
    return new And(parts);
  }

  @Test
  public void testSampleString() {
    String str = sampleAnd().toString();
    assertTrue(str.equals("x1 ∧ x2 ∧ ¬x3 ∧ (x4 ∨ x2) ∧ ¬x5"));
  }

  @Test
  public void testNegation() {
    String str = sampleAnd().negate().toString();
    assertTrue(str.equals("¬x1 ∨ ¬x2 ∨ x3 ∨ (¬x4 ∧ ¬x2) ∨ x5"));
  }

  @Test
  public void testAtom() {
    assertTrue(sampleAnd().queryAtom() == null);
    ArrayList<Formula> parts = new ArrayList<Formula>();
    parts.add(new AtomicFormula(new Variable("x"), true));
    Formula a = new And(parts);
    assertTrue(a.queryAtom() != null);
    parts = new ArrayList<Formula>();
    parts.add(new Or(new AtomicFormula(new Atom(new Variable("x"), true)),
                     new AtomicFormula(new Atom(new Variable("y"), true))));
    a = new And(parts);
    assertTrue(a.queryAtom() == null);
  }

  @Test
  public void testAddClauses() {
    ClauseCollector coll = new ClauseCollector();
    sampleAnd().addClauses(coll);
    assertTrue(coll.size() == 5);
    assertTrue(coll.contains("x1"));
    assertTrue(coll.contains("x2"));
    assertTrue(coll.contains("¬x3"));
    assertTrue(coll.contains("x2 ∨ x4"));
    assertTrue(coll.contains("¬x5"));
  }

  @Test
  public void testAddClausesImpliedBy() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleAnd();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesIfThisIsImpliedBy(myatom, coll);
    assertTrue(coll.size() == 5);
    assertTrue(coll.contains("x1 ∨ ¬myvar"));
    assertTrue(coll.contains("x2 ∨ ¬myvar"));
    assertTrue(coll.contains("¬x3 ∨ ¬myvar"));
    assertTrue(coll.contains("x2 ∨ x4 ∨ ¬myvar"));
    assertTrue(coll.contains("¬x5 ∨ ¬myvar"));
  }

  @Test
  public void testAddClausesImplies() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleAnd();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesIfThisImplies(myatom, coll);
    assertTrue(coll.size() == 4); 
    assertTrue(coll.contains("¬x1 ∨ ¬x2 ∨ x3 ∨ x5 ∨ myvar ∨ ¬⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("¬x2 ∨ ⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("¬x4 ∨ ⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("x2 ∨ x4 ∨ ¬⟦x4 ∨ x2⟧"));
  }

  @Test
  public void testVariableReuse() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleAnd();
    Atom myatom = new Atom(new Variable("myvar"), true);
    coll.addToMemory("⟦x4 ∨ x2⟧");
    sample.addClausesIfThisImplies(myatom, coll);
    assertTrue(coll.size() == 1); 
    assertTrue(coll.contains("¬x1 ∨ ¬x2 ∨ x3 ∨ x5 ∨ myvar ∨ ¬⟦x4 ∨ x2⟧"));
  }

  @Test
  public void testAndClausesDef() {
    Variable.reset();
    ClauseCollector coll = new ClauseCollector();
    Formula sample = sampleAnd();
    Atom myatom = new Atom(new Variable("myvar"), true);
    sample.addClausesDef(myatom, coll);
    assertTrue(coll.size() == 9);
    assertTrue(coll.contains("x1 ∨ ¬myvar"));
    assertTrue(coll.contains("x2 ∨ ¬myvar"));
    assertTrue(coll.contains("¬x3 ∨ ¬myvar"));
    assertTrue(coll.contains("¬myvar ∨ ⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("¬x5 ∨ ¬myvar"));
    assertTrue(coll.contains("¬x1 ∨ ¬x2 ∨ x3 ∨ x5 ∨ myvar ∨ ¬⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("¬x2 ∨ ⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("¬x4 ∨ ⟦x4 ∨ x2⟧"));
    assertTrue(coll.contains("x2 ∨ x4 ∨ ¬⟦x4 ∨ x2⟧"));
  }

  @Test
  public void testInstantiate() throws language.parser.ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i] :: Bool for i ∈ {1..10}", vars);
    Formula x1 = InputReader.readFormulaFromString("x[a]", vars);
    Formula x2 = InputReader.readFormulaFromString("x[b]", vars);
    Formula conjunction = new And(x1, x2);
    assertFalse(conjunction.queryClosed());
    assertTrue(conjunction.toString().equals("x[a] ∧ x[b]"));
    Formula inst1 = conjunction.instantiate(new Assignment("a", 1));
    Formula inst2 = conjunction.instantiate(new Assignment("a", 1, "b", 2));
    assertFalse(inst1.queryClosed());
    assertTrue(inst1.toString().equals("x[1] ∧ x[b]"));
    assertTrue(inst2.queryClosed());
    assertTrue(inst2.toString().equals("x[1] ∧ x[2]"));
  }
}

