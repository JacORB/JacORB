package demo.hello;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.omg.CORBA.*;

/**
 * Applet adopted from Chapter 5, Vogel/Duddy, "Java Programming with CORBA"
 */

public class Applet
    extends java.applet.Applet
    implements ActionListener 
{

    private ORB orb;
    private GoodDay goodDay;
    private Button helloWorldButton;
    private TextField textField;

    public void init() 
    {
        helloWorldButton = new Button("Invoke remote method");
        helloWorldButton.setFont(new Font("Helvetica",
					  Font.BOLD, 20));
	helloWorldButton.setActionCommand("invoke");
	helloWorldButton.addActionListener( (ActionListener) this );

        textField = new TextField();
        textField.setEditable(false);
        textField.setFont(new Font("Helvetica", Font.BOLD, 14));
        
        setLayout( new GridLayout(2,1));
        add( helloWorldButton );
        add( textField );

        try 
	{
            // initilaze the ORB (using this applet)
            orb = ORB.init( this, null );

            // bind to object
            goodDay = GoodDayHelper.narrow( orb.string_to_object( readIOR() ));
        }
        // catch exceptions
        catch(SystemException ex) 
	{
	    System.err.println("ORB is not initialized");
	    System.err.println(ex);
        }
    }

    public void actionPerformed( ActionEvent e ) 
    {

	if( e.getActionCommand().equals("invoke") ) 
	{

            // invoke the operation
            try 
	    {
                textField.setText( goodDay.hello() );
            }

            // catch CORBA system exceptions
            catch(SystemException ex) 
	    {
		ex.printStackTrace();
                System.err.println(ex);
            }
        }
    }

    private String readIOR()
    {
	try
	{
	    System.out.println("Trying to read from " +getCodeBase().toString()+"ior");
	    URL iorURL = new URL( getCodeBase().toString()+"ior");
	    String line;
	    BufferedReader in = new BufferedReader(new InputStreamReader(iorURL.openStream()) ); 
	    line = in.readLine();	    	
	    in.close();
	    return line;
	}
	catch( Exception ex )
	{
	    System.err.println(ex);
	}
	return null;
    }

}
