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

/** A simple node to represent LTE operation */
public class LteOperator extends TCLNode {

    public LteOperator(Token tok) {
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

	if (_comp == 1 ) {
	    return EvaluationResult.BOOL_FALSE;
	}
	return EvaluationResult.BOOL_TRUE;
    }

    public String toString() {
	return "<=";
    }

    static final String NAME = "LteOperator";
    public String getName() {
	return NAME;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {

    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }
}
