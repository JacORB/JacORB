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

/** A simple node to represent DIV operation */
public class DivOperator extends TCLNode {

    public DivOperator(Token tok) {
	super(tok);
    }

    public String toString() {
	return " /";
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
	       EvaluationException {

	EvaluationResult _res = new EvaluationResult();
	EvaluationResult _left, _right;
	_left = left().evaluate(context);
	_right = right().evaluate(context);

	if (_left.isFloat() ||
	    _right.isFloat()) {

	    float _l, _r;
	    _l = _left.getFloat();
	    _r = _right.getFloat();

	    _res.setFloat(_l / _r);
	} else {

	    int _l, _r;
	    _l = left().evaluate(context).getInt();
	    _r = right().evaluate(context).getInt();
	    _res.setInt(_l / _r);
	}
	return _res;
    }

    public void accept(TCLVisitor visitor) throws VisitorException {
	visitor.visitDiv(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitDiv(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitDiv(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitDiv(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }

    public String getName() {
	return "DivOperator";
    }
}
