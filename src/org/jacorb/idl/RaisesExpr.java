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
import java.io.PrintWriter;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class RaisesExpr 
    extends IdlSymbol
{
    public Vector nameList;

    public RaisesExpr(int num)
    {
        super(num);
        nameList = new Vector();
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        for( Enumeration e = nameList.elements();       
             e.hasMoreElements();
             ((ScopedName)e.nextElement()).setPackage(s))
            ;
    }

    public boolean empty()
    {
        return ( nameList.size() == 0 );
    }

    public String[] getExceptionNames()
    {
        String[] result = new String[nameList.size()];
        Enumeration e = nameList.elements();
        for( int i = 0; i < result.length; i++)
        {
            result[i] = ((ScopedName)e.nextElement()).toString();
        }
        return result;
    }

    public String[] getExceptionIds()
    {
        String[] result = new String[nameList.size()];
        Enumeration e = nameList.elements();
        for( int i = 0; i < result.length; i++)
        {
            result[i] = ((ScopedName)e.nextElement()).id();
        }
        return result;    
    }

    public String[] getExceptionClassNames()
    {
        String[] result = new String[nameList.size()];
        Enumeration e = nameList.elements();
        for( int i = 0; i < result.length; i++)
        {
            result[i] = ((ScopedName)e.nextElement()).toString();
        }
        return result;
    }

    public void parse()
    {
        Hashtable h = new Hashtable(); // for removing duplicate exception names
        for( Enumeration e = nameList.elements(); e.hasMoreElements();)
        {
            ScopedName name = null;
            try
            {
                name = (ScopedName)e.nextElement();
                TypeSpec ts = name.resolvedTypeSpec();
                if( ((StructType)((ConstrTypeSpec)ts).declaration()).isException())
                {
                    h.put( name.resolvedName(), name );
                    continue; // ok
                }
                // else: go to the exception
            }
            catch( Exception ex )
            {
                // any type cast errors
                // ex.printStackTrace();
            }
            parser.fatal_error("Illegal type in raises clause: " + 
                                     name.toString(), token);
        }
        // remove duplicate exceptions, i.e. ScopedNames that, when 
        // fully qualified, point to the same exception declaration
        nameList = new Vector();
        for( Enumeration e = h.keys(); e.hasMoreElements();)
        {
            nameList.addElement( h.get( (String)e.nextElement()));
        }
        h.clear();
    }


    public void print(PrintWriter ps)
    {
        Enumeration e = nameList.elements();
        if(e.hasMoreElements())
        {
            ps.print(" throws " +  ((ScopedName)e.nextElement()));
        }
        for(; e.hasMoreElements();)
        {
            ps.print("," + ((ScopedName)e.nextElement()));
        }
    }
}

