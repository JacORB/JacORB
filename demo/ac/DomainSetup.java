package demo.ac;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import jacorb.orb.domain.*;

import java.io.*;

/**
 * DomainSetup.java
 *
 * Created: Wed Jul 12 14:22:22 2000
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class DomainSetup 
{    
    public static void main(String[] args) 
    {
        try
        {
            ORB orb = ORB.init(args, null);
            POA poa = 
                POAHelper.narrow( orb.resolve_initial_references("RootPOA"));

            Domain dm = null;

            if (args.length == 0)
            {
                dm = DomainHelper.narrow
                    (orb.resolve_initial_references("DomainService"));
            }
            else
            {
                try
                {
                    BufferedReader f_in = new BufferedReader
                        (new FileReader(args[0]));
                    
                    dm = DomainHelper.narrow
                        (orb.string_to_object(f_in.readLine()));
                    
                    f_in.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            // create meta policy            
            PropertyPolicy meta = dm.createMetaPropertyPolicy();
            meta.short_description("AccessControl Meta Policy");
            meta.put("ResolverClass", "demo.ac.BooleanConflictResolver");
            meta.put("Operation", "OR");
            meta.setPolicyType(200);

            Domain hype_inc = 
                dm.createDomain( new org.omg.CORBA.Object[0], 
                                 new Policy[] {meta}, // pp
                                 "HypeInc.");

            dm.insertChild(hype_inc);

            Domain e_branch = dm.createDomain(new org.omg.CORBA.Object[0], 
                                              new Policy[0], // pp
                                              "EuropeanBranch");

            hype_inc.insertChild(e_branch);

            Domain us_branch = dm.createDomain(new org.omg.CORBA.Object[0], 
                                               new Policy[0], 
                                               "USBranch");

            hype_inc.insertChild(us_branch);


            Domain acc = dm.createDomain(new org.omg.CORBA.Object[0], 
                                         new Policy[0], // pp
                                         "R&D");

            e_branch.insertChild(acc);

            //acc.insertChild(orb_dm);

            // create and register printer objects
 
            PrinterImpl braillePrinter = new PrinterImpl("BrP1.500");
            PrinterImpl laserPrinter = new PrinterImpl("HP 8500");
            Printer braillePrinterObject = 
                PrinterHelper.narrow( poa.servant_to_reference( braillePrinter ));
            Printer laserPrinterObject = 
                PrinterHelper.narrow( poa.servant_to_reference( laserPrinter ));

            acc.insertMemberWithName("Braille Printer", braillePrinterObject );
            acc.insertMemberWithName("Laser Printer", laserPrinterObject );

            // create and register document objects

            DocumentImpl contract = 
                new DocumentImpl("contract", "This is a contract...");
            DocumentImpl invoice = 
                new DocumentImpl("invoice", "This is an invoice");

            Document contractObject = 
                DocumentHelper.narrow( poa.servant_to_reference( contract ));
            Document invoiceObject = 
                DocumentHelper.narrow( poa.servant_to_reference( invoice ));

            acc.insertMemberWithName("Contract", contractObject );
            acc.insertMemberWithName("Invoice", invoiceObject );


            Domain sales = dm.createDomain( new org.omg.CORBA.Object[0], 
                                            new Policy[] {}, // pp
                                            "Sales");

            e_branch.insertChild(sales);
            orb.run();
            
            orb.shutdown(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }    
} // DomainSetup
