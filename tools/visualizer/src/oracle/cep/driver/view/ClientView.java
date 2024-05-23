package oracle.cep.driver.view;

import oracle.cep.driver.data.*;
import oracle.cep.driver.view.dialog.*;
import oracle.cep.driver.view.input.*;
import oracle.cep.driver.view.result.StreamDisplay;
import oracle.cep.driver.view.result.RelationDisplay;
import oracle.cep.driver.view.menu.MenuBar;
import oracle.cep.driver.util.*;
import oracle.cep.driver.net.*;
import oracle.cep.common.Constants;

import java.sql.SQLException;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.net.UnknownHostException;

import org.w3c.dom.*;

public class ClientView extends JFrame 
    implements ActionListener, ClientListener {
    
    /// The client who we are visualizing
    private Client client;
    
    // Dimensions
    private int width;
    private int height;

    /// display positions
    private int feederViewStartX;
    private int feederViewStartY;
    private int queryResultStartX;
    private int queryResultStartY;
    
    private static final int FEEDERVIEW_XINC    = 220;
    private static final int FEEDERVIEW_YINC    = 0;
    private static final int QUERYRESULT_XINC   = 60;
    private static final int QUERYRESULT_YINC   = 30;    
    private static final int FEEDERVIEW_XINIT   = 0;
    private static final int FEEDERVIEW_YINIT   = 0;
    private static final int QUERYRESULT_XINIT  = 0;
    private static final int QUERYRESULT_YINIT  = 120;    
    
    private static final float LEGEND_WIDTH     = 0.3f;    
    private static final float LEGEND_HEIGHT    = 0.15f;    
    private static final float PLAN_PANE_WIDTH  = 0.6661f;    
    private static final float PLAN_PANE_HEIGHT = 1.0f;

    /// init manager
    public InitManager initMgr = new InitManager();

    //------------------------------------------------------------
    // (Main) Subcomponents of the visualizer
    //------------------------------------------------------------
    
    /// The pane in which the query plans are shown
    private JScrollPane mainPane;
    
    /// Pane showing the list of registered tables (displayed in mainPane)
    private NamedTableList tableList;
    
    /// Pane showing the list of queries (displayed in mainPane)
    private QueryList queryList;
    
    /// The list of plan views
    private List planViews;
    
    /// Mapping from query -> plans
    private Map qryToPlanView;
    
    /// menu bar
    private MenuBar menuBar;
    
    /// The entity observer pane where information about the selected
    /// components of a plan are shown
    EntityObserver entityObs;    
    
    //------------------------------------------------------------
    // The buttons for various commands
    //------------------------------------------------------------
    
    /// Register a stream
    JButton regStreamButton;
    
    /// Register a relation
    JButton regRelationButton;
    
    /// Register a query
    JButton regQueryButton;
    
    /// Generate plan
    JButton genPlanButton;
    
    /// Execute
    JButton runButton;
    
    /// Reset
    JButton resetButton;
    
    /// Bind Stream source
    JButton bindStrmSrc;
    
    /// Bind Relation source
    JButton bindRelnSrc;
    
    /// Bind Query Destination
    JButton bindQueryDestn;

    /// Plan2
    JButton plan2Button;
    
    /// Start query
    JButton startButton;
    
    
    //------------------------------------------------------------
    // The dialogs 
    //------------------------------------------------------------
    
    RegisterInput regStreamDialog;
    
    RegisterInput regRelationDialog;
    
    RegisterQuery regQueryDialog;
    
    //BindStreamSrc bindStrmSrcDialog;
    BindSrcDest bindStrmSrcDialog;
    
    BindSrcDest bindRelnSrcDialog;
    
    BindSrcDest bindQueryDestDialog;
    
    StartNamedQuery startQueryDialog;
    
    
    //------------------------------------------------------------
    // Stream/Relation output & input displays
    //------------------------------------------------------------
    
    /// List of displays showing an output stream
    List streamDisplays;
    
    /// List of displays showing an output relation
    List relnDisplays;
    
    /// List of tables
    List tables;

    /// mapping from tables to TableFeederViews
    Map  tableFeederViews;

    /// mapping from tables to TableBindingInfo
    Hashtable tableBindings;
    
    /// Mapping from query -> output display 
    Map queryToDisplay;
    
    //------------------------------------------------------------
    
    private static final String TITLE = "STREAM Visualizer";

    // Whether to read xml data from file dump or via explainPlan call to server
    private static boolean fromFile;

    // user supplied test filename
    private static String suppliedFileName;

    // server url
    private static String url;
    
    public ClientView (Client client) {
	super (TITLE);
	
	this.client = client;
	
	// determine the screen dimensions
	setSize ();
        feederViewStartX = FEEDERVIEW_XINIT;
        feederViewStartY = FEEDERVIEW_YINIT;
        queryResultStartX= QUERYRESULT_XINIT;
        queryResultStartY= QUERYRESULT_YINIT;
    
	// Action buttons lead the user through the sequence of 
	// actions -- registering streams/relations etc
	Component actionButtonPane = createActionButtons (client);
	
	// Entity observer provides information about an entity
	// (operator, queue, etc) when the entity is selected
	entityObs = new EntityObserver (this);
	
	// Legend pane is the legend of the figures used in the
	// query plan
	Component legendPane = createLegendPane();
	
	// Pack the above 3 panes into a single pane that occupies
	// the right portion of the visualizer
	Component rightPane = packRightPane (actionButtonPane, 
					     entityObs,
					     legendPane);
	
	// The main plan visualizer pane
	mainPane = createMainPane ();
	
	// The menu
	menuBar = new MenuBar (client, this);
	setJMenuBar (menuBar);
	
	// Putting it all together
	JSplitPane wholePane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
					       true, mainPane, rightPane);
	Dimension minimumSize = new Dimension(0,0);
	rightPane.setMinimumSize(minimumSize);
	wholePane.setResizeWeight(1.0);

	// add quit on close listener
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    exit ();
		}
	    });
	
	// create the dialogs 
	createDialogs ();
	
	streamDisplays = new ArrayList ();
	relnDisplays = new ArrayList ();

	tables = new ArrayList();
	tableFeederViews = new HashMap ();
	tableBindings = new Hashtable ();

	queryToDisplay = new HashMap ();
	
	getContentPane().add (wholePane);
	pack ();
    }
    
    private void exit () {
      client.end ();
    }
    
    public void connect () {
//	Connect connect = new Connect (this);
//	boolean success = false;

//	while(!success) {	
//	    connect.setVisible (true);
	
//	    String host = connect.getHost ();
	
	    // host == null is the flag that the user canceled he connection
//	    if (host == null) {
//		System.out.println("host is null");
//		System.exit (0);
//	    }
//	    int port = connect.getPort ();
	    
	    try {
		client.setServerInfo (null, -1, url);
//		success = true;
	    }
	    catch (FatalException e) {
		JOptionPane.showMessageDialog (this, 
					       e.toString(), 
					       "Fail to connect to Server",
					       JOptionPane.ERROR_MESSAGE);
	    }
//	}
    }
    
    public int getScreenWidth () {
	return width;
    }
    
    public int getScreenHeight () {
	return height;
    }
    
    public JScrollPane getMainPane () {
	return mainPane;
    }    
    
    public void regStream() throws FatalException {
	
	regStreamDialog.reset ();
	while (true) {
	    try {
		regStreamDialog.setVisible (true);
		
		NamedTable table = regStreamDialog.getInputTable ();
		if (table == null)
		    return;
		
		client.registerBaseTable (table);
		
		// break not reached if exception occurs
		break;
	    }
	    catch (NonFatalException e) {
	      JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }
	}
    }
    
    public void regRelation() throws FatalException {
	regRelationDialog.reset ();
	
	while (true) {
	    try {
		regRelationDialog.setVisible (true);
	
		NamedTable table = regRelationDialog.getInputTable ();
		if (table == null)
		    return;
		
		client.registerBaseTable (table);
		
		break;
	    }
	    catch (NonFatalException e) {
		JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }	    
	}
    }
    
    public void bindStrm() throws FatalException {
      bindStrmSrcDialog.reset();
      while(true) {
        try {
          bindStrmSrcDialog.setVisible(true);
          String strmName = bindStrmSrcDialog.getElemName();
          String pathName = bindStrmSrcDialog.getPath();
          String schemeName = bindStrmSrcDialog.getScheme();
          
          if(strmName == null || pathName == null || schemeName == null)
            return;
          client.bindSrcDest(strmName,schemeName,pathName,1);
          break;
        }
          
        catch (NonFatalException e) {
        JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
                           JOptionPane.ERROR_MESSAGE);
        // goto while (true)
        }
        
      }
    }
    
    public void bindReln() throws FatalException {
      bindRelnSrcDialog.reset();
      while(true) {
        try {
          bindRelnSrcDialog.setVisible(true);
          String relnName = bindRelnSrcDialog.getElemName();
          String pathName = bindRelnSrcDialog.getPath();
          String schemeName = bindRelnSrcDialog.getScheme();
          
          if(relnName == null || pathName == null || schemeName == null)
            return;
          client.bindSrcDest(relnName,schemeName,pathName,2);
          break;
        }
          
        catch (NonFatalException e) {
        JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
                           JOptionPane.ERROR_MESSAGE);
        // goto while (true)
        }
        
      }
    }
    
    public void bindQuery() throws FatalException {
      bindQueryDestDialog.reset();
      while(true) {
        try {
          bindQueryDestDialog.setVisible(true);
          String queryName = bindQueryDestDialog.getElemName();
          String pathName = bindQueryDestDialog.getPath();
          String schemeName = bindQueryDestDialog.getScheme();
          
          if(queryName == null || pathName == null || schemeName == null)
            return;
          client.bindSrcDest(queryName,schemeName,pathName,3);
          break;
        }
          
        catch (NonFatalException e) {
        JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
                           JOptionPane.ERROR_MESSAGE);
        // goto while (true)
        }
      }
    }
    
    public void startQuery() throws FatalException {
      startQueryDialog.reset();
      while(true) {
        try {
          startQueryDialog.setVisible(true);
          String queryName = startQueryDialog.getQueryName();
          if(queryName == null)
            return;
          client.startNamedQuery(queryName);
          break;
        }
        catch (NonFatalException e) {
          JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
                             JOptionPane.ERROR_MESSAGE);
        }
        
      }
    }
    public void regQuery() throws FatalException {
	regQueryDialog.reset ();

	while (true) {
	    try {
		regQueryDialog.setVisible (true);
		
		String queryText = regQueryDialog.getQueryText ();	
    // queryText == null => user pressed cancel
		if (queryText == null)
		    return;	
	
		boolean isNamed = regQueryDialog.isNamed ();	
		boolean hasOutput = regQueryDialog.outputRequired ();       
		Query query = new Query (queryText, hasOutput, isNamed);
		
		client.registerQuery (query);
		break;
	    }
	    catch (NonFatalException e) {
		JOptionPane.showMessageDialog (this, e.getMesg(), "Error",
					       JOptionPane.ERROR_MESSAGE);
		// goto while (true)
	    }
	}
    }
    
    public void showTableList() {
	assert (tableList != null);
	
	mainPane.getViewport().setView(tableList);
	
	setTitle (TITLE + ": Registered streams/relations");
    }
    
    public void showQueryList() {
	assert (queryList != null);
	
	mainPane.getViewport().setView(queryList);
	
	setTitle (TITLE + ": Registered queries");
    }
    
    public void showPlan (Query qry) {
	assert (qry.hasOutput ());
	
	QueryPlanView planView = (QueryPlanView)qryToPlanView.get (qry);
	mainPane.getViewport().setView (planView);
	
	setTitle (TITLE + ": Query " + qry.getQueryId());
    }
    
    public void showDisplay (Query qry) {
	assert (qry.hasOutput ());
	
	Component display = (Component)queryToDisplay.get (qry);
	display.setVisible (true);
    }
    
    public void hideDisplay (Query qry) {	
	assert (qry.hasOutput ());
	
	Component display = (Component)queryToDisplay.get (qry);
	display.setVisible (false);	
    }
    
    //------------------------------------------------------------
    // GUI related
    //------------------------------------------------------------
    
    private void setSize () {
    	Toolkit t = Toolkit.getDefaultToolkit();
    	width = t.getScreenSize().width - 100;
    	height = t.getScreenSize().height - 150; 
    }
    
    /**
     * Buttons 
     */
    private JPanel createActionButtons (Client client) {	
	// The text indicating the role of the buttons	
	JLabel heading = new JLabel ("Commands");
	heading.setFont(heading.getFont().deriveFont(Font.BOLD));
	
	// Action buttons
	regStreamButton   = new JButton ("Register Stream");
	regRelationButton = new JButton ("Register Relation");
	regQueryButton    = new JButton ("Register Query");
	genPlanButton     = new JButton ("Generate Plan");
	runButton         = new JButton ("Run");
	resetButton       = new JButton ("Reset");
	plan2Button	      = new JButton ("Plan2");
    bindStrmSrc       = new JButton ("Bind Stream Source");
    bindRelnSrc       = new JButton ("Bind Relation Source");
    bindQueryDestn    = new JButton ("Bind Query Destination");
    startButton       = new JButton ("Start Query");
	
	// Set the action listener
	regStreamButton.addActionListener (this);
	regRelationButton.addActionListener (this);
	regQueryButton.addActionListener (this);
	genPlanButton.addActionListener (this);
	runButton.addActionListener (this);
	resetButton.addActionListener (this);
	plan2Button.addActionListener (this);
    bindStrmSrc.addActionListener (this);
    bindRelnSrc.addActionListener (this);
    bindQueryDestn.addActionListener(this);
	startButton.addActionListener(this);
    
	JPanel buttonPanel = new JPanel (new GridLayout (4,3,2,2));
    
	JButton temp1 = new JButton("");
    JButton temp2 = new JButton("");
    
    buttonPanel.add (regStreamButton);
    buttonPanel.add (regRelationButton);
    buttonPanel.add (regQueryButton);
    buttonPanel.add (bindStrmSrc);
    buttonPanel.add (bindRelnSrc);
    buttonPanel.add (bindQueryDestn);
    buttonPanel.add(temp1);
    buttonPanel.add(temp2);
    temp1.setVisible(false);
    temp2.setVisible(false);
    buttonPanel.add (startButton);
    
    buttonPanel.add (genPlanButton);
    buttonPanel.add (plan2Button);
    plan2Button.setVisible((false));
	buttonPanel.add (runButton);
	//buttonPanel.add (resetButton);
    
	JPanel fullPanel = new JPanel (new BorderLayout (10,10));
	fullPanel.add (heading, BorderLayout.NORTH);
	fullPanel.add (buttonPanel, BorderLayout.CENTER);
	
	return fullPanel;
    }
    
    private Component createLegendPane() {
	JScrollPane legendPane = 
	    new JScrollPane (new LegendPane());
	legendPane.setPreferredSize 
	    (new Dimension ((int)(width * LEGEND_WIDTH),
			    (int)(height * LEGEND_HEIGHT)));
	return legendPane;
    }
    
    private JPanel packRightPane (Component actionButtonPane, 
				  Component entityObs, 
				  Component legendPane) {
	
	// Combine entityObs and legendPane using a split pane
	JSplitPane splitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT,
					       true, 
					       entityObs, 
					       legendPane);
	
	splitPane.setBorder(null);
	
	JPanel rightPane = new JPanel (new BorderLayout (10,10));
	rightPane.add (actionButtonPane, BorderLayout.NORTH);
	rightPane.add (splitPane, BorderLayout.CENTER);
	
	return rightPane;
    }    
    
    private JScrollPane createMainPane () {
	// An empty panel until we have a plan
	JPanel filler = new JPanel();	
	filler.setBackground(Color.white);
	
	JScrollPane scrollPane = new JScrollPane(filler);	
	scrollPane.setPreferredSize
	    (new Dimension((int)(width * PLAN_PANE_WIDTH),
			   (int)(height * PLAN_PANE_HEIGHT)));
	scrollPane.setVerticalScrollBarPolicy
	    (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setHorizontalScrollBarPolicy
	    (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
	tableList = new NamedTableList(client, this);	
	queryList = new QueryList(client, this);
	planViews = new ArrayList ();
	qryToPlanView = new HashMap ();
	
	//queryList.setLayout(new BoxLayout(queryList, BoxLayout.PAGE_AXIS));
	
	return scrollPane;
    }    
    
    private void createDialogs() {
      regStreamDialog = new RegisterInput(this, true);
      regRelationDialog = new RegisterInput(this, false);
      regQueryDialog = new RegisterQuery(this);
      bindStrmSrcDialog = new BindSrcDest(this, 1, "Bind Stream Source");
      bindRelnSrcDialog = new BindSrcDest(this, 2, "Bind Relation Source");
      bindQueryDestDialog = new BindSrcDest(this, 3, "Bind Query Destination");
      startQueryDialog = new StartNamedQuery(this);
    }

    private void explainPlan() throws FatalException {
      String planString;
      try {
///
	if(fromFile)
	{
          /*
          String view_root = System.getenv("T_WORK");
          String fname     = null;
          if(view_root != null)
          {
            fname = view_root + "/cep/" + suppliedFileName + "_dump.xml";
            System.out.println("****************Plan File = "+fname+" ************");
          }
          else 
            throw new IOException("$T_WORK not set");
          */
          BufferedReader reader = new BufferedReader( new FileReader(suppliedFileName));
          char[] buf = new char[1024];
          int numread = 0;
          StringBuilder filedata = new StringBuilder();
          while( (numread=reader.read(buf)) != -1) {
                  String readdata = String.valueOf(buf,0,numread);
                  filedata.append(readdata);
                  buf = new char[1024];
          }
          reader.close();
          planString = filedata.toString();
        }
        else
        {
          planString = client.explainPlan();
        }
///
        QueryPlan plan = new QueryPlan(planString);
        QueryPlanView planView = new QueryPlanView(null, plan, entityObs, client,
            this);
        planViews.add(planView);
        mainPane.getViewport().setView(planView);
      } 
      catch (NonFatalException e) {
        throw new FatalException("IO Exception");
      }
      catch (IOException e)
      {
        throw new FatalException("IO Exception"+e.getMessage());
      }
    }
    
    //------------------------------------------------------------
    // Respond to user Action
    //------------------------------------------------------------
    
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	
	try {
	    if (source == regStreamButton) {
		regStream();  
	    }
	    
	    else if (source == regRelationButton) {
		regRelation();
	    }
	    
	    else if (source == regQueryButton) {	    
		regQuery();
	    }
	    
	    else if (source == genPlanButton) {
		  explainPlan();
	    }
	    
	    else if (source == runButton) {	    
		client.run ();
	    }
	    
	    else if (source == resetButton) {
		client.softReset ();
	    }

	    else if (source == bindStrmSrc) {
          bindStrm ();
        }
        
        else if (source == bindRelnSrc) {
          bindReln ();
        }
        
        else if (source == bindQueryDestn) {
          bindQuery ();
        }
        
        else if(source == startButton) {
          startQuery ();
        }
	}
	catch (FatalException f) {
	    JOptionPane.showMessageDialog (this, f.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    //System.exit (0);
	}
    }    
    
    
    //------------------------------------------------------------
    // ClientListener
    //------------------------------------------------------------

    /**
     * The state of the client changed from 'oldState' to 'newState'
     */
    public void stateChanged (int oldState, int newState) {
	updateButtonEnabling (newState);
    }
    
    /**
     * A new base table has been added.
     */
    public void baseTableAdded (NamedTable table) {
	//record the base table here
	tables.add (table);	
    }
    
    /**
     * A new query has been added
     */
    public void queryAdded (Query query, UnnamedTable unnamedTable) {	
	
	try {
	    // If the query is a named query, we want to get the 
	    // attirubute & output names of the query
	    if (query.isNamed() && client.getState() == Client.USERAPPSPEC) {
		AssignName assignName = 
		    new AssignName (this, unnamedTable);
		
		while (true) {
		    try {
			assignName.setVisible (true);
			
			NamedTable table = assignName.getNamedTable ();
			if (table == null) 
			    return; 
			
			client.registerView (query, table);
			break;
		    } 
		    catch (NonFatalException e) {
			JOptionPane.showMessageDialog 
			    (this, e.getMesg(), "Error",
			     JOptionPane.ERROR_MESSAGE);
			// goto while (true)			
		    }
		}
	    }
	}
	catch(FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	}
    }
    
    /**
     * A new view has been specified
     */
    public void viewAdded (Query query, NamedTable table) {
    }
    
    /**
     * A query result is available
     */
    public void queryResultAvailable (QueryResult result) {
	if (result.getSchema().isStream()) {
	    StreamDisplay sd = new StreamDisplay (result);
	    sd.setLocation(new Point(queryResultStartX,
				     queryResultStartY));
	    queryResultStartX += QUERYRESULT_XINC;
	    queryResultStartY += QUERYRESULT_YINC;
	    streamDisplays.add (sd);
	    sd.setVisible (true);
	    queryToDisplay.put (result.getQuery(), sd);
	}
	
	else {
	    RelationDisplay rd = new RelationDisplay (result);
	    rd.setLocation(new Point(queryResultStartX,
				     queryResultStartY));
	    queryResultStartX += QUERYRESULT_XINC;
	    queryResultStartY += QUERYRESULT_YINC;
	    relnDisplays.add (rd);
	    rd.setVisible (true);
	    queryToDisplay.put (result.getQuery(), rd);
	}
    }

    public void monitorAdded (Monitor mon, Query qry, QueryResult res, 
			      QueryPlan plan) {
	QueryPlanView planView = new QueryPlanView (qry, plan, 
						    entityObs, client, this);
	planViews.add (planView);
	qryToPlanView.put (qry, planView);	
    }
    
    /**
     * Need to create a new TableFeederView
     */
    public TableFeederView createAndShowTableFeederView (NamedTable table) {
	TableFeederView view = (TableFeederView)tableFeederViews.get(table);
	if ( view == null ) {
	    view = new TableFeederView (table, this);
	    view.setLocation(new Point(feederViewStartX,
				       feederViewStartY));
	    
	    tableFeederViews.put(table, view);
	    feederViewStartX += FEEDERVIEW_XINC;
	    feederViewStartY += FEEDERVIEW_YINC;
	}
	
	view.setVisible(true);
	return view;
    }
    
    /**
     * bind TableFeeder to a table
     */
    public TableFeeder bindTableFeeder (NamedTable table, String fileName,
					boolean bLoop, boolean bAppTs) {
	TableFeeder feeder = null;
	try {
	    feeder = client.createTableFeeder 
		(table, fileName, bLoop, bAppTs);
	}
	catch (FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);	    
	}
	
	TableBindingInfo tbi = new TableBindingInfo(table.getTableName(),
						    fileName, bLoop,
						    bAppTs);
	tableBindings.put(table, tbi);
	
	return feeder;
    }
    
    /**
     * unbind TableFeeder from table
     */
    public void unbindTableFeeder (NamedTable table) {
	try {
	    client.destroyTableFeeder (table);
	}
	catch (FatalException e) {
	    JOptionPane.showMessageDialog (this, e.toString(), "Error",
					   JOptionPane.ERROR_MESSAGE);
	    System.exit(0);	    
	}
    }

    /**
     * Encode table binding 
     */
    public String encodeTableBindings () {
	StringBuffer strBuf = new StringBuffer();

	Enumeration enumer = tableBindings.keys();
	while (enumer.hasMoreElements()) {
	    Object key = enumer.nextElement();
	    TableBindingInfo tbi = (TableBindingInfo) tableBindings.get(key);
	    strBuf.append("<Binding>\n");

	    strBuf.append("<TableName>");
	    strBuf.append(tbi.getTableName());
	    strBuf.append("</TableName>\n");
	    strBuf.append("<FileName>");
	    strBuf.append(tbi.getFileName());
	    strBuf.append("</FileName>\n");
	    strBuf.append("<bLoop>");
	    strBuf.append(tbi.getbLoop());
	    strBuf.append("</bLoop>\n");
	    strBuf.append("<bAppTs>");
	    strBuf.append(tbi.getbAppTs());
	    strBuf.append("</bAppTs>\n");

	    strBuf.append("</Binding>\n");
	}

	return strBuf.toString();
    }

    /* load table binding */
    public void loadBinding (Node node) {
	TableBindingInfo tbi = new TableBindingInfo();

	try {
	    tbi.genTableBindingInfo(node);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}

	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (table.getTableName().equals(tbi.getTableName())) {
	       
		TableFeederView tbv = createAndShowTableFeederView(table);
		tbv.bindTableFeeder(tbi.getFileName(),
				    tbi.getbLoop(),
				    tbi.getbAppTs());
		break;
	    }
	}	
    }

    /* load table binding */
    public void loadDemoBinding (Node node) {
	TableBindingInfo tbi = new TableBindingInfo();

	try {
	    tbi.genTableBindingInfo(node);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}

	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (table.getTableName().equals(tbi.getTableName())) {
	       
		TableFeederView tbv = createAndShowTableFeederView(table);
		String fileName = tbi.getFileName();
		fileName =InitManager.getDemoPath() + fileName;
		tbv.bindTableFeeder(fileName,
				    tbi.getbLoop(),
				    tbi.getbAppTs());
		break;
	    }
	}	
    }

    /* a convenient method to make demoMenu easier */
    public void loadRegistryFromFile(File file) {
	menuBar.getFileMenu().loadRegistryFromFile(file);
    }

    public void loadRegistryFromFile(InputStream in) {
	menuBar.getFileMenu().loadRegistryFromFile(in);
    }
    

    /**
     * Query plan has been generated. Generate a planView for each
     * output query in the plan
     */
    public void planGenerated (QueryPlan plan) {
	List queries = client.getRegisteredQueries ();
	
	for (int q = 0 ; q < queries.size() ; q++) {
	    Query qry = (Query)queries.get(q);
	    
	    if (qry.hasOutput ()) {
		QueryPlanView planView = new QueryPlanView 
		    (qry, plan, entityObs, client, this);
		planViews.add (planView);
		qryToPlanView.put (qry, planView);
	    }
	}	
    }
    
    public void resetEvent () {
	
	// reset the plan views - 
	for (int v = 0 ; v < planViews.size () ; v++) {
	    QueryPlanView planView = (QueryPlanView)planViews.get (v);
	    planView.reset ();
	}
	
	entityObs.clear();

	// reset the plan
	planViews.clear ();
	qryToPlanView.clear ();
	JPanel filler = new JPanel();	
	filler.setBackground(Color.white);	
	mainPane.getViewport().setView(filler);
	setTitle (TITLE);	
	
	// Close all the stream displays
	for (int s = 0 ; s < streamDisplays.size() ; s++) {
	    StreamDisplay sd = (StreamDisplay)streamDisplays.get(s);
	    sd.setVisible(false);
	}
	streamDisplays.clear ();
	
	// Close all relation displays
	for (int r = 0 ; r < relnDisplays.size() ; r++) {
	    RelationDisplay rd = (RelationDisplay)relnDisplays.get(r);
	    rd.setVisible (false);
	}
	relnDisplays.clear ();
	
	// Close all the table feeder views
	for (int t = 0 ; t < tables.size(); t++) { 
	    NamedTable table = (NamedTable)tables.get (t);
	    
	    if (!table.isBase()) 
		continue;
	    
	    TableFeederView tfv = (TableFeederView)tableFeederViews.get (table);
	    if(tfv != null) tfv.setVisible(false);
	}

	// clear current info about tables
	tables.clear ();
	tableFeederViews.clear ();
	tableBindings.clear ();

	// reset position info
        feederViewStartX = FEEDERVIEW_XINIT;
        feederViewStartY = FEEDERVIEW_YINIT;
        queryResultStartX= QUERYRESULT_XINIT;
        queryResultStartY= QUERYRESULT_YINIT;
     }
    
    public void closeEvent () {} 
    
    //------------------------------------------------------------
    // Action Buttons Enabling/disabling logic
    //------------------------------------------------------------
    
    private void disableAllButtons () {
      regStreamButton.setEnabled (false);
      regRelationButton.setEnabled (false);
      regQueryButton.setEnabled (false);
      genPlanButton.setEnabled (false);
      runButton.setEnabled (false);
      resetButton.setEnabled (false);	
    }
    
    private void updateButtonEnabling (int state) {
	disableAllButtons ();
	
	switch (state) {
	case Client.USERAPPSPEC:
	    regStreamButton.setEnabled (true);
	    regRelationButton.setEnabled (true);
	    regQueryButton.setEnabled (true);
	    genPlanButton.setEnabled (true);
	    resetButton.setEnabled (true);
	    break;
	    
	case Client.PLANGEN:
	    runButton.setEnabled (true);
	    resetButton.setEnabled (true);
	    break;
	    
	case Client.RUN:
        regStreamButton.setEnabled (true);
        regRelationButton.setEnabled (true);
        regQueryButton.setEnabled (true);
        genPlanButton.setEnabled (true);
        resetButton.setEnabled (true);
	    break;
	    
	default:
	    break;
	}
    }    
    
    //Main testing
    private static void createAndShowGUI() {
	try {
	    UIManager.setLookAndFeel
		(UIManager.getSystemLookAndFeelClassName());
	    Client client = new Client ();
	    ClientView clientView = new ClientView (client);
	    client.addListener (clientView);
	    clientView.setVisible(true);
            if(!fromFile)
              clientView.connect();
	}
	catch (Exception e) {
	    System.out.println (e);
	}
    }

    private static void printArgs() {
	System.out.println("Incorrect argmuents");
        System.out.println("sh vis.sh <true/false> <testfile name>/[jdbc server address]");
        System.out.println("Arg 1 - specify if the dump file should be read" +
                           " from file or should be obtained from jdbc server");
        System.out.println("Arg 2 - if reading from file, the testfile name e.g.tkdata1 (no default value)" + 
                           " else jdbc server url, default option is local server");
    }  
 
    public static void main(String[] args) {

	if(args.length < 1 || args.length > 2)
	{
            printArgs();
	    return;
	}
        if(args[0].equals("true"))
        {
            fromFile = true;
	    if(args.length != 2)
            {
              printArgs();
              return;
            }
            else
            {
              suppliedFileName = args[1];
            }
        }
        else if(args[0].equals("false"))
        {
            if(args.length != 2)
            {
                //default url (local server)
                try
                {
                    String hostName = InetAddress.getLocalHost().getHostName();
                    url = "jdbc:oracle:cep:@" + hostName + ":1199:sys";
                }
                catch(UnknownHostException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                url = args[1];
            }
        }
        else
        {
           printArgs();
           return;
        }
	try {
	    InitManager.setupPaths();
	} 
	catch (IOException e) {
	    e.printStackTrace();
	    System.exit(0);
	}

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    createAndShowGUI();
		}
	    });
    }

    //private class to record table binding info
    private class TableBindingInfo {
	private String tableName;
	private String fileName;
	private boolean bLoop;
	private boolean bAppTs;

	public TableBindingInfo () { super(); }

	public TableBindingInfo (String tableName,
				 String fileName,
				 boolean bLoop,
				 boolean bAppTs) {
	    this.tableName = tableName;
	    this.fileName = fileName;
	    this.bLoop = bLoop;
	    this.bAppTs = bAppTs;
	}

	public String getTableName () {return tableName;}
	public String getFileName () {return fileName;}
	public boolean getbLoop () {return bLoop;}
	public boolean getbAppTs () {return bAppTs;}
	
	public void genTableBindingInfo (Node node) 
	throws FatalException {

	    NodeList childs = node.getChildNodes ();
	    String tableName="", fileName="", bVal;
	    boolean bLoop=false, bAppTs=false;

	    for (int n = 0 ; n < childs.getLength () ; n++) {
		Node child = childs.item (n);
	    
		if (child.getNodeType () != Node.ELEMENT_NODE)
		    continue;
	    
		if (child.getNodeName().equals ("TableName")) {
		    tableName = XmlHelper.getText (child);
		    continue;
		}

		if (child.getNodeName().equals ("FileName")) {
		    fileName = XmlHelper.getText (child);
		    continue;
		}
	    
		if (child.getNodeName().equals ("bLoop")) {
		    bVal = XmlHelper.getText (child);
		    if(bVal.equals("true")) bLoop=true;
		    else bLoop = false;
		    continue;
		}
	    
		if (child.getNodeName().equals ("bAppTs")) {
		    bVal = XmlHelper.getText (child);
		    if(bVal.equals("true")) bAppTs=true;
		    else bAppTs = false;
		    continue;
		}	    
	    }

	    this.tableName = tableName;
	    this.fileName = fileName;
	    this.bLoop = bLoop;
	    this.bAppTs = bAppTs;
	}
    }
}
