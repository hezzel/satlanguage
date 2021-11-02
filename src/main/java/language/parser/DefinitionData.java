package language.parser;

import logic.parameter.Function;
import logic.parameter.Property;
import language.execution.StringFunction;
import java.util.TreeMap;

public class DefinitionData {
  TreeMap<String,Integer> _macros;
  TreeMap<String,Function> _functions;
  TreeMap<String,Property> _properties;
  TreeMap<String,StringFunction> _enums;

  public DefinitionData() {
    _macros = new TreeMap<String,Integer>();
    _functions = new TreeMap<String,Function>();
    _properties = new TreeMap<String,Property>();
    _enums = new TreeMap<String,StringFunction>();
  }

  public void setMacro(String m, int value) {
    _macros.put(m, value);
  }

  public void setFunction(String m, Function value) {
    _functions.put(m, value);
  }

  public void setEnum(String s, StringFunction value) {
    _enums.put(s, value);
  }

  public int getMacro(String m) {
    return _macros.get(m);
  }
  
  public Function getFunction(String f) {
    return _functions.get(f);
  }

  public Property getProperty(String p) {
    return _properties.get(p);
  }

  public StringFunction getEnum(String e) {
    return _enums.get(e);
  }

  public boolean defines(String name) {
    return _macros.containsKey(name) ||
           _functions.containsKey(name) ||
           _properties.containsKey(name) ||
           _enums.containsKey(name);
  }

  public String definedAsWhat(String name) {
    if (_macros.containsKey(name)) return "macro";
    if (_functions.containsKey(name)) return "function";
    if (_properties.containsKey(name)) return "property";
    if (_enums.containsKey(name)) return "enum";
    return "nothing";
  }
}

