package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent DOT operation */
public class DotOperator extends TCLNode {

    DotOperator() {
	super();
	setName("DotOperator");
    }

    public DotOperator(Token tok) {
	super(tok);
	setName("DotOperator");
    }

    public String toString() {
	return ".";
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	visitor.visitDot(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getNextSibling()).acceptInOrder(visitor);
	visitor.visitDot(this);

    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitDot(this);
	((TCLNode)getNextSibling()).acceptPreOrder(visitor);
    }
}
