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

import org.omg.ETF.Connection;
import org.omg.ETF.Handle;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.ETF._ListenerLocalBase;

/**
 * See {@link org.jacorb.test.orb.etf.wiop.WIOPFactories WIOPFactories} for
 * a description of WIOP.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class WIOPListener extends _ListenerLocalBase
{
    private int tag = 0;
    private Listener delegate = null;
    
    public WIOPListener (Listener delegate, int tag)
    {
        this.delegate = delegate;
        this.tag = tag;
    }
    
    public void set_handle(Handle up)
    {
        delegate.set_handle (up);
    }

    public Connection accept()
    {
        return new WIOPConnection (delegate.accept(), tag);
    }

    public void listen()
    {
        delegate.listen();
    }

    public void destroy()
    {
        delegate.destroy();
    }

    public void completed_data (Connection conn)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Profile endpoint()
    {
        return new WIOPProfile (delegate.endpoint(), tag);
    }

}
