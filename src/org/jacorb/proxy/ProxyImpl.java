package org.jacorb.proxy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.*;
import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import org.jacorb.orb.giop.*;
import org.jacorb.util.*;
import org.jacorb.orb.*;

/**
 * This is the appligator implementation that recieves redirected calls
 * via DSI and calls on the the original target via DII.
 *
 * @author Nicolas Noffke, Sebastian Müller, Steve Osselton
 */

class ProxyImpl extends ProxyPOA
{
    private static final String[] IDS = {"IDL:org/jacorb/proxy/Proxy:1.0"};
    private static final String DEFAULT_ID = "Appligator";

    private static final int HDR_SIZE = 12;

    private Hashtable forwardMap = new Hashtable ();
    private Hashtable iorMap = new Hashtable ();
    private Hashtable iorRefCnt = new Hashtable ();
    private org.jacorb.orb.ORB orb;

    /**
     * Server main line
     */

    public static void main (String[] args)
    {
        boolean dynamic = false;

        if (args.length < 2 || args.length > 3)
        {
            usage ();
        }

        if (args.length == 3)
        {
            if (! args[2].equals ("-dynamic"))
            {
                usage ();
            }
            dynamic = true;
        }

        java.util.Properties props = new java.util.Properties ();

        props.put ("OAPort", args[0]);
        props.put ("org.omg.PortableInterceptor.ORBInitializerClass."
            + "org.jacorb.proxy.ProxyServerInitializer", "");
        props.put ("jacorb.implname", "Appligator");

        ORB orb = org.omg.CORBA.ORB.init (args, props);

        ProxyImpl proxyimpl = new ProxyImpl (orb, args[1], dynamic);

        orb.run ();
    }

    private static void usage ()
    {
        System.err.println ("usage: appligator <port> <IOR-File> [-dynamic]");
        System.exit (1);
    }

