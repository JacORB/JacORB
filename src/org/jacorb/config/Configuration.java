package org.jacorb.config;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.util.List;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.ORB;

/**
 * ORB configuration objects are read-only representations of files with
 * configuration properties.
 *
 * ORB configuration options for a given name are looked up and loaded as follows:
 * <ol>
 * <li>System properties are loaded first, but only to get properties
 *     that affect further property loading
 * <li>the file <tt>orb.properties</tt> is loaded from java.home/lib
 *     and user.home if it exists
 * <li>if the ORBid is property is set, the file <tt>ORBid.properties</tt> is
 *     loaded from jacorb.config.dir/etc, if that exists, or jacorb.home/etc, or '.'
 *     If ORBid is not set, the default file <tt>jacorb.properties</tt>
 *     is loaded from these places.
 * <li>Custom properties are loaded from each file name listed int the system property
 *     <tt>custom.props</tt>
 * <li>To also support packaged servers in jar files, the configuration
 *     file lookup mechanism finally tries to load named properties files
 *     (<tt>ORBid.properties</tt>, or <tt>jacorb.properties</tt>) from
 *     the classpath, if it cannot find them in the config dictionary.
 * <li> After all property files have been loaded, the System properties are
 *      loaded again, so that command-line properties take precedence
 * <li> Finally, properties hard-coded and passed in through ORB.init() are
 *      loaded.
 *</ol>
 *
 * The Configuration object is also used by JacORB components to
 * retreive their Logger objects.
 *
 * @author Gerald Brose, XTRADYNE Technologies
 * @version $Id$
 */

public interface Configuration
    extends org.apache.avalon.framework.configuration.Configuration
{
    /**
     * @return the ORB for which this configuration was created
     */

    ORB getORB();


    /**
     * @param name the name of the logger, which also functions
     *        as a log category
     * @return a Logger for a given name
     */

    Logger getNamedLogger(String name);



    /**
     * For a property that has a list of comma-separated values,
     * this method returns these values as a list of Strings.
     * If the property is not set, an empty list is returned.
     */

    List getAttributeList(String key);

    /**
     * Create an object from the given property. The class's default
     * constructor will be used.
     *
     * @return an object of the class of the keys value, or null, if
     * no class name is found for the key
     * @throws ConfigurationException
     */

    Object getAttributeAsObject( String key ) throws ConfigurationException;

    boolean getAttributeAsBoolean(String key)
        throws ConfigurationException;

    boolean getAttributeAsBoolean(String key, boolean defaultValue);


    String getLoggerName(Class clazz);


    void setAttribute(String key, String value);

    /**
     * return all attribute names that start
     * with the specified prefix
     */
    List getAttributeNamesWithPrefix(String string);
}
