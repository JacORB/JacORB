package demo.poa_monitor.client;

import demo.poa_monitor.foox.*;
import org.omg.CosNaming.*;
import java.io.*;

public class Client 
{
    public static int speed = 0;
    public static int cost = 0;
    public static String serverDescription = "description not available";

    private static TestFrame frame;
    private static FooFactory factory;
    private static Foo [] foos;
    private static RequestGenerator [] [] generators;
	
    private static int allConsumed;
    private static int allEffort;
    private static int firstPOAConsumed;
    private static int firstPOAEffort;
    private static int firstObjectConsumed;
    private static int firstObjectEffort;
    private static int allICount;
    private static int firstPOAICount;	
    private static int firstObjectICount;
    private static boolean firstPOAContact = true;

    public static void actionCancel() 
    {
        actionStop();
        try {
            for (int i=0; i<generators.length; i++) {
                for (int k=0; k<generators[i].length; k++) {
                    generators[i][k].join();
                }
            }
        } catch (Throwable e) {
        }
        System.exit(0);
    }

    public static void actionStart(int objects, int threads) 
    {
        foos = new Foo[objects];
        generators = new RequestGenerator[objects][threads];
        try 
        {
            for (int i=0; i<foos.length; i++) 
            {

                if (firstPOAContact) 
                {       
                    long startTime = System.currentTimeMillis();
                    foos[i] = factory.createFoo(""+(1000+i));
                    long stopTime = System.currentTimeMillis();		   
                    Client.addTime((int)(stopTime-startTime), 0, false);					
                    firstPOAContact = false;
					
                } else {					
                    foos[i] = factory.createFoo(""+(1000+i));
                }				
                if (foos[i] == null) throw new Error("error: createFoo returns null");

                for (int k=0; k<threads; k++) {
                    generators[i][k] = new RequestGenerator(foos[i], k==0);
                    if (k==0) generators[i][k].start();
                }
            }
            Thread.currentThread().sleep(1000);				
            for (int i=0; i<generators.length; i++) {
                for (int k=1; k<generators[i].length; k++) {
                    generators[i][k].start();
                }
            }	
            System.out.println("[ "+(objects*threads)+" RequestGenerators started ]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void actionStop() 
    {
        for (int i=0; i<generators.length; i++) 
        {
            for (int k=0; k<generators[i].length; k++) 
            {
                generators[i][k].active = false;
            }
        }

        System.out.println("[ RequestGenerators stopped ]");

        for (int i=0; i<foos.length; i++) 
        {
            try 
            {
                foos[i].deactivate();
            }
            catch (Throwable e) 
            {
                System.out.println("[ exception occured during object deactivation ]");
                System.out.println(e.toString());
            }
        }
        System.out.println("[ Remote objects deactivated (don't worry about wrong policy exception messages s\n  in server 3 (Servant Locator) ]");
        printTime();		
    }

    synchronized public static void addTime(int consumed, int effort, boolean firstObjectContact) {
        if (firstPOAContact) {
            firstPOAConsumed += consumed;
            firstPOAEffort += effort;
            firstPOAICount++;
            return;
        }
        if (firstObjectContact) {
            firstObjectConsumed += consumed;
            firstObjectEffort += effort;
            firstObjectICount++;
        }
        allConsumed += consumed;
        allEffort += effort;
        allICount++;
    }

    public static void main(String args[]) 
    {
        try 
        {		
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
            // get hold of the naming service and create a fooFactory object
            NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            factory = FooFactoryHelper.narrow(nc.resolve(nc.to_name("FooFactory.service") ));
            System.out.println("[ FooFactory created ]");
			
            // get the server description
            serverDescription = factory.getServerDescription();
			
            // set up the gui
            frame = new TestFrame();
            frame.setVisible(true);		
        }
        catch (Exception e) {
            e.printStackTrace();
        }		
    }

    public static void printTime() 
    {
		
        /*
          long startTime = System.currentTimeMillis();
          while (nb-- > 0)
          ret_vals = server.intTransfer(myInt);

          // for(int i = 0; i < size; System.out.print(" " + ret_vals[i++]));

          long stopTime = System.currentTimeMillis();
          System.out.println(">>> Elapsed time = " 
          + (stopTime - startTime)/1000 
          + " secs      Average time = "
          +  ((stopTime - startTime) / (float)loop) 
          + " msecs");
				       
          if (firstPOAContact) {
          firstPOAConsumed += consumed;
          firstPOAEffort += effort;
          } else if (firstObjectContact) {
          firstObjectConsumed += consumed;
          firstObjectEffort += effort;
          }
          allConsumed += consumed;
          allEffort += effort;
          invocationCount++;
        */
        String v;
        int w = 12;
        int its_p = firstPOAConsumed-firstPOAEffort;
        int its_o = firstObjectConsumed-firstObjectEffort;
        int its_a = allConsumed-allEffort;
        float ita_p = its_p/(float)firstPOAICount;
        float ita_o = its_o/(float)firstObjectICount;
        float ita_a = its_a/(float)allICount;
		
        System.out.println("       IS           CTS(s)       CTA(ms)      ETS(s)       ETA(ms)      ITS(s)       ITA(ms)");
        System.out.println("       ---------------------------------------------------------------------------------------");
        System.out.print("FPCL:  ");
        v = firstPOAICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstPOAConsumed/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstPOAConsumed/(float)firstPOAICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstPOAEffort/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstPOAEffort/(float)firstPOAICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = its_p/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = ita_p+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
		
        System.out.println("\n");
        System.out.print("FOCL:  ");
        v = firstObjectICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstObjectConsumed/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstObjectConsumed/(float)firstObjectICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstObjectEffort/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = firstObjectEffort/(float)firstObjectICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = its_o/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = ita_o+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
		
        System.out.println("\n");
        System.out.print("All :  ");
        v = allICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = allConsumed/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = allConsumed/(float)allICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = allEffort/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = allEffort/(float)allICount+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = its_a/(float)1000+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
        v = ita_a+"";
        System.out.print(v); for(int i=v.length(); i<=w; i++) System.out.print(" ");
		  
        System.out.println("");
        System.out.println("       ---------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("       I...invocation     C...consumed       T...time           S...sum            A...average");
        System.out.println("       F...first          P...poa -          O...object         C...contact        L...latency");
        System.out.println("");
    }
}
