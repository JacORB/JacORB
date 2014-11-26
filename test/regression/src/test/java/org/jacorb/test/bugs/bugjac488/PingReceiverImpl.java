package org.jacorb.test.bugs.bugjac488;

import org.jacorb.test.harness.TestUtils;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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


public class PingReceiverImpl extends PingReceiverPOA
{
   public void ping ()
   {
      TestUtils.getLogger().debug ("ping() invoked");
   }

   public void shutdown ()
   {
       new Thread (new Runnable()
       {
           public void run()
           {
               TestUtils.getLogger().debug ("Shutting down server");
               try
               {
                   Thread.sleep (5000);
               }
               catch (InterruptedException e)
               {
               }
               System.exit(1);
           }
       },
           "Exiting").start();
   }
}
