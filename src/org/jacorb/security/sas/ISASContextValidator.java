package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2002 Gerald Brose
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

//import org.omg.PortableInterceptor.*;
//import org.omg.PortableInterceptor.ORBInitInfoPackage.*;
//import org.omg.SecurityReplaceable.*;
//import org.omg.Security.*;
//import org.omg.IOP.*;
//import org.omg.IOP.CodecFactoryPackage.*;
//import org.jacorb.util.*;

//import org.jacorb.util.Environment;
import org.omg.PortableInterceptor.*;

public interface ISASContextValidator
{
    public boolean validate(ServerRequestInfo ri, byte[] contextToken);
    public String getPrincipalName();
}
