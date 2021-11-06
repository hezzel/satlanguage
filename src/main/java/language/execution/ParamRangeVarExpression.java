package language.execution;

import logic.sat.Variable;
import logic.parameter.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;

import java.util.ArrayList;
import java.util.TreeSet;

public class ParamRangeVarExpression implements PExpression {
  private ParamRangeVar _var;
  private ArrayList<PExpression> _arguments;

  public ParamRangeVarExpression(ParamRangeVar x, ArrayList<PExpression> args) {
    _var = x;
    _arguments = new ArrayList<PExpression>(args);
    if (_arguments.size() != x.queryParameters().size()) {
      throw new Error("ParamRangeVarExpression created with " + _arguments.size() + " arguments " +
        "given; " + x.queryParameters().size() + " expected.");
    }
  }

  public PExpression substitute(Substitution subst) {
    ArrayList<PExpression> args = new ArrayList<PExpression>(_arguments);
    for (int i = 0; i < args.size(); i++) args.set(i, args.get(i).substitute(subst));
    return new ParamRangeVarExpression(_var, args);
  }

  public int evaluate(Assignment ass) {
    Assignment indexes = new Assignment();
    ParameterList params = _var.queryParameters();
    for (int i = 0; i < params.size(); i++) {
      indexes.put(params.get(i).queryName(), _arguments.get(i).evaluate(ass));
    }
    RangeVariable v = _var.queryVar(indexes);
    if (ass instanceof ProgramState) return ((ProgramState)ass).queryValue(v);
    throw new Error("Evaluating ParamRangeVarExpression with Assignment that is not a " +
      "ProgramState.");
  }

  public int queryKind() {
    return PExpression.PARAMETER;
  }

  public boolean queryConstant() {
    return false;
  }

  public PExpression queryLeft() {
    return null;
  }

  public PExpression queryRight() {
    return null;
  }

  public int queryLeadingInteger() {
    return 1;
  }

  public String toString() {
    return _var.toString(_arguments);
  }

  public TreeSet<String> queryParameters() {
    TreeSet<String> ret = new TreeSet<String>();
    for (int i = 0; i < _arguments.size(); i++) {
      ret.addAll(_arguments.get(i).queryParameters());
    }   
    return ret;
  }

  public PExpression add(int value) {
    if (value == 0) return this;
    return new SumExpression(this, new ConstantExpression(value));
  }

  public PExpression multiply(int value) {
    if (value == 0) return new ConstantExpression(0);
    if (value == 1) return this;
    return new ProductExpression(new ConstantExpression(value), this);
  }

  public boolean equals(PExpression other) {
    if (!(other instanceof ParamRangeVarExpression)) return false;
    ParamRangeVarExpression o = (ParamRangeVarExpression)other;
    if (!o._var.equals(_var)) return false;
    for (int i = 0; i < _arguments.size(); i++) {
      if (!_arguments.get(i).equals(o._arguments.get(i))) return false;
    }
    return true;
  }
}
