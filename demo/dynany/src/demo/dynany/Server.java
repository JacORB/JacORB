package demo.dynany;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynArray;
import org.omg.DynamicAny.DynEnum;
import org.omg.DynamicAny.DynSequence;
import org.omg.DynamicAny.DynStruct;
import org.omg.DynamicAny.DynUnion;

public class Server
extends AnyServerPOA
{
    public static org.omg.CORBA.ORB orb;
    DynAnyFactory factory;

    public Server()
    {
        try
        {
            factory = DynAnyFactoryHelper.narrow( orb.resolve_initial_references("DynAnyFactory"));
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    public java.lang.String generic(Any a)
    {
        printAny(a);
        return "done.";
    }

    private void printAny(Any a)
    {
        try
        {
            switch( a.type().kind().value() )
            {
                case TCKind._tk_char:
                    System.out.println("[Server]: char: " + a.extract_char());
                    break;
                case TCKind._tk_short:
                    System.out.println("[Server]: Short: " + a.extract_short());
                    break;
                case TCKind._tk_long:
                    System.out.println("[Server]: Long: " + a.extract_long());
                    break;
                case TCKind._tk_double:
                    System.out.println("[Server]: Double: " + a.extract_double());
                    break;
                case TCKind._tk_float:
                    System.out.println("[Server]: Float: " + a.extract_float());
                    break;
                case TCKind._tk_string:
                    System.out.println("[Server]: String: " + a.extract_string());
                    break;
                case TCKind._tk_enum:
                    DynEnum dynEnum = (DynEnum)factory.create_dyn_any(a);
                    System.out.println("[Server]: ** Enum **");
                    System.out.println( dynEnum.get_as_string());
                    break;
                case TCKind._tk_struct:
                    DynStruct dynstruct = (DynStruct)factory.create_dyn_any( a );
                    org.omg.DynamicAny.NameValuePair[] members = dynstruct.get_members();
                    System.out.println("[Server]: ** Struct **");
                    for( int i = 0; i < members.length; i++ )
                    {
                        System.out.println("[Server]: name: " + members[i].id + " value: " );
                        printAny( members[i].value);
                    }
                    break;
                case TCKind._tk_union:
                    System.out.println("[Server]: ** Union **");
                    DynUnion dynunion = (DynUnion)factory.create_dyn_any( a );
                    System.out.println("[Server]: member name " + dynunion.member_name());
                    printAny(dynunion.member().to_any());
                    break;
                case TCKind._tk_sequence:
                    DynSequence dynseq = (DynSequence)factory.create_dyn_any( a );
                    Any[] contents = dynseq.get_elements();
                    System.out.println("[Server]: ** Sequence of length " + contents.length + " **");
                    for( int i = 0; i < contents.length; i++)
                        printAny( contents[i]);
                    break;
                case TCKind._tk_array:
                    DynArray dynarray = (DynArray)factory.create_dyn_any( a );
                    Any[] array_contents = dynarray.get_elements();
                    System.out.println("[Server]: ** Array **");
                    for( int i = 0; i < array_contents.length; i++)
                        printAny( array_contents[i]);
                    break;
                default:
                    System.out.println("[Server]: Unknown, kind " + a.type().kind().value());

            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) throws Exception
    {
        orb = org.omg.CORBA.ORB.init(args, null);
        org.omg.PortableServer.POA poa =
            org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        poa.the_POAManager().activate();

        org.omg.CORBA.Object o = poa.servant_to_reference(new Server());

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


