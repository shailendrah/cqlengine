/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPComplexRegexpNode.java /main/11 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to a regular expression with operators

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    04/01/09 - adding myString
    skmishra    02/05/09 - adding toString
    parujain    08/15/08 - error offset
    rkomurav    03/18/08 - change return type of collectcorrnames
    rkomurav    03/02/08 - add getallreferencedCorrAttrs
    rkomurav    02/08/07 - small fix
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPComplexRegexpNode.java /main/10 2009/04/13 14:26:25 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.common.RegexpOp;

/**
 * Parse tree node for a regular expression with operators
 *
 * @since 1.0
 */

public class CEPComplexRegexpNode extends CEPRegexpNode {

  /** The regexp operator */
  protected RegexpOp regexpOp;

  /** The left operand, only applicable in case of a binary regexp operator */
  protected CEPRegexpNode leftOperand;

  /** The right operand, only applicable in case of binary regexp operator */
  protected CEPRegexpNode rightOperand;

  /** The unary operand, only applicable in case of a unary regexp operator */
  protected CEPRegexpNode unaryOperand;
  
  /**
   * Constructor in case of a binary regexp operator
   * @param binaryOp the binary regexp operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  public CEPComplexRegexpNode(RegexpOp binaryOp, CEPRegexpNode leftOperand,
                       CEPRegexpNode rightOperand) {
    this.regexpOp     = binaryOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    this.unaryOperand = null;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(rightOperand.getEndOffset());
  }

  /**
   * Constructor in case of a unary regexp operator
   * @param regexpOp the regexp operator
   * @param unaryOperand the operand
   */
  public CEPComplexRegexpNode(RegexpOp unaryOp, CEPRegexpNode unaryOperand) {
    this.regexpOp     = unaryOp;
    this.unaryOperand = unaryOperand;
    this.leftOperand  = null;
    this.rightOperand = null;
    setStartOffset(unaryOperand.getStartOffset());
    setEndOffset(unaryOperand.getEndOffset());
  }

  // getter methods

  /**
   * Get the regexp operator
   * @return the regexp operator
   */
  public RegexpOp getRegexpOp() {
    return regexpOp;
  }

  /**
   * Get the left operand
   * @return the left operand
   */
  public CEPRegexpNode getLeftOperand() {
    return leftOperand;
  }

  /**
   * Get the right operand
   * @return the right operand
   */
  public CEPRegexpNode getRightOperand() {
    return rightOperand;
  }

  /**
   * Get the unary operand
   * @return the unary operand
   */
  public CEPRegexpNode getUnaryOperand() {
    return unaryOperand;
  }

  public String toString()
  {
    if (myString != null)
      return myString;

    else
    {
      StringBuilder myStringBldr = new StringBuilder(25);
      if (this.unaryOperand != null)
      {
        myStringBldr.append(unaryOperand.toString() + regexpOp.toString());
      }

      else
      {
        myStringBldr.append(leftOperand.toString() + regexpOp.toString()
            + rightOperand.toString() + " ");
      }
      myString = myStringBldr.toString();
    }
    
    return myString;
  }
  
  /**
   * Get all the referenced correlation names
   */
  public boolean getAllReferencedCorrNames(List<String> corrs)
  {
    boolean left;
    boolean right;
    
    if(this.unaryOperand != null)
    {
      return unaryOperand.getAllReferencedCorrNames(corrs);
    }
    else
    {
      left  = leftOperand.getAllReferencedCorrNames(corrs);
      right = rightOperand.getAllReferencedCorrNames(corrs);
      return (left || right);
    }
  }
  
  public String toVisualizerXml()
  {
    if(unaryOperand != null)
      return "\t\t<pattern-attr>" + unaryOperand.toString() + regexpOp.toString() + "</pattern-attr>\n";
    else
      return leftOperand.toVisualizerXml() + "\t\t<pattern-attr>" + rightOperand.toString() + "</pattern-attr>\n";
  }
}
