package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent Array operation */
public class ArrayOperator extends TCLNode {

    public ArrayOperator(Token tok) {
	super(tok);
	arrayIndex_ = Integer.parseInt(tok.getText());
	setName("ArrayOperator");
    }

    int arrayIndex_;

    public int getArrayIndex() {
	return arrayIndex_;
    }

    public String toString() {
	return "[" + arrayIndex_ + "]";
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	visitor.visitArray(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {

    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }
}
