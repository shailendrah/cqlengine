/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelationConstraintNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    08/11/08 - error offset
    sbishnoi    10/25/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPRelationConstraintNode.java /main/3 2008/08/25 19:27:24 parujain Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;
import java.util.Iterator;

import oracle.cep.exceptions.CEPException;

public class CEPRelationConstraintNode implements CEPParseTreeNode {
  
  /** name of Table */
  String tableName;
  
  /** list of columns */
  String[] columns;
  
  int numAttrs;
  
  int startOffset;
  
  int endOffset;
  
  /**
   * Constructor < List of Columns>
   * @param columns
   * @throws CEPException
   */
  public CEPRelationConstraintNode(List<CEPStringTokenNode> columns)
    throws CEPException
  {
    numAttrs = columns.size();
    this.columns = new String[numAttrs];
    int i = 0;
    Iterator<CEPStringTokenNode> iter = columns.iterator();
    while(iter.hasNext())
    {
      CEPStringTokenNode node = iter.next();
      this.columns[i] = node.getValue();
      if(i == 0)
        setStartOffset(node.getStartOffset());
      if(i == (numAttrs-1))
        setEndOffset(node.getEndOffset());
      i++;
    }
  }
  
  /**
   * Constructor
   */
  public CEPRelationConstraintNode()
  {
   this.columns = new String[1];
  }
  
  public void addColumn(CEPAttrSpecNode node)
  {
    this.columns[numAttrs++] = node.name;
  }
  
  public String[] getColumns()
  {
    return this.columns;
  }
  
  public String getTableName()
  {
    return this.tableName;
  }
  
  public void setTableName(String tableName)
  {
    this.tableName = tableName;
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
