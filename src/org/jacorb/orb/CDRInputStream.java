package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

/**
 * Read CDR encoded data 
 *
 * @author Gerald Brose, FU Berlin
 * $Id$
 */

public class CDRInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
{
    /** index for reading from the stream in plain java.io. style */
    int read_index;

    /** the stack for saving/restoring encapsulation information */
    private Stack encaps_stack = new Stack();
    private Hashtable TCTable = new Hashtable();

    /** indexes to support mark/reset */
    private int marked_pos;
    private int marked_index;

    /* character encoding code sets for char and wchar, default ISO8859_1 */
    private int codeSet =  CodeSet.getTCSDefault();
    private int codeSetW=  CodeSet.getTCSWDefault();
    private int minorGIOPVersion = 1; // needed to determine size in chars

    private boolean closed = false;

    /**
     * Maps indices within the buffer (java.lang.Integer) to the values that 
     * appear at these indices.
     */
    private Hashtable valueMap = new Hashtable();

    /**
     * Maps indices within the buffer (java.lang.Integer) to repository ids
     * that appear at these indices.
     */
    private Hashtable repIdMap = new Hashtable();

    public boolean littleEndian = false;
	
    /** indices into the actual buffer */
    protected byte[] buffer = null;
    protected int pos = 0;
    protected int index = 0;

    /** 
        for this stream to be able to return a live object reference,
	a full ORB (not the Singleton!) must be known. If this stream 
	is used only to demarshal base type data, the Singleton is enough
    */
    public org.omg.CORBA.ORB orb = null;
 
    public CDRInputStream( org.omg.CORBA.ORB orb, byte[] buf )
    {
	this.orb = orb;
	buffer = buf;
    }

    public CDRInputStream( org.omg.CORBA.ORB orb, 
                           byte[] buf, 
                           boolean littleEndian )
    {       
        this( orb, buf );
	this.littleEndian = littleEndian;
    }

    public void close()
	throws java.io.IOException
    {
	if( closed )
	    throw new java.io.IOException("Stream already closed!");

	encaps_stack.removeAllElements();
        //	TCTable.clear();
	BufferManager.getInstance().returnBuffer(buffer);
	closed = true;
    }
	
    public void setCodeSet( int codeSet, int codeSetWide )
    {
        this.codeSet = codeSet;
        this.codeSetW = codeSetWide;
    }


    private static final int _read4int(boolean _littleEndian, byte[] _buffer, int _pos)
    {
	if (_littleEndian)
	    return (((_buffer[_pos+3] & 0xff) << 24) +
		    ((_buffer[_pos+2] & 0xff) << 16) +
		    ((_buffer[_pos+1] & 0xff) << 8) +
		    ((_buffer[_pos] & 0xff) << 0));
	else
	    return (((_buffer[_pos] & 0xff) << 24) +
		    ((_buffer[_pos+1] & 0xff) << 16) +
		    ((_buffer[_pos+2] & 0xff) << 8) +
		    ((_buffer[_pos+3] & 0xff) << 0));
    }    

    private static final short _read2int(boolean _littleEndian, byte[] _buffer, int _pos)
    {
	if (_littleEndian)
	    return  (short)(((_buffer[_pos+1] & 0xff) << 8) +
			    ((_buffer[_pos] & 0xff) << 0));
	else
	    return (short)(((_buffer[_pos ] & 0xff) << 8) +
			   ((_buffer[_pos + 1] & 0xff) << 0));
    }


    protected final void skip(int distance)
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

	Debug.output(8,"Closing Encapsulation at pos: " + pos  + " littleEndian now: " + littleEndian + ",  index now " + index );
	//+ "\nnext bytes: " + buffer[pos] + " " + buffer[pos+1] + " " + buffer[pos+2]);
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

