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

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.parser.TCLParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * PropertyShorthandNode.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PropertyShorthandNode extends AbstractTCLNode
{

    String value_;

    ComponentName shorthandVariableHeader_;
    ComponentName shorthandFilterableData_;
    ComponentName shorthandDefault_;
    ComponentName shorthandDefaultAny_;

    public PropertyShorthandNode(String value)
    {
        try {

            value_ = value;

            shorthandVariableHeader_ =
                (ComponentName)TCLParser.parse("$.header.variable_header(" + value + ")");

            shorthandVariableHeader_.acceptInOrder(new TCLCleanUp());

            shorthandFilterableData_ = (ComponentName)TCLParser.parse("$.filterable_data(" + value + ")");
            shorthandFilterableData_.acceptInOrder(new TCLCleanUp());

            shorthandDefault_ = (ComponentName)TCLParser.parse("$." + value );
            shorthandDefault_.acceptInOrder(new TCLCleanUp());

            shorthandDefaultAny_ = (ComponentName)TCLParser.parse("$(" + value + ")");
            shorthandDefaultAny_.acceptInOrder(new TCLCleanUp());

        } catch (TokenStreamException e) {
            logger_.fatalError("Exception during parse", e);
            throw new RuntimeException();
        } catch (RecognitionException e) {
            logger_.fatalError("Exception during parse", e);
            throw new RuntimeException();
        } catch (VisitorException e) {
            logger_.fatalError("Exception during parse", e);
            throw new RuntimeException();
        }

    }

    public EvaluationResult evaluate(EvaluationContext context) throws EvaluationException {

        Message _event = context.getNotificationEvent();
        EvaluationResult _res = null;

        try {
            _res = _event.extractVariableHeader(context,
                                                shorthandVariableHeader_,
                                                value_);

        } catch (EvaluationException e) {}

        if (_res == null) {
            try {
                _res = _event.extractFilterableData(context,
                                                    shorthandFilterableData_,
                                                    value_);
            } catch (EvaluationException e) {}

            if (_res == null) {

                _res = extractDefaultValue(context);

            }

            if (_res == null) {
                _res = extractDefaultAnyValue(context);
            }
        }

        return _res;
    }

    public EvaluationResult extractDefaultValue(EvaluationContext context) {
        try {
            return context.getNotificationEvent().extractValue(context, shorthandDefault_);
        } catch (Exception e) {
            return null;
        }
    }

    public EvaluationResult extractDefaultAnyValue(EvaluationContext context) {
        try {
            return context.getNotificationEvent().extractValue(context, shorthandDefaultAny_);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString() {
        return "PropertyShorthandNode: " + value_;
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        if (getFirstChild() != null) {
            ( ( AbstractTCLNode ) getFirstChild() ).acceptPostOrder( visitor );
        }
        //    visitor.visitComponent( this );
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        //        visitor.visitComponent( this );
        ( ( AbstractTCLNode ) getFirstChild() ).acceptPreOrder( visitor );
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        ( ( AbstractTCLNode ) getFirstChild() ).acceptInOrder( visitor );
        //        visitor.visitComponent( this );
    }
}
