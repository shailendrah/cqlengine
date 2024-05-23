/* $Header: ComplexRegexp.java 07-feb-2007.05:57:40 rkomurav Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    Semantic representation of complex regular expression for patterns

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    02/07/07 - Creation
 */

/**
 *  @version $Header: ComplexRegexp.java 07-feb-2007.05:57:40 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.RegexpOp;

public class ComplexRegexp extends Regexp
{
  /** The regexp operator */
  private RegexpOp regexpOp;

  /** The left operand */
  private Regexp leftOperand;

  /** The right operand */
  private Regexp rightOperand;

  /** The unary operand, only applicable in case of a unary regexp operator */
  private Regexp unaryOperand;
  
  /**
   * Constructor in case of a binary regexp operator
   * @param binaryOp the binary regexp operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  ComplexRegexp(RegexpOp binaryOp, Regexp leftOperand,
                Regexp rightOperand) {
    this.regexpOp     = binaryOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    this.unaryOperand = null;
  }

  /**
   * Constructor in case of a unary regexp operator
   * @param regexpOp the regexp operator
   * @param unaryOperand the operand
   */
  ComplexRegexp(RegexpOp unaryOp, Regexp unaryOperand) {
    this.regexpOp     = unaryOp;
    this.unaryOperand = unaryOperand;
    this.leftOperand  = null;
    this.rightOperand = null;
  }

  /**
   * @return Returns the leftOperand.
   */
  public Regexp getLeftOperand() {
    return leftOperand;
  }

  /**
   * @return Returns the regexpOp.
   */
  public RegexpOp getRegexpOp() {
    return regexpOp;
  }

  /**
   * @return Returns the rightOperand.
   */
  public Regexp getRightOperand() {
    return rightOperand;
  }

  /**
   * @return Returns the unaryOperand.
   */
  public Regexp getUnaryOperand() {
    return unaryOperand;
  }
  
}
