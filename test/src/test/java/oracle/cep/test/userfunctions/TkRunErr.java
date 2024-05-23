/* $Header: TkRunErr.java 13-may-2008.09:01:41 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

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
    parujain    04/16/07 - Creation
 */

/**
 *  @version $Header: TkRunErr.java 13-may-2008.09:01:41 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

public class TkRunErr implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException {
   
    if(args.length > 0)
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "TkRunErr");
   
    return null;
  }

}
