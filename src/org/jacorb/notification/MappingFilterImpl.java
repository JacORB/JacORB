package org.jacorb.notification;

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

import java.util.Hashtable;
import java.util.Map;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TypeCode;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.ConstraintNotFound;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.CosNotifyFilter.InvalidValue;
import org.omg.CosNotifyFilter.MappingConstraintInfo;
import org.omg.CosNotifyFilter.MappingConstraintPair;
import org.omg.CosNotifyFilter.MappingFilterPOA;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * MappingFilterImpl.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MappingFilterImpl extends MappingFilterPOA
{

    static class ValueMap
    {
        private Map valueMap_ = new Hashtable();

        public void put( int key, Any value )
        {
            valueMap_.put( new Integer( key ), value );
        }

        public Any get( int key )
        {
            return ( Any ) valueMap_.get( new Integer( key ) );
        }

        public Any remove( int key )
        {
            return ( Any ) valueMap_.remove( new Integer( key ) );
        }

        public void clear()
        {
            valueMap_.clear();
        }
    }

    ////////////////////////////////////////

    Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    private FilterImpl filterImpl_;
    private Any defaultValue_;
    private ValueMap valueMap_ = new ValueMap();

    public MappingFilterImpl( ApplicationContext context,
                              FilterImpl filterImpl,
                              Any defaultValue )
    {
        filterImpl_ = filterImpl;
        defaultValue_ = defaultValue;
    }

    // Implementation of org.omg.CosNotifyFilter.MappingFilterOperations

    /**
     * Describe <code>destroy</code> method here.
     *
     */
    public void destroy()
    {
        filterImpl_.destroy();
        defaultValue_ = null;
    }

    /**
     * Describe <code>constraint_grammar</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String constraint_grammar()
    {
        return filterImpl_.constraint_grammar();
    }

    /**
     * Describe <code>value_type</code> method here.
     *
     * @return a <code>TypeCode</code> value
     */
    public TypeCode value_type()
    {
        return defaultValue_.type();
    }

    /**
     * Describe <code>default_value</code> method here.
     *
     * @return an <code>Any</code> value
     */
    public Any default_value()
    {
        return defaultValue_;
    }

    public MappingConstraintInfo[] add_mapping_constraints( MappingConstraintPair[] mappingConstraintPairArray )
        throws InvalidValue, InvalidConstraint
    {
        ConstraintExp[] _constraintExpArray =
            new ConstraintExp[ mappingConstraintPairArray.length ];

        for ( int x = 0; x < mappingConstraintPairArray.length; ++x )
        {
            _constraintExpArray[ x ] =
                mappingConstraintPairArray[ x ].constraint_expression;
        }

        ConstraintInfo[] _constraintInfo =
            filterImpl_.add_constraints( _constraintExpArray );

        MappingConstraintInfo[] _mappingConstraintInfo =
            new MappingConstraintInfo[ _constraintInfo.length ];

        for ( int x = 0; x < _constraintInfo.length; ++x )
        {
            _mappingConstraintInfo[ x ] =
                new MappingConstraintInfo( _constraintInfo[ x ].constraint_expression,
                                           _constraintInfo[ x ].constraint_id,
                                           mappingConstraintPairArray[ x ].result_to_set );

            valueMap_.put( _constraintInfo[ x ].constraint_id ,
                           mappingConstraintPairArray[ x ].result_to_set );
        }

        return _mappingConstraintInfo;
    }

    /**
     * Describe <code>modify_mapping_constraints</code> method here.
     *
     * @param intArray an <code>int[]</code> value
     * @param mappingConstraintInfoArray a
     * <code>MappingConstraintInfo[]</code> value
     * @exception ConstraintNotFound if an error occurs
     * @exception InvalidValue if an error occurs
     * @exception InvalidConstraint if an error occurs
     */
    public void modify_mapping_constraints( int[] intArray,
                                            MappingConstraintInfo[] mappingConstraintInfoArray )
        throws ConstraintNotFound,
               InvalidValue,
               InvalidConstraint
    {
        ConstraintInfo[] _constraintInfo =
            new ConstraintInfo[ mappingConstraintInfoArray.length ];

        for ( int x = 0; x < _constraintInfo.length; ++x )
        {
            _constraintInfo[ x ] =
                new ConstraintInfo( mappingConstraintInfoArray[ x ].constraint_expression,
                                    mappingConstraintInfoArray[ x ].constraint_id );

            valueMap_.remove( mappingConstraintInfoArray[ x ].constraint_id );
        }

        filterImpl_.modify_constraints( intArray, _constraintInfo );

        for ( int x = 0; x < mappingConstraintInfoArray.length; ++x )
        {
            valueMap_.put( mappingConstraintInfoArray[ x ].constraint_id ,
                           mappingConstraintInfoArray[ x ].value );
        }
    }

    /**
     * Describe <code>get_mapping_constraints</code> method here.
     *
     * @param intArray an <code>int[]</code> value
     * @return a <code>MappingConstraintInfo[]</code> value
     * @exception ConstraintNotFound if an error occurs
     */
    public MappingConstraintInfo[] get_mapping_constraints( int[] intArray ) throws ConstraintNotFound
    {
        ConstraintInfo[] _constraintInfo = filterImpl_.get_constraints( intArray );

        MappingConstraintInfo[] _mappingConstraintInfo =
            new MappingConstraintInfo[ _constraintInfo.length ];


        for ( int x = 0; x < _constraintInfo.length; ++x )
        {
            _mappingConstraintInfo[ x ] =
                new MappingConstraintInfo( _constraintInfo[ x ].constraint_expression,
                                           _constraintInfo[ x ].constraint_id,
                                           valueMap_.get( _constraintInfo[ x ].constraint_id ) );
        }

        return _mappingConstraintInfo;
    }

    /**
     * Describe <code>get_all_mapping_constraints</code> method here.
     *
     * @return a <code>MappingConstraintInfo[]</code> value
     */
    public MappingConstraintInfo[] get_all_mapping_constraints()
    {
        ConstraintInfo[] _constraintInfo = filterImpl_.get_all_constraints();

        MappingConstraintInfo[] _mappingConstraintInfo =
            new MappingConstraintInfo[ _constraintInfo.length ];

        for ( int x = 0; x < _constraintInfo.length; ++x )
        {
            _mappingConstraintInfo[ x ] =
                new MappingConstraintInfo( _constraintInfo[ x ].constraint_expression,
                                           _constraintInfo[ x ].constraint_id,
                                           valueMap_.get( _constraintInfo[ x ].constraint_id ) );
        }

        return _mappingConstraintInfo;
    }

    /**
     * Describe <code>remove_all_mapping_constraints</code> method here.
     *
     */
    public void remove_all_mapping_constraints()
    {
        filterImpl_.remove_all_constraints();
        valueMap_.clear();
    }

    /**
     * Describe <code>match</code> method here.
     *
     * @param any an <code>Any</code> value
     * @param anyHolder an <code>AnyHolder</code> value
     * @return a <code>boolean</code> value
     * @exception UnsupportedFilterableData if an error occurs
     */
    public boolean match( Any any, AnyHolder anyHolder ) throws UnsupportedFilterableData
    {
        logger_.debug( "match" );

        int _filterId = filterImpl_.match_internal( any );

        if ( _filterId != FilterImpl.NO_CONSTRAINT )
        {
            anyHolder.value = valueMap_.get( _filterId );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Describe <code>match_structured</code> method here.
     *
     * @param structuredEvent a <code>StructuredEvent</code> value
     * @param anyHolder an <code>AnyHolder</code> value
     * @return a <code>boolean</code> value
     * @exception UnsupportedFilterableData if an error occurs
     */
    public boolean match_structured( StructuredEvent structuredEvent,
                                     AnyHolder anyHolder ) throws UnsupportedFilterableData
    {
        int _filterId = filterImpl_.match_structured_internal( structuredEvent );

        if ( _filterId != FilterImpl.NO_CONSTRAINT )
        {
            anyHolder.value = valueMap_.get( _filterId );

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Describe <code>match_typed</code> method here.
     *
     * @param propertyArray a <code>Property[]</code> value
     * @param anyHolder an <code>AnyHolder</code> value
     * @return a <code>boolean</code> value
     * @exception UnsupportedFilterableData if an error occurs
     */
    public boolean match_typed( Property[] propertyArray,
                                AnyHolder anyHolder ) throws UnsupportedFilterableData
    {
        throw new NO_IMPLEMENT();
    }
}
