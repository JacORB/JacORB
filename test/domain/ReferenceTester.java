package org.jacorb.orb.domain.test;

import org.jacorb.orb.domain.*;
import org.omg.CosNaming.*;
import org.jacorb.util.Debug;
import java.io.*;

/**
 * This program tests the object two reference operations.
 * @author Herbert Kiefer
 * @version $Revision$
 */
public class ReferenceTester  {
  
 public static void main( String[] args )

  {
    new ReferenceTester(args);
  }

  ReferenceTester(String[] args) {

    int n= 10;
    Domain dm[]= new Domain[n];
    TestPolicy pol[]= new TestPolicy[10];
    org.omg.CORBA.Policy policy=     null;
    TestPolicy           testPolicy= null;
    org.jacorb.orb.domain.Domain[] list;
    org.omg.CORBA.Object object= null; // the object to test
    String [] lines={
      "get policy", 
      "get domain managers", 
      "-------------------", 
      "change test policy ",
      "load obj from ns  ",
      "load obj from file",
      "finish"};
    org.omg.CORBA.ORB orb= null;
    NamingContextExt nc= null;
    NameComponent [] name = new NameComponent[1];


    
    try 
      {
	orb = org.omg.CORBA.ORB.init(args,null);
	// get name service
	nc = NamingContextExtHelper.narrow
	  (orb.resolve_initial_references("NameService"));

	// get test policies
	    for (int i= 0; i < 10; i++) 
	    {
	      name[0] = new NameComponent("TestPolicy"+i, "whatever");
	      pol[i]= TestPolicyHelper.narrow(nc.resolve(name));
	    
	    }
	if ( args.length > 0 )
	  { // take object reference from args
	    if( args[0].equals("-f"))
	      object= loadFromFile(args[1], orb);
	    else object=  orb.string_to_object(args[0]);
	    
	  }
	else object= pol[0];
      } 
    catch (IOException io) { System.out.println(io); usage(); }
    catch (java.lang.RuntimeException run) { System.out.println(run); usage(); }
    catch (org.omg.CosNaming.NamingContextPackage.NotFound notFound) 
      {
	System.out.println("WARNING: test policies not available, (run the test server)");
	// System.exit(-1);
      }
    catch (Exception e) {	Debug.output(1,e); }
      
    // main loop
    boolean end= false;
    int j, answer;
    
  try {

    while (!end)

      {
	if (object != null) 
	  System.out.println("current: "+ Util.toID( object.toString() ));     
	else System.out.println("WARNING: current object is null !");     

	answer= Util.textmenu(lines);

	System.out.println("answer is " + answer);
	switch (answer) {

	case 1: // get policy
	  System.out.println("of which type ?: ");
          int i= Util.readInt();
	  try 
	    {
	      System.out.println("policy is :"
			       + Util.downcast( object._get_policy(i))) ;
	    } 
	  catch (org.omg.CORBA.INV_POLICY invalid)
	    {	      
	      System.out.println("no policy defined.");
	    }
	   catch (org.omg.CORBA.COMM_FAILURE fail)
	    {	      
	      System.out.println("connection failed");
	    }
	  break;

	case 2: // get domain managers
	  try
	    {
	      list= (Domain[]) object._get_domain_managers();
	      Util.quicksort(0, list.length-1, list);

	      System.out.println(list.length + " domain(s):");
	      for (i= 0; i < list.length; i++) 
		System.out.println("\t" + list[i].name());
	    }
	  catch (org.omg.CORBA.COMM_FAILURE fail)
	    {	      
	      System.out.println("connection failed");
	    }
	  break;

	case 3: // change test policy 
	  System.out.println("to which type ?: ");
          object= pol[ Util.readInt() ];

	  break;

	case 4: // load object from name server 
	  if (nc != null)
	    {
	      System.out.print("Enter name of object: ");
	      String objName= null;
	      try 
		{     
		  DataInputStream input= new DataInputStream(System.in);
		  objName= input.readLine(); 
		  System.out.println("name is " + objName);
		  object= nc.resolve_str(objName);
		}
	      catch (IOException e)
		{ 
		  System.out.println("readChar: couldn't read line. reason: " + e);
		  break;
		}
	      catch (org.omg.CosNaming.NamingContextPackage.NotFound notFound)
		{ 
		  System.out.println(notFound + ": " +  objName);
		  break;
		}


	    }
	  else System.out.println("name server not available.");

	  break;
	  
	case 5: // load object from file
	  org.omg.CORBA.Object result= loadFromFile(orb);
	  if (result != null) object= result;
	  break;

	case 6: 
	  end= true; 
	  break;

	}
      }
  } catch (Exception e) { Debug.output(1,e); }
  }
  
  org.omg.CORBA.Object loadFromFile( org.omg.CORBA.ORB orb)
  {

    javax.swing.JFileChooser dialog= new javax.swing.JFileChooser();
    int answer= dialog.showOpenDialog(null);
    try 
      {
	if (answer == javax.swing.JFileChooser.APPROVE_OPTION)
	  return loadFromFile( dialog.getSelectedFile().toString(), orb);
	else return null;
      }
    catch (IOException ioex)
      {
	Debug.output(1, ioex);
	return null;
      }
  } 
  org.omg.CORBA.Object loadFromFile(String filename, org.omg.CORBA.ORB orb)
  throws IOException
  {
    // if ( args.length > 0 )
    
    
    String iorString= null;
    
    //	if( args[0].equals("-f"))
	
    String line= null;
    // try
      {
	
	BufferedReader br = new BufferedReader (new FileReader(filename),2048 );
	line = br.readLine();
			System.out.print ( line );
	if ( line != null ) 
	  { 
	    iorString = line;
	    while ( line != null ) 
	      { 
		line = br.readLine();
		if ( line != null ) iorString = iorString + line;
		 System.out.print ( line );
	      }
	  }
	System.out.println ( "red IOR from file: filename" );
      } 
    // catch ( IOException ioe ) { ioe.printStackTrace(); }

    return orb.string_to_object(iorString);
} // readFromFile

   private static void usage()
    {
	System.err.println
	  ("Usage: java org.jacorb.orb.domain.test.ReferenceTester <IOR> | -f <filename>");
	System.exit(1);
    }

} // ReferenceTester






