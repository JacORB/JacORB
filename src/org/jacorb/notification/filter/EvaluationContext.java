package org.jacorb.notification.filter;

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

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.AbstractMessage;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ArrayOperator;
import org.jacorb.notification.filter.etcl.AssocOperator;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.IdentValue;
import org.jacorb.notification.filter.etcl.ImplicitOperator;
import org.jacorb.notification.filter.etcl.ImplicitOperatorNode;
import org.jacorb.notification.filter.etcl.NumberValue;
import org.jacorb.notification.filter.etcl.UnionPositionOperator;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.util.AbstractPoolable;
import org.jacorb.notification.util.LogUtil;
import org.omg.CORBA.Any;

/**
 * @todo remove the static dependeny to package filter.etcl.
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationContext extends AbstractPoolable
{
    private final ETCLEvaluator dynamicEvaluator_;

    private Message message_;

    private final Map resultCache_;

    private final Map anyCache_;

    private final Logger logger_ = LogUtil.getLogger(getClass().getName());

    ////////////////////////////////////////

    public EvaluationContext(ETCLEvaluator evaluator)
    {
        dynamicEvaluator_ = evaluator;

        resultCache_ = new WeakHashMap();
        anyCache_ = new WeakHashMap();
    }

    ////////////////////////////////////////

    public void reset()
    {
        resultCache_.clear();
        anyCache_.clear();
    }

    public ETCLEvaluator getDynamicEvaluator()
    {
        return dynamicEvaluator_;
    }

    public Message getCurrentMessage()
    {
        return message_;
    }

    public void setCurrentMessage(Message message)
    {
        message_ = message;
    }

    public void storeResult(String name, EvaluationResult value)
    {
        resultCache_.put(name, value);
    }

    public EvaluationResult lookupResult(String name)
    {
        return (EvaluationResult) resultCache_.get(name);
    }

    public void eraseResult(String name)
    {
        resultCache_.remove(name);
    }

    public void storeAny(String name, Any any)
    {
        anyCache_.put(name, any);
    }

    public Any lookupAny(String name)
    {
        return (Any) anyCache_.get(name);
    }

    public void eraseAny(String name)
    {
        anyCache_.remove(name);
    }

    /**
     * resolve the RuntimeVariable (e.g. $curtime). then see if some more work has to be done (e.g.
     * $curtime._repos_id)
     */
    public EvaluationResult extractFromMessage(EvaluationResult evaluationResult,
            ComponentName componentName, RuntimeVariable runtimeVariable)
            throws EvaluationException
    {
        ETCLComponentName _componentName = (ETCLComponentName) componentName;

        if (_componentName.right() != null)
        {
            return extractFromAny(_componentName.right(), evaluationResult.getAny(),
                    runtimeVariable.toString());
        }

        return evaluationResult;
    }

    /**
     * fetch the values denoted by the provided ComponentName out of the Message.
     */
    public EvaluationResult extractFromMessage(AbstractMessage message,
            ComponentName componentName) throws EvaluationException
    {
        ETCLComponentName _componentName = (ETCLComponentName) componentName;

        return extractFromAny(_componentName.left(), message.toAny(), _componentName.toString());
    }

    private EvaluationResult extractFromAny(AbstractTCLNode expr, Any any, String rootName)
            throws EvaluationException
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("extractFromAny" + "\n\trootname=" + rootName + "\n\tvalue=" + any);
        }

        EvaluationResult _ret = null;
        Any _result = null;

        AbstractTCLNode _currentOperator = expr;
        Any _currentAny = any;

        StringBuffer _currentPath = new StringBuffer(rootName);

        while (_currentOperator != null)
        {
            _currentPath.append(_currentOperator.toString());

            if (logger_.isDebugEnabled())
            {
                logger_.debug("current path=" + _currentPath.toString());
                logger_.debug("current operator=" + _currentOperator.toString());
                logger_.debug("current any=" + _currentAny);
            }

            // lookup result in cache
            _result = lookupAny(_currentPath.toString());

            if (_result == null)
            // cache MISS
            {
                switch (_currentOperator.getType()) {
                case AbstractTCLNode.DOT:
                    // dots are skipped
                    break;

                case AbstractTCLNode.UNION_POS:
                    logger_.debug("evaluate union by position");
                    UnionPositionOperator _upo = (UnionPositionOperator) _currentOperator;

                    // default union
                    if (_upo.isDefault())
                    {
                        _result = getDynamicEvaluator().evaluateUnion(_currentAny);
                    }
                    else
                    {
                        _result = getDynamicEvaluator().evaluateUnion(_currentAny,
                                _upo.getPosition());
                    }

                    break;

                case AbstractTCLNode.IDENTIFIER:
                    String _identifer = ((IdentValue) _currentOperator).getIdentifier();

                    _result = getDynamicEvaluator().evaluateIdentifier(_currentAny, _identifer);

                    break;

                case AbstractTCLNode.NUMBER:
                    int _pos = ((NumberValue) _currentOperator).getNumber().intValue();

                    _result = getDynamicEvaluator().evaluateIdentifier(_currentAny, _pos);

                    break;

                case AbstractTCLNode.IMPLICIT:
                    ImplicitOperator _op = ((ImplicitOperatorNode) _currentOperator).getOperator();

                    if (logger_.isDebugEnabled())
                    {
                        logger_.debug(_op + " is an implict Operator");
                    }

                    _result = _op.evaluateImplicit(getDynamicEvaluator(), _currentAny);

                    _ret = EvaluationResult.fromAny(_result);

                    _ret.addAny(_currentAny);

                    if (logger_.isDebugEnabled())
                    {
                        logger_.debug("result=" + _result);
                    }

                    return _ret;

                case AbstractTCLNode.ARRAY:
                    int _arrayIndex = ((ArrayOperator) _currentOperator).getArrayIndex();

                    _result = getDynamicEvaluator().evaluateArrayIndex(_currentAny, _arrayIndex);

                    break;

                case AbstractTCLNode.ASSOC:
                    String _assocName = ((AssocOperator) _currentOperator).getAssocName();

                    _result = getDynamicEvaluator().evaluateNamedValueList(_currentAny, _assocName);

                    break;

                default:
                    throw new RuntimeException("unexpected operator: "
                            + AbstractTCLNode.getNameForType(_currentOperator.getType()));
                }
            }
            else
            {
                logger_.debug("Any Cache HIT");
            }

            if (_result != null)
            {
                storeAny(_currentPath.toString(), _result);
                _currentAny = _result;
            }
            _currentOperator = (AbstractTCLNode) _currentOperator.getNextSibling();
        }

        // Create the EvaluationResult
        _ret = EvaluationResult.fromAny(_result);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("extracted: " + _ret);
        }

        return _ret;
    }
}