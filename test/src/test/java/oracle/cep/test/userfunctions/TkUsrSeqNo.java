/* $Header: TkUsrSeqNo.java 13-may-2008.09:02:35 hopark Exp $ */

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
    najain      01/22/07 - Creation
 */

/**
 *  @version $Header: TkUsrSeqNo.java 13-may-2008.09:02:35 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrSeqNo implements SingleElementFunction {
  private static int seqNo;

  static
  {
    seqNo = 0;
  }

  public Object execute(Object[] args) throws UDFException{
    return new Integer(seqNo++);    
  }

}
