package org.jacorb.imr;

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;

import org.jacorb.util.*;
/**
 * This class stores information about a POA. It also provides methods 
 * for reactivation, conversion, and for waiting for reactivation.
 *
 * @author Nicolas Noffke
 * 
 * @version $Id$
 */

public class ImRPOAInfo implements java.io.Serializable{
    protected int port;
    protected ImRServerInfo server;
    protected String host;
    protected String name;
    protected boolean active;
    protected static long timeout = 120000; // 2 min.

    static{
	// read in timeout from Environment
	String _tmp = Environment.getProperty("jacorb.imr.timeout");
	if (_tmp != null)
	    try{
		timeout = Integer.parseInt(_tmp);
	    }catch(Exception _e){}
    }
    
    /**
     * The constructor of this class.
     *
     * @param name the POAs name.
     * @param host the POAs host.
     * @param port the port the POA listens on.
     * @param server the server the POA is associated with.
     * @exception IllegalPOAName thrown when <code>name</code> is 
     * <code>null</code> or of length zero.
     **/
    public ImRPOAInfo(String name, String host, 
                      int port, ImRServerInfo server) 
	throws IllegalPOAName {

	if (name == null || name.length() == 0)
	    throw new IllegalPOAName(name);

	this.name = name;
	this.host = host;
	this.port = port;
	this.server = server;
	this.active = true;
    }

    /**
     * "Converts" this Object to an instance of the POAInfo class.
     * 
     * @return a POAInfo object.
     **/    
    public POAInfo toPOAInfo(){
	return new POAInfo(name, host, port, 
                           server.name, active); 
    }

    /**
     * Reactivates this POA, i.e. sets it to active and unblocks any 
     * waiting threads.
     *
     * @param host the POAs new host.
     * @param port the POAs new port.
     **/
    public synchronized void reactivate(String host, int port){
	this.host = host;
	this.port = port;
	active = true;	
	server.active = true;
	server.restarting = false;
	notifyAll();
    }

    /**
     * This method blocks until the POA is reactivated, or the
     * timeout is exceeded.
     * 
     * @return false, if the timeout has been exceeded, true otherwise.
     **/        
    public synchronized boolean awaitActivation(){
	while(!active){
	    try{
		long _sleep_begin = System.currentTimeMillis();
		wait(timeout);
		if (!active && 
                    (System.currentTimeMillis() - _sleep_begin) > timeout)
		{
		    Debug.output(Debug.IMR | Debug.INFORMATION,
                                 "awaitActivation, time_out");
		    return false;
		}
	    }catch (java.lang.Exception _e){
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }
	}
        Debug.output(Debug.IMR | Debug.INFORMATION, 
                     "awaitActivation, returns true");

	return true;
    }
} // ImRPOAInfo


