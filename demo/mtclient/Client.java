package demo.mtclient;

/**
 *
 * Test multi-threading and call-back support:
 *
 * use any number of ClientThreads on the same server 
 * object. 
 */

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.CORBA.*;


public class Client
{
    public static void main( String[] args )
    {
        MyServer s = null;

        try
        {
            int clientNum = 2;

            if( args.length > 0 )
                clientNum = Integer.parseInt( args[0] );
              
            String msg = "<test_msg>";
            /* Make sure that you allow a maximum thread
             * pool size > 1, otherwise this will block.
             */
            java.util.Properties props = new java.util.Properties();
            props.put("jacorb.poa.thread_pool_max", Integer.toString( clientNum * 2 ));

            ORB orb = ORB.init(args, props);

            // get hold of the naming service
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            s = MyServerHelper.narrow(nc.resolve(nc.to_name("Thread.example")));

            POA poa = 
                POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        
            poa.the_POAManager().activate();
        
            /* create thread objects */
            ClientThread [] clientThread = new ClientThread [clientNum] ;
            for( int i = 0; i < clientNum; i++)
            {
                clientThread[i] = new ClientThread(s, msg, i); 
            }

            /* create CORBA references for each client thread */
            Observer [] observers = new  Observer [clientNum];
            for( int i = 0; i < clientNum; i++)
            { 
                observers[i] = 
                    ObserverHelper.narrow(poa.servant_to_reference( new ObserverPOATie( clientThread[i] )));
                clientThread[i].setMe( observers[i]);
            }

            /* start threads */

            for( int i = 0; i < clientNum; i++)
            { 
                clientThread[i].start();   
            }
          
            int which = 0;
            while( which < clientNum )
            {
                while( clientThread[which].isAlive() )
                    Thread.currentThread().sleep(500);
                which++;
            }

            System.out.println("Going down...");

            orb.shutdown(true);

        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}

