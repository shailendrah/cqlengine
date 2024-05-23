/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/CEPException.java /main/9 2011/11/23 09:58:43 alealves Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The base class for all CEP Exceptions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/25/09 - use MessageCatalog
    parujain    09/05/08 - offset support
    parujain    08/20/08 - support offset
    hopark      02/05/08 - parameterized error
    hopark      11/16/07 - make errorMessageBundle public
    sbishnoi    02/08/07 - support for locale-specific & formatted messages
    sbishnoi    01/30/07 - modify getMessage() method
    anasrini    03/01/06 - reimplement getMEssage 
    anasrini    02/07/06 - Constructor with CEPError 
    anasrini    11/30/05 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/CEPException.java /main/8 2010/08/12 08:40:08 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.exceptions;
import  java.util.ResourceBundle;
import  java.text.MessageFormat;

import oracle.cep.common.Constants;
import oracle.cep.util.I18NUtil;

import com.oracle.osa.exceptions.ErrorCode;

/**
 * The base class for all CEP Exceptions
 *
 * @since 1.0
 */

public class CEPException extends Exception {

  private Object[]       args;
  private String         msg;
  private String         actionMsg;
  private ErrorCode      errorCode;
  private MessageFormat  mFormat;  
  private int            startOffset =0;
  private int            endOffset=0;

  private static ResourceBundle errorMessageBundle;

  static
  {
    if (!Constants.I18N_MESSAGE_CATALOG)
    {
      errorMessageBundle = ResourceBundle.getBundle("oracle.cep.exceptions.CEPResourceBundle"); 
    }
  }

  /**
   * Constructs a new CEPException with the specified error code
   * @param errorCode specified ErrorCode
   */
  public CEPException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.mFormat   = new MessageFormat("");
  }

  /**
   * Constructs a new CEPException with the specified error code
   * @param errorCode specified ErrorCode
   */
  public CEPException(ErrorCode errorCode, int start, int end) {
    this.errorCode = errorCode;
    this.startOffset = start;
    this.endOffset = end;
    this.mFormat   = new MessageFormat("");
  }

  /**
   * Constructs a new CEPException with the specified error code
   * @param errorCode specified ErrorCode
   * @param args arguments to replace placeholders in the message
   */
  public CEPException(ErrorCode errorCode, Object ... args) {
    this.errorCode = errorCode;
    this.args      = args;
    this.mFormat   = new MessageFormat("");
  }


  /**
   * Constructs a new CEPException with the specified error code
   * @param errorCode specified ErrorCode
   * @param args arguments to replace placeholders in the message
   */
  public CEPException(ErrorCode errorCode,int start, int end, Object ... args) {
    this.errorCode = errorCode;
    this.startOffset = start;
    this.endOffset = end;
    this.args      = args;
    this.mFormat   = new MessageFormat("");
  }

  /**
   * Constructs a new CEPException with the specified error code and cause
   * @param errorCode specified ErrorCode
   * @param cause the cause. A null value is permitted, and indicates that
   * the cause is nonexistent or unknown.
   */
  public CEPException(ErrorCode errorCode, Throwable cause, int start, int end) {
    super(cause);
    this.errorCode = errorCode;
    this.startOffset = start;
    this.endOffset = end;
    this.mFormat   = new MessageFormat("");
  }
 

  /**
   * Constructs a new CEPException with the specified error code and cause
   * @param errorCode specified ErrorCode
   * @param cause the cause. A null value is permitted, and indicates that
   * the cause is nonexistent or unknown.
   */
  public CEPException(ErrorCode errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
    this.mFormat   = new MessageFormat("");
  }
  
  public CEPException(ErrorCode errorCode, Throwable cause,int start,
		   int end, Object ... args) {
    super(cause);
    this.errorCode = errorCode;
    this.args      = args;
    this.startOffset = start;
    this.endOffset = end;
    this.mFormat   = new MessageFormat("");
  }
 

  /**
   * Constructs a new CEPException with the specified error code and cause
   * @param errorCode specified ErrorCode
   * @param cause the cause. A null value is permitted, and indicates that
   * the cause is nonexistent or unknown.
   * @param args arguments to replace placeholders in the message
   */
  public CEPException(ErrorCode errorCode, Throwable cause, Object ... args) {
    super(cause);
    this.errorCode = errorCode;
    this.args      = args;
    this.mFormat   = new MessageFormat("");
  }

  /**
   * Override this method to return the locale specific error message 
   * corresponding to the error code.
   * @return locale specific error message corresponding to the error code
   */
  public String getMessage() 
  {
    return getMessage(errorCode, args);
  }

  /**
   * get the error code which resulted in this exception being raised
   * @return the error code associated with this exception
   */
  public ErrorCode  getErrorCode() {
    return errorCode;
  }

  public String getAction() 
  {
    return getAction(errorCode, args);
  }
  
  public String getCauseMessage()
  {
    return getCause(errorCode, args);
  }

  public int getStartOffset()
  {
    return this.startOffset;
  }

  public int getEndOffset()
  {
    return this.endOffset;
  }
 
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  public Object [] getArgs()
  {
    return args;
  }

  public static String getMessage(ErrorCode ec, Object... args)
  {
    if (Constants.I18N_MESSAGE_CATALOG)
    {
      return I18NUtil.getMessage(ec, args);
    }
    String msg = errorMessageBundle.getString(ErrorHelper.getMessageKey(ec));
    if (args != null)
       return MessageFormat.format(msg, args);
    else
       return msg;
  }
  
  public static String getAction(ErrorCode ec, Object... args)
  {
    if (Constants.I18N_MESSAGE_CATALOG)
    {
      return I18NUtil.getAction(ec, args);
    }
    String actionMsg = errorMessageBundle.getString(ErrorHelper.getMessageActionKey(ec));
    if (args != null)
      return MessageFormat.format(actionMsg, args);
    else
       return actionMsg;
  }
  
  private String getCause(ErrorCode ec, Object[] args2)
  {
    if (Constants.I18N_MESSAGE_CATALOG)
    {
      return I18NUtil.getCause(ec, args);
    }
    String actionMsg = errorMessageBundle.getString(ErrorHelper.getMessageCauseKey(ec));
    if (args != null)
      return MessageFormat.format(actionMsg, args);
    else
       return actionMsg;
  }

 
  
}
