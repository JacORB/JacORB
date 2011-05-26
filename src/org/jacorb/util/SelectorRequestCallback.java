/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2008 Gerald Brose.
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

public abstract class SelectorRequestCallback {

  /**
   * Requester overrides this method to do intended activity.
   * This is non-blocking api so keep your callback activities short; do IO, don't wait here.
   * Before doubng anything check request status.
   * The callback can happen in user or selector thread.
   * If request needs to remain on top of requests stack for this action
   *  simply return true;
   * A return value of false will cleanup request from Selector pool.
  */
  protected abstract boolean call (SelectorRequest action);
}
