/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrFloat.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Aggregate Float type

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
 *  @version $Header: AggrFloat.java 31-mar-2008.02:23:06 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrFloat extends AggrValue
{
  /** float value */
  float value;

  /**
   * @return Returns the value.
   */
  public Float getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    assert value instanceof Float;
    this.value = (Float)value;
  }
  
  public AggrValue clone() {
    AggrFloat aggr = new AggrFloat();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrFloat;
    ((AggrFloat)(aValue)).setValue(this.value);
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

