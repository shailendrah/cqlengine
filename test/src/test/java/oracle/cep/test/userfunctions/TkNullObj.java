/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkNullObj.java /main/1 2010/02/25 04:17:04 sborah Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      02/21/10 - returns null object.
    sborah      02/21/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkNullObj.java /main/1 2010/02/25 04:17:04 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;


public class TkNullObj
implements SingleElementFunction
{
  
  public Object execute(Object args[])
  throws UDFException
  {
    if((Integer)args[0] % 2 == 1)
      return null;
    else
      return new Integer(0);
  }
  
  public TkNullObj()
  {
  }
}

