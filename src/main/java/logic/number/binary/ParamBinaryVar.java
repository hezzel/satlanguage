package logic.number.binary;

import logic.sat.Atom;
import logic.sat.ClauseCollection;
import logic.parameter.*;

/**
 * A ParamBinaryVar -- or Parametrised Binary IntegerVariable -- is a set of BinaryVariables,
 * indexed with one or more named integers parameters which are allowed to occupy a fixed range.
 */
public class ParamBinaryVar extends ParametrisedObject<BinaryVariable> {
  private String _declarationDescription;

  /**
   * Creates a parametrised range variable with a given list of parameters.
   * The truth atom should be such that it represents "true" (so should be forced to hold in the
   * SAT requirements).
   * All range variables have length bits, plus a "negative" bit if neg is true (indicating that
   * the variables are allowed to be negative).
   */
  public ParamBinaryVar(String name, ParameterList params, int length, boolean neg, Atom truth) {
    super(name, params, makeHelper(name, length, neg, params, truth));
    if (neg) _declarationDescription = "Int" + length;
    else _declarationDescription = "Nat" + length;
  }

  /**
   * Helper function for the first constructor: generates the helper object which the ParamTree
   * requires to construct itself.
   */
  private static ParamTree.ConstructorHelper<BinaryVariable> makeHelper(String name,
                                                                        int length,
                                                                        boolean allowNegative,
                                                                        ParameterList params,
                                                                        Atom truth) {
    return new ParamTree.ConstructorHelper<BinaryVariable>() {
      /** Creates a variable for a suitable, complete combination of parameters. */
      public BinaryVariable generate(Assignment args) {
        String varname = queryObjectName(name, params, new Substitution(args));
        return new BinaryVariable(varname, length, allowNegative, truth);
      }
    };
  }

  /**
   * Creates a parametrised range variable with a given list of parameters, and which is expected
   * to be in the given range (min--max, which may depend on the parameters).
   * The truth atom should be such that it represents "true" (so should be forced to hold in the
   * SAT requirements).
   */
  public ParamBinaryVar(String name, ParameterList params, PExpression min, PExpression max,
                        Atom truth) {
    super(name, params, makeHelper(name, min, max, params, truth));
    _declarationDescription = "Int? ∈ { " + min.toString() + ".." + max.toString() + " }";
  }

  /** Helper function for the second constructor. */
  private static ParamTree.ConstructorHelper<BinaryVariable> makeHelper(String name,
                                                                        PExpression min,
                                                                        PExpression max,
                                                                        ParameterList params,
                                                                        Atom truth) {
    return new ParamTree.ConstructorHelper<BinaryVariable>() {
      /** Creates a variable for a suitable, complete combination of parameters. */
      public BinaryVariable generate(Assignment args) {
        String varname = queryObjectName(name, params, new Substitution(args));
        int minimum = min.evaluate(args);
        int maximum = max.evaluate(args);
        return new BinaryVariable(varname, minimum, maximum, truth);
      }
    };
  }

  public String queryDeclarationDescription() {
    return _declarationDescription;
  }

  /**
   * This returns the binary integer corresponding to the given indexes.
   * If one of the indexes is out of range (so this does not represent a proper variable), or not
   * all the required parameters are provided, then an Error is thrown instead.
   */
  public BinaryVariable queryVar(Assignment values) {
    BinaryVariable ret = queryObject(values);
    if (ret == null) {
      throw new Error("Instantiation of binary integer variable " + toString() + " with " +
        values + " violates restrictions: parameter values are out of range.");
    }
    return ret;
  }

  private void addWelldefinednessClauses(int i, Assignment sigma, ClauseCollection col) {
    if (i >= _parameters.size()) queryVar(sigma).addWelldefinednessClauses(col);
    else {
      int min = _parameters.get(i).queryMinimum().evaluate(sigma);
      int max = _parameters.get(i).queryMaximum().evaluate(sigma);
      for (int k = min; k <= max; k++) {
        sigma.put(_parameters.get(i).queryName(), k);
        if (_parameters.get(i).queryRestriction().evaluate(sigma)) {
          addWelldefinednessClauses(i+1, sigma, col);
        }
        sigma.remove(_parameters.get(i).queryName());
      }
    }
  }

  /** Adds clauses to col indicating that all instances are well-defined variables. */
  public void addWelldefinednessClauses(ClauseCollection col) {
    addWelldefinednessClauses(0, new Assignment(), col);
  }
}

