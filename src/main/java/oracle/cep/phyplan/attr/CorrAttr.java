/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/attr/CorrAttr.java /main/5 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/22/11 - support getSQLEquivalent
    sborah      04/26/09 - add getSignature()
    rkomurav    05/22/07 - add getBindPos
    rkomurav    05/29/07 - add getBindPos
    rkomurav    03/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/attr/CorrAttr.java /main/4 2009/04/27 11:34:36 sborah Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.attr;

import oracle.cep.service.ExecContext;

public class CorrAttr extends Attr
{
  /** Binding pos */
  int binding;

  /**
   * @param input
   * @param pos
   * @param binding
   */
  public CorrAttr(int input, int pos, int binding)
  {
    this.input   = input;
    this.pos     = pos;
    this.binding = binding;
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    CorrAttr other = (CorrAttr)otherObject;
    return (input == other.getInput() && pos == other.getPos()
        && binding == other.binding);
  }

  /**
   * Method to calculate a concise String representation
   * of the attribute based on its input and position value.
   * @return 
   *      A concise String representation of the attribute.
   */
  public String getSignature()
  {
    return  "(" + this.input + ","  + this.pos + 
            "," + this.getBindPos() + ")";
  }
  
  public String getSQLEquivalent(ExecContext ec)
  {
    return null;
  }
  
  public int getBindPos() 
  {
    return binding;
  }

}

