/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

/**
 * @author Gerald Brose
 *
 * A trivial holder for scoped names that appear as truncatable ancestors
 * in a value type's inheritance spec.
 *
 * @version $Id$
 */

class Truncatable
    extends IdlSymbol
{
    ScopedName scopedName;

    public Truncatable( int num )
    {
        super( num );
    }

    public String getId()
    {
        return scopedName.id();
    }

    public void print( java.io.PrintWriter ps )
    {
        ps.print( toString() );
    }

    public String toString()
    {
        return "truncatable " + scopedName.toString();
    }
}



