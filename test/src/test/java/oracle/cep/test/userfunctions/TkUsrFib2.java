/* $Header: TkUsrFib2.java 13-may-2008.09:02:08 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/13/08 - 
    sbishnoi    02/11/08 - error parameterization
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    skmishra    12/09/06 - 
    najain      09/21/06 - Creation
 */

/**
 *  @version $Header: TkUsrFib2.java 13-may-2008.09:02:08 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

public class TkUsrFib2 implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException{
    float arg = 0;
    int res = 0;
    try {
      arg = ((Float)args[0]).floatValue();
      res = getFib((int)arg);
    }
    catch (Exception e) {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                             "TkUsrFib2");
    }

    return new Integer(res);    
  }

  private int getFib(int n) {
    if (n < 0)  return 0;
    if (n == 0) return 1;
    if (n == 1) return 1;

    return getFib(n-1) + getFib(n-2);
  }
  
}
