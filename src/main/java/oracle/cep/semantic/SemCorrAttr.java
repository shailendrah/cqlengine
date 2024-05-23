/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemCorrAttr.java /main/4 2012/05/02 03:06:05 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Semantic layer representation of a correlation attribute

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - included datatype arg in constructor
    udeshmuk    04/01/11 - store name of attr
    rkomurav    05/28/07 - add equals
    rkomurav    05/27/07 - 
    anasrini    05/25/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemCorrAttr.java /main/4 2012/05/02 03:06:05 pkali Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;

/**
 * Semantic layer representation of a correlation attribute
 * <p>
 * A correlation attribute is essentially a named attribute with one
 * extra field. It also contains the varId of the base entity of which
 * its correlation name corresponds to
 */

public class SemCorrAttr extends Attr {
  
  private int baseEntityVarId;

  /**
   * Constructor
   * @param varId internal identifier of correlation name
   * @param attrId internal identifier of attribute of this correlation name
   * @param baseEntityVarId varId of the base entity of which its correlation
   *                        name corresponds to
   * @param name  actual name of the attr
   */
  public SemCorrAttr(int varId, int attrId, int baseEntityVarId, String name, 
                                 Datatype dt) {
    super(varId, attrId, name, dt);
    this.baseEntityVarId = baseEntityVarId;
  }

  /**
   * Get the base entity's varId
   * @return the base entity's varId
   */
  public int getBaseEntityVarId() {
    return baseEntityVarId;
  }

  public SemAttrType getSemAttrType() {
    return SemAttrType.CORR;
  }
  
  public boolean equals(Object otherObject)
  {
    boolean check = super.equals(otherObject);
    
    if(!check)
      return false;
    
    SemCorrAttr other = (SemCorrAttr) otherObject;

    return (baseEntityVarId == other.baseEntityVarId);
  }

}
