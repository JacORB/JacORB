/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2011 Gerald Brose
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

package org.jacorb.orb.nio;

import org.jacorb.config.*;
import org.omg.ETF.Profile;
import org.jacorb.orb.iiop.IIOPListener;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;

/**
 * @author Ciju John
 * @version $Id$
 *
 * This class is identical to the iiop.IIOPFactories except for the static
 *  initialization bit.
 */
public class NIOFactories
        extends org.jacorb.orb.iiop.IIOPFactories
{
    static
    {
        connectionClz = ClientNIOConnection.class;
    }

}
