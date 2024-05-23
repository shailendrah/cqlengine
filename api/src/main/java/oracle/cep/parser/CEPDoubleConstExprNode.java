/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPDoubleConstExprNode.java /main/7 2010/03/27 11:20:11 hopark Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
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
    udeshmuk    01/30/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPDoubleConstExprNode.java /main/7 2010/03/27 11:20:11 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.text.DecimalFormat;

/**
 * Parse tree node for double constant
 */

public class CEPDoubleConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private double value;
  private static DecimalFormat formatter = new DecimalFormat("#.#D");
  /**
   * Constructor
   * @param value the constant value
   */
  public CEPDoubleConstExprNode(double value) {
    this.value = value;
  }

  /**
   * Get the double constant value
   * @return the double constant value
   */
  public Double getValue() {
    return new Double(value);
  }
  
  public String getExpression()
  {
    return formatter.format(value);
  }
  
  public String toString()
  {
    if(alias != null)
    {
      return formatter.format(value) + " as " + alias;
    }
    
    else
    {
      return formatter.format(value);
    }
  }
}
