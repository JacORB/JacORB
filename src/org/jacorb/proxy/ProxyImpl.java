package org.jacorb.proxy;

import java.util.Hashtable;
import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import org.jacorb.orb.connection.*;
import org.jacorb.util.*;
import org.jacorb.orb.*;

class ProxyImpl extends ProxyPOA
{
    private static final String[] IDS = {"IDL:org/jacorb/proxy/Proxy:1.0"};

    private Hashtable forwardMap = new Hashtable ();
    private Hashtable iorMap = new Hashtable ();
    private Hashtable iorRefCnt = new Hashtable ();
    private org.jacorb.orb.ORB orb;

    /**
     * Server main line
     */

    public static void main (String[] args)
    {
        if (args.length != 2)
        {
            System.err.println ("usage: appligator <port> <IOR-File>");
            System.exit (1);
        }

        java.util.Properties props = new java.util.Properties ();

        props.put ("OAPort", args[0] );
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass." +
            "org.jacorb.proxy.ProxyServerInitializer", "");
        props.put ("jacorb.implname", "Appligator");

        ORB orb = org.omg.CORBA.ORB.init (args, props);

        ProxyImpl proxyimpl = new ProxyImpl (orb, args[1]);

        orb.run ();
    }

    public ProxyImpl (ORB orb, String filelocation)
    {
        NamingContextExt nc = null;
        org.omg.CORBA.Object forwarderRef = null;
        org.omg.CORBA.Object obj;

        this.orb = (org.jacorb.orb.ORB) orb;

        try
        {
            POA rootPOA;
            POA forwarderPOA;
            POAManager poaMgr;
            Servant forwarder;
            org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[3];

            obj = orb.resolve_initial_references ("RootPOA");
            rootPOA = POAHelper.narrow (obj);
            poaMgr = rootPOA.the_POAManager ();

            policies[0] = rootPOA.create_request_processing_policy
                (RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);
            policies[1] = rootPOA.create_id_uniqueness_policy
                (IdUniquenessPolicyValue.MULTIPLE_ID);
            policies[2] = rootPOA.create_servant_retention_policy
                (ServantRetentionPolicyValue.NON_RETAIN);

            forwarderPOA = rootPOA.create_POA ("FORWARDER_POA", poaMgr, policies);

            for (int i =0; i < policies.length; i++)
            {
                policies[i].destroy ();
            }

            forwarder = new ProxyEntry (orb);
            forwarderRef = rootPOA.servant_to_reference (forwarder);

            forwarderPOA.set_servant (new ProxyEntry (orb));
            poaMgr.activate ();
        }
        catch (Exception ex)
        {
            Debug.output (1, "Unexpected exception while initializing Proxy: " + ex);
            System.exit (1);
        }

        try
        {
            java.io.FileWriter fout = new java.io.FileWriter (filelocation);
            fout.write (orb.object_to_string (forwarderRef));
            fout.close ();
        }
        catch (java.io.IOException ioe)
        {
            Debug.output (1, "Could not write IOR File: " + filelocation);
        }

        try
        {
            obj = orb.resolve_initial_references ("NameService");
            nc = NamingContextExtHelper.narrow (obj);
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName ex)
        {
            Debug.output (2, ex);
        }

        if (nc == null)
        {
            Debug.output (1, "Name service not present. Trying without");
        }
        else
        {
            try
            {
               nc.rebind (nc.to_name ("proxyserver"), forwarderRef);
            }
            catch (org.omg.CORBA.UserException ex)
            {
               // Should not happen
            }
            catch (org.omg.CORBA.SystemException ex)
            {
               // Server not actually running or otherwise unavailable

               Debug.output (1, "Failed to register with name server");
            }
        }
    }

    private class ProxyEntry
        extends org.omg.PortableServer.DynamicImplementation
    {
        private org.jacorb.orb.ORB orb;

        public ProxyEntry (org.omg.CORBA.ORB orb)
        {
            this.orb = (org.jacorb.orb.ORB) orb;
        }

        public String[] _all_interfaces
            (org.omg.PortableServer.POA poa, byte[] objectId)
        {
            return IDS;
        }

        public void Xswap4 (byte[] by,int a, int b, int c, int d, int off)
        {
            byte swap;

            swap = by[a+off];
            by[a+off] = by[d+off];
            by[d+off] = swap;

            swap = by[b+off];
            by[b+off] = by[c+off];
            by[c+off] = swap;
        }

        public int doAlign (int index, int boundary)
        {
            if( index%boundary != 0 )
            {
                index = index + boundary - (index%boundary);
            }
            return index;
        }

        public void changeByteOrder (byte[] buffer)
        {
            boolean little = ((buffer[6]&1) == 0);
            boolean is_giop_1_1 = (buffer[5] == 1);

            int msgSize = 0;
            int serviceContextLength = 0;
            int object_keyLength = 0;
            int opname_Length = 0;
            int off = 0;

            //debug
            if (Environment.verbosityLevel() >= 3)
            {
                System.out.println( "[changeByteOrder] little=" + little +
                                    " is_giop_1_1=" + is_giop_1_1 );
            }

            off += 8;
            Xswap4 (buffer,0,1,2,3,off);     //request size
            if (little)
            {
                msgSize = (((buffer[3+off] & 0xff) << 24) +
                           ((buffer[2+off] & 0xff) << 16) +
                           ((buffer[1+off] & 0xff) << 8) +
                           ((buffer[0+off] & 0xff) << 0));
            }
            else
            {
                msgSize = (((buffer[0+off] & 0xff) << 24) +
                           ((buffer[1+off] & 0xff) << 16) +
                           ((buffer[2+off] & 0xff) << 8) +
                           ((buffer[3+off] & 0xff) << 0));
            }

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off +
                                    "]] msgSize=" + msgSize );
            }

            off += 4;
            Xswap4 (buffer,0,1,2,3,off);   //serviceContext Length

            if (little)
            {
                serviceContextLength = (((buffer[3+off] & 0xff) << 24) +
                                        ((buffer[2+off] & 0xff) << 16) +
                                        ((buffer[1+off] & 0xff) << 8) +
                                        ((buffer[0+off] & 0xff) << 0));
            }
            else
            {
                serviceContextLength = (((buffer[0+off] & 0xff) << 24) +
                                        ((buffer[1+off] & 0xff) << 16) +
                                        ((buffer[2+off] & 0xff) << 8) +
                                        ((buffer[3+off] & 0xff) << 0));
            }

            if (Environment.verbosityLevel() >= 3)
            {
                System.out.println( "[changeByteOrder[" + off +
                                    "]] serviceContextLength=" +
                                    serviceContextLength );
            }

            off += 4; // we are now at the RequestHeader
            for (int i = serviceContextLength; i > 0; i--)
            {
                Xswap4 (buffer,0,1,2,3,off); //context_id
                Xswap4 (buffer,4,5,6,7,off); //context_dataLength
                int context_dataLength;
                if (little)
                {
                    context_dataLength = (((buffer[7+off] & 0xff) << 24)+
                                          ((buffer[6+off] & 0xff) << 16)+
                                          ((buffer[5+off] & 0xff) << 8)+
                                          ((buffer[4+off] & 0xff) << 0));
                }
                else
                {
                    context_dataLength = (((buffer[4+off] & 0xff) << 24)+
                                          ((buffer[5+off] & 0xff) << 16)+
                                          ((buffer[6+off] & 0xff) << 8) +
                                          ((buffer[7+off] & 0xff) << 0));
                }

                if (Environment.verbosityLevel() >= 3)
                {
                    System.out.println( "[changeByteOrder[" + off +
                                        "]] context_dataLength=" +
                                        context_dataLength );
                }

                off = off + 8 + context_dataLength;
                // align to next long
                off = doAlign (off,4);
            }

            // we are now at request_id
            Xswap4 (buffer,0,1,2,3,off); //request_ID
            off += 4;
            off += 1; // skip response_expected
            if (is_giop_1_1)
            {
                off += 3;
            }
            // align
            off = doAlign (off,4);
            Xswap4 (buffer,0,1,2,3,off); //object_keyLength
            if (little)
            {
                object_keyLength = (((buffer[3+off] & 0xff) << 24) +
                                    ((buffer[2+off] & 0xff) << 16) +
                                    ((buffer[1+off] & 0xff) << 8) +
                                    ((buffer[0+off] & 0xff) << 0));
            }
            else
            {
                object_keyLength = (((buffer[0+off] & 0xff) << 24) +
                                    ((buffer[1+off] & 0xff) << 16) +
                                    ((buffer[2+off] & 0xff) << 8) +
                                    ((buffer[3+off] & 0xff) << 0));
            }

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off +
                                    "]] object_keyLength=" +
                                    object_keyLength );
            }

            off+=4 + object_keyLength;
            // we are now at String Operation

            // align
            off = doAlign (off,4);

            Xswap4 (buffer,0,1,2,3,off); //OpnameLength;

            if (little)
            {
                opname_Length = (((buffer[3+off] & 0xff) << 24) +
                                 ((buffer[2+off] & 0xff) << 16) +
                                 ((buffer[1+off] & 0xff) << 8) +
                                 ((buffer[0+off] & 0xff) << 0));
            }
            else
            {
                opname_Length = (((buffer[0+off] & 0xff) << 24) +
                                 ((buffer[1+off] & 0xff) << 16) +
                                 ((buffer[2+off] & 0xff) << 8) +
                                 ((buffer[3+off] & 0xff) << 0));
            }

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off +
                                    "]] opname_Length=" + opname_Length );
            }

            buffer[6] = (little) ? (byte)1 : (byte)0; //toggle the endian byte
        }

        public void invoke (org.omg.CORBA.ServerRequest req)
        {
            org.jacorb.orb.dsi.ServerRequest request = null;
            RequestInputStream reqis = null;
            RequestOutputStream reqos = null;
            ReplyInputStream repis = null;
            ReplyOutputStream repos = null;
            String ior_str = null;
            org.omg.CORBA.Object target = null;
            org.omg.PortableInterceptor.Current pi_current = null;
            Delegate delegate = null;

            request = (org.jacorb.orb.dsi.ServerRequest) req;
            reqis = request.get_in ();

            Debug.output (1, "Proxy DSI call to " + request.operation ());

            try
            {
                pi_current = (org.omg.PortableInterceptor.Current)
                    orb.resolve_initial_references ("PICurrent");

                ior_str = new String (pi_current.get_slot (ProxyServerInitializer.slot_id).toString ());
                target = orb.string_to_object (ior_str);
            }
            catch (org.omg.CORBA.UserException e)
            {
                e.printStackTrace();
            }


            //TODO: Reuse delegates

            // create and bind delegate
            // use special delegate without exception checking

            delegate = new Delegate (orb, ior_str, true);
            reqos = (RequestOutputStream) delegate.request
            (
                target,
                request.operation (),
                (0x03 & reqis.req_hdr.response_flags) == 0x03
            );

            //manipulate buffer

            synchronized (delegate)
            {
                byte[] outbuffer = reqos.getInternalBuffer ();

                int msg_size = reqis.msg_size + 12;

                //compute length of incoming data

                int datalength = (msg_size - (int) reqis.get_pos ());

                //getting a buffer of the right size

                byte[] new_array = BufferManager.getInstance().getBuffer
                    (datalength + reqos.size ());

                //copying the old header to the new array
                System.arraycopy
                (
                    outbuffer,
                    0,
                    new_array,
                    0,
                    reqos.size ()
                );

                //replacing the local pointer
                outbuffer = new_array;

                //replace buffer
                //this overwrites the size/pos of the stream

                reqos.setBufferWithoutReset (outbuffer);

                if (datalength > 0)
                {
                    System.arraycopy
                    (
                        reqis.getBuffer (),
                        reqis.get_pos (),
                        outbuffer,
                        reqos.size (),
                        datalength
                    );
                }
                reqos.setSize (reqos.size () + datalength);
                reqos.insertMsgSize (reqos.size () + datalength);

                // Check GIOP header flags endian bit

                if ((reqis.getBuffer()[6]&1) != (outbuffer[6]&1))
                {
                    changeByteOrder (outbuffer);
                }

                Debug.output (1, "SubRequest to send", outbuffer);

                //send it

                try
                {
                    repis = (ReplyInputStream) delegate.invoke (target, reqos);
                }
                catch (org.omg.CORBA.portable.ApplicationException ae)
                {
                    ae.printStackTrace ();
                }
                catch (org.omg.CORBA.portable.RemarshalException re)
                {
                    re.printStackTrace ();
                }

                ////////////////////////
                //construct reply
                ///////////////////////
                        
                repos = request.get_out ();
                byte[] reply_outbuffer = repos.getInternalBuffer ();

                int reply_msg_size = repis.msg_size + 12;
                        
                //compute length of incoming data

                int reply_datalength = reply_msg_size - (int) repis.get_pos();

                //getting a buffer of the right size

                byte[] reply_new_array = BufferManager.getInstance().getBuffer
                  (reply_datalength + repos.size ());

                //copying the old header to the new array

                System.arraycopy
                (
                    reply_outbuffer,
                    0,
                    reply_new_array,
                    0,
                    repos.size ()
                );

                //replacing the local pointer

                reply_outbuffer = reply_new_array;

                //replace buffer
                //this overwrites the size/pos of the stream

                repos.setBufferWithoutReset (reply_outbuffer);

                if (reply_datalength > 0)
                {
                    System.arraycopy
                    (
                        repis.getBuffer (),
                        repis.get_pos (),
                        reply_outbuffer,
                        repos.size (),
                        reply_datalength
                    );
                }

                repos.setSize (repos.size () + reply_datalength);
                repos.insertMsgSize (repos.size () + reply_datalength);

                request.setUsePreconstructedReply (true);
            }
        }
    }
}
