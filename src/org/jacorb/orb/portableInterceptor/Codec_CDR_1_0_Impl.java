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
package org.jacorb.orb.portableInterceptor;

import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.Codec;
import org.omg.CORBA.*;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.util.Debug;
/**
 * This class represents a codec for encoding ENCODING_CDR_ENCAPS 1.0.
 *
 * See PI SPec p.10-77ff
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Codec_CDR_1_0_Impl 
    extends org.omg.CORBA.LocalObject 
    implements Codec 
{

    private ORB orb = null;

    public Codec_CDR_1_0_Impl(ORB orb) {
        this.orb = orb;
    }

    // implementation of org.omg.IOP.CodecOperations interface

    public Any decode(byte[] data) throws FormatMismatch 
    {
        CDRInputStream in = new CDRInputStream(orb, data);

        in.openEncapsulatedArray();
        Any result = in.read_any();
    
        //not necessary, since stream is never used again
        //in.closeEncapsulation();

        return result;
    }


    public Any decode_value(byte[] data, TypeCode tc) 
        throws FormatMismatch, TypeMismatch 
    {
        CDRInputStream in = new CDRInputStream(orb, data);

        in.openEncapsulatedArray();
        Any result = orb.create_any();
        result.read_value(in, tc);
   
        //not necessary, since stream is never used again
        //in.closeEncasupaltion(); 

        return result;
    }

    public byte[] encode(Any data) 
        throws InvalidTypeForEncoding 
    {
        CDROutputStream out = new CDROutputStream(orb);
    
        out.beginEncapsulatedArray();
        out.write_any(data);

        /*
          closing must not be done, since it will patch the
          array with a size!
        try
        {
            out.endEncapsulation();
        }
        catch (java.io.IOException e)
        {
            Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, e);
        }
        */

        /*
         * We have to copy anyway since we need an exact-sized array.
         * Closing afterwards, to return buffer to BufferManager.
         */
        byte[] result = out.getBufferCopy();
        out.release();

        return result;
    }

    public byte[] encode_value(Any data) 
        throws InvalidTypeForEncoding 
    {
  
        CDROutputStream out = new CDROutputStream(orb);
    
        out.beginEncapsulatedArray();
        data.write_value(out);

        /*
          closing must not be done, since it will patch the
          array with a size!
    
        try
        {
            out.endEncapsulation();
        }
        catch (java.io.IOException e)
        {
            Debug.output(Debug.INFORMATION | Debug.INTERCEPTOR, e);
        }
        */

        /*
         * We have to copy anyway since we need an exact-sized array.
         * Closing afterwards, to return buffer to BufferManager.
         */
        byte[] result = out.getBufferCopy();
        out.release();

        return result;
    }

} // Codec_CDR_1_0_Impl






