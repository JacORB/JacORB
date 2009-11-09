package demo.outparam;

/**
 * An example server for using out and inout holders
 */

public class MyServerImpl extends MyServerPOA
{

    public void op1(java.lang.String a1,
            stringSeqHolder/*out*/ s)
    {
        String result [] = new String[5];
        for( int j = 0; j < 5; j++ )
            result[j] = a1;
        s.value = result;
    }

    public void op2(MyServerHolder s)
    {
        s.value = _this();
    }

    public void op3(my_structHolder/*out*/ m)
    {
        m.value = new my_struct("hallo", 4711, null);
    }

    public void op4(stringArrayHolder sah)
    {
        String s[] = new String[5];
        for( int i = 0; i < 5; s[i++]= "***");
        sah.value = s;
    }

    public String op5( org.omg.CORBA.StringHolder ws)
    {
        ws.value = "1234567890";
        return "op5 done.";
    }

    public void print(String s)
    {
        System.out.println(s);
    }

    public void stringCubeInOut(stringCubeHolder/*inout*/ sc)
    {
        String [][][] string_cube = sc.value;

        for( int outer = 0; outer < string_cube.length; outer++ )
        {
            for( int middle = 0; middle < string_cube[outer].length; middle++ )
            {
                for( int inner = 0; inner < string_cube[outer][middle].length; inner++ )
                {
                    System.out.print("StringCube ["+outer+"]["+middle+"]["+inner+"]: ");
                    System.out.println(string_cube[outer][middle][inner]);
                    string_cube[outer][middle][inner] = "Returned element ["+outer+"]["+middle+"]["+inner+"]";
                }
            }
        }
    }

    public boolean addNums(double n1, double n2, org.omg.CORBA.DoubleHolder n3)
    {
        n3.value = n1 + n2;
        return true;
    }
}


