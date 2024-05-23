/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/RelationSpec.java /main/1 2009/06/04 17:45:06 sbishnoi Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/18/09 - from clause relation spec
    parujain    05/18/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/RelationSpec.java /main/1 2009/06/04 17:45:06 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

public class RelationSpec
{
  int varId;
  
  boolean isOuterRelation;

  public RelationSpec(int id)
  {
    this.varId = id;
    this.isOuterRelation = false;
  }

  public int getVarId()
  {
    return this.varId;
  }
  
  public void setOuterRelation(boolean isouter)
  {
    this.isOuterRelation = isouter;
  }
  
  public boolean isOuterRelation()
  {
    return isOuterRelation;
  }
  
  public String toString()
  {
    return "" + varId;
  }
}
