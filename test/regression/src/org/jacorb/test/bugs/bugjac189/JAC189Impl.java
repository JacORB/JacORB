package org.jacorb.test.bugs.bugjac189;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.ThreadPolicyValue;


/**
 * <code>JAC189Impl</code> is the implementation code to test creating multiple
 * POAs with SINGLE_THREAD and shutting them down.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JAC189Impl extends JAC189POA implements Configurable
{
    /**
     * <code>rootPoa</code> is the root POA.
     */
    private POA rootPoa;

    /**
     * <code>_requestCounter</code> is a count of POAs.
     */
    private static int _requestCounter = 0;


    /**
     * <code>login</code>
     *
     * @return a <code>Session</code> value
     */
    public Session login ()
    {
        Session result = null;

        _requestCounter++;

        String poaName = "SessionPOA_" + _requestCounter;

        org.omg.CORBA.Policy[] policies =
        {
            rootPoa.create_thread_policy(ThreadPolicyValue.SINGLE_THREAD_MODEL)
        };

        try
        {
            POA childPOA = rootPoa.create_POA(poaName,
                                              rootPoa.the_POAManager(),
                                              policies);

            childPOA.the_POAManager().activate();

            SessionImpl imp = new SessionImpl(childPOA);

            imp.activate();

            result = SessionHelper.narrow(imp.reference());
        }
        catch(Exception e)
        {
            throw new INTERNAL ("Error creating session " + e);
        }
        return result;
    }


    public void configure(Configuration arg0) throws ConfigurationException
    {
        ORB orb = ((org.jacorb.config.Configuration)arg0).getORB();
        try
        {
            rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        } catch (InvalidName e)
        {
            throw new IllegalArgumentException();
        }
    }
}
