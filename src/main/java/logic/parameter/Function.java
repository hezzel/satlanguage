package logic.parameter;

import java.util.ArrayList;
import java.util.Set;

/**
 * A Function is a way to map a fixed number of integers to another integer, meant to be used on
 * parameter expressions.
 */
public class Function {
  private class MatchResult {
    Match _match;
    PExpression _output;
    
    MatchResult(Match m, PExpression o) { _match = m; _output = o; }
  }

  private String _name;
  private ArrayList<String> _argumentNames;
  private ArrayList<MatchResult> _matches;

  public Function(String name, ArrayList<String> args) {
    _name = name;
    _argumentNames = new ArrayList<String>(args);
    _matches = new ArrayList<MatchResult>();
  }

  public Function(String name, String arg) {
    _name = name;
    _argumentNames = new ArrayList<String>();
    _argumentNames.add(arg);
    _matches = new ArrayList<MatchResult>();
  }

   public Function(String name, String arg1, String arg2) {
    _name = name;
    _argumentNames = new ArrayList<String>();
    _argumentNames.add(arg1);
    _argumentNames.add(arg2);
    _matches = new ArrayList<MatchResult>();
  }

  public void setValue(Match match, int result) {
    _matches.add(new MatchResult(match, new ConstantExpression(result)));
  }

  public void setValue(Match match, PExpression result) {
    Set<String> params = result.queryParameters();
    for (int i = 0; i < _argumentNames.size(); i++) params.remove(_argumentNames.get(i));
    if (params.size() > 0) {
      throw new Error("Illegal value set in Function " + _name + ": cannot use extra parameters " +
        " for match (" + params.toString() + ")");
    }
    _matches.add(new MatchResult(match, result));
  }

  private int evaluate(PExpression expr, ArrayList<Integer> args) {
    if (args.size() != _argumentNames.size()) {
      throw new Error("Invalid number of arguments to function " + _name + ": " + args.size() +
        " given, but " + _argumentNames.size() + " expected.");
    }
    Assignment ass = new Assignment();
    for (int i = 0; i < args.size(); i++) {
      ass.put(_argumentNames.get(i), args.get(i));
    }
    return expr.evaluate(ass);
  }

  public int arity() {
    return _argumentNames.size();
  }

  public int size() {
    return _matches.size();
  }

  /**
   * Returns the value associated with the given match, or throws an Error if there is no
   * associated value.  Matching goes first-to-last; that is, if two distinct matches both match,
   * then the result associated with that one that was added first is returned.  This makes it
   * possible to end the matching with a catch-all mapping such as (_,_,_) ⇒ i
   */
  public int lookup(ArrayList<Integer> value) {
    for (int i = 0; i < _matches.size(); i++) {
      if (_matches.get(i)._match.isMatch(value)) return evaluate(_matches.get(i)._output, value);
    }
    throw new Error("No valid match for function " + _name + " on input " + value);
  }

  /** Quicker lookup function for a single value. */
  public int lookup(int value) {
    ArrayList<Integer> values = new ArrayList<Integer>();
    values.add(value);
    return lookup(values);
  }

  public String queryName() {
    return _name;
  }

  public String toString() { 
    String ret = _name + "(";
    for (int i = 0; i < _argumentNames.size(); i++) {
      if (i > 0) ret += ",";
      ret += _argumentNames.get(i);
    }
    ret += ") { ";
    for (int i = 0; i < _matches.size(); i++) {
      if (i > 0) ret += " ; ";
      ret += _matches.get(i)._match.toString() + " ⇒ " + _matches.get(i)._output.toString();
    }
    return ret + " }";
  }
}

