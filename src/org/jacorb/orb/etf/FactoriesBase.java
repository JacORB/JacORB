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
package org.jacorb.orb.etf;

import org.apache.avalon.framework.configuration.*;

import org.omg.ETF.*;
import org.omg.IOP.*;
import org.omg.RTCORBA.ProtocolProperties;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public abstract class FactoriesBase 
    extends org.omg.ETF._FactoriesLocalBase
    implements Configurable
{
    protected org.jacorb.config.Configuration configuration;
    
    protected static Class connectionClz;
    
    protected static Class listenerClz;
    
    protected static Class profileClz;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
    }
    
    /**
    * ETF defined operation to create a connection.
    */
    public Connection create_connection(ProtocolProperties props)
    {
        Connection connection = null;
        try
        {
            connection = (Connection)connectionClz.newInstance();
        }
        catch (Exception ie)
        {
            throw new org.omg.CORBA.INTERNAL("Cannot instantiate ETF::Connection class: " + ie.getMessage());
        }
        try
        {
            if (connection instanceof Configurable)
            {
                ((Configurable)connection).configure(configuration);
            }
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
        return connection;
    }
    
    /**
    * ETF defined operation to create a connection.
    */
    public Listener create_listener(ProtocolProperties props,
                                    int stacksize,
                                    short base_priority)
    {
        Listener listener = null;
        try
        {
            listener = (Listener)listenerClz.newInstance();
        }
        catch (Exception ie)
        {
            throw new org.omg.CORBA.INTERNAL("Cannot instantiate ETF::Listener class: " + ie.getMessage());
        }
        try
        {
            if (listener instanceof Configurable)
            {
                ((Configurable)listener).configure(configuration);
            }
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
        return listener;
    }
    
    public Profile demarshal_profile(TaggedProfileHolder tagged_profile,
                                      TaggedComponentSeqHolder components)
    {
        ProfileBase profile = null;
        try
        {
            profile = (ProfileBase)profileClz.newInstance();
        }
        catch (Exception ie)
        {
            throw new org.omg.CORBA.INTERNAL("Cannot instantiate ETF::Profile class: " + ie.getMessage());
        }
        
        profile.demarshal(tagged_profile, components);
        
        try
        {
            if (profile instanceof Configurable)
            {
                ((Configurable)profile).configure(configuration);
            }
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
        
        return profile;
    }
    
    public abstract Profile decode_corbaloc (String corbaloc);
}
