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

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventHeader;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.IdentValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.node.DynamicTypeException;

/**
 * Adapt an Any to the NotificationEvent Interface.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class NotificationAnyEvent extends NotificationEvent
{

    private static final Property[] sFilterableData;
    private static final EventHeader sEventHeader;
    private static final String sAnyKey =
        FilterUtils.calcConstraintKey( "", "%ANY" );

    static {
        EventType _type = new EventType( "", "%ANY" );
        FixedEventHeader _fixed = new FixedEventHeader( _type, "" );
        Property[] _variable = new Property[ 0 ];
        sEventHeader = new EventHeader( _fixed, _variable );
        sFilterableData = new Property[ 0 ];
    }

    ////////////////////////////////////////

    /**
     * the wrapped value
     */
    private Any anyValue_;

    /**
     * the wrapped Any converted to a StructuredEvent
     */
    private StructuredEvent structuredEventValue_;

    ////////////////////////////////////////

    NotificationAnyEvent( ApplicationContext appContext )
    {
        super( appContext );
    }

    ////////////////////////////////////////

    public void setAny( Any any )
    {
        anyValue_ = any;
    }

    public synchronized void reset()
    {
        super.reset();
        anyValue_ = null;
        structuredEventValue_ = null;
    }

    public EventTypeIdentifier getEventTypeIdentifier()
    {
        return null;
    }

    public int getType()
    {
        return TYPE_ANY;
    }

    public Any toAny()
    {
        return anyValue_;
    }

    public StructuredEvent toStructuredEvent()
    {
        // the conversion should only be done once !

        if ( structuredEventValue_ == null )
        {
            synchronized ( this )
            {
                if ( structuredEventValue_ == null )
                {
                    structuredEventValue_ = new StructuredEvent();
                    structuredEventValue_.header = sEventHeader;
                    structuredEventValue_.filterable_data = sFilterableData;
                    structuredEventValue_.remainder_of_body = anyValue_;
                }
            }
        }

        return structuredEventValue_;
    }

    public String getConstraintKey()
    {
        return sAnyKey;
    }

    public EvaluationResult testExists( EvaluationContext evaluationContext,
                                        ComponentName op )
    throws EvaluationException
    {

        try
        {
            evaluate( evaluationContext, op );
            return EvaluationResult.BOOL_TRUE;
        }
        catch ( EvaluationException e )
        {
            return EvaluationResult.BOOL_FALSE;
        }
    }

    public EvaluationResult hasDefault( EvaluationContext evaluationContext,
                                        ComponentName op )
    throws EvaluationException
    {

        try
        {
            EvaluationResult _er = evaluate( evaluationContext, op );
            Any _any = _er.getAny();

            if ( evaluationContext.getDynamicEvaluator().hasDefaultDiscriminator( _any ) )
            {
                return EvaluationResult.BOOL_TRUE;
            }
            else
            {
                return EvaluationResult.BOOL_FALSE;
            }
        }
        catch ( BadKind bk )
        {
            throw NotificationEventUtils.getException( bk );
        }
    }

    public EvaluationResult evaluate( EvaluationContext evaluationContext, ComponentName op )
    throws EvaluationException
    {

        try
        {
            TCLNode _left = ( TCLNode ) op.left();
            Any _res;
            EvaluationResult _ret = null;

            if ( _left == null )
            {
                return evaluationContext.getResultExtractor().extractFromAny( anyValue_ );
            }

            switch ( _left.getType() )
            {

            case TCLNode.IDENTIFIER:
                IdentValue _iv = ( IdentValue ) _left;

                _ret = NotificationEventUtils.evaluateShorthand( evaluationContext,
                        anyValue_,
                        op,
                        _iv );

                break;

            case TCLNode.DOT:
                _ret = NotificationEventUtils.evaluateComponent( evaluationContext,
                        anyValue_,
                        op );

                break;

            default:
                throw new RuntimeException();
            }

            return _ret;
        }
        catch ( TypeMismatch tm )
        {
            throw NotificationEventUtils.getException( tm );
        }
        catch ( InconsistentTypeCode itc )
        {
            throw NotificationEventUtils.getException( itc );
        }
        catch ( InvalidValue iv )
        {
            throw NotificationEventUtils.getException( iv );
        }
        catch ( DynamicTypeException d )
        {
            throw NotificationEventUtils.getException( d );
        }
    }
}
