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

/**
 * @author Gerald Brose
 * @version $Id$
 *
 */

package org.jacorb.idl;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

class MemberList
    extends SymbolList
{
    Vector extendVector = new Vector();
    private TypeDeclaration containingType;
    private boolean parsed = false;

    public MemberList(int num)
    {
        super(num);
    }

    public void setContainingType (TypeDeclaration t)
    {
        containingType = t;
        Enumeration e = v.elements();
        for(; e.hasMoreElements(); )
        {
            Member m = (Member)e.nextElement();
            m.setContainingType (t);
        }
    }

    public void parse()          
    {
        if( parsed)
            throw new RuntimeException("Compiler error: MemberList already parsed!");

        Enumeration e = v.elements();
        for(; e.hasMoreElements(); )
        {
            Member m = (Member)e.nextElement();
            m.setExtendVector( extendVector );
            m.parse();
        }

        /* after all members are parsed, we have accumulated
           a new member list in "normal form" in extendVector
        */

        v = extendVector;
        parsed = true;

    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            throw new RuntimeException("Compiler Error: trying to reassign container");
        }

        enclosing_symbol = s;

        for(Enumeration e = v.elements(); e.hasMoreElements(); )
        {
            Member m = (Member)e.nextElement();
            m.setEnclosingSymbol(s);
        }
    }

}

