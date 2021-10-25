package logic.range;

import logic.sat.Variable;
import logic.parameter.PExpression;
import logic.parameter.Assignment;
import logic.parameter.Substitution;
import java.util.Set;

/** A QuantifiedRangePlus represents the addition of two QuantifiedRangeIntegers. */
public class QuantifiedRangePlus implements QuantifiedRangeInteger {
  private QuantifiedRangeInteger _left;
  private QuantifiedRangeInteger _right;

  public QuantifiedRangePlus(QuantifiedRangeInteger l, QuantifiedRangeInteger r) {
    _left = l;
    _right = r;
  }

  public Set<String> queryParameters() {
    Set<String> ret = _left.queryParameters();
    ret.addAll(_right.queryParameters());
    return ret;
  }

  public boolean queryClosed() {
    return _left.queryClosed() && _right.queryClosed();
  }

  public QuantifiedRangePlus substitute(Substitution subst) {
    return new QuantifiedRangePlus(_left.substitute(subst), _right.substitute(subst));
  }

  public RangeInteger instantiate(Assignment ass) {
    RangeInteger l = _left.instantiate(ass);
    RangeInteger r = _right.instantiate(ass);
    // both are just constants!
    if (l.queryMinimum() == l.queryMaximum() && r.queryMinimum() == r.queryMaximum()) {
      return new RangeConstant(l.queryMinimum() + r.queryMinimum(),
                               l.queryGeqVariable(l.queryMaximum() + 1),  // false
                               l.queryGeqVariable(l.queryMinimum())       // true
                              );
    }
    // one is a constant but the other isn't => turn it into a shift!
    if (l.queryMinimum() == l.queryMaximum()) {
      if (l.queryMinimum() == 0) return r;
      else return new RangeShift(r, l.queryMinimum());
    }
    if (r.queryMinimum() == r.queryMaximum()) {
      if (r.queryMinimum() == 0) return l;
      else return new RangeShift(l, r.queryMinimum());
    }
    // neither are constants -- we will need RangePlus
    return new RangePlus(l, r);
  }

  public String toString() {
    return _left + " ⊕ " + _right;
  }
}

