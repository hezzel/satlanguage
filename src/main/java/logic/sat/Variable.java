package logic.sat;

import java.lang.Comparable;
import java.util.TreeMap;

/**
 * A variable is uniquely defined by its name.  Variables are always booleans.
 * Following the standards of the SAT competition, each variable is identified with a unique
 * identifier.  Hence, within the program, name and identifier should be equivalent.
 * (To avoid overlaps,it is advised that user-defined variables should satisfy some restrictions
 * that automatically generated variables do not.)
 * Variables are immutable objects.
 */
public class Variable implements Comparable<Variable> {
  private int _id;
  private String _name;

  private static int _lastUsed = 0;
  private static TreeMap<String,Integer> _nameToId = new TreeMap<String,Integer>();

  /**
   * This resets the registered variables, so that variables will be numbered from 1 onwards again
   * and all names in the system are forgotten.
   * Do this only when starting a new SAT problem, since newly created variables will now get the
   * same ID as some previous variables.
   */
  public static void reset() {
    _lastUsed = 0;
    _nameToId = new TreeMap<String,Integer>();
  }

  /** This generates a name that is not yet in use. */
  public static String generateFresh() {
    String name = "_var" + _lastUsed;
    while (_nameToId.containsKey(name)) {
      _lastUsed++;
      name = "_var" + _lastUsed;
    }
    return name;
  }

  /** This returns whether a variable by the given name already exists. */
  public boolean exists(String name) {
    return _nameToId.containsKey(name);
  }

  /**
   * This creates a variable with the given name.
   * If the name was previously used, the corresponding ID will be the same as the previous one
   * with that name.  If it wasn't used, a fresh ID is generated.
   */
  public Variable(String name) {
    if (_nameToId.containsKey(name)) {
      _name = name;
      _id = _nameToId.get(name);
    }
    else {
      _lastUsed++;
      _id = _lastUsed;
      _name = name;
      _nameToId.put(_name, _id);
    }
  }

  public int compareTo(Variable x) {
    if (x._id < _id) return 1;
    if (x._id > _id) return -1;
    return 0;
  }

  /**
   * Two variables are equal if and only if they have the same ID.
   * (By construction, this can hold if and only if they have the same name.)
   */
  public boolean equals(Variable x) {
    return compareTo(x) == 0;
  }

  /** @return the variable's unique identifier */
  public int queryIndex() {
    return _id;
  }

  /** @return the variable's name */
  public String toString() {
    return _name;
  }
}

