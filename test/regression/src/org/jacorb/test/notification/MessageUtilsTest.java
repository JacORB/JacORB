package org.jacorb.test.notification;

import junit.framework.Test;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.test.notification.common.NotificationTestUtils;

/**
 * @author Alphonse Bendt
 */

public class MessageUtilsTest extends NotificationTestCase
{
    EvaluationContext context_;

    NotificationTestUtils testUtils_;

    DefaultMessageFactory messageFactory_;

    public MessageUtilsTest( String name, NotificationTestCaseSetup setup )
    {
        super( name, setup );
    }

    public void setUpTest() throws Exception
    {
        testUtils_ = new NotificationTestUtils(getORB());

        context_ = new EvaluationContext(getEvaluator());

        messageFactory_ = new DefaultMessageFactory(getORB(), getConfiguration());
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
        return NotificationTestCase.suite(MessageUtilsTest.class);
    }
}
