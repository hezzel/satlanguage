lexer grammar LogicLexer;

@header { package language.parser; }

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

RANGETYPE           : 'Number' ;

INTTYPE             : 'Int' [1-9] [0-9]* ;

NATTYPE             : 'Nat' [1-9] [0-9]* ;

FREEINTTYPE         : 'Int?' ;

TO                  : 'to' ;

DO                  : 'do' ;

ITE                 : 'ite' ;

MIN                 : 'min' ;

MAX                 : 'max' ;

DEFINE              : 'define' ;

FUNCTION            : 'function' ;

PROPERTY            : 'property' ;

DATA                : 'data' ;

ENUM                : 'enum' ;

SUM                 : 'Σ' | 'SUM' ;

UNDERSCORE          : '_' ;

IDENTIFIER          : [a-zA-Z_] [a-z0-9A-Z_]* ;

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

BINARYPLUS          : '⊞' | '[+]' | '\\boxplus' ;

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

QUESTION            : '?' ;

COLON               : ':' ;

SEMICOLON           : ';' ;

DOTS                : '..' ;

MID                 : '|' ;

FORALL              : ('∀' | '?A' | '\\forall') ;

EXISTS              : ('∃' | '?E' | '\\exists') ;

STRING              : ('"' (~[\n\r"])* '"') | ('\'' (~[\n\r'])* '\'') ;

SEPARATOR           : '==' '='+ ;

COMMENT             : '#' (~[\n\r])* -> skip ;

WHITESPACE          : [ \t\r\n]+ -> skip ;
