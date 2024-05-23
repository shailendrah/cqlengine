/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTableAttrEntry.java /main/2 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    This corresponds to an entry for an attribute of an inline view

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      07/15/09 - support for bigdecimal
    rkomurav    05/27/07 - 
    anasrini    05/25/07 - add nextAttrVarId
    anasrini    05/23/07 - add attrId
    anasrini    05/18/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTableAttrEntry.java /main/2 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;

/**
 * This corresponds to an entry for an attribute of an inline view. 
 * Since inline views are not persistent, this is the only place
 * where their attributes are registered
 */

public class SymbolTableAttrEntry extends SymbolTableEntry {

  private int      inlineViewVarId;
  private int      attrId;
  private Datatype dt;
  private int      len;
  private int      precision;
  private int      scale;
  private int      nextAttrVarId;

  /**
   * Constructor
   * @param inlineViewVarId the varId of the inline view of which this is
   *                        an attribute
   * @param attrId id of the attribute. It will be a value between 0 .. n-1
   *               where n is the number of attributes of the inline view
   * @param dt the datatype of this attribute
   * @param len the maximum length of this attribute if this is a variable
   *            length attribute
   * @param precision the precision value of the attribute if it is of 
   *                  type bigdecimal
   *@param scale the scale value of the attribute if it is of 
   *             type bigdecimal
   */
  SymbolTableAttrEntry(String varName, int inlineViewVarId, int attrId,
                       Datatype dt, int len, int precision, int scale) {
    super(varName);
    this.inlineViewVarId = inlineViewVarId;
    this.attrId          = attrId;
    this.dt              = dt;
    this.len             = len;
    this.precision       = precision;
    this.scale           = scale;
    // set this to an illegal value
    this.nextAttrVarId   = -1;
  }
  
  /**
   * Constructor
   * @param inlineViewVarId the varId of the inline view of which this is
   *                        an attribute
   * @param attrId id of the attribute. It will be a value between 0 .. n-1
   *               where n is the number of attributes of the inline view
   * @param dt the datatype of this attribute
   * @param len the maximum length of this attribute if this is a variable
   *            length attribute
   */
  SymbolTableAttrEntry(String varName, int inlineViewVarId, int attrId,
                       Datatype dt, int len) 
  {
    this(varName, inlineViewVarId, attrId, dt, len, dt.getPrecision(), 0);
  }

  // Setters

  /**
   * This sets the varId of the next attr of this inline view.
   * Applies only to INLINE_VIEW. Does not apply to PERSISTENT sources.
   * @param nextAttrvarId varId of the next attr of this inline view
   */
  void setNextAttrVarId(int nextAttrVarId) {
    this.nextAttrVarId = nextAttrVarId;
  }

  // Getters
  
  /**
   * Get the symbol table entry id of the inline view of which this 
   * is an attribute
   * @return the symbol table entry id of the inline view of which this 
   *         is an attribute
   */
  int getInlineViewVarId() {
    return inlineViewVarId;
  }

  /**
   * Get the attrId for this attribute
   * @return the attrId for this attribute
   */
  int getAttrId() {
    return attrId;
  }

  /**
   * Get the datatype for this attribute
   * @return the datatype for this attribute
   */
  Datatype getAttrType() {
    return dt;
  }

  /**
   * Get the length for this attribute. Applies only if it is variable length
   * @return the length for this attribute. Applies only if it is variable
   *         length, else return 0.
   */
  int getAttrLen() {
    return len;
  }
  
  /**
   * Get the precision for this attribute.Applies only for bigdecimal attribute
   * @return the precision for this attribute. 
   */
  int getAttrPrecision() 
  {
    return precision;
  }

  /**
   * Get the precision for this attribute.Applies only for bigdecimal attribute
   * @return the precision for this attribute. 
   */
  int getAttrScale() 
  {
    return scale;
  }
  
  /**
   * Get the varId of the next attribute of an inline view
   * @return the varId of the next attribute of an inline view
   */
  int getNextAttrVarId() {
    return nextAttrVarId;
  }

  SymTableEntryType getEntryType() {
    return SymTableEntryType.ATTR;
  }

}



