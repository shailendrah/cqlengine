/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/Attr.java /main/4 2012/05/02 03:06:01 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Post semantic analysis representation of an attribute

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - added datatype member
    udeshmuk    04/01/11 - store name of the attr
    rkomurav    05/28/07 - add equals
    anasrini    05/25/07 - corr attr support
    anasrini    02/26/06 - implement toString 
    anasrini    02/24/06 - add javadoc comments 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/Attr.java /main/4 2012/05/02 03:06:01 pkali Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;

/**
 * Post semantic analysis representation of an attribute.
 * <p>
 * Reference to an attribute in a  query: indicated by a variable id and
 * the  attribute id.   The variable  id identifies  the relation/stream
 * that  the  attribute  belongs  to  and attribute  id  identifies  the
 * attribute among the attributes of the relation/stream.  Note that the
 * same  stream/relation could  occur many  times in  a query  and so
 * could have more than one variable id.
 *
 * @since 1.0
 */

public class Attr {
  
  private int varId;
  private int attrId;
  private String actualName;
  private Datatype dt;

  /**
   * Constructor
   * @param varId internal identifier of a relation in the from clause
   * @param attrId internal identifier of an attribute of that relation
   */
  public Attr(int varId, int attrId, String actualName, Datatype dt) {
    this.varId  = varId;
    this.attrId = attrId;
    this.actualName = actualName;
    this.dt = dt;
  }

  /**
   * Get the internal identifer of the from clause relation that this
   * attribute belongs to
   * @return the internal identifer of the associated from clause relation
   */
  public int getVarId() {
    return varId;
  }

  /**
   * Get the internal identifer of the associated relation attribute
   * @return the internal identifer of the associated relation attribute
   */
  public int getAttrId() {
    return attrId;
  }

  /**
   * Get the data type of the  attribute
   * @return attribute datatype
   */
  public Datatype getDatatype() {
    return dt;
  }
  
  /**
   * Get the actual name of the attribute
   * @return name of the attribute (fully qualified if available)
   */
  public String getActualName()
  {
    return actualName;
  }
  
  /**
   * @return the type of the semantic layer attribute
   */
  public SemAttrType getSemAttrType() {
    return SemAttrType.NAMED;
  }

  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;
    
   if (getClass() != otherObject.getClass())
      return false;

    Attr other = (Attr) otherObject;

    return ((varId == other.varId) && (attrId == other.attrId));
  }

  // toString
  public String toString() {
    return "<attr varId=\"" + varId + "\" attrId=\"" + attrId + "\" />";
  }
}
