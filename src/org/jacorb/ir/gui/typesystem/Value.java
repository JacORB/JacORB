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
package org.jacorb.ir.gui.typesystem;


/**
 *  The interface of our representation of value types.
 */
public interface Value 
{
    /**
     *  Return the concrete base value of this value, or null
     *  if this base value has no base value.
     */
    public Value getBaseValue();

    /**
     * Returns all value members defined here, including value members from
     * the base value.
     */
    public TypeSystemNode[] getAllMembers();

    /**
     * Returns all fields defined here, including fields from
     * the base value and interfaces.
     */
    public TypeSystemNode[] getAllFields();

    /**
     * Returns all operations defined here, including operations from
     * the base value and interfaces, but excluding initializers.
     */ 
    public TypeSystemNode[] getAllOperations();

    /**
     * Return an array of the interfaces that this value implements.
     */
    public Interface[] getInterfaces();
}
