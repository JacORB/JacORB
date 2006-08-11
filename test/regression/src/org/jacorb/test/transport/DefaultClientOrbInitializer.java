/**
 * 
 */
package org.jacorb.test.transport;

public class DefaultClientOrbInitializer extends AbstractOrbInitializer {

    public DefaultClientOrbInitializer() {

        super (new DefaultTester (), null);
    }
}