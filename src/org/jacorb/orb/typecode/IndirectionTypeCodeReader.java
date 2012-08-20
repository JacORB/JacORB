/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.orb.typecode;

import java.util.Map;
import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TypeCode;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 */
public class IndirectionTypeCodeReader extends AbstractTypeCodeReader
{
    protected TypeCode doReadTypeCode(CDRInputStream in, Map recursiveTCMap,
            Map repeatedTCMap, Integer startPosition, int kind)
    {
        final Logger logger = in.getLogger();

        // recursive or repeated TC
        final int negative_offset = in.read_long();

        final Integer origTCStartPos = Integer.valueOf(in.get_pos() - 4 + negative_offset);

        if (logger.isDebugEnabled())
        {
            logger.debug(in.getIndentString() + "  startPosition: " + startPosition);
            logger.debug(in.getIndentString() + "  negative offset: " + negative_offset);
            logger.debug(in.getIndentString() + "  calculated position: " + origTCStartPos);
            logger.debug(in.getIndentString() + "  repeated TC map: " + repeatedTCMap);
            logger.debug(in.getIndentString() + "  recursive TC map: " + recursiveTCMap);
        }

        // check repeatedTCMap first
        // this map contains TypeCode's that are already completely read in
        final org.omg.CORBA.TypeCode repeatedTC =
            (org.omg.CORBA.TypeCode)repeatedTCMap.get(origTCStartPos);

        if (repeatedTC != null)
        {
            return repeatedTC;
        }

        // check recursiveTCMap next
        // this map contains not yet completely read in TypeCode's
        final String recursiveId = (String) recursiveTCMap.get(origTCStartPos);
        if (recursiveId != null)
        {
            return orb.create_recursive_tc(recursiveId);
        }

        //if we end up here, we didn't find an entry in either
        //repeatedTCMap and recursiveTCMap
        throw new MARSHAL(
            "Found indirection marker, but no corresponding "+
            "original typecode (pos: " + origTCStartPos + ")" );
    }
}
