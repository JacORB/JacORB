package test.nio.timeout;

import org.omg.CORBA.*;
import java.io.*;
import org.omg.Messaging.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.lang.reflect.Method;

public class Client
{
    public static final int MSEC_FACTOR = 10000;
    public static final int ONE_SECOND = 1000 * MSEC_FACTOR;
    public static final int QUATER_SECOND = 250 * MSEC_FACTOR;
    private static ORB orb;
    private static GoodDay goodDay;
    private static AtomicInteger callId = new AtomicInteger (1);
    private static AtomicInteger timeoutCount = new AtomicInteger (0);
    private static CyclicBarrier barrier;
    private static String data;
    private static int perThreadIteration;

    public static void usage ()
    {
        System.out.println("Usage: jaco test.nio.timeout.Client <thread count> <per thread iteration> <data length> <corbaloc>");
    }

    public static void main(String args[])
    {
        String ior = "corbaloc:iiop:127.0.0.1:6969/VeryShortKey";
        int threadCount = 1, dataLength = 10;

        if (args.length > 0)
        {
            try
            {
                threadCount = Integer.decode (args[0]);
            }
            catch (NumberFormatException e)
            {
                usage ();
                return;
            }
        }

        if (args.length > 1)
        {
            try
            {
                perThreadIteration = Integer.decode (args[1]);
            }
            catch (NumberFormatException ex)
            {
                usage ();
                return;
            }
        }

        if (args.length > 2)
        {
            try
            {
                dataLength = Integer.decode (args[2]);
            }
            catch (NumberFormatException ex)
            {
                usage ();
                return;
            }
        }

        if (args.length > 3)
        {
            ior = args[3];
        }

        ThreadPoolExecutor tpExecutor = null;

        try
        {

            System.out.println ("Thread count: " + threadCount);
            barrier = new CyclicBarrier (threadCount);
            tpExecutor = new ThreadPoolExecutor (0, threadCount, 60L, TimeUnit.SECONDS,
                                                 new SynchronousQueue<Runnable>());

            char[] charArray = new char [dataLength];
            for (int i = 0; i < dataLength; i++)
            {
                charArray[i] = 'a';
            }
            data = new String (charArray);

            // initialize the ORB.
            orb = ORB.init( new String[] {}, null );

            // Lets call the known short object key
            System.out.println ("Calling server using short key form ...");

            org.omg.CORBA.Object obj = orb.string_to_object (ior);

            System.out.println ("Narrowing reference ...");
            // and narrow it to HelloWorld.GoodDay
            // if this fails, a BAD_PARAM will be thrown
            goodDay = GoodDayHelper.narrow( obj );

            Class helperClz = Class.forName ("test.nio.timeout.GoodDayHelper");
            Class corbaObj = Class.forName ("org.omg.CORBA.Object");
            Method narrow = helperClz.getMethod ("narrow",corbaObj);
            org.omg.CORBA.Object narrowed =
                (org.omg.CORBA.Object)narrow.invoke(null, obj);

            boolean yes = narrowed._is_equivalent (goodDay);

            System.out.println ("is equivalent sez " + yes);

            System.out.println ("Getting PolicyManager ...");
           // get PolicyManager and create policies ....
            PolicyManager policyManager =
                PolicyManagerHelper.narrow( orb.resolve_initial_references("ORBPolicyManager"));

            System.out.println ("Creating RTT policy object ...");
            // create an timeout value of 1 sec. The unit is a time
            // step of 100 nano secs., so 10000 of these make up a
            // micro second.
            Any rrtPolicyAny = orb.create_any();
            rrtPolicyAny.insert_ulonglong (ONE_SECOND*2);
            //rrtPolicyAny.insert_ulonglong (QUATER_SECOND);

            // create a relative roundtrip timeout policy and set this
            // policy ORB-wide
            Policy rrtPolicy =
                orb.create_policy( RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                                   rrtPolicyAny );

            System.out.println ("setting policy ...");
            policyManager.set_policy_overrides( new Policy[] {rrtPolicy},
                                                SetOverrideType.ADD_OVERRIDE);

            System.out.println ("Client connected to server. Sleeping for 10 seconds");
            try
            {
                Thread.currentThread().sleep (10000);
            }
            catch (InterruptedException e)
            {
                System.out.println (e);
            }
            System.out.println ("Client awake and gonna do its job");

            Runnable r1 = new Runnable()
            {
                public void run ()
                {

                    System.out.println ("Thread id: " + Thread.currentThread().getName());

                    int id = callId.getAndIncrement() * 1000;
                    int myTimeOutCount = 0;

                    try
                    {
                        barrier.await ();
                    }
                    catch (InterruptedException e)
                    {
                        System.err.println (e);
                    }
                    catch (BrokenBarrierException ex)
                    {
                        System.err.println (ex);
                    }

                    for (int count = 0; count < perThreadIteration; ++count)
                    {
                        try
                        {
                            goodDay.hello_simple (id++, data);
                        }
                        catch ( org.omg.CORBA.TIMEOUT t )
                        {
                            ++myTimeOutCount;
                            //timeoutCount.incrementAndGet();
                            //System.out.println ("***EXCEPTION*** Request id " + id + " timed out.");
                        }
                    }

                    timeoutCount.getAndAdd (myTimeOutCount);
                }
            };

            for (int i = 0; i < threadCount; i++)
            {
                tpExecutor.execute (r1);
            }

        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        finally
        {

            System.out.println ("Winding down the Threadpool.");
            tpExecutor.shutdown ();
            while (true)
            {
                try
                {
                    if (tpExecutor.awaitTermination (Long.MAX_VALUE, TimeUnit.SECONDS))
                    {
                        System.out.println ("Threadpool shut down.");
                        break;
                    }
                }
                catch (InterruptedException ex)
                {
                    // disregard
                }
            }
        }


        System.out.println ("Total timedout requests: " + timeoutCount.get());
        if (timeoutCount.get() > 0)
        {
            // give server some time to respond back without encountering exception
            try
            {
                Thread.currentThread().sleep (10000);
            }
            catch (InterruptedException ex)
            {
                // disregard
            }
        }

        System.out.println ("Shutting down the ORB");
        try
        {
            orb.shutdown (true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
