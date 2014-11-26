package org.jacorb.test.orb;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.jacorb.test.LongLongSeqServer;
import org.jacorb.test.LongLongSeqServerHelper;
import org.jacorb.test.LongLongSeqServerPackage.SeqLongLongHolder;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LongLongSeq extends ClientServerTestCase
{
    private LongLongSeqServer server;

    private static void test( long[] arg )
    {
        assertEquals( arg[0], Long.MIN_VALUE );
        assertEquals( arg[1], Long.MIN_VALUE );
    }

    @Before
    public void setUp() throws Exception
    {
        server = LongLongSeqServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup ("org.jacorb.test.orb.LongLongSeqServerImpl" );
    }

    @Test
    public void test_longlong()
    {
        long[] l = new long[]{ Long.MIN_VALUE, Long.MIN_VALUE };

        SeqLongLongHolder h_out = new SeqLongLongHolder();
        SeqLongLongHolder h_inout = new SeqLongLongHolder();

        h_inout.value = l;

        try
        {
            for( int i = 0; i < 1000; i++ )
            {
                test( server.test1( l, h_out, h_inout ));
                test( h_out.value );
                test( h_inout.value );

                test( server.test2( l, h_out ));
                test( h_out.value );

                server.test3( h_inout );
                test( h_inout.value );
            }
        }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }
}
