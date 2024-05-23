/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPArithExprNode.java /main/9 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a arith expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    03/16/09 - removing extra () from toString
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/16/09 - adding parens to toString to maintain precedence
    skmishra    02/13/09 - adding alias to toString
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    rkomurav    01/13/07 - cleanup
    anasrini    02/22/06 - add getter methods 
    anasrini    12/21/05 - Parse tree node for an arithmetic expression 
    anasrini    12/21/05 - Parse tree node for an arithmetic expression 
    anasrini    12/21/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPArithExprNode.java /main/8 2009/04/03 18:57:07 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.ArithOp;

/**
 * Parse tree node for a arith expression
 *
 * @since 1.0
 */

public class CEPArithExprNode extends CEPExprNode {

  /** The arith operator */
  protected ArithOp arithOp;

  /** The left operand, only applicable in case of a binary arith operator */
  protected CEPExprNode leftOperand;

  /** The right operand, only applicable in case of binary arith operator */
  protected CEPExprNode rightOperand;

  /**
   * Constructor in case of a binary arith operator
   * @param arithOp the arith operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  public CEPArithExprNode(ArithOp arithOp, CEPExprNode leftOperand,
                   CEPExprNode rightOperand) {
    this.arithOp    = arithOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(rightOperand.getEndOffset());
  }

  // getter methods

  /**
   * Get the arithmetic operator
   * @return the arithmetic operator
   */
  public ArithOp getArithOp() {
    return arithOp;
  }

  /**
   * Get the left operand
   * @return the left operand
   */
  public CEPExprNode getLeftOperand() {
    return leftOperand;
  }

  /**
   * Get the right operand
   * @return the right operand
   */
  public CEPExprNode getRightOperand() {
    return rightOperand;
  }

  public String toString()
  {
    if (myString != null)
    {
      if (alias != null)
        return myString + " as " + alias;
      else
        return myString;

    }
    else
    {
      if (alias == null)
        return leftOperand.toString() + arithOp.getExpression()
            + rightOperand.toString();
      else
        return leftOperand.toString() + arithOp.getExpression()
            + rightOperand.toString() + " AS " + alias;
    }
  }

  public String getExpression()
  {
    if(myString!=null)
      return myString;
    else 
      myString = leftOperand.toString() + " " + arithOp.getExpression() + " " + rightOperand.toString(); 
    return myString;
  }
}
