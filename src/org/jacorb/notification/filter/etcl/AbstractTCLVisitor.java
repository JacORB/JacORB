package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

/**
 * Adapter Class to help creating a Visitor for TCL
 * Trees. Override the appropiate Methods.
 *
 * @version $Id$
 */

abstract public class AbstractTCLVisitor {

    public void visitPlus(PlusOperator plus) throws VisitorException
    {
    }

    public void visitAnd(AndOperator and) throws VisitorException
    {
    }

    public void visitMinus(MinusOperator minus) throws VisitorException
    {
    }

    public void visitDiv(DivOperator div) throws VisitorException
    {
    }

    public void visitMult(MultOperator mult) throws VisitorException
    {
    }

    public void visitBool(BoolValue bool) throws VisitorException
    {
    }

    public void visitNumber(NumberValue number) throws VisitorException
    {
    }

    public void visitIdent(IdentValue ident) throws VisitorException
    {
    }

    public void visitSubstr(SubstrOperator substr) throws VisitorException
    {
    }

    public void visitString(StringValue string) throws VisitorException
    {
    }

    public void visitDot(DotOperator dot) throws VisitorException
    {
    }

    public void visitComponent(ETCLComponentName component)
        throws VisitorException
    {
    }

    public void visitNot(NotOperator not) throws VisitorException
    {
    }

    public void visitOr(OrOperator or) throws VisitorException {
    }

    public void visitEq(EqOperator or) throws VisitorException {
    }

    public void visitNeq(NeqOperator or) throws VisitorException {
    }

    public void visitLt(LtOperator lt) throws VisitorException
    {
    }

    public void visitLte(LteOperator lt) throws VisitorException
    {
    }

    public void visitGt(GtOperator gt) throws VisitorException
    {
    }

    public void visitArray(ArrayOperator array) throws VisitorException
    {
    }

    public void visitAssoc(AssocOperator assoc) throws VisitorException
    {
    }

    public void visitComponentPosition(ComponentPositionOperator compPos)
        throws VisitorException
    {
    }

    public void visitUnionPosition(UnionPositionOperator unionPos)
        throws VisitorException
    {
    }

    public void visitImplicit(ImplicitOperatorNode operator) throws VisitorException
    {
    }

    public void visitExist(ExistOperator exist) throws VisitorException
    {
    }

    public void visitDefault(DefaultOperator defaultOp) throws VisitorException
    {
    }

    public void visitIn(InOperator in) throws VisitorException
    {
    }

    public void visitGteOperator(GteOperator o) throws VisitorException
    {
    }

    public void visitRuntimeVariable(RuntimeVariableNode r) throws VisitorException
    {
    }
}
