package org.jacorb.test.bugs.bug957;

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

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;


public class BasicServerImpl extends org.jacorb.test.orb.BasicServerImpl
{
    private org.omg.CORBA.ORB orb;
    private boolean calledPrintSAS = false;

    public BasicServerImpl (ORB orb)
    {
        this.orb = orb;
    }


    public void ping()
    {
        try {
            org.omg.PortableInterceptor.Current current = (org.omg.PortableInterceptor.Current)orb.resolve_initial_references("PICurrent");
            org.omg.CORBA.Any anyName = current.get_slot(org.jacorb.security.sas.SASInitializer.sasPrincipalNamePIC);

            if( anyName.type().kind().value() == org.omg.CORBA.TCKind._tk_null )
            {
                System.out.println("Null Name");
            }
            else
            {
                String name = anyName.extract_string();
                System.out.println("printSAS for user " + name);
            }
        }
        catch (Exception e)
        {
            throw new INTERNAL ("Caught e" + e);
        }

        if (!calledPrintSAS)
        {
            calledPrintSAS = true;
            _this().ping();
        }
    }
}
