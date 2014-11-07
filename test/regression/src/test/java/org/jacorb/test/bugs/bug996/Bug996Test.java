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

package org.jacorb.test.bugs.bug996;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nick Cross
 */
public class Bug996Test extends ORBTestCase
{
    private CDROutputStream out;
    private CDRInputStream in;

    @Before
    public void setUp() throws Exception
    {
        out = (CDROutputStream) orb.create_output_stream();
        out.write_fixed(new BigDecimal("0"), (short)1, (short)0);

        in = (CDRInputStream) out.create_input_stream();
    }

    @Test
    public void testReadFixed()
    {
        assertEquals(new BigDecimal("0"), in.read_fixed((short)1, (short)0));
    }
}
