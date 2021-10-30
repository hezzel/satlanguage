package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Function;
import language.parser.InputReader;
import language.parser.ParserException;
import language.parser.DefinitionData;

import java.util.ArrayList;

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
    Function f = InputReader.readFunctionFromString("function TEST(i) { 1 ⇒ 2 ; 3 ⇒ 4 }", defs());
    assertTrue(f.lookup(1) == 2);
    assertTrue(f.lookup(3) == 4);
  }

  @Test
  public void testReadPureMappingWithTwoArguments() throws ParserException {
    Function f = InputReader.readFunctionFromString(
      "function TEST(i,j) { (1,1) ⇒ 2 ; (1,3) ⇒ 4 }", defs());
    assertTrue(f.toString().equals("TEST(i,j) { (1,1) ⇒ 2 ; (1,3) ⇒ 4 }"));
  }

  @Test
  public void testMappingInDefsAfterReading() throws ParserException {
    DefinitionData dd = defs();
    Function f = InputReader.readFunctionFromString("function TEST(i) { 1 ⇒ 2 ; 3 ⇒ 4 }", dd);
    assertTrue(dd.getFunction("TEST") == f);
  }

  @Test
  public void testReadMappingWithCalculation() throws ParserException {
    Function f = InputReader.readFunctionFromString("function TEST(i) { 9 ⇒ 1+5 / 3}", defs());
    assertTrue(f.lookup(9) == 2);
  }

  @Test
  public void testReadPureFunction() throws ParserException {
    Function f = InputReader.readFunctionFromString(
      "function DING(i,j) { (_,_) ⇒ i * j - 1 }", defs());
    ArrayList<Integer> arr = new ArrayList<Integer>();
    arr.add(7);
    arr.add(2);
    assertTrue(f.lookup(arr) == 13);
  }

  @Test
  public void testReadLongUnaryFunction() throws ParserException {
    Function f = InputReader.readFunctionFromString(
      "function HELLO(i) { 1 => 7 ; 2 => -4 ; -1 => 3 ; _ => i }", defs());
    assertTrue(f.lookup(1) == 7);
    assertTrue(f.lookup(2) == -4);
    assertTrue(f.lookup(-1) == 3);
    assertTrue(f.lookup(0) == 0);
    assertTrue(f.lookup(42) == 42);
  }
  
  @Test
  public void testCatchAll() throws ParserException {
    Function f = InputReader.readFunctionFromString(
      "function HELLO(i,j) { _ ⇒ 9 ; _ ⇒ 10 }", defs());
    ArrayList<Integer> arr = new ArrayList<Integer>();
    arr.add(5);
    arr.add(7);
    assertTrue(f.lookup(arr) == 9);
  }

  @Test
  public void testParameterMeaning() throws ParserException {
    Function f = InputReader.readFunctionFromString("function A(i) { 1 ⇒ i ; _ => 7 }", defs());
    assertTrue(f.lookup(1) == 1);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadMappingWithUndefinedParameter() throws ParserException {
    Function f = InputReader.readFunctionFromString("function BOOP(j) { 1 ⇒ i ; _ ⇒ 2 }", defs());
  }

  @Test
  public void testReadMappingWithDefinitionAsKey() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    Function f = InputReader.readFunctionFromString("function X(i) {A ⇒ 18 ; -2 ⇒ 4 }", dd);
    assertTrue(f.lookup(3) == 18);
  }

  @Test
  public void testReadMappingWithDefinitionAsValue() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    Function f = InputReader.readFunctionFromString("function X(i) { 7 ⇒ A ; 8 ⇒ A + 2 }", dd);
    assertTrue(f.lookup(7) == 3);
    assertTrue(f.lookup(8) == 5);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDoubleMappingDefinition() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readFunctionFromString("function ABC(i) {1 ⇒ 2 ; 3 ⇒ 4 }", dd);
    InputReader.readFunctionFromString("function ABC(j) {1 ⇒ 2 ; 3 ⇒ 4 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMappingPreviouslyDefinedAsMacro() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readMacroFromString("define A 3", dd);
    InputReader.readFunctionFromString("function A(i) { 1 ⇒ 2 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMacroPreviouslyDefinedAsMapping() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readFunctionFromString("function A(i) { 1 ⇒ 2 }", dd);
    InputReader.readMacroFromString("define A 3", dd);
  }
}

