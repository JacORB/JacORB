package  org.jacorb.orb.domain.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.PrintIOR;

/** 
 * This dialog box display properties of an org.omg.CORBA.Object.
 */

public class PropertyDialog 
    extends JDialog 
{
    // handmade
    /** the object which properties are displayed */
    org.omg.CORBA.Object _object;

    JTabbedPane TabbedPane = new JTabbedPane();
    JLabel TypeIdLabel = new JLabel();
    JLabel HostLabel = new JLabel();
    JLabel ObjectKeyLabel = new JLabel();

    public PropertyDialog(org.omg.CORBA.Object obj,
                          Frame frame, 
                          String title, 
                          boolean modal ) 
    {
        super( frame, title, modal );

        try 
        {
            jbInit();

            // do handmade initialization AFTER jbInit
            _object = obj;
            initIORPane( _object );

            setLocationRelativeTo( frame ); // put dialog into center of parent
        }
        catch( Exception e ) 
        {
            e.printStackTrace();
        }

        pack();
    }

    public PropertyDialog( org.omg.CORBA.Object obj, 
                           Frame frame, 
                           String title ) 
    {
        this(obj, frame, title, false);
    }

    public PropertyDialog( org.omg.CORBA.Object obj, 
                           Frame frame ) 
    {
        this(obj, frame, "", false);
    }

    private void jbInit() throws Exception 
    {
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();
        getContentPane().setLayout( gbl );

        //add tabbed pane
        buildConstraints( constraints, 0, 0, 1, 1 );
        constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 10;
	constraints.weighty = 10;
        gbl.setConstraints( TabbedPane, constraints );
        getContentPane().add( TabbedPane );        

        //add ok button
        JButton OkButton = new JButton( "OK" );
        OkButton.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent evt ){
                dispose();
            }
        } );
        buildConstraints( constraints, 0, 1, 1, 1 );
        gbl.setConstraints( OkButton, constraints );
        getContentPane().add( OkButton );        

        add( "IOR", createIORPanel() );

        addWindowListener( new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        } );
    }

    private JPanel createIORPanel()
    {
        JPanel panel = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout( gbl );

        JPanel inner = new JPanel();
        inner.setLayout( new BorderLayout() );
        inner.setBorder( BorderFactory.createTitledBorder( "IOR Components" ));
        
        inner.add( BorderLayout.NORTH, TypeIdLabel );
        inner.add( BorderLayout.CENTER, HostLabel );
        inner.add( BorderLayout.SOUTH, ObjectKeyLabel );

        buildConstraints( constraints, 0, 0, 1, 1 );
        gbl.setConstraints( inner, constraints );
        panel.add( inner ); 

        return panel;
    }

    /** initializes the labels of the IOR pane */
    public void initIORPane( org.omg.CORBA.Object obj )
    {

        ParsedIOR pior= new ParsedIOR( obj.toString() );
        org.omg.IOP.IOR ior= pior.getIOR();

        // extract values from ior/pior and put them into corresponding labels
        TypeIdLabel.setText( "Type ID: " + ior.type_id );
        HostLabel.setText( "Host: " + pior.getAddress() );
        ObjectKeyLabel.setText( "Object Key: " + dumpHex( pior.get_object_key() ));
    } 


    /** converts an object key from byte to string. */
    public static String dumpHex(byte bs[])
    {
        StringBuffer result = new StringBuffer();
        //String result= "";
        for (int i=0; i<bs.length; i++)	
        {
            int n1 = (bs[i] & 0xff) / 16;
            int n2 = (bs[i] & 0xff) % 16;

            char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
            char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));

            result.append( c1 );
            result.append( c2 );
            result.append( ' ' );
            //result= result + c1 + (c2 + " ");
        }
        return result.toString();
    } // dumpHex

    /** adds a component to the tabbed pane at the end.
     * @param title the title to be display on the tab
     * @param comp the component to add
     */
    public Component add(String title, Component comp)
    {
        TabbedPane.addTab(title, comp);
        return comp;
    }

    /** adds a component to the tabbed pane at the specified index.
     * @param title the title to be display on the tab
     * @param comp the component to add
     * @param index the index where to add. A index of -1 means at the end of
     *              the components list.
     */
    public Component add(String title, Component comp, int index)
    {
        TabbedPane.insertTab(title, null, comp, title, index);
        return comp;
    }

    private void buildConstraints( GridBagConstraints gbc, 
                                   int gx, 
                                   int gy, 
                                   int gw, 
                                   int gh )
    {
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = 0;
	gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
    }
} // PropertyDialog







