/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPBaseBooleanExprNode.java /main/13 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
  <short description of component this file declares/defines>

 PRIVATE CLASSES
  <list of private classes defined - with one-line descriptions>

 NOTES
  <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
  hopark      04/21/11 - make constructor public
  skmishra    03/23/09 - bug 8360502
  skmishra    02/20/09 - adding toExprHtml
  mthatte     02/09/09 - toString html compliant
  mthatte     08/21/08 - import, reorg
  parujain    08/15/08 - error offset
  mthatte     04/07/08 - adding toString()
  mthatte     10/03/07 - Making constructor public
  rkomurav    11/08/06 - add outer join support
  parujain    10/31/06 - Base Boolean Node
  parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBaseBooleanExprNode.java /main/12 2009/04/13 14:26:25 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.Collection;

import oracle.cep.common.CompOp;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.OuterJoinType;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a Base boolean expression
 * 
 * @since 1.0
 */

/**
 * The distinction between both the operators is kept in order to avoid the
 * misuse of Comparison opertors and Logical operators. Logical operators shouls
 * always be of complex type
 */

public class CEPBaseBooleanExprNode extends CEPBooleanExprNode {
  /** The Comparison boolean operator */
  protected CompOp compOp;

  /** The Unary boolean operator */
  protected UnaryOp unaryOp;

  /** The join type */
  protected OuterJoinType outerJoinType;

  /** The left operand, only applicable in case of a binary boolean operator */
  protected CEPExprNode leftOperand;

  /** The right operand, only applicable in case of binary boolean operator */
  protected CEPExprNode rightOperand;

  /** The unary operand, only applicable in case of a unary boolean operator */
  protected CEPExprNode unaryOperand;

  /**
   * Constructor in case of a binary boolean operator
   * 
   * @param compOp
   *          the comparison boolean operator
   * @param leftOperand
   *          the left operand
   * @param rightOperand
   *          the right operand
   */
  public CEPBaseBooleanExprNode(CompOp compOp, CEPExprNode leftOperand,
      CEPExprNode rightOperand) {
    this.compOp = compOp;
    this.leftOperand = leftOperand;
    this.rightOperand = rightOperand;
    this.unaryOp = null;
    this.unaryOperand = null;
    this.outerJoinType = null;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(rightOperand.getEndOffset());
  }

  /**
   * Constructor in case of a binary boolean operator with outer JOIN
   * 
   * @param compOp
   *          the comparison boolean operator
   * @param leftOperand
   *          the left operand
   * @param rightOperand
   *          the right operand
   * @param outerJoinType
   *          the type of outer join
   */
  public CEPBaseBooleanExprNode(CompOp compOp, CEPExprNode leftOperand,
      CEPExprNode rightOperand, OuterJoinType outerJoinType) {
    this.compOp = compOp;
    this.leftOperand = leftOperand;
    this.rightOperand = rightOperand;
    this.outerJoinType = outerJoinType;
    this.unaryOp = null;
    this.unaryOperand = null;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(rightOperand.getEndOffset());
  }

  /**
   * Constructor in case of a unary boolean operator
   * 
   * @param unaryOp
   *          the Unary boolean operator
   * @param unaryOperand
   *          the operand
   */
  public CEPBaseBooleanExprNode(UnaryOp unaryOp, CEPExprNode unaryOperand) {
    this.unaryOp = unaryOp;
    this.unaryOperand = unaryOperand;
    this.compOp = null;
    this.leftOperand = null;
    this.rightOperand = null;
    this.outerJoinType = null;
    setStartOffset(unaryOperand.getStartOffset());
    setEndOffset(unaryOperand.getEndOffset());
  }

  // getter methods

  /**
   * Get the comparison binary boolean operator
   * 
   * @return the comparison boolean operator
   */
  public CompOp getCompOp() {
    return compOp;
  }

  /**
   * Get the comparison unary boolean operator
   * 
   * @return the unary boolean operator
   */
  public UnaryOp getUnaryOp() {
    return unaryOp;
  }

  /**
   * Get the outer join type
   * 
   * @return the outer join type
   */
  public OuterJoinType getOuterJoinType() {
    return outerJoinType;
  }

  /**
   * Get the left operand
   * 
   * @return the left operand
   */
  public CEPExprNode getLeftOperand() {
    return leftOperand;
  }

  /**
   * Get the right operand
   * 
   * @return the right operand
   */
  public CEPExprNode getRightOperand() {
    return rightOperand;
  }

  /**
   * Get the Unary operand
   * 
   * @return Unary operand
   */
  public CEPExprNode getUnaryOperand() {
    return unaryOperand;
  }
  
  @Override
  public String getXmlExpression()
  {
    return getExpression(true);
  }
  
  public String getExpression()
  {
    return getExpression(false);
  }
  
