/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPFloatConstExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $ */

/* Copyright (c) 2005, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/21/08 - import, reorg
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - returning Object in getValue()
    sbishnoi    06/19/07 - Make constructor public
    anasrini    02/23/06 - add getter methods 
    anasrini    12/22/05 - parse tree node for a float constant 
    anasrini    12/22/05 - parse tree node for a float constant 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPFloatConstExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a float constant 
 */

public class CEPFloatConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private float value;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPFloatConstExprNode(double value) {
    this.value = (new Double(value)).floatValue();
  }

  /**
   * Get the float constant value
   * @return the float constant value
   */
  public Float getValue() {
    return new Float(value);
  }
  
  public String getExpression()
  {
    return Float.toString(value);
  }
  
  public String toString()
  {
    if(alias != null)
      return Float.toString(value) + " as " + alias;
    else
      return Float.toString(value);
  }
}