        Debug.output(8,"Opening Encapsulation at pos: " + _pos + " size: " + size);
	// Debug.output(8,"( saved " + (index ) + " littleEndian was : " + old_endian + " previously");
        openEncapsulatedArray();	
    }

    public final void openEncapsulatedArray()
    {
        /* reset index to zero, i.e. align relative to the beginning of the encaps. */
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

    public int read( byte[] b)
	throws java.io.IOException
    {
	return read(b, 0, b.length);
    }
    

    public int read(byte[] b, int off, int len)
	throws java.io.IOException
    {
	if( b == null )
	    throw new NullPointerException();

	if( off < 0 || len < 0 || off + len > b.length )
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
	    throw new Error("Unexpected boolean value: " + bb 
			    + " pos: " + pos + " index: " + index);
    }

    /** arrays */

    public final  void read_boolean_array(boolean[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_boolean(); // inlining later...
    }

    public final char read_char(int tcs)
    {
	switch(tcs)
	{
	case 0:
	case CodeSet.ISO8859_1: 
	    index++; 
	    return (char)(0xff & buffer[pos++]);	       	
	case CodeSet.UTF8:
	    short b=(short)(0xff & buffer[pos++]); 
	    index++;
	    //System.out.print("{"+b+"}");
	    if ( (b & 0x80) == 0) 
            {
		return (char)b;
            }
	    else if((b & 0xe0) == 0xc0) 
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
	case CodeSet.UTF16:
	    index += 2; 
	    pos += 2;
            if( littleEndian ) // fix ???
                return (char)((buffer[pos-1] << 8) | (buffer[pos-2] & 0xFF));
            else
                return (char)((buffer[pos-2] << 8) | (buffer[pos-1] & 0xFF));
	}
	throw new Error("Bad CodeSet: "+tcs);
    }
	
    public final char read_char()
    {
	return read_char(codeSet);
    }

    public final void read_char_array(char[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_char(codeSet ); // inlining later...
    }

    public final double read_double() 
    {
	return Double.longBitsToDouble(read_longlong());
    }

    public final void read_double_array(double[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_double(); // inlining later...
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

    public final float read_float() 
    {
	return Float.intBitsToFloat(read_long());
    }

    public final void read_float_array(float[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_float(); // inlining later...
    }

    public final int read_long() 
    {
	int result;

	int remainder = 4 - (index % 4);
	if (remainder != 4)
	{
	    index += remainder;
	    pos+=remainder;
	}

	result = _read4int(littleEndian,buffer,pos);

	index += 4;
	pos += 4;
	return result;
    }

    public final void read_long_array(int[] value, int offset, int length)
    {
	int remainder = 4 - (index % 4);
	if (remainder != 4)
	{
	    index += remainder;
	    pos+=remainder;
	}

	for(int j=offset; j < offset+length; j++)
	{
	    value[j] = _read4int(littleEndian,buffer,pos); // inlining read_long()
	    pos += 4;
	}

	index += 4 * length;
    }


    public final long read_longlong() 
    {    
 	int remainder = 8 - (index % 8);
 	if (remainder != 8)
 	{
 	    index += remainder;
 	    pos+=remainder;
 	}

	if (littleEndian)
	    return ((long) read_long() & 0xFFFFFFFFL) + ((long) read_long() << 32);
	else
	    return ((long) read_long() << 32) + ((long) read_long() & 0xFFFFFFFFL);
    }

    public final void read_longlong_array(long[] value, int offset, int length)
    {
 	int remainder = 8 - (index % 8);
 	if (remainder != 8)
 	{
 	    index += remainder;
 	    pos+=remainder;
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

	pos += 8 * length;
	index += 8 * length;
    }

    public final org.omg.CORBA.Object read_Object()
    {
       	org.omg.IOP.IOR ior = org.omg.IOP.IORHelper.read(this);
	ParsedIOR pior = new ParsedIOR( ior );

	if( pior.isNull() ) 
	    return null;
	else
	{
	    if( ! (orb instanceof org.jacorb.orb.ORB))
		throw new java.lang.RuntimeException(
                                                     "Can not use the singleton ORB to receive object references" + 
                                                     ", please initialize a full ORB instead.");
	    else
		return ((org.jacorb.orb.ORB)orb)._getObject( pior );
	}
    }

    public final byte read_octet()
    {
	index++;
	return buffer[pos++];
    }

    public final void read_octet_array(byte[] value, int offset, int length)
    {
	System.arraycopy(buffer,pos,value,offset,length);
	index += length;
	pos += length;

	//	for(int j=offset; j < offset+length; j++)
	//  value[j] = read_octet(); // inlining later...
    }

    public final org.omg.CORBA.Principal read_Principal()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     *   Read methods for big-endian as well as little endian data input
     *   contributed by Mark Allerton <MAllerton@img.seagatesoftware.com>
     */

    public final short read_short() 
    {
	int remainder = 2 - (index % 2);
	if (remainder != 2)
	{
	    index += remainder;
	    pos+=remainder;
	}

	short result = _read2int(littleEndian,buffer,pos);
	pos += 2;
	index += 2;
	return result;
    }

    public final void read_short_array(short[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_short(); // inlining later...
    }

    public final String read_string()
    {
	return read_string( codeSet );
    }
	
    public final String read_string( int tcs )
    {
	int remainder = 4 - (index % 4);
	if (remainder != 4)
	{
	    index += remainder;
	    pos+=remainder;
	}

	// read size, size is in bytes not chars, but in GIOP prior to
	// 1.2, UTF16 wstring length is encoded as char count !!
	int size = _read4int( littleEndian, buffer, pos);
	index += 4; 
	pos += 4;

	if( size <= 0 ) 
	    return ""; // not allowed by specs, but possible :-)
	
	// performace, devik: the next code is one of fastest ways how
	// to do it because String constructor which is taking byte[] as parameter
	// will do its own conversion internaly. Only slow ops in sequence
	// bellow are "new" and read_char. The new operations can
	// be made faster by caching char[] buffer. The read_char might be inlined
	// but the performance gain will not be so big expecially on Java Hotspot
	// engine.

	char[] buf = new char[size];
	int i;
	int endPos = pos + 
	    ((tcs==CodeSet.UTF16 &&  minorGIOPVersion < 2 ) ? size*2 : size);

	for( i=0; pos < endPos; i++ ) 
	    buf[i] = read_char( tcs );
	
	// devik: detect optional terminating zero, it's not optional in spec.
	// but clever orb should be able to handle missing terminator
	if( buf[i-1] ==0 ) 
	    i--; 
	
	return new String( buf, 0, i);
    }

    /**
     * This operation my only be used directly after reading a string.
     * It "returns" the string to the stream, resetting the read
     * position immediately in front of the string. This is necessary
     * for reading and returning an exception's name to the string.
     * TODO: (devik) THIS FUNCTION IS DANGEROUS BECAUSE OF CODESETS !!!
     * 		why about to replace it with position marking ?
     */

    protected final void unread_string(String str)
    {
	int diff = 4 + str.length() + 1;
	pos -= diff;
	index -= diff;
    }

    public final org.omg.CORBA.TypeCode read_TypeCode()
    {
        return read_TypeCode( new Hashtable());
    }

    public final org.omg.CORBA.TypeCode read_TypeCode(Hashtable tcMap)
    {
	int start_pos = pos;
	int kind = read_long();
	Debug.output(4,"Read Type code of kind " + kind + " at pos: " + start_pos );

	String id, name;
	String[] member_names;
	org.omg.CORBA.TypeCode[] member_types;
	int member_count, length;
	org.omg.CORBA.TypeCode content_type;
	org.omg.CORBA.TypeCode result_tc;
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
	case TCKind._tk_Principal:
	    return orb.get_primitive_tc( org.omg.CORBA.TCKind.from_int(kind) );
	case TCKind._tk_objref: 
	    openEncapsulation();
	    id = read_string();
	    name = read_string();
	    closeEncapsulation();
	    return orb.create_interface_tc(id, name);
	case TCKind._tk_struct: 
	    openEncapsulation();
	    id = read_string();
            tcMap.put( new Integer( start_pos ), id );
	    name = read_string();
	    member_count = read_long();
	    StructMember[] struct_members = new StructMember[member_count];
	    for( int i = 0; i < member_count; i++)
	    {
		struct_members[i] = new StructMember( read_string(),
                                                      read_TypeCode(tcMap), 
                                                      null);
	    }
	    closeEncapsulation();
	    result_tc = orb.create_struct_tc(id, name, struct_members );

	    return result_tc;
	case TCKind._tk_except:
	    openEncapsulation();
	    id = read_string();
            tcMap.put( new Integer( start_pos ), id );
	    name = read_string();
	    member_count = read_long();
	    StructMember[] members = new StructMember[member_count];
	    for( int i = 0; i < member_count; i++)
	    {
		members[i] = new StructMember( read_string(),read_TypeCode(), null);
	    }
	    closeEncapsulation();
	    result_tc = orb.create_struct_tc(id, name, members );
	    return result_tc;
	case TCKind._tk_enum:
	    openEncapsulation();
	    id = read_string();
	    name = read_string();
	    member_count = read_long();
	    member_names = new String[member_count];
	    for( int i = 0; i < member_count; i++)
	    {
		member_names[i] = read_string();
	    }
	    closeEncapsulation();
	    return orb.create_enum_tc(id, name, member_names);
	case TCKind._tk_union:
	    {
		Debug.output(4, "TC Union at pos" + 
                             pos, buffer, pos, buffer.length );

		openEncapsulation();
		id = read_string();
                tcMap.put( new Integer(start_pos), id ); // remember this TC's id and start_pos

		name = read_string();
		Debug.output(4, "TC Union has name " + name + " at pos" + pos );
		org.omg.CORBA.TypeCode discriminator_type = read_TypeCode(tcMap);

		int default_index = read_long();
		Debug.output(4, "TC Union has default idx: " +  
                             default_index +  "  (at pos " + pos );

		member_count = read_long();

		Debug.output(4, "TC Union has " + member_count + 
                             " members at pos " + pos );
		UnionMember[] union_members = new UnionMember[member_count];
		for( int i = 0; i < member_count; i++)
		{
		    Debug.output(4, "Member " + i + "in  union " + 
                                 id + " , " + name + ", start reading TC at pos " + pos );
		    org.omg.CORBA.Any label = orb.create_any();
		    
		    if( i == default_index )
		    {
			//Debug.output(4, "Default discr.");
                        label.insert_octet( read_octet());
		    } 
		    else 
		    {
			label.read_value( this,discriminator_type  );

			Debug.output(4, "non-default discr.: " + 
                                     ((org.jacorb.orb.Any)label).type().kind().value() + 
                                     " " + ((org.jacorb.orb.Any)label).value() );
		    }
		    Debug.output(4, "Member " + i + 
                                 "  start to read name at pos " + pos ); 
		    String mn = read_string();
		    Debug.output(4, "Member " + i + " , read name at pos " + 
                                 pos + " : " + mn); 
		    union_members[i] = 
                        new UnionMember( mn, label, read_TypeCode(tcMap), null);
		    Debug.output(4, "Member " + i + " created " ); 
		}		
		closeEncapsulation();
		result_tc = orb.create_union_tc( id, name, discriminator_type, union_members );
		Debug.output(4, "Done with union " + id + " at pos " + pos ); 
		return result_tc;
	    }
	case TCKind._tk_string: 
	    return orb.create_string_tc(read_long());
	case TCKind._tk_wstring: 
	    return orb.create_wstring_tc(read_long());
	case TCKind._tk_fixed: 
	    return orb.create_fixed_tc(read_ushort(), read_short() );
	case TCKind._tk_array: 
	    openEncapsulation();
	    content_type = read_TypeCode(tcMap);
	    length = read_long();
	    closeEncapsulation();
	    return orb.create_array_tc(length, content_type);
	case TCKind._tk_sequence: 
	    openEncapsulation();
	    content_type = read_TypeCode(tcMap);
	    length = read_long();
	    closeEncapsulation();
	    org.omg.CORBA.TypeCode seq_tc = 
                orb.create_sequence_tc(0, content_type);
	    return seq_tc;
	case TCKind._tk_alias: 
	    openEncapsulation();
	    id = read_string();
	    name = read_string();
	    content_type = read_TypeCode( tcMap );
	    closeEncapsulation();
            result_tc = orb.create_alias_tc( id, name, content_type );
	    return result_tc;
	case TCKind._tk_value: 
	    openEncapsulation();
	    id = read_string();
            tcMap.put( new Integer( start_pos ), id );
	    name = read_string();
            short type_modifier = read_short();
	    org.omg.CORBA.TypeCode concrete_base_type = read_TypeCode( tcMap );
	    member_count = read_long();
	    ValueMember[] vMembers = new ValueMember[member_count];
	    for( int i = 0; i < member_count; i++)
	    {
		vMembers[i] = new ValueMember(read_string(),
                                              null, // id
                                              null, // defined_in
                                              null, // version
                                              read_TypeCode( tcMap ),
                                              null, // type_def
                                              read_short());
	    }
	    closeEncapsulation();
	    return  orb.create_value_tc(id, name, type_modifier,
                                        concrete_base_type, vMembers);
	case TCKind._tk_value_box: 
	    openEncapsulation();
	    id = read_string();
	    name = read_string();
	    content_type = read_TypeCode( tcMap );
	    closeEncapsulation();
            return orb.create_value_box_tc( id, name, content_type );
	case 0xffffffff:
	    /* recursive TC */
	    int offset = pos + read_long();
            String recursiveId = (String)tcMap.get( new Integer(offset));
            Debug.assert( recursiveId != null,
                          "Could not resolve for recursive TypeCode!");
	    org.omg.CORBA.TypeCode rec_tc = 
                orb.create_recursive_tc( recursiveId );

	    return rec_tc;
	default:
	    // error, dump buffer contents for diagnosis
            Debug.output( 2, 
                          "CDRInputStream Buffer Content",
                          buffer);
            Debug.output(2, "Pos : " + pos ); 
	    throw new org.omg.CORBA.MARSHAL("Cannot handle TypeCode with kind " + kind);
	}
    }

    public final int read_ulong()
    {
	return read_long();
    }

    public final void read_ulong_array(int[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_ulong(); // inlining later...
    }

    public final long read_ulonglong()
    {
	return read_longlong();
    }

    public final void read_ulonglong_array(long[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_ulonglong(); // inlining later...
    }

    public final short read_ushort()
    {
	return read_short();
    }

    public final void read_ushort_array(short[] value, int offset, int length)
    {
	for( int j = offset; j < offset+length; j++ )
	    value[j] = read_ushort(); // inlining later...
    }

    public final char read_wchar()
    {
	return read_char( codeSetW );
    }

    public final void read_wchar_array(char[] value, int offset, int length)
    {
	for(int j=offset; j < offset+length; j++)
	    value[j] = read_char( codeSetW ); // inlining later...
    }

    public final String read_wstring()
    {
	return read_string( codeSetW );
    }

    public boolean markSupported()
    {
	return true;
    }

    public void mark( int readLimit )
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

    public final void setLittleEndian(boolean b)
    {
	littleEndian = b;
    }

    /** 
     * to be called from Any 
     */

    final void read_value( org.omg.CORBA.TypeCode tc, CDROutputStream out)
    {
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
	    out.write_Principal( read_Principal());
	    break;
	case TCKind._tk_objref: 
	    out.write_Object( read_Object());
	    break;
	case TCKind._tk_string: 
	    out.write_string( read_string());
	    break;
	case TCKind._tk_wstring: 
	    out.write_wstring( read_wstring());
	    break;
	case TCKind._tk_array: 
	    try
	    {
		int length = tc.length();
		for( int i = 0; i < length; i++ )
		    read_value( tc.content_type(), out);
	    } catch ( org.omg.CORBA.TypeCodePackage.BadKind b ){} 
	    break;
	case TCKind._tk_sequence: 
	    try
	    {
		int len = read_long();
		out.write_long(len);
		for( int i = 0; i < len; i++ )
		    read_value( tc.content_type(), out);
	    } 
	    catch ( org.omg.CORBA.TypeCodePackage.BadKind b ){} 
	    break;
	case TCKind._tk_except:
	    out.write_string( read_string());
	    // don't break, fall through to ...
	case TCKind._tk_struct: 
	    try
	    {
		for( int i = 0; i < tc.member_count(); i++)
		    read_value( tc.member_type(i), out);
	    } 
	    catch ( org.omg.CORBA.TypeCodePackage.BadKind b ){} 
	    catch ( org.omg.CORBA.TypeCodePackage.Bounds b ){}
	    break;
	case TCKind._tk_enum:
	    out.write_long( read_long() );
	    break;
	case TCKind._tk_alias:
	    //	    out.write_string( read_string());
	    //	    out.write_string( read_string());
	    //	    out.write_TypeCode( read_TypeCode());
	    try
	    {
		out.write_value( tc.content_type(), this );
	    }
	    catch ( org.omg.CORBA.TypeCodePackage.BadKind b ){} 
	    break;
	case TCKind._tk_union:
	    try
	    {
		org.omg.CORBA.TypeCode disc = tc.discriminator_type();
		int def_idx = tc.default_index();
		Debug.output(4, "Union Default index " + def_idx ); 
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
		case TCKind._tk_enum:
		    {
			int s = read_long();
                        Debug.output(10, 
                                     "Input  switch: " + s + " at pos " + pos );
			out.write_long(s);
			for( int i = 0 ; i < tc.member_count() ; i++)
			{
			    if( i != def_idx)
			    {
				int label = 
                                    tc.member_label(i).create_input_stream().read_long();

				Debug.output(10, "Input label: " +label + " switch: " + s );
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
		    throw new RuntimeException("Unfinished implementation for unions in anys, sorry.");
		} // switch

		if( member_idx != -1 )
		    read_value( tc.member_type(member_idx), out);
		else
		{
		    if( def_idx == -1 )
		    {
                        Debug.output(4, " -- TC error ", buffer );
			throw new RuntimeException("Error in Union, no member and no default!");
		    }
		    read_value( tc.member_type( def_idx ), out);
		}
	    } 
	    catch ( org.omg.CORBA.TypeCodePackage.BadKind b ){} 
	    catch ( org.omg.CORBA.TypeCodePackage.Bounds b ){}
	    break;	
	case 0xffffffff:
	    out.write_long( read_long());
	    break;
	default:
	    throw new RuntimeException("Cannot handle TypeCode with kind " + kind);
	}
    }

    public java.io.Serializable read_value() 
    {
        int tag = read_long();
        if (tag == 0x7fffff00)
            throw new org.omg.CORBA.MARSHAL ("missing value type information");
        else if (tag == 0x7fffff02)
            return read_typed_value();
        else
            return read_special_value (tag);
    }

    public java.io.Serializable read_value (String rep_id) 
    {
        int tag = read_long();
        if (tag == 0x7fffff00)
            return read_untyped_value (rep_id, pos - 4);
        else if (tag == 0x7fffff02)
            return read_typed_value();
        else
            return read_special_value (tag);
    }

    public java.io.Serializable read_value (java.lang.Class clz) 
    {
        int tag = read_long();
        if (tag == 0x7fffff00)
            return read_untyped_value (org.jacorb.ir.RepositoryID.repId (clz),
                                       pos - 4);
        else if (tag == 0x7fffff02)
            return read_typed_value();
        else
            return read_special_value (tag);
    }

    public java.io.Serializable read_value (
      org.omg.CORBA.portable.BoxedValueHelper factory) 
    {
        int tag = read_long();
        if (tag == 0x7fffff00) 
        {
            int index = pos - 4;
            java.io.Serializable result = factory.read_value (this);
            valueMap.put (new Integer(index), result);
            return result;
        } 
        else 
            return read_special_value (tag);
    }

    /**
     * Immediateley reads a value from this stream; i.e. without any
     * repository id preceding it.  The expected type of the value is given
     * by `repository_id', and the index at which the value started is
     * `index'.
     */
    private java.io.Serializable read_untyped_value (String repository_id,
                                                     int index)
    {
        java.io.Serializable result;
        if (repository_id.startsWith ("IDL:"))
        {
            org.omg.CORBA.portable.ValueFactory factory =
                ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory 
                                                            (repository_id);
            result = factory.read_value (this);
        }
        else // RMI
        {
            javax.rmi.CORBA.ValueHandler v = 
                javax.rmi.CORBA.Util.createValueHandler();

            // ValueHandler wants class, repository_id, and sending context.
            // I wonder why it wants all of these.
            // If we settle down on this implementation, compute these 
            // values more efficiently elsewhere.
            String className = 
                org.jacorb.ir.RepositoryID.className (repository_id);
            Class c = null;
            try {
                c = Class.forName (className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException ("class not found: " + c);
            }
            result = v.readValue (this, index, 
                                  c,
                                  repository_id, 
                                  // use our own code base for now
                                  v.getRunTimeCodeBase());
        }
        
        valueMap.put (new Integer (index), result);
        return result;
    }

    /**
     * Reads a value with type information, i.e. one that is preceded 
     * by a RepositoryID.  It is assumed that the tag of the value
     * has already been read.
     */
    private java.io.Serializable read_typed_value() 
    {
        int index = pos - 4;
        return read_untyped_value (read_repository_id(), index);
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
                throw 
                 new org.omg.CORBA.MARSHAL ("stale RepositoryID indirection");
            else
                return repId;
        }
        else
        {
            // a new id
            pos -= 4;
            int index = pos;
            String repId = read_string();
            repIdMap.put (new Integer(index), repId);
            return repId;
        }
    }

    private java.io.Serializable read_special_value (int tag) {
        if (tag == 0x00000000) 
            // null tag
            return null;
        else if (tag == 0xffffffff) 
        {
            // indirection
            int index = read_long();
            index = index + pos - 4;
            java.lang.Object value = valueMap.get (new Integer(index));
            if (value == null)
                throw new org.omg.CORBA.MARSHAL ("stale value indirection");
            else
                return (java.io.Serializable)value;
        } 
        else
            throw new org.omg.CORBA.MARSHAL ("unknown value tag: " + tag);
    } 

    //      public byte[]  get_buffer(){
    //  	return buffer;
    //      }

    public int get_pos(){
	return pos;
    }

}



