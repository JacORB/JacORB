package org.jacorb.orb.domain;

import org.jacorb.orb.domain.*;
import org.jacorb.util.Debug;
import java.io.*;
import org.jacorb.orb.ParsedIOR;

/**
 * A utility class with some useful static functions.
 *
 * Created: Thu Jan 13 14:16:16 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class Util  
{

    /** used for echoing reads to a file */
    private static PrintWriter _out= null;

    /** displays a menu and reads a choice */
    public static int textmenu(String[] entries)
    {
        // i is index for array, j is printed out
        int i, j=0, answer= -1;
        while (answer < 1 || answer > entries.length) 
        {
            System.out.println();
            i= 0; j=0; 
            while (i < entries.length) // print menu 
            {
                if ( entries[i].charAt(0) == '-')
                    System.out.println(" "+          "\t " + entries[i]);
                else 
                    System.out.println(" "+ (++j) + ")\t " + entries[i]);
                i++;
            }
            System.out.print("choice(1-" + j+ "): ");
            answer= readInt(System.in);
            // System.out.println("answer is " + answer);
        }
        return answer;
    } // textmenu

    /** reads an int from standard input */
    public static int readInt() 
    { 
        return readInt(System.in); 
    }

    /** reads an int from InputStream in */
    public static int readInt(InputStream in)
    {
        int result= -1;
        DataInputStream input= new DataInputStream((in));
        String line;

        try 
        { 
            line= input.readLine(); 
        }
        catch (IOException e){ 
            System.out.println("readInt: couldn't read line."); return -1;
        }

        // System.out.println("line is: " + line);
        try
        { 
            result= Integer.parseInt(line); 
        }
        catch (NumberFormatException e) 
        { 
            System.out.print("readInt: couln't convert input to int, try again: ");
            result= readInt(in);
        }
        // System.out.println("answer is " + result);
    
        if (_out != null) // echo result to file
        {
            _out.println(result);
            _out.flush();
        }

        return result;
    } // readInt

    /** reads an char from standard input */
    public static char readChar() 
    { 
        return readChar(System.in); 
    } 

    /** reads an char from InputStream in */
    public static char readChar(InputStream in)
    {
        char result= '?';
        DataInputStream input= new DataInputStream(in);
   
    
        String line;
        try 
        { 
            line= input.readLine();
        }
        catch (IOException e)
        { 
            System.out.println("readChar: couldn't read line."); return '?';
        }
        try 
        { 
            result= line.charAt(0); 
        }
        catch (Exception e) 
        { 
            System.out.print
                ("readChar: couldn't convert input to char, try again: ");
            result= readChar(in);
        }
        //        System.out.println("answer is " + result);
    
        if (_out != null) // echo result to file
        {
            _out.println(result);
            _out.flush();
        }

        return result;
    } // readChar

    /** enables echoing for readInt and readChar to a file */
    public  static void FileEchoOn(String filename)
    {
        if (_out == null) 
        {
            try 
            {
                FileOutputStream file = new FileOutputStream(filename);
                _out = new PrintWriter(file);
            } 
            catch (Exception notFound) 
            {
                //catching Exception instead of more specific one
                //since jdk1.1 and jdk1.2 differ in what is thrown
                System.out.println(" file " + filename + " not found.");
            }        
        }
    }

    /** disables echoing for readInt and readChar */
    public  static void FileEchoOff()
    {
        if (_out != null) 
        {
            _out.flush();
            _out.close();
            _out = null;
        }
    }

    /** 
     *  sorts an  array of  policies in  alphabetical order  using the
     * string representation of getNameOfPolicy.
     *  @see Util#getNameOfPolicy 
     */
    public static void quicksort (int links, 
                                  int rechts, 
                                  org.omg.CORBA.Policy[] array)
    { 
        if (links < rechts)
        {
            String el= getNameOfPolicy(array[links]); 
            int i=links, j=rechts;
            while (i<=j)
            {
                while ( (getNameOfPolicy(array[i])).compareTo(el)  < 0 ) i++;
                // while (array[i] < el) i++;
                while ( (getNameOfPolicy(array[j])).compareTo(el) > 0) j--;
                // while (array[j] > el) j--;
                if (i<=j) {swap(i,j, array); i++; j--; }
            }
            quicksort(links,j,array);
            quicksort(i,rechts,array);
        }
    } // quicksort <Policy[]>

    /**
     *  sorts an  array of  objects  in alphabetical  order using  the
     * string representation of downcast.
     *  @see Util#downcast 
     */
    public static void quicksort (int links, 
                                  int rechts, 
                                  org.omg.CORBA.Object[] array)
    { 
        if (links < rechts)
        {
            // int el= findEl(links,rechts,arr);
            String el= downcast(array[links]); //findMiddle(links,rechts,arr);
            int i=links, j=rechts;
            while (i<=j)
            {
                while ( (downcast(array[i])).compareTo(el)  < 0 ) i++;
                // while (array[i] < el) i++;
                while ( (downcast(array[j])).compareTo(el) > 0) j--;
                // while (array[j] > el) j--;
                if (i<=j) {swap(i,j, array); i++; j--; }
            }
            quicksort(links,j,array);
            quicksort(i,rechts,array);
        }
    } // quicksort <Object[]>

    /** 
     * sorts an array of strings in alphabetical order. 
     */
    public static void quicksort (int links, int rechts, String[] array)
    { 
        if (links < rechts)
        {
            // int el= findEl(links,rechts,arr);
            String el= array[links]; //findMiddle(links,rechts,arr);
            int i=links, j=rechts;
            while (i<=j)
            {
                while ( array[i].compareTo(el)  < 0 ) i++;
                // while (array[i] < el) i++;
                while ( array[j].compareTo(el) > 0) j--;
                // while (array[j] > el) j--;
                if (i<=j) {swap(i,j, array); i++; j--; }
            }
            quicksort(links,j,array);
            quicksort(i,rechts,array);
        }
    }

    /** swaps the contents of an array determined by the two indexes. */
    public static void swap(int i, int j, Object[] array)
    { 
        Object temp=array[i]; 
        array[i]= array[j];
        array[j]= temp; 
    }

    /** swaps the contents of an array determined by the two indexes. */
    public static void swap(int i, int j, String[] array)
    { 
        String temp=array[i]; 
        array[i]= array[j];
        array[j]= temp; 
    }

    /** 
     * downcasts a corba object to some preassumed types. 
     *  @return string representing the downcasted object 
     */
    public static String downcast(org.omg.CORBA.Object obj)
    {
        // first try test policy type
        try
        {
            TestPolicy policy = TestPolicyHelper.narrow(obj);
            return policy.description();
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {}
    
        // then try domain type
        try
        {
            Domain aDomain= DomainHelper.narrow(obj);
            return aDomain.name();
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {}

        // finally, use type id
        return toID(obj.toString());

        // finally, use ior
        // return obj.toString();
    } // downcast

    /** 
     * extracts the type id and the object key from an IOR string. 
     */
    public static String toID (String iorString)
    {
        ParsedIOR pior = new ParsedIOR( iorString );
        org.omg.IOP.IOR ior = pior.getIOR();
        return ior.type_id; //	dumpHex( pior.get_object_key() );
    }

    /** 
     * @return a name for a policy. 
     * The name depends of the actual type of the policy. 
     */
    public static String getNameOfPolicy(org.omg.CORBA.Policy pol)
    {
        try
        {
            ManagementPolicy manage = 
                ManagementPolicyHelper.narrow(pol);
            return manage.short_description();
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {}

        try
        {
            PropertyPolicy prop = 
                PropertyPolicyHelper.narrow(pol);
            return prop.name();
        }
        catch( org.omg.CORBA.BAD_PARAM bp )
        {}

        return "Type " + Integer.toString( pol.policy_type() );
    }

    /**
     * returns a string which can be used as a key for a policy in a domain 
     */
    public static String getNamedKeyOfPolicy(org.omg.CORBA.Policy pol)
    {
        return getNameOfPolicy(pol) + Integer.toString( pol.policy_type() );
    }

 
} // Util











