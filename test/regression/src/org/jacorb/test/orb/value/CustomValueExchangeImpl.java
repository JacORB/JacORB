package org.jacorb.test.orb.value;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

/**
 * <code>CustomValueExchangeImpl</code> is the implementation of the
 * interface for testing sending custom valuetypes.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class CustomValueExchangeImpl extends CustomValueExchangePOA
{
    /**
     * <code>sendValueExample</code> receives a custom marshalled valuetype.
     *
     * @param value a <code>CustomValueExample</code> value
     */
    public void sendValueExample( CustomValueExample value )
    {
        if (value != null)
        {
            System.out.println
                ("CustomValueExchangeImpl::sendValueExample::number " + value.number());
            value.print();
        }
        else
        {
            System.out.println
                ("CustomValueExchangeImpl::sendValueExample::value is nil");
        }
    }
}
