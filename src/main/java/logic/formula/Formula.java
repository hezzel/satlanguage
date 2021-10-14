package logic.formula;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import logic.parameter.Assignment;
import logic.parameter.Substitution;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * A logical formula which knows how to turn itself into an equivalent set of clauses.
 * Formulas may use parameters (in particular as part of parametrised variables), and can handle
 * instantiation or substitution of those parameters.
 */
public abstract class Formula {
  static final int ATOM        = 0;
  static final int JUNCTION    = 1;
  static final int IMPLICATION = 2;
  static final int OTHER       = 3;

  /**
   * It is the responsibility of the constructors for the various inheriting classes to update the
   * _usedParameters so as to contain all parameters in the formula.
   */
  protected TreeSet<String> _usedParameters;

  /**
   * Initialises _usedParameters and makes them contain the parameters of all children.
   * The children will not be modified.
   */
  protected Formula(ArrayList<Formula> children) {
    _usedParameters = new TreeSet<String>();
    for (int i = 0; i < children.size(); i++) {
      _usedParameters.addAll(children.get(i)._usedParameters);
    }
  }

  /** Initialises _usedParameters to an empty set. */
  protected Formula() {
    _usedParameters = new TreeSet<String>();
  }

  /** Initialises _usedParameters to the parameters occurring in the single child. */
  protected Formula(Formula child) {
    _usedParameters = new TreeSet<String>(child._usedParameters);
  }

  /** Initialises _usedParameters to the parameters occurring in the two children combined. */
  protected Formula(Formula child1, Formula child2) {
    _usedParameters = new TreeSet<String>(child1._usedParameters);
    _usedParameters.addAll(child2._usedParameters);
  }

  /** Initialises _usedParameters to the parameters occurring in the three children combined. */
  protected Formula(Formula child1, Formula child2, Formula child3) {
    _usedParameters = new TreeSet<String>(child1._usedParameters);
    _usedParameters.addAll(child2._usedParameters);
    _usedParameters.addAll(child3._usedParameters);
  }

  /** Returns false if the formula uses any parameters, otherwise true. */
  public boolean queryClosed() {
    return _usedParameters.size() == 0;
  }

  /** A Formula representing the negation of the present one. */
  public abstract Formula negate();
  
  /** If the present formula is atomic, this returns the corresponding Atom; otherwise null. */
  public Atom queryAtom() {
    return null;
  }

  /**
   * Given an assignment of parameters to PExpressions, this returns a formula equivalent to the
   * current one with occurrences of param replaced by ass[param].
   */
  protected abstract Formula substitute(Substitution subst);

  /**
   * Given a (full or partial) assignment of parameters to integers, this returns a formula
   * equivalent to the current one with occurrences of param replaced by ass[param].
   */
  public Formula instantiate(Assignment ass) {
    return substitute(new Substitution(ass));
  }

  /** 
   * Returns an Atom that is supposed to be equivalent to the given formula.  If the given formula
   * is already an atom, this just returns the formula; otherwise it creates a variable that
   * represents the given formula, and adds the requirements that it's equivalent.
   * Note: we assume here that variables of the form ⟦φ⟧ are ONLY created by this function, and
   * therefore the existence of such a formula implies that col *already contains* a set of clauses
   * exactly describing that ⟦φ⟧ <-> φ.  We also assume that the string representation of a formula
   * uniquely describes that formula.
   */
  protected Atom queryAtomFor(Formula formula, ClauseCollection col) {
    Atom ret = formula.queryAtom();
    if (ret != null) return ret;

    String varname = "⟦" + formula.toString() + "⟧";
    boolean exists = Variable.exists(varname);
    ret = new Atom(new Variable(varname), true);
    if (!exists) formula.addClausesDef(ret, col);
    return ret;
  }

  /**
   * This adds a set of clauses φ to prob such that, if x1...xn are the variables that occur in φ
   * but not in the present formula: this ↔ ∃x1...xn [∧φ].
   */
  public abstract void addClauses(ClauseCollection col);
  
  /**
   * This adds a set of clauses φ to prob such that, if x1...xn are the variables that occur in φ
   * but not in the present formula: (a → this) ↔ ∃x1...xn [∧φ].
   */
  public abstract void addClausesIfThisIsImpliedBy(Atom a, ClauseCollection col);

  /**
   * This adds a set of clauses φ to prob such that, if x1...xn are the variables that occur in φ
   * but not in the present formula: (this → a) ↔ ∃x1...xn [φ].
   */
  public abstract void addClausesIfThisImplies(Atom a, ClauseCollection col);

  /**
   * This adds a set of clauses φ to prob such that, if x1...xn are the variables that occur in φ
   * but not in the present formula: (a ↔ this) ↔ ∃x1...xn [∧φ].
   */
  public void addClausesDef(Atom a, ClauseCollection col) {
    addClausesIfThisIsImpliedBy(a, col);
    addClausesIfThisImplies(a, col);
  }

  /**
   * Used for printing: when printing something of a lower level, it is not needed to put brackets
   * around it.  Note that this should always return one of the constants defined in the interface.
   */
  public abstract int queryAssocLevel();
}

