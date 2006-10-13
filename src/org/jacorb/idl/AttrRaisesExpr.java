package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2006 Gerald Brose.
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

import java.io.PrintWriter;
import java.util.*;

/**
 * This is only a holder object for the result of the attr_raises_expr
 * production.  The values are immediately transferred into the corresponding
 * AttrDecl object during parsing.
 * 
 * @author Andre Spiegel
 * @version $Id$
 */

public class AttrRaisesExpr
        extends IdlSymbol
{
    public Vector getNameList;
    public Vector setNameList;

    public AttrRaisesExpr( int num )
    {
        super( num );
        getNameList = new Vector();
        setNameList = new Vector();
    }
}
