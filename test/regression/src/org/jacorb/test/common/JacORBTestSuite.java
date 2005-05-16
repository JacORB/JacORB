package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
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

import java.lang.reflect.*;

import junit.framework.*;
import junit.extensions.*;

/**
 * A special TestSuite that accepts only Tests that are applicable
 * to a given client and server version.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class JacORBTestSuite extends TestSuite implements JacORBTest
{
    private TestAnnotations annotations = null;
    
    private String clientVersion = null;
    private String serverVersion = null;

    /**
     * Constructs a JacORBTestSuite for a given name, using the 
     * annotatedClass to find test annotations.  This constructor
     * is for stand-alone use of this class, e.g. as part of a TestCase.
     */
    public JacORBTestSuite (String name,
                            Class annotatedClass)
    {
        super (name);
        clientVersion = System.getProperty ("jacorb.test.client.version", "cvs");
        serverVersion = System.getProperty ("jacorb.test.server.version", "cvs");
        if (!clientVersion.equals("cvs") || !serverVersion.equals("cvs"))
        {
            annotations = TestAnnotations.forClass (annotatedClass);
        }
    }
                            
    /**
     * Constructs a JacORBTestSuite for the given name.  This constructor
     * may only be used by subclasses, because the test annotations will
     * be parsed from the defining class.
     */
    protected JacORBTestSuite (String name)
    {
        super (name);
        clientVersion = System.getProperty ("jacorb.test.client.version", "cvs");
        serverVersion = System.getProperty ("jacorb.test.server.version", "cvs");
        if (!clientVersion.equals("cvs") || !serverVersion.equals("cvs"))
        {
            annotations = TestAnnotations.forTestSuite (this);
        }
    }
    
    public boolean isApplicableTo (String clientVersion, String serverVersion)
    {
        if (annotations == null)
            return true;
        else
        {
            boolean result = annotations.isApplicableTo (clientVersion,
                                                         serverVersion);
            if (!result) System.out.println ("not applicable: " + getName());
            return result;
        }
    }
    
    /**
     * Adds a test to this suite, but if it's a JacORB test, it is
     * only added if it is applicable to the currently tested client
     * and server versions.
     */
    public void addTest (Test test)
    {
        if (clientVersion == null && serverVersion == null)
        {
            super.addTest (test);
        }
        else if (test instanceof JacORBTest)
        {
            if (((JacORBTest)test).isApplicableTo (clientVersion,
                                                   serverVersion))
                super.addTest (test);
        }
        else if (test instanceof TestDecorator)
        {
            TestDecorator decorator = (TestDecorator)test;
            Test t = decorator.getTest();
            if (t instanceof JacORBTest)
            {
                if (((JacORBTest)t).isApplicableTo (clientVersion,
                                                    serverVersion))
                    super.addTest (test);
            }
            else
                super.addTest (test);
        }
        else
        {
            super.addTest (test);
        }
    }
    
    /**
     * Adds the TestSuite defined by class c to this JacORBTestSuite,
     * but only if the TestSuite is applicable to the client and server
     * version under test.  The new TestSuite is computed by invoking the
     * suite() method of class c -- this is different from what the superclass
     * implementation of this method does, but it doesn't cause problems in
     * practice.  The advantage of invoking suite() here is that for
     * non-applicable tests, the suite() method is never invoked and so the
     * corresponding classes needn't be loaded.  
     */
    public void addTestSuite (Class c)
    {
        if (JacORBTest.class.isAssignableFrom(c))
        {
            TestAnnotationsParser p = TestAnnotationsParser.getInstance(c);
            TestAnnotations ta = p.getClassAnnotations();
            if (ta == null || ta.isApplicableTo (clientVersion, serverVersion))
            {
                TestSuite ts = invokeSuiteMethod (c);
                super.addTest (ts);
            }
        }
        else
        {
            super.addTestSuite(c);
        }
    }

    /**
     * Invokes the static suite() method of the class c, and returns the
     * result.  If there is no suite() method, or the result of it is not
     * a TestSuite, this method throws a RuntimeException.
     */
    private TestSuite invokeSuiteMethod (Class c)
    {
        try
        {
            Method suiteMethod = c.getDeclaredMethod ("suite",
                                                      new Class[]{});
            Object result = suiteMethod.invoke(null, new Object[]{});
            return (TestSuite)result;
        }
        catch (Exception ex)
        {
            throw new RuntimeException (ex);
        }
    }
    
}
