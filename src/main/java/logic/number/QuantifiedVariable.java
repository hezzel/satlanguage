package logic.number;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.ParameterExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.parameter.ParameterList;
import logic.number.range.RangeVariable;
import logic.number.range.ParamRangeVar;
import logic.number.binary.BinaryVariable;
import logic.number.binary.ParamBinaryVar;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * A quantified variable is the combination of either a ParamRangeVar or a ParamBinaryVar with a
 * substitution on its parameters.  When the parameters are all instantiated, this can be evaluated
 * to a VariableInteger.
 */
public class QuantifiedVariable implements QuantifiedInteger {
  private ParamRangeVar _rvar;
  private ParamBinaryVar _bvar;
  private ParameterList _parameters;
  private Substitution _substitution;

  private QuantifiedVariable(ParamRangeVar r, ParamBinaryVar b, Substitution subst) {
    _rvar = r;
    _bvar = b;
    if (_rvar != null) _parameters = _rvar.queryParameters();
    else _parameters = _bvar.queryParameters();
    _substitution = subst;
  }

  public QuantifiedVariable(ParamRangeVar x, Substitution subst) {
    _rvar = x;
    _bvar = null;
    _substitution = subst;
    _parameters = x.queryParameters();
  }

  public QuantifiedVariable(ParamBinaryVar x, Substitution subst) {
    _bvar = x;
    _rvar = null;
    _substitution = subst;
    _parameters = x.queryParameters();
  }

  private void buildSubstitution(String name, ArrayList<PExpression> args) {
    if (_parameters.size() != args.size()) {
      throw new Error("QuantifiedVariable " + name + " created with args of size " +
        args.size() + " while " + _parameters.size() + " arguments are expected.");
    }
    _substitution = new Substitution();
    for (int i = 0; i < _parameters.size(); i++) {
      _substitution.put(_parameters.get(i).queryName(), args.get(i));
    }
  }

  public QuantifiedVariable(ParamRangeVar x, ArrayList<PExpression> args) {
    _rvar = x;
    _bvar = null;
    _parameters = x.queryParameters();
    buildSubstitution(x.toString(), args);
  }

  public QuantifiedVariable(ParamBinaryVar x, ArrayList<PExpression> args) {
    _bvar = x;
    _rvar = null;
    _parameters = x.queryParameters();
    buildSubstitution(x.toString(), args);
  }

  public TreeSet<String> queryParameters() {
    TreeSet<String> ret = new TreeSet<String>();
    for (int i = 0; i < _parameters.size(); i++) {
      String p = _parameters.get(i).queryName();
      PExpression e = _substitution.get(p);
      if (e == null) ret.add(p);
      else ret.addAll(e.queryParameters());
    }
    return ret;
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedVariable substitute(Substitution subst) {
    Substitution newsubst = new Substitution();
    for (int i = 0; i < _parameters.size(); i++) {
      String name = _parameters.get(i).queryName();
      PExpression p = new ParameterExpression(name);
      p = p.substitute(_substitution);
      p = p.substitute(subst);
      newsubst.put(name, p);
    }
    return new QuantifiedVariable(_rvar, _bvar, newsubst);
  }

  public VariableInteger instantiate(Assignment ass) {
    Assignment newass = new Assignment();
    for (int i = 0; i < _parameters.size(); i++) {
      String name = _parameters.get(i).queryName();
      PExpression p = new ParameterExpression(name);
      p = p.substitute(_substitution);
      newass.put(name, p.evaluate(ass));
    }
    if (_rvar != null) return new VariableInteger(_rvar.queryVar(newass));
    else return new VariableInteger(_bvar.queryVar(newass));
  }

  public String toString() {
    return _rvar == null ? _bvar.toString(_substitution) : _rvar.toString(_substitution);
  }
}

