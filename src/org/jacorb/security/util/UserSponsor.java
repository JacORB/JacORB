package org.jacorb.security.util;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A user sponsor, i.e. a frame that asks for user input
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class UserSponsor
    extends JDialog
    implements ActionListener  
{    
    private JButton okButton;
    private JButton cancelButton;
    private boolean done = false;
    private boolean cancelled = false;
    private JPasswordField [] opaqueFields;
    private JTextField[] clearFields;
    private JComboBox[] listOptions;

    /**
     */
    UserSponsor(String title, 
		String message, 
		String[] clear_input_labels, 
		String[] opaque_input_labels)
    {
	this(title, message,  clear_input_labels, null, null, opaque_input_labels);
    }



    /**
     */
    UserSponsor(String title, 
		String message, 
		String[] clear_input_labels,
		String[] list_option_labels, 
		String[][] input_options,
		String[] opaque_input_labels)
    {
	if( list_option_labels != null && input_options != null &&
	    list_option_labels.length != input_options.length )
	{
	    throw new IllegalArgumentException(
		 "Number of list option labels must match number of input option lists!") ;
	}

	setTitle(title);
	setModal(true);

	done = false;
	cancelled = false;

	int clear_len = ( clear_input_labels != null ? clear_input_labels.length : 0 );
	int list_len = (  list_option_labels != null ? list_option_labels.length : 0 );
	int opaque_len = ( opaque_input_labels != null ? opaque_input_labels.length : 0 );

	JPanel[] panels = new JPanel[clear_len + list_len + opaque_len + 2];

	clearFields = new JTextField[clear_len];
	opaqueFields = new JPasswordField[opaque_len];
	listOptions = new JComboBox[list_len];
	
	panels[0] = new JPanel();
	panels[0].setLayout( new BorderLayout());
	panels[0].add( new JLabel( message ));

	int idx = 1;

	if( clear_len > 0 )
	{
	    for( int i = 0; i < clear_len; i++ )
	    {
		panels[idx+ i] = new JPanel();
		panels[idx+ i].setLayout( new BorderLayout());
		panels[idx+ i].add( new JLabel( clear_input_labels[i]), BorderLayout.WEST);
		JTextField jtext = new JTextField(20);
		jtext.addActionListener( this );
		clearFields[i] = jtext;
		panels[idx+ i].add( clearFields[i],BorderLayout.EAST);
		panels[idx+i-1].add(panels[idx + i], BorderLayout.SOUTH);
	    }
	}

	idx += clear_len;

	for( int i = 0; i < opaque_len; i++ )
	{
	    panels[idx + i] = new JPanel();
	    panels[idx + i].setLayout( new BorderLayout());
	    panels[idx + i].add( new JLabel( opaque_input_labels[i]), BorderLayout.WEST);
	    JPasswordField jpwd = new JPasswordField(20);
	    jpwd.addActionListener( this );
	    opaqueFields[i] = jpwd;
	    panels[idx + i].add( opaqueFields[i],BorderLayout.EAST);
	    panels[idx + i-1].add(panels[idx + i], BorderLayout.SOUTH);
	}

	idx += opaque_len;

	for( int i = 0; i < list_len; i++ )
	{
	    panels[idx + i] = new JPanel();
	    panels[idx + i].setLayout( new BorderLayout());
	    panels[idx + i].add( new JLabel( list_option_labels[i]), BorderLayout.WEST);
	    listOptions[i] = new JComboBox( input_options[i] );
	    listOptions[i].setEditable(false);
	    panels[idx + i].add( listOptions[i],BorderLayout.EAST);
	    panels[idx + i-1].add(panels[idx + i], BorderLayout.SOUTH);
	}

	panels[panels.length-1] = new JPanel();

	okButton = new JButton ("OK");
	okButton.addActionListener(this);

	panels[panels.length-1].add(okButton);

	cancelButton = new JButton ("Cancel");
	cancelButton.addActionListener(this);

	panels[panels.length-1].add(cancelButton);

	panels[panels.length-2].add( panels[panels.length-1], BorderLayout.SOUTH);

	getContentPane().add(panels[0]);
	pack();
	setVisible(true);

    }



    // implementation of java.awt.event.ActionListener interface

    /**
     *
     * @param param1 <description>
     */

    public void actionPerformed(ActionEvent event) 
    {
       if( event.getSource() instanceof JTextField )
       {
	   cancelled = false;
       }
       else
       {
	   JButton _source = (JButton) event.getSource();
	
	   if (_source == cancelButton)
	   {
	       cancelled = true;
	   }
	   else if (_source == okButton)
	   {
	       cancelled = false;
	   }
       }

       done = true;
       synchronized( this )
       {
	   notifyAll();
       }
       setVisible(false);
       dispose();
    }

    public boolean getInput(String[] clear_input, 
			    char[][] opaque_input)
    {

	return getInput(clear_input, null, opaque_input);
    }

    public boolean getInput(String[] clear_input, String[] list_input,
			    char[][] opaque_input)
    {
	if( list_input != null && 
	    ( listOptions == null || 
		( listOptions.length != list_input.length )
	      )
	    )
	{
	    throw new IllegalArgumentException("Length of input list must match number of option lists!") ;
	}

	while(!done)
	{
	    try
	    {	    
		synchronized( this )
		{
		    wait();
		}
	    }
	    catch( InterruptedException ie )
	    {
		cancelled = true;
		done = true;
	    }
	};
	
	if( !cancelled )
	{
	    if( clear_input != null )
	    {
		for( int i = 0; i < clear_input.length; i++ )
		    clear_input[i] = clearFields[i].getText();
	    }
	    
	    if( opaque_input != null )
	    {
		for( int i = 0; i < opaque_input.length; i++ )
		    opaque_input[i] =opaqueFields[i].getPassword();
	    }


	    if( list_input != null )
	    {
		for( int i = 0; i < list_input.length; i++ )
		{
		    int idx = listOptions[i].getSelectedIndex();
		    if( idx == -1 )
			list_input[i] = "<unselected";
		    else
			list_input[i] = (String)listOptions[i].getModel().getElementAt(idx);
		}
	    }
	}
	
	return !cancelled;
    }

    public static char[] getPasswd(String message)
    {
	    char[][] passwordHolder = new char[1][];
	    UserSponsor us = new UserSponsor( "Password", 
					      message, 
					      null, new String[] { "Password" }
					      );
	    us.getInput ( null, passwordHolder );	    
	    return passwordHolder[ 0 ];
    }


}


