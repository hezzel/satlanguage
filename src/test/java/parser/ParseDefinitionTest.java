package logic.parameter;

import org.junit.Test;
import static org.junit.Assert.*;

import logic.parameter.Function;
import language.execution.StringFunction;
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

  @Test
  public void testUseNegativeMacro() throws ParserException {
    DefinitionData dd = defs();
    int a = InputReader.readMacroFromString("define A 3", dd);
    int b = InputReader.readMacroFromString("define B -A", dd);
    assertTrue(b == -3);
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
  public void testReadProperty() throws ParserException {
    Property p = InputReader.readPropertyFromString(
      "property PROP { (1,2) ; (3,_,4) ; 2 ; (1,2) }", defs());
    assertTrue(p.lookup(2));
    assertFalse(p.lookup(3));
    ArrayList<Integer> arr = new ArrayList<Integer>();
    arr.add(1);
    arr.add(2);
    assertTrue(p.lookup(arr));
    arr.add(4);
    assertFalse(p.lookup(arr));
    arr.set(0, 3);
    assertTrue(p.lookup(arr));
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

  @Test
  public void testReadDataWithTwoFunctions() throws ParserException {
    DefinitionData dd = new DefinitionData();
    InputReader.readDataFromString(
      "data STUDENT(KIND, WANTSLUNCH) {" +
      "  \"This one\" ⇒ (3, 1) ; " +
      "  ThatOne      ⇒ (7, 0) " +
      "}", dd);
    
    // check if the enum is well-defined
    StringFunction student = dd.getEnum("STUDENT");
    assertTrue(student.toString().equals("{ 1 ⇒ \"This one\" ; 2 ⇒ \"ThatOne\" }"));
    // check if the keys of the enum are defined as macros
    assertTrue(dd.getMacro("\"This one\"") == 1);
    assertTrue(dd.getMacro("ThatOne") == 2);
    // check if the functions are well-defined
    Function kind = dd.getFunction("KIND");
    assertTrue(kind.toString().equals("KIND(student) { 1 ⇒ 3 ; 2 ⇒ 7 }"));
    Function lunch = dd.getFunction("WANTSLUNCH");
    assertTrue(lunch.toString().equals("WANTSLUNCH(student) { 1 ⇒ 1 ; 2 ⇒ 0 }"));
  }

  @Test
  public void testReadDataWithSingleFunction() throws ParserException {
    DefinitionData dd = new DefinitionData();
    InputReader.readDataFromString(
      "data STUDENT(KIND) {" +
      "  \"This one\" ⇒ 3 ; " +
      "  ThatOne      ⇒ 6 " +
      "}", dd);
    
    // check if the enum is well-defined
    StringFunction student = dd.getEnum("STUDENT");
    assertTrue(student != null);
    // check if the functions are well-defined
    Function kind = dd.getFunction("KIND");
    assertTrue(kind.toString().equals("KIND(student) { 1 ⇒ 3 ; 2 ⇒ 6 }"));
  }

  @Test(expected = language.parser.ParserException.class)
  public void testCatchallNotAllowedInProperty() throws ParserException {
    Property p = InputReader.readPropertyFromString("property PROP { (1,2) ; _ }", defs());
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

  @Test
  public void testReadEnumerate() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readEnumFromString("enum A { \"B\" ; \"c d\" ; E }", dd);
    StringFunction f = dd.getEnum("A");
    assertTrue(f.toString().equals("{ 1 ⇒ \"B\" ; 2 ⇒ \"c d\" ; 3 ⇒ \"E\" }"));
    assertTrue(dd.getMacro("\"B\"") == 1);
    assertTrue(dd.getMacro("\"c d\"") == 2);
    assertTrue(dd.getMacro("E") == 3);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDoubleMappingDefinition() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readFunctionFromString("function ABC(i) {1 ⇒ 2 ; 3 ⇒ 4 }", dd);
    InputReader.readFunctionFromString("function ABC(j) {1 ⇒ 2 ; 3 ⇒ 4 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testEntryWithWrongLength() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readFunctionFromString("function A(i) { (1,2) ⇒ 2 }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testFunctionPreviouslyDefinedAsMacro() throws ParserException {
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

  @Test(expected = language.parser.ParserException.class)
  public void testMacroPreviouslyDefinedAsEnum() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readEnumFromString("enum A { B ; C }", dd);
    InputReader.readMacroFromString("define A 3", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testMacroPreviouslyDefinedInEnum() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readEnumFromString("enum A { B ; C }", dd);
    InputReader.readMacroFromString("define B 3", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testEnumerateNamePreviouslyDefinedAsFunction() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readFunctionFromString("function A(i) { 1 ⇒ 2 }", dd);
    InputReader.readEnumFromString("enum A { B ; C }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testEnumerateKeyPreviouslyDefined() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readEnumFromString("enum A { B ; C }", dd);
    InputReader.readEnumFromString("enum E { C ; D }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testDuplicateEnumerateKey() throws ParserException {
    DefinitionData dd = defs();
    InputReader.readEnumFromString("enum A { B ; \"C\" ; B }", dd);
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadDataWrongNumberOfArguments() throws ParserException {
    InputReader.readDataFromString(
      "data STUDENT(KIND, WANTSLUNCH) {" +
      "  \"This one\" ⇒ (3, 1) ; " +
      "  ThatOne      ⇒ (7) " +
      "}", defs());
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadDataDuplicateKey() throws ParserException {
    InputReader.readDataFromString(
      "data STUDENT(KIND, WANTSLUNCH) {" +
      "  \"This one\" ⇒ (3, 1) ; " +
      "  \"This one\" ⇒ (3, 1) ; " +
      "}", defs());
  }

  @Test(expected = language.parser.ParserException.class)
  public void testReadDataDuplicateFunction() throws ParserException {
    InputReader.readDataFromString(
      "data STUDENT(KIND, KIND) {\"This one\" ⇒ (3, 1) }", defs());
  }
}

