package org.jacorb.test.naming;

/**
 * ContextTest.java
 *
 * Naming Service Tests for creating, resolving, and destroying contexts
 *
 */

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;

import junit.framework.*;
import junit.extensions.TestSetup;

import org.jacorb.test.common.ORBSetup;

public class ContextTest extends TestCase
{
    private static org.omg.CORBA.ORB orb = null;   
    private static NamingContextExt rootContext =  null;
    private static  NameComponent[] firstName, secondName, thirdName , failureName;

    private static class Setup extends TestSetup
    {
        public Setup (Test test)
        {
            super (test);
        }

        protected void setUp ()
        {
            org.omg.CORBA.Object obj = null;
            
            orb = ORBSetup.getORB ();
            try
            {
                obj = orb.resolve_initial_references ("NameService");
            }
            catch (org.omg.CORBA.ORBPackage.InvalidName ex)
            {
                fail ("Failed to resolve NameService: " + ex);
            }
            try
            {
                rootContext = 	
                    NamingContextExtHelper.narrow( orb.resolve_initial_references("NameService"));
            }
            catch (Throwable ex)
            {
                fail ("Failed to narrow to NamingContext: " + ex);
            }
            firstName = new NameComponent[1];
            firstName[0] = new NameComponent("first","context");

            secondName = new NameComponent[1];
            secondName[0] = new NameComponent("second","context");
        
            thirdName = new NameComponent[1];
            thirdName[0] = new NameComponent("third","context");

            failureName = new NameComponent[2];
            failureName[0] = secondName[0];
            failureName[1] = thirdName[0];
        }


        protected void tearDown ()
        {
        }

    }




   public ContextTest (String name)
   {
      super (name);
   }


   public static Test suite()
   {
      TestSuite suite = new TestSuite("Naming Context Tests");
      Setup setup = new Setup( suite );
      ORBSetup osetup = new ORBSetup( setup );

      suite.addTest (new  ContextTest("testCreateContextSuccess"));
      suite.addTest (new  ContextTest("testCreateContextFailure"));
      suite.addTest (new  ContextTest("testUnbindContext"));

      return osetup;
   }

    public void testUnbindContext()
    {
        try
        {
            rootContext.unbind( failureName );
            rootContext.unbind( secondName );
            rootContext.unbind( firstName );
        }
        catch( Exception e) 
	{
	    fail("Exception  " + e);
        }

    }


   public void testCreateContextFailure()
   {
        /* create a subcontext with an existing name, must fail with
           AlreadyBound! */
        try
        {
            NamingContextExt failContext =
                NamingContextExtHelper.narrow(rootContext.bind_new_context( failureName ));               

            fail("NamingContext was expected to be already bound!");

	}
	catch( AlreadyBound e )
        {
            assertTrue ("We should get here!", true );
        } 
	catch( Exception e ) 
	{
	    fail("Expected AlreadyBound exception, but got " + e);
        }
   }

   /**
    * Test creating and resolving contexts
    */

   public void testCreateContextSuccess()
   {


        /* create new contexts */
	try
	{
	    NamingContextExt firstsubContext = 
                NamingContextExtHelper.narrow( rootContext.bind_new_context( firstName ) );
        }
        catch (Exception ex)
        {
            fail ("Failed to bind first name: " + ex);
        }
        
        org.omg.CORBA.Object obj = null;
        try
        {
            obj = rootContext.resolve( firstName );
        }
        catch (Exception ex)
        {
            fail ("Failed to resolve first name: " + ex);
        }
       
        /* create subcontexts */

        try
        {
            NamingContextExt secondsubContext =
                NamingContextExtHelper.narrow( rootContext.bind_new_context( secondName ));               

            NamingContextExt subsubContext = 
                NamingContextExtHelper.narrow( secondsubContext.bind_new_context( thirdName ));
	
	}
	catch (Exception e) 
	{
	    fail( "Exception " + e );
        }


    }
}



