package logic.number.binary;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import java.util.ArrayList;

/**
 * This class provides static functions which calculate the clauses involved in comparing two
 * binary integers to each other.
 */
public class BinaryComparison {
  public static ArrayList<Clause> generateGeqOrGreaterClauses(BinaryInteger a, BinaryInteger b,
                                                              boolean equalAllowed) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    int length = a.length() > b.length() ? a.length() : b.length();
    String symbol = equalAllowed ? "≥" : ">";

    // if a is positive, then a ≥ b holds if b is negative OR a⟨0..len⟩ ≥ b⟨0..len⟩
    // if a is negative, then a ≥ b holds if b is negative AND a⟨0..len⟩ ≥ b⟨0..len⟩
    Atom xx = new Atom(new Variable(a.toString() + "⟨0.." + length + "⟩" + symbol +
                                    b.toString() + "⟨0.." + length + "⟩"), true);
    Atom aneg = a.queryNegativeBit(), apos = aneg.negate();
    Atom bneg = b.queryNegativeBit();
    ret.add(new Clause(aneg, bneg, xx));  // a ≥ 0 → b < 0 ∨ a⟨0..len⟩ ≥ b⟨0..len⟩
    ret.add(new Clause(apos, bneg));      // a < 0 → b < 0
    ret.add(new Clause(apos, xx));        // a < 0 → a⟨0..len⟩ ≥ b⟨0..len⟩
    
    // a⟨0..i⟩ ≥ b⟨0..i⟩ <-> a⟨i⟩ > b⟨i⟩ ∨ (a⟨i⟩ = b⟨i⟩ ∧ a⟨0..i-1⟩ ≥ b⟨0..i-1⟩)
    //                   <-> if a⟨i⟩ then ¬b⟨i⟩ ∨ a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
    //                               else ¬b⟨i⟩ ∧ a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
    for (int i = length-1; i > 0; i--) {
      // a⟨0..i⟩ ≥ b⟨0..i⟩ holds only if:
      //   if a⟨i⟩ = 1, then b⟨i⟩ = 0 OR a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
      //                else b⟨i⟩ = 0 AND a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
      Atom yy = new Atom(new Variable(a.toString() + "⟨0.." + (i-1) + "⟩" + symbol +
                                      b.toString() + "⟨0.." + (i-1) + "⟩"), true);
      Atom ai = a.queryBit(i);
      Atom bineg = b.queryBit(i).negate();
      
      // a⟨0..i⟩ ≥ b⟨0..i⟩ ∧ a⟨i⟩ → ¬b⟨i⟩ ∨ a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
      ret.add(new Clause(xx.negate(), new Clause(ai.negate(), bineg, yy)));
      // a⟨0..i⟩ ≥ b⟨0..i⟩ ∧ ¬a⟨i⟩ → ¬b⟨i⟩ ∧ a⟨0..i-1⟩ ≥ b⟨0..i-1⟩
      ret.add(new Clause(xx.negate(), ai, bineg));
      ret.add(new Clause(xx.negate(), ai, yy));
      
      xx = yy;
    }

    // a⟨0..0⟩ ≥ b⟨0..0⟩ only if: a⟨0⟩ ∨ ¬b⟨0⟩
    if (equalAllowed) {
      ret.add(new Clause(xx.negate(), a.queryBit(0), b.queryBit(0).negate()));
    }
    // a⟨0..0⟩ > b⟨0..0⟩ only if: a⟨0⟩ ∨ ¬b⟨0⟩
    else {
      ret.add(new Clause(xx.negate(), a.queryBit(0)));
      ret.add(new Clause(xx.negate(), b.queryBit(0).negate()));
    }

    return ret;
  }

  public static ArrayList<Clause> generateGeqClauses(BinaryInteger a, BinaryInteger b) {
    return generateGeqOrGreaterClauses(a, b, true);
  }

  /** Returns the clauses that imply a < b, so b ≥ a+1. */
  public static ArrayList<Clause> generateSmallerClauses(BinaryInteger a, BinaryInteger b) {
    return generateGeqOrGreaterClauses(b, a, false);
  }

  /** Returns the clauses that imply a = b. */
  public static ArrayList<Clause> generateEqualClauses(BinaryInteger a, BinaryInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    int length = a.length() > b.length() ? a.length() : b.length();
    // a negative <-> b negative
    ret.add(new Clause(a.queryNegativeBit().negate(), b.queryNegativeBit()));
    ret.add(new Clause(a.queryNegativeBit(), b.queryNegativeBit().negate()));
    for (int i = length-1; i >= 0; i--) {
      // a⟨i⟩ <-> b⟨i⟩
      ret.add(new Clause(a.queryBit(i).negate(), b.queryBit(i)));
      ret.add(new Clause(a.queryBit(i), b.queryBit(i).negate()));
    }
    return ret;
  }

  /** Returns the clauses that imply a ≠ b. */
  public static ArrayList<Clause> generateNeqClauses(BinaryInteger a, BinaryInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    int length = a.length() > b.length() ? a.length() : b.length();
    ArrayList<Atom> someunequal = new ArrayList<Atom>();

    // a⟨-⟩≠b⟨-⟩ -> a⟨-⟩ ∧ ¬b⟨-⟩ OR ¬a⟨-⟩ ∧ b⟨-⟩
    Atom xx = new Atom(new Variable(a.queryNegativeBit().toString() + "≠" +
                                    b.queryNegativeBit().toString()), true);
    ret.add(new Clause(xx.negate(), a.queryNegativeBit().negate(), b.queryNegativeBit().negate()));
    ret.add(new Clause(xx.negate(), a.queryNegativeBit(), b.queryNegativeBit()));
    someunequal.add(xx);

    for (int i = length-1; i >= 0; i--) {
      // a⟨i⟩≠b⟨i⟩ -> a⟨i⟩ ∧ ¬b⟨i⟩ OR ¬a⟨i⟩ ∧ b⟨i⟩
      xx = new Atom(new Variable(a.queryBit(i).toString() + "≠" +
                                 b.queryBit(i).toString()), true);
      ret.add(new Clause(xx.negate(), a.queryBit(i).negate(), b.queryBit(i).negate()));
      ret.add(new Clause(xx.negate(), a.queryBit(i), b.queryBit(i)));
      someunequal.add(xx);
    }

    // at least one of the bit matches is unequal
    ret.add(new Clause(someunequal));
    return ret;
  }
}

