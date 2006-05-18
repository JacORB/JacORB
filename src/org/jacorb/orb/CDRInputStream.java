package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.io.IOException;
import java.util.*;

import org.apache.avalon.framework.configuration.*;
import org.apache.avalon.framework.logger.*;

import org.jacorb.orb.giop.CodeSet;
import org.jacorb.util.ValueHandler;
import org.jacorb.ir.RepositoryID;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;

/**
 * Read CDR encoded data
 *
 * @author Gerald Brose, FU Berlin
 * $Id$
 */

public class CDRInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
{
    /**
     * <code>uniqueValue</code> is used to fill in a value for empty
     * member names.
     */
    private int uniqueValue;

    /**
     * <code>encaps_stack</code> is used to saving/restoring
     * encapsulation information. Do NOT access this variable directly.
     * It is initialized on demand. Use the method {@link #getEncapsStack()}
     */
    private Stack encaps_stack;

    /**
     * <code>recursiveTCMap</code> is used to to remember the original
     * TCs for a given ID that is used in a recursive/repeated TC. Do
     * NOT access this variable directly. It is initialized on demand.
     * Use the method {@link #getRecursiveTCMap()}
     */
    private Map recursiveTCMap;

    /**
     * <code>cachedTypecodes</code> stores a mapping of ID/Typecode to
     * speed reading from the stream. Do NOT access this variable directly. It
     * is initialized on demand. Use the methods
     * {@link #getCachedTypecode(String id)} and
     * {@link #putCachedTypecode(String id, org.omg.CORBA.TypeCode result)}
     * Skip amount is
     * skip (size - ((pos - start_pos) - 4 - 4));
     * EncapsulationSize -
     * ( PositionAfterReadingId - start_pos
     *   - 4 [Size] - 4 [KindSize] ) = RemainingSizeToSkip
     */
    private Map cachedTypecodes;

    /** indexes to support mark/reset */
    private int marked_pos;
    private int marked_index;

    private boolean closed;

    /** configurable properties */
    private Logger logger;
    private boolean useBOM;
    private boolean cometInteropFix;
    private boolean laxBooleanEncoding;
    private boolean cacheTypecodes;

    /* character encoding code sets for char and wchar, default ISO8859_1 */
    private int codeSet =  CodeSet.getTCSDefault();
    private int codeSetW = CodeSet.getTCSWDefault();
    protected int giop_minor = 2; // needed to determine size in chars

    /**
     * <code>valueMap</code> maps indices within the buffer
     * (java.lang.Integer) to the values that appear at these indices. Do
     * NOT access this variable directly. It is initialized on demand.
     * Use the method {@link #getValueMap()}
     */
    private Map valueMap;

    /**
     * Index of the current IDL value that is being unmarshalled.
     * This is kept here so that when the value object has been
     * created, the value factory can immediately store it into this
     * stream's valueMap by calling `register_value()'.
     */
    private int currentValueIndex;

    /**
     * <code>repIdMap</code> maps indices within the buffer
     * (java.lang.Integer) to repository ids that appear at these indices.
     * Do NOT access this variable directly. It is initialized on demand.
     * Use the method {@link #getRepIdMap()}
     */
    private Map repIdMap;

    /**
     * <code>codebaseMap</code> maps indices within the buffer
     * (java.lang.Integer) to codebase strings that appear at these indices.
     * Do NOT access this variable directly. It is initialized on demand.
     * Use the method {@link #getCodebaseMap()}
     */
    private Map codebaseMap;

    public boolean littleEndian = false;

    /** indices into the actual buffer */
    protected byte[] buffer = null;
    protected int pos = 0;
    protected int index = 0;

    /** Last value tag read had the chunking bit on */
    private boolean chunkedValue = false;

    /** Nesting level of chunked valuetypes */
    private int valueNestingLevel = 0;

    /** Ending position of the current chunk */
    private int chunk_end_pos = -1;   // -1 means we're not within a chunk


    /**
     * for this stream to be able to return a live object reference, a
     * full ORB (not the Singleton!) must be known. If this stream is
     * used only to demarshal base type data, the Singleton is enough
     */
    private org.omg.CORBA.ORB orb = null;

    public CDRInputStream(final org.omg.CORBA.ORB orb, final byte[] buf)
    {
        buffer = buf;
        // orb may be null!
        if (orb != null)
        {
            this.orb = orb;
            // orb may be the singleton!
            if (orb instanceof org.jacorb.orb.ORB)
            {
                try
                {

                    configure(((org.jacorb.orb.ORB)orb).getConfiguration());
                }
                catch( ConfigurationException ce )
                {
                    throw new INTERNAL("ConfigurationException: " + ce);
                }
            }
        }
        else
            this.orb = org.omg.CORBA.ORB.init();
    }

    public CDRInputStream(final org.omg.CORBA.ORB orb,
                          final byte[] buf,
                          final boolean littleEndian )
    {
        this( orb, buf );
        this.littleEndian = littleEndian;
    }


    /**
     * This stream is self-configuring, i.e. configure() is private
     * and only called from the constructors.
     */

    private void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger =
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.orb.cdr");

