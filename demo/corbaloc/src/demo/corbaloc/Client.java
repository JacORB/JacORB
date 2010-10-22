package demo.corbaloc;

import org.omg.CORBA.*;
import java.io.*;

public class Client
{
   private static ORB orb;

   public static void main(String args[])
   {
      if (args.length != 1)
      {
         System.out.println("Usage: jaco demo.hello.Client <corbaloc>");
         System.exit(1);
      }

      // initialize the ORB.
      orb = ORB.init( new String[]{}, null );

      callServer (args[0] + "persistent");
      callServer (args[0] + "transient");

      // Lets call the known short object key
      System.out.println ("Calling server using short key form...");

      org.omg.CORBA.Object obj = orb.string_to_object("corbaloc:iiop:127.0.0.1:6969/VeryShortKey");

      // and narrow it to HelloWorld.GoodDay
      // if this fails, a BAD_PARAM will be thrown
      GoodDay goodDay = GoodDayHelper.narrow( obj );

      // invoke the operation and print the result
      System.out.println( goodDay.hello_simple() );
   }

   private static void callServer (String args)
   {
      try
      {
         File f = new File( args);

         //check if file exists
         if( ! f.exists() )
         {
            System.out.println("File " + args +
                               " does not exist.");

            System.exit( -1 );
         }

         //check if args points to a directory
         if( f.isDirectory() )
         {
            System.out.println("File " + args +
                               " is a directory.");

            System.exit( -1 );
         }

         BufferedReader br =
         new BufferedReader( new FileReader( f ));

         // get object reference from command-line argument file
         org.omg.CORBA.Object obj = orb.string_to_object( br.readLine() );

         br.close();

         // and narrow it to HelloWorld.GoodDay
         // if this fails, a BAD_PARAM will be thrown
         GoodDay goodDay = GoodDayHelper.narrow( obj );


         // invoke the operation and print the result
         System.out.println( goodDay.hello_simple() );

      }
      catch( Exception ex )
      {
         ex.printStackTrace();
      }
   }
}
