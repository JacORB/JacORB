package org.omg.PortableServer;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
abstract public class DynamicImplementation extends Servant {
	abstract public void invoke(org.omg.CORBA.ServerRequest request); 
} 


