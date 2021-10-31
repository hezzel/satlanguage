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

ITE                 : 'ite' ;

MIN                 : 'min' ;

MAX                 : 'max' ;

DEFINE              : 'define' ;

FUNCTION            : 'function' ;

IDENTIFIER          : [a-z] [a-z0-9A-Z_]* ;

DEFINITION          : [A-Z] [a-z0-9A-Z_]* ;

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

RANGEPLUS           : '⊕' | '(+)' | '\\oplus' ;

MINUS               : '-' ;

TIMES               : '*' ;

DIV                 : '/' ;

MOD                 : '%' ;

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

FUNCARROW           : ('⇒' | '=>' | '\\Rightarrow') ;

UNDERSCORE          : '_' ;

QUESTION            : '?' ;

COLON               : ':' ;

SEMICOLON           : ';' ;

DOTS                : '..' ;

SUM                 : 'Σ' | 'SUM' ;

MID                 : '|' ;

FORALL              : ('∀' | '?A' | '\\forall') ;

EXISTS              : ('∃' | '?E' | '\\exists') ;

STRING              : ('"' (~[\n\r"])* '"') | ('\'' (~[\n\r'])* '\'') ;

SEPARATOR           : '==' '='+ ;

COMMENT             : '#' (~[\n\r])* -> skip ;

WHITESPACE          : [ \t\r\n]+ -> skip ;

