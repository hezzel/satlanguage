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
