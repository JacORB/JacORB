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

import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.node.AbstractTCLNode;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.AssocOperator;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.ImplicitOperator;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.jacorb.notification.node.NumberValue;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.util.Debug;

import org.omg.CORBA.Any;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MessageUtils
{
    static Logger logger_ = Debug.getNamedLogger( MessageUtils.class.getName() );

    public static EvaluationResult extractFromAny(AbstractTCLNode operator,
                                                  Any event,
                                                  EvaluationContext evaluationContext,
                                                  String rootName)
        throws EvaluationException
    {
        if ( logger_.isDebugEnabled() ) {
            logger_.debug("rootname: " + rootName);
            logger_.debug("value " + event);
        }

        EvaluationResult _ret = null;
        Any _result = null;
        AbstractTCLNode _operator = operator;
        Any _valueCursor = event;

        StringBuffer _path = new StringBuffer( rootName );

        while ( _operator != null )
        {
            _path.append(_operator.toString());

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "current path: " + _path.toString() );
                logger_.debug( "current operator is: " + _operator.toString() );
                logger_.debug( "current cursor: " + _valueCursor);
            }

            _result = evaluationContext.lookupAny( _path.toString() );

            if ( _result == null )
            {
                switch ( _operator.getType() )
                {
                    case AbstractTCLNode.DOT:
                        // skip
                        logger_.debug( "skip" );
                        break;

                    case AbstractTCLNode.UNION_POS:
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

                    case AbstractTCLNode.IDENTIFIER:
                        logger_.debug( "eval by identifier" );

                        String _identifer = ((IdentValue) _operator).getIdentifier();

                        _result =
                            evaluationContext.
                            getDynamicEvaluator().
                            evaluateIdentifier( _valueCursor, _identifer);

                        break;

                    case AbstractTCLNode.NUMBER:
                        logger_.debug( "eval by position" );

                        int _pos = ((NumberValue) _operator).getNumber().intValue();

                        _result =
                            evaluationContext.
                            getDynamicEvaluator().
                            evaluateIdentifier( _valueCursor, _pos);

                        break;

                    case AbstractTCLNode.IMPLICIT:
                        ImplicitOperator _op = ( ( ImplicitOperatorNode ) _operator ).getOperator();

                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( "Implicit Operator: " + _op );
                            logger_.debug( "Cursor => " + _valueCursor );
                        }

                        _result = _op.evaluateImplicit(evaluationContext, _valueCursor);

                        _ret = EvaluationResult.fromAny( _result );

                        _ret.addAny( _valueCursor );

                        if ( logger_.isDebugEnabled() )
                        {
                            logger_.debug( "result is " + _result );
                        }

                        return _ret;

                    case AbstractTCLNode.ARRAY:
                        int _arrayIndex = ((ArrayOperator) _operator).getArrayIndex();

                        _result = evaluationContext.
                                  getDynamicEvaluator().
                                  evaluateArrayIndex( _valueCursor, _arrayIndex);

                        break;

                    case AbstractTCLNode.ASSOC:
                        String _assocName = ((AssocOperator) _operator).getAssocName();

                        _result = evaluationContext.
                                  getDynamicEvaluator().
                                  evaluateNamedValueList( _valueCursor, _assocName);

                        break;

                    default:
                        throw new RuntimeException("unknown operator: "
                                                   + AbstractTCLNode.getNameForType(_operator.getType()));
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
            _operator = (AbstractTCLNode)_operator.getNextSibling();
        }

        // Create the EvaluationResult
        _ret = EvaluationResult.fromAny( _result );

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "extracted: " + _ret );
        }


        return _ret;
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

    /**
     * Provide a Uniform Mapping from domain_name and type_name to a
     * Key that can be used to put EventTypes into a Map.
     * if (d1 == d2) AND (t1 == t2) => calcConstraintKey(d1, t1) ==
     * calcConstraintKey(d2, t2).
     *
     * @param domain_name a <code>String</code> value
     * @param type_name a <code>String</code> value
     * @return an Unique Constraint Key.
     */
    public static String calcConstraintKey( String domain_name, String type_name )
    {
        StringBuffer _b = new StringBuffer( domain_name );
        _b.append( "__%%__" );
        _b.append( type_name );

        return _b.toString();
    }


}
