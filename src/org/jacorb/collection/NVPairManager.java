package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.omg.CORBA.*;

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
