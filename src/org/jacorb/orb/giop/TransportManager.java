package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.omg.ETF.*;

import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.util.*;

/**
 * This class manages Transports. On the one hand it creates them, and
 * on the other it enforces an upper limit on the open transports.
 *
 * @author Nicolas Noffke
 * @version $Id$
 * */

public class TransportManager
{
    public static SocketFactory socket_factory = null;
    public static SocketFactory ssl_socket_factory = null;

    private ProfileSelector profileSelector = null;

    /**
     * Maps ETF Profile tags (Integer) to ETF Factories objects.
     */
    private Map  factoriesMap  = null;

    /**
     * List of all installed ETF Factories.  This list contains an
     * instance of each Factories class, ordered in the same way as
     * they were specified in the jacorb.transport.factories property.
     */
    private List factoriesList = null;

    public TransportManager( ORB orb )
    {
        socket_factory = SocketFactoryManager.getSocketFactory (orb);

        if( Environment.isPropertyOn( "jacorb.security.support_ssl" ))
        {
            String s = Environment.getProperty( "jacorb.ssl.socket_factory" );
            if( s == null || s.length() == 0 )
            {
                throw new RuntimeException( "SSL support is on, but the property \"jacorb.ssl.socket_factory\" is not set!" );
            }

            try
            {
                Class ssl = Environment.classForName( s );

                Constructor constr = ssl.getConstructor( new Class[]{
                    ORB.class });

                ssl_socket_factory = (SocketFactory)
                    constr.newInstance( new Object[]{ orb });
            }
            catch (Exception e)
            {
                Debug.output( 2, e.getMessage());

                throw new RuntimeException( "SSL support is on, but the ssl socket factory can't be instanciated (see trace)!" );
            }
        }
    }

    public ProfileSelector getProfileSelector()
    {
        if (profileSelector == null)
        {
            profileSelector =
                (ProfileSelector)Environment.getObjectProperty
                                         ("jacorb.transport.client.selector");
            if (profileSelector == null)
            {
                profileSelector = new DefaultProfileSelector();
            }
        }
        return profileSelector;
    }

    /**
     * Returns an ETF Factories object for the given tag, or null
     * if no Factories class has been defined for this tag.
     */
    public org.omg.ETF.Factories getFactories (int tag)
    {
        if (factoriesMap == null)
        {
            loadFactories();
        }
        return (Factories)factoriesMap.get (new Integer (tag));
    }

    /**
     * Returns a list of Factories for all configured transport plugins,
     * in the same order as they were specified in the
     * jacorb.transport.factories property.
     */
    public List getFactoriesList()
    {
        if (factoriesList == null)
        {
            loadFactories();
        }
        return Collections.unmodifiableList (factoriesList);
    }

    /**
     * Build the factoriesMap and factoriesList.
     */
    private void loadFactories()
    {
        factoriesMap  = new HashMap();
        factoriesList = new ArrayList();

        List classNames =
            Environment.getListProperty ("jacorb.transport.factories");
        if (classNames.isEmpty())
            classNames.add ("org.jacorb.orb.iiop.IIOPFactories");

        for (Iterator i = classNames.iterator(); i.hasNext();)
        {
            String className = (String)i.next();
            Factories f    = instantiateFactories (className);
            factoriesMap.put (new Integer (f.profile_tag()), f);
            factoriesList.add (f);
        }
    }

    /**
     * Instantiates the given Factories class.
     */
    private org.omg.ETF.Factories instantiateFactories (String className)
    {
        try
        {
            // Environment.classForName() uses the context class loader.
            // This is important here because JacORB might be on the
            // bootclasspath, and the external transport on the normal
            // classpath.
            Class c = Environment.classForName(className);
            return (Factories)c.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException
                ("could not instantiate Factories class " + className
                 + ", exception: " + e);
        }
    }

}
