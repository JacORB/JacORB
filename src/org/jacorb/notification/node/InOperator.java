package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationContext;

/** A simple node to represent IN operation */
public class InOperator extends TCLNode {

    public InOperator(Token tok) {
	super(tok);
    }

    /** Compute value of subtree; this is heterogeneous part :) */
    public int value() {
	return 0;
    }

    public String toString() {
	return " in";
    }

    public String getName() {
	return getClass().getName();
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch {

	return null;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
    }

}
