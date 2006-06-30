package org.jacorb.test.bugs.bugjac257;


/**
 * This is a very simple hello world server.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JAC257Impl extends JAC257POA
{
    /**
     * <code>hello</code> prints out the parameter.
     *
     * @param in a <code>String</code> value
     */
    public void hello (String in)
    {
        System.err.println ("Received " + in);
    }

}
