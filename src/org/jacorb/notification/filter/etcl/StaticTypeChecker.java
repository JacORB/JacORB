package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StaticTypeChecker
    extends AbstractTCLVisitor
    implements TCLParserTokenTypes
{
    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    ////////////////////////////////////////

    public void check(AbstractTCLNode rootNode) throws StaticTypeException
    {
        try
        {
            rootNode.acceptPostOrder(this);
        }
        catch (VisitorException e)
        {
            throw new StaticTypeException(e.getMessage());
        }
    }


    static void checkBinaryNumaryOperatorNode(AbstractTCLNode node)
        throws StaticTypeException
    {
        if (node.isStatic())
        {
            if (node.left().isNumber() && node.right().isNumber())
            {
                return;
            }
            throw new StaticTypeException("num or float or identifier (or bool) excepted)");
        }
        else
        {
            return;
        }
    }


    static void checkCMPNode(AbstractTCLNode node) throws StaticTypeException
    {
        //      if (_leftKind.equals(TCKind.tk_boolean) &&
        //          _rightKind.equals(TCKind.tk_boolean)) {
        //          return;
        //      }

        //      if (_leftKind.equals(TCKind.tk_string) &&
        //          _rightKind.equals(TCKind.tk_string)) {
        //          return;
        //      }

        //      if ((_leftKind.equals(TCKind.tk_float) ||
        //           _leftKind.equals(TCKind.tk_long) ||
        //           _leftKind.equals(TCKind.tk_boolean))
        //          &&
        //          (_rightKind.equals(TCKind.tk_float) ||
        //           _rightKind.equals(TCKind.tk_long) ||
        //           _rightKind.equals(TCKind.tk_boolean))) {
        //          return;
        //      }

        //      throw new StaticTypeException("incompatible operands");
    }


    public void visitGt(GtOperator n) throws VisitorException
    {
        checkCMPNode(n);
    }


    public void visitPlus(PlusOperator n) throws VisitorException
    {
        checkBinaryNumaryOperatorNode(n);
    }


    public void visitMinus(MinusOperator node) throws VisitorException
    {
        checkBinaryNumaryOperatorNode(node);
    }


    public void visitDiv(DivOperator node) throws VisitorException
    {
        checkBinaryNumaryOperatorNode(node);
    }


    public void visitMult(MultOperator node) throws VisitorException
    {
        checkBinaryNumaryOperatorNode(node);
    }


    public void visitSubstr(SubstrOperator node) throws VisitorException
    {
        if (node.isStatic())
        {
            if (node.left().isString() && node.right().isString())
            {
                return;
            }
            throw new StaticTypeException("~ Operator expects 2 Strings");
        }
    }


    public void visitAnd(AndOperator and) throws VisitorException
    {
        if (and.isStatic())
        {
            if (and.left().isBoolean() && and.right().isBoolean())
            {
                return;
            }
            throw new StaticTypeException("bool value expected");
        }
    }

}
