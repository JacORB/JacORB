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

package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.IORInfo;

/**
 * This class extends the standard defined interface of the IORInfo
 * object given to each IORInterceptor during creation of new IORs.
 * It provides convenience methods to modify the newly created IOR.

 * @author Marc Heide
 * @version $Id$
 */

public interface IORInfoExt extends IORInfo
{
   /**
    * This method adds a further profile to an IOR.
    * By using this method it is possible to append e.g. further IIOP
    * profiles. The added profile is marshalled after all profiles
    * already existing in profile list.
    * @param profile       the profile to add
    */
   public void add_profile(org.omg.ETF.Profile profile);

   /**
    * This method returns the number of profiles of the given type.
    * The returned value can be used to iterate over the existing
    * profiles of given type (get_profile()).
    * @param tag     profile tag, e.g. TAG_INTERNET_IOP.value
    * @return        number of profiles of given tag
    */
   public int get_number_of_profiles(int tag);

   /**
    * Returns the profile with the given tag at the given position.
    * Following rule must apply to parameter position:<p>
    * <code> 0 <= position < get_number_of_profiles(tag) </code><p>
    * @param tag        tag of profile, e.g. TAG_INTERNET_IOP.value
    * @param position   position in IOR
    * @return           profile
    * @exception       ArrayIndexOutOfBoundsException if position is
    *                   out of range
    */
   public org.omg.ETF.Profile get_profile(int tag, int position);

   /**
    * Returns the first profile with the given tag (position == 0).
    * If no profile with given tag exists, null is returned.
    * @param tag        tag of profile, e.g. TAG_INTERNET_IOP.value
    * @return           first profile or null if no profile with given
    *                   tag exists
    */
   public org.omg.ETF.Profile get_profile(int tag);
}
