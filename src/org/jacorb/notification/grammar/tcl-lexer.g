// $Id$

header {
    package org.jacorb.notification.parser;
}

{
    import antlr.TokenStreamSelector;
}

class TCLLexer extends Lexer;

options {
    charVocabulary = '\0' .. '\377';
    testLiterals=false;
    k=2;
    importVocab=Common;
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

{
    public static final String LEXER_NAME = "component";

    TokenStreamSelector tokenStreamSelector;

    void setTokenStreamSelector(TokenStreamSelector s) {
        tokenStreamSelector = s;
    }

    // switch to Component Lexer
    void toggleLexer() {
        tokenStreamSelector.select(ComponentLexer.LEXER_NAME);
    }
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

// '$' switches to the component lexer
// otherwise it wouldn't be possible to recognize
// $.1 
// the Standard Lexer would recognize DOLLAR FLOAT which is wrong.
// it should be DOLLAR DOT INTEGER
DOLLAR     : '$' { toggleLexer(); };

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
	:	'.'          { _ttype = DOT; }                  // a single dot
        (((DIGIT)+   { _ttype = NUM_FLOAT; } )          // its a float number
        ((EXPONENT)  )?)?

	|	(	'0'      {isDecimal = true;}                // special case for just '0'
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
