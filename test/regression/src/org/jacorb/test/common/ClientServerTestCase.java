package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

/**
 * A special <code>TestCase</code> that provides access to a 
 * <code>ClientServerSetup</code>.  For information how to wrap
 * a <code>ClientServerSetup</code> around a suite of 
 * <code>ClientServerTestCase</code>s, see the class comment of
 * {@link ClientServerSetup}.
 * <p>
 * Each individual test case can access the server object by calling
 * <code>setup.getServerObject()</code>.  However, this returns
 * a generic CORBA Object.  It is usually more convenient to narrow
 * it to the desired type automatically, which can be done by overriding
 * the <code>setUp</code> method:
 * 
 * <p><blockquote><pre>
 * public class MyTest extends ClientServerTestCase
 * {
 *     protected MyServer server;
 * 
 *     public void setUp() throws Exception
 *     {
 *         server = MyServerHelper.narrow ( setup.getServerObject() );
 *     }
 * 
 *     ...
 * }
 * </pre></blockquote><p>
 * 
 * This way, each individual test case can simply use the 
 * <code>server</code> instance variable to access the server
 * object with correct type information.
 * 
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class ClientServerTestCase extends TestCase
{
    protected ClientServerSetup setup;

    public ClientServerTestCase( String name, ClientServerSetup setup )
    {
        super( name );
        this.setup = setup;
    }

}