  public String getExpression(boolean isXmlFormat)
  {
    if (myString != null)
      return " " + myString + " ";
    else
    {
      if (unaryOp != null)
        myString = " " + unaryOperand.toString() + unaryOp.getSymbol() + " ";

      else if (leftOperand != null && rightOperand != null)
      {
        // could be outer join
        if (outerJoinType != null)
        {
          if (outerJoinType == OuterJoinType.RIGHT_OUTER)
            myString = " " + leftOperand.toString() + "(+)"
                + (isXmlFormat ? compOp.getHtmlExpression():compOp.getSymbol()) + rightOperand.toString() + " ";
          else if (outerJoinType == OuterJoinType.LEFT_OUTER)
            myString = " " + leftOperand.toString() + (isXmlFormat ? compOp.getHtmlExpression():compOp.getSymbol())
                + rightOperand.toString() + "(+) ";
          else if (outerJoinType == OuterJoinType.FULL_OUTER)
            myString = " " + leftOperand.toString() + "(+) "+ (isXmlFormat ? compOp.getHtmlExpression():compOp.getSymbol())
                + rightOperand.toString() + "(+) ";
        }
        // no outer join
        else
          myString = " " + leftOperand.toString() + (isXmlFormat ? compOp.getHtmlExpression():compOp.getSymbol())
              + rightOperand.toString() + " ";
      }
    }
    return myString;
  }
  
  @Override
  public String getAliasOrExpression(Collection<CEPExprNode> other)
  {
    String myString = null;
    if (unaryOp != null)
        myString = " " + unaryOperand.getAliasOrExpression(other) + " "+ unaryOp.getSymbol() + " ";

      else if (leftOperand != null && rightOperand != null)
      {
        // could be outer join
        if (outerJoinType != null)
        {
          if (outerJoinType == OuterJoinType.RIGHT_OUTER)
            myString = " " + leftOperand.getAliasOrExpression(other) + "(+)"
                + compOp.getSymbol() + rightOperand.getAliasOrExpression(other) + " ";
          else if (outerJoinType == OuterJoinType.LEFT_OUTER)
            myString = " " + leftOperand.getAliasOrExpression(other) + compOp.getSymbol()
                + rightOperand.getAliasOrExpression(other) + "(+) ";
          else if(outerJoinType == OuterJoinType.FULL_OUTER)
            myString = " " + leftOperand.getAliasOrExpression(other) + "(+)" + compOp.getSymbol()
            + rightOperand.getAliasOrExpression(other) + "(+) ";
        }
        // no outer join
        else
          myString = " " + leftOperand.getAliasOrExpression(other) + compOp.getSymbol()
              + rightOperand.getAliasOrExpression(other) + " ";
      }
    return myString;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPBaseBooleanExprNode other = (CEPBaseBooleanExprNode) obj;
    if (compOp != other.compOp)
      return false;
    if (leftOperand == null)
    {
      if (other.leftOperand != null)
        return false;
    } 
    else if (!leftOperand.equals(other.leftOperand))
      return false;
    if (outerJoinType != other.outerJoinType)
      return false;
    if (rightOperand == null)
    {
      if (other.rightOperand != null)
        return false;
    } else if (!rightOperand.equals(other.rightOperand))
      return false;
    if (unaryOp != other.unaryOp)
      return false;
    if (unaryOperand == null)
    {
      if (other.unaryOperand != null)
        return false;
    } else if (!unaryOperand.equals(other.unaryOperand))
      return false;
    return true;
  }

  public String toString()
  {
    if (myString != null)
    {
      if (alias != null)
        return " " + myString + " AS " + alias + " ";
      else
        return " " + myString + " ";
    }

    else
    {
      String myString=" ";
      if (unaryOp != null)
        myString = " " + unaryOperand.toString() + unaryOp.getSymbol() + " ";

      else if (leftOperand != null && rightOperand != null)
      {
        // could be outer join
        if (outerJoinType != null)
        {
          if (outerJoinType == OuterJoinType.RIGHT_OUTER)
            myString = " " + leftOperand.toString() + "(+)"
                + compOp.getHtmlExpression() + rightOperand.toString() + " ";
          else if (outerJoinType == OuterJoinType.LEFT_OUTER)
            myString = " " + leftOperand.toString() + compOp.getHtmlExpression()
                + rightOperand.toString() + "(+) ";
          else if (outerJoinType == OuterJoinType.FULL_OUTER)
            myString = " " + leftOperand.toString()+ "(+) " + compOp.getHtmlExpression()
                + rightOperand.toString() + "(+) ";
        }
        // no outer join
        else
          myString = " " + leftOperand.toString() + compOp.getHtmlExpression()
              + rightOperand.toString() + " ";
      }

      if (alias != null)
        myString = " " + myString.concat(" AS " + alias + " ");

      return XMLHelper.toHTMLString(myString);
    }
  }
}
