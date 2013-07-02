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

package org.jacorb.test.bugs.bugjac445;

import java.util.Properties;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.ORBTestCase;

/**
 * @author Alphonse Bendt
 */
public class BugJac445Test extends ORBTestCase
{
    private A a;
    private B b;
    private First first;
    private Second second;
    private Both both;

    protected void doSetUp()
    {
        a = new A();
        a.value = 10;

        b = new B();
        b.a_struct = a;
        b.value = 20;

        first = new First();

        first.b_struct = b;

        second = new Second();

        second.a_struct = a;
        second.b_struct = b;

        both = new Both();
        both.first_any = orb.create_any();
        both.second_any = orb.create_any();

        FirstHelper.insert(both.first_any, first);
        SecondHelper.insert(both.second_any, second);
    }

    protected void patchORBProperties(String testName, Properties props)
    {
        props.setProperty("jacorb.interop.indirection_encoding_disable", "off");
        props.setProperty("jacorb.cacheTypecodes", "on");
    }

    public void testCacheOverMemberBoundaries() throws Exception
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();

        BothHelper.write(out, both);

        CDRInputStream in = new CDRInputStream(orb, out.getBufferCopy());
        Both copy = BothHelper.read(in);

        FirstHelper.extract(copy.first_any);
        SecondHelper.extract(copy.second_any);

        assertTrue(FirstHelper.type().equivalent(copy.first_any.type()));
        assertTrue(SecondHelper.type().equivalent(copy.second_any.type()));
    }

    public void testCacheRecursiveOverMemberBoundaries() throws Exception
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();

        RecursiveA recA = new RecursiveA();
        recA.value = 1234;
        recA.member = new RecursiveA[0];

        RecursiveB recB = new RecursiveB();
        recB.a_struct = recA;
        recB.value = 2000;

        RecursiveC recC = new RecursiveC();
        recC.a_struct = recA;
        recC.b_struct = recB;

        RecursiveBHelper.insert(both.first_any, recB);
        RecursiveCHelper.insert(both.second_any, recC);

        BothHelper.write(out, both);

        CDRInputStream in = new CDRInputStream(orb, out.getBufferCopy());
        Both copy = BothHelper.read(in);

        assertTrue(RecursiveBHelper.type().equivalent(copy.first_any.type()));
        assertTrue(RecursiveCHelper.type().equivalent(copy.second_any.type()));
    }
}
