package org.jacorb.test.bugs.bugjac631;


/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */


import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.Context;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.UnknownUserException;


/**
 * <code>BugJac631Test</code> is used by testing DII.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac631Test extends ClientServerTestCase
{
    private org.omg.CORBA.Object server = null;

    public BugJac631Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "bugjac631" );
        ClientServerSetup setup =
        new ClientServerSetup( suite,
                               "org.jacorb.test.bugs.bugjac631.ServerImpl" );

        TestUtils.addToSuite(suite, setup, BugJac631Test.class);

        return setup;
    }

    /**
     * <code>testDIIcreaterequest</code> tests that invoking a DII call
     * succeeds.
     *
     * @exception Exception if an error occurs
     */
    public void testDIIcreaterequest () throws Exception
    {
        // Thanks to a rather good tutorial on
        // http://www.unix.com.ua/orelly/java-ent/jenut/ch04_05.htm

        // Now make a dynamic call to the myFunction method.  The first step is
        // to build the argument list.  In this case, there's an int argument
        // and then a inout int argument to the method, so create an NVList of
        // length 2.  Next create an Any object to hold the value of the
        // argument and insert the desired value.  Finally, wrap the Any object
        // with a NamedValue and insert it into the NVList, specifying that it
        // is an input parameter.
        NVList argList = setup.getClientOrb ().create_list (2);

        Any argument1 = setup.getClientOrb ().create_any ();
        argument1.insert_long(-100);
        Any argument2 = setup.getClientOrb ().create_any ();
        argument2.insert_long(0);

        NamedValue nvArg1 = argList.add_value ("paramIn", argument1, org.omg.CORBA.ARG_IN.value);
        NamedValue nvArg2 = argList.add_value ("paramOut", argument2, org.omg.CORBA.ARG_OUT.value);


        // Create an Any object to hold the return value of the method and
        // wrap it in a NamedValue
        Any result = setup.getClientOrb ().create_any();
        result.insert_long (0);

        NamedValue resultVal = setup.getClientOrb ().create_named_value("result", result,
            org.omg.CORBA.ARG_OUT.value);

        Context ctx = null;

        // Create the method request using the default context, the name of
        // the method, the NVList argument list, and the NamedValue for the
        // result.  Then invoke the method by calling invoke() on the Request.
        Request thisReq =
        server._create_request(ctx, "myFunction", argList, resultVal, null, null);
        thisReq.invoke();

        assertTrue (nvArg2.value ().extract_long () == nvArg1.value ().extract_long ());

        // Get the return value from the Request object and output results.
        result = thisReq.result().value();
        assertTrue (result.extract_long () == -100);

        // Should be no exception thrown.
        assertTrue (thisReq.env ().exception () == null);
    }


    /**
     * <code>testDIIcreaterequestsystemexception</code> tests that a DII call can throw a System
     * Exception
     *
     * @exception Exception if an error occurs
     */
    public void testDIIcreaterequestsystemexception () throws Exception
    {
        // Thanks to a rather good tutorial on
        // http://www.unix.com.ua/orelly/java-ent/jenut/ch04_05.htm

        // Now make a dynamic call to the myFunction method.  The first step is
        // to build the argument list.  In this case, there's an int argument
        // and then a inout int argument to the method, so create an NVList of
        // length 2.  Next create an Any object to hold the value of the
        // argument and insert the desired value.  Finally, wrap the Any object
        // with a NamedValue and insert it into the NVList, specifying that it
        // is an input parameter.
        NVList argList = setup.getClientOrb ().create_list (2);

        Any argument1 = setup.getClientOrb ().create_any ();
        argument1.insert_long(10);
        Any argument2 = setup.getClientOrb ().create_any ();
        argument2.insert_long(0);

        NamedValue nvArg1 = argList.add_value ("paramIn", argument1, org.omg.CORBA.ARG_IN.value);
        NamedValue nvArg2 = argList.add_value ("paramOut", argument2, org.omg.CORBA.ARG_OUT.value);


        // Create an Any object to hold the return value of the method and
        // wrap it in a NamedValue
        Any result = setup.getClientOrb ().create_any();
        result.insert_long (0);

        NamedValue resultVal = setup.getClientOrb ().create_named_value("result", result,
            org.omg.CORBA.ARG_OUT.value);

        // Get the local context from the ORB.
        Context ctx = null;

        // Build the exception list.
        ExceptionList exceptions = null;

        // Create the method request using the default context, the name of
        // the method, the NVList argument list, and the NamedValue for the
        // result.  Then invoke the method by calling invoke() on the Request.
        Request thisReq =
        server._create_request(ctx, "myFunction", argList, resultVal, exceptions, null);

        thisReq.invoke();

        result = thisReq.result().value();

        // Should be an exception thrown.
        assertTrue (thisReq.env ().exception () != null);
        assertTrue (thisReq.env ().exception () instanceof INTERNAL);
        assertTrue (thisReq.env ().exception ().toString ().indexOf ("A system exception!") != -1);
        assertTrue (nvArg2.value ().extract_long () != nvArg1.value ().extract_long ());
    }


    /**
     * <code>testDIIcreaterequestuserexception</code> tests that a DII call can throw a User
     * Exception
     *
     * @exception Exception if an error occurs
     */
    public void testDIIcreaterequestuserexception () throws Exception
    {
        // Thanks to a rather good tutorial on
        // http://www.unix.com.ua/orelly/java-ent/jenut/ch04_05.htm

        // Now make a dynamic call to the myFunction method.  The first step is
        // to build the argument list.  In this case, there's an int argument
        // and then a inout int argument to the method, so create an NVList of
        // length 2.  Next create an Any object to hold the value of the
        // argument and insert the desired value.  Finally, wrap the Any object
        // with a NamedValue and insert it into the NVList, specifying that it
        // is an input parameter.
        NVList argList = setup.getClientOrb ().create_list (2);

        Any argument1 = setup.getClientOrb ().create_any ();
        argument1.insert_long(100);
        Any argument2 = setup.getClientOrb ().create_any ();
        argument2.insert_long(0);

        NamedValue nvArg1 = argList.add_value ("paramIn", argument1, org.omg.CORBA.ARG_IN.value);
        NamedValue nvArg2 = argList.add_value ("paramOut", argument2, org.omg.CORBA.ARG_OUT.value);


        // Create an Any object to hold the return value of the method and
        // wrap it in a NamedValue
        Any result = setup.getClientOrb ().create_any();
        result.insert_long (0);

        NamedValue resultVal = setup.getClientOrb ().create_named_value("result", result,
            org.omg.CORBA.ARG_OUT.value);

        // Get the local context from the ORB.
        Context ctx = null;

        // Build the exception list.
        ExceptionList exceptions = new org.jacorb.orb.dii.ExceptionList();
        exceptions.add (MyOwnExceptionHelper.type ());

        // Create the method request using the default context, the name of
        // the method, the NVList argument list, and the NamedValue for the
        // result.  Then invoke the method by calling invoke() on the Request.
        Request thisReq =
        server._create_request(ctx, "myFunction", argList, resultVal, exceptions, null);

        thisReq.invoke();

        result = thisReq.result().value();

        // Should be an exception thrown.
        assertTrue (thisReq.env ().exception () != null);
        assertTrue
           (((UnknownUserException)thisReq.env ().exception ()).except.type ().equal (MyOwnExceptionHelper.type ()));
        assertTrue
           ("Param1 > 0".equals ((MyOwnExceptionHelper.read (((UnknownUserException)thisReq.env ().exception ()).except.create_input_stream ())).message));
        assertTrue (nvArg2.value ().extract_long () != nvArg1.value ().extract_long ());
    }


    /**
     * <code>testDIIcreaterequestunexpectedexception</code> tests that a DII call can throw a
     * User Exception that has not been configured the via the exception list.
     *
     * @exception Exception if an error occurs
     */
    public void testDIIcreaterequestunexpectedexception () throws Exception
    {
        // Thanks to a rather good tutorial on
        // http://www.unix.com.ua/orelly/java-ent/jenut/ch04_05.htm

        // Now make a dynamic call to the myFunction method.  The first step is
        // to build the argument list.  In this case, there's an int argument
        // and then a inout int argument to the method, so create an NVList of
        // length 2.  Next create an Any object to hold the value of the
        // argument and insert the desired value.  Finally, wrap the Any object
        // with a NamedValue and insert it into the NVList, specifying that it
        // is an input parameter.
        NVList argList = setup.getClientOrb ().create_list (2);

        Any argument1 = setup.getClientOrb ().create_any ();
        argument1.insert_long(100);
        Any argument2 = setup.getClientOrb ().create_any ();
        argument2.insert_long(0);

        NamedValue nvArg1 = argList.add_value ("paramIn", argument1, org.omg.CORBA.ARG_IN.value);
        NamedValue nvArg2 = argList.add_value ("paramOut", argument2, org.omg.CORBA.ARG_OUT.value);


        // Create an Any object to hold the return value of the method and
        // wrap it in a NamedValue
        Any result = setup.getClientOrb ().create_any();
        result.insert_long (0);

        NamedValue resultVal = setup.getClientOrb ().create_named_value("result", result,
            org.omg.CORBA.ARG_OUT.value);

        // Get the local context from the ORB.
        Context ctx = null;

        // Build the exception list.
        ExceptionList exceptions = null;
        // new org.jacorb.orb.dii.ExceptionList();
        // exceptions.add (MyOwnExceptionHelper.type ());

        // Create the method request using the default context, the name of
        // the method, the NVList argument list, and the NamedValue for the
        // result.  Then invoke the method by calling invoke() on the Request.
        Request thisReq =
        server._create_request(ctx, "myFunction", argList, resultVal, exceptions, null);

        thisReq.invoke();

        result = thisReq.result().value();

        // Should be an exception thrown.
        assertTrue (thisReq.env ().exception () != null);
        assertTrue (thisReq.env ().exception () instanceof UNKNOWN);
        assertTrue (thisReq.env ().exception ().toString ().indexOf ("Caught an unknown exception with typecode id of IDL:org/jacorb/test/bugs/bugjac631/MyOwnException") != -1);
        assertTrue (nvArg2.value ().extract_long () != nvArg1.value ().extract_long ());
    }


    /**
     * <code>testDIIcreaterequestnullpointerexception</code> tests that a DII call can throw a
     * Java Exception that has not been configured the via the exception list.
     *
     * @exception Exception if an error occurs
     */
    public void testDIIcreaterequestnullpointerexception () throws Exception
    {
        // Thanks to a rather good tutorial on
        // http://www.unix.com.ua/orelly/java-ent/jenut/ch04_05.htm

        // Now make a dynamic call to the myFunction method.  The first step is
        // to build the argument list.  In this case, there's an int argument
        // and then a inout int argument to the method, so create an NVList of
        // length 2.  Next create an Any object to hold the value of the
        // argument and insert the desired value.  Finally, wrap the Any object
        // with a NamedValue and insert it into the NVList, specifying that it
        // is an input parameter.
        NVList argList = setup.getClientOrb ().create_list (2);

        Any argument1 = setup.getClientOrb ().create_any ();
        argument1.insert_long(20);
        Any argument2 = setup.getClientOrb ().create_any ();
        argument2.insert_long(0);

        NamedValue nvArg1 = argList.add_value ("paramIn", argument1, org.omg.CORBA.ARG_IN.value);
        NamedValue nvArg2 = argList.add_value ("paramOut", argument2, org.omg.CORBA.ARG_OUT.value);


        // Create an Any object to hold the return value of the method and
        // wrap it in a NamedValue
        Any result = setup.getClientOrb ().create_any();
        result.insert_long (0);

        NamedValue resultVal = setup.getClientOrb ().create_named_value("result", result,
            org.omg.CORBA.ARG_OUT.value);

        // Get the local context from the ORB.
        Context ctx = null;

        // Build the exception list.
        ExceptionList exceptions = null;
        // new org.jacorb.orb.dii.ExceptionList();
        // exceptions.add (MyOwnExceptionHelper.type ());

        // Create the method request using the default context, the name of
        // the method, the NVList argument list, and the NamedValue for the
        // result.  Then invoke the method by calling invoke() on the Request.
        Request thisReq =
        server._create_request(ctx, "myFunction", argList, resultVal, exceptions, null);

        thisReq.invoke();

        result = thisReq.result().value();

        // Should be an exception thrown.
        assertTrue (thisReq.env ().exception () != null);
        assertTrue (thisReq.env ().exception () instanceof UNKNOWN);
        assertTrue (thisReq.env ().exception ().toString ().indexOf ("A nullpointer exception") != -1);
        assertTrue (nvArg2.value ().extract_long () != nvArg1.value ().extract_long ());
    }
}
