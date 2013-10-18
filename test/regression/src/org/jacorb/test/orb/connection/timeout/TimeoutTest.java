package org.jacorb.test.orb.connection.timeout;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimeoutTest extends ClientServerTestCase
{
    private MyServer grid;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup(GridImpl.class.getName());
    }

    @Before
    public void setUp() throws Exception
    {
        grid = MyServerHelper.narrow(setup.getServerObject());
    }

    @After
    public void tearDown() throws Exception
    {
        grid = null;
    }

    @Test
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
