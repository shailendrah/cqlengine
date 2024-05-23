/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/Page.java /main/12 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    It is only used when some how dynamic page class generation using BCEL
    is not available.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/27/11 - adding support for interval year to month
    hopark      10/24/10 - fix no of attrib limit
    hopark      12/02/09 - fix cValueSet maxlen check
    sborah      06/30/09 - support for bigdecimal
    hopark      10/16/08 - fix xValueGEt
    hopark      05/15/08 - xValueGet for object type
    hopark      02/27/08 - fix page serialization
    hopark      02/09/08 - object representation of xml
    udeshmuk    01/31/08 - support for double datatype
    hopark      11/08/07 - fix serialization
    hopark      11/27/07 - add boolean type
    najain      11/15/07 - xquery support
    hopark      10/24/07 - getOffset optimization
    hopark      10/05/07 - set NameSpace
    hopark      10/03/07 - add Externalizable
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/Page.java /main/11 2010/10/27 23:23:49 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import java.io.Externalizable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.BitSet;

import oracle.cep.common.IntervalFormat;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.util.DebugUtil;

import org.xml.sax.SAXException;

public class Page extends PageBase
  implements IPage, Externalizable
{
  int[]         m_intAttrs;
  long[]        m_longAttrs;
  float[]       m_floatAttrs;
  double[]      m_doubleAttrs;
  BigDecimal[]  m_bigDecimalAttrs;
  long[]        m_timeAttrs;
  long[]        m_intervalAttrs;
  long[]        m_intervalymAttrs;
  char[][]      m_charAttrs;
  Object[]      m_xmlAttrs;
  byte[][]      m_byteAttrs;
  Object[]      m_objAttrs;
  boolean[]     m_boolAttrs;
  
  public Page()
  {
    super();
  }
  
  public Page(int id, int pgSize, PageLayout layout)
  {
    super(id, pgSize, layout);
    
    short[] typeUsages = layout.getTypeUsages(); 
    for (byte type = 0; type < typeUsages.length; type++)
    {
      short t = typeUsages[type];
      if (t <= 0)
        continue;
      int noprims = m_noObjs * t;
      switch(type) 
      {
      case PageLayout.INT:
        m_intAttrs = new int[noprims];
        break;
      case PageLayout.LONG:
        m_longAttrs = new long[noprims];
        break;
      case PageLayout.FLOAT:
        m_floatAttrs = new float[noprims];
        break;
      case PageLayout.DOUBLE:
        m_doubleAttrs = new double[noprims];
        break;
      case PageLayout.BIGDECIMAL:
        m_bigDecimalAttrs = new BigDecimal[noprims];
        break;
      case PageLayout.TIME:
        m_timeAttrs = new long[noprims];
        break;
      case PageLayout.INTERVAL:
        m_intervalAttrs = new long[noprims];
        break;
      case PageLayout.INTERVALYM:
        m_intervalymAttrs = new long[noprims];
        break;
      case PageLayout.VCHAR:
        m_charAttrs = new char[noprims][];
        break;
      case PageLayout.VBYTE:
        m_byteAttrs = new byte[noprims][];
        break;
      case PageLayout.XML:
        m_xmlAttrs = new Object[noprims];
        break;
      case PageLayout.OBJ:
        m_objAttrs = new Object[noprims];
        break;
      case PageLayout.BOOLEAN:
        m_boolAttrs = new boolean[noprims];
      }
    }
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
   * Gets the value of a double attribute
   */
  public double dValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_doubleAttrs[offset];
  }

  /**
   * Sets the value of a double attribute
   */
  public void dValueSet(int obj, int pos, double v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_doubleAttrs[offset] = v;
  }
  
  /**
   * Gets the value of a bigdecimal attribute
   */
  public BigDecimal nValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_bigDecimalAttrs[offset];
  }
  
  /**
   * Set the bigdecimal value of the attribute
   */
  public void nValueSet(int obj, int pos, BigDecimal v, int precision,
                        int scale) throws ExecException
  {
    BigDecimal val  = new BigDecimal(v.toString(), 
                                    new MathContext(precision)
                                    ).setScale(scale, RoundingMode.HALF_UP);
    
     //  The given number should be of the given precision value
    if(val.precision() > precision)
    {
      throw new ExecException(ExecutionError.PRECISION_ERROR, v.toString() , precision);
    }
    
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_bigDecimalAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = precision;
    lpos = m_layout.getLength2Pos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = scale;
  }              
  
  /**
   * Get the precision of the bigdecimal attribute
   */
  public int nPrecisionGet(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
  }
  
  /**
   * Get the precision of the bigdecimal attribute
   */
  public int nScaleGet(int obj, int pos)
  {
    int lpos = m_layout.getLength2Pos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return m_intAttrs[offset];
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
   * Sets the value of the interval attribute
   */ 
   public void vValueSet(int obj, int pos, long v, IntervalFormat format)
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
   * Sets the value of the interval year to month attribute
   */ 
   public void vymValueSet(int obj, int pos, long v, 
                           IntervalFormat format)
   {
     int offset = m_offsets[obj * m_noAttribs + pos];
     m_intervalymAttrs[offset] = v;
   }
   
   /**
    * Gets the interval attribute year to month value in long
    */
   public long vymValueGet(int obj, int pos)
   {
     int offset = m_offsets[obj * m_noAttribs + pos];
     return m_intervalymAttrs[offset];
   }
   
   @Override
   public IntervalFormat vFormatGet()
   {
     return null;
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
  public void cValueSet(int obj, int pos, char[] v, int l) throws ExecException
  {
   int maxLen = m_layout.getMaxLen(pos);
   if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_charAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  }

  /**
   * Returns true if xmltype uses object representation
   */
  public boolean xIsObj(int obj, int pos)
  {
    int lpos = m_layout.getLengthPos(pos);
    int offset = m_offsets[obj * m_noAttribs + lpos];
    return (m_intAttrs[offset] == -1);  
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
    int len = m_intAttrs[loffset];
    if (len == -1)
    {
      // length=-1 means the value is object not char[]
      // convert it to String
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
    int maxLen = m_layout.getMaxLen(pos);
    if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
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
    int maxLen = m_layout.getMaxLen(pos);
    if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_byteAttrs[offset] = v;
    int lpos = m_layout.getLengthPos(pos);
    offset = m_offsets[obj * m_noAttribs + lpos];
    m_intAttrs[offset] = l;
  }

  /**
   * Gets the value of an Object attribute
   */
  @SuppressWarnings("unchecked")
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
   * Gets the value of an boolean attribute
   */
  public boolean boolValueGet(int obj, int pos)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    return m_boolAttrs[offset];
  }
  
  /**
   * Sets the value of an int attribute
   */
  public void boolValueSet(int obj, int pos, boolean v)
  {
    int offset = m_offsets[obj * m_noAttribs + pos];
    m_boolAttrs[offset] = v;
  }
    
  protected  int[] getIntAttrs() {return m_intAttrs;}
  protected  long[] getLongAttrs() {return m_longAttrs;}
  protected  float[] getFloatAttrs() {return m_floatAttrs;}
  protected  double[] getDoubleAttrs() {return m_doubleAttrs;}
  protected  long[] getTimeAttrs() {return m_timeAttrs;}
  protected  long[] getIntervalAttrs() {return m_intervalAttrs;}
  protected  long[] getIntervalYMAttrs() {return m_intervalymAttrs;}
  protected  char[][] getCharAttrs() {return m_charAttrs;}
  protected  byte[][] getByteAttrs() {return m_byteAttrs;}
  protected  Object[] getObjAttrs() {return m_objAttrs;}
  protected  Object[] getXMLAttrs() {return m_xmlAttrs;}
  protected  boolean[] getBoolAttrs() {return m_boolAttrs;}
  protected  BigDecimal[] getNumberAttrs(){return m_bigDecimalAttrs;}

  protected  void setIntAttrs(int[] a) {m_intAttrs = a;}
  protected  void setLongAttrs(long[] a) {m_longAttrs = a;}
  protected  void setFloatAttrs(float[] a) {m_floatAttrs = a;}
  protected  void setDoubleAttrs(double[] a) {m_doubleAttrs = a;}
  protected  void setTimeAttrs(long[] a) {m_timeAttrs = a;}
  protected  void setIntervalAttrs(long[] a) {m_intervalAttrs = a;}
  protected  void setIntervalYMAttrs(long[] a) {m_intervalymAttrs = a;}
  protected  void setCharAttrs(char[][] a) {m_charAttrs = a;}
  protected  void setByteAttrs(byte[][] a) {m_byteAttrs = a;}
  protected  void setObjAttrs(Object[] a) {m_objAttrs = a;}
  protected  void setXMLAttrs(Object[] a) {m_xmlAttrs = a;}
  protected  void setBoolAttrs(boolean[] a) {m_boolAttrs = a;}
  protected  void setNumberAttrs(BigDecimal[] a) {m_bigDecimalAttrs = a;}

  public void copy(PageBase pg)
  {
    if (DebugUtil.DEBUG_PAGE_SERIALIZATION)
    {

    int[] iattrs = pg.getIntAttrs();
    if (iattrs != null)
    {
      m_intAttrs = new int[iattrs.length];
      System.arraycopy(iattrs, 0, m_intAttrs, 0, iattrs.length);
    }
    long[] lattrs = pg.getLongAttrs();
    if (lattrs != null)
    {
      m_longAttrs = new long[lattrs.length];
      System.arraycopy(lattrs, 0, m_longAttrs, 0, lattrs.length);
    }
    float[] fattrs = pg.getFloatAttrs();
    if (fattrs != null)
    {
      m_floatAttrs = new float[fattrs.length];
      System.arraycopy(fattrs, 0, m_floatAttrs, 0, fattrs.length);
    }
    long[] tattrs = pg.getTimeAttrs();
    if (tattrs != null)
    {
      m_timeAttrs = new long[tattrs.length];
      System.arraycopy(tattrs, 0, m_timeAttrs, 0, tattrs.length);
    }
    long[] vattrs = pg.getIntervalAttrs();
    if (vattrs != null)
    {
      m_intervalAttrs = new long[vattrs.length];
      System.arraycopy(vattrs, 0, m_intervalAttrs, 0, vattrs.length);
    }
    long[] vymattrs = pg.getIntervalYMAttrs();
    if (vymattrs != null)
    {
      m_intervalymAttrs = new long[vymattrs.length];
      System.arraycopy(vattrs, 0, m_intervalymAttrs, 0, vymattrs.length);
    }
    char[][] cattrs = pg.getCharAttrs();
    if (cattrs != null)
    {
      int calen = cattrs.length;
      m_charAttrs = new char[calen][];
      for (int i = 0; i < calen; i++)
      {
        char[] cas = cattrs[i];
        char[] ca = null;
        if (cas != null)
        {
          ca = new char[cas.length];
          System.arraycopy(cas, 0, ca, 0, cas.length);
        }
        m_charAttrs[i] = ca;
      }
      
    }
    byte[][] battrs = pg.getByteAttrs();
    if (battrs != null)
    {
      int calen = battrs.length;
      m_byteAttrs = new byte[calen][];
      for (int i = 0; i < calen; i++)
      {
        byte[] cas = battrs[i];
        byte[] ca = null;
        if (cas != null)
        {
          ca = new byte[cas.length];
          System.arraycopy(cas, 0, ca, 0, cas.length);
        }
        m_byteAttrs[i] = ca;
      }
    }
    Object[] oattrs = pg.getObjAttrs();
    if (oattrs != null)
    {
      m_objAttrs = new Object[oattrs.length];
      System.arraycopy(oattrs, 0, m_objAttrs, 0, oattrs.length);
    }
    Object[] xattrs = pg.getXMLAttrs();
    if (xattrs != null)
    {
      m_xmlAttrs = new Object[xattrs.length];
      System.arraycopy(xattrs, 0, m_xmlAttrs, 0, xattrs.length);
    }
    boolean[] boolattrs = pg.getBoolAttrs();
    if (boolattrs != null)
    {
      m_boolAttrs = new boolean[boolattrs.length];
      System.arraycopy(boolattrs, 0, m_boolAttrs, 0, boolattrs.length);
    }
    BigDecimal[] numberattrs = pg.getNumberAttrs();
    if (numberattrs != null)
    {
      m_bigDecimalAttrs = new BigDecimal[numberattrs.length];
      System.arraycopy(numberattrs, 0, m_bigDecimalAttrs, 0, numberattrs.length);
    }
    
    m_usage = new BitSet();
    m_usage.or(pg.m_usage);

    }
  }

}

