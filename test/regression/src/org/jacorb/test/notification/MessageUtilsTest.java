package org.jacorb.test.notification;

import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.DynamicEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.interfaces.Message;

import org.omg.DynamicAny.DynAnyFactoryHelper;

import junit.framework.Test;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MessageUtilsTest extends NotificationTestCase
{
    EvaluationContext context_;

    NotificationTestUtils testUtils_;

    MessageFactory messageFactory_;

    public MessageUtilsTest( String name, NotificationTestCaseSetup setup )
    {
        super( name, setup );
    }

    public void setUp() throws Exception
    {
        testUtils_ = new NotificationTestUtils(getORB());

        context_ = new EvaluationContext();
        DynamicEvaluator _dynEvaluator =
            new DynamicEvaluator(DynAnyFactoryHelper.narrow( getORB().resolve_initial_references( "DynAnyFactory" ) ) );

        _dynEvaluator.configure(getConfiguration());

        context_.setDynamicEvaluator( _dynEvaluator );

        messageFactory_ = new MessageFactory();
        messageFactory_.configure( getConfiguration() );
    }


    public void testEvaluateCachesResult() throws Exception
    {
        AbstractTCLNode _root = TCLParser.parse( "$.first_name" );
        _root.acceptPreOrder( new TCLCleanUp() );

        Message _event = messageFactory_.newMessage(testUtils_.getTestPersonAny());
        _event.extractValue( context_, ( ETCLComponentName ) _root );

        assertNotNull( context_.lookupResult( "$.first_name" ) );

        _event.extractValue(context_,  ( ETCLComponentName ) _root );

        assertEquals( "firstname", context_.lookupResult( "$.first_name" ).getString() );
    }


    public void testEvaluateCachesAny() throws Exception
    {
        AbstractTCLNode _root = TCLParser.parse( "$.home_address.street" );
        _root.acceptPreOrder( new TCLCleanUp() );

        Message _event = messageFactory_.newMessage(testUtils_.getTestPersonAny());

        _event.extractValue(context_, ( ETCLComponentName ) _root );

        assertNotNull( context_.lookupAny( "$.home_address" ) );
        assertNotNull( context_.lookupAny( "$.home_address.street" ) );

        _event.extractValue( context_, ( ETCLComponentName ) _root );

        context_.eraseResult( "$.home_address.street" );

        _event.extractValue( context_, ( ETCLComponentName ) _root );
    }


    public static Test suite() throws Exception
    {
        return NotificationTestCase.notificationSuite(MessageUtilsTest.class);
    }
}
