package org.jacorb.naming;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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
 * Implementation of the  "BindingIterator" interface
 * @author Gerald Brose
 * @version $Id$
 * $Log$
 * Revision 1.4  2000/12/04 16:22:38  brose
 * *** empty log message ***
 *
 * Revision 1.3  1999/11/25 16:05:57  brose
 * cosmetics
 *
 * Revision 1.2  1999/11/03 18:06:01  brose
 * *** empty log message ***
 *
 * Revision 1.1.1.1  1999-08-05 12:22:05+02  brose
 * First initial preliminary ... attempt
 *
 * Revision 1.2  1999-07-27 10:51:49+02  brose
 * corrected next_one
 *
 */

import  org.omg.CosNaming.*;

public class BindingIteratorImpl
    extends org.omg.CosNaming.BindingIteratorPOA
{
    Binding [] bindings;
    int iterator_pos = 0;

    public BindingIteratorImpl( Binding [] b )
    {
	bindings = b;
	if( b.length > 0 )
	    iterator_pos = 0;
    }

    public void destroy()
    {
	bindings = null;
	try 
	{
	    finalize();
	} catch ( Throwable t ){}
    }

    public boolean next_n(int how_many, 
			  org.omg.CosNaming.BindingListHolder bl)
    {
	int diff = bindings.length - iterator_pos;
	if( diff > 0 )
	{
	    Binding [] bndgs = null;
	    if( how_many <= diff )
	    {
		bndgs = new Binding[how_many];
		System.arraycopy(bindings, iterator_pos, bndgs, 0, how_many);
		iterator_pos += how_many;
	    }
	    else
	    {
		bndgs = new Binding[diff];
		System.arraycopy(bindings, iterator_pos, bndgs, 0, diff);
		iterator_pos = bindings.length;
	    }
	    bl.value = bndgs;
	    return true;
	} 
	else 
	{
	    bl.value = new org.omg.CosNaming.Binding[0];
	    return false;
	}
    }

    public boolean next_one(org.omg.CosNaming.BindingHolder b)
    {
	if( iterator_pos < bindings.length ) 
	{
	    b.value = bindings[iterator_pos++];
	    return true;
	} 
	else
	{
	    b.value = new Binding(new org.omg.CosNaming.NameComponent[0], 
				  org.omg.CosNaming.BindingType.nobject);
	    return false;
	}
    }
}



