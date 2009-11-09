package demo.arrays;

import demo.arrays.MyServerPackage.*;

class serverImpl 
    extends MyServerPOA
{

    public void _notify(MyServer[] s)
    {
	int [] j = new int[]{1,2,3};
	for( int i = 0; i < s.length; i++)
	    s[i].write("Notification # "+i, j);
    }

    public void notify2(MyServer[] s)
    {
	int [] j = new int[]{26,19};
	for( int i = 0; i < s.length; i++)
	    s[i].write2("Another Notification", j);
    }

    public void notify3(demo.arrays.MyServerPackage.arrayContainer ac)
    {
	int [] j = new int[2];
	for( int i = 0; i < ac.shorty.length; i++)
	    System.out.println("notify3, ac["+i+"][0]: "+ ac.shorty[i][0]);
    }

    public int[] write(String s, int[] j)
    {
	int [] a = new int[]{ 42, 34,13};
	java.lang.System.out.println("write: " + s + " size: " + j.length);
	return a;
    }
    public int[] write2(String s, int[] j)
    {
	int [] a = new int[]{665};

	System.out.println("write2: " + s + " size: " + j.length);
	for( int i = 0; i < j.length; i++ )
	    System.out.println("[" + i + "]: " + j[i] ); 
	return a;
    }

    public void printLongArray(long[] refs) {
        System.out.println("Taille du tableau de long : " + refs.length);
        for (int i=0; i<refs.length; i++ )
            System.out.println("refs[" + i + "] = " + refs[i]);
    }

    public void printDoubleArray(double[] refs) {
        System.out.println("Taille du tableau de doble : " + refs.length);
        for (int i=0; i<refs.length; i++ )
            System.out.println("refs[" + i + "] = " + refs[i]);
    }

}


