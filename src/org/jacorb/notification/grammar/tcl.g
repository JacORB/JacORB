// $Id$

header {
    package org.jacorb.notification.node;
}

{
    import java.io.*;
}

class TCLParser extends Parser;

options {
    k=2;
    buildAST = true;
    exportVocab=TCL;
    defaultErrorHandler=false;
}

tokens {
    DOLLAR      <AST=ComponentOperator>;
    EXIST       <AST=ExistOperator>;
    DOT         <AST=DotOperator>;
    AND         <AST=AndOperator>;
    OR          <AST=OrOperator>;
    NOT         <AST=NotOperator>;

    IN          <AST=InOperator>;
    IDENTIFIER  <AST=IdentValue>;
    STRING      <AST=StringValue>;
    TRUE        <AST=BoolValue>;
    FALSE       <AST=BoolValue>;
    PLUS        <AST=PlusOperator>;
    MINUS       <AST=MinusOperator>;
    UNARY_PLUS;
    UNARY_MINUS;

    MULT        <AST=MultOperator>;
    DIV         <AST=DivOperator>;

    NUMBER      <AST=NumberValue>;
    NUM_FLOAT   <AST=NumberValue>;
    COMP_POS    <AST=ComponentPositionOperator>;

    SUBSTR      <AST=SubstrOperator>;

    GT          <AST=GtOperator>;
    LT          <AST=LtOperator>;
    GTE         <AST=GteOperator>;
    LTE         <AST=LteOperator>;
    EQ          <AST=EqOperator>;
    NEQ         <AST=NeqOperator>;

    ARRAY;
    ASSOC;
    UNION_POS;

    IMPLICIT;
    DISCRIM     <AST=ImplicitOperatorNode>;
    LENGTH      <AST=ImplicitOperatorNode>;
    TYPE_ID     <AST=ImplicitOperatorNode>;
    REPO_ID     <AST=ImplicitOperatorNode>;

    DEFAULT     <AST=DefaultOperator>;
}

// parser rules
startRule
//    : ((constraint|preference) SEMI)* EOF
    : constraint
    ;

constraint
    : // empty
    | bool
    ;

preference
    : // empty
    | MIN^ bool
    | MAX^ bool
    | WITH^ bool
    | RANDOM^
    | FIRST^
    ;

bool
    : bool_or
    ;

bool_or
    : bool_and (OR^ bool_and)*
    ;

bool_or2
    : OR^ bool_and bool_or2
    | // empty
    ;

bool_and
    : bool_compare (AND^ bool_compare)*
    ;

bool_and2
    : AND^ bool_compare bool_and2
    | // empty
    ;

bool_compare
    : expr_in ((EQ^|NEQ^|LT^|LTE^|GT^|GTE^) expr_in)*
    ;

expr_in
    : expr_twiddle ((IN^ IDENTIFIER)|(IN^ dollarComponent))?
    ;

expr_twiddle
    : expr ((SUBSTR^) expr)*
    ;

// expr
//     : term expr2
//     ;

// expr2
//     : PLUS^ term expr2
//     | MINUS^ term expr2
//     | // empty
//     ;
expr
    : term ((PLUS^ |MINUS^) term)*
    ;

// term
//     : factor_not term2
//     ;

// term2
//     : MULT factor_not term2
//     | DIV factor_not term2
//     | // empty
//     ;
term
    : factor_not ((MULT^ |DIV^)factor_not)*
    ;

factor_not
    : (NOT^)? factor
    ;

