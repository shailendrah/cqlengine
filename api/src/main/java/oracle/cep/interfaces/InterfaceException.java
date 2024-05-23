/* $Header: pcbpel/cep/common/src/oracle/cep/interfaces/InterfaceException.java /main/3 2008/09/10 14:06:33 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/21/08 - package
    sbishnoi    02/11/08 - error parameterization
    parujain    05/23/07 - Interface Exception
    parujain    05/23/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/interfaces/InterfaceException.java /main/3 2008/09/10 14:06:33 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;

public class InterfaceException extends CEPException 
{
  static final long serialVersionUID = 1;

 /**
   * Constructor for InterfaceException
   * 
   * @param err
   *          InterfaceError code
   */
  public InterfaceException(InterfaceError err)
  {
    super(err);
  }

  /**
   * Constructor for InterfaceException
   * 
   * @param err
   *          InterfaceError code
   * @param cause
   *          Throwable error causing the exception
   */
  public InterfaceException(InterfaceError err, Throwable cause)
  {
    super(err, cause);
  }

  /**
   * Constructor for InterfaceException
   *
   * @param err
   *          InterfaceError code
   * @param args
   *          Arguments
   */
  public InterfaceException(InterfaceError err, Object ... args) {
	super(err, args);
  }


  /**
   * Constructor for InterfaceException
   * 
   * @param err
   *          InterfaceError code
   * @param cause
   *          Throwable error causing the exception
   * @param args
   *          Arguments
   */

  public InterfaceException(InterfaceError err, Throwable cause, Object ... args) {
	super(err, cause, args);
  }
}
