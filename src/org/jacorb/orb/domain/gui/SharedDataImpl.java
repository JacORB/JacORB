package org.jacorb.orb.domain.gui;

import org.jacorb.util.*;
import org.jacorb.orb.domain.*;
import java.util.*;
/**
 * SharedDataImpl.java
 * An implementation of the interface SharedData.
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class SharedDataImpl 
    implements SharedData
{
    /** the registered frames */
    private Hashtable _frames;

    /** the used orb */
    private org.omg.CORBA.ORB _orb;

    /** the orb domain */
    private Domain _orbDomain;

    /** the policy editors, modelled by a property policy*/
    private PropertyPolicy _policyEditors;

    /** the buffer where to temporalely store a domain member */
    private org.omg.CORBA.Object _memberBuffer    = null;
    private String               _memberNameBuffer= null;
    private java.lang.Object     _memberLock      = new java.lang.Object(); // for synchronisation

    /** the policy buffer */
    private org.omg.CORBA.Policy _policyBuffer    = null;
    private java.lang.Object     _policyLock      = new java.lang.Object(); // for synchronisation

    /** the domain bufffer */
    private org.jacorb.orb.domain.Domain _domainBuffer= null;
    private java.lang.Object         _domainLock  = new java.lang.Object(); // for synchronisation

    SharedDataImpl() 
    {
        _frames= new Hashtable(10);
        initPropertyPolicy();

    }
    SharedDataImpl(org.omg.CORBA.ORB orb)
    {
        this();
        _orb= orb;    
    }


    /** 
     *  returns the orb which all browser frames share. 
     *  On the first call it is created dynamically.
     */

    public org.omg.CORBA.ORB getORB()
    {
        if (_orb == null) 
            _orb= initORB();
        return _orb;
    }

    /** 
     * initializes the orb.
     */
    private org.omg.CORBA.ORB initORB()
    {
        org.jacorb.util.Debug.output(2, "orb initializing");
        org.omg.CORBA.ORB orb   = null;
        org.jacorb.orb.ORB jacorbORB= null;

        try
        { 
            orb = org.omg.CORBA.ORB.init(new String[0], null);
            jacorbORB = (org.jacorb.orb.ORB) orb;
        }     
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            System.out.println("SharedDataImpl.initORB: " + fail +
                               ", cannot continue.");
            System.exit(-1);
        }
        catch (Exception e)
        { 
            org.jacorb.util.Debug.output(1,e);
        }
        try 
        {
            Debug.output(Debug.DOMAIN | 6," before insert GUI orb domain");
            jacorbORB.mountORBDomain("GUI orb domain");
        }
        catch (org.omg.CORBA.COMM_FAILURE fail)
        {
            Debug.output(Debug.DOMAIN | Debug.INFORMATION,
                         "SharedDataImpl.initORB: " + fail +
                         ", skipping mounting of gui orb domain.");
        }
        catch (Exception e)
        { 
            org.jacorb.util.Debug.output(1,e);
        }
	
        org.jacorb.util.Debug.output(2, "orb initializaion finished");
        return orb;
    } // initORB
  
    /** 
     * return@ the orb domain of the orb returned by getORB().
     */
    public org.jacorb.orb.domain.Domain getORBDomain()
    {
        if (_orbDomain == null)
        {
            try 
            {
                _orbDomain = 
                    DomainHelper.narrow(getORB().resolve_initial_references("LocalDomainService"));
            org.jacorb.util.Debug.myAssert(1, 
                                         _orbDomain != null, 
                                         "orb domain  not running");

            }
            catch (org.omg.CORBA.ORBPackage.InvalidName inv) 
            {
                org.jacorb.util.Debug.output(1, "local orb domain not found at orb");
            }
            catch (Exception e) 
            {
                org.jacorb.util.Debug.output(1, e);
            }
        }
        return _orbDomain;
    } // getORBDomain


    //// reference counting of frames

    /**
     * registers a frame.
     */
    public synchronized void registerFrame(BrowserFrame frame)
    {
        _frames.put(frame, frame);
        // tell new frame if some buffers are filled 
        if ( ! DomainBufferIsEmpty() ) frame.enableDomainPasteMenuItem();
        if ( ! MemberBufferIsEmpty() ) frame.enableMemberPasteMenuItem();
        if ( ! PolicyBufferIsEmpty() ) frame.enablePolicyPasteMenuItem();
    }

    /**
     * deregisters a frame.
     */
    public synchronized void deregisterFrame(BrowserFrame frame)
    {
        _frames.remove(frame);
        if (getFrameCount() == 0) 
        {
            try 
            {
                frame.getDomainServer().deleteChild( this.getORBDomain() );
            }
            catch (org.omg.CORBA.COMM_FAILURE fail)
            {
                Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                             "SharedDataImpl.deregisterFrame: "
                             +" unmounting of gui orb domain not possible, skipping.");
            }
            ((org.jacorb.orb.ORB) _orb).shutdown(false);
            System.exit(0);
        }
    }
 
    /** 
     * @return the number of currently registered frames. 
     */
    public synchronized int getFrameCount()
    {
        return _frames.size();
    }
  
    /** 
     * returns  a property policy. This  policy is used by  a frame to
     * map from policy types to java class names which are then loaded
     * dynamically edit the policy of the corresponding type.  
     */

    public PropertyPolicy getPolicyEditors() 
    {
        Debug.myAssert(1, 
                     _policyEditors != null, 
                     "SharedDataImpl.PolicyEditors: _policyEditors"
                     +"is null");
        return _policyEditors;
    }

    /// buffers

    // member buffer

  
    /** 
     * returns an object member which has been copied into the buffer
     * via  setMemberBuffer.  The buffer  is initially empty.  If the
     * buffer is empty getMemberBuffer returns null.
     *
     * @param memberName  a string buffer is used  as an out parameter
     *  to  provide  the  previously  used name  of  the  member.  The
     * memberName is "" if the buffer is empty.  Because memberName is
     * used as an out  parameter a valid StringBuffer reference (means
     * not null) have to be provided prior to operation call
     */
    public org.omg.CORBA.Object getMemberBuffer(StringBuffer memberName) 
    {
        synchronized(_memberLock)
        {
            memberName.append(_memberNameBuffer);
            return _memberBuffer;
        }
    } 

    /** sets the member buffer with object member. A following call of
     *   getMemberBuffer will  return member.  Any following  calls of
     *  setMemberBuffer will overwrite the old value in the buffer.
     *  @param member the object to copy into the member buffer
     *  @param memberName the name of the member in the domain where 
     * it originated 
     */
    public void setMemberBuffer(org.omg.CORBA.Object member, String memberName)
    {
        synchronized(_memberLock)
        {
            boolean tellOtherFrames= false;
            if ( MemberBufferIsEmpty() ) tellOtherFrames= true;

            _memberBuffer    = member;
            _memberNameBuffer= memberName;

            if (tellOtherFrames) // update only if buffer has been empty and has been filled
            {
                // tell other frames that member buffer has been filled
                Enumeration frameEnum= _frames.keys();
                BrowserFrame frame= null;
                while ( frameEnum.hasMoreElements() ) 
                {
                    frame= (BrowserFrame) frameEnum.nextElement();
                    frame.enableMemberPasteMenuItem();
                }
            }
        }
    } // setMemberBuffer

    /** convenience operation to check if the member buffer is empty.
     *  @return true iff the result of getMemberBuffer is not null 
     */

    public boolean MemberBufferIsEmpty()
    {
        synchronized(_memberLock)
        {
            return (_memberBuffer == null);
        }
    }

    // policy buffer

    /** 
     *  returns  a policy  which has been  copied into the  buffer via
     *  setPolicyBuffer.  The buffer is initially empty. If the buffer
     *  is empty getPolicyBuffer returns null.  
     */

    public org.omg.CORBA.Policy getPolicyBuffer()
    {
        synchronized(_policyLock)
        {
            return _policyBuffer;
        }
    }

    /** 
     *  sets the policy  buffer. A  following call  of getPolicyBuffer
     *  will return  aPolicy. Any  following calls  of setPolicyBuffer
     * will overwrite the old value in the buffer.
     * @param aPolicy the policy to copy into the policy buffer 
     */

    public void setPolicyBuffer(org.omg.CORBA.Policy aPolicy)
    {
        synchronized(_policyLock)
        {
            boolean tellOtherFrames= false;
            if ( PolicyBufferIsEmpty() ) 
                tellOtherFrames= true;

            _policyBuffer    = aPolicy;

            if (tellOtherFrames) // update only if buffer has been empty and has been filled
            {
                // tell other frames that policy buffer has been filled
                Enumeration frameEnum= _frames.keys();
                BrowserFrame frame= null;
                while ( frameEnum.hasMoreElements() ) 
                {
                    frame= (BrowserFrame) frameEnum.nextElement();
                    frame.enablePolicyPasteMenuItem();
                }
            }
        }
    }


    /** 
     * convenience operation to check if the policy buffer is empty.
     * @return true iff the result of getPolicyBuffer is not null 
     */

    public boolean PolicyBufferIsEmpty()
    {
        synchronized(_policyLock)
        {
            return (_policyBuffer == null);
        }
    }

    // domain buffer

    /** 
     * @return a domain  which has  been copied  into the  buffer via
     * setDomainBuffer.  The buffer  is initially empty. If the buffer
     * is empty getDomainBuffer returns null.  
     */

    public org.jacorb.orb.domain.Domain getDomainBuffer()
    {
        synchronized(_domainLock)
        {
            return _domainBuffer;
        }
    }

    /**
     *  sets the domain  buffer. A  following call  of getDomainBuffer
     *  will return  aDomain. Any  following calls  of setMemberBuffer
     * will overwrite the old value in the buffer.
     *  @param aDomain the domain to copy into the domain buffer 
     */

    public void setDomainBuffer(org.jacorb.orb.domain.Domain aDomain)
    {
        synchronized(_domainLock)
        {
            boolean tellOtherFrames= false;
            if ( DomainBufferIsEmpty() ) tellOtherFrames= true;

            _domainBuffer    = aDomain;

            if (tellOtherFrames) // update only if buffer has been empty and has been filled
            {
                // tell other frames that domain buffer has been filled
                Enumeration frameEnum= _frames.keys();
                BrowserFrame frame= null;
                while ( frameEnum.hasMoreElements() ) 
                {
                    frame= (BrowserFrame) frameEnum.nextElement();
                    frame.enableDomainPasteMenuItem();
                }
            }
        }
    }

    /** 
     * convenience operation to check if the domain buffer is empty.
     * @return true iff the result of getDomainBuffer is not null 
     */

    public boolean DomainBufferIsEmpty()
    {
        synchronized(_domainLock)
        {
            return (_domainBuffer == null);
        }
    }

    /** 
     * creates and initializes a property policies from the org.jacorb
     * properties file 
     */

    private void initPropertyPolicy()
    {
        Domain localDomain;
        org.jacorb.orb.domain.PropertyPolicy prop;
   
        localDomain= getORBDomain();
    
        Hashtable result= new Hashtable();
        PropertyPolicyImpl.updatePropertyPolicies(Environment.getProperties(),
                                                  "jacorb.policy.", result, localDomain);
    
        Enumeration policyEnum= result.elements();
        org.jacorb.util.Debug.output(Debug.DOMAIN | 4, 
                                     "found " + result.size() + 
                                     " policies");
        // convert Enumeration to array
      
        while ( policyEnum.hasMoreElements() )
        {
            prop= (PropertyPolicy) policyEnum.nextElement();
            try
            {
                localDomain.set_domain_policy(prop);
            }
            catch (PolicyTypeAlreadyDefined already)
            {
                Debug.output(Debug.DOMAIN | 1,
                             " policy of type " +
                             prop.policy_type()
                             +" already defined in local orb domain ");
            }
        }
        _policyEditors = (PropertyPolicy)result.get("PolicyEditor");
    } // initPropertyPolicy
  
} // SharedData





