package org.jacorb.test.bugs.bugjac581;


/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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


import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.portable.OutputStream;


/**
 * <code>BugJac581Test</code> verifies Union discriminators.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac581Test extends ORBTestCase
{
    /**
     * <code>testUnionHelper</code> checks that demarshalling with a boolean
     * discriminator sets it correctly.
     *
     * @exception Exception if an error occurs
     */
    public void testUnionHelper() throws Exception
    {
        BooleanUnion b = new BooleanUnion();
        b.__default (true);

        OutputStream os = orb.create_output_stream ();

        BooleanUnionHolder bHolder = new BooleanUnionHolder (b);
        bHolder._write (os);

        BooleanUnion result = BooleanUnionHelper.read (os.create_input_stream ());

        assertTrue (result.discriminator () == true);
    }
}
