/* $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkObjFunc.java /main/1 2009/02/19 16:44:30 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    User Defined Function using object

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/13/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkObjFunc.java /main/1 2009/02/19 16:44:30 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

public class TkObjFunc implements SingleElementFunction 
{
  public Object execute(Object[] args) throws UDFException{
    int arg = 0;
    int res = 0;
    TkObj obj = (TkObj) args[0];
    try {
      arg = obj.getIVal();
      res = arg;    
    }
    catch (Exception e) {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                             new Object[] {"TkObjFunc"});
    }
    obj.setIVal(res);
    obj.setLVal(res*100);
    obj.setFVal(res/100);
    obj.setDVal(res/10000);
    obj.setSVal("res"+res);
    return obj;    
  }
  
}
