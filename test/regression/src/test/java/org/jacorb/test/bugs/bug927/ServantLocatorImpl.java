package org.jacorb.test.bugs.bug927;

import java.util.HashMap;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;

class ServantLocatorImpl extends LocalObject implements ServantLocator {

   private POA poa;
   private org.omg.PortableInterceptor.Current piCurrent;
   private HashMap<String,Servant> m_activateObjectMap = new HashMap<String,Servant>();

   ServantLocatorImpl(POA poa, org.omg.PortableInterceptor.Current piCurrent) {
      this.poa = poa;
      this.piCurrent = piCurrent;
   }

   void addStringToSlotId(String methodName)
   {
        try
        {
            Any slotDataAsAny = piCurrent.get_slot( MyInitializer.slot_id);

            // Get the slot data as a string
            String s = null;
            String slotDataAsStr = "<no_slot_data>";
            if( slotDataAsAny.type().kind().value() != org.omg.CORBA.TCKind._tk_null
                && null != (s = slotDataAsAny.extract_string())) {
               slotDataAsStr = s;
            }

            slotDataAsStr += ":" + methodName;
            Thread.dumpStack();
System.out.println("*** addStringToSlotId slotDataAsStr=" + slotDataAsStr);
            slotDataAsAny.insert_string(slotDataAsStr);

            piCurrent.set_slot( MyInitializer.slot_id, slotDataAsAny);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

   }

   public Servant preinvoke(byte[] oid, POA adapter, String operation
         , org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) throws org.omg.PortableServer.ForwardRequest
  {
     String strOid = new String(oid);
     System.out.println("tid="+Thread.currentThread().getName()+","+"ServantLocator.preinvoke, operation=" + operation
              + "; strOid=" + strOid);

     addStringToSlotId("preinvoke");

     // Search for the servant that handles this strOid
     Servant servant = m_activateObjectMap.get(strOid);
     if (null == servant)
     {
        System.out.println("** OBJECT_NOT_EXIST **");
        throw new OBJECT_NOT_EXIST();
     }

     //System.out.println("** returning servant **");
     return servant;
  }

  public void postinvoke(byte[] oid, POA adapter, String operation
         , java.lang.Object the_cookie, Servant the_servant)
  {
     System.out.println("ServantLocator.postinvoke, operation=" + operation);
     addStringToSlotId("postinvoke");
  }

   public org.omg.CORBA.Object registerObject(String strOid, String typeId, Servant servant)
     throws org.omg.PortableServer.POAPackage.WrongPolicy
   {
      System.out.println("Registering strOid: " + strOid);

      byte[] oidAsBytes = strOid.getBytes();

      org.omg.CORBA.Object obj = poa.create_reference_with_id(oidAsBytes, typeId);

      // save in aom
      m_activateObjectMap.put(strOid, servant);

      return obj;
   }

}
