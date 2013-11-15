package org.jacorb.test.orb.connection.maxconnectionenforcement;

import java.util.Random;

public class TestIfImpl extends TestIfPOA
{
    Random rnd = null;

    public TestIfImpl()
    {
        rnd = new Random();
    }

    public boolean op()
    {
        return true;
    }

    public boolean doCallback( CallbackIf callback )
    {
        try
        {
            Thread.sleep( Math.abs( rnd.nextInt() ) % 100 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        return callback.opOnCallback();
    }
}