factor
    : LPAREN! bool_or RPAREN!
    | EXIST^ IDENTIFIER
    | EXIST^ dollarComponent
    | dollarComponent
    | DEFAULT^ dollarComponent
    | IDENTIFIER
    | number
    | PLUS^  number {#PLUS.setType(UNARY_PLUS);}
    | MINUS^ number {#MINUS.setType(UNARY_MINUS);}
    | STRING
    | TRUE
    | FALSE
    ;

dollarComponent
    : DOLLAR^ component
    ;

number
    : NUMBER
    | NUM_FLOAT
    | COMP_POS<AST=NumberValue>{#COMP_POS.setType(NUM_FLOAT);}
    ;

component
    : // empty
    | DOT     compDot
    | COMP_POS compExt
    | compArray
    | compAssoc
    | IDENTIFIER compExt
    ;

compExt
    : // empty
    | DOT compDot
    | COMP_POS compExt
    | compArray
    | compAssoc
    ;

compDot
    : IDENTIFIER compExt
    | compPos
    | unionPos
    | LENGTH  {#LENGTH.setType(IMPLICIT);}
    | DISCRIM {#DISCRIM.setType(IMPLICIT);}
    | TYPE_ID {#TYPE_ID.setType(IMPLICIT);}
    | REPO_ID {#REPO_ID.setType(IMPLICIT);}
    ;

compArray
    : LBRACKET! NUMBER<AST=ArrayOperator>{#NUMBER.setType(ARRAY);} RBRACKET! compExt 
    ;

compAssoc
    : LPAREN! IDENTIFIER<AST=AssocOperator>{#IDENTIFIER.setType(ASSOC);} RPAREN! compExt
    ;

compPos
    : COMP_POS compExt
    ;

unionPos
    : LPAREN<AST=UnionPositionOperator>{#LPAREN.setType(UNION_POS);} unionVal RPAREN! compExt
    ;

unionVal
    : // empty
    | NUMBER
    | PLUS^ NUMBER
    | MINUS^ NUMBER
    | STRING
    ;

class TCLLexer extends Lexer;

options {
    charVocabulary = '\0' .. '\377';
    testLiterals=false;
    k=2;
    exportVocab=TCL;
}

tokens {
    NOT      = "not";
    TRUE     = "TRUE";
    FALSE    = "FALSE";
    DEFAULT  = "default";
    EXIST    = "exist";
    OR       = "or";
    AND      = "and";
    IN       = "in";
    MIN      = "min";
    MAX      = "max";
    WITH     = "with";
    RANDOM   = "random";
    FIRST    = "first";
    TYPE     = "type";
}

// Whitespace
WS
    : ( ' '
        | '\t'
        | '\f'

            // newlines
        | ("\r\n"
            | '\r'
            | '\n' )
            { newline(); }
        )
        { $setType(Token.SKIP); }
    ;

// single-line comment
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
		{$setType(Token.SKIP); newline();}
	;

// Operators
EQ         : "==" ;
NEQ        : "!=" ;
LT         : '<'  ;
LTE        : "<=" ;
GT         : '>'  ;
GTE        : ">=" ;
SUBSTR     : '~'  ;
PLUS       : '+'  ;
MINUS      : '-'  ;
MULT       : '*'  ;
DIV        : '/'  ;
LPAREN     : '('  ;
RPAREN     : ')'  ;
LBRACKET   : '['  ;
RBRACKET   : ']'  ;
DOLLAR     : '$'  ;
DISCRIM    : "_d";
TYPE_ID    : "_type_id";
REPO_ID    : "_repos_id";
LENGTH     : "_length";

STRING
    : '\''! TEXTCHARS '\''!
    ;

IDENTIFIER
options {testLiterals=true;}
    : LEADER FOLLOWSEQ
    | '\\' LEADER FOLLOWSEQ
    ;

protected FOLLOWSEQ
    : // empty
    | FOLLOW FOLLOWSEQ
    ;

protected LEADER
    : ALPHA
    ;

protected FOLLOW
    : ALPHA
    | DIGIT
    | OTHER
    ;

// a numeric literal
NUMBER
    {boolean isDecimal=false;}
	:	'.'         { _ttype = DOT; }                   // a single dot
        (((DIGIT)+   { _ttype = COMP_POS; } )           // could be Positional Notation for a struct
        ((EXPONENT) { _ttype = NUM_FLOAT; })?)?         // its a number !

	|	(	'0'     {isDecimal = true;}                 // special case for just '0'
		|	('1'..'9') (DIGIT)*  {isDecimal=true;}		// non-zero decimal
		)
		(
            // only check to see if it's a float if looks like decimal so far
			{isDecimal}?
			( '.' (DIGIT)* (EXPONENT)? | EXPONENT ) { _ttype = NUM_FLOAT; }
		)?
	;

// a couple protected methods to assist in matching floating point numbers
protected EXPONENT
	:	('e'|'E') ('+'|'-') (DIGIT)+
	;

protected TEXTCHARS
    : // empty
    | TEXTCHAR TEXTCHARS
    ;

protected TEXTCHAR
    : ALPHA
    | DIGIT
    | OTHER
    | OTHER_TEXT
    | ' '
    | SPECIAL
    ;

protected DIGITS
    : (DIGIT)+
    ;

protected DIGIT
    : ('0'..'9')
    ;

protected ALPHA
     : ('a'..'z'|'A'..'Z')
     ;

protected OTHER
     : ('_'|':'|'/')
     ;

// these may appear within text but not within identifiers
protected OTHER_TEXT
    : ('.')
    ;

protected SPECIAL
     : '\\'!
        ( '\''
        | '\\' )
     ;
