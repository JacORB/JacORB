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
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.jacorb.notification.NotificationEvent;

/** A simple node to represent DEFAULT operation */
public class DefaultOperator extends TCLNode {

    public DefaultOperator(Token tok) {
	super(tok);
	setName("DefaultOperator");
    }

    public String toString() {
	return "default";
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {
	
	NotificationEvent _event = context.getEvent();
	EvaluationResult _r = _event.hasDefault((ComponentOperator)left());
	return _r;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitDefault(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitDefault(this);
	left().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	visitor.visitDefault(this);
    }
}
