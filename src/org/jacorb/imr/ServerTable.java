package org.jacorb.imr;

import java.util.*;
import java.io.*;
import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;
import org.jacorb.util.Environment;

/**
 * This class represents the server table of the implementation repository.
 * It contains all servers, POAs and hosts, and is serialized on shutdown,
 * deserialized on startup.
 * <br> It provides methods for adding, deleting and listing servers, 
 * POAs and hosts.
 *
 * @author Nicolas Noffke
 * 
 * $Id$
 */

public class ServerTable implements Serializable {
    private Hashtable servers;
    private transient RessourceLock servers_lock;
    private Hashtable poas;
    private transient RessourceLock poas_lock;
    private Hashtable hosts;
    private transient RessourceLock hosts_lock;

    public transient RessourceLock table_lock;

    public ServerTable() {
	int _no_of_poas = 100;
	int _no_of_servers = 5;

	try{
	    _no_of_poas = Integer.parseInt(Environment.getProperty("jacorb.imr.no_of_poas"));
	}catch (Exception _e){
	    //ignore
	}

	try{
	    _no_of_servers = Integer.parseInt(Environment.getProperty("jacorb.imr.no_of_servers"));
	}catch (Exception _e){
	    //ignore
	}

	servers = new Hashtable((int) ((100.0 / 75.0) * (double) _no_of_servers));
	poas = new Hashtable((int) ((100.0 / 75.0) * (double) _no_of_poas));
	hosts = new Hashtable();

	initTransient();
    }

    /**
     * This method initializes all transient attributes.
     **/
    private void initTransient(){
	// The table lock is a special case. It is used to gain exclusive access to the
	// server table on serialization. That mains the exclusive lock is set on 
	// serialization and, if it was not transient, would be serialized as well.
	// On startup of the repository, if the table is deserialized, the lock is still
	// set, und must be realeased. That means that we have to distinguish between 
	// a new table and a deserialized one. So its cheaper to instanciate the lock
	// on deserialization time again.
	table_lock = new RessourceLock();

	// The locks are needed, because the hashtables have to be copied to arrays sometimes
	// (usually on command of the user), and that is done via Enumerations.
	// Unfortunately Enumerations get messed up when altering the underlying structure
	// while reading from them.
	servers_lock = new RessourceLock();
	poas_lock = new RessourceLock();
	hosts_lock = new RessourceLock();
    }

    /**
     * This method gets a server for a specified name.
     *
     * @param name the servers name.
     * @return ImRServerInfo the ImRServerInfo object with name <code>name</code>.
     * @exception UnknownServerName thrown if the table does not contain 
     * an entry for <code>name</code>.
     **/
    public ImRServerInfo getServer(String name)
	throws UnknownServerName{

	ImRServerInfo _tmp = (ImRServerInfo) servers.get(name);
	if (_tmp == null)
	    throw new UnknownServerName(name);

	return _tmp;
    }

    /**
     * Adds a server to the server table.
     *
     * @param name the servers name.
     * @param server the servers corresponding ImRServerInfo object.
     * @exception DuplicateServerName thrown if <code>name</code> is already
     * in the table.
     **/
    public void putServer(String name, ImRServerInfo server)
	throws DuplicateServerName{

	if (servers.containsKey(name))
	    throw new DuplicateServerName(name);

	table_lock.gainSharedLock();
	servers_lock.gainSharedLock();

	servers.put(name, server);

	servers_lock.releaseSharedLock();
	table_lock.releaseSharedLock();
    }

    /**
     * Remove a server from the server table.
     *
     * @param name the servers name.
     * @exception UnknownServerName thrown if no server with <code>name</code>
     * is found in the table.
     **/
    public void removeServer(String name)
	throws UnknownServerName{

	table_lock.gainSharedLock();
	servers_lock.gainSharedLock();

	Object _obj = servers.remove(name);

	servers_lock.releaseSharedLock();
	table_lock.releaseSharedLock();

	if (_obj == null)
	    throw new UnknownServerName(name);
    }

    /**
     * Get the ImRPOAInfo object of a POA.
     *
     * @param name the POAs name.
     * @return the ImRPOAInfo object for <code>name</code>, 
     * null if <code>name</code> not in the table.
     **/
    public ImRPOAInfo getPOA (String name){
	return (ImRPOAInfo) poas.get(name);
    }

