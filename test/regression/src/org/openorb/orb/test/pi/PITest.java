/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.pi;

import java.util.Properties;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.test.common.ORBSetup;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.Policy;
import org.omg.CORBA.UNKNOWN;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.openorb.orb.test.adapter.poa.Hello;
import org.openorb.orb.test.adapter.poa.HelloHelper;
import org.openorb.orb.test.adapter.poa.HelloPOA;

/**
 * Tests marshaling and unmarshaling of various iiop types.
 *
 * @author Chris Wood
 */
public class PITest extends TestCase
{
   private Hello svrRef = null;
   private Hello m_cltRef = null;

   private static final int SEND_REQ = 0x1;
   private static final int SEND_POLL = 0x2;
   private static final int SEND_SC = 0x8;
   private static final int RECV_REQSC = 0x10;
   private static final int RECV_REQ = 0x20;
   private static final int RECV_SC = 0x80;
   private static final int SEND_REPL = 0x100;
   private static final int SEND_EXPT = 0x200;
   private static final int SEND_OTHR = 0x400;
   private static final int SEND_REPL_SC = 0x800;
   private static final int RECV_REPL = 0x1000;
   private static final int RECV_EXPT = 0x2000;
   private static final int RECV_OTHR = 0x4000;
   private static final int RECV_REPL_SC = 0x8000;

   private static final int NOR_PATH = SEND_REQ | RECV_REQSC | RECV_REQ | SEND_REPL | RECV_REPL;
   private static final int NOR_SC = SEND_SC | RECV_SC | SEND_REPL_SC | RECV_REPL_SC;

   private static final int TEST_SCID = 0x444f7F01;

   private static int s_throwExcept;
   private static int s_visitMask;
   private static int s_retryCount;

   private static int s_slotID;
   private static org.omg.CORBA.Any s_any;

   private org.omg.CORBA.ORB orb;
   private POA rootPOA;
   private ORBSetup clientSetup;

   /**
    * The constructor is responsible for constructing a test category and
    * adding the suite of test cases. It throws CWClassConstructorException
    * if it cannot construct the category.
    *
    * @param name The name of the test case.
    */
   public PITest( String name )
   {
      super( name );
      s_visitMask = 0;
      s_throwExcept = 0;
      s_retryCount = 0;
   }

   private void init ()
   {
      try
      {
         orb = clientSetup.getORB();
         rootPOA = clientSetup.getRootPOA ();

         Policy [] pols = new Policy [1];
         pols[0] = rootPOA.create_implicit_activation_policy
            (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
         POA newPOA = rootPOA.create_POA ("NewPOA",
                                          rootPOA.the_POAManager (),
                                          pols);

         svrRef = ( new HelloImpl (newPOA))._this (orb);

         m_cltRef = HelloHelper.narrow (svrRef);
         s_any = orb.create_any();
         s_any.insert_boolean (true);

         s_slotID = 0;
         s_visitMask = 0;
         s_throwExcept = 0;
         s_retryCount = 0;
      }
      catch ( org.omg.CORBA.UserException ex )
      {
         fail( "exception during setup:" + ex.toString() );
      }
   }

   protected void setUp () throws Exception
   {
       Properties props = new java.util.Properties();
       props.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                          + EmptyInitializer.class.getName(), "" );

       props.setProperty ("jacorb.codeSet", "on");
       props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.standard_init",
                          "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");

