/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.PrintWriter;
import java.io.Serializable;

public interface Operation
    extends Serializable
{
    /**
     * <code>name</code> gives the plain name of the operation
     * @return a <code>String</code> value
     */
    public String name();

    /**
     * <code>opName</code> gives the mangled name in case of attributes
     * (_get_, _set_).
     *
     * @return a <code>String</code> value
     */
    public String opName();


    /**
     * <code>printMethod</code> produces the method code for stubs.
     *
     * @param ps a <code>PrintWriter</code> value
     * @param classname a <code>String</code> value
     * @param is_local a <code>boolean</code> value
     * @param is_abstract a <code>boolean</code> value used by Interface to
     *        denote an abstract.
     */
    public void printMethod( PrintWriter ps, String classname, boolean is_local, boolean is_abstract );

    public void print_sendc_Method( PrintWriter ps, String classname );

    public String signature();


    /**
     * @param printModifiers whether "public abstract" should be added
     */
    void printSignature( PrintWriter ps, boolean printModifiers );

    void printSignature( PrintWriter ps );

    /**
     * Method code for skeletons
     * @param ps a <code>PrintWriter</code> value
     */

    void printDelegatedMethod( PrintWriter ps );

    void printInvocation( PrintWriter ps );

    void accept( IDLTreeVisitor visitor );

}





















