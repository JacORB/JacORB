package org.jacorb.notification.filter.etcl;

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

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

//import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class MessageUtils
{
    private MessageUtils() {}

//     static Logger logger_ = Debug.getNamedLogger( MessageUtils.class.getName() );


    public static EvaluationResult extractFromAny(AbstractTCLNode expr,
                                                  Any any,
                                                  EvaluationContext evaluationContext,
                                                  String rootName)
        throws EvaluationException
    {
//         if ( logger_.isDebugEnabled() ) {
//             logger_.debug("extractFromAny" +
//                           "\n\trootname=" + rootName +
//                           "\n\tvalue=" + any);
//         }

        EvaluationResult _ret = null;
        Any _result = null;

        AbstractTCLNode _currentOperator = expr;
        Any _currentAny = any;

        StringBuffer _currentPath = new StringBuffer( rootName );

        while ( _currentOperator != null )
            {
                _currentPath.append(_currentOperator.toString());

//                 if ( logger_.isDebugEnabled() )
//                     {
//                         logger_.debug( "current path=" + _currentPath.toString() );
//                         logger_.debug( "current operator=" + _currentOperator.toString() );
//                         logger_.debug( "current any=" + _currentAny);
//                     }

                // lookup result in cache
                _result = evaluationContext.lookupAny( _currentPath.toString() );

                if ( _result == null )
                    // cache MISS
                    {
                        switch ( _currentOperator.getType() )
                            {
                            case AbstractTCLNode.DOT:
                                // dots are skipped
                                break;

                            case AbstractTCLNode.UNION_POS:
//                                 logger_.debug( "evaluate union by position" );
                                UnionPositionOperator _upo = ( UnionPositionOperator ) _currentOperator;

                                // default union
                                if ( _upo.isDefault() )
                                    {
                                        _result =
                                            evaluationContext.getDynamicEvaluator().evaluateUnion( _currentAny );
                                    }
                                else
                                    {
                                        _result =
                                            evaluationContext.
                                            getDynamicEvaluator().
                                            evaluateUnion( _currentAny, _upo.getPosition() );
                                    }

                                break;

                            case AbstractTCLNode.IDENTIFIER:
//                                 logger_.debug( "evaluate struct by identifier" );

                                String _identifer = ((IdentValue) _currentOperator).getIdentifier();

                                _result =
                                    evaluationContext.
                                    getDynamicEvaluator().
                                    evaluateIdentifier( _currentAny, _identifer);

                                break;

                            case AbstractTCLNode.NUMBER:
//                                 logger_.debug( "evaluate struct by position" );

                                int _pos = ((NumberValue) _currentOperator).getNumber().intValue();

                                _result =
                                    evaluationContext.
                                    getDynamicEvaluator().
                                    evaluateIdentifier( _currentAny, _pos);

                                break;

                            case AbstractTCLNode.IMPLICIT:
                                ImplicitOperator _op =
                                    ( ( ImplicitOperatorNode ) _currentOperator ).getOperator();

//                                 if ( logger_.isDebugEnabled() )
//                                     {
//                                         logger_.debug( _op + " is an implict Operator" );
//                                     }

                                _result = _op.evaluateImplicit(evaluationContext, _currentAny);

                                _ret = EvaluationResult.fromAny( _result );

                                _ret.addAny( _currentAny );

//                                 if ( logger_.isDebugEnabled() )
//                                     {
//                                         logger_.debug( "result=" + _result );
//                                     }

                                return _ret;

                            case AbstractTCLNode.ARRAY:
                                int _arrayIndex = ((ArrayOperator) _currentOperator).getArrayIndex();

                                _result = evaluationContext.
                                    getDynamicEvaluator().
                                    evaluateArrayIndex( _currentAny, _arrayIndex);

                                break;

                            case AbstractTCLNode.ASSOC:
                                String _assocName = ((AssocOperator) _currentOperator).getAssocName();

                                _result = evaluationContext.
                                    getDynamicEvaluator().
                                    evaluateNamedValueList( _currentAny, _assocName);

                                break;

                            default:
                                throw new RuntimeException("unexpected operator: "
                                                           + AbstractTCLNode.getNameForType(_currentOperator.getType()));
                            }
                    }
                else
                    {
//                         logger_.debug( "Any Cache HIT" );
                    }

                if ( _result != null )
                    {
                        evaluationContext.storeAny( _currentPath.toString(), _result );
                        _currentAny = _result;
                    }
                _currentOperator = (AbstractTCLNode)_currentOperator.getNextSibling();
            }

        // Create the EvaluationResult
        _ret = EvaluationResult.fromAny( _result );

//         if ( logger_.isDebugEnabled() )
//             {
//                 logger_.debug( "extracted: " + _ret );
//             }

        return _ret;
    }


    public static EvaluationException getException( Exception e )
    {
        return new EvaluationException();
    }


    public static EvaluationException getException()
    {
        return new EvaluationException();
    }

}