       clientSetup = new ORBSetup (this, props);
       clientSetup.setUp ();
   }

   protected void tearDown () throws Exception
   {
      svrRef._release ();
      m_cltRef._release ();
      svrRef = null;
      m_cltRef = null;

      clientSetup.tearDown ();
   }

   /**
    * Test complete request call.
    */
   public void testCompleteCall()
   {
       init ();

       s_throwExcept = 0;
       try
       {
          m_cltRef.hello_op( "A Message from testCompleteCall()..." );
       }
       catch (UNKNOWN ex )
       {
          // The PI throws this exception by intention
       }

       assertEquals( "Complete call did not visit all interception points.",
                     NOR_PATH,
                     s_visitMask);

   }

   /**
    * Test complete request call with service contexts.
    *
    * @exception org.omg.CORBA.UserException if any of the test cases fails
    */
   public void testCompleteCallWithSCs()
      throws org.omg.CORBA.UserException
   {
       init ();

       s_throwExcept = 0;
       Current curr = (Current) orb.resolve_initial_references ("PICurrent");
       curr.set_slot( s_slotID, s_any );

       try
       {
          m_cltRef.hello_op( "A message from testCompleteCallWithSCs()..." );
       }
       catch ( UNKNOWN ex )
       {
          // The PI throws this exception by intention
       }

       assertEquals( "Complete call did not visit all interception points.",
                     NOR_PATH | NOR_SC,
                     s_visitMask);
   }


   /**
    * Abort at send_request.
    */
   public void testToSendRequest()
   {
       init ();

       s_throwExcept = SEND_REQ;

       try
       {
          m_cltRef.hello_op( "A message from testToSendRequest()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        SEND_REQ,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        SEND_REQ,
                        ex.minor );
       }
   }

   /**
    * Abort at receive_request_service_contexts.
    */
   public void testToRecvRequestSC()
   {
      init ();

      s_throwExcept = RECV_REQSC;
      try
      {
         m_cltRef.hello_op( "A message from testToRecvRequestSC()..." );
         fail( "expected exception" );
      }
      catch ( UNKNOWN ex )
      {
         assertEquals( "Complete call did not visit all interception points.",
                       SEND_REQ | RECV_REQSC | RECV_EXPT,
                       s_visitMask);

         assertEquals( "Exception thrown in wrong place",
                       RECV_REQSC,
                       ex.minor);
      }
   }

   /**
    * Abort at receive_request_service_contexts and recieve_exception.
    */
   public void testToRecvRequestSCReceiveException()
   {
       init ();

       s_throwExcept = RECV_REQSC | RECV_EXPT;
       try
       {
          m_cltRef.hello_op( "A message from testToRecvRequestSCReceiveException()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        SEND_REQ | RECV_REQSC | RECV_EXPT ,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        RECV_REQSC | RECV_EXPT,
                        ex.minor);
       }
   }

   /**
    * Abort at receive_request.
    */
   public void testToRecvRequest()
   {
       init ();

       s_throwExcept = RECV_REQ;
       try
       {
          m_cltRef.hello_op( "A message from testToRecvRequest()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        SEND_REQ | RECV_REQSC | RECV_REQ | SEND_EXPT | RECV_EXPT,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        RECV_REQ,
                        ex.minor);
       }
   }

   /**
    * Abort at receive_request and recieve_exception.
    */
   public void testToRecvRequestRecvExcept()
   {
       init ();

       s_throwExcept = RECV_REQ | RECV_EXPT;
       try
       {
          m_cltRef.hello_op( "A message from testToRecvRequestRecvExcept()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        SEND_REQ | RECV_REQSC | RECV_REQ | SEND_EXPT | RECV_EXPT ,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        RECV_REQ | RECV_EXPT,
                        ex.minor);
       }
   }

   /**
    * Abort at send_reply.
    */
   public void testToSendReply()
   {
       init ();

       s_throwExcept = SEND_REPL;
       try
       {
          m_cltRef.hello_op( "A message from testToSendReply()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        SEND_REQ | RECV_REQSC | RECV_REQ | SEND_REPL | RECV_EXPT,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        SEND_REPL,
                        ex.minor);
       }
   }

   /**
    * Abort at send_reply.
    */
   public void testToRecvReply()
   {
       init();

       s_throwExcept = RECV_REPL;
       try
       {
          m_cltRef.hello_op( "A message from testToRecvReply()..." );
          fail( "expected exception" );
       }
       catch ( UNKNOWN ex )
       {
          assertEquals( "Complete call did not visit all interception points.",
                        NOR_PATH,
                        s_visitMask);

          assertEquals( "Exception thrown in wrong place",
                        RECV_REPL,
                        ex.minor);
       }
   }


   static class HelloImpl
      extends HelloPOA
   {
      private POA m_poa;

      HelloImpl( POA poa )
      {
         m_poa = poa;
      }

      public void hello_op( String msg )
      {
         System.out.println( msg );
      }

      public POA _default_POA()
      {
         return m_poa;
      }
   }


   /**
    * An empty ORB initializer class.
    */
   public static class EmptyInitializer
      extends org.omg.CORBA.LocalObject
      implements org.omg.PortableInterceptor.ORBInitializer
   {
      /**
       * Called before init of the actual ORB.
       *
       * @param info The ORB init info.
       */
      public void pre_init( org.omg.PortableInterceptor.ORBInitInfo info )
      {
         try
         {
            s_slotID = info.allocate_slot_id();
            info.add_server_request_interceptor( new EmptyServerInterceptor() );
            info.add_client_request_interceptor( new EmptyClientInterceptor() );
            info.add_ior_interceptor( new EmptyIORInterceptor() );
         }
         catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
         {
            fail( "unexpected exception received: " + ex );
         }
      }

      /**
       * Called after init of the actual ORB.
       *
       * @param info The ORB init info.
       */
      public void post_init( org.omg.PortableInterceptor.ORBInitInfo info )
      {
      }
   }

   static class EmptyIORInterceptor
      extends org.omg.CORBA.LocalObject
      implements org.omg.PortableInterceptor.IORInterceptor
   {
      public String name()
      {
         return "";
      }

      public void destroy()
      {
      }

      public void establish_components (org.omg.PortableInterceptor.IORInfo info)
      {
         info.add_ior_component (new org.omg.IOP.TaggedComponent (TEST_SCID, new byte[ 0 ]));
      }
   }

   static class EmptyClientInterceptor
      extends org.omg.CORBA.LocalObject
      implements org.omg.PortableInterceptor.ClientRequestInterceptor
   {
      public String name()
      {
         return "";
      }

      public void destroy()
      {
      }

      public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         if ( ( s_visitMask & SEND_REQ ) != 0 )
         {
            s_retryCount++;
         }
         s_visitMask = SEND_REQ;

         if ( s_throwExcept == SEND_REQ )
         {
            throw new UNKNOWN( SEND_REQ,
                               CompletionStatus.COMPLETED_NO );
         }

         // add service context.
         try
         {
            ri.get_slot (s_slotID);
            ri.get_slot (s_slotID).type ();
            ri.get_slot (s_slotID).type ().kind ();

            if ( ri.get_slot( s_slotID ).type().kind() != org.omg.CORBA.TCKind.tk_null )
            {


               ri.add_request_service_context( new org.omg.IOP.ServiceContext( TEST_SCID,
                                                                               new byte[ 0 ] ),
                                               true );

               s_visitMask = s_visitMask | SEND_SC;
            }
         }
         catch ( org.omg.PortableInterceptor.InvalidSlot ex )
         {
            fail( ex.toString() );
         }

         // request information.
         ri.request_id();

         assertEquals( "Operation name not correct", "hello_op", ri.operation());

         assertTrue( "No response expected for request with response",
                     ri.response_expected() );

         assertEquals( "Incorrect sync scope", 3, ri.sync_scope() );

         // target information
         ri.target();
         ri.effective_target();
         ri.effective_profile();
         ri.get_effective_component( org.omg.IOP.TAG_CODE_SETS.value );
         ri.get_effective_components( org.omg.IOP.TAG_CODE_SETS.value );

         try
         {
            ri.arguments();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.exceptions();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.contexts();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.operation_context();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
      }

      public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
      {
         s_visitMask = s_visitMask | SEND_POLL;

         if ( ( s_throwExcept & SEND_POLL ) != 0 )
         {
            throw new UNKNOWN( SEND_POLL,
                               CompletionStatus.COMPLETED_YES );
         }
      }

      public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
      {
         s_visitMask = s_visitMask | RECV_REPL;

         if ( 0 != ( s_throwExcept & RECV_REPL ) )
         {
            throw new UNKNOWN( RECV_REPL,
                               CompletionStatus.COMPLETED_YES );
         }

         try
         {
            ri.get_reply_service_context( TEST_SCID );
            s_visitMask = s_visitMask | RECV_REPL_SC;
         }
         catch ( org.omg.CORBA.BAD_PARAM ex )
         {
            /**
             * If a reply_service_context is not found for the request then
             * a BAD_PARAM is thrown.  This is OK for tests that don't have a
             * service context but not for tests where a service context should
             * have been found.
             */
         }

         try
         {
            ri.result();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request results. These will always fail
         }
      }

      public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | RECV_OTHR;

         if ( 0 != ( s_throwExcept & RECV_OTHR ) )
         {
            throw new UNKNOWN( RECV_OTHR,
                               CompletionStatus.COMPLETED_YES );
         }
      }

      public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | RECV_EXPT;

         if ( ri.received_exception_id().equals( org.omg.CORBA.UNKNOWNHelper.id() )
              && 0 != ( s_throwExcept & RECV_EXPT ) )
         {
            UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
               ri.received_exception() );

            uex.minor = uex.minor | RECV_EXPT;

            throw uex;
         }
      }
   }

   static class EmptyServerInterceptor
      extends org.omg.CORBA.LocalObject
      implements org.omg.PortableInterceptor.ServerRequestInterceptor
   {
      public java.lang.String name()
      {
         return "";
      }

      public void destroy()
      {
      }

      public void receive_request_service_contexts(
         org.omg.PortableInterceptor.ServerRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | RECV_REQSC;

         if ( 0 != ( s_throwExcept & RECV_REQSC ) )
         {
            throw new UNKNOWN( RECV_REQSC,
                               CompletionStatus.COMPLETED_NO );
         }
         // request information.
         ri.request_id();
         assertEquals( "Operation name not correct", "hello_op", ri.operation());

         assertTrue( "No response expected for request with response",
                     ri.response_expected() );

         assertEquals( "Incorrect sync scope", 3, ri.sync_scope());

         try
         {
            ri.get_request_service_context( TEST_SCID );
            s_visitMask = s_visitMask | RECV_SC;

            ri.set_slot( s_slotID, s_any );
         }
         catch ( org.omg.CORBA.BAD_PARAM ex )
         {
            // normal !?
         }
         catch ( org.omg.PortableInterceptor.InvalidSlot ex )
         {
            fail( "unexpected exception received: " + ex );
         }
      }

      public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | RECV_REQ;

         if ( 0 != ( s_throwExcept & RECV_REQ ) )
         {
            throw new UNKNOWN( RECV_REQ,
                               CompletionStatus.COMPLETED_NO );
         }

         // request information
         ri.object_id();
         byte []aid = ri.adapter_id();

         assertTrue (aid != null);
         assertTrue (aid.length != 0);

         String adapters [] = ri.adapter_name ();
         assertTrue (adapters != null);
         assertTrue (adapters[0].equals ("RootPOA"));
         assertTrue (adapters[1].equals ("NewPOA"));

         ri.target_most_derived_interface();

         assertTrue( "target does not implement object",
                     ri.target_is_a( "IDL:omg.org/CORBA/Object:1.0" ) );

         try
         {
            ri.arguments();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.exceptions();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.contexts();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
         try
         {
            ri.operation_context();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request parameters. These will always fail
         }
      }

      public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
      {
         s_visitMask = s_visitMask | SEND_REPL;

         if ( 0 != ( s_throwExcept & SEND_REPL ) )
         {
            throw new UNKNOWN( SEND_REPL,
                               CompletionStatus.COMPLETED_YES );
         }

         try
         {
            if ( ri.get_slot( s_slotID ).type().kind() != org.omg.CORBA.TCKind.tk_null )
            {
               ri.add_reply_service_context( new org.omg.IOP.ServiceContext( TEST_SCID,
                                                                             new byte[ 0 ] ),
                                             true );

               s_visitMask = s_visitMask | SEND_REPL_SC;
            }
         }
         catch ( org.omg.PortableInterceptor.InvalidSlot ex )
         {
            fail( ex.toString() );
         }
      }

      public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | SEND_EXPT;

         org.omg.CORBA.Any any = ri.sending_exception();

         if ( any.type().equals( org.omg.CORBA.UNKNOWNHelper.type() )
              && 0 != ( s_throwExcept & SEND_EXPT ) )
         {
            UNKNOWN uex = org.omg.CORBA.UNKNOWNHelper.extract(
               ri.sending_exception() );

            uex.minor = uex.minor | SEND_EXPT;

            uex.completed = CompletionStatus.COMPLETED_YES;
            throw uex;
         }

         try
         {
            ri.result();
         }
         catch ( NO_RESOURCES ex )
         {
            // test retrieving request results. These will always fail
         }
         catch (org.omg.CORBA.BAD_INV_ORDER bio)
         {
            // result is not available at send_exception point - the spec
            // says throw BAD_INV_ORDER
         }
      }

      public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
         throws org.omg.PortableInterceptor.ForwardRequest
      {
         s_visitMask = s_visitMask | SEND_OTHR;

         if ( 0 != ( s_throwExcept & SEND_OTHR ) )
         {
            throw new UNKNOWN( SEND_OTHR,
                               CompletionStatus.COMPLETED_YES );
         }
      }
   }

   /**
    * The entry point of the test case.
    *
    * @param args The command line arguments.
    */
   public static void main( String args[] )
   {
      junit.textui.TestRunner.run( new TestSuite( PITest.class ) );
   }
}
