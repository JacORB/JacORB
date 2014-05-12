/*
 *        JacORB - a free Java ORB
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

package org.jacorb.util;

/**
 * Base class for the callback handlers to be suppied to the SelectorRequest
 * object. You supply an implementation of the call method.
 *
 * @author Ciju John <johnc@ociweb.com>
 */
public abstract class SelectorRequestCallback
{

    /**
     * called when the registered event occurs. It is called in the SelectorManager
     * thread, so it should be a non-blocking call. It should be used only to retrieve
     * or send whatever can be right away. If more needs to be done then a subsequent
     * call can handle that.
     * @param action is the SelectorRequest that triggered the callback
     * @returns true if more waiting is required, in which case the SelectorRequest will
     * stay in the pool. A return of fall indicates that the Selector request may be
     * cleaned up.
    */
    protected abstract boolean call (SelectorRequest action);
}
