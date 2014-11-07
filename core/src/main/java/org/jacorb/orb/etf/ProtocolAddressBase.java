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
package org.jacorb.orb.etf;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDROutputStream;


/**
 * Provides an abstraction of a protocol specific address.
 * This is necessary to allow the ORB and other components deal with
 * addresses generally rather than using protocol specific address elements
 * such as IIOP-centric host and port values.
 *
 * @author Phil Mesnier
 */

public abstract class ProtocolAddressBase
    implements Cloneable, Configurable
{
    protected org.jacorb.config.Configuration configuration;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        if( configuration == null )
        {
            throw new ConfigurationException("ProtocolAddressBase: given configuration was null");
        }

        this.configuration = configuration;
    }


    public abstract String toString();

    public abstract boolean fromString(String s);

    public abstract void write (CDROutputStream s);

    public byte [] toCDR ()
    {
        final CDROutputStream out = new CDROutputStream();

        try
        {
            out.beginEncapsulatedArray();
            this.write(out);
            return out.getBufferCopy();
        }
        finally
        {
            out.close();
        }
    }

    /**
     * This function shall return an equivalent, copy of the profile.
     */
    public abstract ProtocolAddressBase copy();
}
