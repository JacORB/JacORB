package test.connection.timeout;

/**
 * A very simple implementation of a 2-D grid
 */


public class gridImpl  
    extends MyServerPOA
{
    protected short height = 31;
    protected short width = 14;
    protected java.math.BigDecimal[][] mygrid;

    private void sleep( int how_long )
    {
        try
        {
            System.out.println( "Going to sleep...");
            Thread.currentThread().sleep( how_long );
        }
        catch( InterruptedException ie )
        {
            ie.printStackTrace();
        }
        System.out.println( "Awake again");
    }

 
    public gridImpl()
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


