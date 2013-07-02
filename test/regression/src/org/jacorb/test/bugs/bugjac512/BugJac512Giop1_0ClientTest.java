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

package org.jacorb.test.bugs.bugjac512;

import java.util.Properties;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.ReplyOutputStream;
import org.jacorb.test.common.MyNullLogger;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.MARSHAL;
import org.omg.GIOP.ReplyStatusType_1_2;

/**
 * @author Alphonse Bendt
 */
public class BugJac512Giop1_0ClientTest extends ORBTestCase
{
    protected void patchORBProperties(String testName, Properties props) throws Exception
    {
        props.setProperty("jacorb.giop_minor_version", "0");
    }

    public void testDoesNotLikeWCharWithinReply()
    {
        ReplyOutputStream out = new ReplyOutputStream(orb, 1, ReplyStatusType_1_2.NO_EXCEPTION, 0, false, new MyNullLogger());

        out.write_wchar('a');

        ReplyInputStream in = new ReplyInputStream(orb, out.getBufferCopy());

        assertEquals(0, in.getGIOPMinor());

        try
        {
            in.read_wchar();
            fail();
        }
        catch(MARSHAL e)
        {
            assertEquals(6, e.minor);
        }
    }

    public void testServerDoesNotLikeWString()
    {
        ReplyOutputStream out = new ReplyOutputStream(orb, 1, ReplyStatusType_1_2.NO_EXCEPTION, 0, false, new MyNullLogger());

        out.write_wstring("string");

        ReplyInputStream in = new ReplyInputStream(orb, out.getBufferCopy());

        assertEquals(0, in.getGIOPMinor());

        try
        {
            in.read_wstring();
            fail();
        }
        catch(MARSHAL e)
        {
            assertEquals(6, e.minor);
        }
    }
}
