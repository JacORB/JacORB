/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.*;
import java.util.*;
/**
 * This is the current object for the portable
 * interceptors. It is merely a slot table, but
 * can be bound to a thread scope.
 *
 * See PI Spec p. 6-55ff
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class PICurrentImpl extends org.omg.CORBA.LocalObject
  implements org.omg.PortableInterceptor.Current{

  private Any[] m_slots = null;
  private ORB m_orb = null;
  
  /**
   * Create an empty current object.
   * All slots will contain an empty Any.
   */
  public PICurrentImpl(ORB orb, int no_of_anys) {
    m_orb = orb;
    m_slots = new Any[no_of_anys];

    for(int _i = 0; _i < m_slots.length; _i++)
      m_slots[_i] = m_orb.create_any();
  }

  /**
   * Make a deep copy of an existing PICurrent.
   */
  public PICurrentImpl(PICurrentImpl source){
    m_orb = source.m_orb;
    m_slots = new Any[source.m_slots.length];

    for(int _i = 0; _i < m_slots.length; _i++){
      m_slots[_i] = m_orb.create_any();
      ((org.jacorb.orb.Any) m_slots[_i]).insert_object(source.m_slots[_i].type(),
						  ((org.jacorb.orb.Any) source.m_slots[_i]).value());
    }
  }
    
  // implementation of org.omg.PortableInterceptor.CurrentOperations interface
  public Any get_slot(int id) throws InvalidSlot {
    if ((id >= m_slots.length) || (id < 0))
      throw new InvalidSlot();
    
    return (Any) m_slots[id];
  }
  
  public void set_slot(int id, Any data) throws InvalidSlot {
    if ((id >= m_slots.length) || (id < 0))
      throw new InvalidSlot();
    
    m_slots[id] = data;
  }
} // PICurrentImpl






