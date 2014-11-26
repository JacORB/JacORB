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

package org.jacorb.test.bugs.bugjac440;

import java.util.Properties;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.INITIALIZE;

/**
 * @author Alphonse Bendt
 */
public class BugJac440Test extends ORBTestCase
{
    @Test (expected=INITIALIZE.class)
    public void testInvokeSetSlotDuringORBInitFails() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.test", ORBInitializer.class.getName());
        props.setProperty("jacorb.orb_initializer.fail_on_error", "on");

        getAnotherORB(props);
    }
}
