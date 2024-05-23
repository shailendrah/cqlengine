/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/PageTmpl.java /main/8 2009/12/05 13:43:53 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/09 - handle maxlen for byte,char types
    hopark      05/16/08 - add xIsObj
    hopark      02/10/08 - object representation of xml
    hopark      02/04/08 - support double type
    hopark      12/28/07 - add xmltype
    hopark      11/27/07 - add boolean type
    hopark      11/16/07 - xquery support
    hopark      10/24/07 - getOffset optimization
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/PageTmpl.java /main/8 2009/12/05 13:43:53 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.util;

import oracle.cep.common.IntervalFormat;
import oracle.cep.execution.ExecException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.memmgr.PageBase;
import oracle.cep.memmgr.PageLayout;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.BitSet;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.xml.sax.SAXException;

public class PageTmpl extends PageBase
{
  int[]         m_intAttrs;
  long[]        m_longAttrs;
  float[]       m_floatAttrs;
  long[]        m_timeAttrs;
  long[]        m_intervalAttrs;
  char[][]      m_charAttrs;
  Object[]      m_xmlAttrs;
  byte[][]      m_byteAttrs;
  Object[]      m_objAttrs;
  boolean[]     m_boolAttrs;
  double[]      m_doubleAttrs;
  BigDecimal[]  m_numberAttrs;

  public PageTmpl()
  {
    super();
  }

  public PageTmpl(int id, int pgSize, PageLayout layout)
  {
    super(id, pgSize, layout);
    
    m_intAttrs = new int[111];
    m_longAttrs = new long[222];
    m_floatAttrs = new float[333];
    m_timeAttrs = new long[444];
    m_intervalAttrs = new long[555];
    m_charAttrs = new char[666][];
    m_byteAttrs = new byte[777][];
    m_objAttrs = new Object[888];    
    m_boolAttrs = new boolean[999];    
    m_numberAttrs = new BigDecimal[1111];    
  }

