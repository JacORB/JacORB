package org.jacorb.test.orb.value;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

public class PasserImpl extends PasserPOA
{

    public void pass_ops(BaseHolder outarg)
    {
        FooImpl foo = new FooImpl();
        
        try
        {
            byte oid[] = this._default_POA().activate_object(foo);
            org.omg.CORBA.Object obj = this._default_POA().id_to_reference(oid);
            outarg.value = FooHelper.narrow(obj); 
        }
        catch (Exception e)
        {
            throw new org.omg.CORBA.BAD_OPERATION (e.toString());
        }
    }

    public void pass_state(BaseHolder outarg)
    {
        TreeController tc = new TreeControllerImpl ();

        tc.root = new StringNodeImpl ("RootNode");
        
        tc.root.left = new StringNodeImpl ("LeftNode");
        
        tc.root.right = new StringNodeImpl ("RightNode");
        
        outarg.value = tc;
    }

    public void pass_nil(BaseHolder outarg)
    {
        outarg.value = null;
    }

    public void shutdown()
    {
        // noop
    }

}
