package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2003 Gerald Brose
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

import org.apache.avalon.framework.logger.Logger;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This initializes the SAS Client Security Service (CSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class GSSUPProviderInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    /** the logger used by the naming service implementation */
    private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

    /**
     * This method registers the interceptors.
     */
    public void pre_init( ORBInitInfo info )
    {
        // save ORB
        org.jacorb.security.sas.GSSUPProvider.orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();

        // save Codec
        try
        {
            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
            org.jacorb.security.sas.GSSUPProvider.codec = info.codec_factory().create_codec(encoding);
        }
        catch (UnknownEncoding unknownEncoding)
        {
            logger.error("UnknownEncoding", unknownEncoding);
        }
    }

    public void post_init(ORBInitInfo info)
    {
    }
}
