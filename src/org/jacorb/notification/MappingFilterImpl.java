package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.interfaces.Disposable;
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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MappingFilterImpl extends MappingFilterPOA implements Disposable
{
    /**
     * map constraint ids used by class FilterImpl to default values
     * used by MappingFilterImpl.
     */
    private static class ValueMap
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

        public void dispose()
        {
            valueMap_.clear();
        }
    }

    ////////////////////////////////////////

//     private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    private AbstractFilter filterImpl_;

    private Any defaultValue_;

    private ValueMap valueMap_ = new ValueMap();
    private Logger logger_ = null;
    private org.jacorb.config.Configuration config_ = null;

    ////////////////////////////////////////

    public MappingFilterImpl( ApplicationContext context,
                              AbstractFilter filterImpl,
                              Any defaultValue )
    {
        filterImpl_ = filterImpl;
        defaultValue_ = defaultValue;
    }

    ////////////////////////////////////////

    public void configure (Configuration conf)
    {
        config_ = ((org.jacorb.config.Configuration)conf);
        logger_ = config_.getNamedLogger(getClass().getName());
    }



    public void destroy()
    {
        dispose();
    }


    public void dispose() {
        filterImpl_.dispose();
        valueMap_.dispose();
        defaultValue_ = null;
    }


    public String constraint_grammar()
    {
        return filterImpl_.constraint_grammar();
    }


    public TypeCode value_type()
    {
        return defaultValue_.type();
    }


    public Any default_value()
    {
        return defaultValue_;
    }


    public MappingConstraintInfo[] add_mapping_constraints( MappingConstraintPair[] mcp )
        throws InvalidValue, InvalidConstraint
    {
        ConstraintExp[] _constraintExpArray =
            new ConstraintExp[ mcp.length ];

        for ( int x = 0; x < mcp.length; ++x )
        {
            _constraintExpArray[ x ] =
                mcp[ x ].constraint_expression;
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
                                           mcp[ x ].result_to_set );

            valueMap_.put( _constraintInfo[ x ].constraint_id ,
                           mcp[ x ].result_to_set );
        }

        return _mappingConstraintInfo;
    }


    public void modify_mapping_constraints( int[] intArray,
                                            MappingConstraintInfo[] mappingConstraintInfos )
        throws ConstraintNotFound,
               InvalidValue,
               InvalidConstraint
    {
        ConstraintInfo[] _constraintInfo =
            new ConstraintInfo[ mappingConstraintInfos.length ];

        for ( int x = 0; x < _constraintInfo.length; ++x )
        {
            _constraintInfo[ x ] =
                new ConstraintInfo( mappingConstraintInfos[ x ].constraint_expression,
                                    mappingConstraintInfos[ x ].constraint_id );

            valueMap_.remove( mappingConstraintInfos[ x ].constraint_id );
        }

        filterImpl_.modify_constraints( intArray, _constraintInfo );

        for ( int x = 0; x < mappingConstraintInfos.length; ++x )
        {
            valueMap_.put( mappingConstraintInfos[ x ].constraint_id ,
                           mappingConstraintInfos[ x ].value );
        }
    }


    public MappingConstraintInfo[] get_mapping_constraints( int[] constraintIds )
        throws ConstraintNotFound
    {
        ConstraintInfo[] _constraintInfo = filterImpl_.get_constraints( constraintIds );

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


    public void remove_all_mapping_constraints()
    {
        filterImpl_.remove_all_constraints();

        valueMap_.dispose();
    }


    public boolean match( Any any, AnyHolder anyHolder )
        throws UnsupportedFilterableData
    {
        int _filterId = filterImpl_.match_internal( any );

        if ( _filterId >= 0 )
        {
            anyHolder.value = valueMap_.get( _filterId );

            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean match_structured( StructuredEvent structuredEvent,
                                     AnyHolder anyHolder )
        throws UnsupportedFilterableData
    {
        int _filterId = filterImpl_.match_structured_internal( structuredEvent );

        if ( _filterId >= 0 )
        {
            anyHolder.value = valueMap_.get( _filterId );

            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean match_typed( Property[] propertyArray,
                                AnyHolder anyHolder )
        throws UnsupportedFilterableData
    {
        throw new NO_IMPLEMENT();
    }
}
