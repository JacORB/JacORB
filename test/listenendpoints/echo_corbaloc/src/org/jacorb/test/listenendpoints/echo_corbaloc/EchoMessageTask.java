
package org.jacorb.test.listenendpoints.echo_corbaloc;


/**
 *
 * @author nguyenq
 */
public class EchoMessageTask
{
    private CmdArgs cmdArgs = null;
    private EchoMessage servant = null;
    private String threadName = "";
    private int threadNum = -1;
    private boolean terminate = false;

    public EchoMessageTask(String ThreadName, int threadNum, CmdArgs cmdArgs, EchoMessage servant)
    {
        this.cmdArgs = cmdArgs;
        this.servant = servant;
        this.threadName = ThreadName;
        this.threadNum = threadNum;
    }

    public void runEcho()
    {
        if (cmdArgs == null) return;

        try
        {
            int cnt = 0;
            while ( terminate != true )
                {
                    try
                    {
                        cnt++;
                        // Calendar lCDateTime = Calendar.getInstance();
                        String outMsg = new String(Integer.toString(cnt) + " " + cmdArgs.getEchoMsg());
                        long tms_out = System.currentTimeMillis();

                        // send it to the server
                        String inMsg = servant.echo_string(outMsg);

                        long tms_in = System.currentTimeMillis();
                        long tms_dif = tms_in - tms_out;
                        if (inMsg.equals(outMsg))
                        {
                           log("OK" + " " + tms_dif + "mSec <" + inMsg + ">");
                        }
                        else
                        {
                            log("ERR" + " " + tms_dif + "mSec in=<" + inMsg + "> out=<" + outMsg + ">");
                        }
                        Thread.sleep(cmdArgs.getDelay());

                    }
                    catch (Exception e)
                    {
                        System.err.println(threadName + " thread " + threadNum
                                + ": got an exception in run(): " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }

                    if (cmdArgs.getnTimes() != -1 && cnt == cmdArgs.getnTimes())
                    {
                        terminate();
                        break;
                    }
                }


        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    public void terminate()
    {
        terminate = true;
    }

    private void log(String msg)
    {
        System.out.println(threadName + " thread " + threadNum + ": " +  "EchoMessageTask: " + msg);
    }
}
