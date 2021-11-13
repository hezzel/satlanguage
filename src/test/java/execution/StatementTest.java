import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Solution;
import logic.parameter.*;
import language.execution.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class StatementTest {
  private class Output {
    public String _txt;
    public Output() { _txt = ""; }
  }

  private class NewPrint extends Print {
    public Output _output;

    public NewPrint(Output o, StringExpression expr) {
      super(expr);
      _output = o;
    }

    public NewPrint(Output o, ArrayList<StringExpression> exprs) {
      super(exprs);
      _output = o;
    }

    protected void output(String str) {
      _output._txt += str;
    }
  }

  @Test
  public void testPrint() {
    ArrayList<StringExpression> parts = new ArrayList<StringExpression>();
    parts.add(new StringExpression("abc"));
    parts.add(new StringExpression(new SumExpression(new ConstantExpression(1),
                                                     new ConstantExpression(2))));
    Output o = new Output();
    Print pr = new NewPrint(o, parts);
    assertTrue(pr.toString().equals("print(\"abc\", 1+2)"));
    pr.execute(new ProgramState(new Solution(new TreeSet<Integer>())));
    assertTrue(o._txt.equals("abc3"));
  }

  @Test
  public void testPrintWithFunction() {
    StringFunction f = new StringFunction("f", 2);
    f.setValue(new Match(0, null), "Hello");
    f.setValue(new Match(null, 1), "World");
    ArrayList<PExpression> args = new ArrayList<PExpression>();
    args.add(new ConstantExpression(0));
    args.add(new ConstantExpression(17));
    StringExpression e1 = new StringExpression(f, args);
    args.set(0, new ConstantExpression(12));
    args.set(1, new ConstantExpression(1));
    StringExpression e2 = new StringExpression(f, args);
    Output o = new Output();
    ArrayList<StringExpression> parts = new ArrayList<StringExpression>();
    parts.add(e1);
    parts.add(new StringExpression(" "));
    parts.add(e2);
    Print pr = new NewPrint(o, parts);
    assertTrue(pr.toString().equals("print(f(0,17), \" \", f(12,1))"));
    pr.execute(new ProgramState(new Solution(new TreeSet<Integer>())));
    assertTrue(o._txt.equals("Hello World"));
  }

  @Test
  public void testAssign() {
    Statement let1 = new Let("i", new ConstantExpression(5));
    Statement let2 = new Let("i", (new ParameterExpression("i")).add(1));
    assertTrue(let1.toString().equals("i := 5"));
    assertTrue(let2.toString("  ").equals("  i := i+1"));
    Solution sol = new Solution(new TreeSet<Integer>());
    ProgramState stat = new ProgramState(sol);
    let1.execute(stat);
    let2.execute(stat);
    assertTrue(stat.get("i") == 6);
  }

  @Test
  public void testIfThenElse() {
    Output o = new Output();
    Print pr1 = new NewPrint(o, new StringExpression("a"));
    Print pr2 = new NewPrint(o, new StringExpression("b"));
    Statement ifst = new If(new SmallerConstraint(new ParameterExpression("i"),
                                                  new ConstantExpression(0)),
                            pr1, pr2);
    Solution sol = new Solution(new TreeSet<Integer>());
    ProgramState stat = new ProgramState(sol);
    stat.put("i", -1);
    ifst.execute(stat);
    stat.put("i", 1);
    ifst.execute(stat);
    assertTrue(o._txt.equals("ab"));
  }

  @Test
  public void testIfThen() {
    Output o = new Output();
    Print pr1 = new NewPrint(o, new StringExpression("a"));
    Statement ifst = new If(new EqualConstraint(new ParameterExpression("i"),
                                                new ConstantExpression(0)), pr1, null);
    Solution sol = new Solution(new TreeSet<Integer>());
    ProgramState stat = new ProgramState(sol);
    stat.put("i", -1);
    ifst.execute(stat);
    assertTrue(o._txt.equals(""));
    stat.put("i", 0);
    ifst.execute(stat);
    assertTrue(o._txt.equals("a"));
  }

  @Test
  public void testFor() {
    Output o = new Output();
    ArrayList<StringExpression> parts = new ArrayList<StringExpression>();
    parts.add(new StringExpression(new ParameterExpression("i")));
    parts.add(new StringExpression(" "));
    Print pr = new NewPrint(o, parts);
    Statement statement = new For("i", new ConstantExpression(1), new ConstantExpression(5), pr);
    ProgramState state = new ProgramState(new Solution(new TreeSet<Integer>()));
    statement.execute(state);
    assertTrue(o._txt.equals("1 2 3 4 5 "));
  }

  @Test
  public void testForWithParameters() {
    Output o = new Output();
    PExpression minimum = new ParameterExpression("x");
    PExpression maximum = (new ParameterExpression("x")).add(5);
    ArrayList<StringExpression> parts = new ArrayList<StringExpression>();
    parts.add(new StringExpression(new ParameterExpression("i")));
    parts.add(new StringExpression(" "));
    Print pr = new NewPrint(o, parts);
    Statement statement = new For("i", minimum, maximum, pr);
    ProgramState state = new ProgramState(new Solution(new TreeSet<Integer>()));
    state.put("i", 2);

    state.put("x", 0);
    statement.execute(state);
    assertTrue(o._txt.equals("0 1 2 3 4 5 "));
    assertTrue(state.get("i") == 5);
    assertTrue(state.get("x") == 0);

    o._txt = "";
    state.put("x", 7);
    statement.execute(state);
    assertTrue(o._txt.equals("7 8 9 10 11 12 "));
  }

  @Test
  public void testBlock() {
    Output o = new Output();
    ArrayList<Statement> prs = new ArrayList<Statement>();
    prs.add(new NewPrint(o, new StringExpression("X")));
    prs.add(new NewPrint(o, new StringExpression("Y")));
    Statement statement = new Block(prs);
    ProgramState state = new ProgramState(new Solution(new TreeSet<Integer>()));
    statement.execute(state);
    assertTrue(o._txt.equals("XY"));
  }
}

