package org.jacorb.orb.domain.gui;

import org.jacorb.orb.domain.*;

/**
 * SharedData.java
 * An interface for sharing data between browser frames.
 * Implementations of this class provide the following data for browser frames:
 * - an orb
 * - a simple frame reference count mechanism
 * - a property policy called "policy editors"
 * @author Herbert Kiefer
 * @version $Revision$
 */

public interface SharedData
{
  /** returns the orb which all browser frames share. 
   *  On the first call it is created dynamically.*/
  public org.omg.CORBA.ORB getORB();

  /** returns the orb domain of the orb returned by getORB().*/
  public org.jacorb.orb.domain.Domain getORBDomain();

  //// reference counting of frames

  /** registers a frame.
   */
  public void registerFrame(BrowserFrame frame);

 /** deregisters a frame.
   */
  public void deregisterFrame(BrowserFrame frame);
 
  /** returns the number of currently registered frames. */
  public int getFrameCount();

  /** returns a property policy. This policy is used by a frame to map from 
   *  policy types to java class names which are then loaded dynamically edit the
   *  policy of the corresponding type. 
   */
  public PropertyPolicy getPolicyEditors();

  /// buffers

  // member buffer

  /** returns an object member which has been copied into the buffer via setMemberBuffer. 
   *  The buffer is initially empty. If the buffer is empty getMemberBuffer returns null. 
   * @param memberName a string buffer is used as an out parameter to provide the previously
   *                   used name of the member. The memberName is "" if the buffer is empty.
   *                   Because memberName is used as an out parameter a valid StringBuffer
   *                   reference (means not null) have to be provided prior to operation call
   */
  org.omg.CORBA.Object getMemberBuffer(StringBuffer memberName);

  /** sets the member buffer with object member. A following call of getMemberBuffer will
   *  return member. Any following calls of setMemberBuffer will overwrite the old value
   *  in the buffer.
   *  @param member the object to copy into the member buffer
   *  @param memberName the name of the member in the domain where it originated
   */
  public void setMemberBuffer(org.omg.CORBA.Object member, String memberName);

  /** convinience operation to check if the member buffer is empty.
   *  @return true iff the result of getMemberBuffer is not null */
  public boolean MemberBufferIsEmpty();

  // policy buffer

 /** returns a policy which has been copied into the buffer via setPolicyBuffer. 
   *  The buffer is initially empty. If the buffer is empty getPolicyBuffer returns null. 
   */
  public org.omg.CORBA.Policy getPolicyBuffer();

  /** sets the policy buffer. A following call of getPolicyBuffer will
   *  return aPolicy. Any following calls of setPolicyBuffer will overwrite the old value
   *  in the buffer.
   *  @param aPolicy the policy to copy into the policy buffer
   */
  public void setPolicyBuffer(org.omg.CORBA.Policy Policy);

  /** convinience operation to check if the policy buffer is empty.
   *  @return true iff the result of getPolicyBuffer is not null */
  public boolean PolicyBufferIsEmpty();

  // domain buffer

 /** returns a domain which has been copied into the buffer via setDomainBuffer. 
   *  The buffer is initially empty. If the buffer is empty getDomainBuffer returns null. 
   */
  public org.jacorb.orb.domain.Domain getDomainBuffer();

  /** sets the domain buffer. A following call of getDomainBuffer will
   *  return aDomain. Any following calls of setMemberBuffer will overwrite the old value
   *  in the buffer.
   *  @param aDomain the domain to copy into the domain buffer
   */
  public void setDomainBuffer(org.jacorb.orb.domain.Domain aDomain);

  /** convinience operation to check if the domain buffer is empty.
   *  @return true iff the result of getDomainBuffer is not null */
  public boolean DomainBufferIsEmpty();
} // SharedData




