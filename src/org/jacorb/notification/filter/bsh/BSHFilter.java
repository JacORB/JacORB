package org.jacorb.notification.filter.bsh;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.Date;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.AbstractFilter;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.interfaces.EvaluationContextFactory;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.PortableServer.POA;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BSHFilter extends AbstractFilter
{
    public static final String CONSTRAINT_GRAMMAR = "BSH";

    public BSHFilter(Configuration config, EvaluationContextFactory evaluationContextFactory,
            MessageFactory messageFactory, ORB orb, POA poa) throws ConfigurationException
    {
        super(config, evaluationContextFactory, messageFactory, orb, poa);
    }

    public String constraint_grammar()
    {
        return CONSTRAINT_GRAMMAR;
    }

    public FilterConstraint newFilterConstraint(ConstraintExp constraintExp)
    {
        return new BSHFilterConstraint(constraintExp);
    }

    private static class BSHFilterConstraint implements FilterConstraint
    {
        private final String constraint_;

        BSHFilterConstraint(ConstraintExp constraintExp)
        {
            constraint_ = constraintExp.constraint_expr;
        }

        public EvaluationResult evaluate(EvaluationContext context, Message message)
                throws EvaluationException
        {
            try
            {
                Interpreter _interpreter = new Interpreter();

                // TODO import useful stuff
                // predefine useful functions?
                _interpreter.eval("import org.omg.CORBA.*;");

                _interpreter.set("event", message.toAny());
                _interpreter.set("date", new Date());
                _interpreter.set("constraint", constraint_);
                Object _result = _interpreter.eval(constraint_);

                if (_result == null)
                {
                    return EvaluationResult.BOOL_FALSE;
                }

                if (_result instanceof Boolean)
                {
                    if (_result.equals(Boolean.TRUE))
                    {
                        return EvaluationResult.BOOL_TRUE;
                    }

                    return EvaluationResult.BOOL_FALSE;
                }

                if (_result instanceof String)
                {
                    if ("".equals(_result))
                    {
                        return EvaluationResult.BOOL_FALSE;
                    }
                    return EvaluationResult.BOOL_TRUE;
                }

                return EvaluationResult.BOOL_TRUE;
            } catch (EvalError e)
            {
                throw new EvaluationException(e);
            }
        }
    }
}