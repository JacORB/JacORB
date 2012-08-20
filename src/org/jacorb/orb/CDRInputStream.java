package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.typecode.DelegatingTypeCodeReader;
import org.jacorb.orb.typecode.TypeCodeCache;
import org.jacorb.util.ObjectUtil;
import org.jacorb.util.Stack;
import org.jacorb.util.ValueHandler;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.ValueInputStream;
import org.omg.GIOP.MsgType_1_1;
import org.slf4j.Logger;

/**
 * Read CDR encoded data
 *
 * @author Gerald Brose, FU Berlin
 */

public class CDRInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
    implements CodeSet.InputBuffer, ValueInputStream
{
    /**
     * <code>encaps_stack</code> is used to saving/restoring
     * encapsulation information.
     */
    private Stack encaps_stack;

    /**
     * This Map is basically a one-entry map pool to be used in
     * read_TypeCode() as the repeatedTCMap.
     */
    private SortedMap repeatedTCMap;

    private Map recursiveTCMap;

    /** indexes to support mark/reset */
    private int marked_pos;
    private int marked_index;
    private int marked_chunk_end_pos;
    private int marked_valueNestingLevel;

    private boolean closed;

    /** configurable properties */
    Logger logger;
    private boolean cometInteropFix;
    private boolean laxBooleanEncoding;
    private boolean nullStringEncoding;

    /* character encoding code sets for char and wchar, default ISO8859_1 */
    private CodeSet codeSet =  CodeSet.getTCSDefault();
    private CodeSet codeSetW = CodeSet.getTCSWDefault();
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

    private boolean littleEndian = false;

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
     * <code>mutator</code> is a pluggable IOR mutator.
     */
    private IORMutator mutator;

    private boolean isMutatorEnabled;

    /**
     * <code>codesetEnabled</code> denotes whether codeset marshalling is enabled.
     */
    private boolean codesetEnabled;

    /**
     * for this stream to be able to return a live object reference, a
     * full ORB (not the Singleton!) must be known. If this stream is
     * used only to demarshal base type data, the Singleton is enough
     */
    private final org.omg.CORBA.ORB orb;

    /**
     * this is the lowest possible value_tag indicating the
     * begin of a valuetype (15.3.4)
     */
    private static final int MAX_BLOCK_SIZE = 0x7fffff00;

    /**
     * fixes RMI/IIOP related interoperability issues with the
     * sun the orb that occured
     * while receiving serialized collections.
     * see <a href="http://lists.spline.inf.fu-berlin.de/mailman/htdig/jacorb-developer/2006-May/008251.html">mailing list</a>
     * for details.
     */
    private boolean sunInteropFix;

    private static final DelegatingTypeCodeReader typeCodeReader = new DelegatingTypeCodeReader();

    private final TypeCodeCache typeCodeCache;

    private int typeCodeNestingLevel = -1;


    private CDRInputStream(org.omg.CORBA.ORB orb)
    {
        super();

        if (orb == null)
        {
            this.orb = org.omg.CORBA.ORBSingleton.init();
        }
        else
        {
            this.orb = orb;
        }

        if (! (this.orb instanceof ORBSingleton))
        {
            throw new BAD_PARAM("don't pass in a non JacORB ORB");
        }

        try
        {
            configure(((ORBSingleton)this.orb).getConfiguration());
        }
        catch( ConfigurationException e )
        {
            throw new INTERNAL("ConfigurationException: " + e);
        }

        typeCodeCache = ((ORBSingleton)this.orb).getTypeCodeCache();
    }


    private CDRInputStream(org.omg.CORBA.ORB orb, byte[] buffer, Object ignored)
    {
        this(orb);

        if (buffer == null)
        {
            throw new IllegalArgumentException();
        }

        this.buffer = buffer;
    }

    public CDRInputStream(byte[] buffer)
    {
        this(null, buffer, null);
    }

    public CDRInputStream(final org.omg.CORBA.ORB orb, final byte[] buf)
    {
        this(orb, buf, null);

        if (orb == null)
        {
            throw new BAD_PARAM("don't pass in a null ORB");
        }
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
        logger = configuration.getLogger("jacorb.orb.cdr");

        codesetEnabled  = configuration.getAttributeAsBoolean ("jacorb.codeset", false);
        cometInteropFix = configuration.getAttributeAsBoolean ("jacorb.interop.comet",false);
        laxBooleanEncoding = configuration.getAttributeAsBoolean("jacorb.interop.lax_boolean_encoding", false);
        sunInteropFix = configuration.getAttributeAsBoolean("jacorb.interop.sun", false);
        nullStringEncoding = configuration.getAttributeAsBoolean("jacorb.interop.null_string_encoding", false);

        mutator = (IORMutator) configuration.getAttributeAsObject("jacorb.iormutator");
        isMutatorEnabled = (mutator != null);
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

    public void setGIOPMinor(final  int giop_minor )
    {
        this.giop_minor = giop_minor;
    }

    public int getGIOPMinor()
    {
        return giop_minor;
    }

    public void close()
    {
        // Don't need to call super.close as super is noop.

        if( closed )
        {
            return;
        }

        // commented out as this caused test failures.
        // as the buffer has been passed into this CDRInputStream
        // we cannot assume ownership of the buffer here (alphonse).
        // BufferManager.getInstance().returnBuffer(buffer);

        buffer = null;
        encaps_stack = null;
        closed = true;

        if (recursiveTCMap != null)
        {
            recursiveTCMap.clear();
        }
    }

    public org.omg.CORBA.ORB orb()
    {
        return orb;
    }

    public void setCodeSet( CodeSet codeSet, CodeSet codeSetWide )
    {
        this.codeSet  = codeSet;
        this.codeSetW = codeSetWide;
    }

    private static final int _read4int
       (final boolean _littleEndian, final byte[] _buffer, final int _pos)
    {
        if (_littleEndian)
        {
            return (((_buffer[_pos+3] & 0xff) << 24) +
                    ((_buffer[_pos+2] & 0xff) << 16) +
                    ((_buffer[_pos+1] & 0xff) <<  8) +
                    ((_buffer[_pos]   & 0xff) <<  0));
        }

        return (((_buffer[_pos]   & 0xff) << 24) +
                ((_buffer[_pos+1] & 0xff) << 16) +
                ((_buffer[_pos+2] & 0xff) <<  8) +
                ((_buffer[_pos+3] & 0xff) <<  0));
    }

    private static final short _read2int
       (final boolean _littleEndian, final byte[] _buffer, final int _pos)
    {
        if (_littleEndian)
        {
            return  (short)(((_buffer[_pos+1] & 0xff) << 8) +
                            ((_buffer[_pos]   & 0xff) << 0));
        }
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
            return (_read_long() & 0xFFFFFFFFL) + ((long) _read_long() << 32);
        }

        return ((long) _read_long() << 32) + (_read_long() & 0xFFFFFFFFL);
    }

    private final void handle_chunking()
    {
        handle_chunking (true);
    }

    private final void handle_chunking(boolean align)
    {
        if (align)
        {
            int remainder = 4 - (index % 4);
            int aligned_pos = (remainder != 4) ? pos + remainder : pos;
            align = ( chunk_end_pos >= pos && chunk_end_pos <= aligned_pos );
        }
        else
        {
            align = ( chunk_end_pos == pos );
        }

        if ( align )
        {
            chunk_end_pos = -1;
            int saved_pos = pos;
            int saved_index = index;
            int tag = read_long();

            if (tag < 0) {

                // tag is an end tag

                if (-tag > valueNestingLevel)
                {
                    throw new INTERNAL
                    (
                        "received end tag " + tag +
                        " with value nesting level " +
                        valueNestingLevel
                    );
                }
                valueNestingLevel = -tag;
                valueNestingLevel--;

                if (valueNestingLevel > 0)
                {
                    chunk_end_pos = pos;
                    handle_chunking();
                }
            }
            else if (tag > 0 && tag < 0x7fffff00)
            {
                // tag is the chunk size tag of another chunk

                chunk_end_pos = pos + tag;
            }
            else // (tag == 0 || tag >= 0x7fffff00)
            {
                // tag is the null value tag or the value tag of a nested value

                pos = saved_pos;      // "unread" the tag
                index = saved_index;
            }
        }
    }

    public final void skip(final int distance)
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
        {
            pos = start + size;
        }

        index = ei.index + size;
    }

    /**
     * open a CDR encapsulation and
     * restore index and byte order information
     */

    public final int openEncapsulation()
    {
        boolean old_endian = littleEndian;
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
                logger.debug("Size of CDR encapsulation larger than buffer, swapping byte order. " +
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
     * Return a copy of the current buffer.
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
        {
            throw new java.io.IOException("Stream already closed!");
        }

        if( available() < 1 )
        {
            return -1;
        }

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
     * @see #read(byte[], int, int)
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
        handle_chunking(false);
        index++;
        byte value = buffer[pos++];

        return parseBoolean(value);
    }

    /** arrays */

    public final void read_boolean_array
       (final boolean[] value, final int offset, final int length)
    {
        handle_chunking();

        if (length == 0)
        {
            return;
        }

        final int until = offset + length;

        int j = offset;
        do
        {
            final byte bb = buffer[pos++];
            value[j] = parseBoolean(bb);
            ++j;
        }
        while(j < until);

        index += length;
    }

    private final boolean parseBoolean(final byte value)
    {
        if (value == 0)
        {
            return false;
        }
        else
        {
            if (value == 1)
            {
                return true;
            }
            else if (laxBooleanEncoding)
            {
                // Technically only valid values are 0 (false) and 1 (true)
                // however some ORBs send values other than 1 for true.
                return true;
            }
            else
            {
                throw new MARSHAL ("Unexpected boolean value: " + value
                        + " pos: " + pos);
            }
        }
    }


    /**
     * <code>read_char</code> reads a character from the stream.
     *
     * @return a <code>char</code> value
     */
    public final char read_char()
    {
        handle_chunking(false);

        index++;
        return (char)(buffer[pos++] & 0xFF);
    }


    /**
     * <code>read_char_array</code> reads an character array from the stream.
     *
     * @param value a <code>char[]</code>, the result array.
     * @param offset an <code>int</code>, an offset into <code>value</code>
     * @param length an <code>int</code>, the length of the array to read
     */
    public final void read_char_array
        (final char[] value, final int offset, final int length)
    {
        if (value == null)
        {
            throw new MARSHAL("Cannot marshal result into null array.");
        }
        else if ( offset + length > value.length || length < 0 || offset < 0 )
        {
            throw new MARSHAL
                ("Cannot marshal as indices for array are out bounds.");
        }

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
        {
            return;
        }

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

    /**
     * @deprecated use {@link #read_fixed(short, short)} instead
     */
    public BigDecimal read_fixed()
    {
        handle_chunking();

        final StringBuffer sb = new StringBuffer();

        final int signum = read_fixed_internal(sb, (short)-1);

        final BigDecimal result = new BigDecimal( new BigInteger( sb.toString()));

        return read_fixed_negate(signum, result);
    }

    public BigDecimal read_fixed(short digits, short scale)
     {
        if (digits < 1)
        {
            throw new BAD_PARAM("digits must be a positive value: " + digits + ".");
        }

        if (scale < 0)
        {
            throw new BAD_PARAM("scale must be a non-negative value: " + scale +".");
        }

        if (scale > digits)
        {
            throw new BAD_PARAM("scale factor " + scale + " must be less than or equal to the total number of digits " + digits + ".");
        }

        handle_chunking();

        final StringBuffer sb = new StringBuffer();

        final int c = read_fixed_internal(sb, digits);

        final BigDecimal result = new BigDecimal( new BigInteger( sb.toString()), scale);

        return read_fixed_negate(c, result);
    }

    private BigDecimal read_fixed_negate(final int signum, final BigDecimal result)
    {
        if( signum == 0xD )
        {
            return result.negate();
        }

        return result;
    }

    /**
     * @param outBuffer a string representation of the read in fixed will be appended to the buffer.
     * @param digits the number of expected digits the read in fixed should have. -1 means unlimited.
     * @return the signum of the read in fixed (0xC or 0xD).
     */
    private int read_fixed_internal(StringBuffer outBuffer, short digits)
    {
        int b = buffer[pos++];
        int c = b & 0x0F; // second half byte
        index++;

        while(true)
        {
            c = (b & 0xF0) >>> 4;

            if (outBuffer.length() > 0 || c != 0)
            {
                outBuffer.append(c);
            }

            c = b & 0x0F;
            if( c == 0xC || c == 0xD )
            {
                break;
            }
            outBuffer.append(c);

            b = buffer[pos++];
            index++;
        }

        if (digits != -1 && (outBuffer.length() > digits))
        {
            throw new MARSHAL("unexpected number of digits: expected " + digits + " got " + outBuffer.length() + " " + outBuffer);
        }

        return c;
    }

    public final float read_float()
    {
        return Float.intBitsToFloat (read_long());
    }

    public final void read_float_array
       (final float[] value, final int offset, final int length)
    {
        if (length == 0)
        {
            return;
        }

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
        {
            return;
        }

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

        return _read_longlong();
    }

    public final void read_longlong_array
        (final long[] value, final int offset, final int length)
    {
        if (length == 0)
        {
            return;
        }

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
                value[j] = ( _read_long() & 0xFFFFFFFFL) +
                    ((long) _read_long() << 32);
            }
        }
        else
        {
            for(int j=offset; j < offset+length; j++)
            {
                value[j] = ((long) _read_long() << 32) +
                    (_read_long() & 0xFFFFFFFFL);
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

        if (isMutatorEnabled)
        {
            ior = mutator.mutateIncoming (ior);
        }

        if (ParsedIOR.isNull(ior))
        {
            return null;
        }
        else
        {
            return ((org.jacorb.orb.ORB)orb)._getObject(ior);
        }
    }

    public org.omg.CORBA.Object read_Object(final java.lang.Class clazz)
    {
        if (org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(clazz))
        {
            org.omg.CORBA.Object obj = read_Object();
            if (obj instanceof org.omg.CORBA.portable.ObjectImpl)
            {
                org.omg.CORBA.portable.ObjectImpl stub = null;
                try
                {
                    stub = (org.omg.CORBA.portable.ObjectImpl)clazz.newInstance();
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
            return obj;
        }
        else if (clazz.isInterface() &&
                 java.rmi.Remote.class.isAssignableFrom(clazz))
        {
            return (org.omg.CORBA.Object)
                org.jacorb.util.ValueHandler.portableRemoteObject_narrow(
                                                           read_Object(), clazz);
        }
        else
        {
            return read_Object();
        }
    }

    public final byte read_octet()
    {
        handle_chunking(false);
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

    /*
     * @deprecated
     * @see org.omg.CORBA.portable.InputStream#read_Principal()
     */
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
        {
            return;
        }

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


    /**
     * <code>read_string</code> reads a string from the buffer. It is optimized
     * for whether it is reading a blank string, and whether codeset translation
     * is active.
     *
     * @return a <code>String</code> value, possibly blank, never null.
     */
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

        if (size < 1 && ! nullStringEncoding)
        {
            throw new MARSHAL("invalid string size: " + size);
        }

        int start = pos + 4;

        index += (size + 4);
        pos += (size + 4);

        final int stringTerminatorPosition = start + size -1;

        if (nullStringEncoding && size == 0)
        {
            // Some ORBs wrongly encode empty string with a size 0
            return null;
        }
        else if (buffer.length < stringTerminatorPosition + 1)
        {
            throw new MARSHAL("buffer too small");
        }

        if ((buffer[stringTerminatorPosition] == 0))
        {
            size --;
        }
        else
        {
            throw new MARSHAL("unexpected string terminator value " + Integer.toHexString(buffer[stringTerminatorPosition]) + " at buffer index " + stringTerminatorPosition);
        }

        // Optimize for empty strings.
        if (size == 0)
        {
            return "";
        }

        if(start + size > buffer.length)
        {
            final String message = "Size (" + size + ") invalid for string extraction from buffer length of " + buffer.length + " from position " + start;
            if (logger.isDebugEnabled())
            {
                logger.debug(message);
            }
            throw new MARSHAL(message);
        }

        if (codesetEnabled)
        {

            try
            {
                result = new String (buffer, start, size, codeSet.getName() );
            }
            catch (java.io.UnsupportedEncodingException ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("Charset " + codeSet.getName() + " is unsupported");
                    result = "";
                }
            }
        }
        else
        {
            char[] buf = new char[size];

            for (int i=0; i<size; i++)
            {
                buf[i] = (char)(0xff & buffer[start + i]);
            }
            result = new String(buf);
        }

        return result;
    }


    public final org.omg.CORBA.TypeCode read_TypeCode()
    {
        if (recursiveTCMap == null)
        {
            recursiveTCMap = new HashMap();
        }

        if (repeatedTCMap == null)
        {
            repeatedTCMap = new TreeMap();
        }

        try
        {
            return read_TypeCode(recursiveTCMap, repeatedTCMap);
        }
        finally
        {
            recursiveTCMap.clear();
            repeatedTCMap.clear();
        }
    }

    public final org.omg.CORBA.TypeCode read_TypeCode(Map recursiveTCMap, Map repeatedTCMap)
    {
        try
        {
            ++typeCodeNestingLevel;
            return typeCodeReader.readTypeCode(logger, this, recursiveTCMap, repeatedTCMap);
        }
        finally
        {
            --typeCodeNestingLevel;
        }
    }

    /**
     * Skip amount is
     * skip (size - ((pos - start_pos) - 4 - 4));
     * EncapsulationSize -
     * ( PositionAfterReadingId - start_pos
     *   - 4 [Size] - 4 [KindSize] ) = RemainingSizeToSkip
     * @param start_pos
     * @param size
     */
    public void skipRemainingTypeCode(final Integer start_pos, final int size)
    {
        skip (size - ((pos - start_pos.intValue()) - 4 - 4));
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
        {
            return;
        }

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
            return (_read_long() & 0xFFFFFFFFL) + ((long) _read_long() << 32);
        }

        return ((long) _read_long() << 32) + (_read_long() & 0xFFFFFFFFL);
    }

    public final void read_ulonglong_array
        (final long[] value, final int offset, final int length)
    {
        if (length == 0)
        {
            return;
        }

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
                value[j] = ( _read_long() & 0xFFFFFFFFL) +
                    ((long) _read_long() << 32);
            }
        }
        else
        {
            for (int j = offset; j < offset+length; j++)
            {
                value[j] = ((long) _read_long() << 32) +
                    (_read_long() & 0xFFFFFFFFL);
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
        {
            return;
        }

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
        if (giop_minor == 0)
        {
            final int minor = Messages.getMsgType( buffer ) == MsgType_1_1._Reply ? 6 : 5;
            throw new MARSHAL("GIOP 1.0 does not support the type wchar", minor, CompletionStatus.COMPLETED_NO);
        }

        handle_chunking();

        if (giop_minor == 2)
        {
            //ignore size indicator
            read_wchar_size();

            boolean wchar_little_endian = readBOM();

            return read_wchar (wchar_little_endian);
        }

        return read_wchar (littleEndian);
    }


    public byte readByte()
    {
        index++;
        return buffer[ pos++ ];
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
        return codeSetW.read_wchar( this, giop_minor, wchar_little_endian );
    }

    /**
     * Read the byte order marker indicating the endianess.
     *
     * @return true for little endianess, false otherwise (including
     * no BOM present. In this case, big endianess is assumed per
     * spec).
     */
    public final boolean readBOM()
    {
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
        {
            value[j] = read_wchar(); // inlining later...
        }
    }

    public final String read_wstring()
    {
        if (giop_minor == 0)
        {
            final int minor = Messages.getMsgType( buffer ) == MsgType_1_1._Reply ? 6 : 5;
            throw new MARSHAL("GIOP 1.0 does not support the IDL type wstring", minor, CompletionStatus.COMPLETED_NO);
        }

        handle_chunking();

        int remainder = 4 - (index % 4);
        if( remainder != 4 )
        {
            index += remainder;
            pos += remainder;
        }

        // read length indicator
        int size = _read4int( littleEndian, buffer, pos);
        index += 4;
        pos += 4;
        if (size == 0) return "";

        return codeSetW.read_wstring( this, size, this.giop_minor, this.littleEndian );
    }


    public boolean markSupported()
    {
        return true;
    }

    public void mark(final int readLimit)
    {
        marked_pos = pos;
        marked_index = index;
        marked_chunk_end_pos = chunk_end_pos;
        marked_valueNestingLevel = valueNestingLevel;
    }

    public void reset()
        throws IOException
    {
        if( pos < 0 )
        {
            throw new MARSHAL("Mark has not been set!");
        }
        pos = marked_pos;
        index = marked_index;
        chunk_end_pos = marked_chunk_end_pos;
        valueNestingLevel = marked_valueNestingLevel;
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


    protected boolean getLittleEndian ()
    {
       return littleEndian;
    }

    /**
     * Reads an instance of the type described by type code <code>tc</code>
     * from this CDRInputStream, and remarshals it to the given OutputStream,
     * <code>out</code>.  Called from Any.
     */
    final void read_value(final org.omg.CORBA.TypeCode typeCode,
                          final org.omg.CORBA.portable.OutputStream out)
    {
        if (typeCode == null)
        {
            throw new BAD_PARAM("TypeCode is null");
        }

        int kind = typeCode.kind().value();

        try
        {
            switch (kind)
            {
                case TCKind._tk_null:       // 0
                    // fallthrough
                case TCKind._tk_void:       // 1
                {
                    break;
                }
                case TCKind._tk_short:      // 2
                {
                    out.write_short( read_short());
                    break;
                }
                case TCKind._tk_long:       // 3
                {
                    out.write_long( read_long());
                    break;
                }
                case TCKind._tk_ushort:     // 4
                {
                    out.write_ushort( read_ushort());
                    break;
                }
                case TCKind._tk_ulong:      // 5
                {
                    out.write_ulong( read_ulong());
                    break;
                }
                case TCKind._tk_float:      // 6
                {
                    out.write_float( read_float());
                    break;
                }
                case TCKind._tk_double:     // 7
                {
                    out.write_double( read_double());
                    break;
                }
                case TCKind._tk_boolean:    // 8
                {
                    out.write_boolean( read_boolean());
                    break;
                }
                case TCKind._tk_char:       // 9
                {
                    out.write_char( read_char());
                    break;
                }
                case TCKind._tk_octet:      // 10
                {
                    out.write_octet( read_octet());
                    break;
                }
                case TCKind._tk_any:        // 11
                {
                    out.write_any( read_any());
                    break;
                }
                case TCKind._tk_TypeCode:   // 12
                {
                    out.write_TypeCode( read_TypeCode());
                    break;
                }
                case TCKind._tk_Principal:  // 13
                {
                    throw new NO_IMPLEMENT ("Principal deprecated");
                }
                case TCKind._tk_objref:     // 14
                {
                    out.write_Object( read_Object());
                    break;
                }
                case TCKind._tk_struct:     // 15
                {
                    for( int i = 0; i < typeCode.member_count(); i++)
                    {
                        read_value( typeCode.member_type(i), out );
                    }
                    break;
                }
                case TCKind._tk_union:      // 16
                {
                    org.omg.CORBA.TypeCode disc = typeCode.discriminator_type();
                    disc = TypeCode.originalType(disc);
                    int def_idx = typeCode.default_index();
                    int member_idx = -1;
                    switch( disc.kind().value() )
                    {
                        case TCKind._tk_short:  // 2
                        {
                            short s = read_short();
                            out.write_short(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_short())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }

                        case TCKind._tk_long:   // 3
                        {
                            int s = read_long();
                            out.write_long(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_long())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_ushort: // 4
                        {
                            short s = read_ushort();
                            out.write_ushort(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_ushort())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }

                        case TCKind._tk_ulong:  // 5
                        {
                            int s = read_ulong();
                            out.write_ulong(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_ulong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_float:      // 6
                            // fallthrough
                        case TCKind._tk_double:     // 7
                        {
                            throw new MARSHAL(
                                "Invalid union discriminator type: " + disc);
                        }
                        case TCKind._tk_boolean:    // 8
                        {
                            boolean b = read_boolean();
                            out.write_boolean( b );
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if( i != def_idx)
                                {
                                    if( b == typeCode.member_label(i).extract_boolean() )
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_char:   // 9
                        {
                            char s = read_char();
                            out.write_char(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_char())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_octet:      // 10
                            // fallthrough
                        case TCKind._tk_any:        // 11
                            // fallthrough
                        case TCKind._tk_TypeCode:   // 12
                            // fallthrough
                        case TCKind._tk_Principal:  // 13
                            // fallthrough
                        case TCKind._tk_objref:     // 14
                            // fallthrough
                        case TCKind._tk_struct:     // 15
                            // fallthrough
                        case TCKind._tk_union:      // 16
                        {
                            throw new MARSHAL(
                                "Invalid union discriminator type: " + disc);
                        }
                        case TCKind._tk_enum:       // 17
                        {
                            int s = read_long();
                            out.write_long(s);
                            for( int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if( i != def_idx)
                                {
                                    int label =
                                    typeCode.member_label(i).create_input_stream().read_long();
                                    if(s == label)
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_string:     // 18
                            // fallthrough
                        case TCKind._tk_sequence:   // 19
                            // fallthrough
                        case TCKind._tk_array:      // 20
                            // fallthrough
                        case TCKind._tk_alias:      // 21
                            // fallthrough
                        case TCKind._tk_except:     // 22
                        {
                            throw new MARSHAL(
                                "Invalid union discriminator type: " + disc);
                        }
                        case TCKind._tk_longlong:  // 23
                        {
                            long s = read_longlong();
                            out.write_longlong(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_longlong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_ulonglong:  // 24
                        {
                            long s = read_ulonglong();
                            out.write_ulonglong(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_ulonglong())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        default:
                        {
                            throw new MARSHAL("Invalid union discriminator type: " + disc);
                        }
                    } // switch

                    if( member_idx != -1 )
                    {
                        read_value( typeCode.member_type( member_idx ), out );
                    }
                    else if( def_idx != -1 )
                    {
                        read_value( typeCode.member_type( def_idx ), out );
                    }
                    break;
                }
                case TCKind._tk_enum:       // 17
                {
                    out.write_long( read_long() );
                    break;
                }
                case TCKind._tk_string:     // 18
                {
                    out.write_string( read_string());
                    break;
                }
                case TCKind._tk_sequence:   // 19
                {
                    int len = read_long();
                    out.write_long(len);
                    for( int i = 0; i < len; i++ )
                    {
                        read_value( typeCode.content_type(), out );
                    }
                    break;
                }
                case TCKind._tk_array:      // 20
                {
                    int length = typeCode.length();
                    for( int i = 0; i < length; i++ )
                    {
                        read_value( typeCode.content_type(), out );
                    }
                    break;
                }
                case TCKind._tk_alias:      // 21
                {
                    read_value( typeCode.content_type(), out  );
                    break;
                }
                case TCKind._tk_except:     // 22
                {
                    out.write_string( read_string());

                    for( int i = 0; i < typeCode.member_count(); i++)
                    {
                        read_value( typeCode.member_type(i), out );
                    }

                    break;
                }
                case TCKind._tk_longlong:   // 23
                {
                    out.write_longlong( read_longlong());
                    break;
                }
                case TCKind._tk_ulonglong:  // 24
                {
                    out.write_ulonglong( read_ulonglong());
                    break;
                }
                case TCKind._tk_longdouble: // 25
                {
                    throw new org.omg.CORBA.BAD_TYPECODE(
                        "type longdouble not supported in java");
                }
                case TCKind._tk_wchar:      // 26
                {
                    out.write_wchar( read_wchar());
                    break;
                }
                case TCKind._tk_wstring:    // 27
                {
                    out.write_wstring( read_wstring());
                    break;
                }
                case TCKind._tk_fixed:      // 28
                {
                    final short digits = typeCode.fixed_digits();
                    final short scale = typeCode.fixed_scale();
                    final BigDecimal value = read_fixed(digits, scale);

                    if (out instanceof CDROutputStream)
                    {
                        CDROutputStream cdrOut = (CDROutputStream) out;
                        cdrOut.write_fixed(value, digits, scale);
                    }
                    else
                    {
                        // TODO can we remove this? mixed usage orb classes from different vendors ...
                        out.write_fixed (value);
                    }
                    break;
                }
                case TCKind._tk_value:      // 29
                {
                    Serializable val = read_value();
                    ((org.omg.CORBA_2_3.portable.OutputStream)out).write_value(val, typeCode.id());
                    break;
                }
                case TCKind._tk_value_box:  // 30
                {
                    String id = typeCode.id();
                    org.omg.CORBA.portable.BoxedValueHelper helper =
                        ((org.jacorb.orb.ORB)orb).getBoxedValueHelper(id);
                    if (helper == null)
                    {
                        throw new MARSHAL ("No BoxedValueHelper for id " + id);
                    }
                    java.io.Serializable value = read_value(helper);
                    ((org.omg.CORBA_2_3.portable.OutputStream)out).write_value(value, helper);
                    break;
                }
                default:
                {
                    throw new MARSHAL("Cannot handle TypeCode with kind " + kind);
                }
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
        {
            throw new MARSHAL ("missing value type information");
        }
        else if (tag == 0x7fffff02)
        {
            return read_typed_value(start_offset, codebase);
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
     * Overrides read_value(java.io.Serializable value) in
     * org.omg.CORBA_2_3.portable.InputStream
     */
    public java.io.Serializable read_value(final String rep_id)
    {
        int tag = read_long();
        final int start_offset = pos - 4;

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

        final String codebase = ((tag & 1) != 0) ? read_codebase() : null;
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
            return read_untyped_value (new String[]{ValueHandler.getRMIRepositoryID(clz)},
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
                              Integer.toHexString(theTag) +
                              " (offset=0x" +
                              Integer.toHexString(start_offset) +
                              ")");
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
                getValueMap().put(Integer.valueOf(start_offset), result);
            }

            return result;
        }
        else if (tag == 0x7fffff02)
        {
            final Serializable result = read_typed_value(start_offset, codebase, factory);

            if (result != null)
            {
                getValueMap().put(Integer.valueOf(start_offset), result);
            }

            return result;
        }
        else
        {
            throw new MARSHAL("unknown value tag: 0x" +
                              Integer.toHexString(theTag) + " (offset=0x" +
                              Integer.toHexString(start_offset) + ")");
        }
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

        if (chunkedValue || (valueNestingLevel > 0 && !sunInteropFix))
        {
            valueNestingLevel++;
            readChunkSizeTag();
        }

        for (int i = 0; i < repository_ids.length; i++)
        {
            if (repository_ids[i].equals(org.omg.CORBA.WStringValueHelper.id()))
            {
                // special handling of strings, according to spec
                result = read_wstring();
                break;
            }
            else if(repository_ids[i].equals(org.omg.CORBA.StringValueHelper.id()))
            {
                // special handling of strings, according to spec
                result = read_string();
                break;
            }
            else if (repository_ids[i].startsWith("RMI:javax.rmi.CORBA.ClassDesc:"))
            {
                // special handling of java.lang.Class instances
                final String classCodebase = (String)read_value(String.class);
                final String reposId = (String)read_value(String.class);
                final String className =
                    org.jacorb.ir.RepositoryID.className(reposId, null);

                try
                {
                    result = loadClass(className, classCodebase);
                }
                catch (ClassNotFoundException e)
                {
                    if( i < repository_ids.length-1 )
                    {
                        continue;
                    }

                    throw new MARSHAL("class not found: " + className);
                }
                break;
            }
            else if (repository_ids[i].startsWith ("IDL:"))
            {
                org.omg.CORBA.portable.ValueFactory factory =
                    ((org.omg.CORBA_2_3.ORB)orb()).lookup_value_factory (repository_ids[i]);

                if (factory != null)
                {
                    currentValueIndex = index;
                    result = factory.read_value (this);
                    break;
                }

                if( i < repository_ids.length-1 )
                {
                    continue;
                }

                throw new MARSHAL ("No factory found for: " + repository_ids[0] );
            }
            else // RMI
            {
                final String className =
                    org.jacorb.ir.RepositoryID.className(repository_ids[i], null);

                try
                {
                    final Class clazz = loadClass(className, codebase);

                    if (IDLEntity.class.isAssignableFrom(clazz))
                    {
                        java.lang.reflect.Method readMethod = null;
                        if (clazz != org.omg.CORBA.Any.class)
                        {
                            String helperClassName = clazz.getName() + "Helper";

                            try
                            {
                                final ClassLoader classLoader = clazz.getClassLoader();
                                final Class helperClass;
                                if (classLoader == null)
                                {
                                    helperClass = ObjectUtil.classForName(helperClassName);
                                }
                                else
                                {
                                    helperClass =
                                        classLoader.loadClass(helperClassName);
                                }

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
                    {
                        result = ValueHandler.readValue(this, index, clazz,
                                                        repository_ids[i],
                                                        null);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    if( i < repository_ids.length-1 )
                    {
                        continue;
                    }

                    throw new MARSHAL ("class not found: " + className);
                }
            }
        }

        // value type instances may be null...
        if( result != null )
        {
            getValueMap().put(Integer.valueOf(index), result);
        }

        return result;
    }

    /** Load the value's class, using the context class loader
     *  of the current thread if possible.  Here's Francisco
     *  Reverbel's <reverbel@ime.usp.br> explanation of why
     *  this is needed in JBoss:
     *
     *  "It seems that ValueHandler.loadClass() uses the thread
     *  context classloader only after it looks for other
     *  classloaders in the call stack (weird). In some
     *  situations (when EJBs are undeployed and then
     *  redeployed) it finds in the call stack a classloader
     *  used for an undeployed EJB. A value of class Foo is
     *  then unmarshalled with type
     *  classloaderOfEJB1:Foo, when the expected type is
     *  classloaderOfEJB2:Foo. I am getting ClassCastExceptions is this
     *  situation.
     *  Explicitly using the thread context class loader in the
     *  first place solves the problem."
     */
    private Class loadClass(String className, final String codebase) throws ClassNotFoundException
    {
        Class clazz;
        ClassLoader clazzLoader = Thread.currentThread().getContextClassLoader();

        if (clazzLoader == null)
        {
            clazz = ValueHandler.loadClass(className, codebase, null);
        }
        else
        {
            try
            {
                clazz = clazzLoader.loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                clazz = ValueHandler.loadClass(className, codebase, null);
            }
        }
        return clazz;
    }

    /**
     * try to read in the chunk size.
     * special handling if there's no chunk size
     * in the stream.
     */
    private void readChunkSizeTag()
    {
        int savedPos = pos;
        int savedIndex = index;
        int chunk_size_tag = read_long();

        if (!sunInteropFix || chunk_size_tag > 0 && chunk_size_tag < MAX_BLOCK_SIZE)
        {
            // valid chunk size: set the ending position of the chunk
            chunk_end_pos = pos + chunk_size_tag;
        }
        else
        {
            // reset buffer and remember that we're not within a chunk
            pos = savedPos;
            index = savedIndex;
            chunk_end_pos = -1;
        }
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
     * Reads a value using the specified factory. The preceeding single RepositoryID is ignored.
     * since the type information is most likely redundant.
     * It is assumed that the tag and the codebase
     * of the value have already been read.
     */
    private java.io.Serializable read_typed_value( final int index,
                                                   final String codebase,
                                                   final org.omg.CORBA.portable.BoxedValueHelper factory)
    {
        String repId = read_repository_id();

        if (!factory.get_id().equals(repId))
        {
            // just to be sure.
            throw new MARSHAL("unexpected RepositoryID. expected: " + factory.get_id() + " got: " + repId);
        }

        return factory.read_value(this);
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
        {
            ids[i] = read_repository_id();
        }

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

            String repId = (String)getRepIdMap().get(Integer.valueOf(index));
            if (repId == null)
            {
                throw new MARSHAL("stale RepositoryID indirection");
            }
            return repId;
        }

        // a new id
        pos -= 4;
        index -= 4;
        int start_offset = pos;
        String repId = read_string();

        getRepIdMap().put(Integer.valueOf(start_offset), repId);
        return repId;
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
            String codebase = (String)getCodebaseMap().get(Integer.valueOf(index));
            if (codebase == null)
            {
                throw
                new MARSHAL("stale codebase indirection");
            }

            return codebase;
        }
        // a new codebase string
        pos -= 4;
        index -= 4;
        int start_offset = pos;
        String codebase = read_string();
        getCodebaseMap().put (Integer.valueOf(start_offset), codebase);
        return codebase;
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
        java.lang.Object value = getValueMap().get (Integer.valueOf(index));

        if (value == null)
        {
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

        return (java.io.Serializable)value;
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
    public java.lang.Object read_abstract_interface(final java.lang.Class clazz)
    {
        return read_boolean() ? (java.lang.Object)read_Object(clazz)
        : (java.lang.Object)read_value(clazz);
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
        getValueMap().put(Integer.valueOf(currentValueIndex), value);
    }

    /**
     * <code>updateMutatorConnection</code> is an accessor that updates the
     * ior mutator.
     *
     * By making callers pass in a GIOPConnection not a transport this allows
     * callers to not have to call getTransport which would require a synchronized
     * lock. Therefore if the mutator has not been enabled this is effectively a
     * NOP.
     *
     * @param connection an <code>org.omg.ETF.Connection</code> value
     */
    public void updateMutatorConnection(GIOPConnection connection)
    {
        if (isMutatorEnabled)
        {
            mutator.updateConnection (connection.getTransport());
        }
    }

    /**
     * update the TypeCodeCache with the contents of the current repeatedTCMap using the
     * entries between startPosition and (startPosition + size).
     * the entries are stored using repositoryID as a key.
     */
    public void updateTypeCodeCache(String repositoryID, Integer startPosition, int size)
    {
        final Integer from = startPosition; //Integer.valueOf(startPosition.intValue());
        final Integer to = Integer.valueOf (startPosition + size);

        final SortedMap sortedMap = repeatedTCMap.subMap(from, to);
        final List toBeCached = new ArrayList();

        // If we have found anything between the original start position and the size.
        for (Iterator j = sortedMap.keySet().iterator(); j.hasNext(); )
        {
            final Integer key = (Integer) j.next();
            final TypeCode value = (TypeCode) sortedMap.get(key);

            // only remember the offset between the start of the nested TypeCode
            // and the start of the parent TypeCode here.
            final Integer offset = Integer.valueOf(key.intValue() - startPosition.intValue());
            toBeCached.add(new TypeCodeCache.Pair(value, offset));
        }

        typeCodeCache.cacheTypeCode(repositoryID, (TypeCodeCache.Pair[])toBeCached.toArray(new TypeCodeCache.Pair[toBeCached.size()]));
    }

    /**
     * try to locate a TypeCode identified by its repository id in the TypeCodeCache.
     * as cached complex TypeCodes may contain nested TypeCodes. the repeatedTCMap
     * will be updated with entries for these TypeCodes. this is necessary so that
     * later indirections pointing to these nested TypeCodes can be resolved.
         *
     * @param repositoryID repository id of the TypeCode that should be looked up in the cache
     * @param startPosition start position in the buffer of the TypeCode that should be looked up in the cache
     * @return the cached TypeCode or null
         */
    public org.omg.CORBA.TypeCode readTypeCodeCache(String repositoryID, Integer startPosition)
    {
        final TypeCodeCache.Pair[] result = typeCodeCache.getCachedTypeCodes(repositoryID);

        if (result == null)
        {
            return null;
        }

        if (result.length == 0)
        {
            return null;
        }

        for (int i = 0; i < result.length; i++)
        {
            // calculate the position of the nested TypeCode by adding its offset
            // to the startPosition of the parent TypeCode
            final Integer position = Integer.valueOf(startPosition.intValue() + result[i].position.intValue());
            repeatedTCMap.put(position, result[i].typeCode);
        }

        return result[0].typeCode;
    }

    /**
     * used for debug/informational output
     */
    public String getIndentString()
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < typeCodeNestingLevel; i++)
        {
            buffer.append("    ");
        }

        return buffer.toString();
    }

    public Logger getLogger()
    {
        return logger;
    }



    /**
     * <code>start_value</code> implements ValueInputStream JavaToIDL mapping 02-01-12.
     * It should read a valuetype header for a nested custom valuetype and increment
     * the valuetype nesting depth.
     */
    public void start_value ()
    {
        int valueTag = read_long ();

        if (valueTag == 0x00000000)
        {
            // null tag, just do nothing
            return;
        }

        // check that codebase is null
        String codebase = ((valueTag & 1) != 0) ? read_codebase() : null;
        if (codebase != null)
        {
            throw new MARSHAL ("Custom marshaled value should have null codebase");
        }

        chunkedValue = ((valueTag & 0x00000008) != 0);

        // single repository ID
        read_repository_id ();

        if (chunkedValue || (valueNestingLevel > 0 && !sunInteropFix))
        {
            valueNestingLevel++;
            readChunkSizeTag();

            handle_chunking();
        }
    }

    /**
     * <code>end_value</code> implements ValueInputStream JavaToIDL mapping 02-01-12.
     * It should read the end tag for the nested custom valuetype (after skipping
     * any data that precedes the end tag) and decrements the valuetype nesting depth.
     *
     */
    public void end_value ()
    {
        // skip rest of chunk to its end
        if (chunk_end_pos != -1 && pos > chunk_end_pos)
        {
            skip (pos - chunk_end_pos);
        }

        handle_chunking ();
    }
}
