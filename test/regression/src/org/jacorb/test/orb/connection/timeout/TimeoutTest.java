package org.jacorb.test.orb.connection.timeout;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

public class TimeoutTest extends ClientServerTestCase
{
    private MyServer grid;
    
    public TimeoutTest (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TimeoutTest.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, GridImpl.class.getName());
        TestUtils.addToSuite(suite, setup, TimeoutTest.class);

        return setup;
    }
    
    protected void setUp() throws Exception
    {
        grid = MyServerHelper.narrow(setup.getServerObject());
    }
    
    protected void tearDown() throws Exception
    {
        grid = null;
    }
    
    public void testTimeout()
    {
        short x = -1;
        short y = -1;
        try
        {
            x = grid.height();
        }
        catch (org.omg.CORBA.IMP_LIMIT e)
        {
            e.printStackTrace();
        }

        System.out.println("Height = " + x);
        try
        {
            y = grid.width();
        }
        catch (org.omg.CORBA.IMP_LIMIT e)
        {
            e.printStackTrace();
        }

        System.out.println("Width = " + y);
        System.out.println("done. ");
    }
}
