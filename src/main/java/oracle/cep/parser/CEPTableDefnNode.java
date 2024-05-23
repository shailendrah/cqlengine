/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTableDefnNode.java /main/16 2014/10/14 06:35:32 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Parse tree node corresponding to a DDL for a table

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/08/14 - add setPartitioned
    vikshukl    07/30/12 - archived dimension relation
    udeshmuk    04/03/12 - propagate worked id and txn id
    udeshmuk    12/27/11 - support archived stream
    udeshmuk    08/24/11 - add event id column name
    udeshmuk    03/02/11 - archived relation DDL
    parujain    08/11/08 - error offset
    parujain    03/04/08 - function timestamp
    parujain    12/12/07 - db-join
    parujain    12/03/07 - external relation
    udeshmuk    11/18/07 - add boolean flag for systemtimestamped.
    sbishnoi    10/25/07 - support for primary key
    sbishnoi    06/07/07 - fix xlint warnings
    dlenkov     12/01/06 - Added isSilent
    anasrini    02/28/06 - Creation
    anasrini    02/28/06 - Creation
    anasrini    02/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTableDefnNode.java /main/16 2014/10/14 06:35:32 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.exceptions.CEPException;


/**
 * Parse tree node corresponding to a DDL for a table
 *
 * @since 1.0
 */

public class CEPTableDefnNode implements CEPParseTreeNode {

  /** The name of the table */
  protected String name;

  /** The attribute specification list */
  protected CEPAttrSpecNode[] attrSpec;

  /** is this a stream or a relation */
  protected boolean isStream;

  protected boolean isSilent;
  
  /** is this source systemtimestamped */
  protected boolean isSystemTimestamped;
  
  /** is this an archived relation */
  protected boolean isArchived;
  
  /** is this archived relation a dimension **/
  protected boolean isDimension;
  
  /** applicable only in the archived relation case.
   *  stores the name of the archiver class.
   */
  protected String archiverName;
  
  /** applicable only in the archived relation case.
   * stores the name of the entity to which this archived relation maps.
   */
  protected String entityName;

  /** is Primary Key Defined or not on present relation */
  protected boolean isPrimaryKeyExist;
  
  /** primary Key Constraint node*/
  protected CEPRelationConstraintNode primaryKeyConstraint;

  /** isExternal Relation */
  protected boolean isExternal;
  
  /** Expr to get the tuple timestamp */
  protected String timestampExpr;
  
  /** Column name of the event identifier column. Used only for archived rel */ 
  protected String eventIdColName;

  /** timestamp column name. used only for archived stream */
  protected String tsColName = null;

  /** replay clause. used only for archived stream */
  protected CEPReplaySpecNode replayClause = null;

  /** worker identifier column name */
  protected String workerIdColName = null;

  /** transaction identifier column name */
  protected String txnIdColName = null;

  /** Is the stream partitioned or not - This could be true only when upstream channel is local/distributed partitioned channel */
  protected boolean isPartitioned= false;

  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor for stream source
   * @param name name of the table
   * @param attrSpecList list of attribute specification
   */
  public CEPTableDefnNode(CEPStringTokenNode token, List<CEPAttrSpecNode> attrSpecList)
    throws CEPException   
  {
    this.name     = token.getValue();
    this.isStream = true;
    this.isSilent = false;
    this.isArchived = false;
    this.isDimension = false;
    this.eventIdColName = null;
    this.workerIdColName = null;
    this.txnIdColName = null;
    this.attrSpec = 
      (CEPAttrSpecNode[])(attrSpecList.toArray(new CEPAttrSpecNode[0]));
    this.timestampExpr = null;
    setStartOffset(token.getStartOffset());
    if(attrSpecList.size() > 0)
      setEndOffset(attrSpecList.get(attrSpecList.size()-1).getEndOffset());
    else
      setEndOffset(token.getEndOffset());
  }
  
  /**
   * Constructor for relation source
   * @param name name of the table 
   * @param relAttrSpec contains primary key constraint node and
   *   attribute specification list
   */
  public CEPTableDefnNode(CEPStringTokenNode token, CEPRelationAttrSpecsNode relAttrSpec) 
  {
    this.name = token.getValue();
    this.isStream = false;
    this.isArchived = false;
    this.eventIdColName = null;
    this.workerIdColName = null;
    this.txnIdColName = null;
    this.attrSpec = relAttrSpec.getAttrSpecList();
    this.isPrimaryKeyExist = 
      ((this.primaryKeyConstraint = relAttrSpec.getPrimaryKeyConstraint()) != null);
    this.timestampExpr = null;
    setStartOffset(token.getStartOffset());
    setEndOffset(relAttrSpec.getEndOffset());
  }
 
