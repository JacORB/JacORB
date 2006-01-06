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

package org.jacorb.notification.interfaces;

/**
 * TODO find a better name for this interface
 * 
 * objects implementing this interface allow other Disposables to be
 * registered. as the main object is disposed, it will also invoke dispose
 * on all registered Disposables.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface NotifyingDisposable extends Disposable
{
    /**
     * the hooks registered by this method will be run when dispose is called.
     */
    void registerDisposable(Disposable disposable);
}
