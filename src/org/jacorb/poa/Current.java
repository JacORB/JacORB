package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
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

import org.jacorb.poa.except.*;
 
import org.omg.PortableServer.CurrentPackage.NoContext;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This class provides access to the identity of the object on which a method 
 * was invoked and the responsible POA for this object.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.03, 21/01/00, RT
 */

public class Current 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements org.omg.PortableServer.Current 
{
	private Hashtable threadTable = new Hashtable(); // Thread -> vector of InvocationContext elements (Stack)

    private Current() 
    {
    }

    public static Current _Current_init() 
    {
        return new Current();
    }

	synchronized public void _addContext(Thread t, InvocationContext c) 
	{		    
		Vector cv = (Vector) threadTable.get(t);

		if (cv == null) {
			cv = new Vector();
			threadTable.put(t, cv);
		}
		
		cv.addElement(c);
	}
	    
	synchronized public void _removeContext(Thread t) 
	{
		Vector cv = (Vector) threadTable.get(t);

		if (cv != null) {
			
			cv.removeElementAt(cv.size()-1);
			
			if (cv.size() == 0) {
				threadTable.remove(t);
			}
		}
	}
	
    public byte[] get_object_id() 
        throws NoContext 
    {
        return getInvocationContext().getObjectId();
    }

    public org.omg.PortableServer.POA get_POA() 
        throws NoContext 
    {
        return getInvocationContext().getPOA();
    }
    
	synchronized private InvocationContext getInvocationContext() 
		throws NoContext 
	{
		Thread ct = Thread.currentThread();

		Vector cv = (Vector) threadTable.get(ct);

		if (cv != null) {
			
			InvocationContext c = (InvocationContext) cv.lastElement();

			if (c != null) {
				return c;
			}
		}

		throw new NoContext();
	}
	    
    protected org.omg.CORBA.ORB getORB() 
        throws NoContext 
    {
        return getInvocationContext().getORB();
    }

    protected org.omg.PortableServer.Servant getServant() 
        throws NoContext 
    {
        return getInvocationContext().getServant();
    }
    
    /*
    public org.jacorb.orb.connection.Connection getConnection()
	throws NoContext
    {
	return ((RequestProcessor)getInvocationContext()).getConnection();
    }
    */
}




