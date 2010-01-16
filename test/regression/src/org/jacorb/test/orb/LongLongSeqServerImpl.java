package org.jacorb.test.orb;

import org.jacorb.test.LongLongSeqServerPOA;
import org.jacorb.test.LongLongSeqServerPackage.SeqLongLongHolder;


public class LongLongSeqServerImpl extends LongLongSeqServerPOA
{
    private void test( long[] arg )
    {
       if ( arg[0] != Long.MIN_VALUE ||
            arg[1] != Long.MIN_VALUE  )
       {
           throw new RuntimeException( "Error - arguments do not match expected value" );
       }
    }

    public long[] test1( long[] argin,
                         SeqLongLongHolder argout,
                         SeqLongLongHolder arginout )
    {
        test( argin );

        test( arginout.value );

        argout.value = argin;
        arginout.value = argin;

        return argin;
    }

    public long[] test2( long[] argin, SeqLongLongHolder argout )
    {
        test( argin );

        argout.value = argin;

        return argin;
    }

    public void test3( SeqLongLongHolder arginout )
    {
        test( arginout.value );
    }
}
