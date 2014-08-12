package org.jacorb.test.bugs.bug983;

import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public final class HelloImpl extends HelloPOA
{
    @Override
    public void sayHello()
    {
        String hello = "Hello, World!";
        TestUtils.getLogger().debug(hello);
    }

    @Override
    public void sayGoodbye()
    {
        try
        {
            String bye = "Good Bye, World!";
            TestUtils.getLogger().debug(bye);
        }
        finally
        {
            final ORB _orb = _orb();
            final byte[] _object_id = _object_id();
            final POA _poa = _poa();
            Thread deactivate = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(1 * 1000);
                        _poa.deactivate_object(_object_id);
                        _orb.shutdown(true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            deactivate.start();
        }
    }
}
