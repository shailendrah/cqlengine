/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAttrNameNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The parse tree node corresponding to attribute name in the DDL
    for a view

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    parujain    08/11/08 - error offset
    najain      05/09/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPAttrNameNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  najain  
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.Datatype;

/**
 * The parse tree node corresponding to attribute name in the DDL
 * for a view
 *
 * @since 1.0
 */

public class CEPAttrNameNode implements CEPParseTreeNode {

  /** Name of the attribute */
  protected String name;

  protected int startOffset;
  
  protected int endOffset;
  /**
   * Constructor
   * @param name name of the attribute
   */
  public CEPAttrNameNode(String name) {
    this.name = name;
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


}

