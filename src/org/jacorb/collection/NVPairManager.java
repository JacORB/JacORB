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
package org.jacorb.collection;

import org.omg.CORBA.TCKind;
import org.omg.CosCollection.NVPair;
import org.omg.CosCollection.Operations;
import org.omg.CosCollection.OperationsHelper;
import org.omg.CosCollection.ParameterInvalid;

public class NVPairManager {
    private NVPair param[];
    public NVPairManager( NVPair[] parameters ){
        param = new NVPair[ parameters.length ];
        for( int i=0; i<parameters.length; i++ ) {
            param[i] = parameters[i];
        }
    };
    public int find_param_idx( String key ){
        for( int i = 0; i<param.length; i++ ){
            if ( key.equals(param[i].name) ) {
                return i;
            }
        };
        return -1;
    };
    public String find_string_param( String key ) throws ParameterInvalid {
        int i = find_param_idx( key );
        if ( i == -1 ) {
            return null;
        }
        if( param[i].value.type().kind().value() != TCKind._tk_string ){
            throw new ParameterInvalid( i, "Invalid parameter type" );
        }
        return param[i].value.extract_string();
    };
    public Integer find_ulong_param( String key ) throws ParameterInvalid {
        int i = find_param_idx( key );
        if ( i == -1 ) {
            return null;
        }
        if( param[i].value.type().kind().value() != TCKind._tk_ulong ){
            throw new ParameterInvalid( i, "Invalid parameter type" );
        }
        return new Integer(param[i].value.extract_ulong());
    };
    public Operations find_operations_param( String key ) throws ParameterInvalid {
        int i = find_param_idx( key );
        if ( i == -1 ) {
            return null;
        }
/*
        if( !param[i].value.type().equal( OperationsHelper.type() ) ){
            throw new ParameterInvalid( i, "Invalid parameter type" );
        }
*/
        org.omg.CORBA.Object obj = param[i].value.extract_Object();
        return OperationsHelper.narrow( obj );
//        return OperationsHelper.extract(param[i].value);
    };
};