        useBOM =
            configuration.getAttribute("jacorb.use_bom","off").equals("on");
        cometInteropFix =
            configuration.getAttribute("jacorb.interop.comet","off").equals("on");
        laxBooleanEncoding =
            configuration.getAttribute("jacorb.interop.lax_boolean_encoding","off").equals("on");
        cacheTypecodes =
            configuration.getAttribute("jacorb.cacheTypecodes","off").equals("on");
    }




    /**
     * <code>getEncapsStack</code> is used to initialize encaps_stack
     * on demand.
     *
     * @return a <code>Stack</code> value
     */
    private Stack getEncapsStack()
    {
        if (encaps_stack == null)
        {
            encaps_stack = new Stack();
        }
        return encaps_stack;
    }


    /**
     * Gets the Map that keeps track of recursive TypeCodes.
     *
     * @return a <code>Map</code> value
     */
    private Map getRecursiveTCMap()
    {
        if (recursiveTCMap == null)
        {
            recursiveTCMap = new HashMap();
        }
        return recursiveTCMap;
    }


    /**
     * Gets the Map that is used to demarshal shared valuetype instances.
     *
     * @return a <code>Map</code> value
     */
    private Map getValueMap()
    {
        if (valueMap == null)
        {
            // Unlike the valueMap in CDROutputStream, this one
            // does need to be an equality-based HashMap.
            valueMap = new HashMap();
        }
        return valueMap;
    }


    /**
     * Gets the Map that is used to implement indirections for RepositoryIDs.
     *
     * @return a <code>Map</code> value
     */
    private Map getRepIdMap()
    {
        if (repIdMap == null)
        {
            repIdMap = new HashMap();
        }
        return repIdMap;
    }


    /**
     * Gets the Map that is used to implement sharing for codebase
     * specifications.
     *
     * @return a <code>Map</code> value
     */
    private Map getCodebaseMap()
    {
        if (codebaseMap == null)
        {
            codebaseMap = new HashMap();
        }
        return codebaseMap;
    }


    /**
     * <code>getCachedTypecode</code> to retrieve a value from cachedTypecodes.
     * It may initialize the value on demand.
     *
     * @param id a <code>String</code> value
     * @return a <code>org.omg.CORBA.TypeCode</code> value, possibly null.
     */
    private org.omg.CORBA.TypeCode getCachedTypecode( String id )
    {
        org.omg.CORBA.TypeCode result = null;

        if ( cacheTypecodes )
        {
            if ( cachedTypecodes == null )
            {
                cachedTypecodes = new HashMap();
            }
            else
            {
                result = ( org.omg.CORBA.TypeCode )cachedTypecodes.get( id );
            }
        }
        return result;
    }


    /**
     * <code>putCachedTypecode</code> is used to store typecodes within the
     * cachedTypecodes. It will only do it cacheTypecodes is on.
     *
     * @param id a <code>String</code> value
     * @param result an <code>org.omg.CORBA.TypeCode</code> value
     */
    private void putCachedTypecode( String id, org.omg.CORBA.TypeCode result)
    {
        if ( cacheTypecodes )
        {
            // By definition get/put should be paired so cachedTypecodes should
            // never be null here.
            cachedTypecodes.put (id, result);
        }
    }


    public void setGIOPMinor(final  int giop_minor )
    {
        this.giop_minor = giop_minor;
    }

    public int getGIOPMinor()
    {
        return giop_minor;
    }

    public void close()
        throws IOException
    {
        // Don't need to call super.close as super is noop.
        if( closed )
        {
            return;
        }

        BufferManager.getInstance().returnBuffer(buffer);

        encaps_stack = null;
        recursiveTCMap = null;
        closed = true;
    }

    public org.omg.CORBA.ORB orb()
    {
        if (orb == null) orb = org.omg.CORBA.ORB.init(new String[]{}, null);
        return orb;
    }

    public void setCodeSet(final int codeSet, final int codeSetWide)
    {
        this.codeSet = codeSet;
        this.codeSetW = codeSetWide;
    }

    private static final int _read4int
       (final boolean _littleEndian, final byte[] _buffer, final int _pos)
    {
        if (_littleEndian)
            return (((_buffer[_pos+3] & 0xff) << 24) +
                    ((_buffer[_pos+2] & 0xff) << 16) +
                    ((_buffer[_pos+1] & 0xff) <<  8) +
                    ((_buffer[_pos]   & 0xff) <<  0));
        else
            return (((_buffer[_pos]   & 0xff) << 24) +
                    ((_buffer[_pos+1] & 0xff) << 16) +
                    ((_buffer[_pos+2] & 0xff) <<  8) +
                    ((_buffer[_pos+3] & 0xff) <<  0));
    }

    private static final short _read2int
       (final boolean _littleEndian, final byte[] _buffer, final int _pos)
    {
        if (_littleEndian)
            return  (short)(((_buffer[_pos+1] & 0xff) << 8) +
                            ((_buffer[_pos]   & 0xff) << 0));
        else
            return (short)(((_buffer[_pos ]    & 0xff) << 8) +
                           ((_buffer[_pos + 1] & 0xff) << 0));
    }

    private final int _read_long()
    {
        int result;

        result = _read4int (littleEndian, buffer, pos);

        index += 4;
        pos += 4;
        return result;
    }

    private final long _read_longlong()
    {
        if (littleEndian)
        {
            return ((long) _read_long() & 0xFFFFFFFFL) + ((long) _read_long() << 32);
        }
        else
        {
            return ((long) _read_long() << 32) + ((long) _read_long() & 0xFFFFFFFFL);
        }
    }

    private final void handle_chunking()
    {
        int remainder = 4 - (index % 4);
        int aligned_pos = (remainder != 4) ? pos + remainder : pos;

        if (chunk_end_pos >= pos && chunk_end_pos <= aligned_pos)
        {
            chunk_end_pos = -1;
            int saved_pos = pos;
            int saved_index = index;
            int tag = read_long();

            if (tag < 0) {

                // tag is an end tag

                if ( ! (-tag <= valueNestingLevel))
                {
                    throw new INTERNAL
                    (
                        "received end tag " + tag +
                        " with value nesting level " +
                        valueNestingLevel
                    );
                }
                valueNestingLevel = - tag;
                valueNestingLevel--;

                if (valueNestingLevel > 0)
                {
                    chunk_end_pos = pos;
                    handle_chunking();
                }
            }
            else if (tag < 0x7fffff00)
            {
                // tag is the chunk size tag of another chunk

                chunk_end_pos = pos + tag;
            }
            else // (tag >= 0x7fffff00)
            {
                // tag is the value tag of a nested value

                pos = saved_pos;      // "unread" the value tag
                index = saved_index;
            }
        }
    }

    protected final void skip(final int distance)
    {
        pos += distance;
        index += distance;
    }

    /**
     * close a CDR encapsulation and
     * restore index and byte order information
     */

    public final void closeEncapsulation()
    {
        if (encaps_stack == null)
        {
            throw new MARSHAL( "Internal Error - closeEncapsulation failed" );
        }

        EncapsInfo ei = (EncapsInfo)encaps_stack.pop();
        littleEndian = ei.littleEndian;
        int size = ei.size;
        int start = ei.start;

        if( pos < start + size )
            pos = start + size;

        index = ei.index + size;
    }

    /**
     * open a CDR encapsulation and
     * restore index and byte order information
     */

    public final int openEncapsulation()
    {
        boolean old_endian = littleEndian;
        int _pos = pos;
        int size = read_long();

        // Check if size looks sane. If not try changing byte order.
        // This is a specific fix for interoperability with the Iona
        // Comet COM/CORBA bridge that has problems with size marshalling.

        if (cometInteropFix && ((size < 0) || (size > buffer.length)))
        {
            int temp =
            (
                ((size >> 24) & 0x000000FF) +
                ((size >> 8)  & 0x0000FF00) +
                ((size << 8)  & 0x00FF0000) +
                ((size << 24) & 0xFF000000)
            );

            if (logger.isDebugEnabled())
            {
                logger.debug("Size of CDR encapsulation larger than buffer, swapping byte order\n" +
                             "Size of CDR encapsulation was " + size + ", is now " + temp);
            }

            size = temp;
        }
        /* save current index plus size of the encapsulation on the stack.
           When the encapsulation is closed, this value will be restored as
           index */

        if (encaps_stack == null)
        {
            encaps_stack = new Stack();
        }
        encaps_stack.push(new EncapsInfo(old_endian, index, pos, size ));

        openEncapsulatedArray();

        return size;
    }

    public final void openEncapsulatedArray()
    {
        /* reset index  to zero, i.e. align relative  to the beginning
           of the encaps. */
        resetIndex();
        littleEndian = read_boolean();
    }


    /*
     * Return a copy of the current buffer. Currently only used by ProxyImpl.
     *
     * @return a <code>byte[]</code> value.
     */
    public byte[] getBufferCopy()
    {
        byte[] result = new byte[buffer.length];
        System.arraycopy
        (
            buffer,
            0,
            result,
            0,
            buffer.length
        );
        return result;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned.
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     * @throws java.io.IOException if stream is closed.
     */
    public int read()
        throws java.io.IOException
    {
        if( closed )
            throw new java.io.IOException("Stream already closed!");

        if( available() < 1 )
            return -1;

        ++index;
        return buffer[pos++]; // read_index++];
    }

    /**
     * @return the number of bytes that can be read (or skipped over) from this
     *         input stream.  This is not necessarily the number of 'valid' bytes.
     */
    public int available()
    {
        return buffer.length - index;
    }

    /**
     * Has the effect of read(b, 0, b.length);
     * @see #read
     */
    public int read(final byte[] b)
        throws java.io.IOException
    {
        return read(b, 0, b.length);
    }

    /**
     * Performs as described by <code>java.io.InputStream.read(byte[], int, int)</code>,
     * but never blocks.
     */
    public int read(final byte[] b, final int off, final int len)
        throws java.io.IOException
    {
        if( b == null )
        {
            throw new java.io.IOException("buffer may not be null");
        }

        if( off < 0 ||
            len < 0 ||
            off + len > b.length )
        {
            throw new java.io.IOException("buffer index out of bounds");
        }

        if( len == 0 )
        {
            return 0;
        }

        if( available() < 1 )
        {
            return -1;
        }

        if( closed )
        {
            throw new java.io.IOException("Stream already closed!");
        }

        int min = Math.min(len, available());
        System.arraycopy(buffer, index, b, off, min );
        pos += min;
        index += min;
        return min;
    }


    public final org.omg.CORBA.Any read_any()
    {
        org.omg.CORBA.TypeCode _tc = read_TypeCode();
        org.omg.CORBA.Any any = orb.create_any();
        any.read_value( this, _tc );
        return any;
    }

    public final boolean read_boolean()
    {
        handle_chunking();
        index++;
        byte bb = buffer[pos++];

        if (bb == 0)
        {
            return false;
        }
        else
        {
            if (bb == 1)
            {
                return true;
            }
            else
            {
                if (laxBooleanEncoding)
                {
                    // Technically only valid values are 0 (false) and 1 (true)
                    // however some ORBs send values other than 1 for true.
                    return true;
                }
                else
                {
                    throw new MARSHAL("Unexpected boolean value: " + bb
                                      + " pos: " + pos + " index: " + index);
                }
            }
        }
    }

    /** arrays */

    public final void read_boolean_array
       (final boolean[] value, final int offset, final int length)
    {
        handle_chunking();
        byte bb;
        for (int j = offset; j < offset + length; j++)
        {
            index++;
            bb = buffer[pos++];
            if (bb == 1)
            {
                value[j] = true;
            }
            else if (bb == 0)
            {
                value[j] = false;
            }
            else
            {
                throw new MARSHAL ("Unexpected boolean value: " + bb
                                   + " pos: " + pos + " index: " + index);
            }
        }
    }

    public final char read_char()
    {
        handle_chunking();
        index++;
        return (char)(0xff & buffer[pos++]);
    }

    public final void read_char_array
       (final char[] value, final int offset, final int length)
    {
        handle_chunking();
        for (int j = offset; j < offset + length; j++)
        {
            index++;
            value[j] = (char) (0xff & buffer[pos++]);
        }
    }

    public final double read_double()
    {
        return Double.longBitsToDouble (read_longlong());
    }

    public final void read_double_array
       (final double[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 8 - (index % 8);
        if (remainder != 8)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset + length; j++)
        {
            value[j] = Double.longBitsToDouble (_read_longlong());
        }
    }

    public final java.math.BigDecimal read_fixed()
    {
        handle_chunking();

        StringBuffer sb = new StringBuffer();

        int b = buffer[pos++];
        int c = b & 0x0F; // second half byte
        index++;

        while(true)
        {
            c = (b & 0xF0) >>> 4;
            sb.append(c );
            c = b & 0x0F;
            if( c == 0xC || c == 0xD )
                break;
            sb.append(c );
            b = buffer[pos++];
            index++;
        }

        java.math.BigDecimal result =
        new java.math.BigDecimal( new java.math.BigInteger( sb.toString()));

        if( c == 0xD )
            return result.negate();
        else
            return result;

    }

    public final float read_float()
    {
        return Float.intBitsToFloat (read_long());
    }

    public final void read_float_array
       (final float[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset + length; j++)
        {
            value[j] = Float.intBitsToFloat (_read_long());
        }
    }

    public final int read_long()
    {
        handle_chunking();

        int result;

        int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos += remainder;
        }

        result = _read4int (littleEndian, buffer, pos);

        index += 4;
        pos += 4;
        return result;
    }

    public final void read_long_array
       (final int[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset+length; j++)
        {
            value[j] = _read4int (littleEndian,buffer,pos);
            pos += 4;
        }

        index += 4 * length;
    }


    public final long read_longlong()
    {
        handle_chunking();

        int remainder = 8 - (index % 8);
        if (remainder != 8)
        {
            index += remainder;
            pos += remainder;
        }

        if (littleEndian)
        {
            return ((long) _read_long() & 0xFFFFFFFFL) + ((long) _read_long() << 32);
        }
        else
        {
            return ((long) _read_long() << 32) + ((long) _read_long() & 0xFFFFFFFFL);
        }
    }

    public final void read_longlong_array
        (final long[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 8 - (index % 8);
        if (remainder != 8)
        {
            index += remainder;
            pos += remainder;
        }

        if (littleEndian)
        {
            for(int j=offset; j < offset+length; j++)
            {
                value[j] = ( (long) _read_long() & 0xFFFFFFFFL) +
                    ((long) _read_long() << 32);
            }
        }
        else
        {
            for(int j=offset; j < offset+length; j++)
            {
                value[j] = ((long) _read_long() << 32) +
                    ((long) _read_long() & 0xFFFFFFFFL);
            }
        }

        // Do not need to modify pos and index as use read_long above
    }

    public final org.omg.CORBA.Object read_Object()
    {
        if (! (orb instanceof org.jacorb.orb.ORB))
        {
            throw new MARSHAL
                ( "Cannot use the singleton ORB to receive object references, "
                       + "please initialize a full ORB instead.");
        }

        handle_chunking();

        org.omg.IOP.IOR ior = org.omg.IOP.IORHelper.read(this);
        ParsedIOR pior = new ParsedIOR( ior, (org.jacorb.orb.ORB)orb, logger );

        if( pior.isNull() )
        {
            return null;
        }
        else
        {
            return ((org.jacorb.orb.ORB)orb)._getObject( pior );
        }
    }

    public org.omg.CORBA.Object read_Object(final java.lang.Class clz)
    {
        if (org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(clz))
        {
            org.omg.CORBA.Object obj = read_Object();
            if (obj instanceof org.omg.CORBA.portable.ObjectImpl)
            {
                org.omg.CORBA.portable.ObjectImpl stub = null;
                try
                {
                    stub = (org.omg.CORBA.portable.ObjectImpl)clz.newInstance();
                }
                catch (InstantiationException e)
                {
                    throw new MARSHAL("Exception in stub instantiation: " + e);
                }
                catch (IllegalAccessException e)
                {
                    throw new MARSHAL("Exception in stub instantiation: " + e);
                }
                stub._set_delegate(
                     ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
                return stub;
            }
            else
            {
                return obj;
            }
        }
        else if (clz.isInterface() &&
                 java.rmi.Remote.class.isAssignableFrom(clz))
        {
            return (org.omg.CORBA.Object)
                org.jacorb.util.ValueHandler.portableRemoteObject_narrow(
                                                           read_Object(), clz);
        }
        else
        {
            return read_Object();
        }
    }

    public final byte read_octet()
    {
        handle_chunking();
        index++;
        return buffer[pos++];
    }

    public final void read_octet_array
        (final byte[] value, final int offset, final int length)
    {
        handle_chunking();
        System.arraycopy (buffer,pos,value,offset,length);
        index += length;
        pos += length;
    }

    public final org.omg.CORBA.Principal read_Principal()
    {
        throw new NO_IMPLEMENT ("Principal deprecated");
    }

    /**
     *   Read methods for big-endian as well as little endian data input
     *   contributed by Mark Allerton <MAllerton@img.seagatesoftware.com>
     */

    public final short read_short()
    {
        handle_chunking();

        int remainder = 2 - (index % 2);
        if (remainder != 2)
        {
            index += remainder;
            pos += remainder;
        }

        short result = _read2int (littleEndian,buffer,pos);
        pos += 2;
        index += 2;
        return result;
    }

    public final void read_short_array
        (final short[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 2 - (index % 2);

        if (remainder != 2)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset + length; j++)
        {
            value[j] = _read2int (littleEndian, buffer, pos);
            pos += 2;
        }

        index += length * 2;
    }

    public final String read_string()
    {
        String result = null;

        handle_chunking();

        int remainder = 4 - (index % 4);
        if( remainder != 4 )
        {
            index += remainder;
            pos += remainder;
        }

        // read size (#bytes)
        int size = _read4int( littleEndian, buffer, pos);
        int start = pos + 4;

        index += (size + 4);
        pos += (size + 4);
        String csname = CodeSet.csName(codeSet);

        if ((size > 0) &&
            (buffer[ start + size - 1 ] == 0))
        {
            size --;
        }
        try {
          result = new String (buffer, start, size, csname);
        }
        catch (java.io.UnsupportedEncodingException ex) {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error("Charset " + csname + " is unsupported");
                result = "";
            }
        }
        return result;
    }


    public final org.omg.CORBA.TypeCode read_TypeCode()
    {
        Map tcMap = new HashMap();
        org.omg.CORBA.TypeCode result = read_TypeCode( tcMap );

        return result;
    }

    private final org.omg.CORBA.TypeCode read_TypeCode(final Map tcMap )
    {
        String  id           = null;
        String  name         = null;
        int     member_count = 0;
        int     length       = 0;
        int     size         = 0;
        boolean byteorder    = false;
        org.omg.CORBA.TypeCode result = null;
        org.omg.CORBA.TypeCode content_type = null;
        String[] member_names = null;

        int kind = read_long();
        int start_pos = pos - 4;

        if (logger != null && logger.isDebugEnabled())
        {
            logger.debug("Read Type code of kind " + kind + " at pos: " + start_pos);
        }

        switch( kind )
        {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_short:
            case TCKind._tk_long:
            case TCKind._tk_ushort:
            case TCKind._tk_ulong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_octet:
            case TCKind._tk_any:
            case TCKind._tk_TypeCode:
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_wchar:
            case TCKind._tk_Principal:
            {
                result = orb.get_primitive_tc
                    (org.omg.CORBA.TCKind.from_int (kind));
                break;
            }
            case TCKind._tk_objref:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                }
                else
                {
                    name = validateName (read_string());
                    result = orb.create_interface_tc (id, name);
                    putCachedTypecode( id, result );
                }
                closeEncapsulation();
                break;
            }
            case TCKind._tk_struct:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());
                    member_count = read_long();

                    tcMap.put( new Integer( start_pos ), id );

                    StructMember[] struct_members = new StructMember[member_count];
                    for( int i = 0; i < member_count; i++)
                    {
                        struct_members[i] = new StructMember
                        (
                            read_string(),
                            read_TypeCode (tcMap),
                            null
                        );
                    }
                    result = ((ORBSingleton) orb).create_struct_tc(id, name, struct_members, false);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put (id, result);
                closeEncapsulation();
                break;
            }
            case TCKind._tk_except:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());
                    member_count = read_long();

                    tcMap.put( new Integer( start_pos ), id );

                    StructMember[] members = new StructMember[member_count];
                    for( int i = 0; i < member_count; i++)
                    {
                        members[i] = new StructMember
                        (
                            read_string(),
                            read_TypeCode(tcMap),
                            null
                        );
                    }
                    result = ((ORBSingleton)orb).create_exception_tc (id, name, members, false);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put (id, result);
                closeEncapsulation();
                break;
            }
            case TCKind._tk_enum:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());
                    member_count = read_long();

                    tcMap.put( new Integer( start_pos ), id );

                    member_names = new String[member_count];
                    for( int i = 0; i < member_count; i++)
                    {
                        member_names[i] = read_string();
                    }
                    result = ((ORBSingleton)orb).create_enum_tc (id, name, member_names, false);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put (id, result);
                closeEncapsulation();
                break;
            }
            case TCKind._tk_union:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());

                    tcMap.put( new Integer( start_pos ), id );

                    org.omg.CORBA.TypeCode discriminator_type = read_TypeCode(tcMap);
                    // Use the dealiased discriminator type for the label types.
                    // This works because the JacORB IDL compiler ignores any aliasing
                    // of label types and only the discriminator type is passed on the
                    // wire.
                    org.omg.CORBA.TypeCode orig_disc_type =
                    TypeCode.originalType(discriminator_type);

                    int default_index = read_long();
                    member_count = read_long();

                    UnionMember[] union_members = new UnionMember[member_count];
                    for( int i = 0; i < member_count; i++)
                    {
                        org.omg.CORBA.Any label = orb.create_any();

                        if( i == default_index )
                        {
                            // Default discriminator
                            label.insert_octet( read_octet());
                        }
                        else
                        {
                            // use the dealiased discriminator type to construct labels
                            label.read_value( this, orig_disc_type );
                        }

                        union_members[i] = new UnionMember
                        (
                            read_string(),
                            label,
                            read_TypeCode(tcMap),
                            null
                        );
                    }

                    result = ((ORBSingleton)orb).create_union_tc(id, name, discriminator_type, union_members, false);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put (id, result);
                closeEncapsulation();
                break;
            }
            case TCKind._tk_string:
            {
                result = orb.create_string_tc(read_long());
                break;
            }
            case TCKind._tk_wstring:
            {
                result = orb.create_wstring_tc(read_long());
                break;
            }
            case TCKind._tk_fixed:
            {
                result = orb.create_fixed_tc(read_ushort(), read_short() );
                break;
            }
            case TCKind._tk_array:
            {
                openEncapsulation();

                content_type = read_TypeCode(tcMap);
                length = read_long();

                closeEncapsulation();
                result = orb.create_array_tc(length, content_type);
                break;
            }
            case TCKind._tk_sequence:
            {
                openEncapsulation();

                content_type = read_TypeCode(tcMap);
                length = read_long();

                closeEncapsulation();

                result = orb.create_sequence_tc(length, content_type);
                break;
            }
            case TCKind._tk_alias:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());

                    tcMap.put( new Integer( start_pos ), id );

                    content_type = read_TypeCode( tcMap );
                    result = orb.create_alias_tc (id, name, content_type );
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put (id , result);
                closeEncapsulation();
                break;
            }
            case TCKind._tk_value:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());

                    tcMap.put( new Integer( start_pos ), id );

                    short type_modifier = read_short();
                    org.omg.CORBA.TypeCode concrete_base_type = read_TypeCode( tcMap );
                    member_count = read_long();
                    ValueMember[] vMembers = new ValueMember[member_count];

                    for( int i = 0; i < member_count; i++)
                    {
                        vMembers[i] = new ValueMember
                        (
                            read_string(),
                            null, // id
                            null, // defined_in
                            null, // version
                            read_TypeCode (tcMap),
                            null, // type_def
                            read_short()
                        );
                    }
                    result = orb.create_value_tc
                        (id, name, type_modifier, concrete_base_type, vMembers);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put( id , result );
                closeEncapsulation();
                break;
            }
            case TCKind._tk_value_box:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                    tcMap.put( new Integer( start_pos ), id );
                }
                else
                {
                    name = validateName (read_string());

                    tcMap.put( new Integer( start_pos ), id );

                    content_type = read_TypeCode( tcMap );
                    result = orb.create_value_box_tc (id, name, content_type);
                    putCachedTypecode( id, result );
                }
                getRecursiveTCMap().put( id , result );
                closeEncapsulation();
                break;
            }
            case TCKind._tk_abstract_interface:
            {
                size = openEncapsulation();
                id = validateID (read_string());
                result = getCachedTypecode( id );

                if (result != null)
                {
                    // Skip buffer - see cachedTypecodes for calculation.
                    skip (size - ((pos - start_pos) - 4 - 4));
                }
                else
                {
                    name = validateName (read_string());
                    result = orb.create_abstract_interface_tc (id, name);
                    putCachedTypecode( id, result );
                }
                closeEncapsulation();
                break;
            }
            case 0xffffffff:
            {
                /* recursive TC */
                int negative_offset = read_long();
                String recursiveId =
                    (String)tcMap.get( new Integer( pos - 4 + negative_offset ) );

                if (recursiveId == null)
                {
                    throw new INTERNAL
                    (
                        "No recursive TypeCode! (pos: " +
                        (pos - 4 + negative_offset) +
                        ")"
                    );
                }

                // look up TypeCode in map to check if it's repeated
                org.omg.CORBA.TypeCode rec_tc =
                    (org.omg.CORBA.TypeCode)( getRecursiveTCMap().get( recursiveId ) );

                if (rec_tc == null)
                {
                    // TypeCode is not in map so it is recursive
                    rec_tc = orb.create_recursive_tc( recursiveId );
                }

                result = rec_tc;
                break;
            }
            default:
            {
                // error, dump buffer contents for diagnosis
                throw new MARSHAL("Cannot handle TypeCode with kind " + kind);
            }
        }
        return result;
    }

    public final int read_ulong()
    {
        handle_chunking();

        int result;

        int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos += remainder;
        }

        result = _read4int (littleEndian, buffer, pos);

        index += 4;
        pos += 4;
        return result;
    }

    public final void read_ulong_array
        (final int[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset+length; j++)
        {
            value[j] = _read4int (littleEndian,buffer,pos);
            pos += 4;
        }

        index += 4 * length;
    }

    public final long read_ulonglong()
    {
        handle_chunking();

        int remainder = 8 - (index % 8);
        if (remainder != 8)
        {
            index += remainder;
            pos += remainder;
        }

        if (littleEndian)
        {
            return ((long) _read_long() & 0xFFFFFFFFL) + ((long) _read_long() << 32);
        }
        else
        {
            return ((long) _read_long() << 32) + ((long) _read_long() & 0xFFFFFFFFL);
        }
    }

    public final void read_ulonglong_array
        (final long[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 8 - (index % 8);
        if (remainder != 8)
        {
            index += remainder;
            pos += remainder;
        }

        if (littleEndian)
        {
            for (int j = offset; j < offset+length; j++)
            {
                value[j] = ( (long) _read_long() & 0xFFFFFFFFL) +
                    ((long) _read_long() << 32);
            }
        }
        else
        {
            for (int j = offset; j < offset+length; j++)
            {
                value[j] = ((long) _read_long() << 32) +
                    ((long) _read_long() & 0xFFFFFFFFL);
            }
        }

        // Do not need to modify pos and index as use read_long above
    }

    public final short read_ushort()
    {
        handle_chunking();

        int remainder = 2 - (index % 2);
        if (remainder != 2)
        {
            index += remainder;
            pos += remainder;
        }

        short result = _read2int (littleEndian,buffer,pos);
        pos += 2;
        index += 2;
        return result;
    }

    public final void read_ushort_array
        (final short[] value, final int offset, final int length)
    {
        if (length == 0)
            return;

        handle_chunking();

        int remainder = 2 - (index % 2);

        if (remainder != 2)
        {
            index += remainder;
            pos += remainder;
        }

        for (int j = offset; j < offset + length; j++)
        {
            value[j] = _read2int (littleEndian, buffer, pos);
            pos += 2;
        }

        index += length * 2;
    }

    public final char read_wchar()
    {
        handle_chunking();

        if (giop_minor == 2)
        {
            //ignore size indicator
            read_wchar_size();

            boolean wchar_little_endian = readBOM();

            return read_wchar (wchar_little_endian);
        }
        else
        {
            return read_wchar (littleEndian);
        }
    }

    /**
     * The number of bytes this char takes. This is actually not
     * necessary since the encodings used are either fixed-length
     * (UTF-16) or have their length encoded internally (UTF-8).
     */
    private final int read_wchar_size()
    {
        index++;

        return buffer[ pos++ ];
    }


    private final char read_wchar(final boolean wchar_little_endian)
    {
        switch( codeSetW )
        {
            case CodeSet.UTF8 :
            {
                if( giop_minor < 2 )
                {
                    throw new MARSHAL( "GIOP 1." + giop_minor +
                                       " only allows 2 Byte encodings for wchar, but the selected TCSW is UTF-8" );
                }

                short b = (short) (0xff & buffer[pos++]);
                index++;

                if( (b & 0x80) == 0 )
                {
                    return (char) b;
                }
                else if( (b & 0xe0) == 0xc0 )
                {
                    index++;
                    return (char)(((b & 0x1F) << 6) |
                                  ((short)buffer[pos++] & 0x3F));
                }
                else
                {
                    index += 2;
                    short b2 = (short)(0xff & buffer[pos++]);
                    return (char)(( ( b & 0x0F) << 12) |
                                  ( (b2 & 0x3F) << 6) |
                                  ( (short)buffer[pos++] & 0x3F));
                }
            }
            case CodeSet.UTF16 :
            {
                char c;

                if( wchar_little_endian )
                {
                    c = (char) ( (buffer[ pos++ ] & 0xFF) |
                                 (buffer[ pos++ ] << 8) );
                }
                else
                {
                    c = (char) ( (buffer[ pos++ ] << 8) |
                                 (buffer[ pos++ ] & 0xFF) );
                }

                index += 2;
                return c;
            }
        }

        throw new MARSHAL( "Bad CodeSet: " + codeSetW );
    }

    /**
     * Read the byte order marker indicating the endianess.
     *
     * @return true for little endianess, false otherwise (including
     * no BOM present. In this case, big endianess is assumed per
     * spec).
     */
    private final boolean readBOM()
    {
        /*
          if( !use_BOM )
          return false;
        */

        if( (buffer[ pos     ] == (byte) 0xFE) &&
            (buffer[ pos + 1 ] == (byte) 0xFF) )
        {
            //encountering a byte order marker indicating big
            //endianess

            pos += 2;
            index += 2;

            return false;
        }
        else if( (buffer[ pos     ] == (byte) 0xFF) &&
                 (buffer[ pos + 1 ] == (byte) 0xFE) )
        {
            //encountering a byte order marker indicating
            //little endianess

            pos += 2;
            index += 2;

            return true;
        }
        else
        {
            //no BOM so big endian per spec.
            return false;
        }
    }

    public final void read_wchar_array
        (final char[] value, final int offset, final int length)
    {
        handle_chunking();
        for(int j=offset; j < offset+length; j++)
            value[j] = read_wchar(); // inlining later...
    }

    public final String read_wstring()
    {
        String result = null;
        char buf[] = null;

        handle_chunking();

        int remainder = 4 - (index % 4);
        if( remainder != 4 )
        {
            index += remainder;
            pos += remainder;
        }
        if( giop_minor == 2 )
        {
            // read size in bytes
            int size = _read4int( littleEndian, buffer, pos);
            index += 4;
            pos += 4;

            if( size == 0 )
            {
                return "";
            }

            buf = new char[ size ];

            int i = 0;
            int endPos = pos + size;

            boolean wchar_litte_endian = readBOM();

            while( pos < endPos )
            {
                //ignore size
                //read_wchar_size();

                buf[ i++ ] = read_wchar( wchar_litte_endian );
            }

            result = new String( buf, 0, i );
        }
        else //GIOP 1.1 / 1.0
        {
            // read size
            int size = _read4int( littleEndian, buffer, pos);
            index += 4;
            pos += 4;
            buf = new char[ size ];

            int endPos = pos + size;

            if( codeSetW == CodeSet.UTF16 )
            {
                //size is in chars, but char has 2 bytes
                endPos += size;
            }

            int i = 0;

            while( pos < endPos )
            {
                //use the stream-wide endianess
                buf[ i++ ] = read_wchar( littleEndian );
            }

            if( (i != 0) &&
                (buf[ i - 1 ] == 0) )
            {
                //don't return terminating NUL
                result = new String( buf, 0, i - 1 );
            }
            else
            {
                //doesn't have a terminating NUL. This is actually not
                //allowed.
                result = new String( buf, 0, i );
            }
        }
        buf = null;
        return result;
    }

    public boolean markSupported()
    {
        return true;
    }

    public void mark(final int readLimit)
    {
        marked_pos = pos;
        marked_index = index;
    }

    public void reset()
        throws IOException
    {
        if( pos < 0 )
            throw new MARSHAL("Mark has not been set!");
        pos = marked_pos;
        index = marked_index;
    }

    // JacORB-specific

    private final void resetIndex()
    {
        index = 0;
    }

    public final void setLittleEndian(final boolean b)
    {
        littleEndian = b;
    }

    /**
     * Reads an instance of the type described by type code <code>tc</code>
     * from this CDRInputStream, and remarshals it to the given OutputStream,
     * <code>out</code>.  Called from Any.
     */
    final void read_value(final org.omg.CORBA.TypeCode tc,
                          final org.omg.CORBA.portable.OutputStream out)
    {
        if (tc == null)
        {
            throw new BAD_PARAM("TypeCode is null");
        }

        int kind = tc.kind().value();

        try
        {
            switch (kind)
            {
                case TCKind._tk_null:
                case TCKind._tk_void:
                break;
                case TCKind._tk_boolean:
                out.write_boolean( read_boolean());
                break;
                case TCKind._tk_char:
                out.write_char( read_char());
                break;
                case TCKind._tk_wchar:
                out.write_wchar( read_wchar());
                break;
                case TCKind._tk_octet:
                out.write_octet( read_octet());
                break;
                case TCKind._tk_ushort:
                out.write_ushort( read_ushort());
                break;
                case TCKind._tk_short:
                out.write_short( read_short());
                break;
                case TCKind._tk_long:
                out.write_long( read_long());
                break;
                case TCKind._tk_ulong:
                out.write_ulong( read_ulong());
                break;
                case TCKind._tk_float:
                out.write_float( read_float());
                break;
                case TCKind._tk_double:
                out.write_double( read_double());
                break;
                case TCKind._tk_longlong:
                out.write_longlong( read_longlong());
                break;
                case TCKind._tk_ulonglong:
                out.write_ulonglong( read_ulonglong());
                break;
                case TCKind._tk_any:
                out.write_any( read_any());
                break;
                case TCKind._tk_TypeCode:
                out.write_TypeCode( read_TypeCode());
                break;
                case TCKind._tk_Principal:
                throw new NO_IMPLEMENT ("Principal deprecated");
                case TCKind._tk_objref:
                out.write_Object( read_Object());
                break;
                case TCKind._tk_string:
                out.write_string( read_string());
                break;
                case TCKind._tk_wstring:
                out.write_wstring( read_wstring());
                break;
                case TCKind._tk_fixed:
                out.write_fixed (read_fixed());
                break;
                case TCKind._tk_array:
                {
                    int length = tc.length();
                    for( int i = 0; i < length; i++ )
                        read_value( tc.content_type(), out );
                    break;
                }
                case TCKind._tk_sequence:
                {
                    int len = read_long();
                    out.write_long(len);
                    for( int i = 0; i < len; i++ )
                        read_value( tc.content_type(), out );
                    break;
                }
                case TCKind._tk_except:
                out.write_string( read_string());
                // don't break, fall through to ...
                case TCKind._tk_struct:
                {
                    for( int i = 0; i < tc.member_count(); i++)
                        read_value( tc.member_type(i), out );
                    break;
                }
                case TCKind._tk_enum:
                out.write_long( read_long() );
                break;
                case TCKind._tk_alias:
                {
                    read_value( tc.content_type(), out  );
                    break;
                }
                case TCKind._tk_value_box:
                {
                    String id = tc.id();
                    org.omg.CORBA.portable.BoxedValueHelper helper =
                        ((org.jacorb.orb.ORB)orb).getBoxedValueHelper(id);
                    if (helper == null)
                    {
                        throw new MARSHAL ("No BoxedValueHelper for id " + id);
                    }
                    java.io.Serializable value = read_value(helper);
                    ((org.omg.CORBA_2_3.portable.OutputStream)out)
                    .write_value(value, helper);
                    break;
                }
                case TCKind._tk_union:
                {
                    org.omg.CORBA.TypeCode disc = tc.discriminator_type();
                    disc = TypeCode.originalType(disc);
                    int def_idx = tc.default_index();
                    int member_idx = -1;
                    switch( disc.kind().value() )
                    {
                        case TCKind._tk_short:
                        {
                            short s = read_short();
                            out.write_short(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_short())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }

                        case TCKind._tk_long:
                        {
                            int s = read_long();
                            out.write_long(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_long())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_ushort:
                        {
                            short s = read_ushort();
                            out.write_ushort(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_ushort())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }

                        case TCKind._tk_ulong:
                        {
                            int s = read_ulong();
                            out.write_ulong(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_ulong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_longlong:
                        {
                            long s = read_longlong();
                            out.write_longlong(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_longlong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_ulonglong:
                        {
                            long s = read_ulonglong();
                            out.write_ulonglong(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_ulonglong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_char:
                        {
                            char s = read_char();
                            out.write_char(s);
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == tc.member_label(i).extract_char())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_boolean:
                        {
                            boolean b = read_boolean();
                            out.write_boolean( b );
                            for(int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if( i != def_idx)
                                {
                                    if( b == tc.member_label(i).extract_boolean() )
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_enum:
                        {
                            int s = read_long();
                            out.write_long(s);
                            for( int i = 0 ; i < tc.member_count() ; i++)
                            {
                                if( i != def_idx)
                                {
                                    int label =
                                    tc.member_label(i).create_input_stream().read_long();
                                    if(s == label)
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        default:
                        throw new MARSHAL("Invalid union discriminator type: " + disc);
                    } // switch

                    if( member_idx != -1 )
                    {
                        read_value( tc.member_type( member_idx ), out );
                    }
                    else if( def_idx != -1 )
                    {
                        read_value( tc.member_type( def_idx ), out );
                    }
                    break;
                }
                case 0xffffffff:
                {
                    org.omg.CORBA.TypeCode _tc =
                        (org.omg.CORBA.TypeCode)(getRecursiveTCMap().get ( tc.id() ));


                    if( _tc == null )
                    {
                        throw new MARSHAL("No recursive TC found for " + tc.id());
                    }

                    read_value( _tc , out );
                    break;
                }
                default:
                throw new MARSHAL("Cannot handle TypeCode with kind " + kind);
            }
        }
        catch (BadKind ex)
        {
            throw new MARSHAL
                ("When processing TypeCode with kind: " + kind + " caught " + ex);
        }
        catch (Bounds ex)
        {
            throw new MARSHAL
                ("When processing TypeCode with kind: " + kind + " caught " + ex);
        }
    }


    public java.io.Serializable read_value()
    {
        int tag = read_long();
        int start_offset = pos - 4;

        if (tag == 0xffffffff)
        {
            // indirection
            return read_indirect_value();
        }
        else if (tag == 0x00000000)
        {
            // null tag
            return null;
        }

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;
        chunkedValue = ((tag & 8) != 0);

        int theTag = tag;
        tag = tag & 0xfffffff6;

        if (tag == 0x7fffff00)
            throw new MARSHAL ("missing value type information");
        else if (tag == 0x7fffff02)
        {
            return read_typed_value(start_offset, codebase);
        }
        else if (tag == 0x7fffff06)
        {
            return read_multi_typed_value( start_offset, codebase );
        }
        else
            throw new MARSHAL("unknown value tag: 0x" +
                              Integer.toHexString(theTag) + " (offset=0x" +
                              Integer.toHexString(start_offset) + ")");
    }

    /**
     * Overrides read_value(java.io.Serializable value) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value(final String rep_id)
    {
        int tag = read_long();
        int start_offset = pos - 4;

        if (tag == 0xffffffff)
        {
            // indirection
            return read_indirect_value();
        }
        else if (tag == 0x00000000)
        {
            // null tag
            return null;
        }

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;
        chunkedValue = ((tag & 8) != 0);

        int theTag = tag;
        tag = tag & 0xfffffff6;

        if (tag == 0x7fffff00)
        {
            return read_untyped_value ( new String[]{ rep_id }, start_offset, codebase);
        }
        else if (tag == 0x7fffff02)
        {
            return read_typed_value( start_offset, codebase );
        }
        else if (tag == 0x7fffff06)
        {
            return read_multi_typed_value( start_offset, codebase );
        }
        else
        {
            throw new MARSHAL("unknown value tag: 0x" +
                              Integer.toHexString(theTag) + " (offset=0x" +
                              Integer.toHexString(start_offset) + ")");
        }
    }

    /**
     * Unmarshals a valuetype instance from this stream.  The value returned
     * is the same value passed in, with all the data unmarshaled
     * (IDL-to-Java Mapping 1.2, August 2002, 1.13.1, p. 1-39).  The specified
     * value is an uninitialized value that is added to the ORB's indirection
     * table before unmarshaling (1.21.4.1, p. 1-117).
     *
     * This method is intended to be called from custom valuetype factories.
     * Unlike the other read_value() methods in this class, this method does
     * not expect a GIOP value tag nor a repository id in the stream.
     *
     * Overrides read_value(value) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value(java.io.Serializable value)
    {
        if (value instanceof org.omg.CORBA.portable.Streamable)
        {
            register_value(value);
            ((org.omg.CORBA.portable.Streamable)value)._read(this);
        }
        else if (value instanceof org.omg.CORBA.portable.CustomValue )
        {
            register_value(value);
            ( ( org.omg.CORBA.portable.CustomValue ) value ).unmarshal(
                    new DataInputStream( this ) );
        }
        else
        {
            throw new BAD_PARAM("read_value is only implemented for Streamables");
        }
        return value;
    }

    /**
     * Overrides read_value(clz) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value(final java.lang.Class clz)
    {
        int tag = read_long();
        int start_offset = pos - 4;

        if (tag == 0xffffffff)
        {
            // indirection
            return read_indirect_value();
        }
        else if (tag == 0x00000000)
        {
            // null tag
            return null;
        }

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;
        chunkedValue = ((tag & 8) != 0);

        int theTag = tag;
        tag = tag & 0xfffffff6;

        if (tag == 0x7fffff00)
        {
            return read_untyped_value ( new String[]{ ValueHandler.getRMIRepositoryID(clz) },
                                        start_offset, codebase);
        }
        else if (tag == 0x7fffff02)
        {
            return read_typed_value(start_offset, codebase);
        }
        else if (tag == 0x7fffff06)
        {
            return read_multi_typed_value(start_offset, codebase);
        }
        else
        {
            throw new MARSHAL("unknown value tag: 0x" +
                              Integer.toHexString(theTag) + " (offset=0x" +
                              Integer.toHexString(start_offset) + ")");
        }
    }

    /**
     * Overrides read_value(factory) in
     * org.omg.CORBA_2_3.portable.InputStream
     */


    public java.io.Serializable read_value
        (final org.omg.CORBA.portable.BoxedValueHelper factory)
    {
        int tag = read_long();
        int start_offset = pos - 4;

        if (tag == 0xffffffff)
        {
            // indirection
            return read_indirect_value();
        }
        else if (tag == 0x00000000)
        {
            // null tag, explicit representation of null value
            return null;
        }

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;
        chunkedValue = ((tag & 8) != 0);

        int theTag = tag;
        tag = tag & 0xfffffff6;

        if (tag == 0x7fffff00)
        {
            java.io.Serializable result = factory.read_value (this);

            if( result != null )
            {
                getValueMap().put (new Integer(start_offset), result);
            }

            return result;
        }
        else if (tag == 0x7fffff02)
        {
            // Read value according to type information.
            // Possible optimization: ignore type info and use factory for
            // reading the value anyway, since the type information is
            // most likely redundant.
            return read_typed_value(start_offset, codebase);
        }
        else
            throw new MARSHAL("unknown value tag: 0x" +
                              Integer.toHexString(theTag) + " (offset=0x" +
                              Integer.toHexString(start_offset) + ")");
    }

    /**
     * Immediately reads a value from this stream; i.e. without any
     * repository id preceding it.  The expected type of the value is given
     * by `repository_id', and the index at which the value started is
     * `index'.
     */
    private java.io.Serializable read_untyped_value(final String[] repository_ids,
                                                     final int index,
                                                     final String codebase)
    {
        java.io.Serializable result = null;

        if (chunkedValue || valueNestingLevel > 0)
        {
            valueNestingLevel++;
            int chunk_size_tag = read_long();
            chunk_end_pos = pos + chunk_size_tag;
        }

        for (int r = 0; r < repository_ids.length; r++)
        {
            if (repository_ids[r].equals("IDL:omg.org/CORBA/WStringValue:1.0"))
            {
                // special handling of strings, according to spec
                result = read_wstring();
                break;
            }
            else if (repository_ids[r].startsWith("RMI:javax.rmi.CORBA.ClassDesc:"))
            {
                // special handling of java.lang.Class instances
                String classCodebase = (String)read_value(String.class);
                String reposId = (String)read_value(String.class);
                String className =
                    org.jacorb.ir.RepositoryID.className(reposId, null);
                ClassLoader ctxcl =
                    Thread.currentThread().getContextClassLoader();

                try
                {
                    if (ctxcl != null)
                    {
                        try
                        {
                            result = ctxcl.loadClass(className);
                        }
                        catch (ClassNotFoundException cnfe)
                        {
                            result = ValueHandler.loadClass(className,
                                                            classCodebase,
                                                            null);
                        }
                    }
                    else
                    {
                        result = ValueHandler.loadClass(className,
                                                        classCodebase,
                                                        null);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    if( r < repository_ids.length-1 )
                        continue;
                    else
                        throw new MARSHAL("class not found: " + className);
                }
                break;
            }
            else if (repository_ids[r].startsWith ("IDL:"))
            {
                org.omg.CORBA.portable.ValueFactory factory =
                    ((org.omg.CORBA_2_3.ORB)orb()).lookup_value_factory (repository_ids[r]);

                if (factory != null)
                {
                    currentValueIndex = index;
                    result = factory.read_value (this);
                    break;
                }
                else
                {
                    if( r < repository_ids.length-1 )
                        continue;
                    else
                        throw new MARSHAL ("No factory found for: " + repository_ids[0] );
                }
            }
            else // RMI
            {
                // Load the value's class, using the context class loader
                // of the current thread if possible.  Here's Francisco
                // Reverbel's <reverbel@ime.usp.br> explanation of why
                // this is needed in JBoss:

                // "It seems that ValueHandler.loadClass() uses the thread
                // context classloader only after it looks for other
                // classloaders in the call stack (weird). In some
                // situations (when EJBs are undeployed and then
                // redeployed) it finds in the call stack a classloader
                // used for an undeployed EJB. A value of class Foo is
                // then unmarshalled with type
                // classloaderOfEJB1:Foo, when the expected type is
                // classloaderOfEJB2:Foo. I am getting ClassCastExceptions is this
                // situation.
                // Explicitly using the thread context class loader in the
                // first place solves the problem."

                String className =
                org.jacorb.ir.RepositoryID.className(repository_ids[r], null);

                Class c = null;
                //#ifjdk 1.2
                ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();
                //#else
                //# ClassLoader ctxcl = null;
                //#endif
                try
                {
                    if (ctxcl != null)
                    {
                        try
                        {
                            c = ctxcl.loadClass(className);
                        }
                        catch (ClassNotFoundException cnfe)
                        {
                            c = ValueHandler.loadClass(className, codebase, null);
                        }
                    }
                    else
                    {
                        c = ValueHandler.loadClass(className, codebase, null);
                    }

                    if (IDLEntity.class.isAssignableFrom(c))
                    {
                        java.lang.reflect.Method readMethod = null;
                        if (c != org.omg.CORBA.Any.class)
                        {
                            String helperClassName = c.getName() + "Helper";

                            try
                            {
                                Class helperClass =
                                c.getClassLoader().loadClass(
                                    helperClassName);
                                Class[] paramTypes = {
                                    org.omg.CORBA.portable.InputStream.class
                                };
                                readMethod =
                                helperClass.getMethod("read", paramTypes);
                            }
                            catch (ClassNotFoundException e)
                            {
                                throw new MARSHAL("Error loading class " + helperClassName
                                                  + ": " + e);
                            }
                            catch (NoSuchMethodException e)
                            {
                                throw new MARSHAL("No read method in helper class "
                                                  + helperClassName + ": " + e);
                            }
                        }

                        if (readMethod == null)
                        {
                            result = read_any();
                        }
                        else
                        {
                            try
                            {
                                result =
                                    (java.io.Serializable) readMethod.invoke(
                                        null,
                                        new java.lang.Object[] { this });
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new MARSHAL("Internal error: " + e);
                            }
                            catch (java.lang.reflect.InvocationTargetException e)
                            {
                                throw new MARSHAL("Exception unmarshaling IDLEntity: "
                                                  + e.getTargetException());
                            }
                        }
                    }
                    else
                        result = ValueHandler.readValue(this, index, c,
                                                        repository_ids[r],
                                                        null);
                }
                catch (ClassNotFoundException e)
                {
                    if( r < repository_ids.length-1 )
                        continue;
                    else
                        throw new MARSHAL ("class not found: " + className);
                }

            }
        }

        // value type instances may be null...
        if( result != null )
        {
            getValueMap().put (new Integer (index), result);
        }

        return result;
    }

    /**
     * Reads a value with type information, i.e. one that is preceded
     * by a single RepositoryID.  It is assumed that the tag and the codebase
     * of the value have already been read.
     */

    private java.io.Serializable read_typed_value( final int index,
                                                   final String codebase)
    {
        return read_untyped_value ( new String[]{ read_repository_id() }, index, codebase);
    }

    /**
     * Reads a value with type information, i.e. one that is preceded
     * by an array of RepositoryIDs.  It is assumed that the tag and the codebase
     * of the value have already been read.
     */

    private java.io.Serializable read_multi_typed_value( final int index,
                                                         final String codebase)
    {
        int id_count = read_long();
        String[] ids = new String[ id_count ];

        for( int i = 0; i < id_count; i++ )
            ids[i] = read_repository_id();

        return read_untyped_value (ids, index, codebase);
    }


    /**
     * Reads a RepositoryID from the buffer, either directly or via
     * indirection.
     */
    private String read_repository_id()
    {
        int tag = read_long();
        if (tag == 0xffffffff)
        {
            // indirection
            int index = read_long();
            index = index + pos - 4;

            String repId = (String)getRepIdMap().get (new Integer(index));
            if (repId == null)
                throw new MARSHAL("stale RepositoryID indirection");
            else
                return repId;
        }
        else
        {
            // a new id
            pos -= 4;
            index -= 4;
            int start_offset = pos;
            String repId = read_string();

            getRepIdMap().put (new Integer(start_offset), repId);
            return repId;
        }
    }

    /**
     * Reads a codebase from the buffer, either directly or via
     * indirection.
     */
    private String read_codebase()
    {
        int tag = read_long();

        if (tag == 0xffffffff)
        {
            // indirection
            int index = read_long();
            index = index + pos - 4;
            String codebase = (String)getCodebaseMap().get (new Integer(index));
            if (codebase == null)
                throw
                new MARSHAL("stale codebase indirection");
            else
                return codebase;
        }
        else
        {
            // a new codebase string
            pos -= 4;
            index -= 4;
            int start_offset = pos;
            String codebase = read_string();
            getCodebaseMap().put (new Integer(start_offset), codebase);
            return codebase;
        }
    }

    /**
     * Reads an indirect value from this stream. It is assumed that the
     * value tag (0xffffffff) has already been read.
     */
    private java.io.Serializable read_indirect_value()
    {
        // indirection
        int index = read_long();
        index = index + pos - 4;
        java.lang.Object value = getValueMap().get (new Integer(index));
        if (value == null) {

            // Java to IDL Language Mapping, v1.1, page 1-44:
            //
            // "The ValueHandler object may receive an IndirectionException
            // from the ORB stream. The ORB input stream throws this exception
            // when it is called to unmarshal a value encoded as an indirection
            // that is in the process of being unmarshaled. This can occur when
            // the ORB stream calls the ValueHandler object to unmarshal an RMI
            // value whose state contains a recursive reference to itself.
            // Because the top-level ValueHandler.readValue call has not yet
            // returned a value, the ORB stream's indirection table contains no
            // entry for an object with the stream offset specified by the
            // indirection tag. This stream offset is returned in the
            // exception's offset field."

            throw new org.omg.CORBA.portable.IndirectionException (index);
        }
        else
            return (java.io.Serializable)value;
    }

    private String validateName (String name)
    {
        if (name != null && name.length() == 0)
        {
            return null;
        }
        return name;
    }


    private String validateMember (String name)
    {
        if (name == null || name.length() == 0)
        {
            uniqueValue = (++uniqueValue)%Integer.MAX_VALUE;
            return ("DUMMY_NAME_".concat (String.valueOf (uniqueValue)));
        }
        return name;
    }


    private String validateID (String id)
    {
        if (id == null || id.length() == 0)
        {
            id = "IDL:";
        }
        return id;
    }


    /**
     * Reads an abstract interface from this stream. The abstract interface
     * Reads an abstract interface from this stream. The abstract interface
     * appears as a union with a boolean discriminator, which is true if the
     * union contains a CORBA object reference, or false if the union contains
     * a value.
     */

    public java.lang.Object read_abstract_interface()
    {
        return read_boolean() ? (java.lang.Object)read_Object()
        : (java.lang.Object)read_value();
    }

    /**
     * Reads an abstract interface from this stream. The abstract interface
     * appears as a union with a boolean discriminator, which is true if the
     * union contains a CORBA object reference, or false if the union contains
     * a value.
     */

    public java.lang.Object read_abstract_interface(final java.lang.Class clz)
    {
        return read_boolean() ? (java.lang.Object)read_Object(clz)
        : (java.lang.Object)read_value(clz);
    }


    public int get_pos()
    {
        return pos;
    }

    /**
     * Stores `value' into this stream's valueMap.  This is provided
     * as a callback for value factories, so that a value factory can
     * store an object into the map before actually reading its state.
     * This is essential for unmarshalling recursive values.
     */

    public void register_value(final java.io.Serializable value)
    {
        getValueMap().put(new Integer(currentValueIndex), value);
    }
}
