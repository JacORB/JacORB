/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer;

abstract public class DynamicImplementation extends Servant {

    abstract public void invoke(org.omg.CORBA.ServerRequest request);
}
