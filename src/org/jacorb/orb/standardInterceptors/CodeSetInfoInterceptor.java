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
  
    private static final boolean endian_mode = false;
    private ORB orb = null;
  
    public CodeSetInfoInterceptor(ORB orb) 
    {
	this.orb = orb;
    }

    public String name()
    {
	return "CodeSetInfoComponentCreator";
    }

    // implementation of org.omg.PortableInterceptor.IORInterceptorOperations interface

    /**
     * Creates default IOR codeset component. Because we are in Java, we define
     * UTF8/UTF16 as defaults and ISO8859_1 as supported for backward compatibility
     * with non-codeset aware ORBs.
     * @author devik
     *
     * @param info <description>
     */

    public void establish_components(IORInfo info) 
    {
    
	// create the info
	org.omg.CONV_FRAME.CodeSetComponentInfo cs_info=
	    new org.omg.CONV_FRAME.CodeSetComponentInfo();
		
	// fill the info
	cs_info.ForCharData = new org.omg.CONV_FRAME.
	    CodeSetComponent(CodeSet.UTF8, new int[] { CodeSet.ISO8859_1 });
	cs_info.ForWcharData = new org.omg.CONV_FRAME.
	    CodeSetComponent(CodeSet.UTF16, new int[] { CodeSet.UTF8 });
			
	// encapsulate it into TaggedComponent
	CDROutputStream os = new CDROutputStream(orb);
	os.write_boolean(endian_mode);
	org.omg.CONV_FRAME.CodeSetComponentInfoHelper.write(os,cs_info);

	org.omg.IOP.TaggedComponent tagc = 
	    new org.omg.IOP.TaggedComponent(org.omg.IOP.TAG_CODE_SETS.value,
					    os.getBufferCopy());

	os.close();

	info.add_ior_component_to_profile (tagc, org.omg.IOP.TAG_INTERNET_IOP.value);
	jacorb.util.Debug.output( Debug.INTERCEPTOR | 3, 
                                 "CodeSetInfoCreator added TaggedComponent to TAG_INTERNET_IOP profile");
      
    }
}
