/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrInterval.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

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
 *  @version $Header: AggrInterval.java 31-mar-2008.02:23:10 rkomurav Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.functions;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class AggrInterval extends AggrValue
{
  /** Interval value */
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
    // In case of certain aggregate functions(variance,standardDeviation), the
    // result value will be Float. This will be converted to Long.
    
    // TODO: Please note that converting float to long can loose the precision.
    // This should be handled when we will enhance INTERVAL to allow interval values larger than Long.MAX_VALUE
    assert value instanceof Long || value instanceof Float;
    if(value instanceof Long)
      this.value = (Long)value;
    else
    {
      this.value = ((Float)value).longValue();
      if(Float.compare((Float)value,((Float)value).longValue()) != 0)
        LogUtil.warning(LoggerType.TRACE, "Loosing Precision while converting numeric value "+
                       value+" to Interval value. Maximum allowed numeric value for interval is Long.MAX_VALUE.");
    }
  }
  
  public AggrValue clone() {
    AggrInterval aggr = new AggrInterval();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrInterval;
    ((AggrInterval)(aValue)).setValue(this.value);
  }

  @Override
  public boolean isNumeric()
  {
    return false;
  }

  @Override
  public boolean isConvertibleToNumeric()
  {
    return true;
  }
}
