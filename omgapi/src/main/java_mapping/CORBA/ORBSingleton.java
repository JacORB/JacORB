package org.omg.CORBA;

/*
 *        JacORB  - a free Java ORB
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

public class ORBSingleton
{
   protected static final String DEFAULT_ORB_KEY = "org.omg.CORBA.ORBClass";
   protected static final String DEFAULT_ORB_VALUE = "org.jacorb.orb.ORB";
   protected static final String DEFAULT_ORB_SINGLETON_VALUE = "org.jacorb.orb.ORBSingleton";

   protected static final java.lang.Object SYNCHRONIZER = new java.lang.Object();

   protected static final boolean useTCCL;

   protected static ORB singleton = null;

   static
   {
      String clpolicy = System.getProperty ("jacorb.classloaderpolicy", "tccl");
      if (clpolicy.equalsIgnoreCase ("forname"))
      {
         useTCCL = false;
      }
      else
      {
         useTCCL = true;
      }
   }


   protected static ORB create (String className)
   {
      final ClassLoader cl;

      if (useTCCL)
      {
         if (Thread.currentThread().getContextClassLoader() != null)
         {
            cl = Thread.currentThread().getContextClassLoader();
         }
         else
         {
            cl = ClassLoader.getSystemClassLoader();
         }
      }
      else
      {
         cl = ORB.class.getClassLoader ();
      }

      try
      {
         return (ORB) Class.forName(className, true, cl).newInstance();
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new INITIALIZE("Could not instantiate ORB implementation: " + className);
      }
   }


   public static ORB init()
   {
      synchronized(SYNCHRONIZER)
      {
         if(singleton == null)
         {
            singleton = create(DEFAULT_ORB_SINGLETON_VALUE);
         }
      }

      return singleton;
   }
}
