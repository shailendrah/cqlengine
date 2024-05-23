/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlTableColumnNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     06/18/09 - adding setters for xqrystring
    parujain    08/11/08 - error offset
    mthatte     12/26/07 - 
    najain      11/30/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlTableColumnNode.java /main/3 2009/06/23 14:09:08 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.Datatype;

/**
 * The parse tree node corresponding to xmltable column specification
 *
 * @since 1.0
 */

public class CEPXmlTableColumnNode implements CEPParseTreeNode 
{
  /** Name of the attribute */
  protected String name;

  /** Datatype of the attribute */
  protected Datatype dt;

  /** Length associated with the datatype (optional) */
  protected int length;

  protected String xqryStr;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor
   * @param name name of the attribute
   * @param dt datatype of the attribute
   */
  public CEPXmlTableColumnNode(CEPStringTokenNode nameToken, Datatype dt, CEPStringTokenNode xqryStrToken) 
  {
    this.name = nameToken.getValue();
    this.dt   = dt;
    this.length = dt.getLength();
    this.xqryStr = xqryStrToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(xqryStrToken.getEndOffset());
  }

  /**
   * Constructor for datatype with a length attribute
   * @param name name of the attribute
   * @param dt datatype of the attribute
   * @param length length associated with the datatype
   */
  public CEPXmlTableColumnNode(CEPStringTokenNode nameToken, Datatype dt, CEPIntTokenNode len, CEPStringTokenNode xqryStrToken) 
  {
    this.name   = nameToken.getValue();
    this.dt     = dt;
    this.length = len.getValue();
    this.xqryStr = xqryStrToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(xqryStrToken.getEndOffset());
  }

  // getter methods

  /**
   * Get the name of the attribute
   * @return the name of the attribute
   */
  public String getName() {
    return name;
  }

  /**
   * Get the datatype of the attribute
   * @return the datatype of the attribute
   */
  public Datatype getDatatype() {
    return dt;
  }
  
  /**
   * Get the length associated with the datatype, if any. This applies only 
   * to datatypes that support an associated length (like CHAR)
   * @return the length associated with the datatype, if any
   */
  public int getLength() {
    return length;
  }

  public String getXQryStr() {
    return xqryStr;
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

  public void setXqryStr(String xqryStr) 
  {
	this.xqryStr = xqryStr;
  }
}
