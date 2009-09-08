package org.jacorb.test.orb.value;

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

import junit.framework.*;

import org.jacorb.test.common.*;
import org.omg.CORBA.*;

/**
 * Tests IDL valuetypes, especially sharing and null values.
 */
public class ValueTest extends ClientServerTestCase
{
    private ValueServer server;
    private ORB orb;

    public ValueTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = ValueServerHelper.narrow( setup.getServerObject() );
        orb = setup.getClientOrb();
    }

    protected void tearDown() throws Exception
    {
        server = null;
        orb = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "valuetype tests" );
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.value.ValueServerImpl" );

        TestUtils.addToSuite(suite, setup, ValueTest.class);

        return setup;
    }

    public void test_pass_boxed_long()
    {
        boxedLong p1 = new boxedLong(774);
        boxedLong p2 = new boxedLong(774);
        String result = server.receive_long(p1, p2);
        assertEquals("two longs: 774, 774", result);
    }

    public void test_pass_null_boxed_long()
    {
        String result = server.receive_long(null, null);
        assertEquals("one or two null values", result);
    }

    public void test_pass_shared_boxed_long()
    {
        boxedLong p1 = new boxedLong(441);
        String result = server.receive_long(p1, p1);
        assertEquals("shared long: 441", result);
    }

    /**
     * Passes two boxed longs that are equal but not shared.  This makes
     * sure that reference sharing is indeed determined based on identity,
     * not equality.  (See comments in bug 387 for discussion.)
     */
    public void test_pass_equal_boxed_long()
    {
        boxedLong p1 = new boxedLong(443);
        boxedLong p2 = new boxedLong(443);
        String result = server.receive_long(p1, p2);
        assertEquals("two longs: 443, 443", result);
    }

    public void test_pass_boxed_string()
    {
        String s1 = "hello";
        String s2 = "g'day";
        String result = server.receive_string(s1, s2);
        assertEquals("two strings: `hello', `g'day'", result);
    }

    public void test_pass_null_boxed_string()
    {
        String result = server.receive_string(null, null);
        assertEquals("one or two null values", result);
    }

    public void test_pass_shared_boxed_string()
    {
        String s1 = "hello, world";
        String result = server.receive_string(s1, s1);
        assertEquals("shared string: `hello, world'", result);
    }

    /**
     * Passes two boxed strings that are equal but not shared.  This makes
     * sure that reference sharing is indeed determined based on identity,
     * not equality.  (See comments in bug 387 for discussion.)
     */
    public void test_pass_equal_boxed_string()
    {
        String s1 = "hello, world";
        String s2 = new String(s1);
        String result = server.receive_string(s1, s2);
        assertEquals("two strings: `hello, world', `hello, world'", result);
    }

    public void test_pass_value_sequence_1()
    {
        Record[] seq = new Record[7];
        seq[0] = new RecordImpl(0, "node: 0");
        seq[1] = new RecordImpl(1, "node: 1");
        seq[2] = new RecordImpl(2, "node: 2");
        seq[3] = new RecordImpl(3, "node: 3");
        seq[4] = new RecordImpl(4, "node: 4");
        seq[5] = new RecordImpl(5, "node: 5");
        seq[6] = new RecordImpl(6, "node: 6");

        String result = server.receive_record_sequence(seq);
        assertEquals("list of length 7, null values: , no palindrome", result);
    }

    public void test_pass_value_sequence_2()
    {
        Record[] seq = new Record[7];
        seq[0] = new RecordImpl(0, "node: 0");
        seq[1] = new RecordImpl(1, "node: 1");
        seq[2] = new RecordImpl(2, "node: 2");
        seq[3] = null;
        seq[4] = null;
        seq[5] = new RecordImpl(5, "node: 5");
        seq[6] = new RecordImpl(6, "node: 6");

        String result = server.receive_record_sequence(seq);
        assertEquals("list of length 7, null values: 3 4 , no palindrome",
                     result);
    }

    public void test_pass_value_sequence_3()
    {
        Record[] seq = new Record[7];
        seq[0] = new RecordImpl(0, "node: 0");
        seq[1] = new RecordImpl(1, "node: 1");
        seq[2] = new RecordImpl(2, "node: 2");
        seq[3] = null;
        seq[4] = seq[2];
        seq[5] = seq[1];
        seq[6] = seq[0];

        String result = server.receive_record_sequence(seq);
        assertEquals("list of length 7, null values: 3 , palindrome",
                     result);
    }

    public void test_return_value_sequence()
    {
        Record[] result = server.return_record_sequence(10);
        assertEquals(10, result.length);
        for (int i=0; i<result.length; i++)
        {
            assertEquals(i, result[i].id);
            assertEquals("node: " + i, result[i].text);
        }
    }

    public void test_pass_list()
    {
        Node n1 = new NodeImpl(1);
        Node n2 = new NodeImpl(2);
        Node n3 = new NodeImpl(3);
        Node n4 = new NodeImpl(4);

        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = null;

        String result = server.receive_list(n1);
        assertEquals("list of length: 4 -- 1 2 3 4", result);
    }

    public void test_pass_list_in_any()
    {
        if (TestUtils.isJ2ME())
        {
            return;
        }

        Node n1 = new NodeImpl(1);
        Node n2 = new NodeImpl(2);
        Node n3 = new NodeImpl(3);
        Node n4 = new NodeImpl(4);

        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = null;

        Any any = setup.getClientOrb().create_any();

        NodeHelper.insert(any, n1);

        assertEquals(n1, NodeHelper.extract(any));

        String result = server.receive_list_in_any(any);
        assertEquals("list of length: 4 -- 1 2 3 4", result);
    }

    public void test_pass_circular_list()
    {
        Node n1 = new NodeImpl(1);
        Node n2 = new NodeImpl(2);
        Node n3 = new NodeImpl(3);
        Node n4 = new NodeImpl(4);

        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n1;

        String result = server.receive_list(n1);
        assertEquals("list of length: 4 -- 1 2 3 4 -- shared", result);
    }

    /**
     * <code>test_embedded_valuetype</code> ensures that JacORB can marshal
     * and unmarshal embedded valuetypes.
     */
    public void test_embedded_valuetype()
    {
        ((org.omg.CORBA_2_3.ORB)orb).register_value_factory("IDL:org/jacorb/test/orb/value/NodeData:1.0", new NodeDataDefaultFactory());
        ((org.omg.CORBA_2_3.ORB)orb).register_value_factory("IDL:org/jacorb/test/orb/value/Data:1.0", new DataDefaultFactory());

        NodeData[] n = server.getNodes();

        assertEquals("Nodes length should be two.", n.length, 2);
    }

    public void test_get_rowlistdata() throws Exception
    {
        assertNotNull(server.getData());
    }
}
