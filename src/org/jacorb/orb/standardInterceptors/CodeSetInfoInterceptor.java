package org.jacorb.orb.standardInterceptors;

import org.omg.PortableInterceptor.*;
import org.jacorb.orb.*;
import org.jacorb.orb.connection.CodeSet;
import org.jacorb.util.Debug;

/**
 * This interceptor creates a codeset TaggedComponent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class CodeSetInfoInterceptor 
    extends LocalityConstrainedObject 
    implements IORInterceptor 
{
    private org.omg.IOP.TaggedComponent tagc = null;

    public CodeSetInfoInterceptor(ORB orb) 
    {
	// create the info
	org.omg.CONV_FRAME.CodeSetComponentInfo cs_info =
	    new org.omg.CONV_FRAME.CodeSetComponentInfo();
		
	// fill the info
	cs_info.ForCharData = 
            new org.omg.CONV_FRAME.
                CodeSetComponent( CodeSet.getTCSDefault(), 
                                  new int[] { CodeSet.getConversionDefault() } );

	cs_info.ForWcharData = 
            new org.omg.CONV_FRAME.
                CodeSetComponent( CodeSet.getTCSWDefault(), 
                              new int[] { CodeSet.UTF8 } );
			
	// encapsulate it into TaggedComponent
	CDROutputStream os = new CDROutputStream( orb );
	os.beginEncapsulatedArray();
	org.omg.CONV_FRAME.CodeSetComponentInfoHelper.write( os, cs_info );

	tagc = 
	    new org.omg.IOP.TaggedComponent( org.omg.IOP.TAG_CODE_SETS.value,
                                             os.getBufferCopy());
	os.close();
    }

    public String name()
    {
	return "CodeSetInfoComponentCreator";
    }

    /**
     * Creates default IOR codeset  component. Because we are in Java,
     * we define UTF8/UTF16 as defaults and ISO8859_1 as supported for
     * backward compatibility with non-codeset aware ORBs.
     * @author devik
     *
     */

    public void establish_components( IORInfo info ) 
    {    

	info.add_ior_component_to_profile( tagc, 
                                           org.omg.IOP.TAG_INTERNET_IOP.value );

//  	org.jacorb.util.Debug.output( Debug.INTERCEPTOR | 3, 
//               "CodeSetInfoCreator added TaggedComponent to TAG_MULTIPLE_COMPONENTS profile");
      
    }
}
