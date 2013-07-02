package org.jacorb.test.orb.etf.wiop;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import org.omg.ETF.BufferHolder;
import org.omg.ETF.Connection;
import org.omg.ETF.Profile;
import org.omg.ETF._ConnectionLocalBase;

/**
 * See {@link org.jacorb.test.orb.etf.wiop.WIOPFactories WIOPFactories} for
 * a description of WIOP.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class WIOPConnection extends _ConnectionLocalBase
{
    private int tag = 0;
    private Connection delegate = null;
    
    public WIOPConnection (Connection delegate, int tag)
    {
        this.delegate = delegate;
        this.tag = tag;
    }
    
    public void write (boolean is_first,
                       boolean is_last,
                       byte[] data,
                       int offset,
                       int length,
                       long time_out)
    {
        WIOPFactories.setTransportInUse(true);
        delegate.write (is_first, is_last, data, offset, length, time_out);
    }

    public int read (BufferHolder data,
                     int offset,
                     int min_length,
                     int max_length,
                     long time_out)
    {
        WIOPFactories.setTransportInUse(true);
        return delegate.read (data, offset, min_length, max_length, time_out);
    }

    public void flush()
    {
        delegate.flush();
    }

    public void connect(Profile server_profile, long time_out)
    {
        if (server_profile instanceof WIOPProfile)
        {
            delegate.connect (((WIOPProfile)server_profile).getDelegate(), 
                              time_out);
        }
        else
        {
            throw new org.omg.CORBA.BAD_PARAM
                ( "attempt to connect an WIOP connection "
                + "to a non-WIOP profile: " + server_profile.getClass());
        }
    }

    public void close()
    {
        delegate.close();
    }

    public boolean is_connected()
    {
        return delegate.is_connected();
    }

    public Profile get_server_profile()
    {
        return new WIOPProfile (delegate.get_server_profile(),
                                this.tag);
    }

    public boolean is_data_available()
    {
        return delegate.is_data_available();
    }

    public boolean wait_next_data(long time_out)
    {
        return delegate.wait_next_data (time_out);
    }

    public boolean supports_callback()
    {
        return delegate.supports_callback();
    }

    public boolean use_handle_time_out()
    {
        return delegate.use_handle_time_out();
    }

}
