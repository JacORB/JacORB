package org.jacorb.notification.node;

abstract public class TCLVisitor {

    public void visitPlus(PlusOperator plus) throws VisitorException {
    }

    public void visitAnd(AndOperator and) throws VisitorException {
    }

    public void visitMinus(MinusOperator minus) throws VisitorException {
    }

    public void visitDiv(DivOperator div) throws VisitorException {
    }

    public void visitMult(MultOperator mult) throws VisitorException {
    }

    public void visitBool(BoolValue bool) throws VisitorException {
    }

    public void visitNumber(NumberValue number) throws VisitorException {
    }

    public void visitIdent(IdentValue ident) throws VisitorException {
    }

    public void visitSubstr(SubstrOperator substr) throws VisitorException {
    }

    public void visitString(StringValue string) throws VisitorException {
    }

    public void visitDot(DotOperator dot) throws VisitorException {}

    public void visitComponent(ComponentOperator component)
	throws VisitorException {}

    public void visitNot(NotOperator not) throws VisitorException {}

    public void visitOr(OrOperator or) throws VisitorException {}

    public void visitEq(EqOperator or) throws VisitorException {}

    public void visitLt(LtOperator lt) throws VisitorException {}

    public void visitGt(GtOperator gt) throws VisitorException {}

    public void visitArray(ArrayOperator array) throws VisitorException {}

    public void visitAssoc(AssocOperator assoc) throws VisitorException {}

    public void visitComponentPosition(ComponentPositionOperator compPos) throws VisitorException {}

    public void visitUnionPosition(UnionPositionOperator unionPos) throws VisitorException {}

    public void visitImplicit(ImplicitOperatorNode operator) throws VisitorException {}

    public void visitExist(ExistOperator exist) throws VisitorException {}

    public void visitDefault(DefaultOperator defaultOp) throws VisitorException {}
}
