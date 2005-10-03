/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2004 Thomas Leineweber.
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
 * This Exception will be thrown when there is no Helper class
 * available for a specific CORBA type. It has no methods or fields
 * as it is used only as a kind of marker.
 * 
 * @author Thomas Leineweber
 * @version $Id$
 */


public class NoHelperException extends Exception {

}
