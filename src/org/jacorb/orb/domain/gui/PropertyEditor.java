package org.jacorb.orb.domain.gui;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.jacorb.util.Debug;
import org.jacorb.orb.domain.*;
import org.jacorb.orb.domain.PropertyPolicy;
import java.awt.event.*;

/**
 * Objects of this class are editors for a property policy.
 * @author Herbert Kiefer
 * @version 1.0
 */
public class PropertyEditor 
    extends DefaultPolicyEditor
{
    /** the model used by the table to hold the data, see class at end of file */
    private org.jacorb.orb.domain.gui.PropertyTableModel theTableModel;

    /** whether to use warning messages when deleting rows or not */
    private boolean _useWarningMessagesOnDelete= false;

    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane TableScrollPane = new JScrollPane();
    JPanel MainPanel = new JPanel();
    Box ButtonBox;
    JTable PropertyTable = new JTable();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton NewButton = new JButton();
    JButton DeleteButton = new JButton();
    JLabel PolicyLabel = new JLabel();
    Component component1;
    JButton LoadButton = new JButton();
    JButton StoreButton = new JButton();


    public PropertyEditor()
    {
        _useWarningMessagesOnDelete= true;
        // this(null, true);
    }
    public PropertyEditor(PropertyPolicy propertyPolicy)
    {
        this(propertyPolicy, true);
    }
    /** constructor.
     * @param propertyPolicy the policy this editor edits
     * @param useWarnings if set to true, warning messages are displayed on
     *    row deletion
     */
    public PropertyEditor(PropertyPolicy propertyPolicy, boolean useWarnings)
    {
        super(propertyPolicy);
        try
        {
            _useWarningMessagesOnDelete= useWarnings;
            jbInit();
            MyInit(propertyPolicy);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /////// PolicyEditor interface overrides

    /** returns the property policy type. This editor is a editor for 
     *  a property policy. */
    public int getPolicyTypeResponsibleFor()
    {
        return org.jacorb.orb.domain.PROPERTY_POLICY_ID.value;
    }

    /** sets the policy the editor should edit. reinits the editor */
    public void setEditorPolicy(org.omg.CORBA.Policy policyToEdit)
    {
        _policy= policyToEdit;
        try
        {
            jbInit();
            MyInit( (PropertyPolicy) PropertyPolicyHelper.narrow( policyToEdit ) );
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    } // setEditorPolicy

    /** returns "Properties" as title of this editor. */
    public String getTitle()
    {
        return "Properties";
    }

    /** handmade initialization. */

    private void MyInit(PropertyPolicy propertyPolicy)
    {
        // init label
        PolicyLabel.setText(propertyPolicy.name() + 
                            " (type " + propertyPolicy.policy_type() + ")");
        // init table model
        theTableModel= new PropertyTableModel(propertyPolicy);
        PropertyTable.setModel(theTableModel);
        // theTableModel.addTableModelListener(the);
        PropertyTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        ListSelectionModel selectionModel= PropertyTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    Debug.output(Debug.DOMAIN | 4, e.toString());
                    if ( e.getValueIsAdjusting() ) return; // selection still in progress
                    ListSelectionModel listSelection= (ListSelectionModel) e.getSource();
                    // enable delete button if selection is not empty
                    DeleteButton.setEnabled( ! listSelection.isSelectionEmpty() );
                }
            }
                                                );

    } // MyInit

    private void jbInit() throws Exception
    {
        component1 = Box.createVerticalStrut(8);
        NewButton.setToolTipText("create a new entry in the table");
        NewButton.setMnemonic('N');
        NewButton.setText("New");
        NewButton.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnButtonNew(e);
                }
            });
        ButtonBox = Box.createVerticalBox();
        this.setLayout(borderLayout1);
        MainPanel.setLayout(borderLayout2);
        DeleteButton.setEnabled(false);
        DeleteButton.setToolTipText("delete the selected row(s)");
        DeleteButton.setMnemonic('T');
        DeleteButton.setText("Delete");
        DeleteButton.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnButtonDelete(e);
                }
            });
        PolicyLabel.setFont(new java.awt.Font("Serif", 1, 14));
        PolicyLabel.setToolTipText("");
        PolicyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        PolicyLabel.setText("name and id");
        LoadButton.setToolTipText("load/merge table entries from file");
        LoadButton.setMnemonic('O');
        LoadButton.setText("Load ...");
        LoadButton.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnButtonLoad(e);
                }
            });
        StoreButton.setToolTipText("store table entries to file");
        StoreButton.setMnemonic('S');
        StoreButton.setText("Store ...");
        StoreButton.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    OnButtonStore(e);
                }
            });
        this.add(MainPanel, BorderLayout.CENTER);
        MainPanel.add(ButtonBox, BorderLayout.EAST);
        ButtonBox.add(NewButton, null);
        ButtonBox.add(DeleteButton, null);
        ButtonBox.add(component1, null);
        ButtonBox.add(LoadButton, null);
        ButtonBox.add(StoreButton, null);
        MainPanel.add(TableScrollPane, BorderLayout.CENTER);
        MainPanel.add(PolicyLabel, BorderLayout.NORTH);
        TableScrollPane.getViewport().add(PropertyTable, null);

        // TableScrollPane.setViewportView(PropertyTable);
    }

    public void setTableModel(org.jacorb.orb.domain.gui.PropertyTableModel newTableModel)
    {
        theTableModel = newTableModel;
    }

    public org.jacorb.orb.domain.gui.PropertyTableModel getTableModel()
    {
        return theTableModel;
    }

    /** creates a new property (name, value) entry in the table. updates the model.
     *  The name and the value of the newly created property are empty. If a
     *  property with an empty name already exists in the table (model), the name
     *  becomes "NEW" plus a unique counter value.
     */
    void OnButtonNew(ActionEvent e)
    {
        PropertyTableModel tableModel= (PropertyTableModel) PropertyTable.getModel();
        PropertyPolicy pol= tableModel.getPropertyPolicy();

        // insert an empty property name-value pair
        String name= "";
        String value= "";

        String prefix= "";
        boolean finished= false;
        int counter= 0;
        while (! finished )
        {
            try
            {
                pol.put(prefix+name, value);
                tableModel.setPropertyPolicy(pol);
                int index= tableModel.getIndex(name);
                tableModel.fireTableRowsInserted(index, index);
                finished= true;
            }
            catch (PropertyAlreadyDefined already)
            {
                Debug.output(Debug.DOMAIN | 6, "PropertyEditor.OnButtonNew: prop "
                             + prefix + name + "already defined");
                // change prefix until name is unique
                counter++;
                prefix= "NEW " + counter;
                finished= false;

            }
        } // while
    } // OnButtonNew

    /** deletes all selected rows in the table */
    void OnButtonDelete(ActionEvent e)
    {
        ListSelectionModel selection= PropertyTable.getSelectionModel();
        if ( selection.isSelectionEmpty() ) return;


        PropertyTableModel tableModel= (PropertyTableModel) PropertyTable.getModel();
        PropertyPolicy pol= tableModel.getPropertyPolicy();

        int start= selection.getMinSelectionIndex();
        int end  = selection.getMaxSelectionIndex();

        if (_useWarningMessagesOnDelete)
        {
            int answer= JOptionPane.showConfirmDialog(this, "Do you really want to "
                                                      +" delete row(s) " + start + " to " + end + "?", "Confirm Deletion",
                                                      JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.NO_OPTION) return;
        }
        String name;
        for (int i= start; i <= end; i++)
        {
            name= tableModel.getNameAt(i);

            pol.removeProperty(name);
            org.jacorb.util.Debug.output(Debug.DOMAIN | 6, "detected name " + name + " at index " + i);
        }
        tableModel.setPropertyPolicy(pol);
        DeleteButton.setEnabled(false);
        tableModel.fireTableRowsDeleted(start, end);

    }

    /** opens a file chooser dialog and saves properties to file.*/
    void OnButtonStore(ActionEvent e)
    {
        JFileChooser dialog= new JFileChooser();
        int answer= dialog.showSaveDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                PropertyPolicyImpl.storeToFile(theTableModel.getPropertyPolicy(),
                                               dialog.getSelectedFile(), "jacorb.policy." );
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, ex, "Output Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } // OnButtonStore

    /** opens a file chooser dialog and loads properties from user given file. */
    void OnButtonLoad(ActionEvent e)
    {
        JFileChooser dialog= new JFileChooser();
        int answer= dialog.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                PropertyPolicyImpl.loadFromFile(theTableModel.getPropertyPolicy(),
                                                dialog.getSelectedFile(), "jacorb.policy." );
                // now the property policy of the table contains new data tell to table
                theTableModel.update();
                theTableModel.fireTableDataChanged();
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, ex, "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    } // OnButtonLoad

}

/**  A private table model for the property editor. */
class PropertyTableModel extends AbstractTableModel
  {
    /** the property policy the this table model relies on */
    private PropertyPolicy thePropertyPolicy;
    /** an array of valid names (keys) */
    private String[] theValidNames;

    PropertyTableModel(PropertyPolicy prop)
    {
      super();
      thePropertyPolicy= prop;
      update();
    }

    public PropertyPolicy getPropertyPolicy() { return thePropertyPolicy; }
    public void setPropertyPolicy(PropertyPolicy pol)
    {
      thePropertyPolicy= pol;
      update();
    }

    /** reinitializes data structures of table model to reflect data
     *  changes in underlying property policy.
     */
    public void update()
    {
      theValidNames= thePropertyPolicy.getPropertyNames();
      Util.quicksort(0, theValidNames.length - 1, theValidNames);
    } // update

    public int getRowCount() { return thePropertyPolicy.getPropertyCount(); }
    public int getColumnCount() { return 2; }
    public String getColumnName(int column)
    {
      if (column == 0) return "Property Name";
      else return "Property Value";
    }

    public boolean isCellEditable(int row, int column)
    {
      // only properties value are editable
      // if (column == 1) return true;
      // else return false;
    return true;
    }

    public Object getValueAt(int row, int column)
    {
      if (column == 0)
        // return property name
        return theValidNames[row];
      else // return property value
        return thePropertyPolicy.getValueOfProperty(theValidNames[row]);
    } // getValueAt

    /** called after the table has changed a value in a cell. */
    public void setValueAt(Object value, int row, int column)
    {
      String update= (String) value;
      Debug.output(Debug.DOMAIN | 4, "TableModel.setValueAt: value has changed"
      +" to " + update + " @ (" + row + "," + column +")");
      if (column == 0)
        {  // name column
          String oldName= theValidNames[row];
          if ( oldName.equals(update) ) return; // no real changes
          if ( thePropertyPolicy.containsProperty(update) )
            {
              // show message box
              JOptionPane.showMessageDialog(null, "The new name is already in "
              +" use. Names must be unique. Please choose another one."," Error" , JOptionPane.ERROR_MESSAGE);
              return;
            }
          // ok, update valid, do it
          // remove old property and insert it with a new name
          String oldValue= thePropertyPolicy.getValueOfProperty(oldName);
          thePropertyPolicy.removeProperty(oldName);
          try
            {
              thePropertyPolicy.put(update, oldValue);
            }
          catch (PropertyAlreadyDefined already)
            {
              Debug.output(Debug.DOMAIN | 4, "property name " + oldName
                + " already defined.");
            }
          theValidNames[row]= update; // update valid names

        } // name column
      else
        { // value column
          thePropertyPolicy.changeValueOfProperty(theValidNames[row], update);
        }
    }

    /** returns the index (== row) of a name in the table model.
     * returns -1 if not found.
     */
    public int getIndex(String name)
    {
      for (int i= 0; i < theValidNames.length; i++)
        if (theValidNames[i].equals(name)) return i;
      // not found
      return -1;
    }

    /** returns the name at position i */
    public String getNameAt(int i)
    {
      return theValidNames[i];
    }
  } // PropertyTableModell






