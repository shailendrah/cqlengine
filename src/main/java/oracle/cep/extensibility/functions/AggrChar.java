/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/AggrChar.java /main/3 2011/02/07 22:52:13 sbishnoi Exp $ */

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
 *  @version $Header: AggrChar.java 31-mar-2008.02:23:01 rkomurav Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions;

public class AggrChar extends AggrValue
{
  /** Character value */
  char[] value;

  /**
   * @return Returns the value.
   */
  public char[] getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(Object value) {
    if(value instanceof char[])
      this.value = (char[])value;
    else if(value instanceof String)
    {  
      String inpVal = (String)value;
      char[] resVal = new char[((String)inpVal).length()];
      inpVal.getChars(0, inpVal.length(), resVal, 0);
      this.value = resVal;
    }
    else
      assert false;
  }
  
  public AggrValue clone() {
    AggrChar aggr = new AggrChar();
    aggr.setValue(this.value);
    return aggr;
  }

  public void copy(AggrValue aValue) {
    assert aValue instanceof AggrChar;
    ((AggrChar)(aValue)).setValue(this.value);
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