package org.jacorb.test.orb.etf.wiop;

/*
 *        JacORB  - a free Java ORB
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

import org.omg.ETF.*;

/**
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
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
