// $Id$

header {
package org.jacorb.notification.parser;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
    import java.io.StringReader;
    import org.jacorb.notification.node.AbstractTCLNode;
}

class TCLParser extends Parser;

options {
    k=2;
    buildAST = true;
    importVocab=Common;
    defaultErrorHandler=false;
}

tokens {
    DOLLAR      <AST=org.jacorb.notification.node.ComponentName>;
    EXIST       <AST=org.jacorb.notification.node.ExistOperator>;
    DOT         <AST=org.jacorb.notification.node.DotOperator>;
    AND         <AST=org.jacorb.notification.node.AndOperator>;
    OR          <AST=org.jacorb.notification.node.OrOperator>;
    NOT         <AST=org.jacorb.notification.node.NotOperator>;

    IN          <AST=org.jacorb.notification.node.InOperator>;
    IDENTIFIER  <AST=org.jacorb.notification.node.IdentValue>;
    STRING      <AST=org.jacorb.notification.node.StringValue>;
    TRUE        <AST=org.jacorb.notification.node.BoolValue>;
    FALSE       <AST=org.jacorb.notification.node.BoolValue>;
    PLUS        <AST=org.jacorb.notification.node.PlusOperator>;
    MINUS       <AST=org.jacorb.notification.node.MinusOperator>;
    UNARY_PLUS;
    UNARY_MINUS;

    MULT        <AST=org.jacorb.notification.node.MultOperator>;
    DIV         <AST=org.jacorb.notification.node.DivOperator>;
    NUMBER      <AST=org.jacorb.notification.node.NumberValue>;
    NUM_FLOAT   <AST=org.jacorb.notification.node.NumberValue>;
    SUBSTR      <AST=org.jacorb.notification.node.SubstrOperator>;

    GT          <AST=org.jacorb.notification.node.GtOperator>;
    LT          <AST=org.jacorb.notification.node.LtOperator>;
    GTE         <AST=org.jacorb.notification.node.GteOperator>;
    LTE         <AST=org.jacorb.notification.node.LteOperator>;
    EQ          <AST=org.jacorb.notification.node.EqOperator>;
    NEQ         <AST=org.jacorb.notification.node.NeqOperator>;

    ARRAY;
    ASSOC;
    UNION_POS;

    IMPLICIT;
    DISCRIM     <AST=org.jacorb.notification.node.ImplicitOperatorNode>;
    LENGTH      <AST=org.jacorb.notification.node.ImplicitOperatorNode>;
    TYPE_ID     <AST=org.jacorb.notification.node.ImplicitOperatorNode>;
    REPO_ID     <AST=org.jacorb.notification.node.ImplicitOperatorNode>;

    DEFAULT     <AST=org.jacorb.notification.node.DefaultOperator>;

    RUNTIME_VAR;
}

{
    public static AbstractTCLNode parse( String data )
    throws RecognitionException,
    TokenStreamException
    {

        TokenStreamSelector _selector = new TokenStreamSelector();

        // set up two Lexers
        TCLLexer _tclLexer = new TCLLexer( new StringReader( data ) );
        _tclLexer.setTokenStreamSelector( _selector );

        ComponentLexer _compLexer = new ComponentLexer( _tclLexer.getInputState() );
        _compLexer.setTokenStreamSelector( _selector );

        _selector.addInputStream( _tclLexer, TCLLexer.LEXER_NAME );
        _selector.addInputStream( _compLexer, ComponentLexer.LEXER_NAME );
        _selector.select( TCLLexer.LEXER_NAME );

        // connect the Parser with the two Lexers
        TCLParser _parser = new TCLParser( _selector );

        // begin parse
        _parser.startRule();

        // return AST tree
        return ( AbstractTCLNode ) _parser.getAST();
    }

}

// parser rules
startRule
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

bool_and
    : bool_compare (AND^ bool_compare)*
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

expr
    : term ((PLUS^ |MINUS^) term)*
    ;

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
    ;

component
    : // empty
    | DOT compDot
    | compArray
    | compAssoc
    | IDENTIFIER<AST=org.jacorb.notification.node.RuntimeVariableNode>{#IDENTIFIER.setType(RUNTIME_VAR);} compExt
    ;

compExt
    : // empty
    | DOT compDot
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
    : LBRACKET! NUMBER<AST=org.jacorb.notification.node.ArrayOperator>{#NUMBER.setType(ARRAY);} RBRACKET! compExt 
    ;

compAssoc
    : LPAREN! IDENTIFIER<AST=org.jacorb.notification.node.AssocOperator>{#IDENTIFIER.setType(ASSOC);} RPAREN! compExt
    ;

compPos
    : NUMBER compExt
    ;

unionPos
    : LPAREN<AST=org.jacorb.notification.node.UnionPositionOperator>{#LPAREN.setType(UNION_POS);} unionVal RPAREN! compExt
    ;

unionVal
    : // empty
    | NUMBER
    | PLUS^ NUMBER
    | MINUS^ NUMBER
    | STRING
    ;
