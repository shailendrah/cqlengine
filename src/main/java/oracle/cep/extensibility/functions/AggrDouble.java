/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrDouble.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    rkomurav    03/31/08 - add clone and copy methods
    udeshmuk    01/31/08 - Creation
 */

/**
 *  @version $Header: AggrDouble.java 31-mar-2008.02:23:04 rkomurav Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrDouble extends AggrValue
{
  /** double value */
  double value;

  /**
   * @return Returns the value.
   */
  public Double getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    assert value instanceof Double;
    this.value = (Double)value;
  }
  
  public AggrValue clone() {
    AggrDouble aggr = new AggrDouble();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrDouble;
    ((AggrDouble)(aValue)).setValue(this.value);
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
