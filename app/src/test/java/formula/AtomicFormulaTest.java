import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.formula.AtomicFormula;

import java.util.TreeMap;

public class AtomicFormulaTest {
  private Atom make(String name, boolean value) {
    return new Atom(new Variable(name), value);
  }

  private Atom pos(String varname) {
    return make(varname, true);
  }

  private Atom neg(String varname) { 
    return make(varname, false);
  }
  
  @Test
  public void testAddClauses() {
    ClauseCollector coll = new ClauseCollector();
    AtomicFormula form = new AtomicFormula(pos("x"));
    form.addClauses(coll);
    assertTrue(coll.size() == 1);
    assertTrue(coll.get(0).equals(new Clause(pos("x"))));
  }

  @Test
  public void testNegate() {
    ClauseCollector coll = new ClauseCollector();
    AtomicFormula form = new AtomicFormula(pos("x"));
    form = form.negate();
    form.addClauses(coll);
    assertTrue(coll.size() == 1);
    assertTrue(coll.get(0).equals(new Clause(neg("x"))));
  }
  
  @Test
  public void testAddClausesIfThisIsImpliedBy() {
    ClauseCollector coll = new ClauseCollector();
    AtomicFormula form = new AtomicFormula(pos("x"));
    form.addClausesIfThisIsImpliedBy(pos("y"), coll);
    assertTrue(coll.size() == 1);
    assertTrue(coll.get(0).equals(new Clause(pos("x"), neg("y"))));
  }

  @Test
  public void testAddClausesIfThisImplies() {
    ClauseCollector coll = new ClauseCollector();
    AtomicFormula form = new AtomicFormula(neg("x"));
    form.addClausesIfThisImplies(neg("y"), coll);
    assertTrue(coll.size() == 1);
    assertTrue(coll.get(0).equals(new Clause(pos("x"), neg("y"))));
  }

  @Test
  public void testAddClausesDef() {
    ClauseCollector coll = new ClauseCollector();
    AtomicFormula form = new AtomicFormula(pos("x"));
    form.addClausesDef(pos("x"), coll);
    assertTrue(coll.size() == 2);
    assertTrue(coll.get(0).toString().equals("x ∨ ¬x"));
    assertTrue(coll.get(1).toString().equals("x ∨ ¬x"));
  }
}

