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

/** A simple node to represent Assoc operation */
public class AssocOperator extends TCLNode {

    public AssocOperator(Token tok) {
	super(tok);
	setName("AssocOperator");
	assocName_ = tok.getText();
    }

    String assocName_;

    public String getAssocName() {
	return assocName_;
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       DynamicTypeException,
	       EvaluationException {

	return null;
    }

    public String toString() {
	return "{" + assocName_ + "}";
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	visitor.visitAssoc(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {

    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
    }
}
