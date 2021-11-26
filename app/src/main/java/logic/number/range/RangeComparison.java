package logic.number.range;

import logic.sat.Atom;
import logic.sat.Clause;
import java.util.ArrayList;

/**
 * This class provides static functions which calculate the clauses involved in comparing two range
 * integers to each other.
 */
public class RangeComparison {
  public static ArrayList<Clause> generateGeqClauses(RangeInteger a, RangeInteger b) {
  /* Suppose a ∈ {amin..amax} and b ∈ {bmin..bmax}.  To see that a ≥ b, we must clearly have:
   * - amax ≥ bmin
   * - if bmin > amin, then: a ≥ bmin
   * - if bmax > amax, then: NOT b ≥ amax + 1
   * - ∀ i ∈ {MAX(amin+1,bmin+1) .. MIN(amax,bmax)}. b ≥ i → a ≥ i.
   * The first three items should all hold by transitivity: if a ≥ b, then amax ≥ a ≥ b ≥ bmin
   * and certainly not amax ≥ a ≥ b ≥ amax + 1.  The last item should hold because for ALL
   * integers i we should have that b ≥ i → a ≥ i, again by transitivity of ≥.
   *
   * The above four requirements are also sufficient to show that a ≥ b.  To see this, observe
   * that what we really need to prove is that b ≥ i → a ≥ i for all integers.  Consider i:
   * - If i ≤ amin, then a ≥ i certainly holds, so there is nothing to show.
   * - If amin < i ≤ bmin, then by the second requirement a ≥ bmin, so certainly a ≥ i.
   * - If i > bmax, then b ≥ i does not hold, so there is nothing to show.
   * - If bmax ≥ i > amax, then by the third requirement b < amax+1, so certainly b < i, so b ≥ i
   *   does not hold, and there is nothing to show.
   * - In all other cases, we require b ≥ i → a ≥ i
   */
    ArrayList<Clause> ret = new ArrayList<Clause>();
    if (a.queryMaximum() < b.queryMinimum()) {  // add a ≥ bmin, which is exactly FALSE
      ret.add(new Clause(a.queryGeqAtom(b.queryMinimum())));
      return ret;
    }   
    if (b.queryMinimum() > a.queryMinimum()) {  // add a ≥ bmin
      ret.add(new Clause(a.queryGeqAtom(b.queryMinimum())));
    }   
    if (b.queryMaximum() > a.queryMaximum()) {  // add b < amax + 1
      ret.add(new Clause(b.queryGeqAtom(a.queryMaximum()+1).negate()));
    }   
    int min = (b.queryMinimum() > a.queryMinimum() ? b.queryMinimum() : a.queryMinimum()) + 1;
    int max = b.queryMaximum() < a.queryMaximum() ? b.queryMaximum() : a.queryMaximum();
    for (int i = min; i <= max; i++) {
      ret.add(new Clause(b.queryGeqAtom(i).negate(), a.queryGeqAtom(i)));
    }   
    return ret;
  }

  /** Returns the clauses that imply a < b, so b ≥ a+1. */
  public static ArrayList<Clause> generateSmallerClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    if (b.queryMaximum() < a.queryMinimum() + 1) {  // add b ≥ amax+1, which is exactly FALSE
      ret.add(new Clause(b.queryGeqAtom(b.queryMaximum()+1)));
      return ret;
    }
    if (a.queryMinimum() + 1 > b.queryMinimum()) {  // add b ≥ (a+1)min = amin+1
      ret.add(new Clause(b.queryGeqAtom(a.queryMinimum()+1)));
    }
    if (a.queryMaximum() + 1 > b.queryMaximum()) {  // add a+1 < bmax+1, so a < bmax
      ret.add(new Clause(a.queryGeqAtom(b.queryMaximum()).negate()));
    }
    // ∀ i ∈ {MAX(bmin+1,amin+2) .. MIN(bmax,amax+1)}. a+1 ≥ i → b ≥ i
    int min = (b.queryMinimum() > a.queryMinimum() ? b.queryMinimum() : a.queryMinimum()+1) + 1;
    int max = b.queryMaximum() <= a.queryMaximum() ? b.queryMaximum() : a.queryMaximum() + 1;
    for (int i = min; i <= max; i++) {  // a ≥ i-1 → b ≥ i
      ret.add(new Clause(a.queryGeqAtom(i-1).negate(), b.queryGeqAtom(i)));
    }
    return ret;
  }

  /** Returns the clauses that imply a = b. */
  public static ArrayList<Clause> generateEqualClauses(RangeInteger a, RangeInteger b) {
    // a = b <-> a ≥ b and b ≥ a
    ArrayList<Clause> ret = generateGeqClauses(a, b);
    ret.addAll(generateGeqClauses(b, a));
    return ret;
  }

  /** Returns the clauses that imply a ≠ b. */
  public static ArrayList<Clause> generateNeqClauses(RangeInteger a, RangeInteger b) {
    ArrayList<Clause> ret = new ArrayList<Clause>();
    // a != b <-> for all i: if a = i then b != i
    //        <-> for all i ∈ {amin..amax}: if a = i then b != i (as otherwise a = i does not hold)
    for (int i = a.queryMinimum(); i <= a.queryMaximum(); i++) {
      // if b cannot be equal to a, then we don't need clauses to get b != i
      if (i < b.queryMinimum() || i > b.queryMaximum()) continue;
      // a = i → b ≠ i <--> a ≥ i ∧ a < i+1 → b < i ∨ b ≥ i+1
      //               <--> ¬(a≥i) ∨ a≥i+1 ∨ ¬b≥i ∨ b≥i+1
      // (and don't add atoms that evaluate to false anyway)
      ArrayList<Atom> parts = new ArrayList<Atom>();
      if (i > a.queryMinimum()) parts.add(a.queryGeqAtom(i).negate());
      if (i < a.queryMaximum()) parts.add(a.queryGeqAtom(i+1));
      if (i > b.queryMinimum()) parts.add(b.queryGeqAtom(i).negate());
      if (i < b.queryMaximum()) parts.add(b.queryGeqAtom(i+1));
      if (parts.size() == 0) parts.add(a.queryGeqAtom(a.queryMaximum()+1));
      ret.add(new Clause(parts));
    }   
    return ret;
  }
}

