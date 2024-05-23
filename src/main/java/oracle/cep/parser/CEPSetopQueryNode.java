/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSetopQueryNode.java /main/8 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a query with a set opertion

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    04/29/09 - bug 8469828
    skmishra    02/20/09 - adding isUnionAll to xml
    skmishra    01/27/09 - adding toQCXML()
    parujain    08/13/08 - error offset
    sbishnoi    04/03/07 - support for union all
    dlenkov     06/07/06 - added get methods
    anasrini    12/20/05 - A query with a set operation 
    anasrini    12/20/05 - A query with a set operation 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPSetopQueryNode.java /main/7 2009/04/30 11:40:59 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.RelSetOp;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a query with a set opertion
 *
 * @since 1.0
 */

public class CEPSetopQueryNode extends CEPQueryRelationNode {

  /** The set operator type */
  protected RelSetOp setopType;

  /** The left query. This could inturn be a CEPSetopQuery as well */
  protected String leftTable;
  
  /** The right query. This could inturn be a CEPSetopQuery as well */
  protected String rightTable;
  
  /** Is it a Union All */
  protected boolean isUnionAll;

  /**
   * Constructor
   * @param setopType the set operator type
   * @param leftTable the left operand
   * @param rightTable the right operand
   */
  public CEPSetopQueryNode(RelSetOp setopType, 
                    CEPStringTokenNode leftToken,
                    CEPStringTokenNode rightToken) {
    this.setopType    = setopType;
    this.leftTable  = leftToken.getValue();
    this.rightTable = rightToken.getValue();
    setStartOffset(leftToken.getStartOffset());
    setEndOffset(rightToken.getEndOffset());
  }
 
  /**
   * Constructor
   * @param setopType the set operator type
   * @param leftTable the left operand
   * @param rightTable the right operand
   * @param isUnionAll Is operator union all or not
   */
  public CEPSetopQueryNode(RelSetOp setopType, 
                    CEPStringTokenNode leftToken,
                    CEPStringTokenNode rightToken,
                    boolean isUnionAll) {
    this.setopType    = setopType;
    this.leftTable  = leftToken.getValue();
    this.rightTable = rightToken.getValue();
    this.isUnionAll = isUnionAll;
    setStartOffset(leftToken.getStartOffset());
    setEndOffset(rightToken.getEndOffset());
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
  public String getLeftTable() {
    return leftTable;
  }

  /**
   * Get the right operand
   * @return the right operand
   */
  public String getRightTable() {
    return rightTable;
  }
  
  /**
   * Get type of Union operation
   * @return true if operation is 'union all'
   */
  public boolean isUnionAll() {
	  return isUnionAll;
  }

  public String toString()
  {
    if(isUnionAll)
      return " " + leftTable + " UNION ALL " + rightTable + " ";
    else
      return " " + leftTable + " " + setopType.toString() + " " + rightTable + " ";
  }
  
  private String createSourceXml()
  {
    StringBuilder myXml = new StringBuilder(30);
    
    String leftSourceName = XMLHelper.buildElement(true, VisXMLHelper.sourceNameTag, leftTable, null, null);
    String leftCqlProperty = VisXMLHelper.createCqlPropertyXml("select * from " + leftTable);
    String rightSourceName = XMLHelper.buildElement(true, VisXMLHelper.sourceNameTag, rightTable, null, null);
    String rightCqlProperty = VisXMLHelper.createCqlPropertyXml("select * from " + rightTable);
    String leftSourceOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.sourceOperator, 1, leftSourceName + "\n" + leftCqlProperty);
    String rightSourceOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.sourceOperator, 2, rightSourceName + " \n" + rightCqlProperty);
    
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
 
    String intersectOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.intersectOperator, 3, myXml.toString());
    return intersectOperator;
  }
  
  private String createMinusXml(int operatorId)
  {

    StringBuilder myXml = new StringBuilder(50);
    
    String inputs = VisXMLHelper.createInputsXml(new int[]{1,2});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(toString());
    
    myXml.append(inputs);
    myXml.append(cqlPropertyXml);
 
    String minusOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.minusOperator, 3, myXml.toString());
    return minusOperator;
  }
  
  private String createUnionXml(int operatorId)
  {

    StringBuilder myXml = new StringBuilder(50);
    
    String inputs = VisXMLHelper.createInputsXml(new int[]{1,2});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(toString());
    
    myXml.append(inputs);
    myXml.append(cqlPropertyXml);
    myXml.append(XMLHelper.buildElement(true, VisXMLHelper.unionAllTag, String.valueOf(isUnionAll), null, null));

    String unionOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.unionOperator, 3, myXml.toString());
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
      throw new UnsupportedOperationException("Round trip NOT supported for this type of query/view");
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
