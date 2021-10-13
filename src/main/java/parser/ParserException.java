package language.parser;

import org.antlr.v4.runtime.Token;

/**
 * A ParserException is simply an Exception that is used as the base class for some Exceptions that
 * only arise during parsing.
 * This is used so that parser exceptions can be caught together on user input, but more specific
 * exceptions can be easily tested for.
 */
public class ParserException extends Exception {
  private Token _token;

  /** token is allowed to be null; message is not */
  public ParserException(Token token, String message) {
    super(token == null ? message :
          token.getLine() + ":" + token.getCharPositionInLine() + ": " + message);
    _token = token;
  }

  /** returns the token at which the ParserException occurred */
  public Token queryProblematicToken() {
    return _token;
  }
}

