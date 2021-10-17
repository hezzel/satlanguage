lexer grammar LogicLexer;

@header {
package language.parser;

}

/* Lexer */

DECLARE             : 'declare' ;

WITH                : 'with' ;

FOR                 : 'for' ;

IF                  : 'if' ;

THEN                : 'then' ;

ELSE                : 'else' ;

PRINT               : 'print' ;

PRINTLN             : 'println' ;

BOOLTYPE            : 'Bool' ;

RANGETYPE           : 'Int' ;

TO                  : 'to' ;

DO                  : 'do' ;

IDENTIFIER          : [a-z] [a-z0-9A-Z_]* ;

INTEGER             : '0' | [1-9] [0-9]* ;

BRACKETOPEN         : '(' ;

BRACKETCLOSE        : ')' ;

SBRACKETOPEN        : '[' ;

SBRACKETCLOSE       : ']' ;

BRACEOPEN           : '{' ;

BRACECLOSE          : '}' ;

IN                  : ('∈' | '\\in') ;

DOT                 : '.' ;

COMMA               : ',' ;

TYPEOF              : '::' ;

PLUS                : '+' ;

MINUS               : '-' ;

TIMES               : '*' ;

GREATER             : '>' ;

SMALLER             : '<' ;

GEQ                 : ('≥' | '>=' | '\\geq') ;

LEQ                 : ('≤' | '<=' | '\\leq') ;

NEQ                 : ('≠' | '!=' | '\\neq') ;

EQUALS              : '=' ;

INITIATE            : ':=' ;

TOP                 : ('⊤' | '\\top') ;

BOTTOM              : ('⊥' | '\\bot') ;

AND                 : ('∧' | '/\\' | '\\and' | '\\wedge') ;

OR                  : ('∨' | '\\/' | '\\or' | '\\vee') ;

NOT                 : ('¬' | '\\neg') ;

IMPLIES             : ('→' | '->' | '\\rightarrow') ;

IFF                 : ('↔' | '<->' | '\\leftrightarrow') ;

QUESTION            : '?' ;

COLON               : ':' ;

DOTS                : '..' ;

FORALL              : ('∀' | '?A' | '\\forall') ;

EXISTS              : ('∃' | '?E' | '\\exists') ;

STRING              : '"' (~[\n\r"])* '"' ;

WHITESPACE          : [ \t\r\n]+ -> skip ;

