package org.jacorb.test.poa;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;


public class Test_impl extends Servant
{
   public Test_impl() {}

   public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
   {
      return new String[]{"IDL:omg.org/CORBA/Object:1.0"};
   }
}
