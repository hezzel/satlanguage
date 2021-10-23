package logic.range;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.ParameterExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import logic.parameter.ParameterList;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * A quantified range variable is the combination of a ParamRangeVar with a substitution on its
 * parameters.  When the parameters are all instantiated, this can be evaluated to a RangeVariable.
 */
public class QuantifiedRangeVariable implements QuantifiedRangeInteger {
  private ParamRangeVar _pvar;
  private Substitution _substitution;

  public QuantifiedRangeVariable(ParamRangeVar x, Substitution subst) {
    _pvar = x;
    _substitution = subst;
  }

  public QuantifiedRangeVariable(ParamRangeVar x, ArrayList<PExpression> args) {
    ParameterList lst = x.queryParameters();
    if (lst.size() != args.size()) {
      throw new Error("QuantifiedRangeVariable created with args of size " + args.size() +
        " while " + lst.size() + " arguments are expected.");
    }
    _pvar = x;
    _substitution = new Substitution();
    for (int i = 0; i < lst.size(); i++) {
      _substitution.put(lst.get(i).queryName(), args.get(i));
    }
  }

  public TreeSet<String> queryParameters() {
    TreeSet<String> ret = new TreeSet<String>();
    ParameterList lst = _pvar.queryParameters();
    for (int i = 0; i < lst.size(); i++) {
      String p = lst.get(i).queryName();
      PExpression e = _substitution.get(p);
      if (e == null) ret.add(p);
      else ret.addAll(e.queryParameters());
    }
    return ret;
  }

  public boolean queryClosed() {
    return queryParameters().size() == 0;
  }

  public QuantifiedRangeVariable substitute(Substitution subst) {
    Substitution newsubst = new Substitution();
    ParameterList lst = _pvar.queryParameters();
    for (int i = 0; i < lst.size(); i++) {
      String name = lst.get(i).queryName();
      PExpression p = new ParameterExpression(name);
      p = p.substitute(_substitution);
      p = p.substitute(subst);
      newsubst.put(name, p);
    }
    return new QuantifiedRangeVariable(_pvar, newsubst);
  }

  public RangeVariable instantiate(Assignment ass) {
    Assignment newass = new Assignment();
    ParameterList lst = _pvar.queryParameters();
    for (int i = 0; i < lst.size(); i++) {
      String name = lst.get(i).queryName();
      PExpression p = new ParameterExpression(name);
      p = p.substitute(_substitution);
      newass.put(name, p.evaluate(ass));
    }
    return _pvar.queryVar(newass);
  }

  public String toString() {
    return _pvar.toString(_substitution);
  }
}

