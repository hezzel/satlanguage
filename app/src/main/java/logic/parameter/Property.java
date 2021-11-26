package logic.parameter;

import java.util.ArrayList;
import java.util.Set;

/**
 * A Property is essentially a boolean function: a set of integer tuples which is considered "true"
 * with everything not listed in the property mapped to "false".
 * The tuples do not all need to have the same length.
 */
public class Property {
  private String _name;
  private ArrayList<Match> _matches;

  public Property(String name) {
    _name = name;
    _matches = new ArrayList<Match>();
  }

  public void add(Match match) {
    _matches.add(match);
  }

  public int size() {
    return _matches.size();
  }

  /**
   * Returns true if the given values are in the property, false otherwise.  The entries to value
   * are not allowed to be null.
   */
  public boolean lookup(ArrayList<Integer> value) {
    for (int i = 0; i < _matches.size(); i++) {
      if (_matches.get(i).isMatch(value)) return true;
    }
    return false;
  }

  /** Quicker lookup function for a single value. */
  public boolean lookup(int value) {
    ArrayList<Integer> values = new ArrayList<Integer>();
    values.add(value);
    return lookup(values);
  }

  public String queryName() {
    return _name;
  }

  public String toString() { 
    StringBuilder ret = new StringBuilder(_name + " {");
    for (int i = 0; i < _matches.size(); i++) {
      if (i > 0) ret.append(" ; ");
      ret.append(_matches.get(i).toString());
    }
    ret.append(" }");
    return ret.toString();
  }
}

