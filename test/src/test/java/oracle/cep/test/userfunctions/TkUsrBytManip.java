/* $Header: TkUsrBytManip.java 13-may-2008.09:02:00 hopark Exp $ */

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
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    sbishnoi    03/21/07 - Creation
 */

/**
 *  @version $Header: TkUsrBytManip.java 13-may-2008.09:02:00 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrBytManip implements SingleElementFunction {
  
  public Object execute(Object[] args) throws UDFException{

    if(args[0] != null)
    {
      byte[] b  = (byte[])args[0];
      byte[] b1 = new byte[b.length + 1];
      System.arraycopy(b,0,b1,0,b.length);
      b1[b.length] = 0;
      return b1;
    }
    return null;
    
  }

}
