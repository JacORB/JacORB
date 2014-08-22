/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.RTCORBA.ClientProtocolPolicy;
import org.omg.RTCORBA.Mutex;
import org.omg.RTCORBA.PriorityBand;
import org.omg.RTCORBA.PriorityBandedConnectionPolicy;
import org.omg.RTCORBA.PriorityModel;
import org.omg.RTCORBA.PriorityModelPolicy;
import org.omg.RTCORBA.PrivateConnectionPolicy;
import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.ServerProtocolPolicy;
import org.omg.RTCORBA.ThreadpoolLane;
import org.omg.RTCORBA.ThreadpoolPolicy;
import org.omg.RTCORBA.RTORBPackage.InvalidThreadpool;


/**
 * The RTORB class, a partial implementation of org.omg.RTCORBA.RTORB
 * Currently only used for the creation of ETF related policies.
 *
 * @author Steve Osselton
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


   public org.omg.RTCORBA.TCPProtocolProperties create_tcp_protocol_properties
   (
      int send_buffer_size,
      int recv_buffer_size,
      boolean keep_alive,
      boolean dont_route,
      boolean no_delay
   )
   {
       throw new NO_IMPLEMENT ("NYI");
   }

   public Mutex create_mutex ()
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public PriorityBandedConnectionPolicy create_priority_banded_connection_policy (
            PriorityBand[] priorityBands)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public PriorityModelPolicy create_priority_model_policy (PriorityModel priorityModel,
            short serverPriority)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public PrivateConnectionPolicy create_private_connection_policy ()
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public ServerProtocolPolicy create_server_protocol_policy (Protocol[] protocols)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public int create_threadpool (int stacksize, int staticThreads, int dynamicThreads,
            short defaultPriority, boolean allowRequestBuffering, int maxBufferedRequests,
            int maxRequestBufferSize)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public ThreadpoolPolicy create_threadpool_policy (int threadpool)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public int create_threadpool_with_lanes (int stacksize, ThreadpoolLane[] lanes,
            boolean allowBorrowing, boolean allowRequestBuffering, int maxBufferedRequests,
            int maxRequestBufferSize)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void destroy_mutex (Mutex theMutex)
   {
      throw new NO_IMPLEMENT ("NYI");
   }

   public void destroy_threadpool (int threadpool) throws InvalidThreadpool
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
