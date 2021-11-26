package logic.parameter;

import java.util.ArrayList;

/** A Match is used in Functions and Properties to match a given tuple as input. */
public class Match {
  private ArrayList<Integer> _matches;

  /** Creates a single-int match (use null if any integer should be captured). */
  public Match(Integer arg) {
    _matches = new ArrayList<Integer>();
    _matches.add(arg);
  }

  /** Creates a double-int match (use null if any integer should be captured). */
  public Match(Integer arg1, Integer arg2) {
    _matches = new ArrayList<Integer>();
    _matches.add(arg1);
    _matches.add(arg2);
  }

  /** Creates a triple-int match (use null if any integer should be captured). */
  public Match(Integer arg1, Integer arg2, Integer arg3) {
    _matches = new ArrayList<Integer>();
    _matches.add(arg1);
    _matches.add(arg2);
    _matches.add(arg3);
  }

  /** Use an element null for a catch-all match. */
  public Match(ArrayList<Integer> match) {
    _matches = new ArrayList<Integer>(match);
  }

  /** Use an element null for a catch-all match. */
  public Match(Match match) {
    _matches = new ArrayList<Integer>(match._matches);
  }

  /** Returns the size of the tuple we are matching on. */
  public int length() {
    return _matches.size();
  }

  /** All the elements of ints should be true integers; null is not allowed. */
  public boolean isMatch(ArrayList<Integer> ints) {
    if (ints.size() != _matches.size()) return false;
    for (int i = 0; i < ints.size(); i++) {
      if (_matches.get(i) != null && !_matches.get(i).equals(ints.get(i))) return false;
    }
    return true;
  }

  public String toString() {
    if (_matches.size() == 1) return _matches.get(0) == null ? "_" : _matches.get(0).toString();

    String ret = "(";
    for (int i = 0; i < _matches.size(); i++) {
      if (i > 0) ret += ",";
      if (_matches.get(i) == null) ret += "_";
      else ret += _matches.get(i).toString();
    }
    return ret + ")";
  }
}
