package logic.parameter;

import java.util.ArrayList;

/**
 * A ParametrisedObject is a set of specific objects (type T), indexed with one or more named
 * integer parameters which are allowed to occupy a fixed range.
 */
public class ParametrisedObject<T> {
  protected String _name;
  protected ParameterList _parameters;
  protected ParamTree<T> _objects;

  /** Creates a parametrised object with a given list of parameters. */
  public ParametrisedObject(String name, ParameterList params,
                            ParamTree.ConstructorHelper<T> helper) {
    _name = name;
    _parameters = params;
    _objects = new ParamTree<T>(params, helper);
  }

  /**
   * Returns the name with a parameter list [p1,...,pn] where, in place of each parameter pi, the
   * substituted expression is printed.  If a parameter is not in the substitution, then its name
   * is printed directly.
   * The function is static (and assumes the _name and _parameter elements given) so it can be used
   * as part of the constructor.
   */
  protected static String queryObjectName(String name, ParameterList params,
                                          Substitution substitution) {
    name += "[";
    for (int i = 0; i < params.size(); i++) {
      String pname = params.get(i).queryName();
      String arg = ( (new ParameterExpression(pname)).substitute(substitution) ).toString();
      if (i > 0) name += "," + arg;
      else name += arg;
    }
    return name + "]";
  }

  protected String queryObjectName(Substitution substitution) {
    return queryObjectName(_name, _parameters, substitution);
  }

  /** Returns the parameter list for this object. */
  public ParameterList queryParameters() {
    return _parameters;
  }

  /**
   * This returns the object corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper object), then null
   * is returned instead.
   * If not all required parameters are provided, then an Error is thrown instead.
   */
  protected T queryObject(Assignment values) {
    return _objects.lookup(values);
  }

  /**
   * Returns a string representation where the given values are printed in place of the parameter
   * positions of the object; if a parameter value is not given, then the name of the parameter
   * is printed instead.
   */
  public String toString(ArrayList<PExpression> parameterValues) {
    Substitution subst = new Substitution();
    for (int i = 0; i < _parameters.size(); i++) {
      if (parameterValues.get(i) == null) continue;
      subst.put(_parameters.get(i).queryName(), parameterValues.get(i));
    }
    return queryObjectName(_name, _parameters, subst);
  }

  /**
   * Returns a string representation where the parameters are replaced by their substituted
   * expression.
   */
  public String toString(Substitution subst) {
    return queryObjectName(subst);
  }

  public String toString() {
    return queryObjectName(new Substitution());
  }
}

