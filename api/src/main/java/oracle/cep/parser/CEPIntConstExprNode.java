/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPIntConstExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $ */

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
    skmishra    02/18/09 - add getExpression
    skmishra    08/21/08 - import, reorg
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - changing int to Integer in getValue
    sbishnoi    06/19/07 - Make constructor public
    anasrini    02/23/06 - add getter methods 
    anasrini    12/22/05 - parse tree node for an integer constant 
    anasrini    12/22/05 - parse tree node for an integer constant 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPIntConstExprNode.java /main/7 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for an integer constant 
 */

public class CEPIntConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private int value;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPIntConstExprNode(int value) {
    this.value = value;
  }

  /**
   * Get the integer constant value
   * @return the integer constant value
   */
  public Integer getValue() {
    return new Integer(value);
  }
  
  public String toString()
  {
    if(alias != null)
      return Integer.toString(value) + " AS " + alias;
    else
      return Integer.toString(value);
  }

  @Override
  public String getExpression()
  {
    return Integer.toString(value);
  }
}
