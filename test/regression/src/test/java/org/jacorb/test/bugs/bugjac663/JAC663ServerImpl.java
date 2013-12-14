package org.jacorb.test.bugs.bugjac663;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.Current;
import org.omg.PortableServer.CurrentHelper;
import org.omg.PortableServer.POA;

public class JAC663ServerImpl extends JAC663ServerPOA
{
   private Current current;

   public JAC663ServerImpl (ORB orb)
   {
      try
      {
         org.omg.CORBA.Object obj = orb.resolve_initial_references ("POACurrent");
         current = CurrentHelper.narrow(obj);
      }
      catch (Exception ex)
      {
         throw new INTERNAL ("Exception getting POACurrent "+ ex.getMessage ());
      }
   }

   public void send_message (String config)
   {
      JAC663Server testServer = null;

      try
      {
         POA poa = current.get_POA();
         byte[] oid = current.get_object_id();

         if (oid == null)
         {
             throw new INTERNAL ("Error - oid is null");
         }

         org.omg.CORBA.Object obj = poa.id_to_reference(oid);

         if (obj == null)
         {
            throw new INTERNAL ("Error - obj is null");
         }

         testServer = JAC663ServerHelper.narrow(obj);

         if (testServer == null)
         {
            throw new INTERNAL ("Error - testServer is null");
         }
      }
      catch (Exception e)
      {
          throw new INTERNAL ("Unexpected exception");
      }
   }
}
