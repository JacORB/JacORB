package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

/**
 *  Unit Test for class ApplicationContext
 *
 *
 * Created: Wed Jun  4 18:41:17 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ApplicationContextTest extends TestCase 
{

    /** 
     * Creates a new <code>ApplicationContextTest</code> instance.
     *
     * @param name test name
     */
public ApplicationContextTest (String name)
    {
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite()
    {
	TestSuite suite = new TestSuite(ApplicationContextTest.class);
	
	return suite;
    }

    public void testB() {
	final ORB _orb = ORB.init( new String[0], null );

	
	Thread t = new Thread(
			      new Runnable() {
				  public void run() {
				      _orb.run();
				  }});
	
	t.setDaemon(true);
	t.start();

	Any a = _orb.create_any();

	assertNotNull(a);
    }

    /** 
     * Entry point 
     */ 
public static void main(String[] args) 
    {
	junit.textui.TestRunner.run(suite());
    }
}// ApplicationContextTest
