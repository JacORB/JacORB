package org.jacorb.test.bugs.bug1010;

import org.omg.PortableServer.POA;

/**
 * @author Alon Hessing
 */

public class DummyServantImpl implements DummyServantOperations {
    private final POA poa;
    private byte[] oid;

    public DummyServantImpl(POA poa) {
        this.poa = poa;
    }

    @Override
    public void test() {
        System.out.println("Dummy servant test called");
    }

    public void setOID(byte[] oid) {
        this.oid = oid;
    }

    public void release() {
        try {
            poa.deactivate_object(oid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}