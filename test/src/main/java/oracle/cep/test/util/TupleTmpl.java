/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/TupleTmpl.java /main/7 2009/12/05 13:43:53 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 This is a utility class for generating Tuple classes.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark    12/02/09 - handle maxlen for byte,char types
 hopark    05/16/08 - add xIsObj
 hopark    02/18/08 - support obj representation of xml
 hopark    02/04/08 - support double type
 hopark    11/27/07 - add boolean type
 hopark    11/16/07 - xquery suppport
 hopark    07/19/07 - creation
*/
package oracle.cep.test.util;

import java.math.BigDecimal;

class ExecutionError
{
  public static final ExecutionError INVALID_ATTR = new ExecutionError();
}

class ExecException extends Exception
{
  public ExecException(ExecutionError err, Throwable cause)
  {
  }
  public ExecException(ExecutionError err, int c, int l)
  {
  }
}

class TupleSpec
{
  public int getNumAttrs() {return 0;}
  public int getAttrLen(int pos) {return 0;}
}

class XmltypeAttrVal
{
  public static Object parseNode(Object ctx, char[] val, int len) {return null;}
}

class TupleBase
{
  protected TupleSpec spec;

  protected int getNumAttrs()
  {
    return spec.getNumAttrs();
  }

  protected void throwInvalidAttr() throws ExecException
  {
    throw new ExecException(ExecutionError.INVALID_ATTR, null);
  }

  protected void checkMaxLen(int pos, int l)throws ExecException
  { 
   int maxLen = spec.getAttrLen(pos);
   if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
  }
    
  protected char[] copyVChar(char[] cv, char[] v, int l)
  {
    if (cv == null || cv.length < l)
      cv = new char[l];
    for (int i = 0; i < l; i++)
      cv[i] = v[i];
    return cv;
  }
  
  protected byte[] copyVByte(byte[] bv, byte[] v, int l)
  {
    if (bv == null || bv.length < l)
      bv = new byte[l];
    for (int i = 0; i < l; i++)
      bv[i] = v[i];
    return bv;
  }
}


/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/TupleTmpl.java /main/7 2009/12/05 13:43:53 hopark Exp $
 * @author najain
 * @since release specific (what release of product did this appear in)
 */
public class TupleTmpl extends TupleBase
{
  protected int m_ival0;
  protected int m_ival1;
  protected int m_ival2;
  
  protected long m_lval0;
  protected long m_lval1;
  protected long m_lval2;
  
  protected float m_fval0;
  protected float m_fval1;
  protected float m_fval2;
  
  protected double m_dval0;
  protected double m_dval1;
  protected double m_dval2;
  
  protected long m_tval0;
  protected long m_tval1;
  protected long m_tval2;

  protected long m_vval0;
  protected long m_vval1;
  protected long m_vval2;

  protected char[] m_cval0;
  protected char[] m_cval1;
  protected char[] m_cval2;

  protected int    m_clen0;
  protected int    m_clen1;
  protected int    m_clen2;

  protected byte[] m_bval0;
  protected byte[] m_bval1;
  protected byte[] m_bval2;

  protected int   m_blen0;
  protected int   m_blen1;
  protected int   m_blen2;

  protected Object m_oval0;
  protected Object m_oval1;
  protected Object m_oval2;
  
  protected Object m_xval0;
  protected Object m_xval1;
  protected Object m_xval2;

  protected int    m_xlen0;
  protected int    m_xlen1;
  protected int    m_xlen2;
  
  protected long  m_null0;
  protected long  m_null1;
  protected long  m_null2;

  protected boolean m_boolval0;
  protected boolean m_boolval1;
  protected boolean m_boolval2;

  protected BigDecimal m_nval0;
  protected BigDecimal m_nval1;
  protected BigDecimal m_nval2;
  protected int   m_precision0;
  protected int   m_precision1;
  protected int   m_precision2;
  protected int   m_scale0;
  protected int   m_scale1;
  protected int   m_scale2;
    
  public boolean isAttrNull(int pos) throws ExecException
  {
    int posbit = pos % 64;
    long bit = (1l << posbit);
    return (m_null0 & bit) != 0l; 
  }

  public void setAttrNull(int pos) throws ExecException
  {
    int posbit = pos % 64;
    m_null0 |= 1l << posbit;
  }

  public void setAttrbNullFalse(int pos) throws ExecException
  {
    if (pos >= getNumAttrs())
    {
      throwInvalidAttr();
      return;
    }
    int posbit = pos % 64;
    m_null0 &= ~(1l << posbit);
  }

