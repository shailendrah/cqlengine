/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkMakeId.java /main/1 2009/10/30 15:55:02 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/02/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkMakeId.java /main/1 2009/10/30 15:55:02 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;

public class TkMakeId implements SingleElementFunction {

  public Object execute(Object[] args) {
    long res = 0;
    try {
      long arg0 = (long)((Integer)args[0]).intValue();
      long arg1 = (long)((Integer)args[1]).intValue();
      res = (arg0 << 32) | arg1;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new Long(res);    
  }
}
