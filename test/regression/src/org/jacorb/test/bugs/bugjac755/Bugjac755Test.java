package org.jacorb.test.bugs.bugjac755;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.portable.InputStream;

public class Bugjac755Test extends TestCase
{
    private static final InputStream TAO_EMUL_STREAM = new TAOInputStreamEmulBugjac755();
    
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
    
    public void testUni2()
    {
        boolean corbaBadParam = false;
        
        try
        {
            uni2Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            Assert.fail();
        }
    }
    
    public void testUni3()
    {
        boolean corbaBadParam = false;
        
        try
        {
            uni3Helper.read(TAO_EMUL_STREAM);
        }
        catch(BAD_PARAM e)
        {
            Assert.fail();
        }
    }
    
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
