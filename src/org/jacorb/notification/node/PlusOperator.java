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

/** A simple node to represent PLUS operation */
public class PlusOperator extends TCLNode {
    boolean unary_;

    public PlusOperator(Token tok) {
	super(tok);
    }

    public void setType(int type) {
	unary_ = type == UNARY_PLUS;
    }

    public String getName() {
	return "PlusOperator";
    }

    public String toString() {
	return " +";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode, EvaluationException  {
	EvaluationResult _left = left().evaluate(context);
	if (unary_) {
	    return _left;
	}
	EvaluationResult _right = right().evaluate(context);
	EvaluationResult _ret = new EvaluationResult();

	if (_left.isFloat() ||
	    _right.isFloat() ) {

	    float _l, _r;
	    _l = _left.getFloat();
	    _r = _right.getFloat();
	    _ret.setFloat(_l + _r);
	} else {
	    int _l, _r;
	    _l = _left.getInt();
	    _r = _right.getInt();
	    _ret.setInt(_l + _r);
	}
	return _ret;
    }

    public boolean isStatic() {
	return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitPlus(this);
	if (!unary_) {
	    right().acceptInOrder(visitor);
	}
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	if (!unary_) {
	    right().acceptPostOrder(visitor);
	}
	visitor.visitPlus(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitPlus(this);
	left().acceptPreOrder(visitor);
	if (!unary_) {
	    right().acceptPreOrder(visitor);
	}
    }
}
