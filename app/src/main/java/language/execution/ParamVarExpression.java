package language.execution;

import logic.sat.Variable;
import logic.parameter.*;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;

import java.util.ArrayList;
import java.util.TreeSet;

public class ParamVarExpression implements PExpression {
  private ParamRangeVar _rvar;
  private ParamBinaryVar _bvar;
  private ArrayList<PExpression> _arguments;

  public ParamVarExpression(ParamRangeVar x, ArrayList<PExpression> args) {
    _rvar = x;
    _arguments = new ArrayList<PExpression>(args);
    if (_arguments.size() != x.queryParameters().size()) {
      throw new Error("ParamVarExpression created with " + _arguments.size() + " arguments " +
        "given; range variable " + x.toString() + " expects " + x.queryParameters().size() + ".");
    }
  }

  public ParamVarExpression(ParamBinaryVar x, ArrayList<PExpression> args) {
    _bvar = x;
    _arguments = new ArrayList<PExpression>(args);
    if (_arguments.size() != x.queryParameters().size()) {
      throw new Error("ParamVarExpression created with " + _arguments.size() + " arguments " +
        "given; binary variable " + x.toString() + " expects " + x.queryParameters().size() + ".");
    }
  }

  private ParamVarExpression(ParamRangeVar rvar, ParamBinaryVar bvar, ArrayList<PExpression> args) {
    _rvar = rvar;
    _bvar = bvar;
    _arguments = args;
  }

  public PExpression substitute(Substitution subst) {
    ArrayList<PExpression> args = new ArrayList<PExpression>(_arguments);
    for (int i = 0; i < args.size(); i++) args.set(i, args.get(i).substitute(subst));
    return new ParamVarExpression(_rvar, _bvar, args);
  }

  public int evaluate(Assignment ass) {
    Assignment indexes = new Assignment();
    ParameterList params = _rvar == null ? _bvar.queryParameters() : _rvar.queryParameters();
    for (int i = 0; i < params.size(); i++) {
      indexes.put(params.get(i).queryName(), _arguments.get(i).evaluate(ass));
    }
    if (!(ass instanceof ProgramState)) {
      throw new Error("Evaluating ParamVarExpression with Assignment that is not a " +
        "ProgramState.");
    }
    if (_rvar != null) return ((ProgramState)ass).queryValue(_rvar.queryVar(indexes));
    else return ((ProgramState)ass).queryValue(_bvar.queryVar(indexes));
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
    return _rvar == null ? _bvar.toString(_arguments) : _rvar.toString(_arguments);
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
    if (!(other instanceof ParamVarExpression)) return false;
    ParamVarExpression o = (ParamVarExpression)other;
    if (_rvar != null && !_rvar.equals(o._rvar)) return false;
    if (_bvar != null && !_bvar.equals(o._bvar)) return false;
    for (int i = 0; i < _arguments.size(); i++) {
      if (!_arguments.get(i).equals(o._arguments.get(i))) return false;
    }
    return true;
  }
}

