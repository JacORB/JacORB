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

package org.jacorb.orb.policies;

import org.omg.RTCORBA._ClientProtocolPolicyLocalBase;
import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.ProtocolListHelper;

/**
 * @author Steve Osselton
 * @version $Id$
 */
public class ClientProtocolPolicy extends _ClientProtocolPolicyLocalBase
{
   private final Protocol[] protos;

   public ClientProtocolPolicy(Protocol[] protos)
   {
       super();
       this.protos = protos;
   }

   public ClientProtocolPolicy(org.omg.CORBA.Any any)
   {
      this(ProtocolListHelper.extract (any));
   }

   public int tag()
   {
      if (protos[0] != null)
      {
         return protos[0].protocol_type;
      }
      return org.omg.IOP.TAG_INTERNET_IOP.value;
   }

   public Protocol[] protocols()
   {
      return protos;
   }

   public int policy_type()
   {
      return org.omg.RTCORBA.CLIENT_PROTOCOL_POLICY_TYPE.value;
   }

   public org.omg.CORBA.Policy copy()
   {
      return new ClientProtocolPolicy(protos);
   }

   public void destroy ()
   {
   }
}
