package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
import java.util.*;

import org.omg.CORBA.*;

import org.jacorb.util.*;
import org.jacorb.orb.connection.CodeSet;

import org.jacorb.util.ValueHandler;

/**
 * Read CDR encoded data
 *
 * @author Gerald Brose, FU Berlin
 * $Id$
 */

public class CDRInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
{
    private int uniqueValue;

    /** index for reading from the stream in plain java.io. style */
    int read_index;

    /** the stack for saving/restoring encapsulation information */
    private Stack encaps_stack = new Stack();

    /** hashtable to remember the original  TCs for a given ID that is
        used in a recursive/repeated TC */
    private Hashtable recursiveTCMap = new Hashtable();

    /** indexes to support mark/reset */
    private int marked_pos;
    private int marked_index;

    private boolean closed = false;

    /** can be set on using property */
    private boolean use_BOM = false;

    /* character encoding code sets for char and wchar, default ISO8859_1 */
    private int codeSet =  CodeSet.getTCSDefault();
    private int codeSetW=  CodeSet.getTCSWDefault();
    public int giop_minor = 2; // needed to determine size in chars

    /**
     * Maps indices within the buffer (java.lang.Integer) to the values that
     * appear at these indices.
     */
    private Hashtable valueMap = new Hashtable();

    /**
     * Index of the current IDL value that is being unmarshalled.
     * This is kept here so that when the value object has been
     * created, the value factory can immediately store it into this
     * stream's valueMap by calling `register_value()'.
     */
    private int currentValueIndex;

    /**
     * Maps indices within the buffer (java.lang.Integer) to repository ids
     * that appear at these indices.
     */
    private Hashtable repIdMap = new Hashtable();

    /**
     * Maps indices within the buffer (java.lang.Integer) to codebase strings
     * that appear at these indices.
     */
    private Hashtable codebaseMap = new Hashtable();

    public boolean littleEndian = false;

    /** indices into the actual buffer */
    protected byte[] buffer = null;
    protected int pos = 0;
    protected int index = 0;

    /**
     * for this stream to be able to return a live object reference, a
     * full ORB (not the Singleton!) must be known. If this stream is
     * used only to demarshal base type data, the Singleton is enough
     */
    private org.omg.CORBA.ORB orb = null;

    public CDRInputStream (final org.omg.CORBA.ORB orb, final byte[] buf)
    {
	this.orb = orb;
	buffer = buf;

        use_BOM = org.jacorb.util.Environment.isPropertyOn("jacorb.use_bom");
    }

    public CDRInputStream (final org.omg.CORBA.ORB orb,
                           final byte[] buf,
                           final boolean littleEndian )
    {
        this( orb, buf );
	this.littleEndian = littleEndian;
    }

    public void setGIOPMinor (final  int giop_minor )
    {
        this.giop_minor = giop_minor;
    }

    public int getGIOPMinor()
    {
        return giop_minor;
    }

    public void close()
	throws java.io.IOException
    {
	if( closed )
	{
	    return;
	    //throw new java.io.IOException("Stream already closed!");
	}

	encaps_stack.removeAllElements();
	BufferManager.getInstance().returnBuffer(buffer);
        recursiveTCMap.clear();
	closed = true;
    }

    public org.omg.CORBA.ORB orb ()
    {
        if (orb == null) orb = org.omg.CORBA.ORB.init(new String[]{}, null);
        return orb;
    }

    public void setCodeSet (final int codeSet, final int codeSetWide)
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


    protected final void skip (final int distance)
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
	EncapsInfo ei = (EncapsInfo)encaps_stack.pop();
	littleEndian = ei.littleEndian;
	int size = ei.size;
	int start = ei.start;

	if( pos < start + size )
	    pos = start + size;

	index = ei.index + size;

