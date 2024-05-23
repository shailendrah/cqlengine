/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPNullConstExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    02/18/09 - remove toString
    skmishra    08/21/08 - import, reorg
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - adding getValue
    udeshmuk    01/10/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPNullConstExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
* Parse tree node for NULL literal
*/

public class CEPNullConstExprNode extends CEPConstExprNode {
  
  public CEPNullConstExprNode() {}

  /**
   * @return null!
   */
  public Object getValue(){
    assert false;
    return null;
  }
  
  public String getExpression()
  {
    return "null";
  }
  
  public String toString()
  {
    if(alias != null)
      return " null AS " + alias;
    else
      return " null ";
  }
}