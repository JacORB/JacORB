/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.transaction;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.*;
import org.omg.IOP.*;
/**
 * This class registers the ClientContextTransferInterceptor 
 * and the ServerContextTransferInterceptor with the ORB.
 *
 * @author Vladimir Mencl
 * @version $Id$
 */

public class TransactionInitializer 
  extends org.omg.CORBA.LocalObject
  implements ORBInitializer{
  public static int slot_id;

  public TransactionInitializer() {
  }

  // implementation of org.omg.PortableInterceptor.ORBInitializerOperations interface
  /**
   * This method allocates a slot at the PICurrent, creates a codec and sets
   * up the TransactionCurrent and the interceptor.
   */
  public void post_init(ORBInitInfo info) {
    try{
      ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
      slot_id = info.allocate_slot_id();
    
      Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, 
				       (byte) 1, (byte) 0);
      Codec codec = info.codec_factory().create_codec(encoding);

      TransactionCurrentImpl ts_current = new TransactionCurrentImpl(orb, slot_id);
      info.register_initial_reference("TransactionCurrent", ts_current);

      info.add_client_request_interceptor(
	  new ClientContextTransferInterceptor(slot_id, codec));

      info.add_server_request_interceptor(
	  new ServerContextTransferInterceptor(codec, slot_id, ts_current, 
	  orb));

    } catch (Exception e){
      org.jacorb.util.Debug.output(2, e);
    }
  }

  public void pre_init(ORBInitInfo info) {    
  }

} // TransactionInitializer

