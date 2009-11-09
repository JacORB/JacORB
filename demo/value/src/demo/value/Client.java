package demo.value;

import java.io.*;
import org.omg.CORBA.*;

public class Client
{
    public static void main( String args[] )
    {
        if( args.length != 1 )
    {
            System.out.println("Usage: java demo.value.idl.Client <ior_file>");
            System.exit( 1 );
        }

        try
    {
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

            // initialize the ORB.
            ORB orb = ORB.init( args, null );

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj =
                orb.string_to_object( br.readLine() );
            br.close();

            ValueServer s = ValueServerHelper.narrow( obj );

            // invoke operations and print the results
            boxedLong p1 = new boxedLong (774);
            boxedLong p2 = new boxedLong (774);

            System.out.println ("Passing two integers: "
                                + s.receive_long (p1, p2));

            System.out.println ("Passing one integer twice: "
                                + s.receive_long (p1, p1));

            System.out.println ("Passing two strings: "
                                + s.receive_string ("hello", "hello"));

            System.out.println ("Passing null: "
                                + s.receive_string ("hello", null));

            Node n1 = new NodeImpl (1);
            Node n2 = new NodeImpl (2);
            Node n3 = new NodeImpl (3);
            Node n4 = new NodeImpl (4);

            n1.next = n2;
            n2.next = n3;
            n3.next = n4;
            n4.next = n1;

            System.out.println ("Passing a list structure: "
                                + s.receive_list (n1));
        }
        catch( Exception ex )
    {
            System.err.println( ex );
        }
    }
}

