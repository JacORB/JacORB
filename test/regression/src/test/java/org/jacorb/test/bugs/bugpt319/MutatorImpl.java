package org.jacorb.test.bugs.bugpt319;

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


import org.jacorb.orb.IORMutator;
import org.jacorb.orb.ParsedIOR;
import org.omg.CORBA.ORB;
import org.omg.IOP.IOR;


/**
 * <code>MutatorImpl</code> is a sample Mutator implementation for testing.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class MutatorImpl extends IORMutator
{
    /**
     * <code>totalIncomingObjects</code> records the number of incoming calls.
     */
    public static int totalIncomingObjects;


    /**
     * <code>totalOutgoingObjects</code> records the number of outgoing calls.
     */
    public static int totalOutgoingObjects;


    /**
     * <code>mutateIncoming</code> changes an incoming IOR. For this test
     * it changes the IOR to a different one, outputs the transport and
     * increments the count.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public IOR mutateIncoming (IOR object)
    {
        System.err.println
            ("MutatorImpl::mutateIncoming " + connection.get_server_profile());

        ORB orb = ORB.init(new String[0], null);
        ParsedIOR p = new ParsedIOR ((org.jacorb.orb.ORB)orb, BugPt319Test.IMRIOR);

        totalIncomingObjects++;

        return p.getIOR();
    }


    /**
     * <code>mutateOutgoing</code> changes an outgoing IOR. For this test
     * it returns the same IOR, outputs the transport and increments the
     * count.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public IOR mutateOutgoing (IOR object)
    {
        System.err.println
            ("MutatorImpl::mutateOutgoing " + connection.get_server_profile());

        totalOutgoingObjects++;

        return object;
    }
}