  // setter methods
  public void setPartitioned(boolean isPartn)
  {
    this.isPartitioned = isPartn;
  }

  public void setSystemTimestamped(boolean issys)
  {
    this.isSystemTimestamped = issys;
  }

  public void setExternal(boolean isExt)
  {
    this.isExternal = isExt;
  }
  
  public void setIsSilent(boolean issilent)
  {
    this.isSilent = issilent;
  }

  public void setTimestampExpr(CEPExprNode expr)
  {
    this.timestampExpr = expr.toString();
    setEndOffset(expr.getEndOffset());
  }

  public void setIsArchived(boolean isArchived)
  {
    this.isArchived = isArchived;
  }
  
  public void setIsDimension(boolean isDimension)
  {
    this.isDimension = isDimension;
  }
  
  public void setArchiverName(List<CEPStringTokenNode> nameList)
  {    
    if(nameList.size() > 0)
    {
      this.archiverName = nameList.get(0).getValue();
      nameList.remove(0);
      for(CEPStringTokenNode node : nameList)
      { 
        this.archiverName = this.archiverName + "." + node.getValue();
      }
    }
    else
      this.archiverName = null;
  }

  public void setEntityName(String entityName)
  {
    this.entityName = entityName;
  }

  // getter methods
  
  public boolean isPartitioned()
  {
    return isPartitioned;
  }

  public String getArchiverName()
  {
    return archiverName;
  }

  public String getEntityName()
  {
    return entityName;
  }
  
  public boolean isArchived()
  {
    return isArchived;
  }
  
  public boolean isDimension()
  {
    return isDimension;
  }
  
  public String getEventIdColName()
  {
    return eventIdColName;
  }
  
  public void setEventIdColName(String eventIdColName)
  {
    this.eventIdColName = eventIdColName;
  }

  public String getWorkerIdColName()
  {
    return workerIdColName;
  }
  
  public void setWorkerIdColName(String workerIdColName)
  {
    this.workerIdColName = workerIdColName;
  }

  public String getTxnIdColName()
  {
    return txnIdColName;
  }
  
  public void setTxnIdColName(String txnIdColName)
  {
    this.txnIdColName = txnIdColName;
  }
  public String getTimestampColumn()
  {
    return tsColName;
  }

  public void setTimestampColumn(CEPStringTokenNode tscol)
  {
    this.tsColName = tscol.getValue();
  }

  public CEPReplaySpecNode getReplayClause()
  {
    return this.replayClause;
  }

  public void setReplayClause(CEPReplaySpecNode replayClause)
  {
    this.replayClause = replayClause;
  }
  /**
   * Is this a stream or a relation ?
   * @return true if stream, false if relation
   */
  public boolean isStreamDefn() {
    return isStream;
  }

  /**
   * Is it set silent?
   */

  public boolean getIsSilent() {
    return isSilent;
  }
  
  public boolean getIsSystemTimestamped() {
    return isSystemTimestamped;
  }
  
  /**
   * Get the name of the table
   * @return the name of the table
   */
  public String getName() {
    return name;
  }

  /**
   * Get the attribute specification list
   * @return the attribute specification list
   */
  public CEPAttrSpecNode[] getAttrSpecList() {
    return attrSpec;
  }
  
  /**
   * Get the timestamp expr
   * @return the timestamp expr
   */
  public String getTimeStampExpr()
  {
    return timestampExpr;
  }

  /**
   * Get isPrimaryKeyExist Flag
   * @return
   */
  public boolean getIsPrimaryKeyExist()
  {
    return this.isPrimaryKeyExist;
  }
  
  /**
   * Get Primary Key Constraint Node
   * @return
   */
  public CEPRelationConstraintNode getPrimaryKeyConstraintNode()
  {
    return this.primaryKeyConstraint;
  }

  /**
   * Get whether it is an External relation or not
   * @return Whether it is an External relation or not
   */
  public boolean isExternal() 
  {
    return this.isExternal;
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

  
  
  // toString

  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (isStream) {
      sb.append("<StreamDefn name=\"" + name + "\">");
    }
    else {
      sb.append("<RelationDefn name=\"" + name + "\">");
    }

    for (int i=0; i<attrSpec.length; i++) {
      sb.append(attrSpec[i].toString());
    }
 
    sb.append("isExternal=\"" + isExternal + "\" ");
    sb.append("isArchived=\"" + isArchived + "\" ");
    sb.append("isPartitioned=\"" + isPartitioned + "\" ");

    if (isStream)
      sb.append("</StreamDefn>");
    else
      sb.append("</RelationDefn>");

    return sb.toString();
  }
  
}
