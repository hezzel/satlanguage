package language.parser;

import org.antlr.v4.runtime.Token;

/**
 * A ParserError may arise during parsing, but really should not: this happens if something
 * unexpected occurs in the ParseTree that has not been handled properly in the code, such as
 * changes in the lexer/parser that have not been properly updated along in the input reader.
 */
public class ParserError extends Error {
  private String _text;

  public ParserError(int line, int pos, String text, String message) {
    super(line + ":" + pos + ": Parser exception on input [" + text + "]: " + message);
    _text = text;
  }

  public ParserError(Token token, String text, String message) {
    super(token.getLine() + ":" + token.getCharPositionInLine() + ": Parser exception on input [" +
      text + "]: " + message);
    _text = text;
  }

  public String getProblematicInput() {
    return _text;
  }
}

