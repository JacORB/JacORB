package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class Literal 
    extends IdlSymbol
{
    public String string;
    public java_cup.runtime.token token;

    private ConstDecl declared_in;

    public Literal(int num)
    {
        super(num);
    }

    public void setDeclaration( ConstDecl declared_in )
    {
        this.declared_in = declared_in;
    }

    public void parse()
    {
        // const expressions containign literals can be declared
        // outside cons declarations (e.g, in sequence bounds), 
        // but care only for const declarations here.
        if( declared_in != null )
        {
            TypeSpec ts = declared_in.const_type.symbol.typeSpec();

            Environment.output(2, "Literal " + ts.getClass().getName() + " " + 
                               ( token != null? token.getClass().getName() :"<no token>"));

            if( ts instanceof FloatPtType && 
                !(token instanceof java_cup.runtime.float_token  ))
                parser.error("Expecting float/double constant!" );    
            else if(  ts instanceof FixedPointConstType && 
                      !( token instanceof fixed_token  ) )
                parser.error("Expecting fixed point constant (perhaps a missing \"d\")!" );    
        }
    }


    public void print(PrintWriter ps)
    {
        ps.print( string );
    }
}

