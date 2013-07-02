/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 */

package org.jacorb.test.idl.bugjac144;

import java.io.PrintWriter;
import java.util.List;
import org.jacorb.idl.ObjectCachePlugin;
import org.jacorb.idl.TypeDeclaration;

/**
 * @author Alphonse Bendt
 */
public class BugJac144ObjectCachePlugin implements ObjectCachePlugin
{
    public void printCheckinHelper(PrintWriter ps, TypeDeclaration decl)
    {
    }

    public void printCheckout(PrintWriter ps, String className,
            String variableName)
    {
        ps.println(className + " " + variableName + " = new " + className + "();");
    }

    public void printPostMemberRead(PrintWriter ps, TypeDeclaration decl,
            String variableName)
    {
    }

    public void printPostParamRead(PrintWriter ps, List paramDecls)
    {
    }

    public void printPreMemberRead(PrintWriter ps, TypeDeclaration decl)
    {
    }

    public void printPreParamRead(PrintWriter ps, List paramDecls)
    {
    }

    public void printSkeletonCheckin(PrintWriter ps, List paramDecls,
            String variablePrefix)
    {
    }
}
