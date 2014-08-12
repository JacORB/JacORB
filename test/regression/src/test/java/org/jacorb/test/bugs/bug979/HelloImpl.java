package org.jacorb.test.bugs.bug979;

import static org.junit.Assert.assertFalse;
import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;

public final class HelloImpl extends HelloPOA
{
    private String ior;

    @Override
    public void sayHello()
    {
        String hello = "Hello, World!";
        TestUtils.getLogger().debug(hello);
        ORB orb = this._orb();
        Hello obj = HelloHelper.narrow(orb.string_to_object(ior));

        assertFalse("Object is null ", obj == null);
        obj.sayGoodbye();

        obj._get_component ();
        obj.sayGoodbye();

        obj._non_existent();
        obj.sayGoodbye();

        try
        {
            obj._get_interface_def();
        }
        catch (INITIALIZE e)
        {
            // Thats ok. As long as its not nullpointer.
        }

        obj.sayGoodbye();
    }

    @Override
    public void sayGoodbye()
    {
        String bye = "Good Bye, World!";
        TestUtils.getLogger().debug(bye);
    }

    @Override
    public void setIOR(String ior)
    {
        this.ior = ior;
    }
}
