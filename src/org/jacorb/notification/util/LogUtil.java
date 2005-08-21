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

package org.jacorb.notification.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.config.LogKitLoggerFactory;
import org.jacorb.config.LoggerFactory;
import org.jacorb.util.ObjectUtil;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class LogUtil
{
    private static final LoggerFactory sLoggerFactory;

    static
    {
        sLoggerFactory = newLoggerFactory();
    }
    
    private static LoggerFactory newLoggerFactory()
    {
        try
        {
            Configuration config = Configuration.getConfiguration(new Properties(), null, false);
            
            LoggerFactory factory = newLog4jLoggerFactory(config);
            
            if (factory == null)
            {
                factory = newLogKitFactory(config);
            }
            
            if (factory == null)
            {
                throw new RuntimeException();
            }
            
            return factory;
        } catch (ConfigurationException e)
        {
            throw new RuntimeException("unable to create LoggerFactory for class " + LogUtil.class.getName());
        }
    }

    private static LoggerFactory newLog4jLoggerFactory(Configuration config)
    {
        String clazzName = "org.jboss.util.Log4jLoggerFactory";
        
        try
        {
            // see if Log4j is available
            ObjectUtil.classForName("org.apache.log4j.Level");
            Class clazz = ObjectUtil.classForName(clazzName);
            
            Constructor ctor = clazz.getConstructor(new Class[0]);
            
            final LoggerFactory factory = (LoggerFactory) ctor.newInstance(new Object[0]);
            
            factory.configure(config);
            
            return factory;
        } catch (IllegalArgumentException e)
        {
            return null;
        } catch (ClassNotFoundException e)
        {
            return null;
        } catch (SecurityException e)
        {
            return null;
        } catch (NoSuchMethodException e)
        {
            return null;
        } catch (InstantiationException e)
        {
            return null;
        } catch (IllegalAccessException e)
        {
            return null;
        } catch (InvocationTargetException e)
        {
            return null;
        } catch (ConfigurationException e)
        {
            return null;
        }
    }
    
    private static LoggerFactory newLogKitFactory(Configuration config)
    {
        try
        {
            LogKitLoggerFactory loggerFactory = new LogKitLoggerFactory();
            loggerFactory.configure(config);

            return loggerFactory;
        } catch (ConfigurationException e)
        {
            throw new RuntimeException();
        }
    }

    public static Logger getLogger(org.apache.avalon.framework.configuration.Configuration config,
            String name)
    {
        try
        {
            return ((org.jacorb.config.Configuration) config).getNamedLogger(name);
        } catch (ClassCastException e)
        {
            return getLogger(name);
        }
    }

    public static Logger getLogger(String name)
    {
        return sLoggerFactory.getNamedLogger(name);
    }
}