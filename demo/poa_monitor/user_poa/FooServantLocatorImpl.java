package demo.poa_monitor.user_poa;

import demo.poa_monitor.foox.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

public class FooServantLocatorImpl 
    extends org.omg.PortableServer._ServantLocatorLocalBase
{
    private FooImpl foo = new FooImpl("0");
 
    public void postinvoke(byte[] oid, 
                           POA adapter, 
                           String operation, 
                           java.lang.Object cookie, 
                           Servant servant) 
    {
        String oidStr = new String(oid);
        if (!oidStr.equals(cookie)) 
        {
            System.out.println("[ postinvoke "+operation+" for oid: "+oidStr+": cookie is unknown ]");		
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    public Servant preinvoke(byte[] oid, 
                             POA adapter, 
                             String operation, CookieHolder cookie) 
        throws ForwardRequest 
    {
        String oidStr = new String(oid);
        int oidInt = Integer.parseInt(oidStr);
        if (oidInt >= 1000) 
        {
            cookie.value = oidStr;
            return foo;
        }
        return null;
    }
}
