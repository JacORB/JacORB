package org.jacorb.orb;

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

import org.omg.ETF.Connection;
import org.omg.IOP.IOR;

/**
 * <code>IORMutator</code> is a low level plugin that allows the user
 * to mutate incoming or outgoing objects at the CDRStream level. If
 * the plugin is enabled both mutators will be called by the respective
 * CDRStreams.
 *
 * Note - While this allows the user a lot of power altering objects at
 * a very low level, it is the user's responsibility to ensure that any
 * IOR returned is valid.
 *
 * @author Nick Cross
 * @version $Id$
 */
public abstract class IORMutator
{
    /**
     * <code>connection</code> is the ETF transport describing the connection
     * that this mutator is used for. This should not be altered by the user;
     * any attempt to do so is undefined.
     */
    protected Connection connection;

    /**
     * <code>updateConnection</code> is used to update the ETFConnection information.
     *
     * @param connection a <code>Connection</code> value
     */
    public void updateConnection(final Connection connection)
    {
        this.connection = connection;
    }


    /**
     * <code>mutateIncoming</code> is called by CDRInputStream::readObject.
     * This allows the user to alter the IOR according the their own wishes.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public abstract IOR mutateIncoming (IOR object);


    /**
     * <code>mutateOutgoing</code> is called by CDROutputStream::writeObject.
     * This allows the user to alter the IOR according the their own wishes.
     *
     * @param object an <code>IOR</code> value
     * @return an <code>IOR</code> value
     */
    public abstract IOR mutateOutgoing (IOR object);
}
