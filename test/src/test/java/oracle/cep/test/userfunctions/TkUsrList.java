/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrList.java /main/1 2010/03/20 08:53:21 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    03/17/10 - Creation
 */

package oracle.cep.test.userfunctions;

import java.util.LinkedList;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrList.java /main/1 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TkUsrList implements SingleElementFunction {
  public Object execute(Object[] args) throws UDFException {

   LinkedList<Integer> list = new LinkedList<Integer>();
   try
   {
     int arg = ((Integer)args[0]).intValue();
     for(int i= 1; i <= arg; i++)
      list.add(i);
   }
   catch(Exception e)
   {
      e.printStackTrace();
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
                              new Object[]{"TkUsrList"});
   }
   return list;
  }
}
