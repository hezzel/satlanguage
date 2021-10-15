package logic.formula;

import logic.parameter.Parameter;

/**
 * An Exists formula is a bounded quantification, so essentially syntactic sugar for a disjunction.
 * It has the form ∃ i ∈ {lower..upper} with cond. formula.
 */
public class Exists extends QuantifierFormula {
  /** Creates the formula ∃ param.formula. */
  public Exists(Parameter param, Formula formula) {
    super(param, formula);
  }

  /** Required by the Quantifier inherit: calls the constructor. */
  protected Exists create(Parameter param, Formula formula) {
    return new Exists(param, formula);
  }

  /** Required by the Quantifier inherit: returns the name of the quantifier. */
  protected String queryQuantifierName() {
    return "∃";
  }

  /** Returns the negation of this formula, which is exactly ∀ param.¬formula */
  public Formula negate() {
    return new Forall(_param, _formula.negate());
  }

  /** Translates the Exists into the corresponding disjunction, provided that we are closed. */
  public Formula translate() {
    return new Or(enumerateParts());
  }
}

