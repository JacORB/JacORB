package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent Array operation */
public class ComponentPositionOperator extends TCLNode {

    public ComponentPositionOperator(Token tok) {
        super(tok);
	setName("ComponentPositonOperator");
        position_ = Integer.parseInt(tok.getText().substring(1));
    }

    int position_;

    public int getPosition() {
        return position_;
    }

    public String toString() {
        return "" + position_;
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	}
        visitor.visitComponentPosition(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptInOrder(visitor);
	}
        visitor.visitComponentPosition(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitComponentPosition(this);
	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptPreOrder(visitor);
	}
    }
}
