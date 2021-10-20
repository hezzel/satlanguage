package logic.parameter;

import java.util.Set;
import java.util.TreeMap;

/** A Substitution is a structure that maps some parameter names to PExpressions. */
public class Substitution {
  TreeMap<String,PExpression> _values;

  /** Generates an empty substitution (no parameter is assigned to). */
  public Substitution() {
    _values = new TreeMap<String,PExpression>();
  }

  /** Generates a substitution corresponding to the given assignment. */
  public Substitution(Assignment ass) {
    _values = new TreeMap<String,PExpression>();
    Set<String> keys = ass.queryKeys();
    for (String key : keys) _values.put(key, new ConstantExpression(ass.get(key)));
  }

  /** Copies the given substitution. */
  public Substitution(Substitution subst) {
    _values = new TreeMap<String,PExpression>();
    Set<String> keys = subst.queryKeys();
    for (String key : keys) _values.put(key, subst.get(key));
  }

  /** Generates a substitution with one param → value pair. */
  public Substitution(String param, PExpression e) {
    _values = new TreeMap<String,PExpression>();
    _values.put(param, e);
  }

  /** Generates a substitution with two param → value pairs. */
  public Substitution(String param1, PExpression e1, String param2, PExpression e2) {
    _values = new TreeMap<String,PExpression>();
    _values.put(param1, e1);
    _values.put(param2, e2);
  }

  /** Generates a substitution with three param → value pairs. */
  public Substitution(String param1, PExpression e1, String param2, PExpression e2,
                      String param3, PExpression e3) {
    _values = new TreeMap<String,PExpression>();
    _values.put(param1, e1);
    _values.put(param2, e2);
    _values.put(param3, e3);
  }

  /** Adds a param → value pair to the substitution (or overrides an existing one). */
  public void put(String paramName, PExpression e) {
    _values.put(paramName, e);
  }

  /**
   * Removes the mapping for a substitution.
   * If the parameter is not defined in the substitution, this will simply do nothing.
   */
  public void remove(String paramName) {
    _values.remove(paramName);
  }

  /** Returns the value for the given parameter if one is given, or null if not. */
  public PExpression get(String paramName) {
    if (_values.containsKey(paramName)) return _values.get(paramName);
    else return null;
  }

  /** Returns all the parameters which have a value in the substitution. */
  public Set<String> queryKeys() {
    return _values.keySet();
  }

  public String toString() {
    return _values.toString();
  }
}

