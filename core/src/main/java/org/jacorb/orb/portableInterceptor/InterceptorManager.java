/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jacorb.orb.ORB;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.Interceptor;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.slf4j.Logger;

/**
 * This class "manages" the portable interceptors registered
 * with the ORB, and controls the PICurrent.
 *
 * @author Nicolas Noffke
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

    private final org.omg.CORBA.ORB orb;
    private final int current_slots;
    private final Logger logger;

    private final ThreadLocal<Current> piCurrent = new ThreadLocal<Current>();

    /**
     * This is to hold the PICurrent for local invocations that involve interceptors to prevent the
     * client PICurrent being overwritten by a server PICurrent
     */
    private final ThreadLocal<Current> localPICurrent = new ThreadLocal<Current> ();

    public static final PICurrentImpl EMPTY_CURRENT = new PICurrentImpl(null, 0);

    public InterceptorManager(List<Interceptor> client_interceptors,
                              List<Interceptor> server_interceptors,
                              List<Interceptor> ior_intercept,
                              int slot_count,
                              ORB orb)
    {
        logger =
            orb.getConfiguration().getLogger("org.jacorb.orb.interceptors");

        if (logger.isInfoEnabled())
        {
            logger.info("InterceptorManager started with " +
                        server_interceptors.size() +" Server Interceptors, "
                        + client_interceptors.size() + " Client Interceptors and " +
                        ior_intercept.size() + " IOR Interceptors");
        }

        //build sorted arrays of the different interceptors
        client_req_interceptors = client_interceptors.toArray(new ClientRequestInterceptor[client_interceptors.size()]);
        Arrays.sort(client_req_interceptors, INTERCEPTOR_COMPARATOR);

        server_req_interceptors = server_interceptors.toArray(new ServerRequestInterceptor[server_interceptors.size()]);
        Arrays.sort(server_req_interceptors, INTERCEPTOR_COMPARATOR);

        ior_interceptors = ior_intercept.toArray(new IORInterceptor[ior_intercept.size()]);
        Arrays.sort(ior_interceptors, INTERCEPTOR_COMPARATOR);

        this.orb = orb;
        current_slots = slot_count;
    }

    /**
     * This method returns a thread specific PICurrent.
     */
    public Current getCurrent()
    {
        Current value = null;

        if (localPICurrent.get () != null)
        {
           value = localPICurrent.get ();
        }

        if (value == null)
        {
           value = piCurrent.get();

           if (value == null)
           {
               value = getEmptyCurrent();
               piCurrent.set(value);
           }
        }
        return value;
    }

    public boolean hasCurrent()
    {
       return (piCurrent.get() != null);
    }

    /**
     * Set the local PICurrent with the servers PICurrent
     */
    public void setLocalPICurrent (Current localCurrent)
    {
       localPICurrent.set (localCurrent);
    }

    /**
     * When the local invocation is complete we should clear the local PICurrent
     */
    public void removeLocalPICurrent ()
    {
       localPICurrent.set (null);
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
        return new IORInterceptorIterator(logger, ior_interceptors);
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

        // Clear the threadlocals.
        piCurrent.set (null);
        localPICurrent.set (null);
    }
} // InterceptorManager
