package logic.parameter;

import java.util.ArrayList;

/**
 * A ParamTree is a tree that stores a value (of type T) for any valid combination of parameters.
 */
public class ParamTree<T> {
  ArrayList< ParamTree<T> > _children;
  String _parameterName;
  int _parameterMinimum;
  T _node;

  /** A ConstructorHelper should be passed to the constructor to generate the initial nodes. */
  public interface ConstructorHelper<T> {
    T generate(Assignment args);
  }

  /** Generates a tree for the given parameters. */
  public ParamTree(ParameterList params, ConstructorHelper<T> init) {
    construct(params, new Assignment(), init, 0);
  }

  /** Helper constructor for the recursive construction. */
  private ParamTree(ParameterList params, Assignment args, ConstructorHelper<T> init, int index) {
    construct(params, args, init, index);
  }

  /** Helper function which handles the constructor functionality. */
  private void construct(ParameterList params, Assignment args, ConstructorHelper<T> init,
                         int index) {
    // we're going to generate a node!
    if (index >= params.size()) {
      _parameterName = null;
      _children = null;
      _node = init.generate(args);
      return;
    }

    // we're generating a set of children.
    _node = null;
    _parameterName = params.get(index).queryName();
    _parameterMinimum = params.get(index).queryMinimum().evaluate(args);
    int max = params.get(index).queryMaximum().evaluate(args);
    PConstraint constr = params.get(index).queryRestriction();
    _children = new ArrayList< ParamTree<T> >();
    for (int i = _parameterMinimum; i <= max; i++) {
      args.put(_parameterName, i);
      // we only add a child if the parameter restriction is satisfied for the current assignment
      if (constr.evaluate(args)) {
        _children.add(new ParamTree<T>(params, args, init, index + 1));
      }
      else _children.add(null);
      args.remove(_parameterName);
    }
  }

  /**
   * Looks up the value corresponding to the given arguments, or null if there is none.
   * If not all parameters are defined that are used in the tree, then an Error is thrown.
   */
  public T lookup(Assignment args) {
    if (_parameterName == null) return _node;
    if (!args.defines(_parameterName)) {
      throw new Error("ParamTree::lookup, with undefined param " + _parameterName + ".");
    }
    int k = args.get(_parameterName);
    if (k < _parameterMinimum || k >= _parameterMinimum + _children.size()) return null;
    k -= _parameterMinimum;
    if (_children.get(k) == null) return null;
    return _children.get(k).lookup(args);
  }

  /** Sets the value for the given arguments, or throws an error if there is no such node. */
  public void set(Assignment args, T value) {
    if (_parameterName == null) {
      _node = value;
      return;
    }
    if (!args.defines(_parameterName)) {
      throw new Error("ParamTree::set, where param " + _parameterName + "is undefined.");
    }
    int k = args.get(_parameterName);
    if (k < _parameterMinimum || k >= _parameterMinimum + _children.size()) {
      throw new Error("ParamTree::set, where param " + _parameterName + " is given " +
        "illegal value " + k + " (range is " + _parameterMinimum + "--" +
        (_parameterMinimum + _children.size() - 1) + ")");
    }
    k -= _parameterMinimum;
    if (_children.get(k) == null) {
      throw new Error("ParamTree::set, where param " + _parameterName + " does not have a " +
        "child " + (k + _parameterMinimum) + ".");
    }
    _children.get(k).set(args, value);
  }
}

