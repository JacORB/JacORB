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
package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.EvaluationContext;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent Assoc operation */
public class AssocOperator extends TCLNode {

    public AssocOperator(Token tok) {
	super(tok);
	setName("AssocOperator");
	assocName_ = tok.getText();
    }

    String assocName_;

    public String getAssocName() {
	return assocName_;
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       DynamicTypeException,
	       EvaluationException {

	return null;
    }

    public String toString() {
	return "{" + assocName_ + "}";
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	visitor.visitAssoc(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {

    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }
}
