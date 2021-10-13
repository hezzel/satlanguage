package logic.parameter;

import java.util.Set;
import java.util.TreeMap;

/**
 * An Assignment is a structure that maps each parameter to an integer.
 * The parameter is only referred to by name, so it is not guaranteed that the integer will be in
 * range for the parameter; this should be checked when using the assignment in parametrised
 * variables.
 * Note that an Assignment is not immutable, as mappings can be added and removed. If you want to
 * duplicate an existing Assignment, use the dedicated constructor.
 */
public class Assignment {
  TreeMap<String,Integer> _values;

  /** Generates an empty assignment (no parameter is assigned to). */
  public Assignment() {
    _values = new TreeMap<String,Integer>();
  }

  /** Generates an assignment with one param → value pair. */
  public Assignment(String param, int val) {
    _values = new TreeMap<String,Integer>();
    _values.put(param, val);
  }

  /** Generates a copy of the given assignment. */
  public Assignment(Assignment ass) {
    _values = new TreeMap<String,Integer>(ass._values);
  }

  /** Generates an assignment with two param → value pairs. */
  public Assignment(String param1, int val1, String param2, int val2) {
    _values = new TreeMap<String,Integer>();
    _values.put(param1, val1);
    _values.put(param2, val2);
  }

  /** Generates an assignment with three param → value pairs. */
  public Assignment(String param1, int val1, String param2, int val2, String param3, int val3) {
    _values = new TreeMap<String,Integer>();
    _values.put(param1, val1);
    _values.put(param2, val2);
    _values.put(param3, val3);
  }

  /** Adds a param → value pair to the assignment (or overrides an existing one). */
  public void put(String paramName, int value) {
    _values.put(paramName, value);
  }

  /**
   * Removes the mapping for a parameter.
   * If the parameter is not defined in the assignment, this will throw an Error.
   */
  public void remove(String paramName) {
    if (!_values.containsKey(paramName)) {
      throw new Error("Trying to remove parameter " + paramName + " which is not there.");
    }
    _values.remove(paramName);
  }

  /** Returns true if the Assignment has a value for the given parameter. */
  public boolean defines(String paramName) {
    return _values.containsKey(paramName);
  }

  /** Returns the value for the given parameter if one is given, or throws an Error if not. */
  public int get(String paramName) {
    if (_values.containsKey(paramName)) return _values.get(paramName);
    throw new Error("Looking up undefined parameter " + paramName);
  }

  /** Returns all the parameters which have a value in the assignment. */
  public Set<String> queryKeys() {
    return _values.keySet();
  }

  /** Returns a string representation of the assignment. */
  public String toString() {
    return _values.toString();
  }
}

