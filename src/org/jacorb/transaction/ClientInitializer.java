package org.jacorb.transaction;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.*;
import org.omg.IOP_N.*;
/**
 * This class registers the ClientContextTransferInterceptor 
 * with the ORB.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ClientInitializer 
  extends org.jacorb.orb.LocalityConstrainedObject 
  implements ORBInitializer{

  public ClientInitializer() {
  }

  // implementation of org.omg.PortableInterceptor.ORBInitializerOperations interface
  /**
   * This method allocates a slot at the PICurrent, creates a codec and sets
   * up the TransactionCurrent and the interceptor.
   */
  public void post_init(ORBInitInfo info) {
    try{
      ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
      int slot_id = info.allocate_slot_id();
    
      Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, 
				       (byte) 1, (byte) 0);
      Codec codec = info.codec_factory().create_codec(encoding);

      TransactionCurrentImpl ts_current = new TransactionCurrentImpl(orb, slot_id);
      info.register_initial_reference("TransactionCurrent", ts_current);

      info.add_client_request_interceptor
	(new ClientContextTransferInterceptor(slot_id, codec));
    }catch (Exception e){
      org.jacorb.util.Debug.output(2, e);
    }
  }

  public void pre_init(ORBInitInfo info) {    
  }
} // ClientInitializer
