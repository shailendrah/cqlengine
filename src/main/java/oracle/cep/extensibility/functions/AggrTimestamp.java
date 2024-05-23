/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrTimestamp.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    udeshmuk    10/17/07 - Creation
 */

/**
 *  @version $Header: AggrTimestamp.java 31-mar-2008.02:23:12 rkomurav Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrTimestamp extends AggrValue
{
  /** Timestamp value */
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
    AggrTimestamp aggr = new AggrTimestamp();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrTimestamp;
    ((AggrTimestamp)(aValue)).setValue(this.value);
  }

  @Override
  public boolean isNumeric()
  {
    return false;
  }

  @Override
  public boolean isConvertibleToNumeric()
  {
    return false;
  }
}