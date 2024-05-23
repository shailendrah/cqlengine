/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBaseEntityNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $ */

/* Copyright (c) 2005, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/11/08 - error offset
    anasrini    02/21/06 - add name field 
    anasrini    12/20/05 - Base class for relations and streams 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBaseEntityNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Base class for relation and stream parse tree nodes
 */

public abstract class CEPBaseEntityNode implements CEPParseTreeNode {

  /**
   * The cql representation of this base entity
   */
  String cqlProperty;

  /** The alias representing this entity */
  protected String alias;

  /** The name of the entity (stream/relation) */
  protected String name;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Default constructor
   */
  CEPBaseEntityNode() {
  }

  /**
   * Constructor
   * @param name  the name for this entity
   * @param alias the alias for this entity
   */
  CEPBaseEntityNode(String name, String alias) {
    this.name  = name;
    this.alias = alias;
  }

  

  // Getter methods

  public String getCqlProperty()
  {
    return this.cqlProperty;
  }
  /**
   * Get the name for this entity
   * @return the name for this entity
   */
  public String getName() {
    return name;
  }

  /**
   * Get the alias for this entity
   * @return the alias for this entity
   */
  public String getAlias() {
    return alias;
  }

  // Setter methods

  public void setCqlProperty(String arg)
  {
    this.cqlProperty = arg;
  }
  
  /**
   * Set the name for this entity
   * @param name name for this entity
   */
  void setName(String name) {
    this.name = name;
  }

  /**
   * Set the alias for this entity
   * @param alias alias for this entity
   */
  void setAlias(String alias) {
    this.alias = alias;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  public abstract int toQCXML(StringBuffer queryXml, int operatorID) throws UnsupportedOperationException;
}
