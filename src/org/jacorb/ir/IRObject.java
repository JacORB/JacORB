package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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


import org.omg.CORBA.INTF_REPOS;

/**
 * Base class for Interface Repository Objects
 */

public abstract class IRObject
    implements org.omg.CORBA.IRObjectOperations
{
    protected org.omg.CORBA.Object myRef;
    protected org.omg.CORBA.ORB orb;
    protected org.omg.CORBA.DefinitionKind def_kind;
    protected String name;

    public org.omg.CORBA.DefinitionKind def_kind()
    {
        return def_kind;
    }

    public String getName()
    {
        return name;
    }

    public abstract void destroy();

    /**
     * second phase of loading IRObjects, define any unresolved links
     */
    abstract void define();

    public org.omg.CORBA.Object getReference()
    {
        if( myRef == null )
        {
            throw new INTF_REPOS ("Reference undefined!");
        }
        return myRef;
    }

    public void setReference(org.omg.CORBA.Object ref)
    {
        myRef = ref;
        orb = ((org.omg.CORBA.portable.ObjectImpl)myRef)._orb();
    }

}