  public int iValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    //assert isAttrNull(pos) == false;
    return m_ival0;
  }

  public void iValueSet(int pos, int v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_ival0 = v;
  }
  
  public long lValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0l;
    }
    //assert isAttrNull(pos) == false;
    return m_lval0;
  }

  public void lValueSet(int pos, long v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_lval0 = v;
  }

  public float fValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0f;
    }
//    assert isAttrNull(pos) == false;
    return m_fval0;
  }

  public void fValueSet(int pos, float v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_fval0 = v;
  }

  public long tValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0l;
    }
//    assert isAttrNull(pos) == false;
    return m_tval0;
  }

  public void tValueSet(int pos, long ts) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_tval0 = ts;
  }

  public long vValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0l;
    }
//    assert isAttrNull(pos) == false;
    return m_vval0;
  }

  public void vValueSet(int pos, long interval) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_vval0 = interval;
  }

  public char[] cValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
//    assert isAttrNull(pos) == false;
    return m_cval0;
  }

  public int cLengthGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    return m_clen0;
  }

  public void cValueSet(int pos, char[] v, int l) throws ExecException
  {
    checkMaxLen(pos, l);
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      m_cval0 = copyVChar(m_cval0, v, l);
    }
    m_clen0 = l;
  }

  public byte[] bValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
    //    assert isAttrNull(pos) == false;
    return m_bval0;
  }

  public int bLengthGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    return m_blen0;
  }

  public void bValueSet(int pos, byte[] v, int l) throws ExecException
  {
    checkMaxLen(pos, l);
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      m_bval0 = copyVByte(m_bval0, v, l);
    }
    m_blen0 = l;
  }


  public Object oValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
