package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.*;
import junit.framework.*;
import junit.extensions.TestSetup;
import org.omg.CORBA.ORB;

/**
 * ORB setup for JacORB JUnit tests.
 *
 */

public class ORBSetup extends TestSetup
{
   private static ORB orb = null;
   private static int count = 0;

   public ORBSetup (Test test)
   {
      super (test);
   }

   public void setUp ()
   {
      init (this);
   }

   public void tearDown ()
   {
      destroy ();
   }

   public static ORB getORB ()
   {
      return orb;
   }

   private static synchronized void init (ORBSetup obj)
   {
      if (count == 0)
      {
         String [] args = new String [0];;
         Properties props = new Properties ();

         props.setProperty
            ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
         props.setProperty
            ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

         orb = ORB.init (args, props);
      }
      count++;
   }

   private static synchronized void destroy ()
   {
      if (count == 1)
      {
         orb = null;
      }
      count--;
   }
}
