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
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

public class SubstrOperator extends BinaryOperator {

    static final String NAME = "SubstrOperator";

    public String getName() {
        return NAME;
    }

    public SubstrOperator(Token tok) {
        super(tok);
    }


    public String toString() {
        return " ~";
    }

    public EvaluationResult evaluate(EvaluationContext context, 
            EvaluationResult left, EvaluationResult right)
        throws EvaluationException {

        EvaluationResult _res;

        String _l, _r;

        _l = left.getString();
        _r = right.getString();

        int _idx = _r.indexOf(_l);

        if (_idx == -1) {
            _res = EvaluationResult.BOOL_FALSE;
        } else {
            _res = EvaluationResult.BOOL_TRUE;
        }

        return _res;
    }

    public boolean isStatic() {
        return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(AbstractTCLVisitor visitor)
        throws VisitorException {

        left().acceptInOrder(visitor);
        visitor.visitSubstr(this);
        right().acceptInOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor)
        throws VisitorException {

        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitor.visitSubstr(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor)
        throws VisitorException {

        visitor.visitSubstr(this);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }
}
