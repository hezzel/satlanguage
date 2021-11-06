package logic.number.binary;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.sat.ClauseCollection;
import java.util.ArrayList;

/** The sum of two binary integers. */
public class BinaryPlus implements BinaryInteger {
  private BinaryInteger _left;
  private BinaryInteger _right;
  private ArrayList<Atom> _parts;
  private Atom _negativeBit;
  private Atom _truth;

  public BinaryPlus(BinaryInteger left, BinaryInteger right, Atom truth) {
    int max = left.queryMaximum() + right.queryMaximum();
    int min = left.queryMinimum() + right.queryMinimum();

    // store variables we already know
    _left = left;
    _right = right;
    _truth = truth;

    // create the variables
    _parts = new ArrayList<Atom>();
    for (int i = 0, j = 1; j <= max || -j > min; i++, j *= 2) {
      String name = "(" + _left.toString() + "⊞" + _right.toString() + ")⟨" + i + "⟩";
      _parts.add(new Atom(new Variable(name), true));
    }

    // create the bit for the negative check
    if (min >= 0) _negativeBit = truth.negate();
    else if (max < 0) _negativeBit = truth;
    else {
      String name = "(" + _left.toString() + "⊞" + _right.toString() + ")⟨-⟩";
      _negativeBit = new Atom(new Variable(name), true);
    }
  }

  public int queryMinimum() {
    return _left.queryMinimum() + _right.queryMinimum();
  }

  public int queryMaximum() {
    return _left.queryMaximum() + _right.queryMaximum();
  }

  public int length() {
    return _parts.size();
  }

  public Atom queryBit(int i) {
    if (i < _parts.size()) return _parts.get(i);
    return _negativeBit;
  }

  public Atom queryNegativeBit() {
    return _negativeBit;
  }

  private void makeC(Atom a, Atom b, Atom c, ClauseCollection col) {
    ArrayList<Atom> atoms = new ArrayList<Atom>();
    atoms.add(a);
    atoms.add(b);
    atoms.add(c);
    col.addClause(new Clause(atoms));
  }

  private void makeC(Atom a, Atom b, Atom c, Atom d, ClauseCollection col) {
    ArrayList<Atom> atoms = new ArrayList<Atom>();
    atoms.add(a);
    atoms.add(b);
    atoms.add(c);
    atoms.add(d);
    col.addClause(new Clause(atoms));
  }

  public void addWelldefinednessClauses(ClauseCollection col) {
    if (col.isInMemory("bplus " + toString())) return;
    col.addToMemory("bplus " + toString());
    _left.addWelldefinednessClauses(col);
    _right.addWelldefinednessClauses(col);

    Atom carry = _truth.negate();
    for (int i = 0; i < _parts.size(); i++) {
      Atom c1 = carry, c0 = carry.negate();
      Atom l1 = _left.queryBit(i), l0 = _left.queryBit(i).negate();
      Atom r1 = _right.queryBit(i), r0 = _right.queryBit(i).negate();
      Atom p1 = _parts.get(i), p0 = _parts.get(i).negate();
      // _parts[i] = carry XOR left[i] XOR right[i]
      makeC(c0, l0, r0, p1, col);    // carry ∧ left[i] ∧ right[i] → p1
      makeC(c0, l0, r1, p0, col);    // carry ∧ left[i] ∧ ¬right[i] → ¬p1
      makeC(c0, l1, r0, p0, col);    // carry ∧ ¬left[i] ∧ right[i] → ¬p1
      makeC(c0, l1, r1, p1, col);    // carry ∧ ¬left[i] ∧ ¬right[i] → p1
      makeC(c1, l0, r0, p0, col);    // ¬carry ∧ left[i] ∧ right[i] → ¬p1
      makeC(c1, l0, r1, p1, col);    // ¬carry ∧ left[i] ∧ ¬right[i] → p1
      makeC(c1, l1, r0, p1, col);    // ¬carry ∧ ¬left[i] ∧ right[i] → p1
      makeC(c1, l1, r1, p0, col);    // ¬carry ∧ ¬left[i] ∧ ¬right[i] → ¬p1
      // newcarry = atleasttwo(carry, left[i], right[i])
      Atom nc1;
      if (i == _parts.size() - 1 && (queryMinimum() >= 0 || queryMaximum() < 0)) return;
      nc1 = new Atom(new Variable(toString() + "-carry⟨" + i + "⟩"), true);
      Atom nc0 = nc1.negate();
      makeC(c0, l0, nc1, col); // carry ∧ left[i] → newcarry
      makeC(c1, l1, nc0, col); // ¬carry ∧ ¬left[i] → ¬newcarry
      makeC(l0, r0, nc1, col); // left[i] ∧ right[i] → newcarry
      makeC(l1, r1, nc0, col); // ¬left[i] ∧ ¬right[i] → ¬newcarry
      makeC(c0, r0, nc1, col); // carry ∧ right[i] → newcarry
      makeC(c1, r1, nc0, col); // ¬carry ∧ ¬right[i] → ¬newcarry
      // replace carry by newcarry to carry on!
      carry = nc1;
    }
    Atom l1 = _left.queryNegativeBit(), l0 = l1.negate();
    Atom r1 = _right.queryNegativeBit(), r0 = r1.negate();
    Atom c1 = carry, c0 = carry.negate();
    Atom n1 = queryNegativeBit(), n0 = n1.negate();
    makeC(l1, r1, n0, col);      // l ≥ 0 ∧ r ≥ 0 → l+r ≥ 0
    makeC(l0, r0, n1, col);      // l < 0 ∧ r < 0 → l+r < 0
    makeC(l1, r0, c1, n1, col);  // l ≥ 0 ∧ r < 0 ∧ no carry-out → l+r < 0
    makeC(l1, r0, c0, n0, col);  // l ≥ 0 ∧ r < 0 ∧ carry-out → l+r ≥ 0
    makeC(l0, r1, c1, n1, col);  // l < 0 ∧ r ≥ 0 ∧ no carry-out → l+r < 0
    makeC(l0, r1, c0, n0, col);  // l < 0 ∧ r ≥ 0 ∧ carry-out → l+r ≥ 0
  }

  public String toString() {
    return "(" + _left.toString() + "⊞" + _right.toString() + ")";
  }
}

