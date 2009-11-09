package demo.any;

import java.io.*;
import org.omg.CORBA.Any;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.StringSeqHelper;

public class Client 
    extends AnyServerPOA
{
    public static AnyServer s = null;
    private static int counter;

    private synchronized static void incr(int val)
    {
        counter += val;
    }

    public java.lang.String generic(Any a)
    {
        System.out.println("Someone called me!");
        incr(-1);
        return "call back succeeded!";
    }

    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

        AnyServer s = null;
        if( args.length == 1)
        {
            // read object's ID from file
            BufferedReader br =
                new BufferedReader( new FileReader( args[0] ));

            s = AnyServerHelper.narrow(orb.string_to_object(br.readLine()));
        }
        else
        {
            // get hold of the naming service
            NamingContextExt nc =
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            
            s = AnyServerHelper.narrow(nc.resolve(nc.to_name("AnyServer.service")));
        }
        
        // create a new any
        Any a = org.omg.CORBA.ORB.init().create_any();

        // char
        char ch = 'c';
        System.out.print("Passing a char...");
        a.insert_char( (char)ch );
        System.out.println( s.generic( a ) );
        
        // long
        long l = 4711;
        System.out.print("Passing a longlong...");
        a.insert_longlong( l );
        System.out.println( s.generic( a ) );

        // short
        System.out.print("Passing a short...");
        a.insert_short( (short)5 );
        System.out.println( s.generic( a ) );

        // float
        System.out.print("Passing a float...");
        a.insert_float( (float)3.14);
        System.out.println( s.generic( a ) );

        // string
        System.out.print("Passing a string...");
        a.type( orb.create_string_tc(0));
        a.insert_string("Hi there");
        System.out.println( s.generic( a ) );
        
        // wstring
        System.out.print("Passing a Wstring...");
        a.insert_wstring( "Hi there" );
        
        System.out.println("Any.kind: " + a.type().kind().value() );
        System.out.println( s.generic( a ) );
        
        // sequences
        String [] str = {"hello","world"};
        MyStringSeqHelper.insert( a, str );
        System.out.print("Alias? " + a.type());
        System.out.print("Passing a sequence of strings...");
        System.out.println( s.generic( a ) );
        
        System.out.print("Passing a sequence of octets...");
        byte [] octets = {1,2,3,4};
        OctetSeqHelper.insert( a, octets );
        System.out.println( s.generic( a ) );
        
        // array
        System.out.print("Passing an array...");
        String [] str2 = {"hello","another","world"};
        stringsHelper.insert( a, str2 );
        System.out.println( s.generic( a ) );
        
        // struct
        System.out.print("Passing a struct...");
        Node tail = new Node("tail" , new Node[0] );
        Node head = new Node("head" , new Node[]{tail} );
        NodeHelper.insert( a, head );
        System.out.println( s.generic( a ) );
        
        // union
        System.out.print("Passing a union...");
        Nums n = new Nums();
        n.l(4711);
        NumsHelper.insert( a, n );
        System.out.println( s.generic( a ) );
        
        /* There are two ways to insert object references: */
        
        incr(1); // remember how many call backs we have to expect
        System.out.print("Passing an object...");
        POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        poa.the_POAManager().activate();
        
        Client c = new Client();
        poa.activate_object(c);
        
        /* insert an untyped reference */
        a.insert_Object(c._this_object());
        System.out.println( "Output of generic: " + s.generic( a ) );
        
        incr(1);
        System.out.print("Passing object again");
        
        /* insert an typed reference */
        AnyServerHelper.insert( a, c._this());
        System.out.println( "Output of generic: " + s.generic( a ) );
        
        /* insert an any */
        System.out.print("Passing an any");
        Any inner_any = orb.create_any();
        inner_any.insert_string("Hello in any");
        a.insert_any(inner_any);
        System.out.println( "Output of generic: " + s.generic( a ) );
        
        while( counter > 0 )
        {
            System.out.print("Going to sleep to wait for incoming calls");
            Thread.currentThread().sleep(3000);
        }
        orb.shutdown(true);
    }
}


