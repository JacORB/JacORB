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

/** A simple node to represent NEQ operation */
public class NeqOperator extends TCLNode {

    public NeqOperator(Token tok) {
	super(tok);
    }

    public EvaluationResult evaluate(EvaluationContext context) 
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
	       EvaluationException {

	EvaluationResult _left, _right;
	_left = left().evaluate(context);
	_right = right().evaluate(context);

	int _comp = _left.compareTo(_right);

	if (_comp == 0 ) {
	    return EvaluationResult.BOOL_FALSE;
	}
	return EvaluationResult.BOOL_TRUE;
    }

    public String toString() {
	return "!=";
    }

    public String getName() {
	return getClass().getName();
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }
}
