package org.jacorb.proxy;

import java.util.Hashtable;
import org.omg.PortableServer.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

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

	public void Xswap4(byte[] by,int a, int b, int c, int d, int off){
	    byte swap;
	    swap=by[a+off];by[a+off]=by[d+off];by[d+off]=swap;
	    swap=by[b+off];by[c+off]=by[b+off];by[b+off]=swap;
	}
			
	public void changeByteOrder(byte[] buffer)
	{

	    boolean little=((buffer[6]&1)==0);
	    boolean is_giop_1_1 = (buffer[5]==1);

	    int serviceContextLength=0;
	    int object_keyLength=0;
	    int off=0;
	
	    Xswap4(buffer,8,9,10,11,0);       //request size
	    Xswap4(buffer,12,13,14,15,0);   //serviceContext Length       	

	    if (little)
		serviceContextLength=  (((buffer[15] & 0xff) << 24) +
					((buffer[14] & 0xff) << 16) +
					((buffer[13] & 0xff) << 8) +
					((buffer[12] & 0xff) << 0));
	    else
		serviceContextLength= (((buffer[12] & 0xff) << 24) +
				       ((buffer[13] & 0xff) << 16) +
				       ((buffer[14] & 0xff) << 8) +
				       ((buffer[15] & 0xff) << 0));

	    for(int i=serviceContextLength;i>0;i--){
		Xswap4(buffer,16,17,18,19,off); //context_id
		Xswap4(buffer,20,21,22,23,off); //context_dataLength
		int context_dataLength;

		if (little)
		    context_dataLength=  (((buffer[23+off] & 0xff) << 24) +
					  ((buffer[22+off] & 0xff) << 16) +
					  ((buffer[21+off] & 0xff) << 8) +
					  ((buffer[20+off] & 0xff) << 0));
		else
		    context_dataLength= (((buffer[20+off] & 0xff) << 24) +
					 ((buffer[21+off] & 0xff) << 16) +
					 ((buffer[22+off] & 0xff) << 8) +
					 ((buffer[23+off] & 0xff) << 0));

		off=off+8+context_dataLength;
	    }
	
	    Xswap4(buffer,16,17,18,19,off); //request_ID

	    if( is_giop_1_1 )
		off += 3;
	    Xswap4(buffer,21,22,23,24,off); //object_keyLength

	    if (little)
		object_keyLength=  (((buffer[24+off] & 0xff) << 24) +
				    ((buffer[23+off] & 0xff) << 16) +
				    ((buffer[22+off] & 0xff) << 8) +
				    ((buffer[21+off] & 0xff) << 0));
	    else
		object_keyLength= (((buffer[21+off] & 0xff) << 24) +
				   ((buffer[22+off] & 0xff) << 16) +
				   ((buffer[23+off] & 0xff) << 8) +
				   ((buffer[24+off] & 0xff) << 0));

	    off+=object_keyLength;
	    Xswap4(buffer,25,26,27,28,off); //OpnameLength;
	    buffer[6]= (byte)~(buffer[6] & 0xfe); //toggle the endian byte
	}



	public void invoke(org.omg.CORBA.ServerRequest r)
	{
	    int mycounter=counter++;
	    org.jacorb.util.Debug.output(1,"[DynProxy]invoked:"+mycounter);	    
	    //get oid
	    byte[] oid=null;
	    Integer key = null;

	    org.jacorb.orb.dsi.ServerRequest inrequest = (org.jacorb.orb.dsi.ServerRequest)r;         
	    oid=inrequest.objectId();
	    org.omg.PortableServer.Current poa_current=null;
	    try
	    {
		poa_current=org.omg.PortableServer.CurrentHelper.narrow(orb.resolve_initial_references("POACurrent"));
	    }
	    catch(org.omg.CORBA.UserException ue)
	    {
		ue.printStackTrace();
	    }
		   
	    MiniStub mStub=(MiniStub)forwardMap.get(new String(oid));
	    org.jacorb.orb.ParsedIOR ior=mStub.getParsedIOR();
	    org.jacorb.util.Debug.output(4,"[Call should go to IOR: "+ior+" ]");

	    org.jacorb.orb.connection.ReplyInputStream rep=null;

	    org.jacorb.orb.connection.ClientConnection realCon = 
                mStub.getConnection();

	    if (!realCon.connected())
	    { //Connection was closed
	    	realCon.reconnect();
	    }
	    org.jacorb.orb.connection.RequestOutputStream cdr=null;
		
	    try
	    {	
		//create new Message	
		//Msgheader
		cdr=new org.jacorb.orb.connection.RequestOutputStream(
							    realCon,
							    orb,
							    inrequest.operation(),
							    inrequest.get_in().req_hdr.response_expected,
							    ior.get_object_key(),
							    inrequest.getServiceContext());
		//data
		synchronized(realCon.writeLock)
		{
		    byte outbuffer[] = cdr.getInternalBuffer();
		    System.out.println("["+mycounter+"]Incoming Request with size: "+(inrequest.get_in().msg_hdr.message_size+12));
		    int datalength= ((int)inrequest.get_in().msg_hdr.message_size+12)-((int)inrequest.get_in().get_pos());
		    // inrequest.get_in().get_buffer().length
		    if (datalength>0){
				//copy the data
			System.arraycopy(inrequest.get_in().getBuffer(),
					 inrequest.get_in().get_pos(),
					 outbuffer,
					 //inrequest.get_in().get_pos(),
					 cdr.size(),
					 datalength);
		    }
		    cdr.setSize(cdr.size()+datalength);
		    cdr.insertMsgSize();
		    if ((inrequest.get_in().getBuffer()[6]&1)!=(outbuffer[6]&1))
			changeByteOrder(outbuffer);
		    //send it	
		    //debug
		    if (org.jacorb.util.Environment.verbosityLevel()>=3)
		    {
			jacorb.util.Debug.output(3,"[Proxy:Incoming byte-stream:]");
			for(int i=0;i<inrequest.get_in().msg_hdr.message_size+12;i++)
			    System.out.print(((byte)inrequest.get_in().getBuffer()[i])+"  ");
			jacorb.util.Debug.output(3,"[Proxy:Outgoing byte-stream:]");
			for(int i=0;i<cdr.size();i++)
			{
			    System.out.print(((byte)cdr.getInternalBuffer()[i])+"  ");
			}
			System.out.println("[The real Data:]");
			for(int i=inrequest.get_in().get_pos();i<inrequest.get_in().msg_hdr.message_size+12;i++)
			{
			    System.out.print(inrequest.get_in().getBuffer()[i]+"  ");
			}
		    }		    
		    if (cdr.response_expected())
		    {
			rep=new org.jacorb.orb.connection.ReplyInputStream(realCon, cdr.requestId());
			key = new Integer( cdr.requestId() );
			//			((org.jacorb.orb.ClientSideConnection)realCon).get_buffers().put( key, cdr);
			((org.jacorb.orb.connection.ClientConnection)realCon).get_replies().put( key, rep );
			((org.jacorb.orb.connection.ClientConnection)realCon).get_objects().put( key, poa_current.get_POA().servant_to_reference(this) ); 
		    }
		    //if (! realCon.connected()) done above
		    //	realCon.reconnect();
		    //realCon.get_out_stream().write(cdr.getBuffer(),0,cdr.size());
		    //realCon.get_out_stream().flush();
		    System.out.println("["+mycounter+"]Outgoing Request with size: "+cdr.size());
		    ((org.jacorb.orb.connection.ClientConnection)realCon).writeDirectly(cdr.getInternalBuffer(),cdr.size());
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
		    org.jacorb.orb.CDRInputStream xxx=((org.jacorb.orb.connection.ReplyInputStream)rep.rawResult());
		    //		    ((org.jacorb.orb.connection.ClientConnection)realCon).get_buffers().remove(key);
		    ((org.jacorb.orb.connection.ClientConnection)realCon).get_replies().remove(key);
		    ((org.jacorb.orb.connection.ClientConnection)realCon).get_objects().remove(key);
		    inrequest.reply(xxx.getBuffer(),((org.jacorb.orb.connection.ReplyInputStream)xxx).msg_hdr.message_size+12);
		}
	    }
	    catch (Exception e)
	    {
		jacorb.util.Debug.output(1,"Proxy:reply forward error");
		inrequest.setSystemException(new org.omg.CORBA.COMM_FAILURE(e.toString()));
		inrequest.reply();
	    }
   	    org.jacorb.util.Debug.output(1,"[DynProxy]invoke DONE:"+mycounter);	    
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
	jacorb.util.Debug.output( 2,"Forwading for IOR: " + IOR );
	org.omg.CORBA.Object o = null;
	jacorb.orb.ParsedIOR pIOR = new org.jacorb.orb.ParsedIOR( IOR );
	String oid=null;
	try
	{
		    
	    o = (org.omg.CORBA.Object)iorMap.get(IOR);
	    if (o == null)
	    {
                //IOR not forwarded yet
		jacorb.util.Debug.output(3,"Creating new proxy object");
		forwarderPOA = rootPOA.find_POA("FORWARDER_POA",false);
		o = forwarderPOA.create_reference(pIOR.getIOR().type_id); //create new "CORBA Object"
		oid = new String( forwarderPOA.reference_to_id(o));
                
                org.jacorb.orb.Delegate delegate = (org.jacorb.orb.Delegate)
                    ((org.omg.CORBA.portable.ObjectImpl)o)._get_delegate();

		//jacorb.orb.connection.ClientConnection c =(org.jacorb.orb.connection.ClientConnection)
                //    ((org.jacorb.orb.ORB)orb).getConnectionManager()._getConnection(delegate);

                org.jacorb.orb.connection.ClientConnection c = 
                    (org.jacorb.orb.connection.ClientConnection) orb.getConnectionManager()._getConnection(pIOR.getAddress(), false);

		MiniStub mStub = new MiniStub(c, pIOR );
		forwardMap.put( oid,mStub );
		iorMap.put( IOR, o );
		iorRefCnt.put( IOR,new Integer(1) );
	    }	     
	    else
	    {
		jacorb.util.Debug.output(3,"Proxyobject taken from cache");
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

    public static void  main(String[] args)
    {	
	if (args.length != 2)
	{
	    org.jacorb.util.Debug.output(0,"usage: appligator <port> <IOR-File>");
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

	    NamingContextExt nc =
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    if(nc==null)
	    {
		jacorb.util.Debug.output(1,"Nameserver not present. Trying without");
	    }
	    else
	    {
		nc.bind(nc.to_name("proxyserver"),forwarderRef);
	    }
	    orb.run();
	}
	catch(org.omg.CORBA.UserException ue)
	{
	    ue.printStackTrace();
	}	
	catch(java.io.IOException ioe)
	{
	    org.jacorb.util.Debug.output(1,"Could not write IOR File:"+ioe.toString());
	}
    }

    public synchronized void release(/*in*/String uid)
    {
	jacorb.util.Debug.output(3,"Release starts...");
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
		jacorb.orb.connection.ClientConnection c = mStub.getConnection();
		c.releaseConnection();
		iorRefCnt.remove(IOR);
	    }
	    else
	    { 
                refcount--;
                iorRefCnt.put(IOR,new Integer(refcount));
            }

	}
	catch(NullPointerException npe){} //someone released this ressource allready		
	jacorb.util.Debug.output(3,"Release ends");

    }
}
