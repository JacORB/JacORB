package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2002 Gerald Brose
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

import java.io.*;
import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;
import org.omg.SecurityReplaceable.*;
import org.omg.Security.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.jacorb.util.*;
import org.ietf.jgss.*;

import org.jacorb.util.Environment;

/**
 * This initializes the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class TSSInitializer
        extends org.omg.CORBA.LocalObject
        implements ORBInitializer
{
    public static GSSManager gssManager = GSSManager.getInstance();
    public static int sourceNameSlotID = -1;
    public static int authTokensSlotID = -1;
    public static int sasReplySlotID = -1;

    /**
    * This method registers the interceptors.
    */
    public void post_init( ORBInitInfo info )
    {

        // load any GSS mechanism providors
        for (int i = 1; i <= 16; i++) {
            String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".oid");
            String mechProvider = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".provider");
            if (mechOID == null || mechProvider == null) continue;
            try {
                Oid oid = new org.ietf.jgss.Oid(mechOID);
                Class cls = Class.forName (mechProvider);
                java.lang.Object provider = cls.newInstance ();
                gssManager.addProviderAtFront((java.security.Provider)provider, oid);
                Debug.output(1, "Adding GSS SPI Provider: " + oid + " " + mechProvider);
            } catch (Exception e) {
                Debug.output( 1, "GSSProvider "+mechOID+" "+mechProvider + " error: " +e );
            }
        }

        // install the TSS interceptor
        try
        {
            sourceNameSlotID = info.allocate_slot_id();
            authTokensSlotID = info.allocate_slot_id();
            sasReplySlotID = info.allocate_slot_id();
            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
            Codec codec = info.codec_factory().create_codec(encoding);
            org.jacorb.orb.ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();
            info.add_server_request_interceptor(new TSSInvocationInterceptor(orb, codec, sourceNameSlotID, sasReplySlotID));
        }
        catch (DuplicateName duplicateName)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, duplicateName);
        }
        catch (UnknownEncoding unknownEncoding)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, unknownEncoding);
        }
    }

    public void pre_init(ORBInitInfo info)
    {
    }
}    // SAS setup Initializer
