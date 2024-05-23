/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrByte.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

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
    rkomurav    03/31/08 - add clone and copy methods.
    udeshmuk    10/17/07 - Creation
 */

/**
 *  @version $Header: AggrByte.java 31-mar-2008.02:22:57 rkomurav Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrByte extends AggrValue
{
  /** Byte value */
  byte[] value;

  /**
   * @return Returns the value.
   */
  public byte[] getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    if(value instanceof byte[])
      this.value = (byte[])value;
    else if(value instanceof String)
    {
      this.value = ((String)value).getBytes();
    }
    else
      assert false;
  }
  
  public AggrValue clone() {
    AggrByte aggr = new AggrByte();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrByte;
    ((AggrByte)(aValue)).setValue(this.value);
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