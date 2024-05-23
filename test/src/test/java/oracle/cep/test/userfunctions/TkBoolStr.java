/* $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkBoolStr.java /main/1 2009/02/25 14:23:51 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    User Defined Function - Convert bool to yes/no

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/20/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkBoolStr.java /main/1 2009/02/25 14:23:51 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkBoolStr implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException 
  {
    Boolean arg1 = (Boolean)args[0];
    if (arg1 == null)
      return null;
    return arg1.booleanValue() ? "yes":"no";    
  }
}
