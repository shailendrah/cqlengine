package oracle.cep.driver.view.menu;

import java.awt.event.*;
import javax.swing.*;
import oracle.cep.driver.data.*;
import oracle.cep.driver.view.ClientView;
import oracle.cep.driver.view.dialog.InputInfo;

public class InputsMenu extends JMenu implements ClientListener {
    ClientView clientView;
    Client client;
    
    public InputsMenu (Client client, ClientView clientView) {
	super ("Inputs");
	this.clientView = clientView;
	this.client = client;
	client.addListener (this);	
	setMnemonic (KeyEvent.VK_I);
    }
    
    public void stateChanged (int oldState, int newState) {}
    public void queryAdded (Query query, UnnamedTable outSchema) {}
    public void viewAdded (Query query, NamedTable table) {}
    public void queryResultAvailable (QueryResult result) {}
    public void planGenerated (QueryPlan plan) {}
    public void monitorAdded (Monitor mon, Query qry, 
			      QueryResult res, QueryPlan plan) {}
    
    public void baseTableAdded (NamedTable table) {
	add (new TableInputMenuItem (table));
    }
    
    public void resetEvent () {
	removeAll();
    }    
    
    public void closeEvent () {
	
    }
    
    private class TableInputMenuItem extends JMenuItem 
	implements ActionListener {
	
	private NamedTable table;
	
	TableInputMenuItem (NamedTable table) {
	    super ();
	    this.table = table;
	    String prefix = (table.isStream())? "stream: " : "relation: ";
	    setText (prefix + table.getTableName());
	    addActionListener (this);
	}
	
	public void actionPerformed (ActionEvent e) {
	    if (e.getSource() != this)
		return;
	    
	    clientView.createAndShowTableFeederView(table);
	}
    }
}
