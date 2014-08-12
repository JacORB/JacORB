package org.jacorb.test.transport;

import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.transport.CurrentServer;
import org.jacorb.test.orb.transport.CurrentServerHelper;
import org.jacorb.test.orb.transport.CurrentServerPOA;
import org.junit.Assert;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class CurrentServerImpl extends CurrentServerPOA {

    private int invoked_by_client_called_ = 0;
    private int self_test_called_ = 0;
    private final AbstractTester tester_;


    /**
     * DefaultClientOrbInitializer ctor, used in cases where no server-side testing is required.
     *
     */
    public CurrentServerImpl() {

        this (null, null);
    }


    /**
     * The explicit ctor
     *
     * @param orb
     * @param tester
     */
    public CurrentServerImpl(ORB orb, AbstractTester tester) {
        tester_ = tester;
    }


    @Override
    public void invoked_by_client() {

        invoked_by_client_called_++;

        TestUtils.getLogger().debug ("CurrentServer::invoked_by_client() called");

        if (tester_ != null)
            tester_.test_transport_current (this._orb ());

        try {
            POA rootPOA = POAHelper.narrow (_orb ().resolve_initial_references ("RootPOA"));
            CurrentServer server_ = CurrentServerHelper.narrow (rootPOA.servant_to_reference (this));

            server_.invoked_during_upcall ();
        }
        catch (InvalidName e) {
            // TODO Auto-generated catch block
        }
        catch (ServantNotActive e) {
            // TODO Auto-generated catch block
        }
        catch (WrongPolicy e) {
            // TODO Auto-generated catch block
        }
    }


    @Override
    public void invoked_during_upcall() {

        TestUtils.getLogger().debug ("CurrentServer::invoked_during_upcall() called");

        if (tester_ != null)
            tester_.test_transport_current (this._orb ());
    }


    @Override
    public int self_test() {

        self_test_called_++;

        Assert.assertEquals (1, invoked_by_client_called_);
        Assert.assertEquals (1, self_test_called_);
        Assert.assertEquals (5, ServerInterceptor.interceptions ());
        return 0;
    }


    @Override
    public void shutdown() {

        this._orb ().shutdown (true);
    }


}
