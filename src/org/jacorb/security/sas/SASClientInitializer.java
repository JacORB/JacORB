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

import org.jacorb.util.Debug;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

/**
 * This initializes the SAS Client Security Service (CSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class SASClientInitializer
        extends org.omg.CORBA.LocalObject
        implements ORBInitializer
{
    private static SASClientInterceptor interceptor = null;
    /**
    * This method registers the interceptors.
    */
    public void post_init( ORBInitInfo info )
    {

        // install the CSS interceptor
        try
        {
            interceptor = new SASClientInterceptor(info);
            info.add_client_request_interceptor(interceptor);
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

    public static void setContextToken(byte[] contextToken) {
        interceptor.setContextToken(contextToken);
    }
}






