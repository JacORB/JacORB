/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.PortableServer;

public interface ServantLocatorOperations extends 
                    org.omg.PortableServer.ServantManagerOperations {

    public org.omg.PortableServer.Servant preinvoke(byte[] oid,
        org.omg.PortableServer.POA adapter,
        java.lang.String operation,
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder
                    the_cookie) throws org.omg.PortableServer.ForwardRequest;

    public void postinvoke(byte[] oid,
        org.omg.PortableServer.POA adapter,
        java.lang.String operation,
        java.lang.Object the_cookie,
        org.omg.PortableServer.Servant the_servant) throws
                    org.omg.PortableServer.ForwardRequest;
}
