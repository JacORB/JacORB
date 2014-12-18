package demo.miop;

//
// IDL:demo/miop/Hello:1.0
//

public class GreetingImpl extends GreetingServicePOA
{
   public GreetingImpl()
   {
      System.out.println("Hello created!");
   }

   public void greeting_oneway(String s)
   {
      System.out.println("### Received a string of length " + s.length () + " with '" + s + "'");
   }


   public void shutdown ()
   {
      System.out.println("### Received a shutdown operation.");
   }
}
