/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ErrorHelper.java /main/6 2010/08/12 08:40:08 alealves Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Helper class related to errors

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/24/09 - add wlevs I18N stuffs
    parujain    08/21/08 - action key
    sbishnoi    02/08/07 - modify getMessageKey()
    anasrini    03/01/06 - Creation
    anasrini    03/01/06 - Creation
    anasrini    03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/ErrorHelper.java /main/6 2010/08/12 08:40:08 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorType;

/**
 * Helper class related to errors
 *
 * @since 1.0
 */

public class ErrorHelper {

  /**
   * Error messages are stored in resource files as key and value pairs.
   * This method returns the key corresponding to the value which is the 
   * message for this error code.
   * @param ec the error code for whom the key is sought
   * @return the key to the locale specific error message 
   */
  public static int    ComponentID = 1820;
  public static String PREFIX      = "CEP";
  public static String ActionSUFFIX = "ACTION";
  public static String CauseSUFFIX = "CAUSE";
  private static final String STATEMENT_ERROR_START_MARKER = ">>";
  private static final String STATEMENT_ERROR_END_MARKER = "<<";
  
  public static String getMessageKey(ErrorCode ec) {
    return PREFIX + "-" + getNumString(ec);  
  }
  
  
  public static String getNumString(ErrorCode ec){
    
    int    errnum       = getNum(ec);
    String errNumString = "" + errnum;
    int    len;
    
    errNumString = errNumString.trim();
    len          = errNumString.length();

    // Pad the error number with leading zeros so that length of number is 5
    for (int i=0; i<(5-len); i++)
      errNumString = "0" + errNumString;
    
    return errNumString;
  }
  
  public static String getMessageActionKey(ErrorCode ec) {
    return PREFIX + "-" + getNumString(ec) + "-" + ActionSUFFIX;  
  }
  
  public static String getMessageCauseKey(ErrorCode ec) {
    return PREFIX + "-" + getNumString(ec) + "-" + CauseSUFFIX;  
  }
  
  public static String getCodeName(ErrorCode ec){
    return ec.getErrorDescription().getCategory() + "_" + ec.name(); 
  }
  
  public static int getNum(ErrorCode ec) {
    return ec.getErrorDescription().getNum();
  }

  public static String getText(ErrorCode ec) {
    return ec.getErrorDescription().getText();
  }

  public static ErrorType getType(ErrorCode ec) {
    return ec.getErrorDescription().getType();
  }
  
  public static int getLevel(ErrorCode ec) {
    return ec.getErrorDescription().getLevel();
  }

  public static String getSeverity(ErrorCode ec) {
    ErrorType etype = ec.getErrorDescription().getType();
    int level = ec.getErrorDescription().getLevel();
    //Java Level                ODL MessageType:level
    //SEVERE.intValue()+100     INCIDENT_ERROR:1
    //SEVERE                    ERROR:1
    //WARNING                   WARNING:1
    //INFO                      NOTIFICATION:1
    //CONFIG                    NOTIFICATION:16
    //FINE                      TRACE:1
    //FINER                     TRACE:16
    //FINEST                    TRACE:32
    
    //commonss.logging level
    //fatal, error, warning, info, debug, trace 
    switch (etype)
    {
    case ERROR:                 return "error";
    case INTERNAL_ERROR:        return "error";
    case INCIDENT_ERROR:        return "fatal";
    case NOTIFICATION:          return "info";
    case TRACE:                 return "trace";
    case WARNING:               return "warning";
    }
    return "error";
  }
  
  public static String isDocumented(ErrorCode ec) {
    if (ec.getErrorDescription().isDocumented())
      return "Yes";
    else
      return "No";
  } 
  
  public static String getCause(ErrorCode ec) {
    return ec.getErrorDescription().getCause();
  }
  
  public static String getAction(ErrorCode ec) {
    return ec.getErrorDescription().getAction();
  }
  
  public static String getCategory(ErrorCode ec) {
    return ec.getErrorDescription().getCategory();
  }

  /**
   * Enrich the statement by adding markers on the particular offsets.
   * Useful to enrich DDL statement to add markers to indicate subclause
   * which can't be parsed.
   * @param e
   * @param statement
   * @return
   */
  public static String getAnnotatedCQLStatement(CEPException e, String statement)
  {
    int startOffset = e.getStartOffset();
    int endOffset = e.getEndOffset();
    String markedStatement = statement;
        
    if (statement != null && (startOffset >= 0 && endOffset >= 0) ) 
    {
        int soffset = startOffset;
        int eoffset = endOffset + 1; // point to the character next to the end offset
        
        if (soffset < statement.length()) 
        {
            markedStatement = 
                statement.substring(0, soffset) + 
                STATEMENT_ERROR_START_MARKER + 
                statement.substring(soffset);
        }
        
        if (eoffset < statement.length()) 
        {
            markedStatement = 
                markedStatement.substring(0, eoffset + STATEMENT_ERROR_START_MARKER.length()) + 
                STATEMENT_ERROR_END_MARKER + 
                markedStatement.substring(eoffset + STATEMENT_ERROR_START_MARKER.length());
        }
    }
    return markedStatement;
  }
  
  public static String getErrorMsg(CEPException e, String statement)
  {
    String markedStatement = getAnnotatedCQLStatement(e, statement);
    String m = "";
    String action = "";
    try
    {
      m = e.getMessage();
      if (m == null) m = "";
      action = e.getAction();
      if (action == null) action = "";
    } catch(Throwable ex) {}
    String msg = markedStatement + "\n" + m + ". " + action;
    Throwable c = e.getCause();
    if (c != null)
    {
        msg += "\n"+ c.getMessage();
    }
    return msg;
  }

}

