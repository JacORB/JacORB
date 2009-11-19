package org.jacorb.test.bugs.bugjac45;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.Properties;

import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.Any;


/**
 * <code>TestCase</code> tests The creation of large nested
 * anys. See the bug #JAC45 for more.
 *
 * @author <a href="mailto:sm@prismtechnologies.com"></a>
 * @version $Id$
 */
public class BugJac45Test extends ORBTestCase
{
    protected void patchORBProperties(Properties props)
    {
    	props.setProperty("jacorb.bufferManagerMaxFlush", "0");
    }

    /**
     * <code>testJAC45Regression</code> tests that buffer management is not
     * interfering with creation and marshalling of nested anys.
     */
    public void testJAC45Regression ()
    {
        // two anys
        Any[] anys = new Any[2];

        for (int j = 0; j < 2;  j++)
        {
            // create the any
            anys[j] = orb.create_any();

            TwoStringsStruct tSS = new TwoStringsStruct("String One", "String Two");

            TwoStringsStruct[] tSSSeq = {tSS, tSS};

            TwoStringsStruct[][] tSSSeqSeq = new TwoStringsStruct[10000][];

            for (int i = 0; i < 10000; i++)
            {
                tSSSeqSeq [i] = tSSSeq;
            }

            TwoStringsStructSeqSeqHelper.insert(anys[j], tSSSeqSeq);
        }

        TwoAnys twoAnys = new TwoAnys(anys[0], anys[1]);
        Any any = orb.create_any();

        // This is the failure point we are testing for.
        TwoAnysHelper.insert(any, twoAnys);
    }
}
