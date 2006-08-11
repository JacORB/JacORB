/**
 * 
 */
package org.jacorb.test.transport;

public class IIOPClientOrbInitializer extends AbstractOrbInitializer {

    public IIOPClientOrbInitializer() {

        super (new IIOPTester (), null);
    }
}