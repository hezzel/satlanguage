lexer grammar LogicLexer;

@header {
package language.parser;

}

/* Lexer */

IDENTIFIER          : [a-z] [a-z0-9A-Z_]* ;

INTEGER             : '0' | [1-9] [0-9]* ;

BRACKETOPEN         : '(' ;

BRACKETCLOSE        : ')' ;

PLUS                : '+' ;

MINUS               : '-' ;

TIMES               : '*' ;

AND                 : '∧' | '/' '\\' | '\\' 'a' 'n' 'd' ;

OR                  : '∨' | '\\' '/' | '\\' 'o' 'r' ;

GREATER             : '>' ;

SMALLER             : '<' ;

GEQ                 : '≥' | '>' '=' | '\\' 'g' 'e' 'q' ;

LEQ                 : '≤' | '<' '=' | '\\' 'l' 'e' 'q' ;

NEQ                 : '≠' | '!' '=' | '\\' 'n' 'e' 'q' ;

TOP                 : '⊤' | '\\' 't' 'o' 'p' ;

BOTTOM              : '⊥' | '\\' 'b' 'o' 't' ;

WHITESPACE          : [ \t\r]+ -> channel(HIDDEN) ;

