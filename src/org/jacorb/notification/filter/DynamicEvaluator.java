package org.jacorb.notification.filter;

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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.omg.CosNotification.Property;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynSequence;
import org.omg.DynamicAny.DynSequenceHelper;
import org.omg.DynamicAny.DynStruct;
import org.omg.DynamicAny.DynStructHelper;
import org.omg.DynamicAny.DynUnion;
import org.omg.DynamicAny.DynUnionHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;

/**
 * Provide the Basic operations needed to evaluate filter expressions
 * on Anys.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DynamicEvaluator
    implements Configurable
{
    private static final String NAME = "name";

    private static final String VALUE = "value";

    ////////////////////////////////////////

    private Logger logger_ = null;

    private DynAnyFactory dynAnyFactory_;

    static final private ORB orb_ = ORB.init();

    static final private Any TRUE_ANY = orb_.create_any();

    static final private Any FALSE_ANY = orb_.create_any();

    static {
        TRUE_ANY.insert_boolean( true );
        FALSE_ANY.insert_boolean( false );
    }

    ////////////////////////////////////////

    public DynamicEvaluator( DynAnyFactory dynAnyFactory )
    {
        dynAnyFactory_ = dynAnyFactory;
    }

    public void configure (Configuration conf)
    {
        logger_ = ((org.jacorb.config.Configuration)conf).
            getNamedLogger( getClass().getName() );
    }

    ////////////////////////////////////////

    public boolean hasDefaultDiscriminator( Any any ) throws EvaluationException
    {
        try
        {
            return ( any.type().default_index() != -1 );
        }
        catch ( BadKind e )
        {
            throw getEvaluationException( e );
        }
    }

    public Any evaluateExistIdentifier( Any value,
                                        String identifier )
        throws EvaluationException
    {
        try
            {
            evaluateIdentifier( value, identifier );
            return TRUE_ANY;
        }
        catch ( EvaluationException e )
        {
            return FALSE_ANY;
        }
    }

    /**
     * identify the unscoped IDL type name of a component.
     * (e.g. mystruct._typeid == 'mystruct')
     *
     * @param value the component
     * @return the IDL type name (string) wrapped in an any
     */
    public Any evaluateTypeName( Any value ) throws EvaluationException
    {
        try
        {
            TypeCode _tc = value.type();
            Any _ret = orb_.create_any();
            _ret.insert_string( _tc.name() );

            return _ret;
        }
        catch ( BadKind e )
        {
            throw getEvaluationException( e );
        }
    }

    /**
     * identify the RepositoryId of a component.
     * (e.g. mystruct._repos_id == 'IDL:module/mystruct:1.0'
     *
     * @param value the component
     * @return the IDL type name (string) wrapped in an any
     */
    public Any evaluateRepositoryId( Any value ) throws EvaluationException
    {
        try
        {
            TypeCode _tc = value.type();
            Any _ret = orb_.create_any();
            _ret.insert_string( _tc.id() );

            return _ret;
        }
        catch ( BadKind e )
        {
            throw getEvaluationException( e );
        }
    }

    /**
     * identify the number of elements of a component.
     * if the parameter is a sequence or an array, this method will
     * return the number of elements in the list.
     *
     * @param value the component
     * @return the number of elements in the list
     */
    public Any evaluateListLength( Any value ) throws EvaluationException
    {
        int _length;

        switch ( value.type().kind().value() )
        {
            case TCKind._tk_array:
                DynAny _dynAny = toDynAny( value );
                _length = _dynAny.component_count();
                break;

            case TCKind._tk_sequence:
                DynSequence _dynSequence = toDynSequence( value );
                _length = _dynSequence.get_length();
                break;

            default:
                throw new EvaluationException( "Neither array nor sequence" );
        }

        Any _any = orb_.create_any();
        _any.insert_long( _length );

        return _any;
    }

    String getDefaultUnionMemberName( TypeCode unionTypeCode )
        throws EvaluationException
    {
        try
        {
            int _defaultIndex = unionTypeCode.default_index();

            if ( _defaultIndex != -1 )
            {
                return unionTypeCode.member_name( _defaultIndex );
            }
        }
        catch ( BadKind e )
        {
            throw getEvaluationException( e );
        }
        catch ( Bounds e )
        {
            throw getEvaluationException( e );
        }

        throw new EvaluationException();
    }

    String getUnionMemberNameFromDiscriminator( TypeCode unionTypeCode,
                                                int discriminator )
        throws EvaluationException
    {
        try
        {
            Any _any = orb_.create_any();

            switch ( unionTypeCode.discriminator_type().kind().value() )
            {

                case TCKind._tk_long:
                    _any.insert_long( discriminator );
                    break;

                case TCKind._tk_ulong:
                    _any.insert_ulong( discriminator );
                    break;

                case TCKind._tk_short:
                    _any.insert_short( ( short ) discriminator );
                    break;

                case TCKind._tk_double:
                    _any.insert_double( discriminator );
                    break;

                case TCKind._tk_ushort:
                    _any.insert_ushort( ( short ) discriminator );
                    break;
            }

            int _memberCount = unionTypeCode.member_count();
            String _discrimName = null;

            try
            {
                for ( int _x = 0; _x < _memberCount; _x++ )
                {
                    if ( _any.equal( unionTypeCode.member_label( _x ) ) )
                    {
                        return unionTypeCode.member_name( _x );
                    }
                }
            }
            catch ( Bounds b )
            {}

        }
        catch ( BadKind e )
        {
            throw getEvaluationException( e );
        }

        throw new EvaluationException();
    }

    /**
     * extract the default member from Union wrapped inside the
     * provided Any.
     */
    public Any evaluateUnion( Any value )
        throws EvaluationException
    {
        String _defaultMemberName = getDefaultUnionMemberName( value.type() );

        return evaluateIdentifier( value, _defaultMemberName );
    }


    public Any evaluateUnion( Any value, int position )
        throws EvaluationException
    {
        Any _ret = null;
        DynUnion _dynUnion = toDynUnion( value );

        _dynUnion.seek( 0 );

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "extract idx: "
                           + position
                           + " from Union "
                           + _dynUnion.type() );
        }

        String _discrimName =
            getUnionMemberNameFromDiscriminator( value.type(), position );

        _ret = evaluateIdentifier( _dynUnion, _discrimName );

        return _ret;
    }

    public Any evaluatePropertyList( Property[] list, String name )
    {
        if (logger_.isDebugEnabled() ) {
            logger_.debug( "evaluatePropertyList " + list );
            logger_.debug( "list length: " + list.length );
        }

        for ( int x = 0; x < list.length; ++x )
        {

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( x + ": " + list[ x ].name + " => " + list[ x ].value );
            }

            if ( name.equals( list[ x ].name ) )
            {
                return list[ x ].value;
            }
        }

        return null;
    }

    /**
     * extract a named value out of a sequence of name/value pairs.
     */
    public Any evaluateNamedValueList( Any any, String name )
        throws EvaluationException
    {
        try
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "evaluateNamedValueList(" + any + ", " + name + ")" );
            }

            Any _ret = null;
            DynAny _dynAny = toDynAny( any );
            int _count = _dynAny.component_count();
            _dynAny.rewind();
            DynAny _cursor;

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "Entries: " + _count );
            }

            for ( int _x = 0; _x < _count; _x++ )
            {
                _dynAny.seek( _x );
                _cursor = _dynAny.current_component();
                _ret = evaluateNamedValue( _cursor, name );

                if ( _ret != null )
                {
                    break;
                }
            }

            return _ret;
        }
        catch ( TypeMismatch e )
        {
            throw getEvaluationException( e );
        }
    }

    protected Any evaluateNamedValue( DynAny any, String name )
        throws EvaluationException
    {
        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "evaluate assoc "
                           + name
                           + " on a Any of type: "
                           + any.type() );
        }

        Any _ret = null;

        String _anyName = evaluateIdentifier( any, NAME ).extract_string();

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "test if "
                           + name
                           + " == "
                           + _anyName );
        }

        if ( name.equals( _anyName ) )
        {
            logger_.debug( "YES" );
            _ret = evaluateIdentifier( any, VALUE );
        }

        return _ret;
    }

    /**
     * extract the n-th position out of an Array wrapped inside an Any.
     */
    public Any evaluateArrayIndex( Any any, int index )
        throws EvaluationException
    {
        try
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "evaluate array idx "
                               + index
                               + " on a Any of type: "
                               + any.type() );
            }

            Any _ret = null;

            DynAny _dynAny = toDynAny( any );
            DynAny _cursor;
            Object _res;

            _dynAny.rewind();
            _dynAny.seek( index );
            _cursor = _dynAny.current_component();

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "evaluation result is of type: " + _cursor.type() );
            }

            return _cursor.to_any();
        }
        catch ( TypeMismatch e )
        {
            throw getEvaluationException( e );
        }
    }

    Any evaluateIdentifier( DynAny any, int position )
        throws EvaluationException
    {
        try
        {
            Any _ret = null;
            DynAny _cursor;

            switch ( any.type().kind().value() )
            {

                case TCKind._tk_struct:
                    any.seek( position );
                    _cursor = any.current_component();
                    break;

                default:
                    throw new EvaluationException( "attempt to access member on non-struct" );
            }

            return _cursor.to_any();
        }
        catch ( TypeMismatch e )
        {
            throw getEvaluationException( e );
        }
    }


    public Any evaluateIdentifier( Any any, int position )
        throws EvaluationException
    {
        Any _ret = null;

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "evaluate idx " + position + " on Any" );
        }

        DynAny _dynAny = toDynAny( any );

        return evaluateIdentifier( _dynAny, position );
    }

    public Any evaluateDiscriminator( Any any )
    throws EvaluationException
    {
        switch ( any.type().kind().value() )
        {
            case TCKind._tk_union:
                DynUnion _dynUnion = toDynUnion( any );
                return _dynUnion.get_discriminator().to_any();

            default:
                throw new EvaluationException( "any does not contain member _d" );
        }
    }

    public EvaluationResult evaluateElementInSequence( EvaluationContext context,
                                                       EvaluationResult element,
                                                       Any sequence )
        throws EvaluationException
    {
        try
        {
            DynSequence _dynSequence = DynSequenceHelper.narrow( toDynAny( sequence ) );
            DynAny _currentComponent;

            _dynSequence.rewind();

            while ( true )
            {
                _currentComponent = _dynSequence.current_component();

                EvaluationResult _r =
                    EvaluationResult.fromAny( _currentComponent.to_any() );

                if ( element.compareTo( _r ) == 0 )
                {
                    return EvaluationResult.BOOL_TRUE;
                }

                if ( !_dynSequence.next() )
                {
                    return EvaluationResult.BOOL_FALSE;
                }
            }
        }
        catch ( TypeMismatch e )
        {
            throw getEvaluationException( e );
        }
    }

    /**
     * expensive
     */
    public Any evaluateIdentifier( Any any, String identifier )
        throws EvaluationException
    {

        // expensive call
        DynAny _dynAny = toDynAny( any );

        // expensive call
        return evaluateIdentifier( _dynAny, identifier );
    }

    /**
     *
     */
    Any evaluateIdentifier( DynAny any, String identifier )
        throws EvaluationException
    {
        try
        {
            Any _ret = null;

            String _strippedIdentifier = stripBackslash( identifier );

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "evaluate " + _strippedIdentifier + " on Any" );
            }

            DynAny _cursor = any;
            Object _res;

            switch ( any.type().kind().value() )
            {

                case TCKind._tk_struct:

                    logger_.debug( "Any is a struct" );

                    DynStruct _dynStruct = DynStructHelper.narrow( any );
                    String _currentName;

                    _dynStruct.rewind();

                    while ( true )
                    {
                        _currentName = _dynStruct.current_member_name();

                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( " => " + _currentName );
                        }

                        if ( _currentName.equals( _strippedIdentifier ) )
                        {
                            // expensive operation
                            _cursor = _dynStruct.current_component();
                            break;
                        }

                        boolean _hasNext = _dynStruct.next();

                        if ( !_hasNext )
                        {
                            throw new EvaluationException( "struct has no member " + _strippedIdentifier );
                        }
                    }

                    break;

                case TCKind._tk_union:

                    if ( logger_.isDebugEnabled() )
                    {
                        logger_.debug( "Any is a Union" );
                    }

                    DynUnion _dynUnion = toDynUnion( any );

                    if ( _dynUnion.member_name().equals( _strippedIdentifier ) )
                    {
                        _cursor = _dynUnion.member();
                    }
                    else
                    {
                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( _dynUnion.member_name() + " != " + _strippedIdentifier );
                        }

                        throw new EvaluationException( "member " +
                                                       _strippedIdentifier +
                                                       " is not active on struct" );
                    }

                    break;

                case TCKind._tk_any:
                    logger_.debug( "encapsulated any" );

                    return evaluateIdentifier( any.get_any(), _strippedIdentifier );

                default:
                    logger_.debug( "unknown " + any.type() );
                    return null;
                    //            throw new RuntimeException();
            }

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "Result: " + _cursor );
            }

            if ( _cursor != null && logger_.isDebugEnabled() )
            {
                logger_.debug( "evaluation result is of type: " + _cursor.type() );
            }

            if ( _cursor == null )
            {
                logger_.debug( "Member not found" );

                throw new EvaluationException( "member not found" );
            }

            _ret = _cursor.to_any();

            return _ret;
        }
        catch ( InvalidValue e )
        {
            throw getEvaluationException( e );
        }
        catch ( TypeMismatch e )
        {
            throw getEvaluationException( e );
        }
    }

    ////////////////////////////////////////

    DynAny toDynAny( Any any ) throws EvaluationException
    {
        try
        {
            return dynAnyFactory_.create_dyn_any( any );
        }
        catch ( InconsistentTypeCode e )
        {
            throw getEvaluationException( e );
        }
    }

    DynUnion toDynUnion( Any any ) throws EvaluationException
    {
        return DynUnionHelper.narrow( toDynAny( any ) );
    }

    DynUnion toDynUnion( DynAny dynAny )
    {
        return DynUnionHelper.narrow( dynAny );
    }

    DynStruct toDynStruct( DynAny dynAny ) throws EvaluationException
    {

        return DynStructHelper.narrow( dynAny );

    }

    DynStruct toDynStruct( Any any ) throws EvaluationException
    {
        return DynStructHelper.narrow( toDynAny( any ) );
    }

    DynSequence toDynSequence( Any any ) throws EvaluationException
    {
        return DynSequenceHelper.narrow( toDynAny( any ) );
    }

    static String stripBackslash( String identifier )
    {
        StringBuffer _buffer = new StringBuffer();
        int _length = identifier.length();

        for ( int _x = 0; _x < _length; _x++ )
        {
            if ( identifier.charAt( _x ) != '\\' )
            {
                _buffer.append( identifier.charAt( _x ) );
            }
        }

        return _buffer.toString();
    }

    static EvaluationException getEvaluationException( Exception e )
    {
        return new EvaluationException( e.getMessage() );
    }

}
