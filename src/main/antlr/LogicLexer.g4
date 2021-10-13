lexer grammar LogicLexer;

@header {
package language.parser;

}

/* Lexer */

WITH                : 'with' ;

FOR                 : 'for' ;

IDENTIFIER          : [a-z] [a-z0-9A-Z_]* ;

INTEGER             : '0' | [1-9] [0-9]* ;

BRACKETOPEN         : '(' ;

BRACKETCLOSE        : ')' ;

BRACEOPEN           : '{' ;

BRACECLOSE          : '}' ;

IN                  : ('∈' | '\\in') ;

DOT                 : '.' ;

COMMA               : ',' ;

PLUS                : '+' ;

MINUS               : '-' ;

TIMES               : '*' ;

GREATER             : '>' ;

SMALLER             : '<' ;

GEQ                 : ('≥' | '>=' | '\\geq') ;

LEQ                 : ('≤' | '<=' | '\\leq') ;

NEQ                 : ('≠' | '!=' | '\\neq') ;

TOP                 : ('⊤' | '\\top') ;

BOTTOM              : ('⊥' | '\\bot') ;

AND                 : ('∧' | '/\\' | '\\and') ;

OR                  : ('∨' | '\\/' | '\\or') ;

DOTS                : '..' ;

WHITESPACE          : [ \t\r]+ -> channel(HIDDEN) ;

