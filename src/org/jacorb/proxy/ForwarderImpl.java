package org.jacorb.proxy;

import java.util.Hashtable;
import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import org.jacorb.orb.connection.*;
import org.jacorb.util.*;


class ForwarderImpl 
    extends ForwarderPOA
{
    private static int counter=0;
        
    private class ProxyEntry 
        extends org.omg.PortableServer.DynamicImplementation
        implements org.jacorb.orb.Forwarder
    {
        private org.jacorb.orb.ORB orb;

        public ProxyEntry(org.omg.CORBA.ORB orb)
        {
            this.orb = (org.jacorb.orb.ORB)orb;
        }

        public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId)
        {
            String[] ids={"IDL:jacorb/proxy/Forwarder:1.0"};
            return ids;
        }

        public void Xswap4(byte[] by,int a, int b, int c, int d, int off)
        {
            byte swap;

            swap = by[ a+off ];
            by[a+off] = by[ d+off ];
            by[d+off] = swap;

            //this line is wrong (reported by Armin Schloesser) swap=by[b+off];by[c+off]=by[b+off];by[b+off]=swap;
            swap=by[b+off];by[b+off]=by[c+off];by[c+off]=swap;
        }

	public int doAlign( int index, int boundary ) 
        {
            if( index%boundary != 0 ) 
            {
                index = index + boundary - (index%boundary);
            }
            return index;
        }


                        
        public void changeByteOrder(byte[] buffer)
        {

            // bug fixed alignement was not taken into account
            // 13.4.2000 A.Schloesser (Philips Automation Projects,Kassel)
            // here we have to change the MessageHeader
            //                       and  RequestHeader
            //              length info to the wanted byte Order
            /*
              struct MessageHeader {       offset in buffer
              char magic[4]              0
              struct Version {
              octet major             4
              octet minorA            5
              }
              boolean byte_order         6
              octet   message_type       7
              unsigned long message_size 8
              } end MessageHeader
              struct RequestHeader {
              sequence<ServiceContext> {
              long          length         4 byte align
              unsigned long context_id    
              sequence<octet> data {    
              long length               
              octet [] data
              }
              }
              unsigned long request_id       4 byte align
              boolean  response_expected
              sequence<octet> object_key {
              long length                  4 byte align
              octet[] object_key
              }
              string operation {
              long length                  4 byte align
              char [] operation (null terminated)
              }
              Principal requesting_principal
              } end RequestHeader
            */

            boolean little=((buffer[6]&1)==0);
            boolean is_giop_1_1 = (buffer[5]==1);

            int msgSize=0;
            int serviceContextLength=0;
            int object_keyLength=0;
            int opname_Length=0;
            int off=0;

            //debug
            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder] little=" + little + 
                                    " is_giop_1_1=" + is_giop_1_1 );
            }

            off += 8;
            Xswap4(buffer,0,1,2,3,off);     //request size
            if (little)
                msgSize=  (((buffer[3+off] & 0xff) << 24) +
                           ((buffer[2+off] & 0xff) << 16) +
			   ((buffer[1+off] & 0xff) << 8) +
			   ((buffer[0+off] & 0xff) << 0));
            else
                msgSize= (((buffer[0+off] & 0xff) << 24) +
                          ((buffer[1+off] & 0xff) << 16) +
			  ((buffer[2+off] & 0xff) << 8) +
			  ((buffer[3+off] & 0xff) << 0));

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off + 
                                    "]] msgSize=" + msgSize );
            }

            off += 4;
            Xswap4(buffer,0,1,2,3,off);   //serviceContext Length

            if (little)
                serviceContextLength=  (((buffer[3+off] & 0xff) << 24) +
                                        ((buffer[2+off] & 0xff) << 16) +
                                        ((buffer[1+off] & 0xff) << 8) +
                                        ((buffer[0+off] & 0xff) << 0));
            else
                serviceContextLength= (((buffer[0+off] & 0xff) << 24) +
                                       ((buffer[1+off] & 0xff) << 16) +
                                       ((buffer[2+off] & 0xff) << 8) +
                                       ((buffer[3+off] & 0xff) << 0));

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off + 
                                    "]] serviceContextLength=" + 
                                    serviceContextLength );
            }

            off += 4; // we are now at the RequestHeader
            for( int i = serviceContextLength; i > 0; i-- ) 
            {
                Xswap4(buffer,0,1,2,3,off); //context_id
                Xswap4(buffer,4,5,6,7,off); //context_dataLength
                int context_dataLength;
                if (little)
                    context_dataLength=  (((buffer[7+off] & 0xff) << 24)+
                                          ((buffer[6+off] & 0xff) << 16)+
                                          ((buffer[5+off] & 0xff) << 8)+
                                          ((buffer[4+off] & 0xff) << 0));
                else
                    context_dataLength= (((buffer[4+off] & 0xff) << 24)+
                                         ((buffer[5+off] & 0xff) << 16)+
                                         ((buffer[6+off] & 0xff) << 8) +
                                         ((buffer[7+off] & 0xff) << 0));

                if( Environment.verbosityLevel() >= 3 )
                {
                    System.out.println( "[changeByteOrder[" + off + 
                                        "]] context_dataLength=" + 
                                        context_dataLength );
                }

                off=off+8+context_dataLength;
                // align to next long
                off = doAlign(off,4);
            }

            // we are now at request_id
            Xswap4(buffer,0,1,2,3,off); //request_ID
            off += 4;
            off += 1; // skip response_expected
            if( is_giop_1_1 ) 
            {
                off += 3;
            }
            // align
            off = doAlign(off,4);
            Xswap4(buffer,0,1,2,3,off); //object_keyLength
            if (little)
                object_keyLength=  (((buffer[3+off] & 0xff) << 24) +
                                    ((buffer[2+off] & 0xff) << 16) +
                                    ((buffer[1+off] & 0xff) << 8) +
                                    ((buffer[0+off] & 0xff) << 0));
            else
                object_keyLength= (((buffer[0+off] & 0xff) << 24) +
                                   ((buffer[1+off] & 0xff) << 16) +
                                   ((buffer[2+off] & 0xff) << 8) +
                                   ((buffer[3+off] & 0xff) << 0));

            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off + 
                                    "]] object_keyLength=" + 
                                    object_keyLength );
            }

            off+=4 + object_keyLength;
            // we are now at String Operation

            // align
            off = doAlign(off,4);

            Xswap4(buffer,0,1,2,3,off); //OpnameLength;

            if (little)
                opname_Length=  (((buffer[3+off] & 0xff) << 24) +
                                 ((buffer[2+off] & 0xff) << 16) +
                                 ((buffer[1+off] & 0xff) << 8) +
                                 ((buffer[0+off] & 0xff) << 0));
            else
                opname_Length= (((buffer[0+off] & 0xff) << 24) +
                                ((buffer[1+off] & 0xff) << 16) +
                                ((buffer[2+off] & 0xff) << 8) +
                                ((buffer[3+off] & 0xff) << 0));
            if( Environment.verbosityLevel() >= 3 )
            {
                System.out.println( "[changeByteOrder[" + off + 
                                    "]] opname_Length=" + opname_Length );
            }

            buffer[6] = (little)?(byte)1:(byte)0; //toggle the endian byte

        }



        public void invoke(org.omg.CORBA.ServerRequest r)
        {
            int mycounter=counter++;
            Debug.output(1,"[DynProxy]invoked:"+mycounter);         
            //get oid
            byte[] oid=null;
            Integer key = null;

            org.jacorb.orb.dsi.ServerRequest inrequest = 
                (org.jacorb.orb.dsi.ServerRequest)r;         
            oid=inrequest.objectId();
            org.omg.PortableServer.Current poa_current=null;
            try
            {
                poa_current=
                    org.omg.PortableServer.CurrentHelper.narrow(orb.resolve_initial_references("POACurrent"));
            }
            catch( org.omg.CORBA.UserException ue )
            {
                ue.printStackTrace();
            }
                   
            MiniStub mStub=(MiniStub)forwardMap.get(new String(oid));
            org.jacorb.orb.ParsedIOR ior=mStub.getParsedIOR();
            Debug.output(4,"[Call should go to IOR: "+ior+" ]");

            ReplyInputStream rep=null;

            ClientConnection realCon = 
                mStub.getConnection();

            if (!realCon.connected())
            { //Connection was closed
                realCon.reconnect();
            }
            RequestOutputStream cdr=null;
                
            try
            {   
                //create new Message    
                //Msgheader
                cdr = 
                    new RequestOutputStream( orb,
                                             realCon.getId(),
                                             inrequest.operation(),
                                             inrequest.get_in().req_hdr.response_expected,
                                             ior.get_object_key(),
                                             inrequest.getServiceContext());
                //data
                synchronized(realCon.writeLock)
                {
                    byte[] outbuffer = cdr.getInternalBuffer();

                    System.out.println( "[" + mycounter + 
                                        "]Incoming Request with size: " + 
                                        (inrequest.get_in().msg_hdr.message_size+12 ));

                    int datalength= 
                        ((int) inrequest.get_in().msg_hdr.message_size + 12 )-
                        ((int) inrequest.get_in().get_pos() );
                    // inrequest.get_in().get_buffer().length

                    /*
                     * This is a fix for the following bug: in the previous 
                     * versions, outbuffer was taken, but no check was 
                     * perfomed, to see if the buffer was large enough. This
                     * led to an ArrayIndexOutOfBoundsException.
                     */
                    if( outbuffer.length < (datalength + cdr.size()) )
                    {
                        //getting a buffer of the right size
                        byte[] new_array = 
                            org.jacorb.orb.BufferManager.getInstance().getBuffer( 
                                                                   datalength + 
                                                                   cdr.size() );
                        
                        //copying the old header to the new array
                        System.arraycopy( outbuffer,
                                          0,
                                          new_array,
                                          0,
                                          cdr.size() );
                        
                        //replacing the local pointer
                        outbuffer = new_array;

                        //replacing the internal buffer of the reply.
                        //especially the bit bit with setting the size is
                        //definitely hacky

                        //remember size
                        int old_size = cdr.size();

                        //replace buffer
                        //this overwrites the size/pos of the stream
                        cdr.setBuffer( outbuffer ); 

                        //patch pos
                        cdr.setSize( old_size );
                    }

                    if( datalength > 0 )
                    {                        
                        //copy the data
                        System.arraycopy( inrequest.get_in().getBuffer(),
                                          inrequest.get_in().get_pos(),
                                          outbuffer,
                                          cdr.size(),
                                          datalength );
                    }

                    cdr.setSize(cdr.size()+datalength);
                    cdr.insertMsgSize();
                    if ((inrequest.get_in().getBuffer()[6]&1)!=(outbuffer[6]&1))
                        changeByteOrder(outbuffer);
                    //send it   
                    //debug
                    if (Environment.verbosityLevel()>=3)
                    {
                        Debug.output(3,"[Proxy:Incoming byte-stream:]");

                        for( int i = 0 ; i < inrequest.get_in().msg_hdr.message_size+12; i++ )
                            System.out.print(((byte)inrequest.get_in().getBuffer()[i])+"  ");

                        Debug.output(3, "[Proxy:Outgoing byte-stream:]");
                        for(int i=0; i < cdr.size(); i++)
                        {
                            System.out.print(((byte)cdr.getInternalBuffer()[i])+"  ");
                        }
                        System.out.println("[The real Data:]");
                        for( int i = inrequest.get_in().get_pos(); 
                             i < inrequest.get_in().msg_hdr.message_size + 12;
                             i++ )
                        {
                            System.out.print(inrequest.get_in().getBuffer()[i]+"  ");
                        }
                    }               
                    if (cdr.response_expected())
                    {
                        rep = new ReplyInputStream( orb, cdr.requestId());
                        key = new Integer( cdr.requestId() );
                        //                      ((org.jacorb.orb.ClientSideConnection)realCon).get_buffers().put( key, cdr);
                        ((ClientConnection)realCon).get_replies().put( key, rep );
                    }
                    //if (! realCon.connected()) done above
                    //  realCon.reconnect();
                    //realCon.get_out_stream().write(cdr.getBuffer(),0,cdr.size());
                    //realCon.get_out_stream().flush();
                    System.out.println("["+mycounter+"]Outgoing Request with size: "+cdr.size());
                    ((ClientConnection)realCon).writeDirectly(cdr.getInternalBuffer(),cdr.size());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                if (cdr.response_expected())
                {
                    org.jacorb.orb.CDRInputStream xxx=((ReplyInputStream)rep.rawResult());
                    //              ((ClientConnection)realCon).get_buffers().remove(key);
                    ((ClientConnection)realCon).get_replies().remove(key);
                    inrequest.reply(xxx.getBuffer(),((ReplyInputStream)xxx).msg_hdr.message_size+12);
                }
            }
            catch (Exception e)
            {
                Debug.output(1,"Proxy:reply forward error");
                inrequest.setSystemException(new org.omg.CORBA.COMM_FAILURE(e.toString()));
                inrequest.reply();
            }
            Debug.output(1,"[DynProxy]invoke DONE:"+mycounter);     
        }


                
    }           


    private Hashtable forwardMap=new Hashtable();
    private Hashtable iorMap=new Hashtable();
    private Hashtable iorRefCnt=new Hashtable();
    private org.jacorb.orb.ORB orb;
    private POA rootPOA;
    private POA forwarderPOA;


    public ForwarderImpl(ORB orb)
    {
        this.orb = (org.jacorb.orb.ORB)orb;
        try
        {
            rootPOA=POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POAManager poaMgr=rootPOA.the_POAManager();         

            org.omg.CORBA.Policy[] policies = 
                new org.omg.CORBA.Policy[3];
            policies[0]=
                rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);
            policies[1]=
                rootPOA.create_id_uniqueness_policy(IdUniquenessPolicyValue.MULTIPLE_ID);
            policies[2]=
                rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN);        

            forwarderPOA=rootPOA.create_POA("FORWARDER_POA",poaMgr,policies);
            for(int i =0;i<policies.length;i++) 
                policies[i].destroy();

            forwarderPOA.set_servant(new ProxyEntry(orb));
            poaMgr.activate();
        }
        catch(org.omg.CORBA.UserException ue)
        {
            ue.printStackTrace();
        }
    }



    public String forward( String IOR,
                           org.omg.CORBA.StringHolder uid)
    {           
        Debug.output( 2,"Forwading for IOR: " + IOR );
        org.omg.CORBA.Object o = null;
        org.jacorb.orb.ParsedIOR pIOR = new org.jacorb.orb.ParsedIOR( IOR );
        String oid=null;
        try
        {
                    
            o = (org.omg.CORBA.Object)iorMap.get(IOR);
            if (o == null)
            {
                //IOR not forwarded yet
                Debug.output(3,"Creating new proxy object");
                forwarderPOA = rootPOA.find_POA("FORWARDER_POA",false);
                o = forwarderPOA.create_reference(pIOR.getIOR().type_id); //create new "CORBA Object"
                oid = new String( forwarderPOA.reference_to_id(o));
                
                org.jacorb.orb.Delegate delegate = (org.jacorb.orb.Delegate)
                    ((org.omg.CORBA.portable.ObjectImpl)o)._get_delegate();

                //jacorb.orb.connection.ClientConnection c =(ClientConnection)
                //    ((org.jacorb.orb.ORB)orb).getConnectionManager()._getConnection(delegate);

                ClientConnection c = 
                    (ClientConnection) orb.getConnectionManager()._getConnection(pIOR.getAddress(), false);

                MiniStub mStub = new MiniStub(c, pIOR );
                forwardMap.put( oid,mStub );
                iorMap.put( IOR, o );
                iorRefCnt.put( IOR,new Integer(1) );
            }        
            else
            {
                Debug.output(3,"Proxyobject taken from cache");
                Integer I=(Integer)iorRefCnt.get(IOR);
                int i=I.intValue();
                iorRefCnt.put(IOR,new Integer(++i));
                oid=new String(forwarderPOA.reference_to_id(o));                
            }
        }
        catch(org.omg.CORBA.UserException ue)
        {
            ue.printStackTrace();
        }
        uid.value=oid;
        //new org.omg.CORBA.StringHolder(oid);
        return orb.object_to_string(o);
    }



    public synchronized void release(/*in*/String uid)
    {
        Debug.output(3,"Release starts...");
        try
        {
            MiniStub mStub = (MiniStub)forwardMap.get(uid);
            String IOR=mStub.getParsedIOR().getIORString();
            Integer I=(Integer)iorRefCnt.get(IOR);
            int refcount=I.intValue();
            if (refcount==1)
            {
                iorMap.remove(IOR);
                forwardMap.remove(uid);
                ClientConnection c = 
                    mStub.getConnection();
                orb.getConnectionManager().releaseConnection( c );
                iorRefCnt.remove(IOR);
            }
            else
            { 
                refcount--;
                iorRefCnt.put(IOR,new Integer(refcount));
            }

        }
        catch(NullPointerException npe){} //someone released this ressource allready            
        Debug.output(3,"Release ends");

    }


    /**
     * Server main line
     */

    public static void  main(String[] args)
    {   
        if (args.length != 2)
        {
            Debug.output(0,"usage: appligator <port> <IOR-File>");
            System.exit(1);
        }

        try
        {
            java.util.Properties props = new java.util.Properties();
            props.put("OAPort", args[0] );
            ORB orb = org.omg.CORBA.ORB.init(args, props);

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            Servant forwarder = new ForwarderPOATie(new ForwarderImpl(orb));
            org.omg.CORBA.Object forwarderRef = rootPOA.servant_to_reference(forwarder);

            java.io.FileWriter fout = new java.io.FileWriter(args[1]);
            fout.write(orb.object_to_string(forwarderRef));
            fout.close();

            NamingContextExt nc = null;
            try
            {
                nc = NamingContextExtHelper.narrow
                    (orb.resolve_initial_references("NameService"));
            }
            catch( org.omg.CORBA.BAD_PARAM bp)
            {
                Debug.output( 2, bp );
            }  

            if( nc == null )
            {
                Debug.output(1,"Nameserver not present. Trying without");
            }
            else
            {
                nc.bind( nc.to_name("proxyserver"),forwarderRef);
            }
            orb.run();
        }
        catch(org.omg.CORBA.UserException ue)
        {
            ue.printStackTrace();
        }       
        catch(java.io.IOException ioe)
        {
            Debug.output(1,"Could not write IOR File:"+ioe.toString());
        }
    }

}






