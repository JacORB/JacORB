package org.jacorb.notification.node;

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

import java.lang.reflect.Field;

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.interfaces.AbstractPoolable;
import org.jacorb.notification.parser.TCLParserTokenTypes;
import org.jacorb.notification.util.AbstractObjectPool;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * EvaluationResult.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationResult implements TCLParserTokenTypes
{

    public static final EvaluationResult BOOL_TRUE;
    public static final EvaluationResult BOOL_FALSE;

    static {
        EvaluationResult _r = new EvaluationResult();
        _r.setBool( true );
        BOOL_TRUE = wrapImmutable( _r );
        _r = new EvaluationResult();
        _r.setBool( false );
        BOOL_FALSE = wrapImmutable( _r );
    }

    static Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( EvaluationResult.class.getName() );

    private boolean isFloat_;
    private boolean isLong_;

    private int typeCode_;

    private Object value_;
    private Any any_;

    protected Object getValue()
    {
        return value_;
    }

    private Object setValue( Object value )
    {
        Object _old = value_;

        value_ = value;

        return _old;
    }

    public boolean isLongLong()
    {
        return typeCode_ == TCKind._tk_longlong;
    }

    public boolean isDouble()
    {
        return typeCode_ == TCKind._tk_double;
    }

    public boolean isFloat()
    {
        return typeCode_ == TCKind._tk_float;
    }

    public boolean isLong()
    {
        return typeCode_ == TCKind._tk_long;
    }

    public boolean isString()
    {
        return typeCode_ == TCKind._tk_string;
    }

    public void setString( String s )
    {
        setValue( s );
        typeCode_ = TCKind._tk_string;
    }

    public void setFloat( float f )
    {
        setFloat( new Double( f ) );
    }

    public void setFloat( double d )
    {
        setFloat( new Double( d ) );
    }

    public void setFloat( Double d )
    {
        isFloat_ = true;
        setValue( d );
        typeCode_ = TCKind._tk_float;
    }

    public void setLongLong( long l )
    {
        setLongLong( new Double( l ) );
    }

    public void setLongLong( Double d )
    {
        setValue( d );
        typeCode_ = TCKind._tk_longlong;
    }

    public void setLong( int l )
    {
        setLong( new Double( l ) );
    }

    public void setLong( Double d )
    {
        setValue( d );
        typeCode_ = TCKind._tk_long;
    }

    public void setDouble( Double d )
    {
        setValue( d );
        typeCode_ = TCKind._tk_double;
    }

    public void setDouble( double d )
    {
        setDouble( new Double( d ) );
    }

    public String getString() throws DynamicTypeException
    {
        try
        {
            return ( String ) getValue();
        }
        catch ( ClassCastException c )
        {
            throw new DynamicTypeException();
        }
    }

    public long getLongLong() throws DynamicTypeException
    {
        try
        {
            return ( ( Double ) getValue() ).longValue();
        }
        catch ( ClassCastException e )
        {}

        try
        {
            return ( ( Boolean ) getValue() ).booleanValue() ? 1l : 0;
        }
        catch ( ClassCastException e )
        {}

        try
        {
            String _s = ( String ) getValue();

            if ( _s.length() == 1 )
            {
                return _s.charAt( 0 );
            }
        }
        catch ( ClassCastException e )
        {}

        throw new DynamicTypeException();
    }

    public int getLong() throws DynamicTypeException
    {
        if ( getValue() != null )
        {
            try
            {
                return ( ( Double ) getValue() ).intValue();
            }
            catch ( ClassCastException e )
            {}

            try
            {
                return ( ( Boolean ) getValue() ).booleanValue() ? 1 : 0;
            }
            catch ( ClassCastException e )
            {}

            try
            {
                String _s = ( String ) getValue();

                if ( _s.length() == 1 )
                {
                    return _s.charAt( 0 );
                }
            }
            catch ( ClassCastException e )
            {}

        }
        else
        {
            return any_.extract_long();
        }

        throw new DynamicTypeException();
    }

    public double getDouble() throws DynamicTypeException
    {
        try
        {
            return ( ( Double ) getValue() ).doubleValue();
        }
        catch ( ClassCastException e )
        {}

        try
        {
            return ( ( Boolean ) getValue() ).booleanValue() ? 1d : 0;
        }
        catch ( ClassCastException e )
        {}

        try
        {
            String _s = ( String ) getValue();

            if ( _s.length() == 1 )
            {
                return _s.charAt( 0 );
            }
        }
        catch ( ClassCastException e )
        {}

        throw new DynamicTypeException();
    }


    public float getFloat() throws DynamicTypeException
    {
        try
        {
            return ( ( Double ) getValue() ).floatValue();
        }
        catch ( ClassCastException c )
        {}

        try
        {
            return ( ( Boolean ) getValue() ).booleanValue() ? 1f : 0;
        }
        catch ( ClassCastException c2 )
        {}

        try
        {
            String _s = ( String ) getValue();

            if ( _s.length() == 1 )
            {
                return _s.charAt( 0 );
            }
        }
        catch ( ClassCastException c3 )
        {}

        throw new DynamicTypeException();
    }

    public boolean getBool() throws DynamicTypeException
    {
        try
        {
            return ( ( Boolean ) getValue() ).booleanValue();
        }
        catch ( ClassCastException c )
        {}

        throw new DynamicTypeException();
    }

    public void setBool( boolean b )
    {
        if ( b )
        {
            setValue( Boolean.TRUE );
        }
        else
        {
            setValue( Boolean.FALSE );
        }

        typeCode_ = TCKind._tk_boolean;
    }

    public Any getAny()
    {
        return any_;
    }

    public void addAny( Any any )
    {
        any_ = any;
    }

    static String typeCodeToName( int x )
    {
        try
        {
            Field[] _fields = TCKind.class.getDeclaredFields();

            return _fields[ x ].getName();
        }
        catch ( Exception e )
        {
            return "unknown: " + x;
        }
    }

    public String toString()
    {
        StringBuffer _buffer = new StringBuffer( "{" );

        _buffer.append( getValue() );
        _buffer.append( ";TC=" );
        _buffer.append( typeCodeToName( typeCode_ ) );
        _buffer.append( ";any=" );
        _buffer.append( any_ );
        _buffer.append( "}" );

        return _buffer.toString();
    }

    public boolean equals( Object o )
    {
        logger_.debug( toString() + ".equals(" + o + ")" );

        if ( o instanceof EvaluationResult )
        {
            return ( ( ( EvaluationResult ) o ).getValue().equals( getValue() ) );
        }

        return super.equals( o );
    }

    public int compareTo( EvaluationResult other ) throws DynamicTypeException,
                EvaluationException
    {

        int _ret = Integer.MAX_VALUE;

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "compare " + this + ", " + other );
        }

        if ( getValue() == null && any_ != null && other.getValue() instanceof String )
        {
            logger_.debug( "case 1" );

            try
            {
                String _l = any_.type().member_name( 0 );
                String _r = other.getString();

                _ret = _l.compareTo( other.getString() );
            }
            catch ( BadKind bk )
            {}
            catch ( Bounds bounds )
            {}

        }
        else if ( isString() || other.isString() )
        {

            _ret = getString().compareTo( other.getString() );

            if ( _ret < -1 )
            {
                _ret = -1;
            }
            else if ( _ret > 1 )
            {
                _ret = 1;
            }
        }
        else if ( isFloat() || other.isFloat() )
        {
            float _l = getFloat();
            float _r = other.getFloat();

            if ( _l < _r )
            {
                _ret = -1;
            }
            else if ( _l == _r )
            {
                _ret = 0;
            }
            else
            {
                _ret = 1;
            }

        }
        else
        {
            int _l = getLong();
            int _r = other.getLong();

            if ( _l < _r )
            {
                _ret = -1;
            }
            else if ( _l == _r )
            {
                _ret = 0;
            }
            else
            {
                _ret = 1;
            }
        }

        if ( _ret == Integer.MAX_VALUE )
        {
            throw new DynamicTypeException();
        }

        return _ret;
    }

    public static EvaluationResult wrapImmutable( EvaluationResult er )
    {
        return new ImmutableEvaluationResultWrapper( er );
    }

    public static EvaluationResult plus( EvaluationResult left,
                                         EvaluationResult right ) throws DynamicTypeException
    {

        EvaluationResult _res = new EvaluationResult();

        if ( left.isDouble() ||

                right.isDouble() )
        {
            _res.setDouble( left.getDouble() + right.getDouble() );

        }
        else if ( left.isFloat() ||
                  right.isFloat() )
        {

            _res.setFloat( left.getDouble() + right.getDouble() );

        }
        else if ( left.isLongLong() ||
                  right.isLongLong() )
        {

            _res.setLongLong( left.getLongLong() + right.getLongLong() );

        }
        else if ( left.isLong() ||
                  right.isLong() )
        {

            _res.setLong( left.getLong() + right.getLong() );

        }
        else
        {
            throw new DynamicTypeException();
        }

        return _res;
    }

    public static EvaluationResult minus( EvaluationResult left,
                                          EvaluationResult right )
    throws DynamicTypeException
    {

        EvaluationResult _res = new EvaluationResult();

        if ( left.isDouble() ||

                right.isDouble() )
        {
            _res.setDouble( left.getDouble() - right.getDouble() );

        }
        else if ( left.isFloat() ||
                  right.isFloat() )
        {

            _res.setFloat( left.getDouble() - right.getDouble() );

        }
        else if ( left.isLongLong() ||
                  right.isLongLong() )
        {

            _res.setLongLong( left.getLongLong() - right.getLongLong() );

        }
        else if ( left.isLong() ||
                  right.isLong() )
        {

            _res.setLong( left.getLong() - right.getLong() );

        }
        else
        {
            throw new DynamicTypeException();
        }

        return _res;
    }

    static public EvaluationResult unaryMinus( EvaluationResult r ) throws DynamicTypeException
    {
        EvaluationResult _ret = new EvaluationResult();

        if ( r.isFloat() )
        {
            _ret.setFloat( - r.getFloat() );
        }
        else
        { // (r.isFloat()) {
            _ret.setDouble( - r.getDouble() );
        }

        return _ret;

    }

    static public EvaluationResult div( EvaluationResult left,
                                        EvaluationResult right ) throws DynamicTypeException
    {

        EvaluationResult _res = new EvaluationResult();


        if ( left.isDouble() ||

                right.isDouble() )
        {
            _res.setDouble( left.getDouble() / right.getDouble() );

        }
        else if ( left.isFloat() ||
                  right.isFloat() )
        {

            _res.setFloat( left.getDouble() / right.getDouble() );

        }
        else if ( left.isLongLong() ||
                  right.isLongLong() )
        {

            _res.setLongLong( left.getLongLong() / right.getLongLong() );

        }
        else if ( left.isLong() ||
                  right.isLong() )
        {

            _res.setLong( left.getLong() / right.getLong() );

        }
        else
        {
            throw new DynamicTypeException();
        }

        return _res;

    }

    static public EvaluationResult mult( EvaluationResult left,
                                         EvaluationResult right )
    throws DynamicTypeException
    {

        EvaluationResult _res = new EvaluationResult();

        if ( left.isDouble() ||

                right.isDouble() )
        {
            _res.setDouble( left.getDouble() * right.getDouble() );

        }
        else if ( left.isFloat() ||
                  right.isFloat() )
        {

            _res.setFloat( left.getDouble() * right.getDouble() );

        }
        else if ( left.isLongLong() ||
                  right.isLongLong() )
        {

            _res.setLongLong( left.getLongLong() * right.getLongLong() );

        }
        else if ( left.isLong() ||
                  right.isLong() )
        {

            _res.setLong( left.getLong() * right.getLong() );

        }
        else
        {
            throw new DynamicTypeException();
        }

        return _res;
    }

} // EvaluationResult

