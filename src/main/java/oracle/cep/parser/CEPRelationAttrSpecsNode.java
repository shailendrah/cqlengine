/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelationAttrSpecsNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

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
    sbishnoi    11/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPRelationAttrSpecsNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

public class CEPRelationAttrSpecsNode implements CEPParseTreeNode
{
  /** The attribute specification list */
  protected CEPAttrSpecNode[] attrSpec;
  
  /** primary Key Constraint node*/
  protected CEPRelationConstraintNode primaryKeyConstraint;
  
  protected int startOffset = 0;
  protected int endOffset = 0;
  
  /**
   * Constructor
   * @param attrSpecList List of attribute specifications
   * @param primaryKeyConstraint Primary Key Constraint
   */
  public CEPRelationAttrSpecsNode(List<CEPAttrSpecNode> attrSpecList,
                           CEPRelationConstraintNode primaryKeyConstraint)
  {
    this.primaryKeyConstraint = primaryKeyConstraint;
    this.attrSpec = 
      (CEPAttrSpecNode[])(attrSpecList.toArray(new CEPAttrSpecNode[0]));
    if(!attrSpecList.isEmpty())
      setStartOffset(attrSpecList.get(0).getStartOffset());
    else
      setStartOffset(primaryKeyConstraint.getStartOffset());
    setEndOffset(primaryKeyConstraint.getEndOffset());
  }
  
  /**
   * Constructor 
   * @param primaryKeyConstraint Primary Key Constraint
   * @param attrSpecList List of attribute specifications
   */
  public CEPRelationAttrSpecsNode(CEPRelationConstraintNode primaryKeyConstraint,
		                   List<CEPAttrSpecNode> attrSpecList)
  {
    this.primaryKeyConstraint = primaryKeyConstraint;
    this.attrSpec = 
      (CEPAttrSpecNode[])(attrSpecList.toArray(new CEPAttrSpecNode[0]));
    setStartOffset(primaryKeyConstraint.getStartOffset());
    if(!attrSpecList.isEmpty())
      setEndOffset(attrSpecList.get(attrSpecList.size()-1).getEndOffset());
    else
      setEndOffset(primaryKeyConstraint.getEndOffset());
  }
  
  
  /**
   * Constructor
   * @param attrSpecList : List of Attribute Specifications
   */
  public CEPRelationAttrSpecsNode(List<CEPAttrSpecNode> attrSpecList)
  {
    this.attrSpec = 
      (CEPAttrSpecNode[])(attrSpecList.toArray(new CEPAttrSpecNode[0]));
    if(!attrSpecList.isEmpty())
    {
      setStartOffset(attrSpecList.get(0).getStartOffset());
      setEndOffset(attrSpecList.get(attrSpecList.size()-1).getEndOffset());
    }
  }
  
  /**
   * Get Primary Key constraint node
   * @return primaryKeyConstraint
   */
  public CEPRelationConstraintNode getPrimaryKeyConstraint()
  {
    return this.primaryKeyConstraint;
  }
  
  /**
   * Get Attribute Specification List
   * @return attrSpec
   */
  public CEPAttrSpecNode[] getAttrSpecList()
  {
    return this.attrSpec;
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

