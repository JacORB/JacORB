package org.jacorb.imr;

/*
 *        JacORB - a free Java ORB
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import org.jacorb.imr.AdminPackage.DuplicateServerName;

/**
 * This class represents the server table of the implementation repository.
 * It contains all servers, POAs and hosts, and is serialized on shutdown,
 * deserialized on startup.
 * <br> It provides methods for adding, deleting and listing servers, 
 * POAs and hosts.
 *
 * @author Nicolas Noffke
 * 
 */

public class ServerTable 
    implements Serializable 
{
    private Hashtable servers;
    private transient ResourceLock servers_lock;
    private Hashtable poas;
    private transient ResourceLock poas_lock;
    private Hashtable hosts;
    private transient ResourceLock hosts_lock;

    public transient ResourceLock table_lock;

    public ServerTable() 
    {
        servers = new Hashtable();
        poas = new Hashtable();
        hosts = new Hashtable();

        initTransient();
    }

    /**
     * This method initializes all transient attributes.
     */

    private void initTransient()
    {
        // The table lock is a special case. It is used to gain
        // exclusive access to the server table on serialization. That
        // means the exclusive lock is set on serialization and, if it
        // was not transient, would be serialized as well.  On startup
        // of the repository, if the table is deserialized, the lock
        // is still set, und must be realeased. That means that we
        // have to distinguish between a new table and a deserialized
        // one. So its cheaper to instanciate the lock on
        // deserialization time again.
        table_lock = new ResourceLock();

        // The locks are needed, because the hashtables have to be
        // copied to arrays sometimes (usually on command of the
        // user), and that is done via Enumerations.  Unfortunately
        // Enumerations get messed up when altering the underlying
        // structure while reading from them.
        servers_lock = new ResourceLock();
        poas_lock = new ResourceLock();
        hosts_lock = new ResourceLock();
    }

    /**
     * This method tests, if a server is known.
     *
     * @param name the servers name.
     * @return true, if a server with the specified name has already
     * been registered.
     */

    public boolean hasServer( String name )
    {
        return servers.containsKey(name);
    }


    /**
     * This method gets a server for a specified name.
     *
     * @param name the servers name.
     * @return ImRServerInfo the ImRServerInfo object with name <code>name</code>.
     * @exception UnknownServerName thrown if the table does not contain 
     * an entry for <code>name</code>.
     */

    public ImRServerInfo getServer(String name)
        throws UnknownServerName
    {
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
     */

    public void putServer(String name, ImRServerInfo server)
        throws DuplicateServerName
    {
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
     */

    public void removeServer(String name)
        throws UnknownServerName
    {
        table_lock.gainSharedLock();
        servers_lock.gainSharedLock();

        Object _obj = servers.remove(name);

        servers_lock.releaseSharedLock();
        table_lock.releaseSharedLock();

        if (_obj == null)
            throw new UnknownServerName(name);
    }

    public boolean poa_enp_reused (String name, String host, int port)
    {   
        POAInfo[] poas = getPOAs();
        for (int i = 0; i < poas.length; i++)
        {
          if (poas[i].name.equals (name))
            continue;
            
          if (poas[i].host.equals (host) && poas[i].port == port && poas[i].active == true)
            return true;
        }
       
        return false;
    }

    /**
     * Get the ImRPOAInfo object of a POA.
     *
     * @param name the POAs name.
     * @return the ImRPOAInfo object for <code>name</code>, 
     * null if <code>name</code> not in the table.
     */

    public ImRPOAInfo getPOA (String name)
    {
        return (ImRPOAInfo) poas.get(name);
    }

    /**
     * Add a POA to the server table.
     *
     * @param name the POAs name.
     * @param poa the POAs ImRPOAInfo object.
     */

    public void putPOA(String name, ImRPOAInfo poa)
    {
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
     */

    public void removePOA(String name)
    {
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
     */

    public ServerInfo[] getServers()
    {
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
     */

    public HostInfo[] getHosts()
    {
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
     */

    public POAInfo[] getPOAs()
    {
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
     */

    public void putHost(String name, ImRHostInfo host)
    {
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
     */

    public Object removeHost(String name)
    {
        return hosts.remove(name);
    }

    /**
     * Get the ImRHostInfo object of a host.
     *
     * @param name the hosts name.
     * @return the ImRHostInfo object for <code>name</code>, null 
     * if <code>name</code> not in the table.
     */

    public ImRHostInfo getHost(String name)
    {
        return (ImRHostInfo) hosts.get(name);
    }

    /**
     * Implemented from the Serializable interface. For 
     * automatic initializing after deserialization.
     */

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, java.io.NotActiveException, 
        ClassNotFoundException
    {
        in.defaultReadObject();
        initTransient();
    }
} // ServerTable


