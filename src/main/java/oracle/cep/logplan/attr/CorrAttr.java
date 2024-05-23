/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/CorrAttr.java /main/3 2012/05/02 03:05:59 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Correlation Attribute at logical level

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - added datatype as arg
    anasrini    05/25/07 - inline view support - remove tableid
    rkomurav    03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/CorrAttr.java /main/3 2012/05/02 03:05:59 pkali Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.attr;

import oracle.cep.common.Datatype;

public class CorrAttr extends Attr
{
  /** Variable Identifier */
  private int varId;

  /** varId of base entity for which this is a coorelation name */
  private int baseEntityVarId;

  /** Attribute Identifier */
  private int attrId;
  
  public CorrAttr(Datatype dt)
  {
    attrKind = AttrKind.CORR;
    this.dt  = dt;
  }

  /**
   * Constructor
   */
  public CorrAttr(int varId, int baseEntityVarId, int attrId, Datatype dt)
  {
    this.varId           = varId;
    this.baseEntityVarId = baseEntityVarId;
    this.attrId          = attrId;
    this.attrKind        = AttrKind.CORR;
    this.dt = dt;
  }
  
  //equals method
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    //compare only attrid and tableId with Attrnamed
    if (otherObject instanceof AttrNamed)
    {
      AttrNamed named = (AttrNamed) otherObject;
      return ((baseEntityVarId == named.getVarId()) &&
              (attrId == named.getAttrId())
             );
    }
    
    if (getClass() != otherObject.getClass())
      return false;

    CorrAttr other = (CorrAttr) otherObject;

    return ((varId == other.varId) && 
            (baseEntityVarId == other.baseEntityVarId) &&
            (attrId == other.attrId)
           );
  }

  /**
   * @return the attrId
   */
  public int getAttrId()
  {
    return attrId;
  }

  /**
   * @return the base entity's variable id
   */
  public int getBaseEntityVarId()
  {
    return baseEntityVarId;
  }

  /**
   * @return the varId
   */
  public int getVarId()
  {
    return varId;
  }

}

