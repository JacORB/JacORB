/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.PortableServer;

public interface ServantActivatorOperations extends 
                                org.omg.PortableServer.ServantManagerOperations {

    public org.omg.PortableServer.Servant incarnate(byte[] oid,
                                org.omg.PortableServer.POA adapter) throws
                                    org.omg.PortableServer.ForwardRequest;

    public void etherealize(byte[] oid, org.omg.PortableServer.POA adapter,
                                org.omg.PortableServer.Servant serv,
                                boolean cleanup_in_progress,
                                boolean remaining_activations);
}
