/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPBigDecimalConstExprNode.java /main/1 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/24/09 - support for bigdecimal
    sborah      06/24/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPBigDecimalConstExprNode.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.math.BigDecimal;

/**
 * Parse tree node for BigDecimal constant
 */

public class CEPBigDecimalConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private BigDecimal value;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPBigDecimalConstExprNode(BigDecimal value) {
    this.value = value;
  }

  /**
   * Get the BigDecimal constant value
   * @return the BigDecimal constant value
   */
  public BigDecimal getValue() {
    return value;
  }
  
  public String getExpression()
  {
    return value.toString();
  }
  
  public String toString()
  {
    if(alias != null)
    {
      return value.toString() + " as " + alias;
    }
    
    else
    {
      return value.toString();
    }
  }
}

