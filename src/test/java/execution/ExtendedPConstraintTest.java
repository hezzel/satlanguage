import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Solution;
import logic.parameter.*;
import logic.VariableList;
import language.execution.UndeclaredVariableError;
import language.execution.ProgramState;
import language.execution.VariableConstraint;
import language.execution.ParamBoolVarConstraint;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class ExtendedPConstraintTest {
  private VariableList init() {
    VariableList ret = new VariableList();
    ret.registerBooleanVariable("x");
    ret.registerParametrisedBooleanVariable("y", new ParameterList(
      new Parameter("i", 0, 1), new Parameter("j", 1, 2)));
    return ret;
  }

  private Variable x(VariableList vars) {
    return vars.queryBooleanVariable("x");
  }

  private ParamBoolVar y(VariableList vars) {
    return vars.queryParametrisedBooleanVariable("y");
  }

  private PConstraint createConstr(VariableList vars) {
    // ¬x ∧ (y[b+1,c] ∨ a != c)
    return new AndConstraint(
      new VariableConstraint(x(vars), false),
      new OrConstraint(
        new NeqConstraint(
          new ParameterExpression("b"),
          new ParameterExpression("c")
        ),
        new NeqConstraint(
          new ParameterExpression("a"),
          new ParameterExpression("c")
        )
      )
    );
  }

  @Test
  public void testVariableConstraintBasics() {
    VariableList vars = init();
    PConstraint c = new AndConstraint(new TrueConstraint(), new VariableConstraint(x(vars), true));
    assertTrue(c.toString().equals("⊤ ∧ x"));
    assertTrue(c.negate().toString().equals("⊥ ∨ ¬x"));
    assertFalse(c.isTop());
    assertTrue(c.queryParameters().size() == 0);
    Substitution subst = new Substitution("x", new ConstantExpression(0));
    assertTrue(c.substitute(subst).toString().equals("x"));
  }

  @Test
  public void testVariableConstraintInstantiate() {
    VariableList vars = init();
    PConstraint c = new AndConstraint(new TrueConstraint(), new VariableConstraint(x(vars), true));
    // evaluate constraint with x := false
    TreeSet<Integer> truevars = new TreeSet<Integer>();
    Solution sol = new Solution(truevars);
    ProgramState state = new ProgramState(sol);
    assertFalse(c.evaluate(state));
    // evaluate constraint with x := true
    truevars.add(x(vars).queryIndex());
    sol = new Solution(truevars);
    state = new ProgramState(sol);
    assertTrue(c.evaluate(state));
  }

  @Test
  public void testParamBoolVarConstraintBasics() {
    VariableList vars = init();
    ArrayList<PExpression> args = new ArrayList<PExpression>();
    args.add(new ConstantExpression(0));
    args.add(new ParameterExpression("a"));
    PConstraint ileq1 =
      new SmallerConstraint(new ParameterExpression("i"), new ConstantExpression(1));
    PConstraint c = new OrConstraint(ileq1, new ParamBoolVarConstraint(y(vars), args, false));

    assertTrue(c.toString().equals("i < 1 ∨ ¬y[0,a]"));
    assertTrue(c.negate().toString().equals("0 < i ∧ y[0,a]"));
    assertFalse(c.isTop());
    assertTrue(c.queryParameters().size() == 2);
    Substitution subst = new Substitution("a", new ParameterExpression("j"),
                                          "i", new ParameterExpression("j"));
    PConstraint s = c.substitute(subst);
    assertTrue(s.toString().equals("j < 1 ∨ ¬y[0,j]"));
    assertTrue(s.queryParameters().size() == 1);
  }

  @Test
  public void testParamBoolVarConstraintInstantiate() {
    VariableList vars = init();
    ArrayList<PExpression> args = new ArrayList<PExpression>();
    args.add(new ParameterExpression("a"));
    args.add(new ParameterExpression("b"));
    PConstraint ileq1 =
      new SmallerConstraint(new ConstantExpression(1), new ConstantExpression(0));
    PConstraint c = new OrConstraint(ileq1, new ParamBoolVarConstraint(y(vars), args, false));

    TreeSet<Integer> truevars = new TreeSet<Integer>();
    truevars.add( (new Variable("y[0,1]")).queryIndex() );
    truevars.add( (new Variable("y[0,2]")).queryIndex() );
    truevars.add( (new Variable("y[1,2]")).queryIndex() );
    Solution sol = new Solution(truevars);
    ProgramState state = new ProgramState(sol);

    // evaluate 0 < 1 ∨ y[0,1]
    state.put("a", 0); state.put("b", 1);
    assertTrue(c.evaluate(state));
    // evaluate 0 < 1 ∨ y[0,2]
    state.put("a", 0); state.put("b", 2);
    assertTrue(c.evaluate(state));
    // evaluate 0 < 1 ∨ y[1,1]
    state.put("a", 1); state.put("b", 1);
    assertFalse(c.evaluate(state));
    // evaluate 0 < 1 ∨ y[1,3] (out of range)
    state.put("a", 1); state.put("b", 3);
    assertFalse(c.evaluate(state));
  }

  @Test(expected = language.execution.UndeclaredVariableError.class)
  public void testUndeclaredParameter() {
    PConstraint constr = new SmallerConstraint(new ParameterExpression("i"),
                                               new ConstantExpression(1));
    Solution sol = new Solution(new TreeSet<Integer>());
    ProgramState state = new ProgramState(sol);
    state.evaluate(constr);
  }
}

