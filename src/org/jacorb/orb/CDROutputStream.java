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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.typecode.DelegatingTypeCodeWriter;
import org.jacorb.orb.typecode.TypeCodeCompactor;
import org.jacorb.util.ObjectUtil;
import org.jacorb.util.ValueHandler;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.StringValueHelper;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.WStringValueHelper;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.ValueOutputStream;
import org.omg.IOP.IOR;
import org.omg.IOP.IORHelper;
import org.omg.IOP.TaggedProfile;

/**
 * @author Gerald Brose,  1999
 *
 * A stream for CDR marshalling.
 *
 */

public class CDROutputStream
    extends org.omg.CORBA_2_3.portable.OutputStream implements CodeSet.OutputBuffer, ValueOutputStream
{
    private final static IOR null_ior = new IOR("", new TaggedProfile[0]);

    /** needed for alignment purposes */
    private int index;

    /** the current write position in the buffer */
    private int pos;

    /** the number of bytes that will only make up the final buffer
        size, but that have not yet been written */
    private int deferred_writes;

    private final IBufferManager bufMgr;
    protected byte[] buffer;

    private boolean closed;

    /* character encoding code sets for char and wchar, default ISO8859_1 */
    private CodeSet codeSet =  CodeSet.getTCSDefault();
    private CodeSet codeSetW=  CodeSet.getTCSWDefault();

    private int encaps_start = -1;

    /**
     * <code>encaps_stack</code> is used to store encapsulations. Do NOT
     * access this variable directly. It is initialized on demand. Use the
     * method {@link #getEncapsStack() getEncapsStack()}
     */
    private Stack encaps_stack;


    /**
     * <code>valueMap</code> is used to maps all value objects that have
     * already been written to this stream to their position within the
     * buffer. The position is stored as a java.lang.Integer. Do NOT access
     * this variable directly. It is initialised on demand. Use the method
     * {@link #getValueMap() getValueMap()}
     */
    private Map valueMap;

    /**
     * <code>repIdMap</code> is used to map all repository ids that have already
     * been written to this stream to their position within the buffer.  The
     * position is stored as a java.lang.Integer. Do NOT access this variable
     * directly. It is initialised on demand. Use the method
     * {@link #getRepIdMap() getRepIdMap()}
     */
    private Map repIdMap;

    /**
     * <code>codebaseMap</code> is used to maps all codebase strings that have
     * already been written to this stream to their position within the buffer.
     * The position is stored as a java.lang.Integer. Do NOT access this variable
     * directly. It is initialised on demand. Use the method
     * {@link #getCodebaseMap() getCodebaseMap()}
     */
    private Map codebaseMap;

    /**
     * This Map is basically a one-entry map pool to be used in
     * write_TypeCode() as the repeatedTCMap.
     */
    private Map repeatedTCMap;

    /**
     * This Map is basically a one-entry map pool to be used in
     * write_TypeCode() as the recursiveTCMap.
     */
    private Map recursiveTCMap;

    /** Remembers the starting position of the current chunk. */
    private int chunk_size_tag_pos = -1;   // -1 means we're not within a chunk
    private int chunk_size_tag_index;
    private int chunk_octets_pos;

    /** Nesting level of chunked valuetypes */
    private int valueNestingLevel = 0;

    /** Nesting level of calls write_value_internal */
    private int writeValueNestingLevel = 0;

    /** True if write_value_internal called writeReplace */
    private boolean writeReplaceCalled = false;

    /** deferredArrayQueue is initialized on demand in write_octet_array */
    /* deferredArrayQueue is initialized on demand in write_octet_array */
    private List deferredArrayQueue;

    private int deferredArrayQueueSize;

    protected final org.jacorb.orb.ORBSingleton orb;

    protected int giop_minor = 2;

    /** The chunking flag is either 0 (no chunking) or 0x00000008 (chunking),
        to be bitwise or'ed into value tags. */
    private int chunkingFlag = 0;

    /**
     * <code>mutator</code> is a pluggable IOR mutator.
     */
    private IORMutator mutator;

    private boolean isMutatorEnabled;

    /**
     * <code>codesetEnabled</code> denotes whether codeset marshalling is enabled.
     */
    private boolean codesetEnabled;

    /** configurable properties */
    private boolean useBOM = false;
    private boolean chunkCustomRmiValuetypes = false;
    private boolean useIndirection = true;
    private boolean nullStringEncoding;
    // by default stream version is 1 for GIOP v1.2 messages
    private byte maxStreamFormatVersion = ValueHandler.STREAM_FORMAT_VERSION_1;

    private final TypeCodeCompactor typeCodeCompactor;

    private final static DelegatingTypeCodeWriter typeCodeWriter = new DelegatingTypeCodeWriter();

    private static class DeferredWriteFrame
    {
        public final int write_pos;
        public final int start;
        public final int length;
        public final byte[] buf;

        public DeferredWriteFrame( int write_pos, int start,
                                   int length, byte[] buf )
        {
            super();

            this.write_pos = write_pos;
            this.start = start;
            this.length = length;
            this.buf = buf;
        }
    }

    /**
     * internal c'tor
     * @param orb must be a JacORB ORB
     * @param bufferSize -1 to fetch the default buffer size,
     *        value > 0 to specify a specific size
     */
    private CDROutputStream(org.omg.CORBA.ORB orb, int bufferSize)
    {
        super();

        if (! (orb instanceof ORBSingleton))
        {
            throw new BAD_PARAM("don't pass in a non JacORB ORB");
        }

        this.orb = (ORBSingleton) orb;
        bufMgr = this.orb.getBufferManager();
        typeCodeCompactor = this.orb.getTypeCodeCompactor();

        if (bufferSize == -1)
        {
            buffer = bufMgr.getPreferredMemoryBuffer();
        }
        else
        {
            buffer = bufMgr.getBuffer(bufferSize);
        }
    }
    /**
     * OutputStreams created using this constructor
     * are used also for in memory marshaling, but do use the
     * ORB's output buffer manager
     */
    public CDROutputStream(final org.omg.CORBA.ORB orb)
    {
        this(orb, -1);

        try
        {
            configure(((ORBSingleton)orb).getConfiguration());
        }
        catch(ConfigurationException e)
        {
            throw new INTERNAL(e.getMessage());
        }
    }

    /**
     * OutputStreams created using  the empty constructor are used for
     * in  memory marshaling. A stream created with this c'tor is not explicitly
     * configured, i.e. it will use default configuration only
     */
    public CDROutputStream()
    {
        this(org.omg.CORBA.ORBSingleton.init());
    }


    public org.omg.CORBA.ORB orb()
    {
        return orb;
    }



    /**
     * This stream is self-configuring, i.e. configure() is private
     * and only called from the constructor
     */

    private void configure(Configuration configuration) throws ConfigurationException
    {
        codesetEnabled  = configuration.getAttributeAsBoolean ("jacorb.codeset", false);

        useBOM = configuration.getAttributeAsBoolean("jacorb.use_bom",false);

        chunkCustomRmiValuetypes = configuration.getAttributeAsBoolean("jacorb.interop.chunk_custom_rmi_valuetypes", false);

        useIndirection = !( configuration.getAttributeAsBoolean("jacorb.interop.indirection_encoding_disable", false));

        nullStringEncoding =
        configuration.getAttributeAsBoolean("jacorb.interop.null_string_encoding", false);

        mutator = (IORMutator) configuration.getAttributeAsObject("jacorb.iormutator");

        isMutatorEnabled = (mutator != null);

        deferredArrayQueueSize = (configuration.getAttributeAsInteger("jacorb.deferredArrayQueue", 8)) * 1000;

        maxStreamFormatVersion = (byte) configuration.getAttributeAsInteger("jacorb.interop.maximum_stream_format_version", 1);
    }


    /**
     * <code>getEncapsStack</code> is used to initialize encaps_stack
     * on demand.
     *
     * @return a <code>Stack</code> value
     */
    private Stack<EncapsInfo> getEncapsStack()
    {
        if (encaps_stack == null)
        {
            encaps_stack = new Stack<EncapsInfo>();
        }
        return encaps_stack;
    }


    /**
     * Gets the Map that is used to detect reference sharing when
     * marshaling valuetype instances.
     *
     * @return a <code>Map</code> value
     */
    private Map<Serializable, Integer> getValueMap()
    {
        if (valueMap == null)
        {
            valueMap = new IdentityHashMap<Serializable, Integer>();
        }
        return valueMap;
    }


    /**
     * Gets the Map that is used to implement indirections for RepositoryIDs.
     *
     * @return a <code>Map</code> value
     */
    private Map<String, Integer> getRepIdMap()
    {
        if (repIdMap == null)
        {
            repIdMap = new HashMap<String, Integer>();
        }
        return repIdMap;
    }

    /**
     * Gets the Map that is used to implement indirections for Codebase
     * specifications.
     *
     * @return a <code>Map</code> value
     */
    private Map<String, Integer> getCodebaseMap()
    {
        if (codebaseMap == null)
        {
            codebaseMap = new HashMap<String, Integer>();
        }
        return codebaseMap;
    }


    /**
     * write the contents of this CDR stream to the output stream,
     * includes all deferred writes (e.g., for byte arrays)...
     * called by, e.g. GIOPConnection to write directly to the
     * wire.
     */

    public void write( OutputStream out, int start, int length )
        throws IOException
    {
        int write_idx = start;
        int read_idx = start;

        // needed to calculate the actual read position in the
        // current buffer,
        int skip_count = 0;

        int list_idx = 0;

        DeferredWriteFrame next_frame = null;

        if (deferredArrayQueue != null && deferredArrayQueue.size() > 0 )
        {
            // find the first frame that falls within the current window,
            // i.e. that need s to be written
            next_frame = (DeferredWriteFrame)deferredArrayQueue.get( list_idx++ );

            // skip all frames beginning before the current start pos, but
            // record their length
            while( next_frame.write_pos < start && list_idx < deferredArrayQueue.size() )
            {
                skip_count += next_frame.length;
                next_frame = (DeferredWriteFrame)deferredArrayQueue.get( list_idx++ );
            }

            // skip
            if( next_frame.write_pos < start && list_idx >= deferredArrayQueue.size() )
            {
                skip_count += next_frame.length;
                next_frame = null;
            }
        }

        while( write_idx < start + length )
        {
            if( next_frame != null && write_idx == next_frame.write_pos )
            {
                if ( ! (next_frame.length <= start + length - write_idx))
                {
                    throw new MARSHAL ("Deferred array does not fit");
                }

                // write a frame, i.e. a byte array
                out.write( next_frame.buf, next_frame.start, next_frame.length );

                // advance
                write_idx += next_frame.length;

                // clear the fram variable...
                next_frame = null;

                // and look up the next frame
                if( deferredArrayQueue != null &&
                    list_idx < deferredArrayQueue.size() )
                {
                    next_frame = (DeferredWriteFrame)deferredArrayQueue.get( list_idx++ );
                    if( next_frame.write_pos > start + length )
                    {
                        // unset, frame is beyond our current reach
                        next_frame = null;
                    }
                }
            }

            if( write_idx < start + length )
            {
                // write data that was previously marshaled

                int write_now =
                Math.min( start + length,
                          ( next_frame != null ? next_frame.write_pos : start + length ));

                write_now -= write_idx; // calculate length

                //
                out.write( buffer, read_idx-skip_count , write_now );

                // advance
                read_idx += write_now;
                write_idx += write_now;
            }
        }
    }

    public void setCodeSets(CodeSet codeSet, CodeSet codeSetWide)
    {
        this.codeSet  = codeSet;
        this.codeSetW = codeSetWide;
    }

    public void setGIOPMinor(final int giop_minor)
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

        bufMgr.returnBuffer( buffer, true );

        buffer = null;
        closed = true;
        deferredArrayQueue = null;
        deferred_writes = 0;
    }

    /**
     * This version of check does both array length checking and
     * data type alignment. It is a convenience method.
     */
    public final void check(final int i, final int align)
    {
        final int remainder = align - (index % align);

        check (i + remainder);

        if (remainder != align)
        {
            // Clear padding. Allowing for possible buffer end.
            final int blp = buffer.length - pos;
            final int topad = (blp <= 8 ? blp : 8);

            Arrays.fill(buffer, pos, pos + topad, (byte)0);

            index += remainder;
            pos += remainder;
        }
    }

    /**
     * check whether the current buffer is big enough to receive
     * i more bytes. If it isn't, get a bigger buffer.
     */

    private final void check(final int i)
    {
        final int requiredSize = pos + i + 2;
        if (buffer == null || requiredSize > buffer.length)
        {
            final byte[] new_buf = bufMgr.getExpandedBuffer(requiredSize);

            if (buffer != null)
            {
                System.arraycopy(buffer, 0, new_buf, 0, pos);
            }

            // Change buffer size so return the old one.
            bufMgr.returnBuffer (buffer, true);

            buffer = new_buf;
        }
    }

    private final static void _write4int
       (final byte[] buf, final int _pos, final int value)
    {
        buf[_pos]   = (byte)((value >> 24) & 0xFF);
        buf[_pos+1] = (byte)((value >> 16) & 0xFF);
        buf[_pos+2] = (byte)((value >>  8) & 0xFF);
        buf[_pos+3] = (byte) (value        & 0xFF);
    }

    /**
     *  Start a CDR encapsulation. All subsequent writes
     *  will place data in the encapsulation until
     *  endEncapsulation is called. This will write
     *  the size of the encapsulation.
     */

    public final void beginEncapsulation()
    {
        // align to the next four byte boundary
        // as a preparation for writing the size
        // integer (which we don't know before the
        // encapsulation is closed)

        check(8,4);

        // leave 4 bytes for the encaps. size that
        // is to be written later

        pos += 4;
        index += 4;

        /* Because encapsulations can be nested, we need to
           remember the beginnning of the enclosing
           encapsulation (or -1 if we are in the outermost encapsulation)
           Also, remember the current index and the indirection maps because
           we need to restore these when closing the encapsulation */

        getEncapsStack().push
        (
            new EncapsInfo(index, encaps_start,
                           getValueMap(),
                           getRepIdMap(),
                           getCodebaseMap())
        );

        // set up new indirection maps for this encapsulation

        valueMap = new IdentityHashMap();
        repIdMap = new HashMap();
        codebaseMap = new HashMap();

        // the start of this encapsulation

        encaps_start = pos;
        beginEncapsulatedArray();
    }

    /**
     * Can be used locally for data type conversions
     * without preceeding call to beginEncapsulation, i.e.
     * without a leading long that indicates the size.
     */

    public final void beginEncapsulatedArray()
    {
        /* set the index for alignment to 0, i.e. align relative to the
           beginning of the encapsulation */
        index = 0;

        // byte_order flag set to FALSE

        buffer[pos++] = 0;
        index++;
    }

    /**
     * Terminate the encapsulation by writing its length
     * to its beginning.
     */

    public final void endEncapsulation()
    {
        if( encaps_start == -1 )
        {
            throw new MARSHAL("Too many end-of-encapsulations");
        }

        if( encaps_stack == null )
        {
            throw new MARSHAL("Internal Error - closeEncapsulation failed");
        }

        // determine the size of this encapsulation

        int encaps_size = pos - encaps_start;

        // insert the size integer into the appropriate place

        buffer[encaps_start -4 ]  = (byte)((encaps_size >>> 24) & 0xFF);
        buffer[encaps_start -3 ] = (byte)((encaps_size >>> 16) & 0xFF);
        buffer[encaps_start -2 ] = (byte)((encaps_size >>>  8) & 0xFF);
        buffer[encaps_start -1 ] = (byte)(encaps_size & 0xFF);

        /* restore index and encaps_start information and indirection maps */

        EncapsInfo ei = (EncapsInfo)getEncapsStack().pop();
        encaps_start = ei.start;
        index = ei.index + encaps_size;
        valueMap = ei.valueMap;
        repIdMap = ei.repIdMap;
        codebaseMap = ei.codebaseMap;
    }

    public byte[] getBufferCopy()
    {
        final ByteArrayOutputStream bos =
            new ByteArrayOutputStream(size());

        try
        {
            write( bos, 0, size());
        }
        catch( IOException e )
        {
            throw new INTERNAL("should not happen: " + e.toString());
        }

        return bos.toByteArray();
    }


    public int size()
    {
        return pos + deferred_writes;
    }


    public final void skip(final int step)
    {
        pos += step;
        index += step;
    }

    public final void reduceSize(final int amount)
    {
        pos -= amount;
    }

    /**
     * Add <tt>amount</tt> empty space
     */

    public final void increaseSize(final  int amount)
    {
        check( amount );

        pos += amount;
    }

    /**
     * Replace existing buffer by and new one and reset indices.
     *
     * @param b the new buffer
     */
    public void setBuffer(final byte[] b)
    {
        bufMgr.returnBuffer( buffer, true );

        buffer = b;

        deferredArrayQueue = null;
        pos = 0;
        deferred_writes = 0;
        index = 0;
    }


    /**************************************************
     * The following operations are from OutputStream *
     **************************************************/

    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        if (deferred_writes > 0)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(index + 1);
            try
            {
                write(baos, 0, index);
            }
            catch (IOException e)
            {
                throw new MARSHAL(e.toString());
            }
            return new CDRInputStream(orb(), baos.toByteArray());
        }

        final byte[] result = new byte[index];
        System.arraycopy(buffer, 0, result, 0, result.length);
        return new CDRInputStream(orb, result);
    }

    public final void write_any(final org.omg.CORBA.Any value)
    {
        write_TypeCode( value.type() );
        value.write_value( this ) ;
    }

    public final void write_boolean(final boolean value)
    {
        check(1);

        if( value )
        {
            buffer[pos++] = 1;
        }
        else
        {
            buffer[pos++] = 0;
        }
        index++;
    }

    public final void write_boolean_array
       (final boolean[] value, final int offset, final int length)
    {
        if (value != null )
        {
            //no alignment necessary
            check(length);

            for( int i = offset; i < offset+length; i++ )
            {
                if( value[i] )
                {
                    buffer[pos++] = 1;
                }
                else
                {
                    buffer[pos++] = 0;
                }
            }
            index += length;
        }
    }


    /**
     * <code>write_char</code> writes a character to the output stream. If
     * codeset translation is active then it will use String and an encoding to
     * get the bytes. It can then do a test for whether to throw DATA_CONVERSION.
     *
     * @param c a <code>char</code> value
     */
    public final void write_char (final char c)
    {
        // According to 15.3.1.6 of CORBA 3.0 'a single instance of the char type
        // may only hold one octet of any multi-byte character encoding.'
        // Therefore we ensure that we are in the single byte range i.e.
        // less than 0xFF or \377.
        if (c > '\377')
        {
            throw new DATA_CONVERSION ("Char " + c + " out of range");
        }

        check( 1 );
        buffer[pos++] = (byte)c;
        index++;
    }


    public final void write_char_array
       (final char[] value, final int offset, final int length)
    {
        if( value == null )
        {
            throw new MARSHAL("Cannot marshall null array.");
        }
        else if ( offset + length > value.length || length < 0 || offset < 0 )
        {
            throw new MARSHAL
                ("Cannot marshall as indices for array are out bounds.");
        }

        check( length );

        for (int i=offset; i < length+offset; i++)
        {
            if (value[i] > '\377')
            {
                throw new DATA_CONVERSION ("Char " + value[i] + " out of range");
            }
            buffer[pos++] = (byte)value[i];
        }
        index+=length;
    }


    /**
     * <code>write_string</code> writes a string to the output stream. It is
     * optimised for whether it is writing a blank string or for whether codeset
     * translation is active.
     *
     * @param value a <code>String</code> value
     */
    public final void write_string(final String value)
    {
        if( value == null )
        {
            if (nullStringEncoding)
            {
                write_long(0);
                return;
            }
            else
            {
                throw new MARSHAL("Cannot marshall null string.");
            }
        }

        final int valueLength = value.length();
        // size leaves room for ulong, plus the string itself (one or more
        // bytes per char in the string, depending on the codeset), plus the
        // terminating NUL char
        int size;
        // sizePosition is the position in the buffer for the size to be
        // written.
        int sizePosition;

        if (valueLength == 0)
        {
            // Blank string; just write size and terminator. Don't need to
            // align here as these methods will do it for us.
            write_long( 1 );
            write_octet( (byte)0 );
        }
        else
        {
            if (codesetEnabled)
            {
                // in the worst case (UTF-8) a string char might take up to 3 bytes
                size = 4 + 3 * valueLength + 1;
            }
            else
            {
                // just one byte per string char
                size = 4 + valueLength + 1;
            }
            check(size, 4);
            sizePosition = pos;

            pos += 4;
            index += 4;

            if (codesetEnabled)
            {
                for (int i = 0; i < valueLength; i++)
                {
                    codeSet.write_char( this, value.charAt(i), false, false, giop_minor );
                }
            }
            else
            {
                // If not using codesets then use the deprecated getBytes for speed.
                value.getBytes(0, valueLength, buffer, pos);
                index += valueLength;
                pos += valueLength;
            }

            buffer[ pos++ ] = (byte) 0; //terminating NUL char
            index ++;

            // Now write the size back in.
            size = pos - (sizePosition + 4); // compute translated size

            _write4int( buffer, sizePosition, size);
        }
    }

    public final void write_wchar(final char c)
    {
        check(6);  // maximum is UTF-16 handling non-BPM character with BOM
        // alignment/check must happen prior to calling.
        codeSetW.write_char( this, c, codeSetW.write_bom( useBOM ), true, giop_minor );
    }


    public void write_byte( byte b )
    {
        buffer[pos++] = b;
        index ++;
    }


    public final void write_wchar_array
       (final char[] value, final int offset, final int length)
    {
        if( value == null )
        {
            throw new MARSHAL("Null References");
        }

        check( length * 3 );

        for( int i = offset; i < offset+length; i++)
        {
            write_wchar( value[i] );
        }
    }

    public final void write_wstring(final String s)
    {
        if( s == null )
        {
            throw new MARSHAL("Null References");
        }

        //size ulong + no of bytes per char (max 3 if UTF-8) +
        //terminating NUL
        check( 4 + s.length() * 3 + 3, 4);

        int startPos = pos;         // store position for length indicator
        pos += 4;
        index += 4;                 // reserve for length indicator

        //the byte order marker
        if( this.giop_minor == 2 && codeSetW.write_bom( useBOM ) && s.length() > 0)
        {
            //big endian encoding
            buffer[ pos++ ] = (byte) 0xFE;
            buffer[ pos++ ] = (byte) 0xFF;

            index += 2;
        }

        // write characters in current wide encoding, add null terminator
        for( int i = 0; i < s.length(); i++ )
        {
            codeSetW.write_char( this, s.charAt(i), false, false, this.giop_minor );
        }

        int str_size;
        if (this.giop_minor >= 2)
        {
            //size in bytes (without the size ulong)
            str_size = pos - startPos - 4;
        }
        else
        {
            //terminating NUL char
            codeSetW.write_char( this, (char)0, false, false, giop_minor );
            str_size = codeSetW.get_wstring_size( s, startPos, pos );
        }

        // write length indicator
        _write4int( buffer, startPos, str_size );
    }


    public final void write_double(final double value)
    {
        write_longlong (Double.doubleToLongBits (value));
    }

    public final void write_double_array
       (final double[] value, final int offset, final int length)
    {
        //if nothing has to be written, return, and especially DON'T
        //ALIGN
        if( length == 0 )
        {
            return;
        }

        /* align to 8 byte boundary */

        check(7 + length*8, 8);

        if( value != null )
        {
            for( int i = offset; i < offset+length; i++ )
            {
                long d = Double.doubleToLongBits(value[i]);
                buffer[pos]   = (byte)((d >>> 56) & 0xFF);
                buffer[pos+1] = (byte)((d >>> 48) & 0xFF);
                buffer[pos+2] = (byte)((d >>> 40) & 0xFF);
                buffer[pos+3] = (byte)((d >>> 32) & 0xFF);
                buffer[pos+4] = (byte)((d >>> 24) & 0xFF);
                buffer[pos+5] = (byte)((d >>> 16) & 0xFF);
                buffer[pos+6] = (byte)((d >>>  8) & 0xFF);
                buffer[pos+7] = (byte) (d & 0xFF);
                pos += 8;
            }
            index += 8*length;
        }
    }

    public void write_fixed(BigDecimal value, short digits, short scale)
    {
        String v = value.unscaledValue().toString();
        byte [] representation;
        int b, c;

        // Strip off any leading '-' from value to encode

        if (v.startsWith ("-"))
        {
            v = v.substring (1);
        }

        if( (v.length() %2) == 0)
        {
            representation = new byte[ v.length()/2 +1];
            representation[0] = 0x00;

            for( int i = 0; i < v.length(); i++ )
            {
                c = Character.digit(v.charAt(i), 10);
                b = representation[(1 + i)/2] << 4;
                b |= c;
                representation[(1 + i)/2] = (byte)b;
            }
        }
        else
        {
            representation = new byte[ (v.length()+1) /2];
            for( int i = 0; i < v.length(); i++ )
            {
                c = Character.digit(v.charAt(i), 10);
                b = representation[i/2] << 4;
                b |= c;
                representation[i/2] = (byte)b;
            }
        }
        b = representation[representation.length-1] << 4;

        representation[representation.length-1] =
            (byte)((value.signum() < 0 )? (b | 0xD) : (b | 0xC));

        check(representation.length);
        System.arraycopy(representation,0,buffer,pos,representation.length);
        index += representation.length;
        pos += representation.length;
    }


    /**
     * @deprecated
     */
    public void write_fixed(final java.math.BigDecimal value)
    {
        write_fixed(value, (short)-1, (short)-1);
    }


    public final void write_float(final float value)
    {
        write_long(Float.floatToIntBits(value));
    }

    public final void write_float_array
       (final float[] value, final int offset, final int length)
    {
        //if nothing has to be written, return, and especially DON'T
        //ALIGN
        if( length == 0 )
        {
            return;
        }

        /* align to 4 byte boundary */

        check(3 + length*4,4);

        if( value != null )
        {
            for( int i = offset; i < offset+length; i++ )
            {
                _write4int(buffer,pos, Float.floatToIntBits( value[i] ));
                pos += 4;
            }
            index += 4*length;
        }
    }

    public final void write_long(final int value)
    {
        check(7,4);

        _write4int(buffer,pos,value);

        pos += 4; index += 4;
    }

    public final void write_long_array
       (final int[] value, final int offset, final int length)
    {
        //if nothing has to be written, return, and especially DON'T
        //ALIGN
        if( length == 0 )
        {
            return;
        }

        /* align to 4 byte boundary */

        check(3 + length*4,4);

        final int remainder = 4 - (index % 4);
        if (remainder != 4)
        {
            index += remainder;
            pos+=remainder;
        }

        if( value != null )
        {
            for( int i = offset; i < offset+length; i++ )
            {
                _write4int(buffer,pos,value[i]);
                pos += 4;
            }
            index += 4*length;
        }
    }

    public final void write_longlong(final long value)
    {
        check(15,8);

        buffer[pos]   = (byte)((value >>> 56) & 0xFF);
        buffer[pos+1] = (byte)((value >>> 48) & 0xFF);
        buffer[pos+2] = (byte)((value >>> 40) & 0xFF);
        buffer[pos+3] = (byte)((value >>> 32) & 0xFF);
        buffer[pos+4] = (byte)((value >>> 24) & 0xFF);
        buffer[pos+5] = (byte)((value >>> 16) & 0xFF);
        buffer[pos+6] = (byte)((value >>>  8) & 0xFF);
        buffer[pos+7] = (byte)(value & 0xFF);

        index += 8;
        pos += 8;
    }

    public final void write_longlong_array
       (final long[] value, final int offset, final int length)
    {
        //if nothing has to be written, return, and especially DON'T
        //ALIGN
        if( length == 0 )
        {
            return;
        }

        check(7 + length*8,8);

        if( value != null )
        {
            for( int i = offset; i < offset+length; i++ )
            {
                buffer[pos]   = (byte)((value[i] >>> 56) & 0xFF);
                buffer[pos+1] = (byte)((value[i] >>> 48) & 0xFF);
                buffer[pos+2] = (byte)((value[i] >>> 40) & 0xFF);
                buffer[pos+3] = (byte)((value[i] >>> 32) & 0xFF);
                buffer[pos+4] = (byte)((value[i] >>> 24) & 0xFF);
                buffer[pos+5] = (byte)((value[i] >>> 16) & 0xFF);
                buffer[pos+6] = (byte)((value[i] >>>  8) & 0xFF);
                buffer[pos+7] = (byte) (value[i] & 0xFF);
                pos += 8;
            }
            index += 8*length;
        }
    }

    public void write_Object(final org.omg.CORBA.Object value)
    {
        if( value == null )
        {
            IORHelper.write(this, null_ior );
        }
        else
        {
            if( value instanceof org.omg.CORBA.LocalObject )
            {
                throw new MARSHAL("Attempt to serialize a locality-constrained object.");
            }
            org.omg.CORBA.portable.ObjectImpl obj =
                (org.omg.CORBA.portable.ObjectImpl)value;

            IOR intermediary = ((Delegate)obj._get_delegate()).getIOR();

            if (isMutatorEnabled)
            {
                intermediary = mutator.mutateOutgoing (intermediary);
            }

            IORHelper.write(this, intermediary);
        }
    }

    ////////////////////////////////////////////// NEW!
    public void write_IOR(final IOR ior)
    {
        if( ior == null )
        {
            IORHelper.write(this, null_ior );
        }
        else
        {
            IORHelper.write(this, ior);
        }
    }
    ////////////////////////////////////////////// NEW!

    public final void write_octet(final byte value)
    {
        check(1);
        index++;
        buffer[pos++] = value;
    }

    public final void write_octet_array( final byte[] value,
                                         final int offset,
                                         final int length)
    {
        if( value != null )
        {
            if( deferredArrayQueueSize > 0 && length > deferredArrayQueueSize )
            {
                if (deferredArrayQueue == null)
                {
                    deferredArrayQueue = new ArrayList();
                }
                deferredArrayQueue.add( new DeferredWriteFrame( index, offset, length, value ));
                index += length;
                deferred_writes += length;
            }
            else
            {
                check(length);
                System.arraycopy(value,offset,buffer,pos,length);
                index += length;
                pos += length;
            }
        }
    }

    public final void write_Principal(final org.omg.CORBA.Principal value)
    {
        throw new NO_IMPLEMENT ("Principal deprecated");
    }

    public final void write_short(final short value)
    {
        check(3,2);

        buffer[pos++] = (byte)((value >> 8) & 0xFF);
        buffer[pos++] = (byte)( value       & 0xFF);
        index += 2;
    }

    public final void write_short_array
       (final short[] value, final int offset, final int length)
    {
        //if nothing has to be written, return, and especially DON'T
        //ALIGN
        if( length == 0 )
        {
            return;
        }

        /* align to 2-byte boundary */

        check(2*length + 3);

        final int remainder = 2 - (index % 2);
        if (remainder != 2)
        {
            index += remainder;
            pos+=remainder;
        }

        if( value != null )
        {
            for( int i = offset; i < offset+length; i++ )
            {
                buffer[pos++] = (byte)((value[i] >>  8) & 0xFF );
                buffer[pos++] = (byte)( value[i]        & 0xFF );
            }
            index += 2*length;
        }
    }

    public final void write_TypeCode (org.omg.CORBA.TypeCode typeCode)
    {
        if (typeCode == null)
        {
            throw new BAD_PARAM("TypeCode is null");
        }

        typeCode = typeCodeCompactor.getCompactTypeCode(typeCode);

        if (repeatedTCMap == null)
        {
            repeatedTCMap = new HashMap();
        }

        if (recursiveTCMap == null)
        {
            recursiveTCMap = new HashMap();
        }

        try
        {
            write_TypeCode(typeCode, recursiveTCMap, repeatedTCMap);
        }
        finally
        {
            repeatedTCMap.clear();
            recursiveTCMap.clear();
        }
    }

    public final void write_TypeCode(final org.omg.CORBA.TypeCode typeCode,
                                      final Map recursiveTCMap,
                                      final Map repeatedTCMap)
    {
        typeCodeWriter.writeTypeCode(typeCode, this, recursiveTCMap, repeatedTCMap);
    }

    public final void write_ulong(final int value)
    {
        write_long (value);
    }

    public final void write_ulong_array
       (final int[] value, final int offset, final int length)
    {
        write_long_array (value, offset, length);
    }

    public final void write_ulonglong(final long value)
    {
        write_longlong (value);
    }

    public final void write_ulonglong_array
       (final long[] value, final int offset, final int length)
    {
        write_longlong_array (value, offset, length);
    }

    public final void write_ushort(final short value)
    {
        write_short (value);
    }

    public final void write_ushort_array
       (final short[] value, final int offset, final int length)
    {
        write_short_array (value, offset, length);
    }

    /**
     * Reads a value of the type indicated by <code>tc</code> from the
     * InputStream <code>in</code> and remarshals it to this CDROutputStream.
     * Called from Any.
     */
    public final void write_value ( final org.omg.CORBA.TypeCode typeCode,
                                    final org.omg.CORBA.portable.InputStream input )
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
                case TCKind._tk_null:   // 0
                    // fallthrough
                case TCKind._tk_void:       // 1
                {
                    break;
                }
                case TCKind._tk_short:      // 2
                {
                    write_short( input.read_short());
                    break;
                }
                case TCKind._tk_long:       // 3
                {
                    write_long( input.read_long());
                    break;
                }
                case TCKind._tk_ushort:     // 4
                {
                    write_ushort(input.read_ushort());
                    break;
                }
                case TCKind._tk_ulong:      // 5
                {
                    write_ulong( input.read_ulong());
                    break;
                }
                case TCKind._tk_float:      // 6
                {
                    write_float( input.read_float());
                    break;
                }
                case TCKind._tk_double:     // 7
                {
                    write_double(input.read_double());
                    break;
                }
                case TCKind._tk_boolean:    // 8
                {
                    write_boolean( input.read_boolean());
                    break;
                }
                case TCKind._tk_char:       // 9
                {
                    write_char( input.read_char());
                    break;
                }
                case TCKind._tk_octet:      // 10
                {
                    write_octet( input.read_octet());
                    break;
                }
                case TCKind._tk_any:        // 11
                {
                    write_any( input.read_any());
                    break;
                }
                case TCKind._tk_TypeCode:   // 12
                {
                    write_TypeCode(input.read_TypeCode());
                    break;
                }
                case TCKind._tk_Principal:  // 13
                {
                    throw new NO_IMPLEMENT ("Principal deprecated");
                }
                case TCKind._tk_objref:     // 14
                {
                    write_Object( input.read_Object());
                    break;
                }
                case TCKind._tk_struct:     // 15
                {
                    for( int i = 0; i < typeCode.member_count(); i++)
                    {
                        write_value( typeCode.member_type(i), input );
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
                        case TCKind._tk_short:      // 2
                        {
                            short s = input.read_short();
                            write_short(s);
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
                        case TCKind._tk_long:       // 3
                        {
                            int s = input.read_long();
                            write_long(s);
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
                        case TCKind._tk_ushort:     // 4
                        {
                            short s = input.read_ushort();
                            write_ushort(s);
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
                        case TCKind._tk_ulong:      // 5
                        {
                            int s = input.read_ulong();
                            write_ulong(s);
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
                            boolean s = input.read_boolean();
                            write_boolean(s);
                            for(int i = 0 ; i < typeCode.member_count() ; i++)
                            {
                                if(i != def_idx)
                                {
                                    if(s == typeCode.member_label(i).extract_boolean())
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_char:       // 9
                        {
                            char s = input.read_char();
                            write_char(s);
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
                        case TCKind._tk_any:        // 11
                        case TCKind._tk_TypeCode:   // 12
                        case TCKind._tk_Principal:  // 13
                        case TCKind._tk_objref:     // 14
                        case TCKind._tk_struct:     // 15
                        case TCKind._tk_union:      // 16
                        {
                            throw new MARSHAL(
                                "Invalid union discriminator type: " + disc);
                        }
                        case TCKind._tk_enum:       // 17
                        {
                            int s = input.read_long();
                            write_long(s);
                            for( int i = 0 ; i < typeCode.member_count(); i++ )
                            {
                                if( i != def_idx)
                                {
                                    int label =
                                    typeCode.member_label(i).create_input_stream().read_long();
                                    /*  we  have to  use  the any's  input
                                        stream   because   enums  are   not
                                        inserted as longs */

                                    if( s == label)
                                    {
                                        member_idx = i;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                        case TCKind._tk_string:     // 18
                        case TCKind._tk_sequence:   // 19
                        case TCKind._tk_array:      // 20
                        case TCKind._tk_alias:      // 21
                        case TCKind._tk_except:     // 22
                        {
                            throw new MARSHAL(
                                "Invalid union discriminator type: " + disc);
                        }
                        case TCKind._tk_longlong:   // 23
                        {
                            long s = input.read_longlong();
                            write_longlong(s);
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
                            long s = input.read_ulonglong();
                            write_ulonglong(s);
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
                    }

                    // write the member or default value, if any
                    // (if the union has no explicit default but the
                    // the case labels do not cover the range of
                    // possible discriminator values, there may be
                    // several "implicit defaults" without associated
                    // union values.

                    if( member_idx != -1 )
                    {
                        write_value( typeCode.member_type( member_idx ), input );
                    }
                    else if( def_idx != -1 )
                    {
                        write_value( typeCode.member_type( def_idx ), input );
                    }
                    break;
                }
                case TCKind._tk_enum:       // 17
                {
                    write_long( input.read_long() );
                    break;
                }
                case TCKind._tk_string:     // 18
                {
                    write_string( input.read_string());
                    break;
                }
                case TCKind._tk_sequence:   // 19
                {
                    int len = input.read_long();
                    write_long(len);

                    org.omg.CORBA.TypeCode content_tc = typeCode.content_type();
                    for( int i = 0; i < len; i++ )
                    {
                        write_value(content_tc, input);
                    }

                    break;
                }
                case TCKind._tk_array:      // 20
                {
                    int length = typeCode.length();
                    if( typeCode.content_type().kind().value() == TCKind._tk_octet )
                    {
                        check( length );
                        input.read_octet_array( buffer, pos, length);
                        index+= length;
                        pos += length;
                    }
                    else
                    {
                        for( int i = 0; i < length; i++ )
                        {
                            write_value( typeCode.content_type(), input );
                        }
                    }
                    break;
                }
                case TCKind._tk_alias:      // 21
                {
                    write_value( typeCode.content_type(), input );
                    break;
                }
                case TCKind._tk_except:     // 22
                {
                    write_string( input.read_string());
                    for (int i = 0; i < typeCode.member_count(); i++)
                    {
                        write_value(typeCode.member_type(i), input);
                    }
                    break;
                }
                case TCKind._tk_longlong:   // 23
                {
                    write_longlong(input.read_longlong());
                    break;
                }
                case TCKind._tk_ulonglong:  // 24
                {
                    write_ulonglong( input.read_ulonglong());
                    break;
                }
                case TCKind._tk_longdouble: // 25
                {
                    throw new org.omg.CORBA.BAD_TYPECODE(
                        "type longdouble not supported in java");
                }
                case TCKind._tk_wchar:      // 26
                {
                    write_wchar( input.read_wchar());
                    break;
                }
                case TCKind._tk_wstring:    // 27
                {
                    write_wstring( input.read_wstring());
                    break;
                }
                case TCKind._tk_fixed:      // 28
                {
                    final short digits = typeCode.fixed_digits();
                    final short scale = typeCode.fixed_scale();
                    final BigDecimal value;

                    if (input instanceof CDRInputStream)
                    {
                        value = ((CDRInputStream)input).read_fixed(digits, scale);
                    }
                    else
                    {
                        // TODO can we remove this? mixed usage orb classes from different vendors ...
                        value = input.read_fixed();
                    }
                    write_fixed(value, digits, scale);
                    break;
                }
                case TCKind._tk_value:      // 29
                {
                    final Serializable val = ((org.omg.CORBA_2_3.portable.InputStream)input).read_value();
                    write_value(val, typeCode.id());
                    break;
                }
                case TCKind._tk_value_box:
                {
                    String id = typeCode.id();
                    org.omg.CORBA.portable.BoxedValueHelper helper =
                        ((org.jacorb.orb.ORB)orb()).getBoxedValueHelper(id);
                    if (helper == null)
                    {
                        throw new MARSHAL("No BoxedValueHelper for id " + id);
                    }
                    java.io.Serializable value =
                        ((org.omg.CORBA_2_3.portable.InputStream)input).read_value(helper);
                    write_value (value, helper);
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

    /**
     * Writes the serialized state of `value' to this stream.
     */

    public void write_value(final java.io.Serializable value)
    {
        if (!write_special_value (value))
        {
            write_value_internal (value,
                                  ValueHandler.getRMIRepositoryID (value.getClass()));
        }
    }

    public void write_value(final java.io.Serializable value,
                            final org.omg.CORBA.portable.BoxedValueHelper factory)
    {
        if (!write_special_value (value))
        {
            write_previous_chunk_size();
            check(7,4);
            getValueMap().put (value, Integer.valueOf(size()));

            if (((value instanceof org.omg.CORBA.portable.IDLEntity) ||
                isSimpleString(value, factory)))
            {
                write_long (0x7fffff00 | chunkingFlag);
            }
            else
            {
                // repository id is required for RMI: types
                write_long (0x7fffff02 | chunkingFlag);

                final String repId = factory.get_id();
                write_repository_id (repId);
            }
            start_chunk();
            factory.write_value (this, value);
            end_chunk();
        }
    }

    /**
     * check if the specified value may be marshalled as a simple string (length, value)
     * or it should be encoded as a valuetype.
     */
    private boolean isSimpleString(Serializable value, BoxedValueHelper factory)
    {
        if (!(value instanceof String))
        {
            return false;
        }

        if (factory.get_id().equals(StringValueHelper.id()))
        {
            return false;
        }

        if (factory.get_id().equals(WStringValueHelper.id()))
        {
            return false;
        }

        return true;
    }

    public void write_value(final java.io.Serializable value,
                            final java.lang.Class clazz)
    {
        if (!write_special_value (value))
        {
            final Class _clazz = value.getClass();
            final String repId = ValueHandler.getRMIRepositoryID(_clazz);
            if (_clazz == clazz && !repId.startsWith("RMI:"))
            {
                // the repository id is required for "RMI:" valuetypes
                write_value_internal (value, null);
            }
            else if (clazz.isInstance (value))
            {
                write_value_internal (value, repId);
            }
            else
            {
                throw new BAD_PARAM();
            }
        }
    }

    public void write_value(final java.io.Serializable value,
                             final String repository_id)
    {
        if (!write_special_value (value))
        {
            write_value_internal (value, repository_id);
        }
    }

    /**
     * If `value' is null, or has already been written to this stream,
     * then this method writes that information to the stream and returns
     * true, otherwise does nothing and returns false.
     */
    private boolean write_special_value(final java.io.Serializable value)
    {
        if (value == null)
        {
            // null tag
            write_long (0x00000000);
            return true;
        }

        Integer index = (Integer)getValueMap().get (value);
        if (index != null)
        {
            // value has already been written -- make an indirection
            write_long (0xffffffff);
            write_long (index.intValue() - size());
            return true;
        }
        return false;
    }

    /**
     * Writes `repository_id' to this stream, perhaps via indirection.
     */
    private void write_repository_id(final String repository_id)
    {
        Integer _index = (Integer)getRepIdMap().get (repository_id);
        if ( _index == null)
        {
            // a new repository id -- write it

            // first make sure the pos we're about to remember is
            // a correctly aligned one, i.e., the actual writing position
            int remainder = 4 - (index % 4);
            if ( remainder != 4 )
            {
                index += remainder;
                pos += remainder;
            }

            getRepIdMap().put (repository_id, Integer.valueOf(size()));
            write_string (repository_id);
        }
        else
        {
            // a previously written repository id -- make an indirection
            write_long (0xffffffff);
            write_long (_index.intValue() - size());
        }
    }

    /**
     * Writes `codebase' to this stream, perhaps via indirection.
     */
    private void write_codebase(final String codebase)
    {
        Integer _index = null;
        if (codebaseMap == null)
        {
            codebaseMap = new HashMap();
        }
        else
        {
            _index = (Integer)getCodebaseMap().get (codebase);
        }
        if ( _index == null)
        {
            // a new codebase -- write it#

            // first make sure the pos we're about to remember is
            // a correctly aligned one
            int remainder = 4 - (index % 4);
            if ( remainder != 4 )
            {
                index += remainder;
                pos += remainder;
            }

            getCodebaseMap().put (codebase, Integer.valueOf(size()));
            write_string (codebase);
        }
        else
        {
            // a previously written codebase -- make an indirection
            write_long (0xffffffff);
            write_long (_index.intValue() - size());
        }
    }

    /**
     * Writes to this stream a value header with the specified `repository_id'
     * and no codebase string.
     */
    private void write_value_header(final String[] repository_ids)
    {
        if (repository_ids != null)
        {
            if( repository_ids.length > 1 )
            {
                // truncatable value type, must use chunking!

                chunkingFlag = 0x00000008;
                write_long (0x7fffff06 | chunkingFlag);
                write_long( repository_ids.length );
                for( int i = 0; i < repository_ids.length; i++ )
                {
                    write_repository_id (repository_ids[i]);
                }
            }
            else
            {
                write_long (0x7fffff02 | chunkingFlag);
                write_repository_id (repository_ids[0]);
            }
        }
        else
        {
            write_long (0x7fffff00 | chunkingFlag);
        }
    }

    /**
     * Writes to this stream a value header with the specified `repository_id'.
     * and `codebase' string.
     */
    private void write_value_header(final String[] repository_ids, final String codebase)
    {
        if (codebase != null)
        {
            if ( repository_ids != null )
            {
                if( repository_ids.length > 1 )
                {
                    // truncatable value type, must use chunking!

                    chunkingFlag = 0x00000008;
                    write_long (0x7fffff07 | chunkingFlag);
                    write_codebase(codebase);
                    write_long( repository_ids.length );

                    for( int i = 0; i < repository_ids.length; i++ )
                    {
                        write_repository_id (repository_ids[i]);
                    }
                }
                else
                {
                    write_long (0x7fffff03 | chunkingFlag);
                    write_codebase(codebase);
                    write_repository_id (repository_ids[0]);
                }
            }
            else
            {
                write_long (0x7fffff01 | chunkingFlag);
                write_codebase(codebase);
            }
        }
        else
        {
            write_value_header (repository_ids);
        }
    }

    /**
     * This method does the actual work of writing `value' to this
     * stream.  If `repository_id' is non-null, then it is used as
     * the type information for `value' (possibly via indirection).
     * If `repository_id' is null, `value' is written without
     * type information.
     * Note: This method does not check for the special cases covered
     * by write_special_value().
     */
    private void write_value_internal(final java.io.Serializable value,
                                       final String repository_id)
    {
        write_previous_chunk_size();
        check(7,4);
        getValueMap().put(value, Integer.valueOf(size()));

        if (value.getClass() == String.class)
        {
            // special handling for strings required according to spec
            String[] repository_ids =
                (repository_id == null) ? null : new String[]{ repository_id };
            write_value_header(repository_ids);
            start_chunk();
            write_wstring((String)value);
            end_chunk();
        }
        else if (value.getClass() == Class.class)
        {
            String[] repository_ids = new String[] {
                    ValueHandler.getRMIRepositoryID(javax.rmi.CORBA.ClassDesc.class)
            };
            write_value_header(repository_ids);
            start_chunk();
            write_value(ValueHandler.getCodebase((Class)value));
            write_value(ValueHandler.getRMIRepositoryID((Class)value));
            end_chunk();
        }
        else if (value instanceof org.omg.CORBA.portable.StreamableValue)
        {
            org.omg.CORBA.portable.StreamableValue streamable =
                (org.omg.CORBA.portable.StreamableValue)value;

            write_value_header( streamable._truncatable_ids() );
            start_chunk();
            ((org.omg.CORBA.portable.StreamableValue)value)._write(this);
            end_chunk();
        }
        else if (value instanceof org.omg.CORBA.portable.CustomValue )
        {
            org.omg.CORBA.DataOutputStream outputStream = new DataOutputStream( this );

            write_value_header
                ( ((org.omg.CORBA.portable.CustomValue )value )._truncatable_ids() );
            ( ( org.omg.CORBA.portable.CustomValue ) value ).marshal( outputStream );
        }
        else
        {
            String[] repository_ids =
                (repository_id == null) ? null : new String[]{ repository_id };
            Class clazz = value.getClass();
            String codebase = ValueHandler.getCodebase(clazz);
            if (value instanceof org.omg.CORBA.portable.IDLEntity)
            {
                java.lang.reflect.Method writeMethod = null;
                if (clazz != org.omg.CORBA.Any.class)
                {
                    String helperClassName = clazz.getName() + "Helper";

                    try
                    {
                        Class helperClass =
                            (clazz.getClassLoader() != null)
                                ? clazz.getClassLoader().loadClass(helperClassName)
                                : ObjectUtil.classForName(helperClassName);

                        Class[] paramTypes =
                            { org.omg.CORBA.portable.OutputStream.class, clazz };
                        writeMethod = helperClass.getMethod("write", paramTypes);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new MARSHAL("Error loading class " + helperClassName
                                          + ": " + e);
                    }
                    catch (NoSuchMethodException e)
                    {
                        throw new MARSHAL("No write method in helper class "
                                          + helperClassName + ": " + e);
                    }
                }
                write_value_header( repository_ids, codebase );
                start_chunk();
                if (writeMethod == null)
                {
                    write_any((org.omg.CORBA.Any)value);
                }
                else
                {
                    try
                    {
                        writeMethod.invoke(null, new Object[] { this, value });
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new MARSHAL("Internal error: " + e);
                    }
                    catch (java.lang.reflect.InvocationTargetException e)
                    {
                        throw new MARSHAL("Exception marshaling IDLEntity: "
                                          + e.getTargetException());
                    }
                }
                end_chunk();

            }
            else
            {
                try
                {
                    writeValueNestingLevel++;
                    if (chunkCustomRmiValuetypes
                        && ValueHandler.isCustomMarshaled(clazz))
                    {
                        chunkingFlag = 0x00000008;
                    }

                    Serializable newValue = value;
                    if (!writeReplaceCalled)
                    {
                        // writeReplace must be called only once for this value
                        newValue = ValueHandler.writeReplace(value);
                        writeReplaceCalled = true; // won't call it again
                    }

                    if (newValue != value)
                    {
                        // recompute codebase and/or repositoryID as might have changed
                        String new_rep_id =
                            ValueHandler.getRMIRepositoryID(newValue.getClass());
                        repository_ids =
                            (new_rep_id == null) ? null : new String []{new_rep_id};
                        clazz = newValue.getClass();
                        codebase = ValueHandler.getCodebase(clazz);
                    }

                    write_value_header( repository_ids, codebase );
                    start_chunk();

                    if (newValue != value)
                    {
                        // look at the new value
                        Integer index = (Integer)getValueMap().get(newValue);
                        if (index != null)
                        {
                            // previously marshaled value -- make an indirection
                            write_long (0xffffffff);
                            write_long (index.intValue() - size());
                        }
                        else if (newValue instanceof org.omg.CORBA.Object)
                        {
                            write_Object((org.omg.CORBA.Object)newValue);
                        }
                        else
                        {
                            ValueHandler.writeValue(this, newValue);
                        }
                    }
                    else
                    {
                        // Skip writeReplace call
                        // (writeReplace was already called for this value)
                        ValueHandler.writeValue(this, value);
                    }
                    end_chunk();
                }
                finally
                {
                    if (--writeValueNestingLevel == 0)
                    {
                        writeReplaceCalled = false;
                    }
                }
            }
        }
    }

    /**
     * start a new chunk, end any previously started chunk (no nesting!)
     */

    private void start_chunk()
    {
        if (chunkingFlag > 0)
        {
            write_previous_chunk_size();
            valueNestingLevel++;
            skip_chunk_size_tag();
        }
    }

    private void end_chunk()
    {
        if (chunkingFlag > 0)
        {
            write_previous_chunk_size();
            write_long(-valueNestingLevel);
            if ( --valueNestingLevel == 0 )
            {
                // ending chunk for outermost value
                chunkingFlag = 0;
            }
            else
            {
                // start continuation chunk for outer value
                skip_chunk_size_tag();
            }
        }
    }


    /**
     * writes the chunk size to the header of the previous chunk
     */
    private void write_previous_chunk_size()
    {
        if (chunk_size_tag_pos != -1)
        {
            if (pos == chunk_octets_pos)
            {
                // empty chunk: erase chunk size tag
                pos = chunk_size_tag_pos;      // the tag will be overwritten
                index = chunk_size_tag_index;  //            by subsequent data
            }
            else
            {
                // go to the beginning of the chunk and write the size tag
                rewrite_long(chunk_size_tag_pos,
                             chunk_size_tag_index,
                             pos - chunk_octets_pos);
            }
            chunk_size_tag_pos = -1; // no chunk is currently open
        }
    }

    private void skip_chunk_size_tag()
    {
        // remember where we are right now,
        chunk_size_tag_pos = pos;
        chunk_size_tag_index = index;

        // insert four bytes here as a place-holder
        write_long(0); // need to go back later and write the actual size

        // remember starting position of chunk data
        chunk_octets_pos = pos;
    }

    /**
     * Writes a CORBA long value to (write_pos, write_index) without clearing
     * the buffer padding. In the case of a non-sequential write, clearing
     * buffer positions after the data just written is likely to erase data
     * previously written.
     */
    private final void rewrite_long(int write_pos,
                                    int write_index,
                                    final int value)
    {
        final int align = 4;
        int remainder = align - (write_index % align);
        if (remainder != align)
        {
            write_pos += remainder;
        }
        _write4int(buffer, write_pos, value);
    }

    /**
     * Writes an abstract interface to this stream. The abstract interface is
     * written as a union with a boolean discriminator, which is true if the
     * union contains a CORBA object reference, or false if the union contains
     * a value.
     */
    public void write_abstract_interface(final java.lang.Object object)
    {
        if (object instanceof org.omg.CORBA.Object)
        {
            write_boolean(true);
            write_Object((org.omg.CORBA.Object)object);
        }
        else
        {
            write_boolean(false);
            write_value((java.io.Serializable)object);
        }
    }


    /**
     * <code>start_value</code> implements ValueOutputStream JavaToIDL mapping 02-01-12.
     * It should end any currently open chunk, write a valuetype header for a nested custom valuetype
     * (with a null codebase and the specified repository ID), and increment the valuetype nesting depth.
     *
     * @param rep_id
     */
    public void start_value (String rep_id)
    {
       write_value_header (new String[] {rep_id});
       start_chunk();
    }


    /**
     * <code>end_value</code> implements ValueOutputStream JavaToIDL mapping 02-01-12.
     * It should end any currently open chunk, write the end tag for the nested custom valuetype,
     * and decrement the valuetype nesting depth.
     */
    public void end_value ()
    {
       end_chunk();
    }

    public byte getMaximumStreamFormatVersion ()
    {
        return maxStreamFormatVersion;
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

    public int get_pos()
    {
        return pos;
    }

    public boolean isIndirectionEnabled()
    {
        return useIndirection;
    }
}
