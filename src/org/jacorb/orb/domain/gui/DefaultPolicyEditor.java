package org.jacorb.orb.domain.gui;

/**
 * A  default implementation  of the interface  PolicyEditor. Subclass
 * this class  for your implementation of your  own policy editor. You
 * should  override the  operations setEditorPolicy and  getTitle. The
 * others may work for you.
 *
 * Created: Tue Jul  4 20:54:41 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$ 
 */

public class DefaultPolicyEditor 
    extends javax.swing.JPanel 
    implements PolicyEditor 
{
    /** the policy this editor edits. */
    protected org.omg.CORBA.Policy _policy;

    public org.omg.CORBA.ORB orb;

    public DefaultPolicyEditor() 
    {
    }

    public DefaultPolicyEditor( org.omg.CORBA.Policy pol )
    {
        _policy= pol;
    }

    public void setORB(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
    }

    /**
     *  @return the policy the editor edtis.
     */

    public org.omg.CORBA.Policy getEditorPolicy()
    {
        return _policy;
    }

    /**
     * sets the policy the editor should edit. reinits the editor 
     */

    public void setEditorPolicy(org.omg.CORBA.Policy policyToEdit)
    {
        _policy = policyToEdit;
    }
  
    /**
     *  @return the  type of policy this editor  is intended for.  (Do
     *  override this operation ! )??  
     */

    public int getPolicyTypeResponsibleFor()
    {
        return _policy.policy_type();
    }

    /** 
     *  @return  the graphical component of the  editor. The graphical
     *  component is used to  display the editor. Normally this is the
     *  main panel the editor is using.  
     */

    public java.awt.Component getGraphicalComponent()
    {
        return this;
    }

    /** 
     *  @return the title of this  editor. The title is a string which
     *  is used in grapical displayment to describe the editor. 
     */
    public String getTitle()
    {
        return "DefaultPolicyEditor";
    }

} // DefaultPolicyEditor






