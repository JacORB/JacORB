package org.jacorb.test.poa;

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

import static org.junit.Assert.fail;
import org.jacorb.poa.util.ByteArrayKey;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;


/**
 * <code>ByteArrayKeyTest</code> tests JacORB ByteArrayKey class.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class ByteArrayKeyTest extends ORBTestCase
{
    /**
     * <code>testKey1</code> tests that JacORB can handle a null key.
     */
    @Test
    public void testKey1 ()
    {
        ByteArrayKey bk = new ByteArrayKey( (byte[])null );

        try
        {
            bk.toString();
            bk.getBytes();
            bk.hashCode();

            if( System.identityHashCode( bk.toString() ) !=
                System.identityHashCode( bk.toString() )  )
            {
                fail( "Different Strings returned on toString" );
            }
        }
        catch( Exception e )
        {
            fail( "Caught exception processing ByteArrayKey" );
        }
    }


    /**
     * <code>testKey2</code> does some basic tests.
     */
    @Test
    public void testKey2 ()
    {
        ByteArrayKey bk = new ByteArrayKey( ( "bytearraykeytest" ).getBytes() );

        try
        {
            bk.toString();
            bk.getBytes();
            bk.hashCode();

            if( System.identityHashCode( bk.toString() ) !=
                System.identityHashCode( bk.toString() )  )
            {
                fail( "Different Strings returned on toString" );
            }
        }
        catch( Exception e )
        {
            fail( "Caught exception processing ByteArrayKey" );
        }
    }
}
