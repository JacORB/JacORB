package demo.domain.printer;

import org.omg.CosNaming.*;

public class Client
{
    public static void main(String args[]) 
    { 
	try
	{
	    ThePrinter printer;
	    Spooler spooler;
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

	    NamingContextExt nc = NamingContextExtHelper.narrow(
								orb.resolve_initial_references("NameService"));
	    
	    printer= ThePrinterHelper.narrow(nc.resolve(nc.to_name("printer.example")));
	    spooler= SpoolerHelper.narrow(nc.resolve(nc.to_name("spooler.example")));

	    // generate data file
	    String data=" This is just a sample file content.";
	  
	    // ok, we assume we cannot directly use the printer object to print the file
	    // but have to use the spooler instead
	      
	    int price= spooler.doPrintJob(printer, data);
	    System.out.println("Price for print job was " + price + " units.");

	    
	    printer._release();
	    spooler._release();
	    System.out.println("done. ");
	

	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }
}


