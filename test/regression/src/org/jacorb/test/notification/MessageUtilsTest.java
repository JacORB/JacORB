package org.jacorb.test.notification;

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.node.AbstractTCLNode;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.parser.TCLParser;

import org.omg.CORBA.ORB;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MessageUtilsTest extends TestCase
{

    ApplicationContext appContext_;
    EvaluationContext context_;
    NotificationTestUtils testUtils_;

    public MessageUtilsTest( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        ORB _orb = ORB.init( new String[ 0 ], null );
        POA _poa = POAHelper.narrow( _orb.resolve_initial_references( "RootPOA" ) );
        appContext_ = new ApplicationContext( _orb, _poa );

        testUtils_ = new NotificationTestUtils(_orb);

        context_ = new EvaluationContext();
        context_.setDynamicEvaluator( new DynamicEvaluator( appContext_.getOrb(),
                                                            DynAnyFactoryHelper.narrow( appContext_.getOrb().resolve_initial_references( "DynAnyFactory" ) ) ) );

        context_.setResultExtractor( new ResultExtractor() );

    }

    public void tearDown() throws Exception {
        super.tearDown();
        appContext_.dispose();
    }

    public void testEvaluateCachesResult() throws Exception
    {
        AbstractTCLNode _root = TCLParser.parse( "$.first_name" );
        _root.acceptPreOrder( new TCLCleanUp() );

        Message _event = appContext_.getMessageFactory().newEvent(testUtils_.getTestPersonAny());
        _event.extractValue( context_, ( ComponentName ) _root );

        assertNotNull( context_.lookupResult( "$.first_name" ) );

        _event.extractValue(context_,  ( ComponentName ) _root );

        assertEquals( "firstname", context_.lookupResult( "$.first_name" ).getString() );
    }

    public void testEvaluateCachesAny() throws Exception
    {
        AbstractTCLNode _root = TCLParser.parse( "$.home_address.street" );
        _root.acceptPreOrder( new TCLCleanUp() );

        Message _event = appContext_.getMessageFactory().newEvent(testUtils_.getTestPersonAny());

        _event.extractValue(context_, ( ComponentName ) _root );

        assertNotNull( context_.lookupAny( "$.home_address" ) );
        assertNotNull( context_.lookupAny( "$.home_address.street" ) );

        _event.extractValue( context_, ( ComponentName ) _root );

        context_.eraseResult( "$.home_address.street" );

        _event.extractValue( context_, ( ComponentName ) _root );
    }

    public static Test suite()
    {
        TestSuite suite;

        suite = new TestSuite( MessageUtilsTest.class );

        return suite;
    }

    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }
}
