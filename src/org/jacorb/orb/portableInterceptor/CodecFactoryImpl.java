/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
package org.jacorb.orb.portableInterceptor;

import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.CORBA.ORB;

/**
 * This class represents a CodecFactory. The factory
 * has currently only one Codec, for ENCODING_CDR_ENCAPS 1.0. <br>
 * If users like to add their own codec, they have to modify
 * create_codec().
 *
 * See PI Spec p.10-80
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class CodecFactoryImpl 
    extends org.omg.CORBA.LocalObject 
    implements CodecFactory 
{
    private ORB orb = null;
  
    public CodecFactoryImpl(ORB orb) 
    {
        this.orb = orb;
    }

    /**
     * implementation of org.omg.IOP.CodecFactoryOperations interface
     */

    public Codec create_codec(Encoding enc) 
        throws UnknownEncoding 
    {
        return new CodecImpl( orb, enc );
    }  
} // CodecFactoryImpl






