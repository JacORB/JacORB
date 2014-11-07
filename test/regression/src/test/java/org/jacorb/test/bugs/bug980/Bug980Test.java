package org.jacorb.test.bugs.bug980;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertFalse;
import java.util.Properties;
import org.jacorb.test.bugs.bug980.Initializer.ORBMediator;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;


/**
 * @author Nick Cross
 *
 */
public class Bug980Test extends ORBTestCase
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "ORBInit", Initializer.class.getName());
    }

    @Test
    public void testCurrent() throws Exception
    {
        ORBMediator med1 =
            (ORBMediator) orb.resolve_initial_references(ORBMediator.INITIAL_REFERENCE_ID);
        Current curr1 = CurrentHelper.narrow(orb.resolve_initial_references("PICurrent"));

        ORB orb2 = getAnotherORB(this.orbProps);
        ORBMediator med2 =
            (ORBMediator) orb2.resolve_initial_references(ORBMediator.INITIAL_REFERENCE_ID);
        Current curr2 = CurrentHelper.narrow(orb2.resolve_initial_references("PICurrent"));

        assertFalse ("ORBs should be different", orb.equals(orb2));

        assertFalse ("Mediators object should be different!", med1.equals(med2));

        assertFalse ("Currents should be different!", curr1.equals(curr2));


        int slot1 = med1.getSlot();
        Any any1 = orb.create_any();
        String info1 = "information from Current 1";
        any1.insert_string(info1);
        curr1.set_slot(slot1, any1);

        int slot2 = med2.getSlot();
        Any anyFrom2 = curr2.get_slot(slot2);

        if (anyFrom2.type().kind().value() != TCKind._tk_null)
        {
            String extractedFrom2 = anyFrom2.extract_string();
System.out.println ("### "+extractedFrom2 +" and " + info1);
            assertFalse("Contains data from ORB 1", extractedFrom2.equals(info1));
        }

    }
}
