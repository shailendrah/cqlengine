/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/Command.java /main/8 2010/01/06 20:33:12 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Command in package oracle.cep.server.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  11/24/09 - synonym
 parujain  01/29/09 - transaction mgmt
 skmishra  12/26/08 - adding isValidate
 hopark    03/17/08 - config reorg
 parujain  03/07/07 - Extensible Windows
 hopark    12/04/06 - record exception
 najain    10/24/06 - integrate with mds
 anasrini  08/02/06 - add getId and setId
 anasrini  06/12/06 - support for user defined functions 
 najain    05/09/06 - support for views 
 skaluska  03/16/06 - implementation
 skaluska  03/15/06 - Creation
 skaluska  03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/Command.java /main/8 2010/01/06 20:33:12 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.server;

import java.util.Properties;

import oracle.cep.metadata.ConfigManager;

/**
 * CEP Command
 * 
 * @author skaluska
 */
public class Command
{
  /* Inputs */
  /** CQL to be processed */
  private String  cql;
  
  /** Is this command intended only for validation */
  private boolean isValidate;
  
  /* Outputs */
  /** whether command was successful */
  private boolean bSuccess;
  /** error message if any */
  private String  errorMsg;
  private Exception exception;
  
  /** id of the object created */
  private int id;

  public Command()
  {
    isValidate = false;
  }
  
  public void init (ConfigManager config)
  {
  }

  /**
   * @return the isValidate
   */
  public boolean isValidate()
  {
    return isValidate;
  }

  /**
   * @param isValidate the isValidate to set
   */
  public void setValidate(boolean isValidate)
  {
    this.isValidate = isValidate;
  }

  /**
   * Getter for bSuccess in Command
   * 
   * @return Returns the bSuccess
   */
  public boolean isBSuccess()
  {
    return bSuccess;
  }

  /**
   * Setter for bSuccess in Command
   * 
   * @param success
   *          The bSuccess to set.
   */
  public void setBSuccess(boolean success)
  {
    bSuccess = success;
  }

  /**
   * Getter for cql in Command
   * 
   * @return Returns the cql
   */
  public String getCql()
  {
    return cql;
  }

  /**
   * Setter for cql in Command
   * 
   * @param cql
   *          The cql to set.
   */
  public void setCql(String cql)
  {
    this.cql = cql;
  }

  /**
   * Getter for errorMsg in Command
   * 
   * @return Returns the errorMsg
   */
  public String getErrorMsg()
  {
    return errorMsg;
  }

  /**
   * Setter for errorMsg in Command
   * 
   * @param errorMsg
   *          The errorMsg to set.
   */
  public void setErrorMsg(String errorMsg)
  {
    this.errorMsg = errorMsg;
  }


  /**
   * Getter for exception in Command
   * 
   * @return Returns the recorded exception
   */
  public Exception getException()
  {
    return exception;
  }

  /**
   * Setter for exception in Command
   * 
   * @param exception
   *          The exception to set.
   */
  public void setException(Exception exception)
  {
    this.exception = exception;
  }

  /**
   * Getter for id in Command
   * 
   * @return Returns the id
   */
  public int getId()
  {
    return id;
  }

  /**
   * Setter for id in Command
   * 
   * @param id
   *          The id to set.
   */
  public void setId(int id)
  {
    this.id = id;
  }

  /**
   * Getter for queryId in Command
   * 
   * @return Returns the queryId
   */
  public int getQueryId()
  {
    return getId();
  }

  /**
   * Setter for queryId in Command
   * 
   * @param queryId
   *          The queryId to set.
   */
  public void setQueryId(int queryId)
  {
    setId(queryId);
  }
  
  /**
   * Setter of synonym id
   * @param synId 
   *             Synonym id
   */
  public void setSynonymId(int synId)
  {
    setId(synId);
  }
  
  /**
   * Getter of synonym id
   * @return
   *        synonym id
   */
  public int getSynonymId()
  {
    return getId();
  }

  /**
   * Getter for tableId in Command
   * @return Returns the tableId
   */
  public int getTableId()
  {
    return getId();
  }

  /**
   * Setter for tableId in Command
   * @param tableId The tableId to set.
   */
  public void setTableId(int tableId)
  {
    setId(tableId);
  }

  /**
   * Getter for viewId in Command
   * @return Returns the viewId
   */
  public int getViewId()
  {
    return getId();
  }

  /**
   * Setter for viewId in Command
   * @param viewId The viewId to set.
   */
  public void setViewId(int viewId)
  {
    setId(viewId);
  }

  /**
   * Getter for function Id in Command
   * @return Returns the function id
   */
  public int getFunctionId()
  {
    return getId();
  }

  /**
   * Setter for function Id in Command
   * @param fnId The function Id to set.
   */
  public void setFunctionId(int fnId)
  {
    setId(fnId);
  }
  
  /**
   * Getter for windowId in Command
   * @return Returns the windowId
   */
  public int getWindowId()
  {
    return getId();
  }

  /**
   * Setter for windowId in Command
   * @param tableId The windowId to set.
   */
  public void setWindowId(int winId)
  {
    setId(winId);
  }

}
