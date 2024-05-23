/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrBigDecimal.java /main/2 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/06/11 - implementing getValue and isNumeric
    sborah      06/24/09 - support for bigdecimal
    sborah      06/24/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrBigDecimal.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

import java.math.BigDecimal;

public class AggrBigDecimal extends AggrValue
{

  private static final long serialVersionUID = 5497877228315962151L;
  
  /** BigDecimal value */
  BigDecimal value;

  /**
   * @return Returns the value.
   */
  public BigDecimal getValue() 
  {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) 
  {
    assert value instanceof BigDecimal;
    this.value = (BigDecimal)value;
  }
  
  public AggrValue clone() 
  {
    AggrBigDecimal aggr = new AggrBigDecimal();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) 
  {
    assert aValue instanceof AggrBigDecimal;
    ((AggrBigDecimal)(aValue)).setValue(this.value);
  }

  @Override
  public boolean isNumeric()
  {
    return true;
  }

  @Override
  public boolean isConvertibleToNumeric()
  {
    return true;
  }
}

