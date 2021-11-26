package language.execution;

import logic.parameter.Match;
import java.util.ArrayList;
import java.util.Set;

/**
 * A StringFunction is a way to map a fixed number of integers to a string, meant to be used as
 * part of a print statement.
 */
public class StringFunction {
  private class MatchResult {
    Match _match;
    String _output;
    
    MatchResult(Match m, String o) { _match = m; _output = o; }
  }

  private String _name;
  private int _arity;
  private ArrayList<MatchResult> _matches;

  public StringFunction(String name, int arity) {
    _name = name;
    _arity = arity;
    _matches = new ArrayList<MatchResult>();
  }

  public void setValue(Match match, String result) {
    _matches.add(new MatchResult(match, result));
  }

  public String queryName() {
    return _name;
  }

  public int arity() {
    return _arity;
  }

  public int size() {
    return _matches.size();
  }

  /**
   * Returns the value associated with the given match, or throws an Error if there is no
   * associated value.  Matching goes first-to-last, like for integer Functions.
   */
  public String lookup(ArrayList<Integer> value) {
    for (int i = 0; i < _matches.size(); i++) {
      if (_matches.get(i)._match.isMatch(value)) return _matches.get(i)._output;
    }
    throw new Error("No valid match for string function on input " + value);
  }

  /** Easier lookup function for a single value. */
  public String lookup(int value) {
    ArrayList<Integer> values = new ArrayList<Integer>();
    values.add(value);
    return lookup(values);
  }

  public String toString() { 
    String ret = "{ ";
    for (int i = 0; i < _matches.size(); i++) {
      if (i > 0) ret += " ; ";
      ret += _matches.get(i)._match.toString() + " ⇒ \"" + _matches.get(i)._output + "\"";
    }
    return ret + " }";
  }
}

