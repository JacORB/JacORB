package org.jacorb.notification.node;

import org.jacorb.notification.node.TCLNode;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.evaluate.EvaluationContext;
import antlr.Token;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * UnionPositionOperator.java
 *
 *
 * Created: Thu Sep 26 14:57:58 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

public class UnionPositionOperator extends TCLNode {

    int position_;
    boolean default_ = false;

    public UnionPositionOperator(Token token) {
	super(token);
	setName("UnionPositionOperator");
	setType(UNION_POS);
    }

    void setPosition(Double pos) {
	position_ = pos.intValue();
	default_ = false;
    }

    void setDefault() {
	default_ = true;
    }

    public boolean isDefault() {
	debug("isDefault " + default_);
	return default_;
    }

    public int getPosition() {
	assert(!default_);

	return position_;
    }

    public String toString() {
	return "<" + (default_? "default" : "" + position_) + ">";
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitUnionPosition(this);

	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptPreOrder(visitor);
	}
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptPostOrder(visitor);
	}

	visitor.visitUnionPosition(this);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitUnionPosition(this);

	if (hasNextSibling()) {
	    ((TCLNode)getNextSibling()).acceptInOrder(visitor);
	}
    }

}// UnionPositionOperator
