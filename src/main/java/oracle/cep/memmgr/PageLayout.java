/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageLayout.java /main/17 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    hopark      12/02/09 - add maxLen in layout
    hopark      10/23/09 - support number type
    sborah      06/30/09 - support for bigdecimal
    hopark      08/22/08 - fix jdk6 warning
    hopark      03/12/08 - move datatype array from tupleptr
    hopark      03/20/08 - fix LinkageError
    hopark      03/05/08 - xml spill
    hopark      02/05/08 - add type toString
    hopark      11/02/07 - add layoutDesc
    hopark      11/27/07 - add BOOLEAN
    najain      11/15/07 - xquery support
    hopark      10/24/07 - optimization
    hopark      10/03/07 - use the factory id
    hopark      07/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/PageLayout.java /main/16 2010/10/27 23:23:49 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class PageLayout
{
  private static final boolean PAGE_GEN_DEBUG = false;

  public static final byte CHAR = 0;
  public static final byte SHORT = 1;
  public static final byte INT = 2;
  public static final byte LONG = 3;
  public static final byte FLOAT = 4;
  public static final byte DOUBLE = 5;
  public static final byte TIME = 6;
  public static final byte INTERVAL = 7;
  public static final byte VCHAR = 8;
  public static final byte VBYTE = 9;
  public static final byte OBJ = 10;
  public static final byte XML = 11;
  public static final byte BOOLEAN = 12;
  public static final byte BIGDECIMAL = 13;
  public static final byte INTERVALYM = 14;
  public static final byte NO_TYPES = 15;
  
  
  private static final int MAX_ATTRIBUTES = Short.MAX_VALUE;     // maximum number of attributes

  LayoutDesc m_desc;
  short     m_noObjs;
  short     m_pageSize;
  short[]   m_offsets;    // stores the offsets (indexed by index * no_attribs + attrib_pos)
  
  short[]    m_typeUsages; // number of usage for each type (indexed by type)
  short     m_size;       // size of element for this page layout
  int       m_id;

  Constructor<?>   m_pageConstructor;
  
  public static short s_sizeof_types[];
  static HashMap<LayoutDesc, PageLayout> s_layoutMap;
  static String s_typeNames[]= {
      "CHR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "TIME",
      "INTERVAL", "CHAR", "BYTE", "OBJ", "XML", "BOOLEAN" , "BIGDECIMAL", "INTERVALYM"};
    
  //Datatype.Kind to PageLayout.Type mapping table
  //The order should be exact..
  private static byte[] s_Datatype_PageLayout =
  {
    PageLayout.INT,     //INT
    PageLayout.LONG, // BIGINT
    PageLayout.FLOAT, // FLOAT
    PageLayout.DOUBLE, // DOUBLE
    PageLayout.VBYTE, // BYTE
    PageLayout.VCHAR, // CHAR
    PageLayout.BOOLEAN, // BOOLEAN
    PageLayout.TIME, // TIMESTAMP
    PageLayout.OBJ, // OBJECT
    PageLayout.INTERVAL, // INTERVAL
    -1, // VOID
    PageLayout.XML, // XMLTYPE
    -1, // UNKNOWN
    PageLayout.BIGDECIMAL, // BIGDECIMAL
    PageLayout.INTERVALYM  // INTERVAL YEAR TO MONTH
  };
 
  private static Datatype[] s_PageLayout_Datatype =
  {
    Datatype.VOID, // CHAR = 0;
    Datatype.VOID, // SHORT = 1;
    Datatype.INT, // INT = 2;
    Datatype.BIGINT, // LONG = 3;
    Datatype.FLOAT, // FLOAT = 4;
    Datatype.DOUBLE, //DOUBLE = 5;
    Datatype.TIMESTAMP, // TIME = 6;
    Datatype.INTERVAL, // INTERVAL = 7;
    Datatype.CHAR, // VCHAR = 8;
    Datatype.BYTE, // VBYTE = 9;
    Datatype.OBJECT, // OBJ = 10;
    Datatype.XMLTYPE, // XML = 11;
    Datatype.BOOLEAN, // BOOLEAN = 12;
    Datatype.BIGDECIMAL, // BIGDECIMAL = 13;
    Datatype.INTERVALYM
  };
  
    static
    {
      assert (Datatype.INT.ordinal() == 0) : "Datatype definition change, apply the change to the table";
      assert (Datatype.BIGDECIMAL.ordinal() == 13) : "Datatype definition change, apply the change to the table";
      assert (Datatype.Kind.values().length  == s_Datatype_PageLayout.length) : "Datatype definition change, apply the change to the table";
  
      assert (PageLayout.CHAR == 0) : "PageLayout definition change, apply the change to the table";
      assert (PageLayout.BIGDECIMAL  == 13) : "PageLayout definition change, apply the change to the table";
      assert (PageLayout.NO_TYPES == s_PageLayout_Datatype.length) : "PageLayout definition change, apply the change to the table";
  
      assert (s_typeNames.length == NO_TYPES);
      s_sizeof_types = new short[NO_TYPES];
      Arrays.fill(s_sizeof_types, (short) 0);
      
      byte[] buff = new byte[100];
      ByteBuffer t = ByteBuffer.wrap(buff);
      int pos0 = t.position();
      t.putInt(5555);
      int pos1 = t.position();
      s_sizeof_types[INT] = (short)(pos1 - pos0);

      pos0 = t.position();
      t.putChar('a');
      pos1 = t.position();
      s_sizeof_types[CHAR] = (short)(pos1 - pos0);

      pos0 = t.position();
      t.putShort((short) 5555);
      pos1 = t.position();
      s_sizeof_types[SHORT] = (short)(pos1 - pos0);

      pos0 = t.position();
      t.putLong(5555);
      pos1 = t.position();
      s_sizeof_types[LONG] = (short)(pos1 - pos0);

      pos0 = t.position();
      t.putFloat(12345.678f);
      pos1 = t.position();
      s_sizeof_types[FLOAT] = (short)(pos1 - pos0);

      pos0 = t.position();
      t.putDouble(12345.678);
      pos1 = t.position();
      s_sizeof_types[DOUBLE] = (short)(pos1 - pos0);

      short longsize = s_sizeof_types[LONG];
      s_sizeof_types[TIME] = longsize;
      s_sizeof_types[INTERVAL] = longsize;
      s_sizeof_types[INTERVALYM] = longsize;
      
      short objsize = 0;
        // FIXME andyp -- this is inherently non-portable!
      String datamodel = System.getProperty("sun.arch.data.model");
      if (datamodel != null && datamodel.equals("64"))
      {
        objsize = longsize;
      }
      else 
      {
          objsize = s_sizeof_types[INT];
      }
      s_sizeof_types[OBJ] = objsize;
      s_sizeof_types[VCHAR] = objsize;
      s_sizeof_types[VBYTE] = objsize;
      s_sizeof_types[XML] = objsize;
      
      //BigDecimal case
      s_sizeof_types[BIGDECIMAL] = objsize;
      
      buff = null;
      t = null;

      s_layoutMap = new HashMap<LayoutDesc, PageLayout>();
  }

  public static class LayoutDesc
  {
    short     m_noAttribs;          // number of attributes
    short     m_noRealAttribs;      // number of real attributes including addtional attribs
                                  // (metadata, length for VCHAR, VBYTE)
    short     m_noObjs;
    short     m_pageSize;
    byte[]    m_types;      // stores the types
    int[]     m_maxLens;
    short[]    m_lenPos;     // positions of length for VCHAR, VBYTE
    short[]    m_len2Pos;    // positions of scale for BIGDECIMAL
    short[]   m_managedObjAttribs;
    byte[]    m_managedObjTypes;
    
    public LayoutDesc(int sz, int sz2)
    {
      m_noAttribs = (short) sz;
      m_types = new byte[sz + sz2];
      m_lenPos = new short[sz + sz2];
      Arrays.fill(m_lenPos, (short)-1);
      m_len2Pos = new short[sz + sz2];
      Arrays.fill(m_len2Pos, (short)-1);
      m_noRealAttribs = (short)(m_noAttribs + sz2);
      assert (m_noRealAttribs < MAX_ATTRIBUTES);
      m_managedObjAttribs = null;
      m_managedObjTypes = null;
      m_maxLens = null;
    }

    public void setPageSize(int pageSize)
    {
      m_pageSize = (short) pageSize;
    }
    
    public void setMinObjs(int minObjs)
    {
      m_noObjs = (short) minObjs;
    }
    
    public static Datatype getDatatype(int layoutType)
    {
      return s_PageLayout_Datatype[layoutType];
    }

    public void setType(int pos, Datatype dataType, int maxLen)
    {
      byte layoutType = s_Datatype_PageLayout[dataType.ordinal()];
      setType(pos, layoutType, maxLen);
    }
    
    /**
     * Sets a type in the layout
     * @param pos
     * @param type
     */
    public void setType(int pos, byte type)
    {
    	setType(pos, type, 0);
    }
    
    private void setType(int pos, byte type, int maxLen)
    {
      assert (pos >= 0 && pos < m_types.length);
      m_types[pos] = type;
      if (type == VBYTE || type == VCHAR || type == XML )
      {
    	  if (maxLen == 0)
    	  {
    		  if (type == XML) maxLen = Constants.MAX_XMLTYPE_LENGTH;
    		  if (type == VBYTE) maxLen = Constants.MAX_BYTE_LENGTH;
    		  if (type == VCHAR) maxLen = Constants.MAX_CHAR_LENGTH;
    	  }
    	  if (m_maxLens == null)
    	  {
    		  m_maxLens = new int[ m_types.length ];
    	  }
    	  m_maxLens[pos] = maxLen;
      }
      if (type == VBYTE || type == VCHAR || type == XML || type == BIGDECIMAL)
      {
        m_lenPos[pos] = (short) m_noRealAttribs++;
        assert (m_noRealAttribs < MAX_ATTRIBUTES);
      }
      if (type == BIGDECIMAL)
      {
        m_len2Pos[pos] = (short) m_noRealAttribs++;
        assert (m_noRealAttribs < MAX_ATTRIBUTES);
      }
    }
    
    public void setManaged(int pos)
    {
      assert (m_types[pos] == OBJ || m_types[pos] == XML);
      if (m_managedObjAttribs == null)
      {
        m_managedObjAttribs = new short[1];
        m_managedObjAttribs[0] = (short) pos;
        m_managedObjTypes = new byte[1];
        m_managedObjTypes[0] = m_types[pos];
      }
      else
      {
        int len = m_managedObjAttribs.length;
        short[] oattrs = new short[len + 1];
        System.arraycopy(m_managedObjAttribs, 0, oattrs, 0, len);
        oattrs[len] = (short) pos;
        m_managedObjAttribs = oattrs;
        
        len = m_managedObjTypes.length;
        byte[] oattrtypes = new byte[len + 1];
        System.arraycopy(m_managedObjTypes, 0, oattrtypes, 0, len);
        oattrtypes[len] = m_types[pos];
        m_managedObjTypes = oattrtypes;
      }
    }
    protected static final int intHash(int key)
    {
      key += ~(key << 15);
      key ^= (key >> 10);
      key += (key << 3);
      key ^= (key >> 6);
      key += ~(key << 11);
      key ^= (key >> 16);
      return key;
    }
    
    public int hashCode()
    {
      int hash = 5381;
      hash = ((hash<< 5) + hash) + intHash(m_noAttribs);
      hash = ((hash<< 5) + hash) + intHash(m_pageSize);
      hash = ((hash<< 5) + hash) + intHash(m_noObjs);
      for (int i = 0; i < m_types.length; i++)
      {
        byte type = m_types[i];
        hash = ((hash<< 5) + hash) + intHash(type);
        if (m_maxLens != null)
        {
        	int maxLen = m_maxLens[i];
            hash = ((hash<< 5) + hash) + intHash(maxLen);
        }
      }
      return hash;
    }
    
    public boolean equals(Object other)
    {
      if (!(other instanceof LayoutDesc))
        return false;
      LayoutDesc o = (LayoutDesc) other;
      if (m_noAttribs != o.m_noAttribs) return false;
      if (m_pageSize != o.m_pageSize) return false;
      if (m_noObjs != o.m_noObjs) return false;
      if (m_maxLens != null)
      {
    	  if (o.m_maxLens == null) return false;
          if (!Arrays.equals(m_maxLens, o.m_maxLens)) return false;
      }
      return Arrays.equals(m_types, o.m_types);
    }
    
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      b.append(hashCode());
      b.append(" attribs=");
      b.append(m_noAttribs);
      b.append(" ");
      String typeDesc = "csilfdtvcboXXXXXXX";
      for (byte type : m_types) 
      {
        b.append(typeDesc.charAt(type));
      }
      b.append(" pageSize=");
      b.append(m_pageSize);
      b.append(" noObjs=");
      b.append(m_noObjs);
      return b.toString();
    }
  }
  
  public static PageLayout create(int id, LayoutDesc desc)
  {
    synchronized(s_layoutMap)
    {
      PageLayout layout = s_layoutMap.get(desc);
      if (layout == null)
      {
        layout = new PageLayout(id, desc);
        s_layoutMap.put(desc, layout);
      }
      return layout;
    }
  }
  
  private PageLayout(int id, LayoutDesc desc)
  {
    m_id = id;
    m_desc = desc;
    m_offsets = null;
  }
  
  public int getId() {return m_id;}
  
  public short getNoAttribs() {return m_desc.m_noAttribs;}
  public short getNoRealAttribs() {return m_desc.m_noRealAttribs;}
  public short[] getManagedObjAttribs() {return m_desc.m_managedObjAttribs;}
  public byte[] getManagedObjTypes() {return m_desc.m_managedObjTypes;}
  public short getPageSize() {return m_desc.m_pageSize;}
  
  /**
   * Gets the type layout
   * @return array of types
   */
  public byte[]  getTypes()
  {
   return m_desc.m_types; 
  }

  /**
   * Return the size for the type in a memory
   * @param type
   * @return
   */
  public static short getTypeSize(short type)
  {
   assert (type >= 0 && type < NO_TYPES);
   short sz = s_sizeof_types[type];
   assert (sz > 0);
   return sz;
  }
  
  /**
   * Gets the total size of this layout
   * @return
   */
  public short getSize()
  {
    getOffsets();
    return m_size;
  }
  
  public short getNoObjs()
  {
    getOffsets();
    return m_desc.m_noObjs;
  }
  
  /**
   * Gets the type usages in this layout
   * 
   * @return array of int <0 means not used.
   */
  public short[] getTypeUsages()
  {
    getOffsets();
    return m_typeUsages;
  }
    
  private final short getTypeUsage(byte t)
  {
    short offset = m_typeUsages[t];
    m_typeUsages[t] = (short) (offset + 1);
    assert (offset < MAX_ATTRIBUTES);
    return offset;
  }
  
  public int getMaxLen(int pos)
  {
	if (m_desc.m_maxLens != null)
		return m_desc.m_maxLens[pos];
	return 0;
  }

  public short getLengthPos(int pos)
  {
    return m_desc.m_lenPos[pos];
  }
  
  public short getLength2Pos(int pos)
  {
    return m_desc.m_len2Pos[pos];
  }
    
  /**
   * Gets the offsets of each type in this layout
   * e.g. i, i, f will be 0, 1, 0
   * 
   * @return
   */
  public short[] getOffsets()
  {
    if (m_offsets != null)
      return m_offsets;
    m_typeUsages = new short[NO_TYPES];
    Arrays.fill(m_typeUsages, (short) 0);
    short[] offsets = new short[m_desc.m_noRealAttribs];
    Arrays.fill(offsets, (short) -1);
    m_size = 0;
    int pos = 0;
    for (byte t : m_desc.m_types)
    {
      int tsize = s_sizeof_types[t];
      m_size += tsize;
      offsets[pos] = getTypeUsage(t);
      if (t == VCHAR || t == VBYTE || t == XML || t == BIGDECIMAL)
      {
        int lpos = getLengthPos(pos);
        offsets[lpos] = getTypeUsage(INT);
      }
      if (t == BIGDECIMAL)
      {
        int lpos = getLength2Pos(pos);
        offsets[lpos] = getTypeUsage(INT);
      }
      pos++;
    } 

    short objs = (short)((m_desc.m_pageSize / m_size) + 1);
    if (objs < m_desc.m_noObjs)
    {
      objs = m_desc.m_noObjs;
      m_pageSize = (short)(m_noObjs * m_size);
    }
    m_noObjs = objs;
    m_offsets = new short[m_noObjs * m_desc.m_noRealAttribs];
    pos = 0;
    for (int obj = 0; obj < m_noObjs; obj++)
    {
      for (int attr = 0; attr < m_desc.m_noRealAttribs; attr++)
      {
        int type = INT;
        if (attr < m_desc.m_types.length) type = m_desc.m_types[attr];
        assert (offsets[attr] >= 0);
        short offset = (short) (m_typeUsages [type] * obj + offsets [attr]);
        m_offsets[pos++] = offset;
      }
    }
    return m_offsets;
  }

  public synchronized Constructor<?> getPageConstructor()
  {
    if (m_pageConstructor != null)
      return m_pageConstructor;
    
    String basClassPath = "oracle.cep.memmgr.PageBase";
    String name = "Page_" + getId();
    PageClassGen gen = new PageClassGen(basClassPath, name, this, m_noObjs);
    gen.create();
    if (PAGE_GEN_DEBUG)
    {
      assert false : "should't be turned on in the release code";
      String filename = "/tmp/" + name + ".class";
      try {
        OutputStream f = new FileOutputStream(filename);
        gen.save(f);
        f.close();
        System.out.println(filename + " generated.");
      } catch (IOException e)
      {
        System.out.println(e);
      }
    }
    Class<?> pageClass = gen.loadToJvm();
    try {
      Class<?>[] ptypes = new Class[3];
      ptypes[0] = int.class;
      ptypes[1] = int.class;
      ptypes[2] = PageLayout.class;
      m_pageConstructor = pageClass.getConstructor(ptypes);
    }
    catch(NoSuchMethodException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      m_pageConstructor = null;
    }
    return m_pageConstructor;
  }
  
  public static String geTypeName(byte type)
  {
    if (type < 0 || type >= s_typeNames.length)
      return "unknown("+type+")";
    return s_typeNames[type];
  }
  
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append("id=");
    b.append(m_id);
    b.append(" ");
    b.append(m_desc.toString());
    return b.toString();
  }
}

