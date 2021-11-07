import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Solution;
import logic.parameter.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import logic.VariableList;
import language.execution.UndeclaredVariableError;
import language.execution.ProgramState;
import language.execution.VariableExpression;
import language.execution.ParamVarExpression;
import language.execution.ParamBoolVarConstraint;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class ExtendedPExpressionTest {
  private VariableList init() {
    VariableList ret = new VariableList();
    ret.registerRangeVariable(new Parameter("x", 1, 10));
    ret.registerParametrisedRangeVariable(new Parameter("y", 1, 5),
      new ParameterList(new Parameter("i", 0, 1), new Parameter("j", 1, 2)));
    ret.registerBinaryVariable("u", 8, true);
    ret.registerParametrisedBinaryVariable("w",
      new ParameterList(new Parameter("i", 0, 4), new Parameter("j", 1, 2)),
      new ConstantExpression(0), new ConstantExpression(7));
    ret.registerParametrisedBooleanVariable("zz",
      new ParameterList(new Parameter("a", 1, 5), new Parameter("b", 1, 3)));
    return ret;
  }

  private RangeVariable x(VariableList vars) {
    return vars.queryRangeVariable("x");
  }

  private ParamRangeVar y(VariableList vars) {
    return vars.queryParametrisedRangeVariable("y");
  }

  private ParamBoolVar z(VariableList vars) {
    return vars.queryParametrisedBooleanVariable("zz");
  }

  private BinaryVariable u(VariableList vars) {
    return vars.queryBinaryVariable("u");
  }

  private ParamBinaryVar w(VariableList vars) {
    return vars.queryParametrisedBinaryVariable("w");
  }

  @Test
  public void testRangeVariableExpressionBasics() {
    VariableList vars = init();
    PExpression e = new VariableExpression(x(vars));
    assertTrue(e.toString().equals("x"));
    e = e.add(5);
    assertTrue(e.toString().equals("x+5"));
    Substitution subst = new Substitution("x", new ConstantExpression(2));
    assertTrue(e.substitute(subst).equals(e));
    assertTrue(e.queryParameters().size() == 0);
    assertFalse(e.queryLeft().queryConstant());
  }

  @Test
  public void testBinaryVariableExpressionBasics() {
    VariableList vars = init();
    PExpression e = new VariableExpression(u(vars));
    assertTrue(e.toString().equals("u"));
    e = e.add(5);
    assertTrue(e.toString().equals("u+5"));
    Substitution subst = new Substitution("u", new ConstantExpression(2));
    assertTrue(e.substitute(subst).equals(e));
    assertTrue(e.queryParameters().size() == 0);
    assertFalse(e.queryLeft().queryConstant());
  }

  @Test
  public void testVariableExpressionInstantiate() {
    VariableList vars = init();
    RangeVariable v = x(vars);
    PExpression e = new SumExpression(new ConstantExpression(3),
                                      new VariableExpression(v));
    // evaluate expression with x = 7
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    for (int i = 1; i <= 7; i++) truevars.add(v.queryGeqAtom(i).queryIndex());
    Solution sol = new Solution(truevars);
    ProgramState state = new ProgramState(sol);
    assertTrue(e.evaluate(state) == 10);
  }

  private PExpression createExpr(VariableList vars) {
    // x + w[i,y[u,j+1]]
    PExpression jplus = (new ParameterExpression("j")).add(1);
    PExpression u = new VariableExpression(u(vars));
    ArrayList<PExpression> args = new ArrayList<PExpression>();
    args.add(u);
    args.add(jplus);
    PExpression yexpr1 = new ParamVarExpression(y(vars), args);
    PExpression i = new ParameterExpression("i");
    args = new ArrayList<PExpression>();
    args.add(i);
    args.add(yexpr1);
    PExpression yexpr2 = new ParamVarExpression(w(vars), args);
    PExpression x = new VariableExpression(x(vars));
    return new SumExpression(x, yexpr2);
  }

  @Test
  public void testParamRangeVarExpressionBasics() {
    VariableList vars = init();
    PExpression e = createExpr(vars);
    assertTrue(e.toString().equals("x+w[i,y[u,j+1]]"));
    assertTrue(e.add(9).toString().equals("x+w[i,y[u,j+1]]+9"));
    assertTrue(e.queryParameters().size() == 2);
    assertTrue(e.queryParameters().contains("i"));
    assertTrue(e.queryParameters().contains("j"));
    assertFalse(e.queryParameters().contains("x"));
    assertFalse(e.queryParameters().contains("u"));
    assertFalse(e.queryParameters().contains("w"));
    Substitution subst = new Substitution(
      "y", new ConstantExpression(7),
      "i", new VariableExpression(x(vars)),
      "j", new ConstantExpression(0));
    assertTrue(e.substitute(subst).toString().equals("x+w[x,y[u,1]]"));
  }

  @Test
  public void testParamRangeVarExpressionInstantiate() {
    VariableList vars = init();
    PExpression e = createExpr(vars);

    // u = 1, y[1,1] = 2, w[0,2] = 5, x = 5
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add( (new Variable("TRUE")).queryIndex() );
    truevars.add( (new Variable("u⟨0⟩")).queryIndex() );
    truevars.add( (new Variable("w[0,2]⟨0⟩")).queryIndex() );
    truevars.add( (new Variable("w[0,2]⟨2⟩")).queryIndex() );
    truevars.add( (new Variable("y[1,1]≥2")).queryIndex() );
    truevars.add( (new Variable("x≥2")).queryIndex() );
    truevars.add( (new Variable("x≥3")).queryIndex() );
    truevars.add( (new Variable("x≥4")).queryIndex() );
    truevars.add( (new Variable("x≥5")).queryIndex() );
    Solution sol = new Solution(truevars);

    ProgramState state = new ProgramState(sol);
    state.put("i", 0);
    state.put("j", 0);
    // x + w[i,y[u,j+1]] state = 5 + w[0, y[1, 0+1]] = 5 + w[0, y[1,1]] = 5 + w[0,2] = 5 + 5 = 10
    assertTrue(e.evaluate(state) == 10);
  }

  @Test
  public void testInstantiateParamBoolContainingParamRange() {
    // create zz[ y[i-1,x], x]
    VariableList vars = init();
    PExpression xx = new VariableExpression(x(vars));
    ArrayList<PExpression> args = new ArrayList<PExpression>();
    args.add( (new ParameterExpression("i")).add(-1) );
    args.add(xx);
    PExpression yy = new ParamVarExpression(y(vars), args);
    args = new ArrayList<PExpression>();
    args.add(yy);
    args.add(xx);
    PConstraint zz = new ParamBoolVarConstraint(z(vars), args, true);

    // x := 2, y[0,2] = 3, y[1,2] = 5, zz[5,2] = true, all other are false
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add( (new Variable("TRUE")).queryIndex() );
    truevars.add( (new Variable("x≥2")).queryIndex() );
    truevars.add( (new Variable("y[0,2]≥2")).queryIndex() );
    truevars.add( (new Variable("y[0,2]≥3")).queryIndex() );
    truevars.add( (new Variable("y[1,2]≥2")).queryIndex() );
    truevars.add( (new Variable("y[1,2]≥3")).queryIndex() );
    truevars.add( (new Variable("y[1,2]≥4")).queryIndex() );
    truevars.add( (new Variable("y[1,2]≥5")).queryIndex() );
    truevars.add( (new Variable("zz[5,2]")).queryIndex() );
    ProgramState state = new ProgramState(new Solution(truevars));
    state.put("i", 1);
    assertFalse(zz.evaluate(state));
    state.put("i", 2);
    assertTrue(zz.evaluate(state));
  }

  @Test(expected = language.execution.UndeclaredVariableError.class)
  public void testUndeclaredParameter() {
    PExpression e = new SumExpression(new ParameterExpression("i"),
                                      new ConstantExpression(1));
    Solution sol = new Solution(new TreeSet<Integer>());
    ProgramState state = new ProgramState(sol);
    state.evaluate(e);
  }
}

