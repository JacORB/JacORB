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

package org.jacorb.test.bugs.bugjac444;

import static org.junit.Assert.assertEquals;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ValueBaseHelper;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueBase;

/**
 * @author Alphonse Bendt
 */
public class BugJac444Test extends ORBTestCase
{
    Event evt = new EventImpl();

    @Test
    public void testEventAsAny()
    {
        Any any = orb.create_any();

        EventHelper.insert(any, evt);
        assertEquals(evt, EventHelper.extract(any));
    }

    @Test
    public void testEventAsStream()
    {
        final OutputStream out = orb.create_output_stream();
        EventHelper.write(out, evt);
        InputStream in = out.create_input_stream();
        assertEquals(evt, EventHelper.read(in));
    }

    @Test
    public void testEventBaseAsAny()
    {
        Any any = orb.create_any();

        ValueBaseHelper.insert(any, evt);
        ValueBase base = (ValueBase) ValueBaseHelper.extract(any);

        assertEquals(evt, base);
    }

    @Test
    public void testEventBaseAsStream()
    {
        OutputStream out = orb.create_output_stream();
        ValueBaseHelper.write(out, evt);
        InputStream in = out.create_input_stream();
        ValueBase base = (ValueBase) ValueBaseHelper.read(in);

        assertEquals(evt, base);
    }
}
