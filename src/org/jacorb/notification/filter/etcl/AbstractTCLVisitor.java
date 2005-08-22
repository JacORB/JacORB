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
 * Adapter Class to help creating a Visitor for TCL Trees. Override the appropiate Methods.
 * 
 * @version $Id$
 */

public abstract class AbstractTCLVisitor
{
    public void visitPlus(PlusOperator plus) throws VisitorException
    {
        // empty
    }

    public void visitAnd(AndOperator and) throws VisitorException
    {
        // empty
    }

    public void visitMinus(MinusOperator minus) throws VisitorException
    {
        // empty
    }

    public void visitDiv(DivOperator div) throws VisitorException
    {
        // empty
    }

    public void visitMult(MultOperator mult) throws VisitorException
    {
        // empty
    }

    public void visitBool(BoolValue bool) throws VisitorException
    {
        // empty
    }

    public void visitNumber(NumberValue number) throws VisitorException
    {
        // empty
    }

    public void visitIdent(IdentValue ident) throws VisitorException
    {
        // empty
    }

    public void visitSubstr(SubstrOperator substr) throws VisitorException
    {
        // empty
    }

    public void visitString(StringValue string) throws VisitorException
    {
        // empty
    }

    public void visitDot(DotOperator dot) throws VisitorException
    {
        // empty
    }

    public void visitComponent(ETCLComponentName component) throws VisitorException
    {
        // empty
    }

    public void visitNot(NotOperator not) throws VisitorException
    {
        // empty
    }

    public void visitOr(OrOperator or) throws VisitorException
    {
        // empty
    }

    public void visitEq(EqOperator or) throws VisitorException
    {
        // empty
    }

    public void visitNeq(NeqOperator or) throws VisitorException
    {
        // empty
    }

    public void visitLt(LtOperator lt) throws VisitorException
    {
        // empty
    }

    public void visitLte(LteOperator lt) throws VisitorException
    {
        // empty
    }

    public void visitGt(GtOperator gt) throws VisitorException
    {
        // empty
    }

    public void visitArray(ArrayOperator array) throws VisitorException
    {
        // empty
    }

    public void visitAssoc(AssocOperator assoc) throws VisitorException
    {
        // empty
    }

    public void visitUnionPosition(UnionPositionOperator unionPos) throws VisitorException
    {
        // empty
    }

    public void visitImplicit(ImplicitOperatorNode operator) throws VisitorException
    {
        // empty
    }

    public void visitExist(ExistOperator exist) throws VisitorException
    {
        // empty
    }

    public void visitDefault(DefaultOperator defaultOp) throws VisitorException
    {
        // empty
    }

    public void visitIn(InOperator in) throws VisitorException
    {
        // empty
    }

    public void visitGteOperator(GteOperator o) throws VisitorException
    {
        // empty
    }

    public void visitRuntimeVariable(RuntimeVariableNode r) throws VisitorException
    {
        // empty
    }
}