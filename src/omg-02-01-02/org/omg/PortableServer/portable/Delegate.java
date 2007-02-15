/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.portable;

public interface Delegate {

    org.omg.CORBA.ORB orb(org.omg.PortableServer.Servant self);
    org.omg.CORBA.Object this_object(org.omg.PortableServer.Servant self);
    org.omg.PortableServer.POA poa(org.omg.PortableServer.Servant self);
    byte[] object_id(org.omg.PortableServer.Servant self);
    org.omg.PortableServer.POA default_POA(
                    org.omg.PortableServer.Servant self);
    boolean is_a(org.omg.PortableServer.Servant self,
                    java.lang.String repository_id);
    boolean non_existent(org.omg.PortableServer.Servant self);
    org.omg.CORBA.Object get_component(
                    org.omg.PortableServer.Servant self);
    /** @deprecated Deprecated by CORBA 2.4
    */
    org.omg.CORBA.InterfaceDef get_interface(
                    org.omg.PortableServer.Servant self);
    org.omg.CORBA.Object get_interface_def(
                    org.omg.PortableServer.Servant self);
}
