package org.jacorb.demo.miop;

public class GreetingImpl extends GreetingServicePOA
{
   boolean shutdown;


   public GreetingImpl()
   {
      System.out.println("Hello created!");
   }

   public void greeting_oneway(String s)
   {
      System.out.println("Received a string of length " + s.length () + " with '" + s + "'");
   }


   public void shutdown ()
   {
      shutdown = true;
      System.out.println("Received a shutdown operation.");
   }
}
