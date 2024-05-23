
/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkGenSeq.java /main/1 2009/10/30 15:55:04 hopark Exp $ */

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
    hopark      05/13/08 - 
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    najain      01/22/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkGenSeq.java /main/1 2009/10/30 15:55:04 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkGenSeq implements SingleElementFunction 
{
  private static final int MAX = 10;
  private static long[] seqNos;

  static
  {
    seqNos = new long[MAX];
    for (int i = 0; i < MAX; i++)
      seqNos[i] = 0;
  }

  public Object execute(Object[] args) throws UDFException
  {
    int id = (Integer)args[0];
    return new Long(seqNos[id]++);    
  }

}
