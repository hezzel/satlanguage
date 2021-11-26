package logic.formula;

import logic.parameter.Parameter;
import java.util.ArrayList;

/**
 * A Forall formula is a bounded quantification, so essentially syntactic sugar for a conjunction.
 * It has the form ∀ i ∈ {lower..upper} with cond. formula.
 */
public class Forall extends QuantifierFormula {
  /** Creates the formula ∀ param.formula. */
  public Forall(Parameter param, Formula formula) {
    super(param, formula);
  }

  /** Required by the Quantifier inherit: calls the constructor. */
  protected Forall create(Parameter param, Formula formula) {
    return new Forall(param, formula);
  }

  /** Required by the Quantifier inherit: returns the name of the quantifier. */
  protected String queryQuantifierName() {
    return "∀";
  }

  /** Returns the negation of this formula, which is exactly ∃ param.¬formula */
  public Formula negate() {
    return new Exists(_param, _formula.negate());
  }

  /** Translates the Forall into the corresponding conjunction, provided that we are closed. */
  public Formula translate() {
    return new And(enumerateParts());
  }
}

