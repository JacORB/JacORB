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

/** A simple node to represent MINUS operation */
public class MinusOperator extends TCLNode {

    boolean unary_;
    static final String NAME = "MinusOperator";

    public String getName() {
	return NAME;
    }

    public MinusOperator(Token tok) {
	super(tok);
    }

    public void setType(int type) {
	unary_ = type == UNARY_MINUS;
    }

    public String toString() {
	return " -";
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
	       EvaluationException {

	EvaluationResult _ret = new EvaluationResult();
	EvaluationResult _left = left().evaluate(context);
	EvaluationResult _right = null;

	if (!unary_) {
	  _right  = right().evaluate(context);
	}

	if (unary_ && _left.isFloat()) {
	    _ret.setFloat(- _left.getFloat() );
	} else if (unary_ && !_left.isFloat()) {
	    _ret.setInt( - _left.getInt() );
	} else if (_left.isFloat() ||
		   _right.isFloat()) {

	    float _l, _r;
	    _l = _left.getFloat();
	    _r = _right.getFloat();
	    _ret.setFloat(_l - _r);
	} else {
	    int _l, _r;
	    _l = left().evaluate(context).getInt();
	    _r = right().evaluate(context).getInt();
	    _ret.setInt(_l - _r);
	}
	return _ret;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitMinus(this);
	if (!unary_) {
	    right().acceptInOrder(visitor);
	}
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	if (!unary_) {
	    right().acceptPostOrder(visitor);
	}
	visitor.visitMinus(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitMinus(this);
	left().acceptPreOrder(visitor);
	if (!unary_) {
	    right().acceptPreOrder(visitor);
	}
    }

}
