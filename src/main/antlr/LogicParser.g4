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

formula             : formula3
                    ;

variable            : IDENTIFIER
                    | paramvar
                    ;

formula0            : BRACKETOPEN formula BRACKETCLOSE
                    | variable
                    | IDENTIFIER
                    | NOT formula0
                    | MINUS formula0
                    ;

quantification      : FORALL parameter DOT formula
                    | EXISTS parameter DOT formula
                    | NOT quantification
                    | MINUS quantification
                    ;

conjunction         : formula0 (AND formula0)+
                    | formula0 (AND formula0)* AND quantification
                    ;

disjunction         : formula0 (OR formula0)+
                    | formula0 (OR formula0)* OR quantification
                    ;

formula1            : formula0
                    | conjunction
                    | disjunction
                    ;

formula2            : formula1
                    | quantification
                    ;

implication         : formula1 IMPLIES formula2
                    ;

iff                 : formula1 IFF formula2
                    ;

formula3            : formula2
                    | implication
                    | iff
                    ;

requirement         : formula end
                    ;