class ImmutableEvaluationResultWrapper extends EvaluationResult
{

    static void unsupported()
    {
        throw new UnsupportedOperationException();
    }

    private EvaluationResult delegate_;

    public int compareTo( EvaluationContext evaluationContext,
                          EvaluationResult evaluationResult ) throws DynamicTypeException,
                EvaluationException
    {

        return delegate_.compareTo( evaluationResult );
    }

    public Object getValue()
    {
        return delegate_.getValue();
    }

    public float getFloat() throws DynamicTypeException
    {
        return delegate_.getFloat();
    }

    public boolean equals( Object object )
    {
        return delegate_.equals( object );
    }

    public String toString()
    {
        return delegate_.toString();
    }

    public String getString() throws DynamicTypeException
    {
        return delegate_.getString();
    }

    public boolean isString()
    {
        return delegate_.isString();
    }

    public boolean isLong()
    {
        return delegate_.isLong();
    }

    public boolean isFloat()
    {
        return delegate_.isFloat();
    }

    public boolean isDouble()
    {
        return delegate_.isDouble();
    }

    public boolean getBool() throws DynamicTypeException
    {
        return delegate_.getBool();
    }

    public Any getAny()
    {
        return delegate_.getAny();
    }

    ImmutableEvaluationResultWrapper( EvaluationResult er )
    {
        delegate_ = er;
    }

    public void release()
    {
        unsupported();
    }

    public void setObjectPool()
    {
        unsupported();
    }

    public void setString( String s )
    {
        unsupported();
    }

    public void setFloat( float f )
    {
        unsupported();
    }

    public void setFloat( Double d )
    {
        unsupported();
    }

    public void setInt( int i )
    {
        unsupported();
    }

    public void setInt( Double i )
    {
        unsupported();
    }

    public void setBool( boolean b )
    {
        unsupported();
    }

    public void addAny( Any a )
    {
        unsupported();
    }
}
