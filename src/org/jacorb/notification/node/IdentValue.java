package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

public class IdentValue extends TCLNode {

    String value_;

    public IdentValue(Token tok) {
	super(tok);
	setKind(null);
	value_ = tok.getText();
	setName("IdentValue");
    }

    public String getIdentifier() {
	return value_;
    }

    public String toString() {
	return value_;
    }

    public boolean isStatic() {
	return true;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitIdent(this);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitIdent(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitIdent(this);
    }
}
