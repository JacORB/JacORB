package demo.domain.printer;

import org.jacorb.orb.domain.*;

/**
 * SpoolerImpl.java
 * Created: Fri Sep  8 19:17:38 2000
 * An implementation of the IDL interface Spooler
 * To get the price of a print job the spooler gets a price policy via the
 * domain service for the target printer.
 * It uses this policy to calculate the price for the print job.
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class SpoolerImpl 
    extends SpoolerPOA 
{
    private static int PRICE_POLICY_TYPE= 300;
  
    public SpoolerImpl() 
    {} 
  
    public int getPriceOfPrintJob( demo.domain.printer.ThePrinter printer, 
                                   java.lang.String data)
    {
        // get price policy of  printer object
        try 
        {
            // here is the important call !!!
            PropertyPolicy price = 
                PropertyPolicyHelper.narrow(printer._get_policy( PRICE_POLICY_TYPE ));

            int pricePerByte = 
                Integer.parseInt( price.getValueOfProperty("Byte") );
            return pricePerByte * data.length();

        }
        catch (org.omg.CORBA.INV_POLICY inv)
        {
            // no price policy defined, so printing costs nothing 
            return 0;
        }
        catch (java.lang.NumberFormatException e)
        { 
            return 0;
        }
    }
	
    public int doPrintJob(demo.domain.printer.ThePrinter printer, 
                          java.lang.String data)
    {
        printer.printFile(data);
        return getPriceOfPrintJob(printer, data);
    } 

} // SpoolerImpl
