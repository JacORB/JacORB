/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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

package org.jacorb.notification.lifecycle;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ServantLifecyleControl implements ManageableServant
{
    private final IServantLifecyle delegate_;

    private final boolean runGCDuringDeactivation_;
    
    private org.omg.CORBA.Object thisRef_;

    private Servant thisServant_;

    public ServantLifecyleControl(IServantLifecyle delegate, Configuration config)
    {
        this(delegate, config.getAttribute(Attributes.RUN_SYSTEM_GC, Default.DEFAULT_RUN_SYSTEM_GC).equalsIgnoreCase("on"));
    }

    public ServantLifecyleControl(IServantLifecyle delegate, boolean runGCDuringDeactivation)
    {
        delegate_ = delegate;
        runGCDuringDeactivation_ = runGCDuringDeactivation;
    }
    
    public synchronized org.omg.CORBA.Object activate()
    {
        if (thisRef_ == null)
        {
            try
            {
                thisRef_ = delegate_.getPOA().servant_to_reference(getServant());
            } catch (ServantNotActive e)
            {
                throw new RuntimeException();
            } catch (WrongPolicy e)
            {
                throw new RuntimeException();
            }
        }

        return thisRef_;
    }

    private Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = delegate_.newServant();
        }

        return thisServant_;
    }

    public synchronized void deactivate()
    {
        if (thisServant_ != null)
        {
            final POA _poa = delegate_.getPOA();
            try
            {
                final byte[] _oid = _poa.servant_to_id(thisServant_);
                delegate_.getPOA().deactivate_object(_oid);
            } catch (WrongPolicy e)
            {
                throw new RuntimeException();
            } catch (ObjectNotActive e)
            {
                throw new RuntimeException();
            } catch (ServantNotActive e)
            {
                throw new RuntimeException();
            } finally
            {
                thisRef_ = null;
                thisServant_ = null;
                
                if (runGCDuringDeactivation_)
                {
                    System.runFinalization();
                    System.gc();
                }
            }
        }
    }
}
