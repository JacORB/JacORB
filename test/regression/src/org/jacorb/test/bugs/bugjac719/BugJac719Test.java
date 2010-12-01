package org.jacorb.test.bugs.bugjac719;

import org.jacorb.test.common.ORBTestCase;

public class BugJac719Test extends ORBTestCase
{
    private static int DIM_ARRAYA_1 = 10;
    private static int DIM_THREEDIMA_1 = 2;
    private static int DIM_THREEDIMA_2 = 3;

    public void testUnion()
    {
        UnionStartMultiDimArrayStruct sample = new UnionStartMultiDimArrayStruct();
        
        sample.threeDimA(new A[DIM_THREEDIMA_1][DIM_THREEDIMA_2][DIM_ARRAYA_1]);
        
        if (sample.threeDimA() == null)
        {
            fail("threeDimA() == null");
        }
        if (sample.threeDimA().length != DIM_THREEDIMA_1)
        {
            fail("Expected threeDimA().length == " 
                    + DIM_THREEDIMA_1 + " ; Received: " 
                    + sample.threeDimA().length);
        }
        
        for (int i = 0; i < DIM_THREEDIMA_1; i++) 
        {
            if (sample.threeDimA()[i].length != DIM_THREEDIMA_2)
            {
                fail("Expected usmds.threeDimA()[" + i + "].length == " 
                    + DIM_THREEDIMA_2 
                    + "; Received: " 
                    + sample.threeDimA()[i].length);
            }
            
            for (int j = 0; j < DIM_THREEDIMA_2; j++)
            {
                if (sample.threeDimA()[i][j].length != DIM_ARRAYA_1)
                {
                    fail("Expected usmds.threeDimA()[" + i + "][" + j + "].length == "
                            + DIM_ARRAYA_1
                            + "; Received: "
                            + sample.threeDimA()[i][j].length);
                }
                for (int k = 0; k < DIM_ARRAYA_1; k++) 
                {
                    // just access the each of array members 
                    A var = sample.threeDimA()[i][j][k];
                }
            }
        }
    }
}
