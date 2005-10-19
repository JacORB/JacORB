package org.jacorb.notification;

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

import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotifyServer
{
    private static org.omg.CORBA.ORB orb = null;
    private static org.jacorb.config.Configuration configuration = null;

    /** the specific logger for this component */
    private static Logger logger = null;

    /** the file name int which the IOR will be stored */
    private static String fileName = null;
    private static String fileNameTyped = null;

    private static Properties props = new Properties();

    public static void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = 
            configuration.getNamedLogger("jacorb.notify");

        fileName = 
            configuration.getAttribute("jacorb.notify.ior_filename", "./notify.ior");
        fileNameTyped = 
            configuration.getAttribute("jacorb.notifyTyped.ior_filename", "./notify.ior");

    }
    
    public static AbstractChannelFactory newFactory( org.omg.CORBA.ORB orb, org.omg.PortableServer.POA rootPOA ) throws Exception
    {
        props.put(Attributes.ENABLE_TYPED_CHANNEL, "off");
        props.put(Attributes.IOR_FILE, fileName);
        props.put(Attributes.START_CHANNELS, "1");
        
        return AbstractChannelFactory.newFactory(orb, null, props);
    }
    
    public static AbstractChannelFactory createInstance( org.omg.CORBA.ORB orb, org.omg.PortableServer.POA rootPOA ) throws Exception
    {
        AbstractChannelFactory factory = null;
        
        try
        {

            Configuration config = 
                ((org.jacorb.orb.ORB)orb).getConfiguration();

            /* configure the name service using the ORB configuration */
            configure(config);
            
            factory = newFactory(orb, rootPOA);
        }
        catch( ConfigurationException e )
        {
            e.printStackTrace();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        return factory;
    }
}
