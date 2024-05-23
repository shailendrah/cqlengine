/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBigintConstExprNode.java /main/8 2009/04/13 14:26:25 skmishra Exp $ */

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
    skmishra    04/08/09 - string representation contains L
    skmishra    02/18/09 - remove toString
    skmishra    08/21/08 - import, reorg
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - returning Object in getValue()
    sbishnoi    06/19/07 - Make constructor public
    hopark      11/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBigintConstExprNode.java /main/8 2009/04/13 14:26:25 skmishra Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for an integer constant 
 */

public class CEPBigintConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private Object value;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPBigintConstExprNode(Object value) {
    this.value = value;
  }

  /**
   * Get the integer constant value
   * @return the integer constant value
   */
  public Long getValue() {
    Long lv = (Long) value;
    return lv;
  }
  
  public String getExpression()
  { 
    return ((Long)value).toString()+"L";
  }
  
  public String toString()
  {
    if(alias != null)
      return " " + ((Long)value).toString() + "L" + " AS " + alias;
    else
      return " " + ((Long)value).toString() + "L ";
  }
}
