package org.jacorb.orb.connection;

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


import java.io.*;
import java.lang.*;
import org.omg.CORBA.ORB;

/**
 * @author Gerald Brose, FU Berlin 1996
 * $Id$	
 *
 * An object of this class is created for every connection and listens
 * for replies.
 */

public class ReplyReceptor 
    extends Thread 
{ 
    private Boolean stopped = new Boolean(false);
    private Boolean onceOnly = new Boolean(false); 
    // For HTTP connections.  Keep separate from stopped since stopped 
    // is checked by exception handlers.
    
    ClientConnection master;

    public ReplyReceptor( ClientConnection _master )
    {
	master = _master;
	try
	{
	    setDaemon( true );
	}
		
	catch( java.lang.SecurityException s)
	{
	    org.jacorb.util.Debug.output(3, 
               "Could not start reply receptor as daemon, running as a regular thread.");
	}
	this.setName("JacORB ReplyReceptor Thread for connection " + master.getInfo() );
	start();
    }

    public ReplyReceptor( ClientConnection _master, boolean one ) 
    {
        this(_master);
        oneRun(one);
    }


    public void stopReceptor()
    {
        synchronized( stopped ) {
            stopped = new Boolean( true );
            interrupt();
        }
    }


    private void oneRun(boolean one) 
    { 
        // Gracefully let receptor exit loop and end after receive loop.
        synchronized(onceOnly) 
        {
	    onceOnly = new Boolean(one);
        }
	//	org.jacorb.util.Debug.output(4,"Reply Receptor thread flagged to end!");
    }

    public boolean isStopped() 
    {
        return( stopped.booleanValue() );
    }

    public void run() 
    {	
        while( !isStopped() ) 
        {
	    try 
            {
                master.receiveReply();
	    }
            catch ( CloseConnectionException c ) 
            {
	    	org.jacorb.util.Debug.output(3, "ReplyReceptor: CloseConnectionException");
	    	master._closeConnection();
                break;
	    }
            catch ( java.io.InterruptedIOException ioint ) 
            {
	       	// when the receptor is interrupted from within
		// Connection.closeConnection
                if( !isStopped())
                    org.jacorb.util.Debug.output(3,ioint);
                break;
	    }
            catch ( IOException e ) 
            {
                if( !isStopped() )
                {
                    org.jacorb.util.Debug.output(3,e);
                    master._closeConnection();
                }
                break;
	    }
            catch ( Exception ie ) 
            {
                org.jacorb.util.Debug.output(3,ie);
	    }
	    if ( onceOnly.booleanValue() == true )
            {
                break;
	    }
        }
    }
}




