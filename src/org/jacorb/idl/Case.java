package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 */

import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class Case 
    extends IdlSymbol 
{
    /** the labels for this case */
    public SymbolList case_label_list = null;

    /** only set after parsing */
    private IdlSymbol[] labels;

    /** this case's element's type's spec */
    public ElementSpec element_spec = null;

    String enum_type_name = null;

    /** the switch type's spec */
    TypeSpec type_spec = null;

    public Case( int num )
    {
        super(num);
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        element_spec.setPackage(s);
        Enumeration e = case_label_list.v.elements();

        for(; e.hasMoreElements(); )
        {
            IdlSymbol sym = (IdlSymbol)e.nextElement();
            if( sym != null )
                sym.setPackage(s);
        }

        if( type_spec != null )
            type_spec.setPackage( s);
    }

    /**
     * pass a reference to the containing union through
     * to the case elements, which pass it on
     */

    public void setUnion( UnionType ut )
    {
        element_spec.setUnion( ut );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name );

        enclosing_symbol = s;
        element_spec.setEnclosingSymbol( s );
    }


    public void setTypeSpec( TypeSpec s)
    { 
        // and enum type name if necessary
        if(s.type_spec instanceof ConstrTypeSpec) 
        {
            enum_type_name = ((ConstrTypeSpec)s.type_spec).typeName();
        } 
        else if( s.type_spec instanceof ScopedName )
        {
            TypeSpec ts=((ScopedName)s.type_spec).resolvedTypeSpec();

            while( ts instanceof ScopedName || ts instanceof AliasTypeSpec )
            {
                if( ts instanceof ScopedName )
                    ts = ((ScopedName)ts).resolvedTypeSpec();
                if( ts instanceof AliasTypeSpec )          
                    ts = ((AliasTypeSpec)ts).originalType();
            }

            if( ts instanceof ConstrTypeSpec )
                enum_type_name = ((ConstrTypeSpec)ts).c_type_spec.full_name();
        }
        type_spec = s;
        type_spec.setPackage( pack_name );
    }

    public void parse()          
    {
        element_spec.parse();

        labels = new IdlSymbol[case_label_list.v.size()];
        int label_idx = 0;

        for( Enumeration e = case_label_list.v.elements(); 
             e.hasMoreElements(); )
        {
            IdlSymbol sym = (IdlSymbol)e.nextElement();
            // remember label names
            labels[ label_idx++ ] = sym;

            // check that no literals are used in case labels when
            // the switch type spec doesn't allow it (i.e. bool, int, char)

            TypeSpec ts = type_spec.typeSpec();

            if( sym != null )
            { 
                // null means "default" label in union
                if( ((ConstExpr)sym).or_expr.xor_expr.and_expr.shift_expr.add_expr.
                    mult_expr.unary_expr.primary_expr.symbol instanceof Literal )
                {
                    Literal literal = 
                        (Literal)((ConstExpr)sym).or_expr.xor_expr.and_expr.shift_expr.add_expr.mult_expr.unary_expr.primary_expr.symbol;

                    if ( ts instanceof ScopedName )
                    {
                        while( ts instanceof ScopedName )
                        {
                            ts = ((ScopedName)type_spec.typeSpec()).resolvedTypeSpec();
                            if( ts instanceof AliasTypeSpec )
                                ts = ((AliasTypeSpec)ts).originalType();
                        }
                    }

                    /* make sure  that case  label  and discriminator
                       value are compatible */
                    
                    if( 
                       (!(ts instanceof BooleanType || 
                          ts instanceof IntType || 
                          ts instanceof CharType || 
                          ( ts instanceof BaseType && ((BaseType)ts).isSwitchType())
                          )
                        )
                        || 
                        ( ts instanceof BooleanType &&
                        !(literal.string.equals("true") || literal.string.equals("false")))
                       ||
                       ( ts instanceof CharType &&
                         !literal.string.startsWith("'") )
                        )
                    {
                        parser.error("Illegal case label <" + literal.string + 
                                     "> for switch type " + type_spec.typeName(), token);
                        return; // abort parsing the case here (we'd get other errors)
                    }

                    if( ts instanceof IntType )
                    {
                        try
                        {
                            int testme = Integer.parseInt( literal.string ); 
                        }
                        catch( NumberFormatException ne )
                        {
                            parser.error("Illegal case label <" + literal.string + 
                                         "> for integral switch type " + type_spec.typeName(), token);
                            return;
                        }
                    }

                }
            }


            // if the switch type for the union we're part of
            // is an enumeration type, the enum type name has
            // been set. 

            if( enum_type_name == null )
            { 
                // no enum
                if( sym != null ) 
                { 
                    // null means "default" label in union
                    sym.parse();
                } 
            } 
            else 
            {     
                // case label const expressions refer to enum values
                if( sym != null ) 
                { 
                    // now, if this is not the default case label...
                    // get the case label (a const expr) as a scoped name
                    ScopedName sn = (ScopedName)((ConstExpr)sym).or_expr.xor_expr.and_expr.
                        shift_expr.add_expr.mult_expr.unary_expr.primary_expr.symbol;

                    // replace the original case label by a new, fully
                    // scoped name for the enum type value

                    int idx = case_label_list.v.indexOf(sym);
                    sym = new ScopedName( new_num() );
                    ((ScopedName)sym).setId( sn.typeName );
                    sym.setPackage( enum_type_name );
                    sym.parse();
                    case_label_list.v.setElementAt(sym,idx);
                }
            }
        } // for
    }

    IdlSymbol[] getLabels()
    {
        return labels;
    }
    

    public void print(java.io.PrintWriter ps)
    {   
        element_spec.print(ps);
    }


}





