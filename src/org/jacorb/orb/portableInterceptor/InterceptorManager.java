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

package org.jacorb.orb.portableInterceptor;

import org.jacorb.orb.ORB;
import org.omg.PortableInterceptor.*;
import org.apache.avalon.framework.logger.Logger;

import java.util.*;

/**
 * This class "manages" the portable interceptors registered
 * with the ORB, and controls the PICurrent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class InterceptorManager
{
    /**
     * compare two comparators by their name
     */
    private static final Comparator INTERCEPTOR_COMPARATOR = new Comparator()
    {
        public int compare(Object arg0, Object arg1)
        {
            final Interceptor left = (Interceptor) arg0;
            final Interceptor right = (Interceptor) arg1;

            return left.name().compareTo(right.name());
        }
    };

    private final Interceptor[] client_req_interceptors;
    private final Interceptor[] server_req_interceptors;
    private final Interceptor[] ior_interceptors;
    private int[] profile_tags = null;

    private final org.omg.CORBA.ORB orb;
    private final int current_slots;
    private final Logger logger;

    private static final ThreadLocal piCurrent = new ThreadLocal();

    public static final PICurrentImpl EMPTY_CURRENT = new PICurrentImpl(null, 0);

    public InterceptorManager(List client_interceptors,
                              List server_interceptors,
                              List ior_intercept,
                              int slot_count,
                              ORB orb)
    {
        logger =
            orb.getConfiguration().getNamedLogger("jacorb.orb.interceptors");

        if (logger.isInfoEnabled())
        {
            logger.info("InterceptorManager started with " +
                        server_interceptors.size() +" Server Interceptors, "
                        + client_interceptors.size() + " Client Interceptors and " +
                        ior_intercept.size() + " IOR Interceptors");
        }

        //build sorted arrays of the different interceptors
        client_req_interceptors = (Interceptor[]) client_interceptors.toArray(new ClientRequestInterceptor[client_interceptors.size()]);
        Arrays.sort(client_req_interceptors, INTERCEPTOR_COMPARATOR);

        server_req_interceptors = (Interceptor[]) server_interceptors.toArray(new ServerRequestInterceptor[server_interceptors.size()]);
        Arrays.sort(server_req_interceptors, INTERCEPTOR_COMPARATOR);

        ior_interceptors = (Interceptor[]) ior_intercept.toArray(new IORInterceptor[ior_intercept.size()]);
        Arrays.sort(ior_interceptors, INTERCEPTOR_COMPARATOR);

        this.orb = orb;
        current_slots = slot_count;
    }

    /**
     * This method returns a thread specific PICurrent.
     */
    public Current getCurrent()
    {
        Current value = (Current)piCurrent.get();
        if (value == null)
        {
            value = getEmptyCurrent();
            piCurrent.set(value);
        }
        return value;
    }

    /**
     * Sets the thread scope current, i.e. a server side current
     * associated with the calling  thread.
     */
    public void setTSCurrent(Current current)
    {
        piCurrent.set(current);
    }

    /**
     * Removes the thread scope current, that is associated with the
     * calling thread.
     */
    public void removeTSCurrent()
    {
        piCurrent.set(null);
    }

    /**
     * Returns an empty current where no slot has been set.
     */
    public Current getEmptyCurrent()
    {
        return new PICurrentImpl(orb, current_slots);
    }

    /**
     * Returns an iterator object that contains the ClientRequestInterceptors
     * of this manager.
     */
    public ClientInterceptorIterator getClientIterator()
    {
        return new ClientInterceptorIterator(logger, client_req_interceptors);
    }

    /**
     * Returns an iterator object that contains the ServerRequestInterceptors
     * of this manager.
     */
    public ServerInterceptorIterator getServerIterator()
    {
        return new ServerInterceptorIterator(server_req_interceptors);
    }

    /**
     * Returns an iterator object that contains the IORInterceptors
     * of this manager.
     */
    public IORInterceptorIterator getIORIterator()
    {
        return new IORInterceptorIterator(logger, ior_interceptors, profile_tags);
    }


    /**
     * Assign the array of profile tags to be passed to the IORInterceptors
     */
    public void setProfileTags (int[] ptags)
    {
        profile_tags = ptags;
    }

    /**
     * Test, if the manager has ClientRequestInterceptors
     */
    public boolean hasClientRequestInterceptors()
    {
        return client_req_interceptors.length > 0;
    }

    /**
     * Test, if the manager has ServerRequestInterceptors
     */
    public boolean hasServerRequestInterceptors()
    {
        return server_req_interceptors.length > 0;
    }

    /**
     * Test, if the manager has IORInterceptors
     */
    public boolean hasIORInterceptors()
    {
        return ior_interceptors.length > 0;
    }

    public void destroy()
    {
        if( hasClientRequestInterceptors() )
        {
            for( int i = 0; i < client_req_interceptors.length; i++ )
            {
                client_req_interceptors[ i ].destroy();
            }
        }

        if( hasServerRequestInterceptors() )
        {
            for( int i = 0; i < server_req_interceptors.length; i++ )
            {
                server_req_interceptors[ i ].destroy();
            }
        }

        if( hasIORInterceptors() )
        {
            for( int i = 0; i < ior_interceptors.length; i++ )
            {
                ior_interceptors[ i ].destroy();
            }
        }
    }
} // InterceptorManager
