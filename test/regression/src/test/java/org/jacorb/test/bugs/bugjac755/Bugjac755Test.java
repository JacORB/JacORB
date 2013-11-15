package org.jacorb.test.bugs.bugjac755;

import org.junit.Assert;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.portable.InputStream;

public class Bugjac755Test
{
    private static final InputStream TAO_EMUL_STREAM = new TAOInputStreamEmulBugjac755();

    @Test
    public void testUni1()
    {
        try
        {
            uni1Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            Assert.fail();
        }
    }

    @Test
    public void testUni2()
    {
        try
        {
            uni2Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            Assert.fail();
        }
    }

    @Test
    public void testUni3()
    {
        try
        {
            uni3Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            Assert.fail();
        }
    }

    @Test
    public void testUni4()
    {
        boolean corbaBadParam = false;

        try
        {
            uni4Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            corbaBadParam = true;
        }

        Assert.assertTrue(corbaBadParam);
    }
}
