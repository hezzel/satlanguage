package logic.formula;

import logic.sat.Atom;
import logic.sat.ClauseCollection;

/** A formula that is just syntactic sugar for another formula (of lower sugar level). */
public abstract class SugarFormula extends Formula {
  /**
   * The translate into a (more basic) formula that will be used for the addClauses methods.
   * Note that this formula may NOT introduce new variables.
   */
  public abstract Formula translate();

  protected SugarFormula() {
    super();
  }

  protected SugarFormula(Formula child) {
    super(child);
  }

  protected SugarFormula(Formula child1, Formula child2) {
    super(child1, child2);
  }

  public void addClauses(ClauseCollection col) {
    translate().addClauses(col);
  }

  public void addClausesDef(Atom x, ClauseCollection col) {
    translate().addClausesDef(x, col);
  }

  public void addClausesIfThisIsImpliedBy(Atom x, ClauseCollection col) {
    translate().addClausesIfThisIsImpliedBy(x, col);
  }

  public void addClausesIfThisImplies(Atom x, ClauseCollection col) {
    translate().addClausesIfThisImplies(x, col);
  }
}

