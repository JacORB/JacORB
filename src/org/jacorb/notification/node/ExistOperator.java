package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.NotificationEvent;

/** A simple node to represent EXIST operation */
public class ExistOperator extends TCLNode {

    public ExistOperator(Token tok) {
	super(tok);
    }

    public String toString() {
	return "exist";
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	NotificationEvent _event = context.getEvent();

	switch(left().getType()) {
	case IDENTIFIER:
	    break;
	case DOLLAR:
	    ComponentOperator _op = (ComponentOperator)left();
	    return _event.testExists(_op);
	}
	throw new RuntimeException();
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitExist(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitExist(this);
	left().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	visitor.visitExist(this);
    }
}
