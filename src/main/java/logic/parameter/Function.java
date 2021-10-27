package logic.parameter;

import java.util.Set;
import java.util.TreeMap;

/**
 * A Function is a way to map a single integer to another integer, meant to be used on parameter
 * expressions.
 */
public class Function {
  private String _name;
  private TreeMap<Integer,Integer> _mapping;
  private String _restName;
  private PExpression _restExpr;

  public Function(String name, TreeMap<Integer,Integer> mapping) {
    _name = name;
    _mapping = new TreeMap<Integer,Integer>(mapping);
    _restName = null;
    _restExpr = null;
  }

  public Function(String name, String restName, PExpression restExpr) {
    _name = name;
    _mapping = null;
    _restName = restName;
    _restExpr = restExpr;
    checkGoodRest();
  }

  public Function(String name, TreeMap<Integer,Integer> mapping, String restName,
                  PExpression restExpr) {
    _name = name;
    _mapping = new TreeMap<Integer,Integer>(mapping);
    _restName = restName;
    _restExpr = restExpr;
    checkGoodRest();
  }

  private void checkGoodRest() {
    Set<String> params = _restExpr.queryParameters();
    params.remove(_restName);
    if (params.size() > 0) {
      throw new Error("Illegal creation of Function " + _name + ": cannot use extra parameters (" +
        params.toString() + ")");
    }
  }

  public int lookup(int value) {
    if (_mapping != null && _mapping.containsKey(value)) return _mapping.get(value);
    if (_restName == null || _restExpr == null) {
      throw new Error("Trying to look up " + _name + "(" + value + ") which is not defined.");
    }
    return _restExpr.evaluate(new Assignment(_restName, value));
  }

  public String toString() { 
    return _name;
  }

  public String toString(PExpression appliedOn) {
    return _name + "(" + appliedOn.toString() + ")";
  }
}
