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

public class StringValue extends TCLNode {

    String value_;

    public StringValue(Token tok) {
	super(tok);
	setKind(TCKind.tk_string);
	value_ = tok.getText();
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }

    public String toString() {
	return "'" + value_ + "'";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode {
	EvaluationResult _res = new EvaluationResult();
	_res.setString(value_);

	return _res;
    }

    public boolean isNumber() {
	return (value_.length() == 1);
    }

    public boolean isStatic() {
	return true;
    }

    public boolean isString() {
	return true;
    }

    public String getName() {
	return "StringValue";
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }
}
