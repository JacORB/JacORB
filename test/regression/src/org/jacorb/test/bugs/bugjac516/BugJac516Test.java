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

package org.jacorb.test.bugs.bugjac516;

import java.math.BigDecimal;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;

/**
 * @author Alphonse Bendt
 */
public class BugJac516Test extends ORBTestCase
{
    private CDROutputStream out;
    private CDRInputStream in;

    protected void doSetUp()
    {
        out = (CDROutputStream) orb.create_output_stream();
        out.write_fixed(new BigDecimal("432.1"), (short)4, (short)1);

        in = (CDRInputStream) out.create_input_stream();
    }

    public void testReadFixedWithWrongDigitsFails1()
    {
        try
        {
            in.read_fixed((short)3, (short)1);
            fail();
        }
        catch(MARSHAL e)
        {
        }
    }

    public void testReadFixedScaleBelowZeroShouldFail()
    {
        try
        {
            in.read_fixed((short)4, (short)-1);
            fail();
        }
        catch(BAD_PARAM e)
        {
        }
    }

    public void testReadFixed1()
    {
        assertEquals(new BigDecimal("432.1"), in.read_fixed((short)4, (short)1));
    }

    public void testReadFixed2()
    {
        assertEquals(new BigDecimal("43.21"), in.read_fixed((short)4, (short)2));
    }

    public void testReadFixed3()
    {
        assertEquals(new BigDecimal("4.321"), in.read_fixed((short)4, (short)3));
    }

    public void testReadFixed4()
    {
        assertEquals(new BigDecimal(".4321"), in.read_fixed((short)4, (short)4));
    }

    public void testReadFixedNegative1()
    {
        try
        {
            in.read_fixed((short)4, (short)-1);
            fail();
        }
        catch(BAD_PARAM e)
        {
        }
    }
}
