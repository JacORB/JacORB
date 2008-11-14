/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.naming.namemanager;

import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;


public class BindNode
{
    protected Binding binding;
    protected String typeID;
    public boolean matched;
    public boolean used;

    public BindNode(Binding b) 
    { 
        binding=b; 
        used=false; 
    }
    public boolean equals(BindNode bnode)
    {
        return toString().equals(bnode.toString());
    }
    public Binding getBinding()
    {
        return binding; 
    }
    public NameComponent[] getName() 
    { 
        return binding.binding_name; 
    }
    public String getTypeID() 
    { 
        return typeID; 
    }
    public boolean isContext() 
    { 
        return binding.binding_type.value()==BindingType._ncontext;
    }
    public void setTypeID(String id) 
    { 
        typeID=id; 
    }

    /**
     *  String representation of this Nodes
     */

    public String toString()
    {
        NameComponent[] name=binding.binding_name;
        /*
          StringBuffer str=new StringBuffer(name[name.length-1].id);
          if (name[name.length-1].kind.length()>0)
          str.append(" ("+name[name.length-1].kind+")");
          return new String(str);
        */
        return name[name.length-1].id;
    }
}


