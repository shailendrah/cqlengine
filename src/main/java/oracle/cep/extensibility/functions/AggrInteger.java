/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrInteger.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Aggregate Integer type

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
 *  @version $Header: AggrInteger.java 31-mar-2008.02:23:07 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrInteger extends AggrValue
{
  /** Integer value */
  int value;

  /**
   * @return Returns the value.
   */
  public Integer getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    assert value instanceof Integer;
    this.value = (Integer)value;
  }
  
  public AggrValue clone() {
    AggrInteger aggr = new AggrInteger();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrInteger;
    ((AggrInteger)(aValue)).setValue(this.value);
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