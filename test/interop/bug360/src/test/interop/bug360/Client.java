package test.interop.bug360;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.omg.CORBA.Any;

public class Client
{
    public static onewayPushConsumer consumer = null;

    public static void main( String[] args )
    {
        try
        {
            if( args.length != 1 )
            {
                System.out.println( "Usage: jaco Client <ior_file>" );
                System.exit( 1 );
            }

            File f = new File( args[ 0 ] );

            //check if file exists
            if( ! f.exists() )
            {
                System.out.println("File " + args[0] +
                                   " does not exist.");

                System.exit( -1 );
            }

            //check if args[0] points to a directory
            if( f.isDirectory() )
            {
                System.out.println("File " + args[0] +
                                   " is a directory.");

                System.exit( -1 );
            }

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );

            org.omg.CORBA.Object obj =
                orb.string_to_object( br.readLine() );

            br.close();

            consumer = onewayPushConsumerHelper.narrow( obj );

            // create a new any
            Any a = org.omg.CORBA.ORB.init().create_any();

            Struct3 s3 =
                new Struct3( Enum5xxxxxxxx.E5Exxx ,
                             (float)0.0, (float)1.1,
                             Enum6xxxxxxxxxxxxxxxxx.E6Cxxxxxxxxxxxxx
                );
            Struct2 s2 =
                new Struct2( Enum1.E1B,
                             1,
                             (float)1.1,
                             Enum2xxxxxxxxxxxx.E2Bxxxxxxxx,
                             (float)2.2,
                             (float)2.2,
                             Enum2xxxxxxxxxxxx.E2Bxxxxxxxx,
                             Enum3xxxxxxxxxxxxxxxx.E3Cxxxxxxxxxxx,
                             (float)3.3,
                             Enum4xxxxxxxxxxxxx.E4Dxxxxxxxx ,
                             s3
                );

            Struct1Helper.insert( a, new Struct1( 1, s2 ) );

            consumer.synchronousPush( a ) ;

        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
    }
}
