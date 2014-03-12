package org.jacorb.test.orb.connection.timeout;

import org.jacorb.test.common.TestUtils;

/**
 * A very simple implementation of a 2-D grid
 */

public class GridImpl
    extends MyServerPOA
{
    protected short height = 31;
    protected short width = 14;
    protected java.math.BigDecimal[][] mygrid;

    private void sleep( int how_long )
    {
        try
        {
            TestUtils.getLogger().debug( "Going to sleep...");
            Thread.sleep( how_long );
        }
        catch( InterruptedException ie )
        {
        }
        TestUtils.getLogger().debug( "Awake again");
    }

    public void gridImpl()
    {
        mygrid = new java.math.BigDecimal[height][width];
        for( short h = 0; h < height; h++ )
        {
            for( short w = 0; w < width; w++ )
            {
                mygrid[h][w] = new java.math.BigDecimal("0.21");
            }
        }
    }

    public java.math.BigDecimal get(short n, short m)
    {
        sleep( 20000 );
        if( ( n <= height ) && ( m <= width ) )
            return mygrid[n][m];
        else
            return new java.math.BigDecimal("0.01");
    }

    public short height()
    {
        sleep( 20000 );
        return height;
    }

    public void set(short n, short m, java.math.BigDecimal value)
    {
        sleep( 20000 );
        if( ( n <= height ) && ( m <= width ) )
            mygrid[n][m] = value;
    }

    public short width()
    {
        sleep( 20000 );
        return width;
    }
}
