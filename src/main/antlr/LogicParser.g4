parser grammar LogicParser;

@header {
package language.parser;
}

options {
  tokenVocab = LogicLexer;
}

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

