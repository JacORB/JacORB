package org.jacorb.test.transport;


// implementation of org.omg.PortableInterceptor.ORBInitializerOperations
// interface
public class DefaultServerOrbInitializer extends AbstractOrbInitializer 
{
	public DefaultServerOrbInitializer() {
        super (null, new DefaultTester());
	}
}// DefaultServerOrbInitializer

