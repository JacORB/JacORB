package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import java.io.Writer;
import java.io.IOException;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;


/** A simple node to represent an INT */
public class NumberValue extends TCLNode {
    private Double  number_;

    Double getNumber() {
	return number_;
    }

    public String getName() {
	return getClass().getName();
    }

    public NumberValue(Token tok) {
	super(tok);
	number_ = new Double(tok.getText());
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode {

	EvaluationResult _res = new EvaluationResult();
	switch(getType()) {
	case NUM_FLOAT:
	    _res.setFloat(number_);
	    break;
	default:
	    _res.setInt(number_);
	    break;
	}
	return _res;
    }

    public String toString() {
	switch(getType()) {
	case NUM_FLOAT:
	    return "" + number_.floatValue();
	default:
	    return "" + number_.longValue();
	}
    }

    public boolean isStatic() {
	return true;
    }

    public boolean isNumber() {
	return true;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitNumber(this);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitNumber(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitNumber(this);
    }
}
