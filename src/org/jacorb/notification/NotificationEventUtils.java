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
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.AssocOperator;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.ImplicitOperator;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.notification.node.DomainNameShorthandNode;
import org.jacorb.notification.node.EventNameShorthandNode;
import org.jacorb.notification.node.TypeNameShorthandNode;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.node.NumberValue;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.jacorb.notification.node.DynamicTypeException;

/**
 * NotificationEventUtils.java
 *
 *
 * Created: Thu May 10 16:30:04 2001
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationEventUtils
{

    static Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor( NotificationEventUtils.class.getName() );

    private static Map shortHandOperators_ = new Hashtable();
    private static boolean error_;

    static 
    {
	
	TCLNode _root;

	_root = new DomainNameShorthandNode();
	shortHandOperators_.put( DomainNameShorthandNode.SHORT_NAME,
                                     _root );
	
	_root = new TypeNameShorthandNode();
	shortHandOperators_.put( TypeNameShorthandNode.SHORT_NAME,
				 _root );
	
	_root = new EventNameShorthandNode();
	shortHandOperators_.put( EventNameShorthandNode.SHORT_NAME,
				 _root );

    }

    static EvaluationResult evaluateShorthand( EvaluationContext evaluationContext,
					       Any event,
					       ComponentName comp,
					       IdentValue ident )
	throws InconsistentTypeCode,
	       TypeMismatch,
	       EvaluationException,
	       InvalidValue,
	       DynamicTypeException
    {
        EvaluationResult _ret = null;
        String _completePath = comp.getComponentName();
        _ret = evaluationContext.lookupResult( _completePath );

        if ( _ret == null )
        {
            ComponentName _expandedOperator =
                ( ComponentName ) shortHandOperators_.get( ident.getIdentifier() );

	    _ret = _expandedOperator.evaluate( evaluationContext );

            evaluationContext.storeResult( _completePath, _ret );
        }

        return _ret;
    }

    static EvaluationResult evaluateComponent( EvaluationContext evaluationContext,
					       Any event,
					       ComponentName comp )
	throws InconsistentTypeCode,
	       TypeMismatch,
	       EvaluationException,
	       InvalidValue
    {
        EvaluationResult _ret = null;
        String _completePath = comp.getComponentName();

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "evaluate Component: " + comp.toStringTree() );
            logger_.debug( "complete path is: " + _completePath );
        }

        _ret = evaluationContext.lookupResult( _completePath );

        if ( _ret == null )
        {
            Any _result = null;
            TCLNode _operator = ( TCLNode ) comp.left();
            Any _valueCursor = event;

            StringBuffer _path = new StringBuffer(comp.toString());
	    _path.append(_operator.toString());

            while ( _operator.hasNextSibling() )
            {
                _operator = ( TCLNode ) _operator.getNextSibling();

                _path.append(_operator.toString());

                if ( logger_.isDebugEnabled() )
                {
                    logger_.debug( "current path: " + _path.toString() );
                    logger_.debug( "next operator is:" + _operator.toString() );
                }

                _result = evaluationContext.lookupAny( _path.toString() );

                if ( _result == null )
                {
                    switch ( _operator.getType() )
                    {

                    case TCLNode.DOT:
                        // skip
                        logger_.debug( "skip" );
                        break;

                    case TCLNode.UNION_POS:
                        logger_.debug( "eval union by pos" );
                        UnionPositionOperator _upo = ( UnionPositionOperator ) _operator;

                        if ( _upo.isDefault() )
                        {
                            _result = 
				evaluationContext.getDynamicEvaluator().evaluateUnion( _valueCursor );
                        }
                        else
                        {
                            _result =
                                evaluationContext.
				getDynamicEvaluator().
				evaluateUnion( _valueCursor, _upo.getPosition() );
                        }

                        break;

                    case TCLNode.IDENTIFIER:
                        logger_.debug( "eval by identifier" );

			String _identifer = ((IdentValue) _operator).getIdentifier();

                        _result =
                            evaluationContext.
			    getDynamicEvaluator().
			    evaluateIdentifier( _valueCursor, _identifer);

                        break;

                    case TCLNode.NUMBER:
                        logger_.debug( "evaluateByPosition()" );

			int _pos = ((NumberValue) _operator).getNumber().intValue();

                        _result =
                            evaluationContext.
			    getDynamicEvaluator().
			    evaluateIdentifier( _valueCursor, _pos);

                        break;

                    case TCLNode.IMPLICIT:
                        logger_.debug( "evaluate implicit operator" );
                        ImplicitOperator _op = ( ( ImplicitOperatorNode ) _operator ).getOperator();

                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( "Operator is " + _op );
                            logger_.debug( "Cursor: " + _valueCursor );
                            logger_.debug( "DynamicEvaluator: " 
					   + evaluationContext.getDynamicEvaluator() );
                        }

                        _result = 
			    NotificationEventUtils.
			    evaluateImplicit( evaluationContext.getDynamicEvaluator(),
					      _op,
					      _valueCursor );

                        _ret = evaluationContext.getResultExtractor().extractFromAny( _result );

                        _ret.addAny( _valueCursor );

                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( "result is " + _result );
                        }

                        return _ret;

                    case TCLNode.ARRAY:
			int _arrayIndex = ((ArrayOperator) _operator).getArrayIndex();

                        _result = evaluationContext.
			    getDynamicEvaluator().
			    evaluateArrayIndex( _valueCursor, _arrayIndex);

                        break;

                    case TCLNode.ASSOC:
			String _assocName = ((AssocOperator) _operator).getAssocName();

                        _result = evaluationContext.
			    getDynamicEvaluator().
			    evaluateNamedValueList( _valueCursor, _assocName);

                        break;

                    default:

                        throw new RuntimeException("unknown operator");
                    }
                }
                else
                {
                    logger_.debug( "Any Cache HIT" );
                }

                if ( _result != null )
                {
                    evaluationContext.storeAny( _path.toString(), _result );
                    _valueCursor = _result;
                }
            }

            // Create the EvaluationResult
            _ret = evaluationContext.getResultExtractor().extractFromAny( _result );

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "extracted: " + _ret );
            }

            // Cache the EvaluationResult
            if ( _ret != null )
            {

                if ( logger_.isDebugEnabled() )
                {
                    logger_.debug( "Cache Result: " + _completePath + " => " + _ret );
                }

                evaluationContext.storeResult( _completePath, _ret );
            }
        }
        else
        {
            logger_.debug( "Result Cache HIT" );
        }

        return _ret;
    }

    static Any evaluateImplicit( DynamicEvaluator evaluator,
				 ImplicitOperator op,
				 Any value ) throws EvaluationException
    {

        try
        {
            if ( op == ImplicitOperatorNode.OPERATOR_DISCRIM )
            {
                return evaluator.evaluateDiscriminator( value );
            }
            else if ( op == ImplicitOperatorNode.OPERATOR_TYPE_ID )
            {
                return evaluator.evaluateTypeName( value );
            }
            else if ( op == ImplicitOperatorNode.OPERATOR_REPO_ID )
            {
                return evaluator.evaluateRepositoryId( value );
            }
            else if ( op == ImplicitOperatorNode.OPERATOR_LENGTH )
            {
                return evaluator.evaluateListLength( value );
            }
            else
            {
                throw getException();
            }
        }
        catch ( BadKind bk )
        {
            throw getException( bk );
        }
        catch ( InconsistentTypeCode itc )
        {
            throw getException( itc );
        }
    }

    public static EvaluationException getException( Exception e )
    {
        e.printStackTrace();
        return new EvaluationException();
    }

    public static EvaluationException getException()
    {
        return new EvaluationException();
    }

} // NotificationEventUtils
