package org.jacorb.notification.interfaces;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.util.List;

/**
 * Abstraction of a ProxyConsumer, SupplierAdmin, ConsumerAdmin,
 * ProxySupplier. This Interface allows to use the mentioned Classes
 * in an uniform way.
 *
 * Created: Thu Nov 14 20:37:21 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public interface FilterStage {

    /**
     * check if this DistributorNode has been disposed.
     */
    boolean isDisposed();

    /**
     * get FilterStages following this Node.
     */
    List getSubsequentFilterStages();

    /**
     * get Filter associated to this FilterStage.
     */
    List getFilters();

    /**
     * check if this FilterStage has a EventConsumer associcated.
     */
    boolean hasEventConsumer();

    /**
     * check if this DistributorNode has OR Semantic enabled.
     */
    boolean hasOrSemantic();

    /**
     * get the associated DeliverTarget or null.
     */
    EventConsumer getEventConsumer();

}// Destination