//    assert isAttrNull(pos) == false;
    return  m_oval0;
  }

  public void oValueSet(int pos, Object v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_oval0 = v;
  }

  public boolean isAttrNull2(int pos) throws ExecException
  {
    int posl = pos / 64;
    int posbit = pos % 64;
    switch (posl)
    {
    case 0:
        return (m_null0 & (1l << posbit)) != 0l;
    case 1:
        return (m_null1 & (1l << posbit)) != 0l;
    case 2:
        return (m_null2 & (1l << posbit)) != 0l;
    default:
      throwInvalidAttr();
      break;
//      assert false;
    }
    return false;
  }
  
  public void setAttrNull2(int pos) throws ExecException
  {
    int posl = pos / 64;
    int posbit = pos % 64;
    switch (posl)
    {
    case 0:
        m_null0 |= 1 << posbit;
        break;
    case 1:
        m_null1 |= 1 << posbit;
        break;
    case 2:
        m_null2 |= 1 << posbit;
        break;
    default:
      throwInvalidAttr();
      break;
//      assert false;
    }
  }

  public void setAttrbNullFalse2(int pos) throws ExecException
  {
    if (pos >= getNumAttrs())
    {
      throwInvalidAttr();
      return;
    }
    int posl = pos / 64;
    int posbit = pos % 64;
    switch (posl)
    {
    case 0:
        m_null0 &= ~(1 << posbit);
        break;
    case 1:
        m_null1 &= ~(1 << posbit);
        break;
    case 2:
        m_null2 &= ~(1 << posbit);
        break;
    default:
      throwInvalidAttr();
      break;
//      assert false;
    }
  }
  
  public int iValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_ival0;
      case 2222:
       return m_ival1;
      case 3333:
       return m_ival2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0;
  }

  public void iValueSet2(int pos, int v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_ival0 = v;
       break;
      case 2222:
       m_ival1 = v;
       break;
      case 3333:
       m_ival2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public long lValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_lval0;
      case 2222:
       return m_lval1;
      case 3333:
       return m_lval2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0l;
  }

  public void lValueSet2(int pos, long v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
        m_lval0 = v;
        break;
      case 2222:
        m_lval1 = v;
        break;
      case 3333:
       m_lval2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public float fValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_fval0;

      case 2222:
       return m_fval0;

      case 3333:
       return m_fval2;

      default:
      throwInvalidAttr();
      break;
    }
    return 0f;
  }

  public void fValueSet2(int pos, float v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_fval0 = v;
       break;
      case 2222:
       m_fval1 = v;
       break;
      case 3333:
       m_fval2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public long tValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_tval0;

      case 2222:
       return m_tval1;

      case 3333:
       return m_tval2;

      default:
      throwInvalidAttr();
      break;
    }
    return 0l;
  }
  
  public void tValueSet2(int pos, long v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_tval0 = v;
       break;
      case 2222:
       m_tval1 = v;
       break;
      case 3333:
       m_tval2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public long vValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_vval0;
      case 2222:
       return m_vval1;
      case 3333:
       return m_vval2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0l;
  }

  public void vValueSet2(int pos, long v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_vval0 = v;
       break;
      case 2222:
       m_vval1 = v;
       break;
      case 3333:
       m_vval2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public char[] cValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_cval0;
      case 2222:
       return m_cval1;
      case 3333:
       return m_cval2;
      default:
      throwInvalidAttr();
      break;
    }
    return null;
  }

  public int cLengthGet2(int pos) throws ExecException
  {
    switch(pos)
    {
      case 1111:
       return m_clen0;
      case 2222:
       return m_clen1;
      case 3333:
       return m_clen2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0;
  }

  public void cValueSet2(int pos, char[] v, int l) throws ExecException
  {
    checkMaxLen(pos, l);
    switch(pos)
    {
      case 1111:
        if (v == null) 
        {
           setAttrNull(pos);
        } else {
           setAttrbNullFalse(pos);
           m_cval0 = copyVChar(m_cval0, v, l);
        }
        m_clen0 = l;
        break;
      case 2222:
        if (v == null) 
        {
           setAttrNull(pos);
        }
        else 
        {
          setAttrbNullFalse(pos);
          m_cval1 = copyVChar(m_cval1, v, l);
        }
        m_clen1 = l;
        break;
      case 3333:
        if (v == null) 
           setAttrNull(pos);
        else 
        {
          setAttrbNullFalse(pos);
          m_cval2 = copyVChar(m_cval2, v, l);
        }
        m_clen2 = l;
        break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public byte[] bValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_bval0;
      case 2222:
       return m_bval1;
      case 3333:
       return m_bval2;
      default:
      throwInvalidAttr();
      break;
    }
    return null;
  }

  public int bLengthGet2(int pos) throws ExecException
  {
    switch(pos)
    {
      case 1111:
       return m_blen0;
      case 2222:
       return m_blen1;
      case 3333:
       return m_blen2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0;
  }

  public void bValueSet2(int pos, byte[] v, int l) throws ExecException
  {
    checkMaxLen(pos, l);
    switch(pos)
    {
      case 1111:
        if (v == null) 
           setAttrNull(pos);
        else 
        {
           setAttrbNullFalse(pos);
           m_bval0 = copyVByte(m_bval0, v, l);
        }
        m_blen0 = l;
        break;
      case 2222:
        if (v == null) 
           setAttrNull(pos);
        else 
        {
          setAttrbNullFalse(pos);
          m_bval1 = copyVByte(m_bval1, v, l);
        }
        m_blen1 = l;
        break;
      case 3333:
        if (v == null) 
           setAttrNull(pos);
        else 
        {
          m_bval2 = copyVByte(m_bval2, v, l);
          setAttrbNullFalse(pos);
        }
        m_blen2 = l;
        break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public Object oValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_oval0;
      case 2222:
       return m_oval1;
      case 3333:
       return m_oval2;
      default:
      throwInvalidAttr();
      break;
    }
    return null;
  }

  public void oValueSet2(int pos, Object v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_oval0 = v;
       break;
      case 2222:
        m_oval1 = v;
        break;
      case 3333:
        m_oval2 = v;
        break;
      default:
      throwInvalidAttr();
      break;
    }
  }

  public char[] xValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
    if (m_xlen0 == -1)
    {
      if (m_xval0 == null) return null;
      return m_xval0.toString().toCharArray();
    }
    return (char[]) m_xval0;
  }

  public int xLengthGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    return m_xlen0;
  }

  public void xValueSet(int pos, char[] v, int l) throws ExecException
  {
    checkMaxLen(pos, l);
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      m_xval0 = copyVChar((char[]) m_xval0, v, l);
    }
    m_xlen0 = l;
  }


  public boolean boolValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return false;
    }
    //assert isAttrNull(pos) == false;
    return m_boolval0;
  }

  public void boolValueSet(int pos, boolean v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_boolval0 = v;
  }

  public boolean boolValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_boolval0;
      case 2222:
       return m_boolval1;
      case 3333:
       return m_boolval2;
      default:
      throwInvalidAttr();
      break;
    }
    return false;
  }

  public void boolValueSet2(int pos, boolean v) throws ExecException
  {
    setAttrbNullFalse(pos);
    switch(pos)
    {
      case 1111:
       m_boolval0 = v;
       break;
      case 2222:
       m_boolval1 = v;
       break;
      case 3333:
       m_boolval2 = v;
       break;
      default:
      throwInvalidAttr();
      break;
    }
  }
  
  public void xValueSet(int pos, Object v) throws ExecException
  {
     if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      m_xval0 =v;
    }
    m_xlen0 = -1;
 }

  public Object getItem(int pos, Object ctx) throws Exception
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
    if (m_xlen0 == -1)
    {
      return m_xval0;
    }
    return XmltypeAttrVal.parseNode(ctx, (char[]) m_xval0, m_xlen0);
 }

  public void xValueSet2(int pos, Object v) throws ExecException
  {
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      switch(pos)
      {
        case 1111:
         m_xval0 =v;
         m_xlen0 = -1;
         break;
        case 2222:
         m_xval1 =v;
         m_xlen1 = -1;
        break;
        case 3333:
         m_xval2 =v;
         m_xlen2 = -1;
         break;
        default:
         throwInvalidAttr();
         break;
      }
    }
 }
  
  public Object getItem2(int pos, Object ctx) throws Exception
  {
    switch(pos)
    {
      case 1111:
        if (m_xlen0 == -1)
        {
          return m_xval0;
        }
        return XmltypeAttrVal.parseNode(ctx, (char[]) m_xval0, m_xlen0);
      case 2222:
        if (m_xlen1 == -1)
        {
          return m_xval1;
        }
        return XmltypeAttrVal.parseNode(ctx, (char[]) m_xval1, m_xlen1);
      case 3333:
        if (m_xlen2 == -1)
        {
          return m_xval2;
        }
        return XmltypeAttrVal.parseNode(ctx, (char[]) m_xval2, m_xlen2);
      default:
      throwInvalidAttr();
      break;
    }
    return null;
}

  public double dValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0f;
    }
