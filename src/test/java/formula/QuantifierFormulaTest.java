import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.parameter.*;
import logic.formula.*;

import java.util.ArrayList;

public class QuantifierFormulaTest {
  private ParamBoolVar makeVar() {    // ft[a,b] for a ∈ {0..4}, b ∈ {0..5} with b < a
    PConstraint constr =
      new SmallerConstraint(new ParameterExpression("b"), new ParameterExpression("a"));
    Parameter a = new Parameter("a", 0, 4); 
    Parameter b = new Parameter("b", 0, 5, constr); 
    ParameterList params = new ParameterList(a, b); 
    return new ParamBoolVar("ft", new Variable("FALSE"), params);
  }

  private QuantifiedAtom makeQAtom() {    // ft[i+1,j]
    ParamBoolVar x = makeVar();
    ArrayList<PExpression> exprs = new ArrayList<PExpression>();
    exprs.add(new SumExpression(new ParameterExpression("i"), new ConstantExpression(1)));
    exprs.add(new ParameterExpression("j"));
    return new QuantifiedAtom(x, true, exprs);
  }

  private Forall makeForall() {   // ∀ i ∈ {0.4} with i != 2.∃ j ∈ {1..5}. x → ft[i+1,j]
    Variable x = new Variable("x");
    Implication impl = new Implication(new AtomicFormula(x, true), makeQAtom());
    PConstraint not2 = new NeqConstraint(new ConstantExpression(2),
                                         new ParameterExpression("i"));
    Parameter i = new Parameter("i", 0, 4, not2);
    Parameter j = new Parameter("j", 1, 5);
    return new Forall(i, new Exists(j, impl));
  }

  @Test
  public void testToString() {
    Formula forall = makeForall();
    assertTrue(forall.toString().equals(
      "∀ i ∈ {0..4} with 2 ≠ i. ∃ j ∈ {1..5}. x → ft[i+1,j]"));
  }

  @Test
  public void testUselessInstantiate() {
    Assignment ass = new Assignment();
    ass.put("i", 2);    // substituting quantified variables has no effect
    ass.put("j", 2);
    Formula forall = makeForall();
    Formula form = forall.instantiate(ass);
    assertTrue(form.toString().equals(forall.toString()));
  }

  @Test
  public void testUsefulInstantiate() {
    Variable x = new Variable("x");
    Implication impl = new Implication(new AtomicFormula(x, true), makeQAtom());
    Parameter j = new Parameter("j", 1, 5);
    Formula exists = new Exists(j, impl);
    Assignment ass = new Assignment("i", 1, "j", 3);
    Formula form = exists.instantiate(ass);
    assertTrue(form.toString().equals("∃ j ∈ {1..5}. x → ft[2,j]"));
  }

  @Test
  public void testTranslate() {
    Formula form = makeForall().translate();
    assertTrue(form.toString().equals("(∃ j ∈ {1..5}. x → ft[1,j]) ∧ (∃ j ∈ {1..5}. x → ft[2,j]) "
      + "∧ (∃ j ∈ {1..5}. x → ft[4,j]) ∧ (∃ j ∈ {1..5}. x → ft[5,j])"));
  }

  @Test
  public void testCreateClauses() {
    QuantifiedAtom x = makeQAtom();
    Parameter pj = new Parameter("j", 1, 2);
    Parameter pi = new Parameter("i", new ParameterExpression("j"), new ConstantExpression(2),
                                 new TrueConstraint());
    Formula phi = new Forall(pj, new Forall(pi, x));
    ClauseCollector coll = new ClauseCollector();
    phi.addClauses(coll);
    assertTrue(coll.size() == 3);
    assertTrue(coll.contains("ft[2,1]"));
    assertTrue(coll.contains("ft[3,1]"));
    assertTrue(coll.contains("ft[3,2]"));
  }

  @Test
  public void testParametersConsideredForClosure() {
    Parameter param = new Parameter("i", 0, 1,
      new SmallerConstraint(new ParameterExpression("i"), new ParameterExpression("j")));
    Formula form = new Forall(param, new AtomicFormula(
      new Variable("bing"), true));
    assertFalse(form.queryClosed());
    Assignment ass = new Assignment("j", 3);
    form = form.instantiate(ass);
    assertTrue(form.queryClosed());
    assertTrue(form.toString().equals("∀ i ∈ {0..1} with i < 3. bing"));
  }
}

