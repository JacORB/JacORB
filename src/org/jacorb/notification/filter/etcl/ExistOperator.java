package org.jacorb.notification.filter.etcl;

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

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.filter.EvaluationException;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;
import org.jacorb.notification.filter.EvaluationResult;

/** A simple node to represent EXIST operation */
public class ExistOperator extends AbstractTCLNode {

    public ExistOperator(Token tok) {
        super(tok);
    }

    public String toString() {
        return "exist";
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws EvaluationException {

        //        Message _event = context.getCurrentMessage();

        switch(left().getType()) {
        case IDENTIFIER:
            break;
        case DOLLAR:
            ETCLComponentName _op = (ETCLComponentName)left();
            try {
                _op.evaluate(context);
                return EvaluationResult.BOOL_TRUE;
            } catch (EvaluationException e) {
                return EvaluationResult.BOOL_FALSE;
            }
            //      return _event.testExists(context,_op);
        }
        throw new RuntimeException();
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitor.visitExist(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitExist(this);
        left().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        visitor.visitExist(this);
    }
}
