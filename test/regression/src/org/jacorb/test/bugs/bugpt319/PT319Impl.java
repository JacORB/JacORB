package org.jacorb.test.bugs.bugpt319;


/**
 * <code>PT319Impl</code> is a simple server implementation to test iormutation.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class PT319Impl extends PT319POA
{
    /**
     * <code>getObject</code> does nothing - all test code is in MutatorImpl or
     * TestCase itself.
     *
     * @param obj an <code>org.omg.CORBA.Object</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     */
    public org.omg.CORBA.Object getObject (org.omg.CORBA.Object obj)
    {
        return obj;
    }
}
