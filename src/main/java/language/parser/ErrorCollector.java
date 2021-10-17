package language.parser;

import java.util.ArrayList;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ErrorCollector extends BaseErrorListener {
  private ArrayList<String> _messages;

  public ErrorCollector() {
    _messages = new ArrayList<String>();
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                          int charPositionInLine, String msg, RecognitionException e) {
    System.out.println("SyntaxError is called here.");
    _messages.add("" + line + ":" + charPositionInLine + ": " + msg);
  }

  public int queryErrorCount() {
    return _messages.size();
  }

  public String queryError(int index) {
    return _messages.get(index);
  }

  /**
   * If any errors have been collected, these are thrown using an AntlrParserException; then,
   * the list of messages is reset.
   * If no errors have been collected, nothing is done.
   */
  public void throwCollectedExceptions() throws AntlrParserException {
    if (_messages.size() == 0) return;
    AntlrParserException ex = new AntlrParserException(_messages);
    _messages = new ArrayList<String>();
    throw ex;
  }
}

