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

WHITESPACE          : [ \t\r\n]+ -> channel(HIDDEN) ;

