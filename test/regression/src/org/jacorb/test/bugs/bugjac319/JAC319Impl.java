package org.jacorb.test.bugs.bugjac319;

/**
 * @author Nick Cross
 * @version $Id$
 */
public class JAC319Impl extends JAC319POA
{
    public org.omg.CORBA.Object getObject (org.omg.CORBA.Object obj)
    {
        return obj;
    }
}
