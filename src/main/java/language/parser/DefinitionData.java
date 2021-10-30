package language.parser;

import logic.parameter.Function;
import java.util.TreeMap;

public class DefinitionData {
  TreeMap<String,Integer> _macros;
  TreeMap<String,Function> _functions;

  public DefinitionData() {
    _macros = new TreeMap<String,Integer>();
    _functions = new TreeMap<String,Function>();
  }

  public void setMacro(String m, int value) {
    _macros.put(m, value);
  }

  public void setFunction(String m, Function value) {
    _functions.put(m, value);
  }

  public int getMacro(String m) {
    return _macros.get(m);
  }
  
  public Function getFunction(String m) {
    return _functions.get(m);
  }
}

