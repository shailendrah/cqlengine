/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrBigInt.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Aggregate Big Int type

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/06/11 - implementing getValue and isNumeric
    rkomurav    03/31/08 - add clone and copy methods
    rkomurav    01/04/07 - Creation
 */

/**
 *  @version $Header: AggrBigInt.java 31-mar-2008.02:22:53 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrBigInt extends AggrValue {
  
  /** Big Int value */
  long value;

  /**
   * @return Returns the value.
   */
  public Long getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    assert value instanceof Long;
    this.value = (Long)value;
  }

  public AggrValue clone() {
    AggrBigInt aggr = new AggrBigInt();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrBigInt;
    ((AggrBigInt)(aValue)).setValue(this.value);
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

