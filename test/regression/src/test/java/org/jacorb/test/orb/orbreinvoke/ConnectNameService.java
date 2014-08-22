package org.jacorb.test.orb.orbreinvoke;

import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageTask;
import org.omg.CORBA.ORB;

/**
 *
 * @author nguyenq
 */
public class ConnectNameService extends Thread
{
    private MyCmdArgs cmdArgs = null;
    private org.omg.CORBA.ORB orb = null;
    private org.omg.CORBA.Object obj = null;
    private EchoMessage servant = null;
    private int threadNum = 0;
    private EchoMessageTask task = null;
    private boolean loop = false;

    public ConnectNameService(MyCmdArgs cmdArgs, int threadNum) throws Exception
    {
        super("ConnectNameService");
        this.cmdArgs = cmdArgs;
        this.threadNum = threadNum;
    }

    private void init() throws Exception
    {
        try
        {
            // initialize the ORB.
            orb = ORB.init( cmdArgs.getCmdArgs(), null );

            obj =orb.string_to_object( cmdArgs.getNsCorbaName() );

            // and narrow it to the servant
            // if this fails, a BAD_PARAM will be thrown
            servant = EchoMessageHelper.narrow( obj );

            // got a greeting message from the servant
            TestUtils.getLogger().debug("ConnectNameService thread " + threadNum + ": " + servant.echo_simple());
        }
        catch (Exception e)
        {
            throw new Exception ("ConnectNameService thread " + threadNum
                    + " got an exception in init(): " + e.getMessage());
            // terminate();
        }
    }

    @Override
    public void run()
    {
        do
        {
            loop = cmdArgs.getLoop();
            try {
                init();
                task = new EchoMessageTask("ConnectNameService", threadNum, cmdArgs, servant);
                task.runEcho();
                terminate();
            }
            catch(Exception e)
            {
               TestUtils.getLogger().debug("ConnectNameService thread " + threadNum
                       + " got an exception in run(): " + e.getMessage());
                terminate();
            }
        } while (cmdArgs.getLoop() == true && loop == true);

    }

    public void terminate()
    {
        loop = false;
        if (task != null) {
            task.terminate();
            task = null;
        }

        if (orb != null) {
            orb.shutdown(true);
            orb = null;
        }
    }

    public org.omg.CORBA.ORB getConnectORB()
    {
        return orb;
    }

    public org.omg.CORBA.Object getConnectObject()
    {
        return obj;
    }
}
