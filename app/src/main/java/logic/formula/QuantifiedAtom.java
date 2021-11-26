package logic.formula;

import logic.sat.*;
import logic.parameter.*;
import java.util.ArrayList;

/**
 * A QuantifiedAtom represents a single atom built using a ParamBoolVar, but one which may have
 * unknowns in its parameter list; for example myvar[i,3,j+1], where i and j are still unknown.
 */
public class QuantifiedAtom extends Formula {
  private ParamBoolVar _variable;
  private boolean _negated;
  private ArrayList<PExpression> _paramValues;

  /**
   * This creates the atom x[params_1,...,params_n], where n = length(params) should be the
   * expected number of parameters for the parametrised variable x.
   */
  public QuantifiedAtom(ParamBoolVar x, boolean value, ArrayList<PExpression> params) {
    super();
    _variable = x;
    _negated = !value;
    _paramValues = new ArrayList<PExpression>(params);
    
    // make sure _usedParameters reflects the parameters actually used
    for (int i = 0; i < params.size(); i++) {
      _usedParameters.addAll(params.get(i).queryParameters());
    }

    // assert that we have been given the right number of parameters
    if (_paramValues.size() != _variable.queryParameters().size()) {
      throw new Error("Creating a QuantifiedAtom with var " + x.toString() + " which should be " +
        "given " + _variable.queryParameters().size() + " variables but is instead given " +
        _paramValues.size() + ".");
    }
  }

  /**
   * This creates the atom x[param_1 gamma, ..., param_n gamma], where [param_1,...,param_n] is the
   * parameter list for the variable x.
   */
  public QuantifiedAtom(ParamBoolVar x, boolean value, Substitution gamma) {
    super();
    _variable = x;
    _negated = !value;
    
    // set up _paramValues based on gamma
    ParameterList ps = x.queryParameters();
    _paramValues = new ArrayList<PExpression>();
    for (int i = 0; i < ps.size(); i++) {
      String paramname = ps.get(i).queryName();
      PExpression assignment = gamma.get(paramname);
      if (assignment == null) assignment = new ParameterExpression(paramname);
      _paramValues.add(assignment);
    }

    // make sure _usedParameters reflects the parameters actually used
    for (int i = 0; i < _paramValues.size(); i++) {
      _usedParameters.addAll(_paramValues.get(i).queryParameters());
    }
  }

  public Formula negate() {
    return new QuantifiedAtom(_variable, _negated, _paramValues);
  }

  /**
   * Subsitutes the quantified atom by replacing some parameters by their substituted values.
   * If all parameters are instantiated to constants, this yields a regular Atom; otherwise it
   * results in a quantified atom again (but now perhaps with fewer parameters).
   */
  public Formula substitute(Substitution subst) {
    ArrayList<PExpression> instantiated = new ArrayList<PExpression>();
    Assignment gamma = new Assignment();
    for (int i = 0; i < _paramValues.size(); i++) {
      instantiated.add(_paramValues.get(i).substitute(subst));
      if (gamma != null && instantiated.get(i).queryConstant()) {
        gamma.put(_variable.queryParameters().get(i).queryName(),
                  instantiated.get(i).evaluate(null));
      }
      else gamma = null;
    }
    if (gamma == null) return new QuantifiedAtom(_variable, !_negated, instantiated);
    else return new AtomicFormula(new Atom(_variable.queryVar(gamma), !_negated));
  }

  /**
   * If all parameters are actually known, the current Formula is an Atom, which is returned.
   * Otherwise, null is returned.
   */
  public Atom queryAtom() {
    if (_usedParameters.size() != 0) return null;
    Assignment ass = new Assignment();
    // for each of the parameters, evaluate the corresponding value to an integer, and store the
    // result into ass
    for (int i = 0; i < _paramValues.size(); i++) {
      int value = _paramValues.get(i).evaluate(null);
      ass.put(_variable.queryParameters().get(i).queryName(), value);
    }
    Variable v = _variable.queryVar(ass);
    return new Atom(v, !_negated);
  }

  private void throwError(String method) {
    throw new Error("Called " + method + " on " + toString() + ", which is not closed.");
  }

  public void addClauses(ClauseCollection col) {
    Atom at = queryAtom();
    if (at == null) throwError("addClauses");
    col.addClause(new Clause(queryAtom()));
  }

  /** Adds clauses corresponding to x → this to col. */
  public void addClausesIfThisIsImpliedBy(Atom x, ClauseCollection col) {
    Atom at = queryAtom();
    if (at == null) throwError("addClauses");
    col.addClause(new Clause(x.negate(), at));
  }

  /** Adds clauses corresponding to this → x to col. */
  public void addClausesIfThisImplies(Atom x, ClauseCollection col) {
    Atom at = queryAtom();
    if (at == null) throwError("addClauses");
    col.addClause(new Clause(at.negate(), x));
  }

  /** @return 0 */
  public int queryAssocLevel() {
    return Formula.ATOM;
  }

  public String toString() {
    String ret = _variable.toString(_paramValues);
    if (_negated) ret = "¬" + ret;
    return ret;
  }
}

