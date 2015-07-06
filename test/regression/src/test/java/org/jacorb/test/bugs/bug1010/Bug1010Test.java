package org.jacorb.test.bugs.bug1010;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.TestName;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * Verify ReplyGroup construction does not cause a nullpointer exception
 */
public class Bug1010Test extends ClientServerTestCase
{
    private MyServer server = null;

    @Rule
    public final StandardOutputStreamLog logO = new StandardOutputStreamLog
            (
                    TestUtils.verbose ? LogMode.LOG_AND_WRITE_TO_STREAM : LogMode.LOG_ONLY
            );

    @Rule
    public final StandardErrorStreamLog logE = new StandardErrorStreamLog
            (
                    TestUtils.verbose ? LogMode.LOG_AND_WRITE_TO_STREAM : LogMode.LOG_ONLY
            );

    @Rule
    public TestName name = new TestName();

    @Before
    public void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.poa.queue_max","1000");
        props.put("jacorb.poa.thread_pool_max", Integer.toString(400));

        setup = new ClientServerSetup
        (
         "org.jacorb.test.bugs.bug1010.ServerImpl",
         props,
         props
        );

        server = MyServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void test1010()
       throws Exception
    {
        int clientNum = 200;
        String msg = "<test_msg>";

        try {
            for (int i = 0; i < 500; ++i) {
                runThreads(server, clientNum, msg);
            }
        }catch(Exception e){}

        assertThat(logE.getLog(), not(containsString("Exception")));
        assertThat(logO.getLog(), not(containsString("Exception")));
    }


    private static void runThreads(MyServer s, int clientNum, String msg) throws InterruptedException {/* create thread objects */
        ClientThread[] clientThread = new ClientThread[clientNum];

        DummyServant dummyServant = s.createDummyServant();


        for (int i = 0; i < clientNum; i++) {
            clientThread[i] = new ClientThread(dummyServant, msg, i);
        }

            /* start threads */

        for (int i = 0; i < clientNum; i++) {
            clientThread[i].start();
        }

        int which = 0;
        while (which < clientNum) {
            while (clientThread[which].isAlive()) { Thread.currentThread().sleep(1); }
            which++;
        }

        dummyServant.release();
    }

    public static void main(String[] args) {
        MyServer s = null;

        try {
            int clientNum = 200;

            if (args.length > 1) { clientNum = Integer.parseInt(args[1]); }

            String msg = "<test_msg>";
            /* Make sure that you allow a maximum thread
             * pool size > 1, otherwise this will block.
             */

            System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");


            String resourceName = "jacorb_3_5.properties";

            Properties props = loadProperties(resourceName);

            props.put("jacorb.poa.thread_pool_max", Integer.toString(clientNum * 2 > 5 ? clientNum * 2 : 5));

            ORB orb = ORB.init(args, props);

            BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = orb.string_to_object(br.readLine());

            br.close();

            s = MyServerHelper.narrow(obj);

            POA poa =
                    POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            poa.the_POAManager().activate();


            for (int i = 0; i < 500; ++i) {
                runThreads(s, clientNum, msg);
            }


            System.out.println("Going down...");

            s.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Properties loadProperties(String resourceName) {

        //ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL url = ServerImpl.class.getResource(resourceName);

        Properties propsNew = new Properties();

        try
        {
            InputStream inStream = url.openStream();
            propsNew.load(inStream);
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propsNew;
    }

}
