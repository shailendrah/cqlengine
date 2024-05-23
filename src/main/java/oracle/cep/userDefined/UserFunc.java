/* $Header: UserFunc.java 28-apr-2006.14:20:45 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/28/06 - Creation
 */

/**
 *  @version $Header: UserFunc.java 28-apr-2006.14:20:45 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.userDefined;

import oracle.cep.common.Datatype;

public class UserFunc
{
  SingleElementUserFunction  func;
  Datatype                   dataType;
  int                        numArgs;
  
  /**
   * @return Returns the dataType.
   */
  public Datatype getDataType() {
    return dataType;
  }
  
  /**
   * @return Returns the func.
   */
  public SingleElementUserFunction getFunc() {
    return func;
  }
  
  /**
   * @return Returns the numArgs.
   */
  public int getNumArgs() {
    return numArgs;
  }
}

