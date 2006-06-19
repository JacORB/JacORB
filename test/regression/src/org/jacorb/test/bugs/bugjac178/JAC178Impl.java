package org.jacorb.test.bugs.bugjac178;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


/**
 * <code>JAC178Impl</code> is the implementation code to test SINGLE_THREAD and
 * ORB_CTRL threading models within the POA.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class JAC178Impl extends JAC178POA implements Configurable
{
    /**
     * <code>orb</code> is the ORB instance.
     */
    private ORB orb;

    /**
     * <code>rootPoa</code> is the root POA.
     */
    private POA rootPoa;

    /**
     * <code>result</code> holds an ordered list the of operation calls to return
     * for checking.
     */
    private static List result = new ArrayList();

    public JAC178Impl()
    {
        super();
    }

    public JAC178Impl(ORB orb, POA poa)
    {
        this();

        this.orb = orb;
        this.rootPoa = poa;
    }

    /**
     * <code>getObject</code> returns a child object within a new POA using the
     * selected threading model.
     *
     * @param sessionID a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     */
    public org.omg.CORBA.Object getObject (String sessionID)
    {
        try
        {
            // Create child using the supplied session ID
            Policy policies[] = new Policy[1];

            System.err.println ("Creating child poa/object with sessionID " + sessionID);

            // The sessionID encodes which thread model to use (sneaky!).
            if (sessionID.startsWith ("Single"))
            {
                policies[0] = rootPoa.create_thread_policy(
                    org.omg.PortableServer.ThreadPolicyValue.SINGLE_THREAD_MODEL);
            }
            else
            {
                policies[0] = rootPoa.create_thread_policy(
                    org.omg.PortableServer.ThreadPolicyValue.ORB_CTRL_MODEL);
            }

            POA poa = rootPoa.create_POA(sessionID, rootPoa.the_POAManager(), policies);

            JAC178Impl child = new JAC178Impl(orb, poa);

            poa.activate_object (child);

            org.omg.CORBA.Object obj = poa.servant_to_reference( child );

            return obj;
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            throw new INTERNAL("Test error " + e);
        }
    }


    /**
     * <code>shortOp</code> is the quick short operation to be interleaved with the
     * longOp.
     *
     * @param name a <code>String</code> value
     */
    public void shortOp (String name)
    {
        add ("begin-shortOp" + name);
        try
        {
            Thread.sleep (1000);
        }
        catch (InterruptedException e)
        {
            throw new INTERNAL("Test error - was interrupted" + e);
        }
        add ("end-shortOp" + name);
    }


    /**
     * <code>longOp</code> is the long operation
     * @see #shortOp(String)
     */
    public void longOp ()
    {
        add ("begin-longOp");
        try
        {
            Thread.sleep (5000);
        }
        catch (InterruptedException e)
        {
            throw new INTERNAL("Test error - was interrupted" + e);
        }
        add ("end-longOp");
    }


    /**
     * <code>getResult</code> returns operations in order of execution.
     *
     * @return a <code>String</code> value
     */
    public String getResult ()
    {
        try
        {
            return result.toString ();
        }
        finally
        {
            result.clear();
        }
    }


    /**
     * <code>add</code> adds the operation name to the stack.
     *
     * @param name a <code>String</code> value
     */
    private synchronized void add (String name)
    {
        result.add(name);
    }


    public void configure(Configuration arg0) throws ConfigurationException
    {
        org.jacorb.config.Configuration config = (org.jacorb.config.Configuration)arg0;
        orb = config.getORB();
        try
        {
            rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        } catch (InvalidName e)
        {
            throw new RuntimeException();
        }
    }
}
