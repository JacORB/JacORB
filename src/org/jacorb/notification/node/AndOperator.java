package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent AND operation */
public class AndOperator extends TCLNode {

    public AndOperator(Token tok) {
	super(tok);
	setKind(TCKind.tk_boolean);
    }

    public String toString() {
	return "and";
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	boolean _l, _r;

	_l = left().evaluate(context).getBool();
	if (!_l) {
	    return EvaluationResult.BOOL_FALSE;
	}

	_r = right().evaluate(context).getBool();

	return (_r ? EvaluationResult.BOOL_TRUE : EvaluationResult.BOOL_FALSE);
    }

    public boolean isStatic() {
	return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitAnd(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitAnd(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitAnd(this);
    }

    public String getName() {
	return NAME;
    }

    static final String NAME = "AndOperator";
}
