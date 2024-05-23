/* $Header: IServerConnection.java 17-apr-2008.03:26:27 rkomurav Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    04/17/08 - Creation
 */

/**
 *  @version $Header: IServerConnection.java 17-apr-2008.03:26:27 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.driver.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

import oracle.cep.driver.util.FatalException;

public abstract class IServerConnection
{
  //------------------------------------------------------------
  // Command Connection Related
  //------------------------------------------------------------
  
  /// Socket for the command connection
  Socket                   commandSocket;
  
  /// Input stream for command connection
  private DataInputStream  commandInStream;
  
  /// Output stream for command connection
  private DataOutputStream commandOutStream;

  /// Host to which we are connecting
  public String            host;
  
  /// The port at which the STREAM server is listening
  public int               port;  
  
  //------------------------------------------------------------
  // Protocol strings
  //------------------------------------------------------------
  public static final String COMMAND_CONN_ID       = "COMMAND_CONN"; 
  public static final String OUTPUT_CONN_ID        = "OUTPUT_CONN";
  public static final String INPUT_CONN_ID         = "INPUT_CONN";
  
  public static final String BEGIN_APP_COMMAND     = "BEGIN APP";
  public static final String REG_INPUT_COMMAND     = "REGISTER INPUT";
  public static final String REG_OUT_QUERY_COMMAND ="REGISTER OUTPUT QUERY";
  public static final String REG_QUERY_COMMAND     = "REGISTER QUERY";
  public static final String REG_VIEW_COMMAND      = "REGISTER NAME";
  public static final String END_APP_COMMAND       = "END APP";    
  public static final String GEN_PLAN_COMMAND      = "GENERATE PLAN";
  public static final String EXECUTE_COMMAND       = "EXECUTE";
  public static final String RESET_COMMAND         = "RESET";
  public static final String TERMINATE_COMMAND     = "TERMINATE";
  public static final String REG_MON_COMMAND       = "REGISTER MONITOR";
  
  public BeginAppRet beginApp () throws FatalException
  {
    BeginAppRet ret = new BeginAppRet ();
    ret.errorCode = getErrorCode ();
    return ret;
  }
  
  public RegMonRet registerMonitor (String monQuery) throws FatalException
  {
    RegMonRet ret = new RegMonRet ();
    try
    {
      //sendMesg (REG_MON_COMMAND);
      //sendMesg (monQuery);
      ret.errorCode = getErrorCode ();
      
      if (ret.errorCode == 0)
      {
        ret.monId = getIntMesg ();
        ret.outputId = getIntMesg ();
        ret.schema = receiveMesg ();
        ret.planString = receiveMesg();   
      }
      
      return ret;
    }
    catch (IOException e)
    {
      throw new FatalException ("IO Exception");
    }
  }
  
  public EndAppRet endApp () throws FatalException
  {
    EndAppRet ret = new EndAppRet ();
    ret.errorCode = getErrorCode ();
    return ret;
  }
  
  public ResetRet reset () throws FatalException
  {
    ResetRet ret = new ResetRet ();
    ret.errorCode = getErrorCode ();
    return ret;
  }
  
  public TerminateRet terminate () throws FatalException
  {
    TerminateRet ret = new TerminateRet ();
    ret.errorCode = getErrorCode ();
    return ret;
  }
  
  private void setupCommandStreams () throws FatalException
  {
    try
    {
      commandInStream = new DataInputStream
        (new BufferedInputStream (commandSocket.getInputStream()));
      commandOutStream = new DataOutputStream
        (new BufferedOutputStream (commandSocket.getOutputStream()));
    }
    
    catch (IOException e)
    {
      throw new FatalException ("IO Exception");
    }
  }
  
  /** 
   * Establish a command connection (used to send commands to the server)
   */ 
  private Socket establishCommandConnection () throws FatalException
  {
    return establishConnection(COMMAND_CONN_ID);
  }
  
  /**
   * Establish an input connection (used to input streams/rels to the server)
   */
  public Socket establishInputConnection (int inputId) throws FatalException
  {
    return establishConnection(INPUT_CONN_ID + inputId);
  }
  
  /**
   * Establish an output connection (used to get output from the server)
   */
  public Socket establishOutputConnection (int outputId) throws FatalException
  {
    return establishConnection(OUTPUT_CONN_ID + outputId);
  }
  
  /**
   * Establish a new connection with the server.  The connection
   * could be a command connection, input connection, or an output
   * connection.  The user of this method encodes the type of the 
   * connection in the connection identifier "connId".  This method
   * just sends the connId to the server and checks if the error code
   * is zero.
   */
  private Socket establishConnection (String connId) throws FatalException
  {
    try
    {
      Socket newSock = new Socket(host, port); 
      //      newSock.setSoTimeout (1000);
      
      DataOutputStream out= new DataOutputStream
        (new BufferedOutputStream(newSock.getOutputStream()));
      DataInputStream in  = new DataInputStream
        (new BufferedInputStream(newSock.getInputStream()));
    
      //sendMesg(out, connId);  
      int errorCode = getErrorCode (in);
      if (errorCode != 0)
      {
        throw new FatalException ("Server returned error while connecting");
      }
      return newSock;     
    }
    catch (UnknownHostException e)
    {
      throw new FatalException("IP address of server could not be determined");
    }

    catch (IOException e)
    {
      throw new FatalException("IO Exception while connecting to the server");
    }

    catch (SecurityException e)
    {
      throw new FatalException
        ("Security Exception while connecting to the server");
    }
  }
  
  /**
   * Send a (text) message to the server - this is one of the communication
   * primitives.  The text message is encoded as (length of the msg) 
   * followed by the actual content.
   */ 
  private void sendMesg (DataOutputStream out, String content) 
    throws IOException
  {   
    byte[] contentBytes = content.getBytes("US-ASCII"); 
    int msgLength = contentBytes.length + 1; 
    
    out.writeInt(msgLength);  
    out.write(contentBytes); 
    out.write(0);
    out.flush();
  }
  
  /**
   * Send message on the command stream.
   */ 
  private void sendMesg (String content) throws IOException
  {   
    byte[] contentBytes = content.getBytes("US-ASCII"); 
    int msgLength = contentBytes.length + 1; 
    
    commandOutStream.writeInt(msgLength);   
    commandOutStream.write(contentBytes); 
    commandOutStream.write(0);
    commandOutStream.flush();
  }
  
  /**
   * Receive a text message from the server - the dual of sendMesg()
   * The text message is encoded as length followed by the actual
   * content in bytes.
   */
  private String receiveMesg(DataInputStream in) throws IOException
  {
    // Length of the message
    int msgLen = in.readInt();
    
    // content
    byte[] buf = new byte[msgLen];
    in.readFully(buf, 0, msgLen);
    String data = new String(buf, 0, msgLen - 1, "US-ASCII");
    
    return data;
  }
  
  /**
   * Get an integer value from the command connection
   */ 
  private int getIntMesg () throws FatalException 
  {
    /*try {
        String intStr = receiveMesg (); 
        return Integer.parseInt (intStr);
    }
    catch (IOException e) {
        throw new FatalException ("IO Exception");
    }
    catch (NumberFormatException e) {
        throw new FatalException ("Protocol Violation");
    }*/
    return 1;
  }
  
  /**
   * Receive message from the command connection
   */    
  private String receiveMesg() throws IOException 
  {
    // Length of the message
    int msgLen = commandInStream.readInt();
    
    // content
    byte[] buf = new byte[msgLen];
    commandInStream.readFully(buf, 0, msgLen);
    String data = new String(buf, 0, msgLen - 1, "US-ASCII");
    
    return data;
  }
  
  public RegViewRet registerView (String regViewStr, int queryId) 
  throws FatalException
  {
    RegViewRet ret = new RegViewRet ();
    ret.errorCode = getErrorCode ();
    return ret;
  }
  
  /**
   * An error code sent by the server is just a message whose 
   * content is an integer containing the error code.
   */
  public int getErrorCode (DataInputStream in) throws FatalException
  {
    try
    {
      String errorCode = receiveMesg (in);  
      return Integer.parseInt (errorCode);
    }
    catch (IOException e)
    {
      throw new FatalException ("IO Exception");
    }
    catch (NumberFormatException e)
    {
      throw new FatalException ("Protocol Violation");
    }
  }
  
  /**
   * Get error code on the command connection
   */    
  public int getErrorCode () throws FatalException {
  /*try
    {
      String errorCode = receiveMesg ();  
      return Integer.parseInt (errorCode);
    }
    catch (IOException e) 
    {
      throw new FatalException ("IO Exception");
    }
    catch (NumberFormatException e)
    {
      throw new FatalException ("Protocol Violation");
    }
    */
    return 0;
  }
  
  public abstract RegInputRet registerInput (String regInputStr) 
  throws FatalException;
  
  public abstract void startNamedQuery(String queryName) throws FatalException;
  
  public abstract void bindSrcDest(String name, String scheme, String path, int type) 
  throws FatalException;
  
  /** 
   * Register a query for which we want an output.
   */ 
  public abstract RegQueryRet registerOutQuery (String query)
  throws FatalException;
  
  /**
   * Register a query for which we do not need an output (pure view)
   */
  
  public abstract RegQueryRet registerQuery (String query) throws FatalException;
  
  public abstract GenPlanRet genPlan () throws FatalException;
  
  public abstract ExecRet execute () throws FatalException;
}
