package org.jacorb.orb.domain;

/**
 * The implementation  of a  meta  property policy.  A meta  property
 * policy   simply  extends   a   property   policy   by  the   meta
 * functionality. That means, an instance both a property policy and a
 * meta policy for some other policies.
 *
 * Created: Tue Aug  1 14:53:29 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$ 
 */

public class MetaPropertyPolicyImpl 
    extends PropertyPolicyImpl
    implements MetaPropertyPolicyOperations 
{
    private int theTypes[]= null;
  
    public MetaPropertyPolicyImpl() 
    {
        super("meta property policy");
    }
    
    // GB: added
    public MetaPropertyPolicyImpl(int[] types) 
    {
        super("meta property policy");
        setManagedTypes(types);
    }

    // getter
    public int[] managedTypes()
    {
        return theTypes;
    }

    // setter
    public void setManagedTypes(int[] types)
    {
        theTypes = types;

        // little hack: show types as property 
        StringBuffer result = new StringBuffer();

        int i = 0;
        for (; i < theTypes.length - 1 ; i++) 
            result.append( theTypes[i] + ", ");

        if (i < theTypes.length)
            result.append( theTypes[i] ); // last one without comma

        changeValueOfProperty(" meta for policy types: (readonly)", 
                              result.toString() );
    } // setManagedTypes

} // MetaPropertyPolicyImpl











