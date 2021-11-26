package language.execution;

import logic.sat.Variable;
import logic.parameter.ConstantExpression;
import logic.parameter.SumExpression;
import logic.parameter.ProductExpression;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.number.range.RangeVariable;
import logic.number.binary.BinaryVariable;

import java.util.TreeSet;

public class VariableExpression implements PExpression {
  private RangeVariable _rvar;
  private BinaryVariable _bvar;

  public VariableExpression(RangeVariable x) {
    _rvar = x;
    _bvar = null;
  }

  public VariableExpression(BinaryVariable x) {
    _rvar = null;
    _bvar = x;
  }

  public PExpression substitute(Substitution subst) {
    return this;
  }

  public int evaluate(Assignment ass) {
    if (ass instanceof ProgramState) {
      if (_rvar != null) return ((ProgramState)ass).queryValue(_rvar);
      else return ((ProgramState)ass).queryValue(_bvar);
    }
    throw new Error("Evaluating VariableExpression with an Assignment that is not a ProgramState.");
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
    return _rvar == null ? _bvar.toString() : _rvar.toString();
  }

  public TreeSet<String> queryParameters() {
    return new TreeSet<String>();
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
    if (!(other instanceof VariableExpression)) return false;
    VariableExpression o = (VariableExpression)other;
    if (_rvar != null) return _rvar.equals(o._rvar);
    return _bvar.equals(o._bvar);
  }
}