    public ProxyImpl (ORB orb, String file, boolean dynamic)
    {
        org.omg.CORBA.Object forwarderRef = null;
        org.omg.CORBA.Object obj;
        String idString;
        String name;
        byte[] id;

        this.orb = (org.jacorb.orb.ORB) orb;

        // Use default id if not configured

        idString = Environment.getProperty ("jacorb.ProxyServer.ID", DEFAULT_ID);
        id = idString.getBytes ();

        try
        {
            POA rootPOA;
            POA forwarderPOA;
            POAManager poaMgr;
            Servant forwarder;
            org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[4];

            obj = orb.resolve_initial_references ("RootPOA");
            rootPOA = POAHelper.narrow (obj);
            poaMgr = rootPOA.the_POAManager ();

            policies[0] = rootPOA.create_request_processing_policy
                (RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);
            policies[1] = rootPOA.create_id_uniqueness_policy
                (IdUniquenessPolicyValue.MULTIPLE_ID);
            policies[2] = rootPOA.create_servant_retention_policy
                (ServantRetentionPolicyValue.NON_RETAIN);

            if (dynamic)
            {
                policies[3] = rootPOA.create_id_assignment_policy
                    (IdAssignmentPolicyValue.SYSTEM_ID);
            }
            else
            {
                policies[3] = rootPOA.create_id_assignment_policy
                    (IdAssignmentPolicyValue.USER_ID);
            }

            forwarderPOA = rootPOA.create_POA ("FORWARDER_POA", poaMgr, policies);

            for (int i =0; i < policies.length; i++)
            {
                policies[i].destroy ();
            }

            forwarder = new ProxyEntry (orb);
            forwarderPOA.set_servant (forwarder);

            // Get reference to proxy either using fixed or system assigned id

            if (dynamic)
            {
                forwarderRef = rootPOA.servant_to_reference (forwarder);
            }
            else
            {
                forwarderRef = forwarderPOA.create_reference_with_id (id, IDS[0]);
            }

            poaMgr.activate ();
        }
        catch (Exception ex)
        {
            Debug.output (1, "Unexpected exception while initializing Proxy: " + ex);
            System.exit (1);
        }

        try
        {
            java.io.FileWriter fout = new java.io.FileWriter (file);
            fout.write (orb.object_to_string (forwarderRef));
            fout.close ();
        }
        catch (java.io.IOException ioe)
        {
            Debug.output (1, "Could not write IOR File: " + file);
        }

        // See if configured to register in name service

        name = Environment.getProperty ("jacorb.ProxyServer.Name", "");
        if (name.length () > 0)
        {
            NamingContextExt nc = null;
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
                    nc.rebind (nc.to_name (name), forwarderRef);
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
            org.omg.CORBA.Any any;
            RequestInputStream reqis = null;
            RequestOutputStream reqos = null;
            ReplyInputStream repis = null;
            ReplyOutputStream repos = null;
            String ior_str = null;
            org.omg.CORBA.Object target = null;
            org.omg.PortableInterceptor.Current pi_current = null;
            Delegate delegate = null;
            byte[] newbuff;
            byte[] outbuff;
            int newlen;
            int datalen;
            int status;

            request = (org.jacorb.orb.dsi.ServerRequest) req;
            reqis = request.get_in ();

            Debug.output (1, "Proxy DSI call to " + request.operation ());

            try
            {
                pi_current = (org.omg.PortableInterceptor.Current)
                    orb.resolve_initial_references ("PICurrent");

                any = pi_current.get_slot (ProxyServerForwardInterceptor.slot);
                ior_str = any.extract_string ();
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
                // Get buffer copy. Cannot get buffer direct as may
                // be delayed writes for byte arrays.

                outbuff = reqos.getBufferCopy ();

                // Compute length of incoming data

                datalen = HDR_SIZE + reqis.msg_size - (int) reqis.get_pos ();

                // Compute total required buffer size

                newlen = datalen + outbuff.length;

                // Get new buffer of the right size

                newbuff = BufferManager.getInstance().getBuffer (newlen);

                // Copying the old header to the new buffer

                System.arraycopy
                (
                    outbuff,
                    0,
                    newbuff,
                    0,
                    outbuff.length
                );

                // Append data to new buffer

                if (datalen > 0)
                {
                    System.arraycopy
                    (
                        reqis.getBuffer (),
                        reqis.get_pos (),
                        newbuff,
                        outbuff.length,
                        datalen
                    );
                }

                // Replace buffer

                reqos.setBufferWithoutReset (newbuff, newlen);
                reqos.insertMsgSize (newlen);

                // Check GIOP header flags endian bit

                if ((reqis.getBuffer()[6]&1) != (outbuff[6]&1))
                {
                    changeByteOrder (outbuff);
                }

                // Call on with DII

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

                /////////////////////
                // construct reply //
                /////////////////////

                // Get reply output stream
                        
                repos = request.get_out ();

                // Patch in reply status if not normal return

                status = repis.rep_hdr.reply_status.value ();
                if (status != org.omg.GIOP.ReplyStatusType_1_2._NO_EXCEPTION)
                {
                    // Reply status is last CORBA long in reply header for
                    // GIOP 1.0 and 1.1. This won't work for GIOP 1.2

                    repos.reduceSize (4);
                    repos.write_long (status);
                }

                // Get copy of buffer from stream

                outbuff = repos.getBufferCopy ();

                // Compute length of returned data

                datalen = HDR_SIZE + repis.msg_size - (int) repis.get_pos ();

                // Compute total required buffer size

                newlen = datalen + outbuff.length;

                // Get new buffer of the right size

                newbuff = BufferManager.getInstance().getBuffer (newlen);

                // Copy old header to the new buffer

                System.arraycopy
                (
                    outbuff,
                    0,
                    newbuff,
                    0,
                    outbuff.length
                );

                // Append return data to new buffer

                if (datalen > 0)
                {
                    System.arraycopy
                    (
                        repis.getBuffer (),
                        repis.get_pos (),
                        newbuff,
                        outbuff.length,
                        datalen
                    );
                }

                // Replace buffer

                repos.setBufferWithoutReset (newbuff, newlen);
                repos.insertMsgSize (newlen);

                request.setUsePreconstructedReply (true);
            }
        }
    }
}
