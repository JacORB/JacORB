package org.jacorb.notification.servant;

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

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.interfaces.FilterStageSource;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;


/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractSupplierAdmin extends AbstractAdmin {
    public abstract void setSubsequentFilterStageSource(FilterStageSource source);

    protected AbstractSupplierAdmin(IEventChannel channelRef, ORB orb, POA poa, Configuration config, MessageFactory messageFactory, OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(channelRef, orb, poa, config, messageFactory, offerManager, subscriptionManager);
    }
}
