/* $Header: SingleElementUserFunction.java 27-apr-2006.18:13:48 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
     Abstract class for Single Defined User Function

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/27/06 - Creation
 */

/**
 *  @version $Header: SingleElementUserFunction.java 27-apr-2006.18:13:48 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.userDefined;

import java.util.Vector;

public abstract class SingleElementUserFunction 
{
  /*
   * Vector parameters - There will be as many input vector elements as the 
   * number of function arguments. parameters(i) will correspond to function 
   * argument i. 
   */
  public abstract Object execute(Vector parameters);
}
