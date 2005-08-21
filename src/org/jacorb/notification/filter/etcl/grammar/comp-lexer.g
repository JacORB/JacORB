// $Id$

header {
package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2005 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
}

{
    import antlr.TokenStreamSelector;
}

class ComponentLexer extends Lexer;

options {
    charVocabulary = '\0' .. '\377';
    testLiterals=false;
    k=2;
    importVocab=Common;
}

// as no action can be associated to a token
// the action must be embedded in the testLiteralsTable Method
// called by the Lexer to recognize literals
tokens {
    AND = "and";
    OR = "or";
}

{
    // name of the other Lexer
    public final static String LEXER_NAME = "comp";

    // this needs to be set before use
    TokenStreamSelector tokenStreamSelector;

    // override testLiteralsTable
    // to switch to other Standard Lexer
    // if one of the tokens has been matched
    public int testLiteralsTable(int ttype) {
        int _ret = super.testLiteralsTable(ttype);
        switch (_ret) {
            case AND:
                // fallthrough
            case OR:
                toggleLexer();
                break;
            default:
                break;
        }
        return _ret;
    }

    void setTokenStreamSelector(TokenStreamSelector s) {
        tokenStreamSelector = s;
    }

    // switch to standard Lexer
    void toggleLexer() {
        tokenStreamSelector.select(TCLLexer.LEXER_NAME);
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

// each of this operators switches to standard lexer mode
EQ         : "==" {toggleLexer(); };
NEQ        : "!=" {toggleLexer(); };
LT         : '<'  {toggleLexer(); };
LTE        : "<=" {toggleLexer(); };
GT         : '>'  {toggleLexer(); };
GTE        : ">=" {toggleLexer(); };
SUBSTR     : '~'  {toggleLexer(); };
PLUS       : '+'  {toggleLexer(); };
MINUS      : '-'  {toggleLexer(); };
MULT       : '*'  {toggleLexer(); };
DIV        : '/'  {toggleLexer(); };

// still inside a component
DOT        : '.'  ;
LPAREN     : '('  ;
RPAREN     : ')'  ;
LBRACKET   : '['  ;
RBRACKET   : ']'  ;
DOLLAR     : '$'  ;
DISCRIM    : "_d";
TYPE_ID    : "_type_id";
REPO_ID    : "_repos_id";
LENGTH     : "_length";

// STRING
//     : '\''! TEXTCHARS '\''!
//     ;

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
    : '0'                  // special case for just '0'
    | ('1'..'9') (DIGIT)*
    ;

// protected TEXTCHARS
//     : // empty
//     | TEXTCHAR TEXTCHARS
//     ;

// protected TEXTCHAR
//     : ALPHA
//     | DIGIT
//     | OTHER
//     | OTHER_TEXT
//     | ' '
//     | SPECIAL
//     ;

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

// // these may appear within text but not within identifiers
// protected OTHER_TEXT
//     : ('.')
//     ;

// protected SPECIAL
//      : '\\'!
//         ( '\''
//         | '\\' )
//      ;
