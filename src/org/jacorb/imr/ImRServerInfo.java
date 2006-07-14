/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.imr;

import java.util.*;

import org.jacorb.imr.AdminPackage.*;

/**
 * This class contains the information about a logical server.
 * It has methods for managing the associated POAs, holding and
 * releasing the server, and, for the "client side", a method
 * that blocks until the server is released.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ImRServerInfo
    implements java.io.Serializable
{
    public static final long serialVersionUID = 1l;

    protected String command;
    protected boolean holding = false;
    protected String host;
    protected String name;
    protected boolean active;
    protected boolean restarting = false;

    private final List poas = new ArrayList();
    private ResourceLock poas_lock = null;

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
     */

    public ImRServerInfo(String name, String host, String command)
        throws IllegalServerName
    {
        if (name == null || name.length() == 0)
        {
            throw new IllegalServerName(name);
        }

        this.name = name;
        this.host = host;
        this.command = command;
        active = false;
        poas_lock = new ResourceLock();
    }

    /**
     * "Converts" this Object to a <code>ServerInfo</code> instance containing
     * the same info as this object.
     *
     * @return a <code>ServerInfo</code> object.
     */

    public ServerInfo toServerInfo()
    {
        poas_lock.gainExclusiveLock();

        // The ServerInfo class stores its POAs in an array, therefore
        // the vector has to copied. Because of backward compatibility
        // issues we decided not to use toArray() from the jdk1.2

        // build array
        final POAInfo[] _info;
        synchronized(poas)
        {
            ImRPOAInfo[] _poas = (ImRPOAInfo[]) poas.toArray(new ImRPOAInfo[poas.size()]);

             _info = new POAInfo[_poas.length];
            for (int i = 0; i < _info.length; i++)
            {
                _info[i] = _poas[i].toPOAInfo();
            }
        }

        poas_lock.releaseExclusiveLock();

        return new ServerInfo(name, command, _info, host, active, holding);
    }

    /**
     * Adds a POA to this server.
     *
     * @param poa the POA to add.
     */

    public void addPOA(ImRPOAInfo poa)
    {
        if (! active)
        {
            active = true;
        }

        poas_lock.gainSharedLock();
        synchronized (poas)
        {
            poas.add(poa);
        }
        poas_lock.releaseSharedLock();
    }

    /**
     * Builds an array of of the names of the POAs associated with this server.
     * <br> This method is needed for deleting a server since its POAs have to be
     * as well removed from the central storage.
     * @return an array of POA names
     */

    protected String[] getPOANames()
    {
        // build array
        final String[] names;
        synchronized(poas)
        {
            ImRPOAInfo[] _poas = (ImRPOAInfo[]) poas.toArray(new POAInfo[poas.size()]);

             names = new String[_poas.length];
            for (int i = 0; i < names.length; i++)
            {
                names[i] = _poas[i].name;
            }
        }

        return names;
    }

    /**
     * Sets the server down, i.e. not active. If a request for a POA
     * of this server is received, the repository tries to restart the server.
     * <br>The server is automatically set back to active when the first of
     * its POAs gets reregistered.
     */

    public void setDown()
    {
        synchronized (poas)
        {
            // sets all associated to not active.
            for (int _i = 0; _i < poas.size(); _i++)
            {
                ((ImRPOAInfo) poas.get(_i)).active = false;
            }
        }

        active = false;
        restarting = false;
    }

    /**
     * This method blocks until the server is released, i.e. set
     * to not holding. <br> This will not time out since holding a
     * server is only done by administrators.
     */

    public synchronized void awaitRelease()
    {
        while(holding)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                // ignored
            }
        }
    }

    /**
     * Release the server and unblock all waiting threads.
     */

    public synchronized void release()
    {
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

    public synchronized boolean shouldBeRestarted()
    {
        boolean _restart = !(active || restarting);
        if (_restart)
        {
            restarting = true;
        }

        return _restart;
    }

    public void setNotRestarting()
    {
        restarting = false;
    }
}
