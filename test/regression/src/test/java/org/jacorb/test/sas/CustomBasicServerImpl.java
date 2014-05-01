package org.jacorb.test.sas;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import static org.junit.Assert.fail;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.InvalidSlot;

public class CustomBasicServerImpl extends BasicServerImpl
{
    private ORB orb;

    public CustomBasicServerImpl(ORB orb)
    {
        this.orb = orb;
    }

    @Override
    public void pass_in_long(int x)
    {
        Current current;
        try
        {
            current = CurrentHelper.narrow(orb.resolve_initial_references("PICurrent"));

            org.omg.CORBA.Any anyName = current.get_slot(org.jacorb.security.sas.SASInitializer.sasPrincipalNamePIC);

            if (anyName.type().kind().value() == org.omg.CORBA.TCKind._tk_null)
            {
                System.out.println("Null Name");
                fail ("Got null name");
            }
            else
            {
                String name = anyName.extract_string();
                System.out.println("printSAS for user " + name);
            }
        }
        catch (InvalidName e)
        {
            throw new RuntimeException("Caught InvalidName" + e);
        }
        catch (InvalidSlot e)
        {
            throw new RuntimeException("Caught InvalidSlot" + e);
        }
    }
}
