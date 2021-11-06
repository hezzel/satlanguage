package logic.range;

import logic.sat.*;
import logic.parameter.Assignment;

import java.util.List;
import java.util.TreeMap;

/**
 * An integer expression of the form a1 +...+an, but with bounds: if the true sum is bigger than
 * the given maximum, then the value of the expresison is set to the maximum; and similar if the
 * true sum is smaller than the given minimum.
 */
public class RangeSum extends RangePlus {
  String _tostring;

  private static RangeInteger firstHalf(List<RangeInteger> components, Variable truevar) {
    if (components.size() == 0) return new RangeConstant(0, truevar);
    if (components.size() <= 2) return components.get(0);
    return new RangeSum(components.subList(0, (components.size()+1)/2), truevar);
  }

  private static RangeInteger secondHalf(List<RangeInteger> components, Variable truevar) {
    if (components.size() <= 1) return new RangeConstant(0, truevar);
    if (components.size() <= 3) return components.get(components.size()-1);
    return new RangeSum(components.subList((components.size()+1)/2, components.size()), truevar);
  }

  public RangeSum(List<RangeInteger> components, Variable truevar) {
    super(firstHalf(components, truevar), secondHalf(components, truevar));
    _tostring = "bsum(" + queryMinimum() + ", " + queryMaximum() + ", ";
    for (int i = 0; i < components.size(); i++) {
      if (i > 0) _tostring += " ⊕ ";
      _tostring += components.toString();
    }
  }

  public String toString() {
    return _tostring;
  }
}

