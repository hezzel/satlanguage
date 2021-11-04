import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.binary.BinaryConstant;

public class BinaryConstantTest {
  @Test
  public void testPositiveConstant() {
    Atom tru = new Atom(new Variable("TRUE"), true);
    Atom fal = tru.negate();
    BinaryConstant b = new BinaryConstant(83, tru);
    // 1010011
    assertTrue(b.length() == 7);
    assertTrue(b.queryBit(0).equals(tru));
    assertTrue(b.queryBit(1).equals(tru));
    assertTrue(b.queryBit(2).equals(fal));
    assertTrue(b.queryBit(3).equals(fal));
    assertTrue(b.queryBit(4).equals(tru));
    assertTrue(b.queryBit(5).equals(fal));
    assertTrue(b.queryBit(6).equals(tru));
    assertTrue(b.queryBit(7).equals(fal));
    assertTrue(b.queryBit(8).equals(fal));
    assertTrue(b.queryNegativeBit().equals(fal));
  }

  @Test
  public void testZero() {
    Atom tru = new Atom(new Variable("TRUE"), true);
    Atom fal = tru.negate();
    BinaryConstant b = new BinaryConstant(0, tru);
    assertTrue(b.length() == 0);
    assertTrue(b.queryBit(0).equals(fal));
    assertTrue(b.queryNegativeBit().equals(fal));
  }

  @Test
  public void testNegativeConstant() {
    Atom tru = new Atom(new Variable("TRUE"), true);
    Atom fal = tru.negate();
    BinaryConstant b = new BinaryConstant(-70, tru);
    // ...1111110111010
    assertTrue(b.length() == 7);
    assertTrue(b.queryBit(0).equals(fal));
    assertTrue(b.queryBit(1).equals(tru));
    assertTrue(b.queryBit(2).equals(fal));
    assertTrue(b.queryBit(3).equals(tru));
    assertTrue(b.queryBit(4).equals(tru));
    assertTrue(b.queryBit(5).equals(tru));
    assertTrue(b.queryBit(6).equals(fal));
    assertTrue(b.queryBit(7).equals(tru));
    assertTrue(b.queryBit(8).equals(tru));
    assertTrue(b.queryNegativeBit().equals(tru));
  }
}

