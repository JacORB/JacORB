package org.jacorb.test.transport;


// implementation of org.omg.PortableInterceptor.ORBInitializerOperations
// interface
public class IIOPServerOrbInitializer extends AbstractOrbInitializer 
{
	public IIOPServerOrbInitializer() {
        super (null, new IIOPTester());
	}
}// DefaultServerOrbInitializer

