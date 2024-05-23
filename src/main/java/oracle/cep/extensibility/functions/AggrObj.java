/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrObj.java /main/2 2011/02/07 22:52:13 sbishnoi Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/extensibility/functions/AggrObj.java /main/1 2009/02/19 16:44:30 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrObj extends AggrValue
{
  Object value;

  /**
   * @return Returns the value.
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    this.value = value;
  }
  
  public AggrValue clone() {
    AggrObj aggr = new AggrObj();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrObj;
    ((AggrObj)(aValue)).setValue(this.value);
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