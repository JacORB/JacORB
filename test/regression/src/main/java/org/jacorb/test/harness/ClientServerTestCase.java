package org.jacorb.test.harness;

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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * An abstract class for those tests that need a client/server test.
 * It provides access to a <b>static</b> ClientServerSetup that may
 * be initialised via a {@literal @}BeforeClass annotation. It is automatically
 * shutdown via an {@literal @}AfterClass annotation.
 * <p></p>
 * Each individual test case can access the server object by calling
 * <code>setup.getServerObject()</code>.  However, this returns
 * a generic CORBA Object.  It is usually more convenient to narrow
 * it to the desired type automatically, which can be done in a method
 * annotated with {@literal @}Before e.g.
 *
 * <pre>
 * <code>
 * public class MyTest extends ClientServerTestCase
 * {
 *    protected MyServer server;
 *
 *    {@literal @}Before
 *    public void setUp() throws Exception
 *    {
 *        server = BasicServerHelper.narrow( setup.getServerObject() );
 *    }
 *     ...
 * }
 * </code></pre>
 *
 * This way, each individual test case can simply use the
 * <code>server</code> instance variable to access the server
 * object with correct type information.
 *
 * @author Andre Spiegel &lt;spiegel@gnu.org&gt;
 * @author Nick Cross
 */
@Category(ClientServerCategory.class)
public abstract class ClientServerTestCase
{
    protected static ClientServerSetup setup;

    @Rule
    public TestName name = new TestName();

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(Description description)
        {
            TestUtils.getLogger().debug("Starting test: {}:{}", description.getClassName(), description.getMethodName());
        }
    };

    /**
     * <code>tearDownAfterClass</code> will automatically tear down the server
     * if it has been created.
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        if ( setup != null)
        {
            setup.tearDown();
        }
    }
}
