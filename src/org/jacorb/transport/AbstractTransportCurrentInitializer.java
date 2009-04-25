package org.jacorb.transport;

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

import org.jacorb.config.*;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

/**
 * An instance of this class plugs-in the ORB initialization mechanism to make
 * sure the infrastructure the Transport Current feature is using, is properly
 * initialized. The initialization does:
 * 
 * <ul>
 * <li>Registers an initial reference under the getName() name;</li>
 * <li>Registers a TransportListener with the ORB's Transport Manager to be
 * able to receive notifications of Transport selection;</li>
 * </ul>
 * 
 * 
 * @author Iliyan Jeliazkov
 */
public abstract class AbstractTransportCurrentInitializer extends LocalObject implements ORBInitializer
{
    protected Logger logger_;
    protected abstract String getName();

    protected abstract DefaultCurrentImpl getCurrentImpl();

    public final void pre_init(ORBInitInfo info) 
    { 

        ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();
        logger_ = orb.getConfiguration().getNamedLogger("jacorb.transport");
        DefaultCurrentImpl impl = getCurrentImpl();
        try {
            impl.configure(orb.getConfiguration ());
            info.register_initial_reference (getName(), impl);
            logger_.info ("Registered initial reference \""+getName ()+"\" for "+impl.getClass().getName());
        }
        catch (ConfigurationException e) {
            e.printStackTrace ();
        }
        catch (InvalidName e) {
            e.printStackTrace ();
        }
                
        // Chain-in our implementation as a listener to the Transport events
        orb.getTransportManager ().addTransportListener (impl);
    }    
    
    public final void post_init(ORBInitInfo info) {
        /* empty */
    }


}
