package org.jacorb.test.orb.etf;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;

public class AbstractWIOPTestCase extends ClientServerTestCase
{
    protected BasicServer server;

    public AbstractWIOPTestCase(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public final void setUp() throws Exception
    {
        WIOPFactories.setTransportInUse(false);
        server = BasicServerHelper.narrow( setup.getServerObject() );

        doSetUp();
    }

    protected void doSetUp() throws Exception
    {
    }

    public final void tearDown() throws Exception
    {
        doTearDown();

        WIOPFactories.setTransportInUse(false);
        server = null;
    }

    protected void doTearDown() throws Exception
    {
    }
}