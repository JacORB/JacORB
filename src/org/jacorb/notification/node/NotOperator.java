package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent NOT operation */
public class NotOperator extends TCLNode {
    public NotOperator(Token tok) {
	super(tok);
    }

    public String toString() {
	return " not";
    }

    public boolean checkOperands() {
	return true;
    }

    public EvaluationResult evaluate(EvaluationContext context) 
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
	       EvaluationException {

	EvaluationResult _eval = left().evaluate(context);
	boolean _b = _eval.getBool();
	return (_b ? EvaluationResult.BOOL_FALSE : EvaluationResult.BOOL_TRUE);
    }

    static final String NAME = "NotOperator";
    public String getName() {
	return NAME;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitNot(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitNot(this);
	left().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitNot(this);
    }
}
