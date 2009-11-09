package demo.dii;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.omg.CORBA.Any;
import org.omg.CosNaming.*;

public class DiiServer
    extends org.omg.PortableServer.DynamicImplementation
{
    private String[] ids = {"IDL:dii/server:1.0"};

    // singleton ORB as any factory
    org.omg.CORBA.ORB orb = null;

    serverImpl impl = new serverImpl();

    public DiiServer( org.omg.CORBA.ORB orb )
    {
        this.orb = orb;
    }

    /** from Servant */

    public String[] _all_interfaces(org.omg.PortableServer.POA poa,
                                    byte[] objectId)
    {
        return ids;
    }


    public void invoke(org.omg.CORBA.ServerRequest request)
    {
        String op = request.operation();
        try
        {
            if( op.equals("_get_long_number"))
            {
                Any a = orb.create_any();
                a.insert_long( impl.long_number());
                request.set_result( a );
            }
            else if( op.equals("_set_long_number"))
            {
                /* set up an argument list */
                org.omg.CORBA.NVList params = orb.create_list(0);
                /* there is only on argument to this call, i.e. a long */
                Any numAny = orb.create_any();
                numAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
                params.add_value( "", numAny, org.omg.CORBA.ARG_IN.value );

                /* extract the argugments */
                request.arguments( params );

                /* make the call */
                impl.long_number( numAny.extract_long());

                /* set up the any for the result */
                Any s = orb.create_any();
                s.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void ));
                request.set_result( s );
            }
            else if( op.equals("writeNumber") )
            {
                org.omg.CORBA.NVList params = orb.create_list(0);
                Any numAny = orb.create_any();
                numAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
                params.add_value( "", numAny,org.omg.CORBA.ARG_IN.value );
                request.arguments( params );
                Any a = orb.create_any();
                a.insert_string( impl.writeNumber( numAny.extract_long() ));
                request.set_result( a );
            }
            else if( op.equals("add") )
            {
                org.omg.CORBA.NVList params = orb.create_list(0);
                Any argOneAny = orb.create_any();
                Any argTwoAny = orb.create_any();
                Any outArgAny = orb.create_any();
                argOneAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
                argTwoAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
                outArgAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));

                /* add these anys to the parameter list */
                params.add_value( "", argOneAny, org.omg.CORBA.ARG_IN.value );
                params.add_value( "", argTwoAny, org.omg.CORBA.ARG_IN.value );
                params.add_value( "", outArgAny, org.omg.CORBA.ARG_OUT.value );

                /* read in and inout arguments */
                request.arguments( params );

                /* do the computation and fill it into the out arg */
                org.omg.CORBA.IntHolder iHolder = new org.omg.CORBA.IntHolder();
                impl.add( argOneAny.extract_long(), argTwoAny.extract_long(), iHolder );

                outArgAny.insert_long( iHolder.value);

                Any resultAny = orb.create_any();
                resultAny.type(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void) );
                request.set_result( resultAny );
            }
            else if( op.equals("writeNumberWithEx") )
            {
                org.omg.CORBA.NVList params = orb.create_list(0);
                Any numAny = orb.create_any();
                numAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
                params.add_value( "", numAny, org.omg.CORBA.ARG_IN.value );
                request.arguments( params );
                Any a = orb.create_any();
                a.insert_string( impl.writeNumberWithEx( numAny.extract_long() ));
                request.set_result( a );
            }
            else if( op.equals("notify") )
            {
                org.omg.CORBA.NVList params = orb.create_list(0);
                Any stringAny = orb.create_any();
                stringAny.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
                params.add_value( "", stringAny, org.omg.CORBA.ARG_IN.value );
                request.arguments( params );
                impl._notify( stringAny.extract_string() );
                Any s = orb.create_any();
                s.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void ));
                request.set_result( s );
            }
            else if( op.equals("_non_existent") )
            {
                Any s = orb.create_any();
                s.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean ));
                s.insert_boolean( _non_existent());
                request.set_result( s );
            }
            /**
             * the following operations would also have to be implemented
             * by delegating to the superclass DynamicImplementation or Servant
             * but are omitted here for brevity
             */
            else if( op.equals("_all_interfaces") )
            {
                throw new org.omg.CORBA.BAD_OPERATION("Object reference operations not implemented in example!");
            }
            else if( op.equals("_get_interface") )
            {
                throw new org.omg.CORBA.BAD_OPERATION("Object reference operations not implemented in example!");
            }
            else if( op.equals("_is_a") )
            {
                throw new org.omg.CORBA.BAD_OPERATION("Object reference operations not implemented in example");
            }
            else
            {
                throw new org.omg.CORBA.BAD_OPERATION(op + " not found.");
            }
        }
        catch ( org.omg.CORBA.UserException e )
        {
            System.out.println("Caught: " + e );

            Any exceptAny = orb.create_any();
            try
            {
                Class helperClass = Class.forName( e.getClass().getName() + "Helper");
                Class anyClass = Class.forName("org.omg.CORBA.Any");
                java.lang.reflect.Method insert =
                    helperClass.getDeclaredMethod("insert",
						  new Class[] { anyClass, e.getClass() });

                insert.invoke( null, new java.lang.Object[]{exceptAny, e});
            }
            catch ( Exception sfe )
            {
                sfe.printStackTrace();
            }
            request.set_exception( exceptAny );
        }
        catch ( org.omg.CORBA.SystemException e )
        {
            try
            {
                ((org.jacorb.orb.dsi.ServerRequest)request).setSystemException( e );
            }
            catch ( Exception sfe )
            {
                sfe.printStackTrace();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    public static void main( String[] args ) throws Exception
    {
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	    org.omg.PortableServer.POA poa =
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o =
                poa.servant_to_reference(new DiiServer( orb ));

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
        ps.println( orb.object_to_string( o ) );
        ps.close();

        if (args.length == 2)
        {
            File killFile = new File(args[1]);
            while(!killFile.exists())
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run();
        }
    }
}
