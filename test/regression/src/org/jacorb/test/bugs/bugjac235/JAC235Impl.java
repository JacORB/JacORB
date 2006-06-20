package org.jacorb.test.bugs.bugjac235;


public class JAC235Impl extends JAC235POA
{
    public String hello(int sleep)
    {
        try
        {
            Thread.sleep(sleep);
        }
        catch(InterruptedException e)
        {
            // ignored
        }

        return "Hello World!";
    }
}
