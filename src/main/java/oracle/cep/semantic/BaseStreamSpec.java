/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseStreamSpec.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       06/27/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/BaseStreamSpec.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

public class BaseStreamSpec
{
  private int varId;
  
  public BaseStreamSpec(int id)
  {
    this.varId = id;
  }

  public int getVarId()
  {
    return this.varId;
  }

  public String toString()
  {
    return "" + varId;
  }
}