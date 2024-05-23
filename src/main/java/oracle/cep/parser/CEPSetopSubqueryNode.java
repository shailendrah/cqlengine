/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSetopSubqueryNode.java /main/1 2011/10/11 14:04:18 vikshukl Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    09/26/11 - Creation
 */

/**
 *  @version $Header: CEPSetopSubqueryNode.java 26-sep-2011.10:08:33 vikshukl Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.RelSetOp;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a query with a set operation
 *
 * @since 1.0
 */

public class CEPSetopSubqueryNode extends CEPQueryRelationNode {

  /** The set operator type */
  protected RelSetOp setopType;

  /** The left query. This could in turn be a CEPSetopQuery as well */
  protected CEPQueryNode left;
  
  /** The right query. This could in turn be a CEPSetopQuery as well */
  protected CEPQueryNode right;
  
  /** Is it a Union All */
  protected boolean isUnionAll;

  /**
   * Constructor
   * @param setopType the set operator type
   * @param leftTable the left operand
   * @param rightTable the right operand
   */
  public CEPSetopSubqueryNode(RelSetOp setopType, 
                              CEPQueryNode left,
                              CEPQueryNode right) {
    this.setopType    = setopType;
    this.left  = left;
    this.right= right;
    this.isUnionAll = false;
    setStartOffset(left.getStartOffset());
    setEndOffset(right.getEndOffset());
  }
 
  /**
   * Constructor
   * @param setopType the set operator type
   * @param leftTable the left operand
   * @param rightTable the right operand
   * @param isUnionAll Is operator union all or not
   */
  public CEPSetopSubqueryNode(RelSetOp setopType, 
                    CEPQueryNode left,
                    CEPQueryNode right,
                    boolean isUnionAll) {
    this.setopType    = setopType;
    this.left  = left;
    this.right = right;
    this.isUnionAll = isUnionAll;
    setStartOffset(left.getStartOffset());
    setEndOffset(right.getEndOffset());
  }

  /**
   * Get the operator
   * @return the operator
   */
  public RelSetOp getRelSetOp() {
    return setopType;
  }

  /**
   * Get the left operand
   * @return the left operand
   */
  public CEPQueryNode getLeft() {
    return left;
  }

  /**
   * Get the right operand
   * @return the right operand
   */
  public CEPQueryNode getRight() {
    return right;
  }
  
  /**
   * Get type of Union operation
   * @return true if operation is 'union all'
   */
  public boolean isUnionAll() {
	  return isUnionAll;
  }

  public boolean isQueryRelationNode() 
  {
    return true;
  }
  
  public String toString()
  {
    if(isUnionAll)
      return " " + left.toString() + " UNION ALL " + right.toString() + " ";
    else
      return " " + left.toString() + " " + setopType.toString() + " " + 
             right.toString() + " ";
  }
  
  private String createSourceXml()
  {
    StringBuilder myXml = new StringBuilder(30);
    
    String leftSourceName = XMLHelper.buildElement(true, 
        VisXMLHelper.sourceNameTag, left.toString(), null, null);
    String leftCqlProperty = 
      VisXMLHelper.createCqlPropertyXml("select * from " + left.toString());
    String rightSourceName = 
      XMLHelper.buildElement(true, VisXMLHelper.sourceNameTag,right.toString(),
                             null, null);
    String rightCqlProperty = 
      VisXMLHelper.createCqlPropertyXml("select * from " + right.toString());
    String leftSourceOperator = 
      VisXMLHelper.createOperatorTag(VisXMLHelper.sourceOperator, 1, 
          leftSourceName + "\n" + leftCqlProperty);
    String rightSourceOperator = 
      VisXMLHelper.createOperatorTag(VisXMLHelper.sourceOperator, 2, 
          rightSourceName + " \n" + rightCqlProperty);
    
    myXml.append("\n" + leftSourceOperator);
    myXml.append("\n" + rightSourceOperator);
    
    return myXml.toString();
  }
  
  private String createIntersectXml(int operatorId)
  {

    StringBuilder myXml = new StringBuilder(50);
    
    String inputs = VisXMLHelper.createInputsXml(new int[]{1,2});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(toString());
    
    myXml.append(inputs);
    myXml.append(cqlPropertyXml);
 
    String intersectOperator = 
      VisXMLHelper.createOperatorTag(VisXMLHelper.intersectOperator, 3, 
          myXml.toString());
    return intersectOperator;
  }
  
  private String createMinusXml(int operatorId)
  {

    StringBuilder myXml = new StringBuilder(50);
    
    String inputs = VisXMLHelper.createInputsXml(new int[]{1,2});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(toString());
    
    myXml.append(inputs);
    myXml.append(cqlPropertyXml);
 
    String minusOperator = 
      VisXMLHelper.createOperatorTag(VisXMLHelper.minusOperator, 3, 
                                     myXml.toString());
    return minusOperator;
  }
  
  private String createUnionXml(int operatorId)
  {

    StringBuilder myXml = new StringBuilder(50);
    
    String inputs = VisXMLHelper.createInputsXml(new int[]{1,2});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(toString());
    
    myXml.append(inputs);
    myXml.append(cqlPropertyXml);
    myXml.append(XMLHelper.buildElement(true, VisXMLHelper.unionAllTag, 
                                        String.valueOf(isUnionAll), null, null));

    String unionOperator = 
      VisXMLHelper.createOperatorTag(VisXMLHelper.unionOperator, 3, 
                                     myXml.toString());
    return unionOperator;
  }
  
  //THIS METHOD ASSUMES THAT THERE ARE EXACTLY TWO INPUTS AND A SET OPERATOR
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    assert operatorID == 1;
    
    switch(setopType)
    {
    case INTERSECT:
      queryXml.append(createSourceXml());
      queryXml.append(createIntersectXml(operatorID));
      break;
    case MINUS:
      queryXml.append(createSourceXml());
      queryXml.append(createMinusXml(operatorID));
      break;
    case UNION:
      queryXml.append(createSourceXml());
      queryXml.append(createUnionXml(operatorID));
      break;
    default:
      throw new 
        UnsupportedOperationException("Operation not supported");
    }
    return 4;
  }
  
  /**
   * Returns true if SetOp Query represented by this node contains
   * any logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    //TODO: In existing scope of implementation, There is no handling of
    // logical syntax in set operations.
    return false;
  }
}
