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

public class BoolValue extends TCLNode {

    boolean value_;

    public BoolValue(Token tok) {
	super(tok);
	value_ = tok.getText().equals("TRUE");
	setKind(TCKind.tk_boolean);
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode {

	if (value_) {
	    return EvaluationResult.BOOL_TRUE;
	} else {
	    return EvaluationResult.BOOL_FALSE;
	}
    }

    public boolean isStatic() {
	return true;
    }

    public boolean isBoolean() {
	return true;
    }

    public String getName() {
	return "BoolValue";
    }

    public String toString() {
	return "" + value_;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitBool(this);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitBool(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitBool(this);
    }
}
