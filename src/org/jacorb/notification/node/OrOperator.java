package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent OR operation */
public class OrOperator extends TCLNode {

    public OrOperator(Token tok) {
	super(tok);
	setKind(TCKind.tk_boolean);
    }

    public String toString() {
	return "or";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
    EvaluationException {
	boolean _l, _r;

	_l = left().evaluate(context).getBool();
	if (_l) {
	    return EvaluationResult.BOOL_TRUE;
	} else {
	    _r = right().evaluate(context).getBool();

	    if (_r) {
		return EvaluationResult.BOOL_TRUE;
	    }
	    return EvaluationResult.BOOL_FALSE;
	}
    }

    static final String NAME = "OrOperator";
    public String getName() {
	return NAME;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitOr(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitOr(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitOr(this);
    }
}
