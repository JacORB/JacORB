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

/** A simple node to represent GT operation */
public class GtOperator extends TCLNode {

    public GtOperator(Token tok) {
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

	if (_comp == -1 || _comp == 0) {
	    return EvaluationResult.BOOL_FALSE;
	}
	return EvaluationResult.BOOL_TRUE;
    }

    public String toString() {
	return ">";
    }

    public String getName() {
	return "GtOperator";
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitGt(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitGt(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitGt(this);
    }
}
