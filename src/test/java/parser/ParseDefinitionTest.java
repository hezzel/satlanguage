package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Function;
import language.parser.InputReader;
import language.parser.ParserException;
import language.parser.DefinitionData;

public class ParseDefinitionTest {
  private DefinitionData defs() {
    return new DefinitionData();
  }

  @Test
  public void testReadBasicDefinition() throws ParserException {
    int k = InputReader.readMacroFromString("define A 3", defs());
    assertTrue(k == 3);
  }

  @Test
  public void testReadCalculationInDefinition() throws ParserException {
    int k = InputReader.readMacroFromString("define A 3-(5%2)", defs());
    assertTrue(k == 2);
  }

  @Test
  public void testUserPreviousDefinition() throws ParserException {
    DefinitionData dd = defs();
    int size = InputReader.readMacroFromString("define SIZE 3", dd);
    int square = InputReader.readMacroFromString("define SQUARE SIZE * SIZE", dd);
    assertTrue(square == 9);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testUseUndefinedDefinitions() throws ParserException {
    InputReader.readMacroFromString("define SQUARE SIZE * SIZE", defs());
  }

  @Test(expected = language.parser.ParserException.class)
  public void testUseParameters() throws ParserException {
    InputReader.readMacroFromString("define SUM 9 + i", defs());
  }

  @Test
  public void testReadPureMapping() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping TEST { 1 : 2 ; 3 : 4 }", defs());
    assertTrue(f.lookup(1) == 2);
    assertTrue(f.lookup(3) == 4);
  }

  @Test public void testMappingInDefsAfterREading() throws ParserException {
    DefinitionData dd = defs();
    Function f = InputReader.readMappingFromString("mapping TEST { 1 : 2 ; 3 : 4 }", dd);
    assertTrue(dd.getMapping("TEST") == f);
  }

  @Test
  public void testReadMappingWithCalculation() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping TEST { 9 : 1+5 / 3}", defs());
    assertTrue(f.lookup(9) == 2);
  }

  @Test
  public void testReadPureFunction() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping DING { i : i * 2 - 1 }", defs());
    assertTrue(f.lookup(7) == 13);
  }

  @Test
  public void testReadFullMapping() throws ParserException {
    Function f = InputReader.readMappingFromString(
      "mapping HELLO { 1 : 7 ; 2 : -4 ; -1 : 3 ; i : i }", defs());
    assertTrue(f.lookup(1) == 7);
    assertTrue(f.lookup(2) == -4);
    assertTrue(f.lookup(-1) == 3);
    assertTrue(f.lookup(0) == 0);
    assertTrue(f.lookup(42) == 42);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadMappingWithDoubleFunction() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping D { i : i * 2 - 1 ; j : j }", defs());
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadMappingWithMisguidedParameter() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping BOOP { 1 : i ; i : 2 }", defs());
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadMappingWithTooManyParams() throws ParserException {
    Function f = InputReader.readMappingFromString("mapping BOOP { i : i + j }", defs());
  }

  @Test
  public void testReadMappingWithDefinitionAsKey() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    Function f = InputReader.readMappingFromString("mapping X {A : 18 ; -2 : 4 }", dd);
    assertTrue(f.lookup(3) == 18);
  }

  @Test
  public void testReadMappingWithDefinitionAsValue() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    Function f = InputReader.readMappingFromString("mapping X { 7 : A ; 8 : A + 2 }", dd);
    assertTrue(f.lookup(7) == 3);
    assertTrue(f.lookup(8) == 5);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDoubleMappingDefinition() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMappingFromString("mapping ABC {1 : 2 ; 3 : 4 }", dd);
    InputReader.readMappingFromString("mapping ABC {1 : 2 ; 3 : 4 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMappingPreviouslyDefinedAsMacro() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    InputReader.readMappingFromString("mapping A { 1 : 2 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMacroPreviouslyDefinedAsMapping() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMappingFromString("mapping A { 1 : 2 }", dd);
    InputReader.readMacroFromString("define A 3", dd);
  }
}

