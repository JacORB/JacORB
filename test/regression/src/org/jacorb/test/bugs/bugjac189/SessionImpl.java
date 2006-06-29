package org.jacorb.test.bugs.bugjac189;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * @author Nick Cross
 * @version $Id$
 */
public class SessionImpl extends SessionPOA
{
    /**
     * Describe variable <code>poa</code> here.
     *
     */
    private final POA poa;


    /**
     * Creates a new <code>SessionImpl</code> instance.
     *
     * @param poa a <code>POA</code> value
     */
    public SessionImpl(POA poa)
    {
        this.poa = poa;
    }


    /**
     * <code>test189Op</code> is a dummy operation to call via the newly created
     * POA.
     *
     */
    public void test189Op() // NOPMD
    {
    }

    /**
     * <code>logout</code> shuts down this POA.
     */
    public void logout()
    {
        try
        {
            poa.destroy(false, false);
        }
        catch(Exception e)
        {
            System.err.println("Error destroying child POA " + e);
            throw new org.omg.CORBA.INTERNAL ("Failed to destroy POA " + e);
        }
    }


    /**
     * Describe <code>activate</code> method here.
     *
     * @exception ServantAlreadyActive if an error occurs
     * @exception WrongPolicy if an error occurs
     */
    public final void activate() throws ServantAlreadyActive, WrongPolicy
    {
        poa.activate_object(this);
    }


    /**
     * Describe <code>reference</code> method here.
     *
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception ServantNotActive if an error occurs
     * @exception WrongPolicy if an error occurs
     */
    public final org.omg.CORBA.Object reference() throws
        ServantNotActive, WrongPolicy
    {
        return poa.servant_to_reference(this);
    }
}
