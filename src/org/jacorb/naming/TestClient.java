package org.jacorb.naming;

import org.omg.CosNaming.*;

public class TestClient
{
    public static void list( NamingContextExt n, String indent)
    {
	
	try
	{
	    BindingListHolder blsoh = 
		new BindingListHolder(new Binding[0]);
	    BindingIteratorHolder bioh = 
		new BindingIteratorHolder();

	    n.list( 0, blsoh, bioh );

	    BindingHolder bh = new BindingHolder();

	    if( bioh.value == null )
		return; 

	    while( bioh.value.next_one( bh ))
	    {
		Name name = new Name( bh.value.binding_name);
		System.out.print( indent + name );
		if( bh.value.binding_type.value() == BindingType._ncontext )
		{
		    String _indent = indent + "\t";
		    System.out.println("/");
		    list(NamingContextExtHelper.narrow(n.resolve(name.components())), _indent);
		}
		else
		    System.out.println();
	    }

	} 
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }


    public static void main(String args[]) 
    { 
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

	    NamingContextExt rootContext = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    /* create new contexts */

	    NameComponent[] nameComp = new NameComponent[1];
	    nameComp[0] = new NameComponent("first","context");
	    NamingContextExt firstsubContext = 
                NamingContextExtHelper.narrow(rootContext.bind_new_context( nameComp ));

	    nameComp = new NameComponent[1];
	    nameComp[0] = new NameComponent("second","context");
	    NamingContextExt secondsubContext =
                NamingContextExtHelper.narrow(rootContext.bind_new_context( nameComp ));

	    nameComp = new NameComponent[1];
	    nameComp[0] = new NameComponent("subsub","context");
	    NamingContextExt subsubContext = 
                NamingContextExtHelper.narrow(secondsubContext.bind_new_context( nameComp ));

	    /* list */
	    list( rootContext ,"");

	    if( args.length > 0 )
		System.out.println(rootContext.to_url( args[0], rootContext.to_string( nameComp )));

	    System.out.println("Wait for the NS to go down, then press <enter> to continue");

	    int n = 0;

	    while( '\n' != System.in.read());

	    list( secondsubContext, "");
	    
	    System.out.println("done. ");       
	    System.exit(0);
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}


