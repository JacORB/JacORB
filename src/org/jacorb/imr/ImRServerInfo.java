package org.jacorb.imr;

import java.util.*;

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;
import org.jacorb.util.Debug;

/**
 * This class contains the information about a logical server.
 * It has methods for managing the associated POAs, holding and
 * releasing the server, and, for the "client side", a method
 * that blocks until the server is released.
 *
 * @author Nicolas Noffke
 *
 * @version $Id$
 *
 */

public class ImRServerInfo  implements java.io.Serializable{
    protected String command;
    protected boolean holding = false;
    protected String host;
    protected String name;
    protected boolean active;
    protected boolean restarting = false;

    private Vector poas = null;
    private RessourceLock poas_lock = null;

    /**
     * The Constructor. It sets up the internal attributes.
     *
     * @param name the logical server name
     * @param host the name of the host on which the server should be restarted
     * (ignored when no startup command is specified).
     * @param command the startup command for this server, passed to the 
     * server startup daemon on <code>host</code> (in case there is one active).
     * @exception IllegalServerName thrown when <code>name</code> is 
     * <code>null</code> or of length zero.
     **/
    public ImRServerInfo(String name, String host, String command) 
	throws IllegalServerName{

	//super(name, host);

	if (name == null || name.length() == 0)
	    throw new IllegalServerName(name);
	
	this.name = name;
	this.host = host;
	this.command = command;
	active = false;
	poas = new Vector(); 
	poas_lock = new RessourceLock();
    }
    
    /**
     * "Converts" this Object to a <code>ServerInfo</code> instance containing
     * the same info as this object.
     *
     * @return a <code>ServerInfo</code> object.
     **/
    public ServerInfo toServerInfo(){
	poas_lock.gainExclusiveLock();

	// The ServerInfo class stores its POAs in an array, therefore
	// the vector has to copied. Because of backward compatibility
	// issues we decided not to use toArray() from the jdk1.2

	// build array
	POAInfo[] _poas = new POAInfo[poas.size()];
	Enumeration _poa_enum = poas.elements();

	// copy vector into array
	int _i = 0;
	while(_poa_enum.hasMoreElements())
	    _poas[_i++] = ((ImRPOAInfo) _poa_enum.nextElement()).toPOAInfo();

	poas_lock.releaseExclusiveLock();

	return new ServerInfo(name, command, _poas, host, active, holding);
    }

    /**
     * Adds a POA to this server.
     *
     * @param poa the POA to add.
     **/
    public void addPOA(ImRPOAInfo poa){
	if (! active)
	    active = true;

	poas_lock.gainSharedLock();
	poas.addElement(poa);
	poas_lock.releaseSharedLock();
    }

    /**
     * Builds an array of of the names of the POAs associated with this server.
     * <br> This method is needed for deleting a server since its POAs have to be
     * as well removed from the central storage.
     * @return an array of POA names
     **/
    protected String[] getPOANames(){
	// not synchronizing here since this method is only called
	// prior to destructing this object.
	String[] _poa_names = new String[poas.size()];
	Enumeration _poa_enum = poas.elements();

	int _i = 0;
	while(_poa_enum.hasMoreElements())
	    _poa_names[_i++] = ((ImRPOAInfo) _poa_enum.nextElement()).name;
	
	return _poa_names;
    }

    /**
     * Sets the server down, i.e. not active. If a request for a POA
     * of this server is received, the repository tries to restart the server.
     * <br>The server is automatically set back to active when the first of
     * its POAs gets reregistered.
     **/
    public void setDown(){
	// sets all associated to not active.
	for (int _i = 0; _i < poas.size(); _i++)
	    ((ImRPOAInfo) poas.elementAt(_i)).active = false;

	active = false;
	restarting = false;
    }

    /**
     * This method blocks until the server is released, i.e. set
     * to not holding. <br> This will not time out since holding a 
     * server is only done by administrators.
     * @param
     **/
    public synchronized void awaitRelease(){
	while(holding){
	    try{		
		wait();
	    }catch (java.lang.Exception _e){
		Debug.output(Debug.IMR | Debug.INFORMATION, _e);
	    }
	}	
    }

    /**
     * Release the server and unblock all waiting threads.
     **/
    public synchronized void release(){
	holding = false;
	notifyAll();
    }
    
    /**
     * Tests if this server should be restarted. That is the
     * case if the server is not active and nobody else is currently
     * trying to restart it. <br>
     * If true is returned the server is set to restarting. That means
     * the thread calling this method has to restart the server, 
     * otherwise it will stay down indefinetly.
     *
     * @return true, if the server should be restarted by the calling thread.
     */
    public synchronized boolean shouldBeRestarted(){
	boolean _restart = !(active || restarting);
	if (_restart)
	    restarting = true;
	    
	return _restart;
    }

    public void setNotRestarting()
    {
        restarting = false;
    }

} // ImRServerInfo

