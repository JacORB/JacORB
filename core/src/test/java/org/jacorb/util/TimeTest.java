package org.jacorb.util;

import org.junit.Test;
import org.omg.TimeBase.UtcT;

import static org.junit.Assert.assertTrue;

public class TimeTest
{
    @Test
    public void waitFor()
    {
        long now = System.nanoTime();
        long later = now + ( 2 * 1000 * 1000000);

        Time.waitFor( new UtcT( (System.currentTimeMillis() + 5000 ) * 10000 + Time.UNIX_OFFSET, 0, (short)0, (short)0) );

        assertTrue( System.nanoTime() >= later );
    }
}
