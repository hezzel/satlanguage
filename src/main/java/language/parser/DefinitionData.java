package language.parser;

import logic.parameter.Function;
import java.util.TreeMap;

public class DefinitionData {
  TreeMap<String,Integer> _macros;
  TreeMap<String,Function> _mappings;

  public DefinitionData() {
    _macros = new TreeMap<String,Integer>();
    _mappings = new TreeMap<String,Function>();
  }

  public void setMacro(String m, int value) {
    _macros.put(m, value);
  }

  public void setMapping(String m, Function value) {
    _mappings.put(m, value);
  }

  public int getMacro(String m) {
    return _macros.get(m);
  }
  
  public Function getMapping(String m) {
    return _mappings.get(m);
  }
}