    /**
     * Add a POA to the server table.
     *
     * @param name the POAs name.
     * @param poa the POAs ImRPOAInfo object.
     **/
    public void putPOA(String name, ImRPOAInfo poa){
	table_lock.gainSharedLock();
	poas_lock.gainSharedLock();

	poas.put(name, poa);

	poas_lock.releaseSharedLock();
	table_lock.releaseSharedLock();
    }

    /**
     * Remove a POA from the server table.
     *
     * @param name the POAs name.
     **/
    public void removePOA(String name){
	table_lock.gainSharedLock();
	poas_lock.gainSharedLock();

	poas.remove(name);

	poas_lock.releaseSharedLock();
	table_lock.releaseSharedLock();
    }

    /**
     * List all servers in the table.
     *
     * @return a ServerInfo array containing all servers.
     * Used by the CORBA interface of the repository.
     **/
    public ServerInfo[] getServers(){
	table_lock.gainSharedLock();
	servers_lock.gainExclusiveLock();

	//build array
	ServerInfo[] _servers = new ServerInfo[servers.size()];
	Enumeration _server_enum = servers.elements();

	//copy elements from vector to array
	int _i = 0;
	while (_server_enum.hasMoreElements())
	    _servers[_i++] = ((ImRServerInfo) _server_enum.nextElement()).toServerInfo();

	servers_lock.releaseExclusiveLock();
	table_lock.releaseSharedLock();

	return _servers;
    }

    /**
     * List all hosts in the table.
     *
     * @return a HostInfo array containing all hosts.
     * Used by the CORBA interface of the repository.
     **/
    public HostInfo[] getHosts(){
	table_lock.gainSharedLock();
	hosts_lock.gainExclusiveLock();

	//build array
	HostInfo[] _hosts = new HostInfo[hosts.size()];
	Enumeration _host_enum = hosts.elements();
	
	//copy elements from vector to array
	int _i = 0;
	while (_host_enum.hasMoreElements())
	    _hosts[_i++] = ((ImRHostInfo) _host_enum.nextElement()).toHostInfo();
	
	hosts_lock.releaseExclusiveLock();
	table_lock.releaseSharedLock();

	return _hosts;
    }

    /**
     * List all POAs in the table.
     *
     * @return a POAInfo array containing all POAs.
     * Used by the CORBA interface of the repository.
     **/
    public POAInfo[] getPOAs(){
	table_lock.gainSharedLock();
	poas_lock.gainExclusiveLock();

	//build array
	POAInfo[] _poas = new POAInfo[poas.size()];
	Enumeration _poa_enum = poas.elements();

	//copy elements from vector to array
	int _i = 0;
	while (_poa_enum.hasMoreElements())
	    _poas[_i++] = ((ImRPOAInfo) _poa_enum.nextElement()).toPOAInfo();
	
	poas_lock.releaseExclusiveLock();
	table_lock.releaseSharedLock();

	return _poas;
    }

    /**
     * Add a host to the table. If an entry for <code>name</code> is already
     * in the table it is overwritten.
     *
     * @param name the hosts name.
     * @param host the hosts ImRHostInfo object.
     **/
    public void putHost(String name, ImRHostInfo host){
	table_lock.gainSharedLock();
	hosts_lock.gainSharedLock();

	hosts.put(name, host);

	hosts_lock.releaseSharedLock();
	table_lock.releaseSharedLock();
    }

    /**
     * Remove a host from the table.
     *
     * @param name the hosts name.
     **/
    public Object removeHost(String name){
	return hosts.remove(name);
    }

    /**
     * Get the ImRHostInfo object of a host.
     *
     * @param name the hosts name.
     * @return the ImRHostInfo object for <code>name</code>, null 
     * if <code>name</code> not in the table.
     **/
    public ImRHostInfo getHost(String name){
	return (ImRHostInfo) hosts.get(name);
    }

    /**
     * Implemented from the Serializable interface. For 
     * automatic initializing after deserialization.
     **/
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, java.io.NotActiveException, 
	ClassNotFoundException{

	in.defaultReadObject();

	initTransient();
    }
} // ServerTable








