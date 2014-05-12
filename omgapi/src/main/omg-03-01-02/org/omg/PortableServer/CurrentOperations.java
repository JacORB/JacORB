/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.PortableServer;

public interface CurrentOperations extends org.omg.CORBA.CurrentOperations {

    public org.omg.PortableServer.POA get_POA() throws
                        org.omg.PortableServer.CurrentPackage.NoContext;

    public byte[] get_object_id() throws org.omg.PortableServer.CurrentPackage.NoContext;

    public org.omg.CORBA.Object get_reference()
       throws org.omg.PortableServer.CurrentPackage.NoContext ;

    public Servant get_servant()
       throws org.omg.PortableServer.CurrentPackage.NoContext ;
}
