package demo.dii;

/**
 * An example for using the Dynamic Invocation Interface
 */
import java.io.BufferedReader;
import java.io.FileReader;

import org.omg.CORBA.WrongTransaction;

public class Client
{
    public static void main( String[] args ) throws Exception
    {
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

        BufferedReader br =
            new BufferedReader( new FileReader( args[0] ));

        org.omg.CORBA.Object server = orb.string_to_object(br.readLine());

        simpleRequest(orb, server);

        requestWithOutArgs(orb, server);

        onewayRequest(server);

        requestWithReturnValue(orb, server);

        deferredRequest(orb, server);

        exceptionRequest(orb, server);

        defererredRequestWithPolling(orb, server);

        orb.shutdown( false );
    }

    private static void deferredRequest(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server) throws WrongTransaction, Exception
    {
        // deferred asynchronous operation, synchronize with result
        org.omg.CORBA.Request request = server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_string ));

        request.send_deferred();
        request.get_response();
        if( request.env().exception() != null )
        {
            throw request.env().exception();
        }
        else
        {
            System.out.println("[Client] 3: " + request.return_value() );
        }
    }

    private static void defererredRequestWithPolling(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server) throws Exception
    {
        // polling until response is there
        org.omg.CORBA.Request r4 =  server._request("writeNumber");
        r4.add_in_arg().insert_long( 5 );
        r4.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_string ));

        r4.send_deferred();

        while( ! r4.poll_response() )
        {
            /* we could be doing s.th. useful here instead of
                   sleeping...*/
            try
            {
                Thread.sleep(10);
            }
            catch ( InterruptedException i){}
            System.out.print(".");
        }

        if( r4.env().exception() != null )
        {
            throw r4.env().exception();
        }
        else
        {
            System.out.println("[Client] 4: " + r4.return_value() );
        }
    }

    private static void exceptionRequest(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server)
    {
        //send a request which throws an exception
        org.omg.CORBA.Request request = server._request("writeNumberWithEx");
        request.add_in_arg().insert_long( 5 );
        org.omg.CORBA.ExceptionList exceptions = request.exceptions();
        org.omg.CORBA.TypeCode tc =
            orb.create_exception_tc(
                    "IDL:dii/server/e:1.0",
                    "e",
                    new org.omg.CORBA.StructMember[]{
                            new org.omg.CORBA.StructMember(
                                    "why",
                                    orb.create_string_tc(0),
                                    null)
                    }
            );
        exceptions.add( tc );

        request.invoke();
        if( request.env().exception() != null )
        {
            System.out.println("[Client] 5: Got exception " +
                    request.env().exception());
            //Hint: what you get here is a
            //org.omg.CORBA.portable.ApplicationException
            //which contains an any containing the real
            //exception.
        }
    }

    private static void requestWithReturnValue(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server) throws Exception
    {
        // a request with a return value
        org.omg.CORBA.Request request =  server._request("writeNumber");
        request.add_in_arg().insert_long( 5 );
        request.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
        request.invoke();
        if( request.env().exception() != null )
        {
            throw request.env().exception();
        }
        else
        {
            System.out.println("[Client] 2: " + request.return_value() );
        }
    }

    private static void onewayRequest(org.omg.CORBA.Object server)
    {
        // another simple request with a string argumente, oneway
        org.omg.CORBA.Request request =  server._request("notify");
        request.add_in_arg().insert_string("hallo");
        request.send_oneway();
    }

    private static void requestWithOutArgs(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server) throws Exception
    {
        // a request with out args
        org.omg.CORBA.Request request = server._request("add");

        request.add_in_arg().insert_long( 3 );
        request.add_in_arg().insert_long( 4 );

        org.omg.CORBA.Any out_arg = request.add_out_arg();
        out_arg.type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_long) );

        request.set_return_type( orb.get_primitive_tc(
                org.omg.CORBA.TCKind.tk_void));

        request.invoke();

        if( request.env().exception() != null )
        {
            throw request.env().exception();
        }
        else
        {
            System.out.println("[Client] 1: " + out_arg.extract_long() );
        }
    }

    private static void simpleRequest(org.omg.CORBA.ORB orb, org.omg.CORBA.Object server) throws Exception
    {
        // a simple request
        org.omg.CORBA.Request request = server._request("_get_long_number");

        request.set_return_type(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

        request.invoke();

        if( request.env().exception() != null )
        {
            throw request.env().exception();
        }
        else
        {
            System.out.println("[Client] 0: " + request.return_value() );
        }
    }
}
