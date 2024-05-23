/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrValue.java /main/4 2011/02/07 22:52:13 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    abstract class for all aggrvalues

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/06/11 - adding getValue and isNumeric
    rkomurav    03/31/08 - add clone and copy methods
    hopark      03/07/07 - add Serializable for Tuple spilling
    rkomurav    01/04/07 - Creation
 */

/**
 *  @version $Header: AggrValue.java 31-mar-2008.02:19:19 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

import java.io.Serializable;

public abstract class AggrValue implements Serializable
{
  /** flag indicating if the AggrValue is null */
  boolean isNull;

  /**
   * @return Returns the isNull.
   */
  public boolean isNull() {
    return isNull;
  }

  /**
   * @param isNull The isNull to set.
   */
  public void setNull(boolean isNull) {
    this.isNull = isNull;
  }
  
  /**
   * clone AggrValue
   * @return Return the cloned Object
   */
  public abstract AggrValue clone();
  
  /**
   * copy this value to the input AggrValue
   * @param aValue Aggrvalue
   */
  public abstract void copy(AggrValue aValue);
  
  /**
   * @returns true if the this is a numeric value
   */
  public abstract boolean isNumeric();
  
  /**
   * @return true if this value can be converted to a numeric value
   */
  public abstract boolean isConvertibleToNumeric();
  
  /**
   * @returns the value wrapped in this object
   */
  public abstract Object getValue();
  
  /**
   * @param value to set in the AggrValue wrapper object
   */
  public abstract void setValue(Object value);
  
}