        //	Debug.output(8,"Closing Encapsulation at pos: " + pos  + " littleEndian now: " + littleEndian + ",  index now " + index );
    }

    /**
     * open a CDR encapsulation and
     * restore index and byte order information
     */

    public final void openEncapsulation()
    {
	boolean old_endian = littleEndian;
	int _pos = pos;
	int size = read_long();

	/* save current index plus size of the encapsulation on the stack.
	   When the encapsulation is closed, this value will be restored as
	   index */

	encaps_stack.push(new EncapsInfo(old_endian, index, pos, size ));

        //        Debug.output(8,"Opening Encapsulation at pos: " + _pos + " size: " + size);
        openEncapsulatedArray();
    }

    public final void openEncapsulatedArray()
    {
        /* reset index  to zero, i.e. align relative  to the beginning
           of the encaps. */
	resetIndex();
	littleEndian = read_boolean();
    }


    public byte[] getBuffer()
    {
	return buffer;
    }


    /* from java.io.InputStream */

    public int read()
	throws java.io.IOException
    {
	if( closed )
	    throw new java.io.IOException("Stream already closed!");

	if( available() < 1 )
	    return -1;

	return buffer[read_index++];
    }

    public int available()
    {
	return pos - read_index;
    }

    public int read (final byte[] b)
	throws java.io.IOException
    {
	return read(b, 0, b.length);
    }


    public int read (final byte[] b, final int off, final int len)
	throws java.io.IOException
    {
        if( b == null )
            throw new NullPointerException();

        if( off < 0 ||
            len < 0 ||
            off + len > b.length )
            throw new IndexOutOfBoundsException();

        if( len == 0 )
            return 0;

        if( available() < 1 )
            return -1;

        if( closed )
            throw new java.io.IOException("Stream already closed!");

        int min = ( len < available() ? len : available());
        System.arraycopy(buffer, 0, b, off, min );
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
        index++;
        byte bb = buffer[pos++];

        if( bb == 1 )
            return true;
        else if ( bb == 0 )
            return false;
        else
        {
            Debug.output( 1, "", buffer );
            throw new Error("Unexpected boolean value: " + bb
                            + " pos: " + pos + " index: " + index);
        }
    }

    /** arrays */

    public final void read_boolean_array
    (final boolean[] value, final int offset, final int length)
    {
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
                Debug.output( 1, "", buffer );
                throw new Error ("Unexpected boolean value: " + bb
                                 + " pos: " + pos + " index: " + index);
            }
        }
    }

    public final char read_char ()
    {
        index++;
        return (char)(0xff & buffer[pos++]);
    }

    public final void read_char_array
    (final char[] value, final int offset, final int length)
    {
        for (int j = offset; j < offset + length; j++)
        {
            index++;
            value[j] = (char) (0xff & buffer[pos++]);
        }
    }

    public final double read_double ()
    {
        return Double.longBitsToDouble (read_longlong ());
    }

    public final void read_double_array
    (final double[] value, final int offset, final int length)
    {
        for (int j = offset; j < offset + length; j++)
        {
            value[j] = Double.longBitsToDouble (read_longlong ());
        }
    }

    public final java.math.BigDecimal read_fixed()
    {
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

    public final float read_float ()
    {
        return Float.intBitsToFloat (read_long ());
    }

    public final void read_float_array
    (final float[] value, final int offset, final int length)
    {
        for (int j = offset; j < offset + length; j++)
        {
            value[j] = Float.intBitsToFloat (read_long ());
        }
    }

    public final int read_long ()
    {
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


    public final long read_longlong ()
    {
 	int remainder = 8 - (index % 8);
 	if (remainder != 8)
 	{
 	    index += remainder;
 	    pos += remainder;
 	}

	if (littleEndian)
        {
	    return ((long) read_long() & 0xFFFFFFFFL) + ((long) read_long() << 32);
        }
	else
        {
	    return ((long) read_long() << 32) + ((long) read_long() & 0xFFFFFFFFL);
        }
    }

    public final void read_longlong_array
    (final long[] value, final int offset, final int length)
    {
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
                value[j] = ( (long) read_long() & 0xFFFFFFFFL) +
                    ((long) read_long() << 32);
            }
        }
        else
        {
            for(int j=offset; j < offset+length; j++)
            {
                value[j] = ((long) read_long() << 32) +
                    ((long) read_long() & 0xFFFFFFFFL);
            }
        }

        // Do not need to modify pos and index as use read_long above
    }

    public final org.omg.CORBA.Object read_Object()
    {
        org.omg.IOP.IOR ior = org.omg.IOP.IORHelper.read(this);
        ParsedIOR pior = new ParsedIOR( ior );

        if( pior.isNull() )
        {
            return null;
        }
        else
        {
            if( ! (orb instanceof org.jacorb.orb.ORB))
            {
                throw new org.omg.CORBA.MARSHAL( "Cannot use the singleton ORB to receive object references, please initialize a full ORB instead.");
            }
            else
            {
                return ((org.jacorb.orb.ORB)orb)._getObject( pior );
            }
        }
    }

    public org.omg.CORBA.Object read_Object (final java.lang.Class clz)
    {
        if (clz.isInterface () && java.rmi.Remote.class.isAssignableFrom (clz))
        {
            return ((org.omg.CORBA.Object)
                    org.jacorb.util.ValueHandler.portableRemoteObject_narrow
                    (read_Object (), clz));
        }
        else
        {
            return (read_Object ());
        }
    }

    public final byte read_octet ()
    {
	index++;
	return buffer[pos++];
    }

    public final void read_octet_array
    (final byte[] value, final int offset, final int length)
    {
	System.arraycopy (buffer,pos,value,offset,length);
	index += length;
	pos += length;
    }

    public final org.omg.CORBA.Principal read_Principal ()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
    }

    /**
     *   Read methods for big-endian as well as little endian data input
     *   contributed by Mark Allerton <MAllerton@img.seagatesoftware.com>
     */

    public final short read_short ()
    {
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
        int remainder = 4 - (index % 4);
        if( remainder != 4 )
        {
            index += remainder;
            pos += remainder;
        }

        // read size (#bytes == #chars)
        int size = _read4int( littleEndian, buffer, pos);
        index += 4;
        pos += 4;

        char[] buf = new char[ size ];
        for( int i = 0; i < size; i++ )
        {
            buf[ i ] = (char)(0xff & buffer[pos++]);
        }

        index += size;

        if( (size > 0) &&
            (buf[ size - 1 ] == 0) )
        {
            //omit terminating NULL char
            return new String( buf, 0, size - 1 );
        }
        else
        {
            return new String( buf );
        }
    }


    public final org.omg.CORBA.TypeCode read_TypeCode()
    {
        Hashtable tcMap = new Hashtable();
        org.omg.CORBA.TypeCode result = read_TypeCode( tcMap );
        tcMap.clear();
        return result;
    }

    private final org.omg.CORBA.TypeCode read_TypeCode (final  Hashtable tcMap )
    {
        org.omg.CORBA.TypeCode result = null;
        int kind = read_long();
        int start_pos = pos - 4;

        //  Debug.output( 4, "Read Type code of kind " +
        //                        kind + " at pos: " + start_pos );

        String id, name;
        String[] member_names;
        org.omg.CORBA.TypeCode[] member_types;
        int member_count, length;
        org.omg.CORBA.TypeCode content_type;
        boolean byteorder = false;

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
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());
                closeEncapsulation();

                result = orb.create_interface_tc (id, name);
                break;
            }
            case TCKind._tk_struct:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());
                member_count = read_long();

                tcMap.put( new Integer( start_pos ), id );

                StructMember[] struct_members = new StructMember[member_count];
                for( int i = 0; i < member_count; i++)
                {
                   struct_members[i] = new StructMember
                   (
                       validateMember (read_string ()),
                       read_TypeCode (tcMap),
                       null
                   );
                }
                closeEncapsulation();

                result = orb.create_struct_tc (id, name, struct_members);
                recursiveTCMap.put (id , result);
                break;
            }
            case TCKind._tk_except:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());
                member_count = read_long();

                tcMap.put( new Integer( start_pos ), id );

                StructMember[] members = new StructMember[member_count];
                for( int i = 0; i < member_count; i++)
                {
                    members[i] = new StructMember
                    (
                        validateMember (read_string()),
                        read_TypeCode (),
                        null
                    );
                }
                closeEncapsulation();

                result = orb.create_struct_tc (id, name, members);
                recursiveTCMap.put (id, result);
                break;
            }
            case TCKind._tk_enum:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());
                member_count = read_long();

                tcMap.put( new Integer( start_pos ), id );

                member_names = new String[member_count];
                for( int i = 0; i < member_count; i++)
                {
                    member_names[i] = validateMember (read_string ());
                }
                closeEncapsulation();

                result = orb.create_enum_tc (id, name, member_names);
                recursiveTCMap.put (id , result);
                break;
            }
            case TCKind._tk_union:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());

                // remember this TC's id and start_pos
                tcMap.put( new Integer(start_pos), id );

                org.omg.CORBA.TypeCode discriminator_type = read_TypeCode(tcMap);

                // Use the dealiased discriminator type for the label types.
                // This works because the JacORB IDL compiler ignores any aliasing
                // of label types and only the discriminator type is passed on the
                // wire.
                org.omg.CORBA.TypeCode orig_disc_type =
                    ((org.jacorb.orb.TypeCode) discriminator_type).originalType();

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
                        validateMember (read_string ()),
                        label,
                        read_TypeCode(tcMap),
                        null
                    );
                }
                closeEncapsulation();

                result = orb.create_union_tc
                    (id, name, discriminator_type, union_members);
                recursiveTCMap.put (id, result);
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
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());

                tcMap.put( new Integer( start_pos ), id );

                content_type = read_TypeCode( tcMap );
                closeEncapsulation();
                result = orb.create_alias_tc (id, name, content_type );
                recursiveTCMap.put (id , result);
                break;
            }
            case TCKind._tk_value:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());

                tcMap.put( new Integer( start_pos ), id );

                short type_modifier = read_short();
                org.omg.CORBA.TypeCode concrete_base_type = read_TypeCode( tcMap );
                member_count = read_long();
                ValueMember[] vMembers = new ValueMember[member_count];

                for( int i = 0; i < member_count; i++)
                {
                    vMembers[i] = new ValueMember
                    (
                        validateMember (read_string ()),
                        null, // id
                        null, // defined_in
                        null, // version
                        read_TypeCode (tcMap),
                        null, // type_def
                        read_short()
                    );
                }
                closeEncapsulation();
                result = orb.create_value_tc
                    (id, name, type_modifier, concrete_base_type, vMembers);
                recursiveTCMap.put( id , result );
                break;
            }
            case TCKind._tk_value_box:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());

                tcMap.put( new Integer( start_pos ), id );
                content_type = read_TypeCode( tcMap );
                closeEncapsulation();

                result = orb.create_value_box_tc (id, name, content_type);
                recursiveTCMap.put( id , result );
                break;
            }
            case TCKind._tk_abstract_interface:
            {
                openEncapsulation();
                id = validateID (read_string ());
                name = validateName (read_string ());
                closeEncapsulation();

                result = orb.create_abstract_interface_tc (id, name);
                break;
            }
            case 0xffffffff:
            {
                /* recursive TC */
                int negative_offset = read_long();
                String recursiveId =
                    (String)tcMap.get( new Integer( pos - 4 + negative_offset ) );

                Debug.myAssert( recursiveId != null,
                                "No recursive TypeCode! (pos: " +
                                (pos - 4 + negative_offset) + ")");

                // look up TypeCode in map to check if it's repeated
                org.omg.CORBA.TypeCode rec_tc =
                    (org.omg.CORBA.TypeCode) recursiveTCMap.get (recursiveId);

                // Debug.output(4, "** found type code in map " + recursiveId );

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
                throw new org.omg.CORBA.MARSHAL("Cannot handle TypeCode with kind " + kind);
            }
        }
        return result;
    }

    public final int read_ulong ()
    {
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

    public final long read_ulonglong ()
    {
 	int remainder = 8 - (index % 8);
 	if (remainder != 8)
 	{
 	    index += remainder;
 	    pos += remainder;
 	}

	if (littleEndian)
        {
	    return ((long) read_long() & 0xFFFFFFFFL) + ((long) read_long() << 32);
        }
	else
        {
	    return ((long) read_long() << 32) + ((long) read_long() & 0xFFFFFFFFL);
        }
    }

    public final void read_ulonglong_array
    (final long[] value, final int offset, final int length)
    {
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
                value[j] = ( (long) read_long() & 0xFFFFFFFFL) +
                    ((long) read_long() << 32);
            }
        }
        else
        {
            for (int j = offset; j < offset+length; j++)
            {
                value[j] = ((long) read_long() << 32) +
                    ((long) read_long() & 0xFFFFFFFFL);
            }
        }

        // Do not need to modify pos and index as use read_long above
    }

    public final short read_ushort ()
    {
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

    public final char read_wchar ()
    {
        if (giop_minor == 2)
        {
            //ignore size indicator
            read_wchar_size();

            boolean wchar_little_endian = readBOM ();

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


    private final char read_wchar (final boolean wchar_little_endian)
    {
	switch( codeSetW )
	{
            case CodeSet.UTF8 :
            {
                if( giop_minor < 2 )
                {
                    throw new Error( "GIOP 1." + giop_minor +
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

	throw new Error( "Bad CodeSet: " + codeSetW );
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
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_wchar(); // inlining later...
    }

    public final String read_wstring()
    {
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

            char[] buf = new char[ size ];

            int i = 0;
            int endPos = pos + size;

            boolean wchar_litte_endian = readBOM();

            while( pos < endPos )
            {
                //ignore size
                //read_wchar_size();

                buf[ i++ ] = read_wchar( wchar_litte_endian );
            }

            return new String( buf, 0, i );
        }
        else //GIOP 1.1 / 1.0
        {
            // read size
            int size = _read4int( littleEndian, buffer, pos);
            index += 4;
            pos += 4;
            char[] buf = new char[ size ];

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
                return new String( buf, 0, i - 1 );
            }
            else
            {
                //doesn't have a terminating NUL. This is actually not
                //allowed.
                return new String( buf, 0, i );
            }
        }
    }

    public boolean markSupported()
    {
	return true;
    }

    public void mark (final int readLimit)
    {
	marked_pos = pos;
	marked_index = index;
    }

    public void reset()
	throws IOException
    {
	if( pos < 0 )
	    throw new IOException("Mark has not been set!");
	pos = marked_pos;
	index = marked_index;
    }

    // JacORB-specific

    private final void resetIndex()
    {
	index = 0;
    }

    public final void setLittleEndian (final boolean b)
    {
	littleEndian = b;
    }

    /**
     * called from Any
     */

    final void read_value
    (
     final org.omg.CORBA.TypeCode tc,
     final org.omg.CORBA.portable.OutputStream out
     )
    {
        if (tc == null)
        {
            throw new org.omg.CORBA.BAD_PARAM("TypeCode is null");
        }
        int kind = ((org.jacorb.orb.TypeCode)tc)._kind();

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
                throw new org.omg.CORBA.NO_IMPLEMENT ("Principal deprecated");
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
                try
                {
                    int length = tc.length();
                    for( int i = 0; i < length; i++ )
                        read_value( tc.content_type(), out );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind b )
                {}
                break;
            case TCKind._tk_sequence:
                try
                {
                    int len = read_long();
                    out.write_long(len);
                    for( int i = 0; i < len; i++ )
                        read_value( tc.content_type(), out );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind b )
                {}
                break;
            case TCKind._tk_except:
                out.write_string( read_string());
                // don't break, fall through to ...
            case TCKind._tk_struct:
                try
                {
                    for( int i = 0; i < tc.member_count(); i++)
                        read_value( tc.member_type(i), out );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind b )
                {
                    b.printStackTrace();
                }
                catch ( org.omg.CORBA.TypeCodePackage.Bounds b )
                {
                    b.printStackTrace();
                }

                break;
            case TCKind._tk_enum:
                out.write_long( read_long() );
                break;
            case TCKind._tk_alias:
                try
                {
                    read_value( tc.content_type(), out  );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind b )
                {
                    b.printStackTrace();
                }
                break;
            case TCKind._tk_union:
                try
                {
                    TypeCode disc = (TypeCode) tc.discriminator_type ();
                    disc = disc.originalType ();
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
                            throw new org.omg.CORBA.MARSHAL
                                ("Invalid union discriminator type: " + disc);
                    } // switch

                    if( member_idx != -1 )
                    {
                        read_value( tc.member_type( member_idx ), out );
                    }
                    else if( def_idx != -1 )
                    {
                        read_value( tc.member_type( def_idx ), out );
                    }
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ){}
                catch ( org.omg.CORBA.TypeCodePackage.Bounds b ){}

                break;
            case 0xffffffff:
                try
                {
                    org.omg.CORBA.TypeCode _tc =
                        (org.omg.CORBA.TypeCode)recursiveTCMap.get(tc.id());


                    if( _tc == null )
                    {
                        throw new org.omg.CORBA.MARSHAL("No recursive TC found for " + tc.id());
                    }

                    // Debug.output(4, "++ found recursive tc " + tc.id()  );

                    read_value( _tc , out );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind b )
                {
                    b.printStackTrace();
                }
                break;
            default:
                throw new org.omg.CORBA.MARSHAL("Cannot handle TypeCode with kind " + kind);
	}
    }


    public java.io.Serializable read_value()
    {
	int start_offset = pos;
        int tag = read_long();

        if (tag == 0xffffffff)
            // indirection
            return read_indirect_value();
        else if (tag == 0x00000000)
            // null tag
            return null;

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;

        tag = tag & 0xfffffffe;

        if (tag == 0x7fffff00)
            throw new org.omg.CORBA.MARSHAL ("missing value type information");
        else if (tag == 0x7fffff02)
        {
            return read_typed_value(start_offset, codebase);
        }
        else if (tag == 0x7fffff06)
        {
            return read_multi_typed_value( start_offset, codebase );
        }
        else
            throw new org.omg.CORBA.MARSHAL ("unknown value tag: "
                                             + Integer.toHexString(tag));
    }

    /**
     * @overrides read_value(java.io.Serializable value) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value (final String rep_id)
    {
	int start_offset = pos;
        int tag = read_long();
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

        tag = tag & 0xfffffffe;

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
            throw new org.omg.CORBA.MARSHAL ("unknown value tag: "
                                             + Integer.toHexString(tag));
        }
    }

    /**
     * @overrides read_value(value) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value(java.io.Serializable value)
    {
        return read_value( value.getClass()); // GB: is that okay?
    }

    /**
     * @overrides read_value(clz) in
     * org.omg.CORBA_2_3.portable.InputStream
     */

    public java.io.Serializable read_value (final java.lang.Class clz)
    {
	int start_offset = pos;
        int tag = read_long();

        if (tag == 0xffffffff)
        {
            // indirection
            return read_indirect_value();
        }
        else if (tag == 0x00000000)
        {
            // null tag
            Debug.output( 4, "read_value(clz): read null tag");
            return null;
        }

        String codebase = ((tag & 1) != 0) ? read_codebase() : null;

        tag = tag & 0xfffffffe;

        if (tag == 0x7fffff00)
        {
            return read_untyped_value ( new String[]{ org.jacorb.ir.RepositoryID.repId (clz) },
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
            throw new org.omg.CORBA.MARSHAL ("unknown value tag: "
                                             + Integer.toHexString(tag));
        }
    }

    /**
     * @overrides read_value(factory) in
     * org.omg.CORBA_2_3.portable.InputStream
     */


    public java.io.Serializable read_value
    (final org.omg.CORBA.portable.BoxedValueHelper factory)
    {
	int start_offset = pos;
        int tag = read_long();

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

        tag = tag & 0xfffffffe;

        if (tag == 0x7fffff00)
        {
            java.io.Serializable result = factory.read_value (this);

            if( result != null )
                valueMap.put (new Integer(start_offset), result);

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
            throw new org.omg.CORBA.MARSHAL ("unknown value tag: "
                                             + Integer.toHexString(tag));
    }

    /**
     * Immediately reads a value from this stream; i.e. without any
     * repository id preceding it.  The expected type of the value is given
     * by `repository_id', and the index at which the value started is
     * `index'.
     */
    private java.io.Serializable read_untyped_value (final String[] repository_ids,
                                                     final int index,
                                                     final String codebase)
    {
        java.io.Serializable result = null;

        for( int r = 0; r < repository_ids.length; r++ )
        {
            if ( repository_ids[r].equals("IDL:omg.org/CORBA/WStringValue:1.0"))
            {
                // special handling of strings, according to spec
                result = read_wstring();
                break;
            }
            else if( repository_ids[r].startsWith ("IDL:"))
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
                        throw new org.omg.CORBA.MARSHAL ("No factory found for: " +
                                                         repository_ids[0] );
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
                    org.jacorb.ir.RepositoryID.className (repository_ids[r]);

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

                    result = ValueHandler.readValue(this, index, c,
                                                    repository_ids[r], null);
                }
                catch (ClassNotFoundException e)
                {
                    if( r < repository_ids.length-1 )
                        continue;
                    else
                        throw new org.omg.CORBA.MARSHAL ("class not found: " + className);
                }

            }
        }

        // value type instances may be null...
        if( result != null )
        {
            valueMap.put (new Integer (index), result);
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

            String repId = (String)repIdMap.get (new Integer(index));
            if (repId == null)
                throw new org.omg.CORBA.MARSHAL ("stale RepositoryID indirection");
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

            repIdMap.put (new Integer(start_offset), repId);
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
            String codebase = (String)codebaseMap.get (new Integer(index));
            if (codebase == null)
                throw
                    new org.omg.CORBA.MARSHAL ("stale codebase indirection");
            else
                return codebase;
        }
        else
        {
            // a new codebase string
            pos -= 4;
            int index = pos;
            String codebase = read_string();
            codebaseMap.put (new Integer(index), codebase);
            return codebase;
        }
    }

    /**
     * Reads an indirect value from this stream. It is assumed that the
     * value tag (0xffffffff) has already been read.
     */
    private java.io.Serializable read_indirect_value ()
    {
        // indirection
        int index = read_long();
        index = index + pos - 4;
        java.lang.Object value = valueMap.get (new Integer(index));
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
        if (name != null && name.length () == 0)
        {
            return null;
        }
        return name;
    }


    private String validateMember (String name)
    {
        if (name == null || name.length () == 0)
        {
            uniqueValue = (++uniqueValue)%Integer.MAX_VALUE;
            return ("DUMMY_NAME_".concat (String.valueOf (uniqueValue)));
        }
        return name;
    }


    private String validateID (String id)
    {
        if (id == null || id.length () == 0)
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

    public java.lang.Object read_abstract_interface() {
	return read_boolean() ? (java.lang.Object)read_Object()
            : (java.lang.Object)read_value();
    }

    /**
     * Reads an abstract interface from this stream. The abstract interface
     * appears as a union with a boolean discriminator, which is true if the
     * union contains a CORBA object reference, or false if the union contains
     * a value.
     */

    public java.lang.Object read_abstract_interface(final java.lang.Class clz) {
	return read_boolean() ? (java.lang.Object)read_Object(clz)
            : (java.lang.Object)read_value(clz);
    }

    //      public byte[]  get_buffer(){
    //  	return buffer;
    //      }

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

    public void register_value (final java.io.Serializable value)
    {
        valueMap.put (new Integer (currentValueIndex), value);
    }

    //      public void finalize()
    //      {
    //  	try
    //  	{
    //  	    close();
    //  	}
    //  	catch( IOException iox )
    //  	{
    //  	    //ignore
    //  	}
    //      }

}
