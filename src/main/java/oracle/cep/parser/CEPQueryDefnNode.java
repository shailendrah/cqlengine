/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryDefnNode.java /main/8 2012/08/09 00:10:57 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/02/12 - evaluate clause cleanup
    sbishnoi    05/16/12 - support slide without window
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/04/09 - adding toQCXML
    parujain    08/11/08 - 
    sbishnoi    11/05/07 - support for update semantics
    dlenkov     08/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryDefnNode.java /main/8 2012/08/09 00:10:57 sbishnoi Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to a DDL for query registration
 *
 * @since 1.0
 */
public class CEPQueryDefnNode implements CEPQueryNode {

  /** The name of the query */
  protected String name;

  /** The query being registered */
  protected CEPQueryNode query;
  
  /** Key defined over output of this query; Used by update semantics*/
  protected CEPRelationConstraintNode outputConstraintNode;
  
  protected int startOffset;
  
  protected int endOffset;

  public CEPQueryDefnNode() {}
  
  /**
   * Constructor 
   *
   * @param name name of the query
   * @param query the query
   */
  public CEPQueryDefnNode( CEPStringTokenNode nameToken, 
                           CEPQueryNode query) 
  {
    this(nameToken, query, null);
  }
  
  /**
   * Constructor
   * @param name name of query
   * @param query the query
   * @param outputConstraintNode
   * @param optional slide expression
   */
  public CEPQueryDefnNode( CEPStringTokenNode nameToken, 
                           CEPQueryNode query, 
                           CEPRelationConstraintNode outputConstraintNode)
  {
    this.name                 = nameToken.getValue();
    this.query                = query;
    this.outputConstraintNode = outputConstraintNode;
    
    setStartOffset(nameToken.getStartOffset());
    if(outputConstraintNode != null)
      setEndOffset(outputConstraintNode.getEndOffset());
    else
      setEndOffset(query.getEndOffset());
    
  }
  
  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return Returns the query.
   */
  public CEPQueryNode getQueryNode()
  {
    return query;
  }
  
  /**
   * @return Returns primary key constraint node
   */
  public CEPRelationConstraintNode getPrimaryKeyNode()
  {
    return outputConstraintNode;
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
  
  public int toQCXML(StringBuffer qXml, int operatorID) 
    throws UnsupportedOperationException
  {
    return query.toQCXML(qXml, operatorID);
  }

  /**
   * Return true if cql query contains one or more than one logical CQL clauses
   */
  @Override
  public boolean isLogical()
  {
    return query.isLogical();
  }

  
}
