package org.jacorb.test.bugs.bugjac319;

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

import org.apache.avalon.framework.logger.NullLogger;
import org.jacorb.orb.IORMutator;
import org.jacorb.orb.ParsedIOR;
import org.omg.CORBA.ORB;
import org.omg.ETF.Connection;
import org.omg.IOP.IOR;

/**
 * <code>MutatorImpl</code> is a sample Mutator implementation for testing.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class MutatorImpl extends IORMutator
{
    public static boolean isConnectionUpdated;

    /**
     * <code>totalIncomingObjects</code> records the number of incoming calls.
     */
    public static int totalIncomingObjects;

    /**
     * <code>totalOutgoingObjects</code> records the number of outgoing calls.
     */
    public static int totalOutgoingObjects;

    public static void reset()
    {
        totalIncomingObjects = 0;
        totalOutgoingObjects = 0;
        isConnectionUpdated = false;
    }

    /**
     * <code>mutateIncoming</code> changes an incoming IOR. For this test
     * it changes the IOR to a different one and
     * increments the count.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public IOR mutateIncoming (IOR object)
    {
        ORB orb = ORB.init(new String[0], null);
        ParsedIOR ior = new ParsedIOR ((org.jacorb.orb.ORB) orb, BugJac319AbstractTest.IMRIOR);

        totalIncomingObjects++;

        return ior.getIOR();
    }

    /**
     * <code>mutateOutgoing</code> changes an outgoing IOR. For this test
     * it returns the same IOR and increments the
     * count.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public IOR mutateOutgoing (IOR object)
    {
        totalOutgoingObjects++;

        return object;
    }

    public void updateConnection(Connection connection)
    {
        super.updateConnection(connection);
        isConnectionUpdated = true;
    }
}
