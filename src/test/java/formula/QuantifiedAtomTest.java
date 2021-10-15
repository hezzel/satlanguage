import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.*;
import logic.parameter.*;
import logic.formula.Formula;
import logic.formula.QuantifiedAtom;

import java.util.ArrayList;

public class QuantifiedAtomTest {
  private ParamBoolVar makeVar() {    // qat[a,b] for a ∈ {1..10}, b ∈ {a+1..10} with b != 7
    PConstraint constr =
      new NeqConstraint(new ParameterExpression("b"), new ConstantExpression(7));
    Parameter a = new Parameter("a", 1, 10); 
    Parameter b = new Parameter("b", (new ParameterExpression("a")).add(1),
                                     new ConstantExpression(10), constr); 
    ParameterList params = new ParameterList(a, b); 
    return new ParamBoolVar("qat", new Variable("FALSE"), params);
  }

  private QuantifiedAtom makeQAtom() {  // qat[i,j+1]
    ParamBoolVar x = makeVar();
    ArrayList<PExpression> exprs = new ArrayList<PExpression>();
    exprs.add(new ParameterExpression("i"));
    exprs.add(new SumExpression(new ParameterExpression("j"), new ConstantExpression(1)));
    return new QuantifiedAtom(x, true, exprs);
  }

  private QuantifiedAtom makeClosedQAtom() {
    ParamBoolVar x = makeVar();         // ¬qat[1+2,0]
    ArrayList<PExpression> exprs = new ArrayList<PExpression>();
    exprs.add(new SumExpression(new ConstantExpression(1), new ConstantExpression(2)));
    exprs.add(new ConstantExpression(0));
    return new QuantifiedAtom(x, false, exprs);
  }

  @Test(expected = java.lang.Error.class)
  public void testInconsistentCreation() {
    // two parameters expected, only one given
    ArrayList<PExpression> params = new ArrayList<PExpression>();
    params.add(new ConstantExpression(5));
    new QuantifiedAtom(makeVar(), true, params);
  }

  @Test
  public void testClosed() {
    assertFalse(makeQAtom().queryClosed());
    assertTrue(makeClosedQAtom().queryClosed());
  }

  @Test
  public void testToString() {
    assertTrue(makeQAtom().toString().equals("qat[i,j+1]"));
    assertTrue(makeClosedQAtom().toString().equals("¬qat[1+2,0]"));
  }

  @Test
  public void testCreateWithSubstitution() {
    ParamBoolVar x = makeVar();
    Substitution subst = new Substitution("a", new ParameterExpression("i"),
                                          "b", (new ParameterExpression("j")).add(1));
    Formula form = new QuantifiedAtom(x, false, subst);
    assertTrue(form.toString().equals("¬qat[i,j+1]"));
  }

  @Test
  public void testCreateWithPartialSubstitution() {
    ParamBoolVar x = makeVar();
    Substitution subst = new Substitution("a", new ParameterExpression("i"));
    Formula form = new QuantifiedAtom(x, true, subst);
    assertTrue(form.toString().equals("qat[i,b]"));
  }

  @Test
  public void testFullInstantiation() {
    ArrayList<PExpression> params = new ArrayList<PExpression>();
    params.add(new ParameterExpression("i"));
    params.add(new SumExpression(new ParameterExpression("i"), new ConstantExpression(1)));
    QuantifiedAtom a = new QuantifiedAtom(makeVar(), true, params);
    Assignment ass = new Assignment();
    ass.put("i", 7);
    Formula inst = a.instantiate(ass);
    assertTrue(inst.toString().equals("qat[7,8]"));
    assertTrue(inst.queryClosed());
    assertTrue(inst.queryAtom() != null);
    assertTrue(inst.queryAtom().toString().equals("qat[7,8]"));
  }

  @Test
  public void testPartialInstantiation() {
    QuantifiedAtom a = makeQAtom();
    Assignment ass = new Assignment("j", 7);
    Formula inst = a.instantiate(ass);
    assertTrue(inst.toString().equals("qat[i,8]"));
    assertTrue(inst.queryAtom() == null);
  }

  @Test
  public void testIllegalInstantiationMinimum() {
    QuantifiedAtom a = makeQAtom();
    Assignment ass = new Assignment("j", 7);
    ass.put("i", 9);
    Formula inst = a.instantiate(ass);
    assertTrue(inst.toString().equals("FALSE"));
  }

  @Test
  public void testIllegalInstantiationConstraint() {
    QuantifiedAtom a = makeQAtom();
    Assignment ass = new Assignment("j", 6);
    ass.put("i", 1);
    Formula inst = a.instantiate(ass);
    assertTrue(inst.toString().equals("FALSE"));
  }

  @Test
  public void testSubstitution() {
    QuantifiedAtom a = makeQAtom();
    Substitution subst = new Substitution("j", (new ParameterExpression("i")).add(-1));
    Formula inst = a.substitute(subst);
    assertTrue(inst.toString().equals("qat[i,i]"));
  }
}

