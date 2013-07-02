package org.jacorb.test.bugs.bug923;

import java.util.HashMap;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;

class ServantLocatorImpl extends LocalObject implements ServantLocator {

   private POA poa;
   private ORB orb;
   private HashMap<String,Servant> m_activateObjectMap = new HashMap<String,Servant>();

    ServantLocatorImpl(org.omg.CORBA.ORB orb, POA poa) {
      this.poa = poa;
      this.orb = orb;
   }

   public Servant preinvoke(byte[] oid, POA adapter, String operation
         , org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) throws org.omg.PortableServer.ForwardRequest
  {
     String strOid = new String(oid);

     System.out.println("tid="+Thread.currentThread().getName()+","+"ServantLocator.preinvoke, operation=" + operation + "; strOid=" + strOid);

     // Search for the servant that handles this strOid
     Servant servant = m_activateObjectMap.get(strOid);
     if (null == servant)
     {
        System.out.println("** OBJECT_NOT_EXIST **");
        throw new OBJECT_NOT_EXIST();
     }

     System.out.println("** returning servant **");
     return servant;
  }

  public void postinvoke(byte[] oid, POA adapter, String operation
         , java.lang.Object the_cookie, Servant the_servant)
  {
     System.out.println("ServantLocator.postinvoke, operation=" + operation);
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
