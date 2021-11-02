parser grammar LogicParser;

@header {
package language.parser;
}

options {
  tokenVocab = LogicLexer;
}

/********** general **********/

integer             : MINUS? INTEGER
                    | MINUS? IDENTIFIER
                    | MINUS? STRING
                    ;

/********** parametrised variables **********/

paramvar            : IDENTIFIER SBRACKETOPEN pexpression (COMMA pexpression)* SBRACKETCLOSE
                    ;

variable            : IDENTIFIER
                    | paramvar
                    ;

/********** PExpression **********/

pexpression         : pexpressionminus
                    | pexpression PLUS pexpressionminus
                    ;

pexpressionminus    : pexpressiontimes
                    | pexpressionminus MINUS pexpressiontimes
                    ;

pexpressiontimes    : pexpressionunit
                    | pexpressiontimes TIMES pexpressionunit
                    | pexpressiontimes DIV pexpressionunit
                    | pexpressiontimes MOD pexpressionunit
                    ;

pexpressionunit     : MINUS? IDENTIFIER
                    | integer
                    | MIN BRACKETOPEN pexpression COMMA pexpression BRACKETCLOSE
                    | MAX BRACKETOPEN pexpression COMMA pexpression BRACKETCLOSE
                    | BRACKETOPEN pexpression BRACKETCLOSE
                    | IDENTIFIER BRACKETOPEN pexpression (COMMA pexpression)* BRACKETCLOSE
                    | MID IDENTIFIER MID
                    | paramvar
                    ;

onlypexpression     : pexpression EOF
                    ;

/********** PConstraint **********/

pconstraint         : pconstraintunit (AND pconstraintunit)*
                    | pconstraintunit (OR pconstraintunit)*
                    ;

pconstraintunit     : BRACKETOPEN pconstraint BRACKETCLOSE
                    | TOP
                    | BOTTOM
                    | (NOT | MINUS) pconstraintunit
                    | pconstraintproperty
                    | pconstraintrelation
                    | variable
                    ;

pconstraintproperty : IDENTIFIER BRACKETOPEN pexpression (COMMA pexpression)* BRACKETCLOSE
                    ;

pconstraintrelation : pexpression GREATER pexpression
                    | pexpression SMALLER pexpression
                    | pexpression GEQ pexpression
                    | pexpression LEQ pexpression
                    | pexpression NEQ pexpression
                    | pexpression EQUALS pexpression
                    ;

onlypconstraint     : pconstraint EOF
                    ;

/********** Parameter and parameterlist **********/

parameter           : IDENTIFIER IN range
                    ;

range               : BRACEOPEN pexpression DOTS pexpression BRACECLOSE (WITH pconstraint)?
                    ;

parameterlist       : parameter (COMMA parameter)*
                    ;

onlyparameter       : parameter EOF
                    ;

/********** declarations **********/

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
                    | intcomparison
                    | variable
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

intcomparison       : intexpression GEQ intexpression
                    | intexpression LEQ intexpression
                    | intexpression GREATER intexpression
                    | intexpression SMALLER intexpression
                    | intexpression EQUALS intexpression
                    | intexpression NEQ intexpression
                    ;

intexpression       : intexpressionmain (rangeplus intexpressionmain)*
                    ;

rangeplus           : RANGEPLUS
                    | PLUS
                    ;

intexpressionmain   : IDENTIFIER
                    | BRACKETOPEN intexpression BRACKETCLOSE
                    | condition QUESTION intexpression
                    | SUM BRACEOPEN intexpression MID parameterlist (MID formula)? BRACECLOSE
                    | paramvar
                    | pexpressionminus
                    ;

condition           : BRACKETOPEN formula BRACKETCLOSE
                    | NOT condition
                    | variable
                    ;


onlyformula         : formula EOF
                    ;

/********** The execution language **********/

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

stringexpr          : STRING
                    | IDENTIFIER BRACKETOPEN pexpression (COMMA pexpression)* BRACKETCLOSE
                    | pexpression
                    ;

block               : BRACEOPEN statement* BRACECLOSE
                    ;

/********** Functions and properties **********/

macro               : DEFINE IDENTIFIER pexpression
                    ;

function            : FUNCTION IDENTIFIER BRACKETOPEN funcargs BRACKETCLOSE BRACEOPEN funcentries BRACECLOSE
                    ;

funcargs            : IDENTIFIER (COMMA IDENTIFIER)*
                    ;

funcentries         : mappingentry (SEMICOLON mappingentry)* SEMICOLON?
                    ;

mappingentry        : match FUNCARROW pexpression
                    ;

match               : optionalinteger
                    | BRACKETOPEN optionalinteger (COMMA optionalinteger)* BRACKETCLOSE
                    ;

optionalinteger     : integer
                    | UNDERSCORE
                    ;

property            : PROPERTY IDENTIFIER BRACEOPEN propentries BRACECLOSE
                    ;

propentries         : match (SEMICOLON match)* SEMICOLON?
                    ;

enumerate           : ENUM IDENTIFIER BRACEOPEN enumentries BRACECLOSE
                    ;

enumentries         : enumkey (SEMICOLON enumkey)* SEMICOLON?
                    ;

enumkey             : STRING
                    | IDENTIFIER
                    ;

data                : DATA IDENTIFIER BRACKETOPEN funcargs BRACKETCLOSE BRACEOPEN dataentries BRACECLOSE
                    ;

dataentries         : dataentry (SEMICOLON dataentry)* SEMICOLON?
                    ;

dataentry           : enumkey FUNCARROW integer
                    | enumkey FUNCARROW BRACKETOPEN integer (COMMA integer)* BRACKETCLOSE
                    ;

definition          : macro
                    | function
                    | property
                    | enumerate
                    | data
                    ;

/********** Full programs **********/


program             : definition* SEPARATOR? (declaration | formula)* SEPARATOR? statement* EOF
                    ;