  /**
   * Gets the value of an int attribute
   */
  public int iValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_intAttrs[offset];
  }
  
  /**
   * Sets the value of an int attribute
   */
  public void iValueSet(int obj, int pos, int v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_intAttrs[offset] = v;
  }

  /**
   * Gets the value of a bigint attribute
   */
  public long lValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_longAttrs[offset];
  }

  /**
   * Sets the value of a bigint attribute
   */
  public void lValueSet(int obj, int pos, long v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_longAttrs[offset] = v;
  }
  
  /**
   * Gets the value of a float attribute
   */
  public float fValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_floatAttrs[offset];
  }

  /**
   * Sets the value of a float attribute
   */
  public void fValueSet(int obj, int pos, float v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_floatAttrs[offset] = v;
  }

  
  /**
   * Returns the timestamp value of the attribute
   */
  public long tValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_timeAttrs[offset];
  }

  /**
   * Sets the value of the TimeStamp attribute
   */
  public void tValueSet(int obj, int pos, long v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_timeAttrs[offset] = v;
  }

 /**
  * Sets the value of the interval attribute
  */ 
  public void vValueSet(int obj, int pos, long v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_intervalAttrs[offset] = v;
  }
  
  /**
   * Gets the interval attribute value in long
   */
  public long vValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_intervalAttrs[offset];
  }

  /**
   * Gets the value of an char attribute
   */
  public char[] cValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_charAttrs[offset];
  }

  /**
   * Gets the length of a char attribute
   */
  public int cLengthGet(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  }
 
  public void cLengthSet(int obj, int pos, int l)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  } 
  
  /**
   * Sets the value of an char attribute
   */
  public void cValueSet(int obj, int pos, char[] v, int l)
    throws ExecException
  {
    checkMaxLen(pos, l);
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_charAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  }

  /**
   * Gets the value of an byte attribute
   */
  public byte[] bValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_byteAttrs[offset];
  }

  /**
   * Gets the length of a byte attribute
   */
  public int bLengthGet(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  } 
  
  public void bLengthSet(int obj, int pos, int l)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  } 

  /**
   * Sets the value of an byte attribute
   */
  public void bValueSet(int obj, int pos, byte[] v, int l)
    throws ExecException
  {
    checkMaxLen(pos, l);
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_byteAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  }

  /**
   * Gets the value of an Object attribute
   */
  public Object oValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_objAttrs[offset];
  }

  /**
   * Sets the value of an Object attribute
   */
  public void oValueSet(int obj, int pos, Object v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_objAttrs[offset] = v;
  }
  
  /**
   * Gets the length of a xmltype attribute
   */
  public int xLengthGet(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  }

  public void xLengthSet(int obj, int pos, int l)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  } 

  /**
   * Gets the value of an xmltype attribute
   */
  public char[] xValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    int lpos = m_layout.getLengthPos(pos);
    int loffset = m_offsets[obj * m_noAttribs + lpos];
    if (m_intAttrs[loffset] == -1)
    {
      Object o = m_xmlAttrs[offset];
      if (o == null) return null;
      return o.toString().toCharArray();
    }
    return (char[]) m_xmlAttrs[offset];
  }

  /**
   * Sets the value of an xmltype attribute
   */
  public void xValueSet(int obj, int pos, char[] v, int l)
    throws ExecException
  {
    checkMaxLen(pos, l);
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_xmlAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  }

  /**
   * Gets the value of an xmltype attribute
   */
  public Object xObjValueGet(int obj, int pos, Object ctx) 
    throws IOException, SAXException
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    Object v = m_xmlAttrs[offset];
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    int len = m_intAttrs[offset];
    if (len == -1)
    {
      // length=-1 means the value is object not char[]
      return v;
    }
    return parseNode(ctx, (char[]) v, len);
  }
  
  /**
   * Sets the value of an xmltype attribute
   */
  public void xObjValueSet(int obj, int pos, Object v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_xmlAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    // mark the value is object type..
    m_intAttrs[offset] = -1;
  }

  /**
   * Gets the value of an boolean attribute
   */
  public boolean boolValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_boolAttrs[offset];
  }
  
  /**
   * Sets the value of an boolean attribute
   */
  public void boolValueSet(int obj, int pos, boolean v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_boolAttrs[offset] = v;
  }

  /**
   * Gets the value of a float attribute
   */
  public double dValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_doubleAttrs[offset];
  }

  /**
   * Sets the value of a float attribute
   */
  public void dValueSet(int obj, int pos, double v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_doubleAttrs[offset] = v;
  }

  /**
   * Gets the value of an char attribute
   */
  public BigDecimal nValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_numberAttrs[offset];
  }

  /**
   * Gets the length of a char attribute
   */
  public int nPrecisionGet(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  }
 
  /**
   * Gets the length of a char attribute
   */
  public int nScaleGet(int obj, int pos)
  {
    int lpos = m_layout.getLength2Pos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  }
 
  public void nValueSet(int obj, int pos, BigDecimal v, int precision, int scale)
    throws ExecException
  {
    BigDecimal val  = new BigDecimal(v.toString(), 
                                    new MathContext(precision)
                                    ).setScale(scale, RoundingMode.HALF_UP);
    
     //  The given number should be of the given precision value
    if(val.precision() > precision)
    {
      throw new ExecException(ExecutionError.PRECISION_ERROR, new Object[] {v.toString() , new Integer(precision)});
    }
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_numberAttrs[offset] = val;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = precision;
    lpos = m_layout.getLength2Pos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = scale;
  }
    
  protected  int[] getIntAttrs() {return m_intAttrs;}
  protected  long[] getLongAttrs() {return m_longAttrs;}
  protected  float[] getFloatAttrs() {return m_floatAttrs;}
  protected  double[] getDoubleAttrs() {return m_doubleAttrs;}
  protected  long[] getTimeAttrs() {return m_timeAttrs;}
  protected  long[] getIntervalAttrs() {return m_intervalAttrs;}
  protected  char[][] getCharAttrs() {return m_charAttrs;}
  protected  byte[][] getByteAttrs() {return m_byteAttrs;}
  protected  Object[] getObjAttrs() {return m_objAttrs;}
  protected  Object[] getXMLAttrs() {return m_xmlAttrs;}
  protected  boolean[] getBoolAttrs() {return m_boolAttrs;}
  protected  BigDecimal[] getNumberAttrs() {return m_numberAttrs;}

  protected  void setIntAttrs(int[] a) {m_intAttrs = a;}
  protected  void setLongAttrs(long[] a) {m_longAttrs = a;}
  protected  void setFloatAttrs(float[] a) {m_floatAttrs = a;}
  protected  void setDoubleAttrs(double[] a) {m_doubleAttrs = a;}
  protected  void setTimeAttrs(long[] a) {m_timeAttrs = a;}
  protected  void setIntervalAttrs(long[] a) {m_intervalAttrs = a;}
  protected  void setCharAttrs(char[][] a) {m_charAttrs = a;}
  protected  void setByteAttrs(byte[][] a) {m_byteAttrs = a;}
  protected  void setObjAttrs(Object[] a) {m_objAttrs = a;}
  protected  void setXMLAttrs(Object[] a) {m_xmlAttrs = a;}
  protected  void setBoolAttrs(boolean[] a) {m_boolAttrs = a;}
  protected  void setNumberAttrs(BigDecimal[] a) {m_numberAttrs = a;}

  public boolean xIsObj(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return (m_intAttrs[offset] == -1);
  }

@Override
public void vValueSet(int obj, int pos, long v, IntervalFormat format) {
    // TODO Auto-generated method stub
    
}

@Override
public void vymValueSet(int obj, int pos, long v, IntervalFormat format) {
    // TODO Auto-generated method stub
    
}

@Override
public long vymValueGet(int obj, int pos) {
    // TODO Auto-generated method stub
    return 0;
}

@Override
public IntervalFormat vFormatGet() {
    // TODO Auto-generated method stub
    return null;
}

@Override
protected long[] getIntervalYMAttrs() {
    // TODO Auto-generated method stub
    return null;
}

@Override
protected void setIntervalYMAttrs(long[] a) {
    // TODO Auto-generated method stub
    
}
}

