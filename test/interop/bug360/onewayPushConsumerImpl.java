package test.interop.bug360;

import org.omg.CORBA.*;

public class onewayPushConsumerImpl 
    extends onewayPushConsumerPOA
{
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();

    public onewayPushConsumerImpl(){}

    public void synchronousPush(Any a)
    {
        process( a );
    }

    public void onewayPush(Any a) 
    { 
       process( a );
    }

    private void process(Any a) 
    {
	String result = "<empty>";
	try
	{ 		
            int kind =  a.type().kind().value();

	    switch( kind )
	    {
	    case TCKind._tk_char:
		result = "char: " + a.extract_char();
		break;
	    case TCKind._tk_longlong:
		result = "longlong: " + a.extract_longlong();
		break;
	    case TCKind._tk_short:
		result ="Short: " + a.extract_short();
		break;
	    case TCKind._tk_double:
		result = "Double: " + a.extract_double();
		break;
	    case TCKind._tk_float:
		result = "Float: " + a.extract_float();
		break;
	    case TCKind._tk_string:
		result = "String: " + a.extract_string();
		break;
	    case TCKind._tk_wstring:
		result = "WString: " + a.extract_wstring();
		break;
	    case TCKind._tk_struct:
		result = "struct " ;
		break;
	    case TCKind._tk_union:
		result = "union " ;
		break;
	    case TCKind._tk_objref:	
		result = "obj " ;
		break;
	    case TCKind._tk_any:
		result = "any " ;
		break;
	    case TCKind._tk_alias:

		break;
	    default:
		System.out.println("Unknown, kind " + a.type().kind().value());
		
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	System.out.println(result);
    }
}


