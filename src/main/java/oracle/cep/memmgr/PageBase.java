/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageBase.java /main/13 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    hopark      10/24/10 - fix no of attrib limit
    hopark      12/02/09 - check MaxLen on char,byte types
    hopark      10/23/09 - support number format
    hopark      10/10/08 - remove statics
    hopark      03/26/08 - server reorg
    hopark      03/11/08 - do not parse if ctx is null
    hopark      02/27/08 - fix serialization
    hopark      02/09/08 - object representation of xml
    hopark      02/04/08 - support double type
    hopark      11/08/07 - fix bitset
    hopark      11/27/07 - add boolean type
    najain      11/15/07 - xquery support
    hopark      10/24/07 - getOffset optimization
    hopark      10/05/07 - set NameSpace
    hopark      10/03/07 - add Externalizable
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageBase.java /main/12 2010/10/27 23:23:49 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import java.math.BigDecimal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;

import org.xml.sax.SAXException;

import oracle.cep.dataStructures.internal.memory.XmltypeAttrVal;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.CEPManager;

public abstract class PageBase 
  implements IPage, Externalizable
{
  int                     m_id;

  protected PageLayout    m_layout;
  protected short         m_noObjs;
  protected short         m_noAttribs;    //number of real attributes
  protected BitSet        m_usage;
  
  protected short[]       m_offsets;      // offset in the entries [index * m_noAttribs + attrpos]
  static EvictStat     s_stat;

  
  static
  {
    IEvictPolicy epm = CEPManager.getInstance().getEvictPolicy();
    s_stat = epm.getStat();
  }
  
  public PageBase()
  {
    super();
    m_id = 0;
    m_layout = null;
    m_noObjs = 0;
    m_usage = null;
  }
  
  public PageBase(int id, int pgSize, PageLayout layout)
  {
    super();
    m_id = id;
    
    m_layout = layout;
    
    m_noObjs = layout.getNoObjs();
    m_usage = new BitSet(m_noObjs);
    m_offsets = m_layout.getOffsets();
    m_noAttribs = m_layout.getNoRealAttribs();
  }
  
  public int getPageId() {return m_id;}
  
  public int getNoObjs()
  {
    return m_noObjs;
  }
  
  public short getNoTypes() 
  {
    return m_layout.getNoAttribs();
  }
  
  public byte[] getTypes()
  {
    return m_layout.getTypes();
  }
  
  public PageLayout getPageLayout() {return m_layout;}
  
  protected void checkMaxLen(int pos, int l) throws ExecException
  { 
   int maxLen = m_layout.getMaxLen(pos);
   if (l > maxLen)
	   throw new ExecException(ExecutionError.INVALID_ATTR, l, maxLen);
  }
      
  /**
   * Allocate an object in this page.
   * Returns the index of the allocated object.
   * If the page is full, then -1 is returned.
   * 
   * @return allocated index
   */
  public int alloc()
  {
    int obj = m_usage.nextClearBit(0);
    if (obj < 0 || obj >= m_noObjs) return -1;
    m_usage.set(obj);
    return obj;
  }
  
  /**
   * Free the object in the page
   * 
   * @return true if the page is empty
   * @param obj : index of object in the page
   */
  public boolean free(int obj)
  {
    assert (obj >= 0 && obj < m_noObjs);
    m_usage.clear(obj);
    return m_usage.isEmpty();
  }
  
  public boolean isEmpty()
  {
    return (m_usage.isEmpty());
  }
  
  protected abstract int[] getIntAttrs();
  protected abstract long[] getLongAttrs();
  protected abstract float[] getFloatAttrs();
  protected abstract double[] getDoubleAttrs();
  protected abstract long[] getTimeAttrs();
  protected abstract long[] getIntervalAttrs();
  protected abstract long[] getIntervalYMAttrs();
  protected abstract char[][] getCharAttrs();
  protected abstract byte[][] getByteAttrs();
  protected abstract Object[] getObjAttrs();
  protected abstract Object[] getXMLAttrs();
  protected abstract boolean[] getBoolAttrs();
  protected abstract BigDecimal[] getNumberAttrs();

  protected abstract void setIntAttrs(int[] a);
  protected abstract void setLongAttrs(long[] a);
  protected abstract void setFloatAttrs(float[] a);
  protected abstract void setDoubleAttrs(double[] a);
  protected abstract void setTimeAttrs(long[] a);
  protected abstract void setIntervalAttrs(long[] a);
  protected abstract void setIntervalYMAttrs(long[] a);
  protected abstract void setCharAttrs(char[][] a);
  protected abstract void setByteAttrs(byte[][] a);
  protected abstract void setObjAttrs(Object[] a);
  protected abstract void setXMLAttrs(Object[] a);
  protected abstract void setBoolAttrs(boolean[] a);
  protected abstract void setNumberAttrs(BigDecimal[] a);
  
  protected void writeIntAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getIntAttrs());
  }
  
  protected void writeLongAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getLongAttrs());
  }
  
  protected void writeFloatAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getFloatAttrs());
  }
  
  protected void writeDoubleAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getDoubleAttrs());
  }
  
  protected void writeTimeAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getTimeAttrs());
  }
  
  protected void writeIntervalAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getIntervalAttrs());
  }
  
  protected void writeIntervalYMAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getIntervalYMAttrs());
  }
  
  protected void writeCharAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getCharAttrs());
  }

  protected void writeXmlAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getXMLAttrs());
  }
  
  protected void writeByteAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getByteAttrs());
  }
  
  protected void writeObjectAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getObjAttrs());
  }
  
  protected void writeBoolAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getBoolAttrs());
  }

  protected void writeNumberAttrs(ObjectOutput stream) throws IOException
  {
    stream.writeObject(getNumberAttrs());
  }

  protected void readIntAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    int[] attrs = (int[]) stream.readObject();
    setIntAttrs(attrs);
  }
  
  protected void readLongAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    long[] attrs = (long[]) stream.readObject();
    setLongAttrs(attrs);
  }
  
  protected void readFloatAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    float[] attrs = (float[]) stream.readObject();
    setFloatAttrs(attrs);
  }
  
  protected void readDoubleAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    double[] attrs = (double[]) stream.readObject();
    setDoubleAttrs(attrs);
  }

  protected void readTimeAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    long[] attrs = (long[]) stream.readObject();
    setTimeAttrs(attrs);
  }
  
  protected void readIntervalAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    long[] attrs = (long[]) stream.readObject();
    setIntervalAttrs(attrs);
  }
  
  protected void readIntervalYMAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    long[] attrs = (long[]) stream.readObject();
    setIntervalYMAttrs(attrs);
  }
  
  protected void readCharAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    char[][] attrs = (char[][]) stream.readObject();
    setCharAttrs(attrs);
  }

  protected void readXmlAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    Object[] attrs = (Object[]) stream.readObject();
    setXMLAttrs(attrs);
  }
  
  protected void readByteAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    byte[][] attrs = (byte[][]) stream.readObject();
    setByteAttrs(attrs);
  }
  
  protected void readObjectAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    Object[] attrs = (Object[]) stream.readObject();
    setObjAttrs(attrs);
  }

  protected void readBoolAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    boolean[] attrs = (boolean[]) stream.readObject();
    setBoolAttrs(attrs);
  }

  protected void readNumberAttrs(ObjectInput stream) throws IOException, ClassNotFoundException
  {
    BigDecimal[] attrs = (BigDecimal[]) stream.readObject();
    setNumberAttrs(attrs);
  }

  public synchronized void writeExternal(ObjectOutput stream)
    throws IOException
  {
    int layoutId = m_layout.getId();
    stream.writeInt(layoutId);
    stream.writeInt(m_id);
    stream.writeObject(m_usage);
    short[] typeUsages = m_layout.getTypeUsages();
    for (byte type = 0; type < typeUsages.length; type++)
    {
      short t = typeUsages[type];
      if (t <= 0)
        continue;
      switch(type) 
      {
      case PageLayout.INT:        writeIntAttrs(stream); break;
      case PageLayout.LONG:       writeLongAttrs(stream); break;
      case PageLayout.FLOAT:      writeFloatAttrs(stream); break;
      case PageLayout.DOUBLE:     writeDoubleAttrs(stream); break;
      case PageLayout.TIME:       writeTimeAttrs(stream); break;
      case PageLayout.INTERVAL:   writeIntervalAttrs(stream); break;
      case PageLayout.INTERVALYM: writeIntervalYMAttrs(stream); break;
      case PageLayout.VCHAR:      writeCharAttrs(stream); break;
      case PageLayout.VBYTE:      writeByteAttrs(stream); break;
      case PageLayout.OBJ:        writeObjectAttrs(stream); break;
      case PageLayout.XML:        writeXmlAttrs(stream);  break;
      case PageLayout.BOOLEAN:    writeBoolAttrs(stream); break;
      case PageLayout.BIGDECIMAL: writeNumberAttrs(stream); break;
      }
    }
    
  }

  @SuppressWarnings("unchecked")
  public synchronized void readExternal(ObjectInput stream) 
    throws IOException
  {
    try
    {
      int layoutId = stream.readInt();
      FactoryManager factoryMgr = CEPManager.getInstance().getFactoryManager();
      PagedFactory fac = (PagedFactory) factoryMgr.get(layoutId);
      m_layout = fac.getPageLayout();
      m_id = stream.readInt();
      m_noObjs = m_layout.getNoObjs();
      m_offsets = m_layout.getOffsets();
      m_noAttribs = m_layout.getNoRealAttribs();
      m_usage = (BitSet) stream.readObject();

      short[] typeUsages = m_layout.getTypeUsages();
      for (byte type = 0; type < typeUsages.length; type++)
      {
        short t = typeUsages[type];
        if (t <= 0)
          continue;
        switch(type) 
        {
        case PageLayout.INT:        readIntAttrs(stream); break;
        case PageLayout.LONG:       readLongAttrs(stream); break;
        case PageLayout.FLOAT:      readFloatAttrs(stream); break;
        case PageLayout.DOUBLE:     readDoubleAttrs(stream); break;
        case PageLayout.TIME:       readTimeAttrs(stream); break;
        case PageLayout.INTERVAL:   readIntervalAttrs(stream); break;
        case PageLayout.INTERVALYM: readIntervalYMAttrs(stream); break;
        case PageLayout.VCHAR:      readCharAttrs(stream); break;
        case PageLayout.VBYTE:      readByteAttrs(stream); break;
        case PageLayout.OBJ:        readObjectAttrs(stream); break;
        case PageLayout.XML:        readXmlAttrs(stream);  break;
        case PageLayout.BOOLEAN:    readBoolAttrs(stream); break;
        case PageLayout.BIGDECIMAL: readNumberAttrs(stream); break;
        }
      }
      
    }
    catch (OptionalDataException oe)
    {
      System.out.println(oe);
    }
    catch (ClassNotFoundException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
  }  
  
  public boolean equals(Object other)
  {
    assert (other instanceof PageBase);
    PageBase o = (PageBase) other;
    assert (m_id == o.m_id);
    assert (m_layout.getId() == o.m_layout.getId());
    assert (m_usage.equals(o.m_usage));
    assert (Arrays.equals(getIntAttrs(), o.getIntAttrs()));
    assert (Arrays.equals(getLongAttrs(), o.getLongAttrs()));
    assert (Arrays.equals(getFloatAttrs(), o.getFloatAttrs()));
    assert (Arrays.equals(getDoubleAttrs(), o.getDoubleAttrs()));
    assert (Arrays.equals(getTimeAttrs(), o.getTimeAttrs()));
    assert (Arrays.equals(getIntervalAttrs(), o.getIntervalAttrs()));
    assert (Arrays.equals(getIntervalYMAttrs(), o.getIntervalYMAttrs()));
    assert (Arrays.equals(getXMLAttrs(), o.getXMLAttrs()));
    assert (Arrays.equals(getBoolAttrs(), o.getBoolAttrs()));
    char[][] ca = getCharAttrs();
    int calen = ca == null ? 0 : ca.length;
    char[][] ca1 = o.getCharAttrs();
    int ca1len = ca1 == null ? 0 : ca1.length;
    assert (calen == ca1len);
    for (int i = 0; i < calen; i++)
      assert (Arrays.equals(ca[i], ca1[i]));
    byte[][] ba = getByteAttrs();
    int balen = ba == null ? 0 : ba.length;
    byte[][] ba1 = o.getByteAttrs();
    int ba1len = ba1 == null ? 0 : ba1.length;
    assert (balen == ba1len);
    for (int i = 0; i < balen; i++)
      assert (Arrays.equals(ba[i], ba1[i]));

    Object[] oa = getObjAttrs();
    Object[] oa1 = o.getObjAttrs();
    int oalen = (oa == null ? 0 : oa.length);
    int oa1len = (oa1 == null ? 0 : oa1.length);
    assert (oalen == oa1len);
    for (int i = 0; i < oalen; i++)
    {
      if (oa[i] == null) assert (oa1[i] == null);
      else assert (oa[i].equals(oa1[i]));
    }
    return true;
  }
  
  public Object parseNode(Object ctx, char[] val, int len)
  throws IOException, SAXException
  {
    if (ctx == null)
      return null;
    return XmltypeAttrVal.parseNode(ctx, val, len);
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("id = ");
    buf.append(m_id);
    buf.append(", noObjs = ");
    buf.append(m_noObjs);
    //buf.append(", usage = ");
   // buf.append(m_usage.toString());
    return buf.toString();
  }
  
}