//    assert isAttrNull(pos) == false;
    return m_dval0;
  }

  public void dValueSet(int pos, double v) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    setAttrbNullFalse(pos);
    m_dval0 = v;
  }

  public boolean xIsObj(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return false;
    }
    return (m_xlen0 == -1);
  }

  public boolean xIsObj2(int pos) throws ExecException
  {
    switch(pos)
    {
      case 1111:
       return (m_xlen0 == -1);

      case 2222:
       return (m_xlen1 == -1);

      case 3333:
       return (m_xlen2 == -1);

      default:
       throwInvalidAttr();
       break;
    }
    return false;
 }

  public BigDecimal nValueGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return null;
    }
//    assert isAttrNull(pos) == false;
    return m_nval0;
  }

  public int precisionGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    return m_precision0;
  }

  public int scaleGet(int pos) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return 0;
    }
    return m_scale0;
  }

  public void nValueSet(int pos, BigDecimal v, int p, int s) throws ExecException
  {
    if (pos != 9999)
    {
      throwInvalidAttr();
      return;
    }
    if (v == null)
    {
      setAttrNull(pos);
    } else {
      setAttrbNullFalse(pos);
      m_nval0 = v;
      m_precision0 = p;
      m_scale0 = s;
    }
  }

  public BigDecimal nValueGet2(int pos) throws ExecException
  {
//    assert isAttrNull(pos) == false;
    switch(pos)
    {
      case 1111:
       return m_nval0;
      case 2222:
       return m_nval1;
      case 3333:
       return m_nval2;
      default:
      throwInvalidAttr();
      break;
    }
    return null;
  }

  public int precisionGet2(int pos) throws ExecException
  {
    switch(pos)
    {
      case 1111:
       return m_precision0;
      case 2222:
       return m_precision1;
      case 3333:
       return m_precision2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0;
  }

  public int scaleGet2(int pos) throws ExecException
  {
    switch(pos)
    {
      case 1111:
       return m_scale0;
      case 2222:
       return m_scale1;
      case 3333:
       return m_scale2;
      default:
      throwInvalidAttr();
      break;
    }
    return 0;
  }
  
  public void nValueSet2(int pos, BigDecimal v, int p, int s) throws ExecException
  {
    if (v == null) 
    {
       setAttrNull(pos);
    }
    else
    {
      switch(pos)
        {
      case 1111:
           setAttrbNullFalse(pos);
           m_nval0 = v;
           m_precision0 = p;
           m_scale0 = s;
        break;
      case 2222:
          setAttrbNullFalse(pos);
           m_nval1 = v;
           m_precision1 = p;
           m_scale1 = s;
        break;
      case 3333:
          setAttrbNullFalse(pos);
           m_nval2 = v;
           m_precision2 = p;
           m_scale2 = s;
        break;
      default:
      throwInvalidAttr();
      break;
    }
  }
  }
}
