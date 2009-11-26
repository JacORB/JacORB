package org.jacorb.orb.giop;


/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2004 Gerald Brose.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/**
 * This interface allows classes, registered with the TransportManager to
 * receive notifications about the use of a Transport (by the current thread).
 *
 * Events are generated before starting the request sending sequence, for
 * parties interested in what Transport is going to be used. Similarly, events
 * are generated before starting the request handling.
 *
 * @author Iliyan Jeliazkov
 * @version $Id: TransportListener.java,v 1.4 2006-08-16 17:46:36
 *          iliyan.jeliazkov Exp $
 *
 */
public interface TransportListener
{

   public static class Event
   {

      private final GIOPConnection giopc_;


      public Event (GIOPConnection giopc)
      {
         giopc_ = giopc;
      }


      public org.omg.ETF.Profile profile ()
      {
         if (transport ().get_server_profile () == null)
         {
            if (giopc_.getProfile () == null)
            {

               // This looks like the most appropriate exception to throw
               throw new NullPointerException ("Primary profile");
            }
            return giopc_.getProfile ();
         }

         return transport ().get_server_profile ();
      }


      public org.omg.ETF.Connection transport ()
      {
         if (giopc_.getTransport () == null)
         {
            // This looks like the most appropriate exception to throw
            throw new NullPointerException ("Transport");
         }
         return giopc_.getTransport ();
      }


      public String toString ()
      {
         // This hack is avoid a null pointer as the loopback connection is not
         // a proper ETF profile
         // and therefore calling profile() raises a null ptr. If JAC528 was
         // done this could be removed.
         if (transport () instanceof org.jacorb.orb.iiop.IIOPLoopbackConnection)
         {
            // Its a loopback connection.
            return ((org.jacorb.orb.iiop.IIOPLoopbackConnection)transport ()).getConnectionInfo ();
         }
         else
         {
            return "[" + profile () + ']';
         }
      }


      public StatisticsProvider getStatisticsProvider (int orderno)
      {
         return giopc_.getStatisticsProvider (orderno);
      }
   }


   /**
    * Call-back for handling Transport selection events.
    *
    * @param event TODO
    */
   void transportSelected (Event event);

}
