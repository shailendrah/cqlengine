/* $Header: TkUsrFib.java 13-may-2008.09:02:06 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    User Defined Function - Fibonnaci Numbers

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/13/08 - 
    sbishnoi    02/05/08 - adding params to UDF Error
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    skmishra    12/09/06 - 
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
 */

/**
 *  @version $Header: TkUsrFib.java 13-may-2008.09:02:06 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

public class TkUsrFib implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException{
    int arg = 0;
    int res = 0;
    try {
      arg = ((Integer)args[0]).intValue();
      res = getFib(arg);
    }
    catch (Exception e) {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                             new Object[] {"TkUsrFib"});
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
