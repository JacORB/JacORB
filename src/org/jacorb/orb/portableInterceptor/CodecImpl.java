/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;

/**
 * This class represents a codec for encoding CDR encapsulations for the supported
 * GIOP versions 1.0-1.2
 *
 * See PI SPec p.10-77ff
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class CodecImpl
    extends org.omg.CORBA.LocalObject
    implements Codec
{
    private final ORB orb;
    private final int giopMinor;

    public CodecImpl(ORB orb, Encoding enc)
        throws UnknownEncoding
    {
        super();

        if (enc.format != ENCODING_CDR_ENCAPS.value)
        {
            throw new UnknownEncoding();
        }

        if (enc.major_version != 1 || enc.minor_version > 2 )
        {
            throw new UnknownEncoding();
        }

        this.orb = orb;
        this.giopMinor = enc.minor_version;
    }

    // implementation of org.omg.IOP.CodecOperations interface

    public Any decode(byte[] data)
        throws FormatMismatch
    {
        final CDRInputStream in = new CDRInputStream(orb, data);

        try
        {
            in.setGIOPMinor( giopMinor );

            in.openEncapsulatedArray();
            Any result = in.read_any();

            //not necessary, since stream is never used again
            //in.closeEncapsulation();

            return result;
        }
        finally
        {
            in.close();
        }
    }


    public Any decode_value(byte[] data, TypeCode tc)
        throws FormatMismatch, TypeMismatch
    {
        final CDRInputStream in = new CDRInputStream(orb, data);

        try
        {
            in.setGIOPMinor( giopMinor );

            in.openEncapsulatedArray();
            Any result = orb.create_any();
            result.read_value(in, tc);

            //not necessary, since stream is never used again
            //in.closeEncasupaltion();

            return result;
        }
        finally
        {
            in.close();
        }
    }

    public byte[] encode(Any data)
        throws InvalidTypeForEncoding
    {
        final CDROutputStream out = new CDROutputStream(orb);
        try
        {
            out.setGIOPMinor( giopMinor );

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
             }
             */

            /*
             * We have to copy anyway since we need an exact-sized array.
             * Closing afterwards, to return buffer to BufferManager.
             */
            byte[] result = out.getBufferCopy();

            return result;
        }
        finally
        {
            out.close();
        }
    }

    public byte[] encode_value(Any data)
        throws InvalidTypeForEncoding
    {
        final CDROutputStream out = new CDROutputStream(orb);

        try
        {
            out.setGIOPMinor( giopMinor );

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
             }
             */

            /*
             * We have to copy anyway since we need an exact-sized array.
             * Closing afterwards, to return buffer to BufferManager.
             */
            byte[] result = out.getBufferCopy();

            return result;
        }
        finally
        {
            out.close();
        }
    }

} // CodecImpl
