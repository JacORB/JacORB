/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2006 Gerald Brose.
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

package org.jacorb.orb;

import org.omg.RTCORBA.ClientProtocolPolicy;
import org.omg.RTCORBA.Protocol;

/**
 * The RTORB class, a partial implementation of org.omg.RTCORBA.RTORB
 * Currently only used for the creation of ETF related policies.
 *
 * @author Steve Osselton
 * @version $Id$
 */
public final class RTORB
   extends org.omg.RTCORBA._RTORBLocalBase
{
   protected RTORB (org.jacorb.orb.ORB orb)
   {
   }

   public ClientProtocolPolicy create_client_protocol_policy(Protocol[] protocols)
   {
      return new org.jacorb.orb.policies.ClientProtocolPolicy(protocols);
   }
}
