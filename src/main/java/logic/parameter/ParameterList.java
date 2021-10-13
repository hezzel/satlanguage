package logic.parameter;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * A list of parameters in a fixed order, so that the definition of each may only refer to
 * previous parameters in the list (in its minimum, maximum or restriction).
 */
public class ParameterList {
  private ArrayList<Parameter> _lst;

  /**
   * This function does the correctness check for the constructors, and throws an error if the
   * list is not valid.
   */
  private void checkValidity() {
    TreeSet<String> sofar = new TreeSet<String>();
    Set<String> used;

    for (int i = 0; i < _lst.size(); i++) {
      Parameter p = _lst.get(i);
      if (sofar.contains(p.queryName())) {
        throw new Error("Illegal parameter list: " + p.queryName() + " occurs twice.");
      }
      // the minimum and maximum may only use constants and previously defined parameters
      used = p.queryMinimum().queryParameters();
      if (!sofar.containsAll(used)) {
        throw new Error("Illegal parameter list: minimum of parameter " + p.queryName() + " is " +
          p.queryMinimum().toString() + ", which uses parameters which are not yet defined.");
      }
      used = p.queryMaximum().queryParameters();
      if (!sofar.containsAll(used)) {
        throw new Error("Illegal parameter list: maximum of parameter " + p.queryName() + " is " +
          p.queryMaximum().toString() + ", which uses parameters which are not yet defined.");
      }
      // the constraint may use the newly defined parameter as well
      sofar.add(p.queryName());
      used = p.queryRestriction().queryParameters();
      if (!sofar.containsAll(used)) {
        throw new Error("Illegal parameter list: restriction " + p.queryRestriction().toString() +
          " of " + p.queryName() + " uses parameters which have not yet been defined.");
      }
    }
  }

  /**
   * Creates the list consisting of the given parameters.
   * This also checks that all the parameters are actually legal (that is, no parameter is used
   * twice, and all parameters used in minima, maxima and restrictions are previously declared).
   * If this is not satisfied, an Error is thrown.
   */
  public ParameterList(ArrayList<Parameter> lst) {
    _lst = new ArrayList<Parameter>(lst);
    checkValidity();
  }

  /** Creates a list with a single parameter. */
  public ParameterList(Parameter p) {
    _lst = new ArrayList<Parameter>();
    _lst.add(p);
    checkValidity();
  }

  /** Creates a list with two parameters. */
  public ParameterList(Parameter p1, Parameter p2) {
    _lst = new ArrayList<Parameter>();
    _lst.add(p1);
    _lst.add(p2);
    checkValidity();
  }

  /** Creates a list with three parameters. */
  public ParameterList(Parameter p1, Parameter p2, Parameter p3) {
    _lst = new ArrayList<Parameter>();
    _lst.add(p1);
    _lst.add(p2);
    _lst.add(p3);
    checkValidity();
  }

  /** Creates the list with the given parameter appended to the given list. */
  public ParameterList(ParameterList plst, Parameter pnew) {
    _lst = new ArrayList<Parameter>(plst._lst);
    _lst.add(pnew);
    checkValidity();
  }

  public int size() { return _lst.size(); }
  public Parameter get(int i) { return _lst.get(i); }
}

