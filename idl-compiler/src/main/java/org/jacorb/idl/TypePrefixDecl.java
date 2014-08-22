/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.idl;

import java.io.PrintWriter;

/**
 * Declaration of typeprefix
 * 
 * @author Alexander Birchenko
 */

public class TypePrefixDecl extends Declaration
{
    public ScopedName scopedname;
    public String prefix;

    public TypePrefixDecl (int num)
    {
        super(num);
    }

    public void print( PrintWriter ps )
    {
        //do nothing
    }

    public void parse() throws ParseException
    {
        String rname = this.scopedname.typeName;

        if(!NameTable.isDefined(rname, IDLTypes.MODULE))
        {
            //            Compose full module name if specified briefly.
            //            For example:
            //
            //            module A1 {
            //                module B {
            //                    module A2 {
            //                        module B {
            //                            typeprefix B "com.something";
            //                        }
            //                    }
            //                }
            //            }
            //
            //            "typeprefix B" will be "typeprefix A1.B"

            java.util.StringTokenizer strtok = new java.util.StringTokenizer( this.pack_name, "." );
            String nameScopes[] = new String[ strtok.countTokens() ];
            boolean isModuleFinded = false;

            int count = 0;
            for( ; strtok.hasMoreTokens(); count++ )
            {
                String name = strtok.nextToken();
                nameScopes[count] = name;

                if(name.equals(rname))
                {
                    isModuleFinded = true;
                    break;
                }
            }

            if(isModuleFinded)
            {
                StringBuffer fullName = new StringBuffer();
                for( int i = 0; i < count+1; ++i )
                {
                    fullName.append( nameScopes[ i ] );
                    fullName.append( "." );
                }

                rname = fullName.substring(0, fullName.length()-1);
            }
            else
            {
                parser.error("Module name " + rname + " undefined", scopedname.token);
            }
        }

        //overrides previously defined prefix for this module without check
        TypePrefixes.define(rname, this.prefix);
    }
}
