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
                    | pexpressionplus
                    ;

pexpressionunit     : IDENTIFIER
                    | INTEGER
                    | MINUS INTEGER
                    | BRACKETOPEN pexpression BRACKETCLOSE
                    ;

pexpressiontimes    : pexpressionunit
                    | pexpressionunit TIMES pexpressiontimes
                    ;

pexpressionplus     : pexpressiontimes PLUS pexpression
                    | pexpressiontimes MINUS pexpression
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
                    | pconstraintrelation
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

paramboolvardec     : paramvar TYPEOF BOOLTYPE FOR parameterlist
                    ;

declaration         : DECLARE boolvardec end
                    | DECLARE paramboolvardec end
                    ;

type                : BOOLTYPE
                    | RANGETYPE IN range
                    ;

end                 : NEWLINE
                    | EOF
                    ;

/********** Formula **********/

formula             : formula2
                    ;

formula0            : BRACKETOPEN formula BRACKETCLOSE
                    | IDENTIFIER
                    | NOT formula0
                    | MINUS formula0
                    ;

conjunction         : formula0 (AND formula0)+
                    ;

disjunction         : formula0 (OR formula0)+
                    ;

formula1            : formula0
                    | conjunction
                    | disjunction
                    ;

implication         : formula1 IMPLIES formula1
                    ;

iff                 : formula1 IFF formula1
                    ;

formula2            : formula1
                    | implication
                    | iff
                    ;

requirement         : formula end
                    ;

