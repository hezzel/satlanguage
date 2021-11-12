import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.parameter.*;
import logic.number.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.formula.Formula;
import logic.formula.Geq;
import logic.VariableList;
import language.parser.InputReader;
import language.parser.ParserException;

public class GeqTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private QuantifiedInteger makeRangeVar(String name, int min, int max) {
    RangeVariable ri = new RangeVariable(name, min, max, truth());
    return new VariableInteger(ri);
  }

  private QuantifiedInteger makeConstant(int num) {
    return new QuantifiedConstant(new ConstantExpression(num), truth());
  }

  @Test
  public void testAddClausesDefSingleAtom() {
    Geq formula = new Geq(makeRangeVar("x", 1, 5), makeConstant(3), true);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 2);
    assertTrue(col.contains("x≥3 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥3 ∨ myvar"));
  }

  @Test
  public void testAddClausesGeqRelevantBoundRight() {
    Variable.reset();
    QuantifiedInteger plus = new QuantifiedPlus(makeRangeVar("x", 0, 5), makeRangeVar("y", 0, 5),
                                                ClosedInteger.RANGE, truth());
    QuantifiedInteger z = makeRangeVar("z", 1, 2); 
    Geq formula = new Geq(plus, z, true); 
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    col.addToMemory("rangevar z");
    formula.addClauses(col);
    // we only really need to know whether x⊕y is at most 0, equal to 1, or at least 2
    assertTrue(col.contains("¬x≥1 ∨ x⊕y≥1"));   // x ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬x≥2 ∨ x⊕y≥2"));   // x ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬y≥1 ∨ x⊕y≥1"));   // y ≥ 1 → x⊕y ≥ 1
    assertTrue(col.contains("¬y≥2 ∨ x⊕y≥2"));   // y ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥1 ∨ x⊕y≥2"));   // x ≥ 1 ∧ y ≥ 1 → x⊕y ≥ 2
    assertTrue(col.contains("x≥1 ∨ y≥1 ∨ ¬x⊕y≥1")); // x ≤ 0 ∧ y ≤ 0 → x⊕y ≤ 0
    assertTrue(col.contains("x≥1 ∨ y≥2 ∨ ¬x⊕y≥2")); // x ≤ 0 ∧ y ≤ 1 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥1 ∨ ¬x⊕y≥2")); // x ≤ 1 ∧ y ≤ 0 → x⊕y ≤ 1
    assertTrue(col.contains("x⊕y≥1"));          // x⊕y is at least zmin
    assertTrue(col.contains("¬z≥2 ∨ x⊕y≥2"));   // z≥2 → x⊕y≥2
    assertTrue(col.size() == 10);
  }

  @Test
  public void testAddClausesGeqRelevantBoundLeft() {
    Variable.reset();
    QuantifiedInteger plus = new QuantifiedPlus(makeRangeVar("x", 0, 5), makeRangeVar("y", 0, 5),
                                                ClosedInteger.RANGE, truth());
    QuantifiedInteger z = makeRangeVar("z", 1, 2); 
    Geq formula = new Geq(z, plus, true); 
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    col.addToMemory("rangevar z");
    formula.addClauses(col);
    // we only really need to know whether x⊕y is at most 1, equal to 2, or at least 3
    assertTrue(col.contains("¬x≥2 ∨ x⊕y≥2"));        // x ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥3 ∨ x⊕y≥3"));        // x ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬y≥2 ∨ x⊕y≥2"));        // y ≥ 2 → x⊕y ≥ 2
    assertTrue(col.contains("¬y≥3 ∨ x⊕y≥3"));        // y ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥1 ∨ x⊕y≥2")); // x ≥ 1 ∧ y ≥ 1 → x⊕y ≥ 2
    assertTrue(col.contains("¬x≥1 ∨ ¬y≥2 ∨ x⊕y≥3")); // x ≥ 1 ∧ y ≥ 2 → x⊕y ≥ 3
    assertTrue(col.contains("¬x≥2 ∨ ¬y≥1 ∨ x⊕y≥3")); // x ≥ 2 ∧ y ≥ 1 → x⊕y ≥ 3
    assertTrue(col.contains("x≥1 ∨ y≥2 ∨ ¬x⊕y≥2"));  // x ≤ 0 ∧ y ≤ 1 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥1 ∨ ¬x⊕y≥2"));  // x ≤ 1 ∧ y ≤ 0 → x⊕y ≤ 1
    assertTrue(col.contains("x≥2 ∨ y≥2 ∨ ¬x⊕y≥3"));  // x ≤ 1 ∧ y ≤ 1 → x⊕y ≤ 2
    assertTrue(col.contains("x≥3 ∨ y≥1 ∨ ¬x⊕y≥3"));  // x ≤ 2 ∧ y ≤ 0 → x⊕y ≤ 2
    assertTrue(col.contains("x≥1 ∨ y≥3 ∨ ¬x⊕y≥3"));  // x ≤ 0 ∧ y ≤ 2 → x⊕y ≤ 2
    assertTrue(col.contains("¬x⊕y≥3"));              // x⊕y is at most 2 = zmax
    assertTrue(col.contains("z≥2 ∨ ¬x⊕y≥2"));        // z≤1 → x⊕y≤1
    assertTrue(col.size() == 14);
  }

  @Test
  public void testAddClausesDefMultipleAtoms() {
    Variable.reset();
    Geq formula = new Geq(makeRangeVar("x", 1, 5), makeRangeVar("y", 3, 7), false);
    ClauseCollector col = new ClauseCollector();
    col.addToMemory("rangevar x");
    col.addToMemory("rangevar y");
    Atom atom = new Atom(new Variable("myvar"), true);
    formula.addClausesDef(atom, col);
    assertTrue(col.size() == 7);
    // atom → x < y  =  ¬atom ∨ x < y, where x < y = ¬x≥3 ∨ y≥4 AND ¬x≥4 ∨ y≥5 AND ¬x≥5 ∨ y≥6
    assertTrue(col.contains("¬x≥3 ∨ y≥4 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥4 ∨ y≥5 ∨ ¬myvar"));
    assertTrue(col.contains("¬x≥5 ∨ y≥6 ∨ ¬myvar"));
    // x < y → atom  =  x ≥ y ∨ atom, where  x ≥ y = x≥3 AND ¬y≥4 ∨ x≥4 AND ¬y≥5 ∨ x≥5 AND ¬y≥6
    assertTrue(col.contains("x≥3 ∨ myvar"));
    assertTrue(col.contains("x≥4 ∨ ¬y≥4 ∨ myvar"));
    assertTrue(col.contains("x≥5 ∨ ¬y≥5 ∨ myvar"));
    assertTrue(col.contains("¬y≥6 ∨ myvar"));
  }

  @Test
  public void testBasics() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.declare("x[i,j] :: Number ∈ {0..10} for i ∈ {1..3}, j ∈ {i+1..4}", vars);
    ParamRangeVar x = vars.queryParametrisedRangeVariable("x");
    Substitution subst = new Substitution("i", InputReader.readPExpressionFromString("a+j"),
                                          "j", InputReader.readPExpressionFromString("b"));
    QuantifiedVariable xajb = new QuantifiedVariable(x, subst);
    QuantifiedVariable xij = new QuantifiedVariable(x, new Substitution());
    Geq geq = new Geq(xajb, xij, true);    // x[a+j,c] ≥ x[i,j]

    assertTrue(geq.toString().equals("x[a+j,b] ≥ x[i,j]"));
    assertTrue(geq.negate().toString().equals("x[a+j,b] < x[i,j]"));
    assertTrue(geq.queryAtom() == null);
    assertFalse(geq.queryClosed());

    subst = new Substitution("a", InputReader.readPExpressionFromString("3"),
                             "b", InputReader.readPExpressionFromString("i"));
    Formula form = geq.substitute(subst);
    assertTrue(form.toString().equals("x[j+3,i] ≥ x[i,j]"));
    assertFalse(form.queryClosed());

    form = form.instantiate(new Assignment("i", 7, "j", 4));
    assertTrue(form.toString().equals("x[7,7] ≥ x[7,4]"));
    assertTrue(form.queryClosed());
    assertTrue(form.negate().toString().equals("x[7,7] < x[7,4]"));
  }
}

