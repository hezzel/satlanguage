parser grammar LogicParser;

@header {
package language.parser;
}

options {
  tokenVocab = LogicLexer;
}

/********** PExpression **********/

onlypexpression     : pexpression EOF
                    ;

pexpression         : pexpressiontimes
                    | pexpression PLUS pexpressiontimes
                    | pexpression MINUS pexpressiontimes
                    ;

pexpressionunit     : IDENTIFIER
                    | INTEGER
                    | MINUS INTEGER
                    | BRACKETOPEN pexpression BRACKETCLOSE
                    ;

pexpressiontimes    : pexpressionunit
                    | pexpressionunit TIMES pexpressiontimes
                    ;

/********** PConstraint **********/

onlypconstraint     : pconstraint EOF
                    ;

pconstraint         : pconstraintunit pconstraintand*
                    | pconstraintunit pconstraintor*
                    ;

pconstraintunit     : BRACKETOPEN pconstraint BRACKETCLOSE
                    | TOP
                    | BOTTOM
                    | (NOT | MINUS) pconstraintunit
                    | pconstraintrelation
                    | variable
                    ;

pconstraintand      : AND pconstraintunit
                    ;

pconstraintor       : OR pconstraintunit
                    ;

pconstraintrelation : pexpression GREATER pexpression
                    | pexpression SMALLER pexpression
                    | pexpression GEQ pexpression
                    | pexpression LEQ pexpression
                    | pexpression NEQ pexpression
                    | pexpression EQUALS pexpression
                    ;

/********** Parameter and parameterlist **********/

onlyparameter       : parameter EOF
                    ;

parameter           : IDENTIFIER IN range
                    ;

range               : BRACEOPEN pexpression DOTS pexpression BRACECLOSE (WITH pconstraint)?
                    ;

parameterlist       : parameter (COMMA parameter)*
                    ;

/********** ParamBoolVar **********/

paramvar            : IDENTIFIER SBRACKETOPEN pexprlist SBRACKETCLOSE
                    ;

pexprlist           :
                    | pexpression (COMMA pexpression)*
                    ;

boolvardec          : IDENTIFIER TYPEOF BOOLTYPE
                    ;

intvardec           : IDENTIFIER TYPEOF RANGETYPE IN range
                    ;

paramboolvardec     : paramvar TYPEOF BOOLTYPE FOR parameterlist
                    ;

paramintvardec      : paramvar TYPEOF RANGETYPE IN range FOR parameterlist
                    ;

type                : BOOLTYPE
                    | RANGETYPE IN range
                    ;

declaration         : DECLARE boolvardec
                    | DECLARE intvardec
                    | DECLARE paramboolvardec
                    | DECLARE paramintvardec
                    ;

internaldeclaration : boolvardec EOF
                    | intvardec EOF
                    | paramboolvardec EOF
                    | paramintvardec EOF
                    ;

/********** Formula **********/

formula             : smallformula
                    | arrow
                    | junction
                    | quantification
                    ;

smallformula        : BRACKETOPEN formula BRACKETCLOSE
                    | (NOT | MINUS) smallformula
                    | ITE BRACKETOPEN formula COMMA formula COMMA formula BRACKETCLOSE
                    | variable
                    ;

variable            : IDENTIFIER
                    | paramvar
                    ;

junction            : smallformula AND formula
                    | smallformula OR formula
                    ;

quantification      : FORALL parameter DOT formula
                    | EXISTS parameter DOT formula
                    | (NOT | MINUS) quantification
                    ;

arrow               : (smallformula | junction) IMPLIES formula
                    | (smallformula | junction) IFF formula
                    ;

onlyformula         : formula EOF
                    ;

/********** The execution language **********/

stringexpr          : STRING
                    | pexpression
                    ;

statement           : ifstatement
                    | forstatement
                    | printstatement
                    | block
                    ;

ifstatement         : IF pconstraint THEN statement
                    | IF pconstraint THEN statement ELSE statement
                    ;

forstatement        : FOR IDENTIFIER INITIATE pexpression TO pexpression DO statement
                    ;

printstatement      : PRINT BRACKETOPEN BRACKETCLOSE
                    | PRINTLN BRACKETOPEN BRACKETCLOSE
                    | PRINT BRACKETOPEN stringexpr ( COMMA stringexpr)* BRACKETCLOSE
                    | PRINTLN BRACKETOPEN stringexpr ( COMMA stringexpr)* BRACKETCLOSE
                    ;

block               : BRACEOPEN statement* BRACECLOSE
                    ;

/********** Full programs **********/

program             : declaration* formula* SEPARATOR statement* EOF
                    ;

