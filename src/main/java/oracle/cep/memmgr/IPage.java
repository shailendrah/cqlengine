/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/IPage.java /main/10 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    sbishnoi    08/27/11 - adding support for interval year to month
    hopark      12/02/09 - handle maxLen in char, byte type
    sborah      06/30/09 - support for bigdecimal
    hopark      05/18/09 - fix boolValueGet api
    hopark      05/16/08 - add xIsObj
    hopark      02/27/08 - fix pge serialization
    hopark      02/09/08 - object representation of xml
    udeshmuk    01/31/08 - support for double data type.
    hopark      11/27/07 - add boolean type
    najain      11/14/07 - xquery support
    hopark      10/24/07 - optimize
    hopark      09/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/IPage.java /main/9 2009/12/05 13:43:53 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import java.io.IOException;
import java.math.BigDecimal;

import oracle.cep.common.IntervalFormat;
import oracle.cep.execution.ExecException;

import org.xml.sax.SAXException;

public interface IPage
{
  int getPageId();
  short getNoTypes(); 
  byte[] getTypes();
  PageLayout getPageLayout();
  
  /**
   * Allocate an object in this page.
   * Returns the index of the allocated object.
   * If the page is full, then -1 is returned.
   * 
   * @return allocated index
   */
  int alloc();
  
  /**
   * Free the object in the page
   * 
   * @param obj : index of object in the page
   */
  boolean free(int obj);
  
  boolean isEmpty();

  /**
   * Gets the value of an int attribute
   */
  int iValueGet(int obj, int pos);
  
  /**
   * Sets the value of an int attribute
   */
  void iValueSet(int obj, int pos, int v);
  
  /**
   * Gets the value of a bigint attribute
   */
  long lValueGet(int obj, int pos);

  /**
   * Sets the value of a bigint attribute
   */
  void lValueSet(int obj, int pos, long v);
  
  /**
   * Gets the value of a float attribute
   */
  float fValueGet(int obj, int pos);

  /**
   * Sets the value of a float attribute
   */
  void fValueSet(int obj, int pos, float v);

  /**
   * Gets the value of a double attribute
   */
  double dValueGet(int obj, int pos);
  
  /**
   * Sets the value of a double attribute
   */
  void dValueSet(int obj, int pos, double v);
    
  /**
   * Gets the value of a bigdecimal attribute
   */
  BigDecimal nValueGet(int obj, int pos);
  
  /**
   * Gets the precision of a bigdecimal attribute
   */
  int nPrecisionGet(int obj, int pos); 
    
  /**
   * Gets the scale of a bigdecimal attribute
   */
  int nScaleGet(int obj, int pos); 
  
  /**
   * Sets the value of a bigdecimal attribute
   */
  void nValueSet(int obj, int pos, BigDecimal v, int precision, int scale)
  throws ExecException;
  
  
  /**
   * Returns the timestamp value of the attribute
   */
  long tValueGet(int obj, int pos);

  /**
   * Sets the value of the TimeStamp attribute
   */
  void tValueSet(int obj, int pos, long v);

 /**
  * Sets the value of the interval day to second attribute
  */ 
  void vValueSet(int obj, int pos, long v, IntervalFormat format);
  
  /**
   * Gets the interval day to second attribute value in long
   */
  long vValueGet(int obj, int pos);
  
  /**
   * Sets the value of the interval year to month attribute
   */ 
  void vymValueSet(int obj, int pos, long v, IntervalFormat format);
     
  /**
   * Gets the interval year to month attribute value in long
   */
  long vymValueGet(int obj, int pos);
  
  /**
   * Get the interval value format
   */
  IntervalFormat vFormatGet();
  
  /**
   * Gets the value of an char attribute
   */
  char[] cValueGet(int obj, int pos);

  /**
   * Gets the length of a char attribute
   */
  int cLengthGet(int obj, int pos);
 
  /**
   * Sets the length of the char attribute
   */
  void cLengthSet(int obj, int pos, int l);
  
  /**
   * Sets the value of an char attribute
   */
  void cValueSet(int obj, int pos, char[] v, int l) throws ExecException;

  /**
   * Returns true if xmltype uses object representation
   */
  boolean xIsObj(int obj, int pos);
  
  /**
   * Gets the length of a xmltype attribute
   */
  int xLengthGet(int obj, int pos);

  /**
   * Gets the value of an xmltype attribute
   */
  char[] xValueGet(int obj, int pos);

  void xLengthSet(int obj, int pos, int l);
  
  /**
   * Sets the value of an xmltype attribute
   */
  void xValueSet(int obj, int pos, char[] v, int l) throws ExecException;

  /**
   * Gets the value of an xmltype attribute
   */
  Object xObjValueGet(int obj, int pos, Object ctx) throws IOException, SAXException;
  
  /**
   * Sets the value of an xmltype attribute
   */
  void xObjValueSet(int obj, int pos, Object v);
  
  /**
   * Gets the value of an byte attribute
   */
  byte[] bValueGet(int obj, int pos);

  /**
   * Gets the length of a byte attribute
   */
  int bLengthGet(int obj, int pos);
  
  void bLengthSet(int obj, int pos, int l);

  /**
   * Sets the value of an byte attribute
   */
  void bValueSet(int obj, int pos, byte[] v, int l) throws ExecException;

  /**
   * Gets the value of an Object attribute
   */
  Object oValueGet(int obj, int pos);

  /**
   * Sets the value of an Object attribute
   */
  void oValueSet(int obj, int pos, Object v);
  
  /**
   * Gets the value of a boolean attribute
   */
  boolean boolValueGet(int obj, int pos);

  /**
   * Sets the value of a boolean attribute
   */
  void boolValueSet(int obj, int pos, boolean v);
}

