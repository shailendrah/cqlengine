/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/paged/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/12/12 - adding timestamp format
 sbishnoi    11/07/11 - formatting timestamp value
 sbishnoi    10/03/11 - changing format to intervalformat
 sbishnoi    08/29/11 - adding support for interval year to month
 udeshmuk    08/28/11 - add setter for id
 sbishnoi    08/27/11 - adding support for interval year to month
 udeshmuk    11/15/10 - support for to_bigint(timestamp)
 sborah      04/08/10 - char to number functions
 hopark      12/02/09 - fix copyTo
 hopark      11/02/09 - support large char
 sborah      06/30/09 - support for bigdecimal
 sborah      06/01/09 - support for xmltype in to_char
 hopark      04/09/09 - add copy
 hopark      03/16/09 - add obj heval
 hopark      02/17/09 - support boolean as external datatype
 hopark      02/17/09 - add OBJ_CPY - objtype support
 hopark      02/02/09 - objtype support
 sborah      02/13/09 - support for is_not_null
 hopark      11/28/08 - use CEPDateFormat
 hopark      10/16/08 - fix AbstraceMethodError from BLEN
 hopark      10/16/08 - fix NPE from copying XMLTYPE
 skmishra    08/22/08 - exception fronm bytetohex
 sbishnoi    07/23/08 - 
 hopark      07/18/08 - backport to drop6
 sbishnoi    07/13/08 - fix systimestampbyg
 parujain    07/08/08 - value based windows
 sbishnoi    06/24/08 - modifying length() to return null if arg attr is null
 sbishnoi    06/20/08 - support of to_char for other datatypes
 sbishnoi    06/19/08 - support for to_char(integer)
 hopark      06/17/08 - fix xmltypecpy
 hopark      05/16/08 - fix xmltuple copy
 parujain    05/12/08 - getitem object
 sbishnoi    04/24/08 - support of modulus function and handled divide by zero
                        exception
 hopark      03/12/08 - move datatype array to layoutdesc
 hopark      03/07/08 - setManagedObj
 hopark      02/27/08 - fix tuple serialization
 hopark      02/18/08 - add string functions
 hopark      02/09/08 - object representation of xml
 hopark      02/05/08 - parameterized error
 najain      02/04/08 - object representation of xml
 udeshmuk    01/31/08 - support for double data type.
 hopark      01/23/08 - fix datatype change
 hopark      12/06/07 - cleanup spill
 hopark      11/03/07 - PageLayout optimization
 hopark      11/27/07 - add BOOLEAN
 najain      11/21/07 - boolean datatype support
 hopark      11/16/07 - xquery support
 najain      11/05/07 - xquery support
 hopark      11/04/07 - fix byte/char init
 hopark      10/31/07 - move IPagePtr
 hopark      10/19/07 - fix aeval
 hopark      07/10/07 - use ITuple
 hopark      06/19/07 - cleanup
 hopark      05/28/07 - logging support
 hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
 najain      04/10/07 - 
 hopark      03/29/07 - added refcount debug facility
 hopark      03/21/07 - add pin
 najain      03/14/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/paged/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.paged;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.memory.TupleBase;
import oracle.cep.dataStructures.internal.memory.TupleBase.Scratch;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.SoftExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.IPage;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.PageManager;
import oracle.cep.memmgr.PageLayout.LayoutDesc;
import oracle.cep.memmgr.PageManager.PagePtr;
import oracle.cep.memmgr.PageManager.PageRef;
import oracle.cep.memmgr.factory.paged.TupleFactory;

import org.xml.sax.SAXException;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/paged/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 * @author hopark
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Hashcode"}, 
    attribVals={"getId", "hashCode"})
public class TuplePtr 
  implements ITuple, ITuplePtr
{
  // change this to true to save memory for TuplePtr
  // it allows id to be stored as part of a page.
  private static final boolean USE_ID_IN_PAGE = false;

  static final long serialVersionUID = 3334931761322104942L;

  public static final int NULL_POS = 0;
  public static final int ATTRNULL_POS = 1;
  public static final int ID_POS = 2;

  private static AtomicLong s_nextId = new AtomicLong();
  
  protected PagePtr  m_pagePtr;
  protected IPage  m_page;
  protected int    m_index;
  protected long   m_id;
  protected long   m_snapshotId;
  protected TupleFactory m_factory;
  
  
  protected static ThreadLocal<Scratch> scratchBuf = new ThreadLocal<Scratch>() {
    protected synchronized Scratch initialValue() {
      return new Scratch();
    }
  };
   
  public TuplePtr()
  {
    m_id = s_nextId.incrementAndGet();
    m_snapshotId = Long.MAX_VALUE;
  }

  public void setFactory(TupleFactory fac)
  {
    m_factory = fac;
  }

  public TupleFactory getFactory()
  {
    return m_factory;
  }
  
  public int getNumAttrs()
  {
    if (m_page == null)
      return 0;

    return m_page.getNoTypes();
  }
  
  public Datatype getAttrType(int pos)
  {
    if (m_page == null)
      return Datatype.VOID;

    byte[] types = m_page.getTypes();
    short noTypes = m_page.getNoTypes();
    assert (pos >= 0 && pos < noTypes);
    byte layoutType = types[pos];
    return LayoutDesc.getDatatype(layoutType);
  }
  
  public static PageLayout getPageLayout(int id, TupleSpec spec, short pageSize, short minObjs)
  {
    int numAttrs = spec.getNumAttrs();
    int auxAttribs = ATTRNULL_POS;
    if (USE_ID_IN_PAGE)
    {
      auxAttribs = ID_POS;
    }
    LayoutDesc desc = new LayoutDesc((short)numAttrs, (short)(auxAttribs + 1));
    desc.setPageSize(pageSize);
    desc.setMinObjs(minObjs);
    for (int i = 0; i < numAttrs; i++)
    {
      Datatype dataType = (spec.getAttrType(i));  
      desc.setType(i, dataType, spec.getAttrLen(i));
    }
    desc.setType(numAttrs + NULL_POS, PageLayout.INT);
    desc.setType(numAttrs + ATTRNULL_POS, PageLayout.OBJ);
    BitSet managedObjs = spec.getManagedObjs();
    for(int i=managedObjs.nextSetBit(0); i>=0; i=managedObjs.nextSetBit(i+1)) 
    {
      desc.setManaged(i);
    }
    if (USE_ID_IN_PAGE)
    {
      desc.setType(numAttrs + ID_POS, PageLayout.LONG);
    }
    return PageLayout.create(id, desc);
  }  
  
  public ITuple peek() {return this;}
  
  public void setTuple(PageRef pgRef, PageManager pm)
   throws ExecException
  {
    m_pagePtr = pgRef.m_pagePtr;
    assert (m_pagePtr != null);
    m_index = pgRef.m_index;
    if (USE_ID_IN_PAGE)
    {
      // copy id to the tuple body.
      m_page = m_pagePtr.pin(pm, IPinnable.WRITE);
      int pos = m_page.getNoTypes() + ID_POS;
      m_page.lValueSet(m_index, pos, m_id);
      //m_pagePtr.unpin();
    }
  }
  
  public PagePtr getPage() {return m_pagePtr;}
  public int getIndex() {return m_index;}
  
  /**
   * Pins the tuple. If the tuple has swappend out, retreive it from
   * the storage and reset the referent.
   * 
   * @return
   */
  public synchronized ITuple pinTuple(int mode) throws ExecException
  {
    if (m_page == null)
    {
      PageRef pgRef = (PageRef) m_factory.allocBody();
      PageManager pm = m_factory.getPageManager();
      setTuple(pgRef, pm);
      m_page = m_pagePtr.pin(pm, IPinnable.WRITE);
      init(m_factory.getTupleSpec(), false /* nullValue */);
    }
    return this;
  }

  public void unpinTuple() throws ExecException
  {
  }

  public boolean isTuplePinned() throws ExecException
  {
    return true;
  }
  
  public void setDirtyTuple() throws ExecException
  {
  }
  
  public final void copy(ITuplePtr srcPtr, int numAttrs) throws ExecException
  {
    ITuple dest = pinTuple(IPinnable.WRITE);
    ITuple src = srcPtr.pinTuple(IPinnable.READ);
    dest.copy(src, numAttrs);
    srcPtr.unpinTuple();
    unpinTuple();
  }
  
  public final void copy(ITuplePtr srcPtr, int[] srcAttrs, int[] destAttrs) throws ExecException
  {
    ITuple dest = pinTuple(IPinnable.WRITE);
    ITuple src = srcPtr.pinTuple(IPinnable.READ);
    dest.copy(src, srcAttrs, destAttrs);
    srcPtr.unpinTuple();
    unpinTuple();
  }  
  
  public boolean equals(Object other)
  {
    if (other instanceof ITuplePtr)
    {
      ITuplePtr e = (ITuplePtr) other;
      return getId() == e.getId();
    }
    return false;
  }

  public final boolean compare(ITuplePtr srcPtr) throws ExecException 
  {
    return compare(srcPtr, null);
  }
  
  public final boolean compare(ITuplePtr srcPtr, int[] skipPos) throws ExecException
  {
    ITuple dest = pinTuple(IPinnable.READ);
    ITuple src = srcPtr.pinTuple(IPinnable.READ);
    boolean b = skipPos != null ? dest.compare(src, skipPos) : dest.compare(src);
    srcPtr.unpinTuple();
    unpinTuple();
    return b;
  }

  public synchronized int getRefCnt()
  {
    if (m_pagePtr == null) return 0;
    return m_pagePtr.getRefCnt(m_index);
  }
  
  public synchronized int addRef(int ref)
  {
    return m_pagePtr.addRef(m_index, ref);
  }
  
  public synchronized int release()
  {
    // There is a case where a tuple is created and released right away.
    // If that happens, m_pagePtr can be null.
    // one example was RelSource:721 with S_PROCESS_UPDATE, !isisPrimaryKeyExist
    if (m_pagePtr == null) return 0;
    int rc = m_pagePtr.release(m_index);
    if (rc == 0)
    {
      m_page = null;
    }
    return rc;
  }  

  /* (non-Javadoc)
   * @see oracle.cep.dataStructures.internal.ITuple#init(oracle.cep.execution.internals.TupleSpec, boolean)
   */
  public void init(TupleSpec spec, boolean nullValue) throws ExecException
  {
    int max = spec.getNumAttrs();

    // Allocate attributes
    for (int i = 0; i < max; i++)
    {
      switch (spec.getAttrType(i).getKind())
      {
        case INT:
          iValueSet(i, 0);
          break;
        case BIGINT:
          lValueSet(i, 0);
          break;
        case FLOAT:
          fValueSet(i, 0);
          break;
        case DOUBLE:
          dValueSet(i,0);
          break;
        case BIGDECIMAL:
          nValueSet(i,BigDecimal.ZERO, 1, 0);
          break;
        case CHAR:
          cLengthSet(i, 0);
          break;
        case BYTE:
          bLengthSet(i, 0);
          break;
        case OBJECT:
          oValueSet(i, null);
          break;
        case TIMESTAMP:
          tValueSet(i, 0);
          tFormatSet(i, spec.getAttrMetadata(i).getTimestampFormat());
          break;
        case INTERVAL:
          vValueSet(i, 0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case INTERVALYM:
          vymValueSet(i, 0, spec.getAttrMetadata(i).getIntervalFormat());
          break;
        case XMLTYPE:
          xLengthSet(i, 0);
          xValueSet(i, null);
          break;
        case BOOLEAN:
          boolValueSet(i, false);
          break;
        default:
          assert false;
      }
      if (nullValue)
        setAttrNull(i);
    }
  }

  public long getId()
  {
    if (USE_ID_IN_PAGE)
    {
      int pos = m_page.getNoTypes() + ID_POS;
      return m_page.lValueGet(m_index, pos);
    }
    return m_id;
    
  }
  
  public void setId(long newId)
  {
    //Currently USE_ID_IN_PAGE is set to false at the beginning
    //and it is a constant. If that is changed then this also needs
    //to change. Currently putting empty implementation for the 
    //case when USE_ID_IN_PAGE is TRUE
    if(USE_ID_IN_PAGE)
    {
    }
    else
      this.m_id = newId;
    
  }
  
  public long getSnapshotId()
  {
    //FIXME: not sure about correctness
    return this.m_snapshotId;
  }
  
  public void setSnapshotId(long newSnapshotId)
  {
    //FIXME: not sure about correctness
    this.m_snapshotId = newSnapshotId;
  }
  
  /* (non-Javadoc)
   * @see oracle.cep.dataStructures.internal.ITuple#isBNull()
   */
  public boolean isBNull()
  {
    int pos = m_page.getNoTypes() + NULL_POS;
    return (m_page.iValueGet(m_index, pos) != 0);
  }


  /* (non-Javadoc)
   * @see oracle.cep.dataStructures.internal.ITuple#setBNull(boolean)
   */
  public void setBNull(boolean null1)
  {
    int pos = m_page.getNoTypes() + NULL_POS;
    m_page.iValueSet(m_index, pos, null1 ? 1:0);
  }
  
  private BitSet getAttrNulls(boolean gen)
    throws ExecException
  {
    int pos = m_page.getNoTypes() + ATTRNULL_POS;
    BitSet s = (BitSet) m_page.oValueGet(m_index, pos);
    if (s == null && gen)
    {
      s = new BitSet();
      m_page.oValueSet(m_index, pos, s);
    }
    return s;
  }
  
  /**
   * Return true if the Value of the attribute is null
   * 
   * @param pos
   *          Position of the attribute
   * @return True if Null else False
   * @throws ExecException
   */
  public boolean isAttrNull(int pos) throws ExecException
  {
    BitSet s = getAttrNulls(false);
    if (s == null) return false;
    return s.get(pos);
  }

  /**
   * Sets the Attribute to be null
   * 
   * @param pos
   *          Position of the attribute in the tuple
   * @throws ExecException
   */
  public void setAttrNull(int pos) throws ExecException
  {
    BitSet s = getAttrNulls(true);
    s.set(pos);
  }

  /**
   * Resets the value of bNull to false. This is the responsibility of the
   * caller to reset Attribute bNull if the value becomes non-null
   * 
   * @param pos
   *          Postion of interest
   * @throws ExecException
   */
  public void setAttrbNullFalse(int pos) throws ExecException
  {
    BitSet s = getAttrNulls(false);
    if (s != null) s.clear(pos);
  }

  /**
   * Gets the value of an int attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public int iValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.iValueGet(m_index, pos);
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void iValueSet(int pos, int v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.iValueSet(m_index, pos, v);
  }

  /**
   * Gets the value of an boolean attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public boolean boolValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.boolValueGet(m_index, pos);
  }

  /**
   * Sets the value of an boolean attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void boolValueSet(int pos, boolean v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.boolValueSet(m_index, pos, v);
  }

  public char[] xValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.xValueGet(m_index, pos);
  }

  public boolean xIsObj(int pos) throws ExecException
  {
    return m_page.xIsObj(m_index, pos);
  }
  
  /**
   * Gets the length of a xmltype attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int xLengthGet(int pos) throws ExecException
  {
    return m_page.xLengthGet(m_index, pos);
  }

  public void xValueSet(int pos, Object o) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.xObjValueSet(m_index, pos, o);
  }

  public Object getItem(int pos, Object ctx)
    throws ExecException, IOException, SAXException
  {
    assert isAttrNull(pos) == false;
    return m_page.xObjValueGet(m_index, pos, ctx);
  }

  public void xValueSet(int pos, char[] v, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    if (v == null)
    {
      setAttrNull(pos);
      m_page.xLengthSet(m_index, pos, l);
    } 
    else 
    {
      // copy value
      char[] v0 = m_page.xValueGet(m_index, pos);
      if (v0 == null || v0.length < l)
        v0 = new char[l];
      System.arraycopy(v, 0, v0, 0, l);
      m_page.xValueSet(m_index, pos, v0, l);
    }
  }

  protected void xLengthSet(int pos, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.xLengthSet(m_index, pos, l);
  }
  
  /**
   * Gets the value of an int attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public long lValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.lValueGet(m_index, pos);
  }

  /**
   * Sets the value of an int attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void lValueSet(int pos, long v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.lValueSet(m_index, pos, v);
  }
  
  /**
   * Sets the value of an int attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public float fValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.fValueGet(m_index, pos);
  }

  /**
   * Gets the float value of an attribute (either int or float)
   * 
   * @param pos
   *          Position of interest
   * @return Float Attibute value
   * @throws ExecException
   */
  public float floatValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    byte[] types = m_page.getTypes();
    switch (types[pos]) {
    case PageLayout.INT:
      return (float) iValueGet(pos);
    case PageLayout.LONG:
      return (float) lValueGet(pos);
    case PageLayout.FLOAT:
      break;
    default:
      throw new ExecException(ExecutionError.TYPE_MISMATCH,
          PageLayout.geTypeName(types[pos]), 
          Datatype.FLOAT.toString() +"," + Datatype.INT.toString() + "," + Datatype.BIGINT.toString());
    }
    return fValueGet(pos);
  }

  /**
   * Sets the value of an float attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void fValueSet(int pos, float v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.fValueSet(m_index, pos, v);
  }

  /**
   * Sets the value of a double attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public double dValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.dValueGet(m_index, pos);
  }

  /**
   * Gets the double value of an attribute (either int or float)
   * 
   * @param pos
   *          Position of interest
   * @return Double Attribute value
   * @throws ExecException
   */
  public double doubleValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    byte[] types = m_page.getTypes();
    switch (types[pos]) {
      case PageLayout.INT:
        return (double) iValueGet(pos);
      case PageLayout.LONG:
        return (double) lValueGet(pos);
      case PageLayout.FLOAT:
        return (double) fValueGet(pos);
      case PageLayout.DOUBLE:
        break;
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return dValueGet(pos);
  }
  
  /**
   * Gets the double value of an attribute (float or double)
   * 
   * @param pos
   *          Position of interest
   * @return Double Attribute value
   * @throws ExecException
   */
  public double dblValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    byte[] types = m_page.getTypes();
    switch (types[pos]) {
      case PageLayout.FLOAT:
        return (double) fValueGet(pos);
      case PageLayout.DOUBLE:
        break;
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return dValueGet(pos);
  }
  
  /**
   * Sets the value of a BigDecimal attribute
   * 
   * @param pos
   *          Position of interest
   * @return BigDecimal Attribute value
   * @throws ExecException
   */
  public BigDecimal nValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.nValueGet(m_index, pos);
  }
  
  /**
   * Gets the precision of a bigdecimal attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int nPrecisionGet(int pos) throws ExecException
  {
    return m_page.nPrecisionGet(m_index, pos);
  }
  
  /**
   * Gets the scale of a bigdecimal attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int nScaleGet(int pos) throws ExecException
  {
    return m_page.nScaleGet(m_index, pos);
  }
  
  /**
   * Gets the BigDecimal value of an attribute (int, bigint, timestamp, 
   * interval, float and double)
   * @param pos
   *          Position of interest
   * @return BigDecimal Attribute value
   * @throws ExecException
   */
  public BigDecimal bigDecimalValueGet(int pos) throws ExecException
  {
    // TODO : how to handle precision and scale for this case ??
    assert isAttrNull(pos) == false;
    byte[] types = m_page.getTypes();
    switch (types[pos]) 
    {
      case PageLayout.INT:
        return new BigDecimal(String.valueOf(iValueGet(pos)));
      case PageLayout.LONG:
        return new BigDecimal(String.valueOf(lValueGet(pos)));
      case PageLayout.FLOAT:
        return new BigDecimal(String.valueOf(fValueGet(pos)));
      case PageLayout.DOUBLE:
        return new BigDecimal(String.valueOf(dValueGet(pos)));
      case PageLayout.BIGDECIMAL:
        break;
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return nValueGet(pos);
  }
  
  /**
   * Gets the long value of an attribute (int, bigint, timestamp or interval)
   * 
   * @param pos
   *          Position of interest
   * @return long value
   * @throws ExecException
   */
  public long longValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    byte[] types = m_page.getTypes();
    switch (types[pos]) {
      case PageLayout.INT:
        return (long) iValueGet(pos);
      case PageLayout.LONG:
        break;
      case PageLayout.INTERVAL:
        return vValueGet(pos);
      case PageLayout.INTERVALYM:
        return vymValueGet(pos);
      case PageLayout.TIME:
        return tValueGet(pos);
      default:
        throw new ExecException(ExecutionError.TYPE_MISMATCH);
    }
    return lValueGet(pos);
	 
  }

  /**
   * Sets the value of an double attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void dValueSet(int pos, double v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.dValueSet(m_index, pos, v);
  }

  /**
   * Sets the value of a BigDecimal attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param precision
   *          precision value of the bigdecimal attribute
   *@param scale
   *          scale value of the bigdecimal attribute
   * @throws ExecException
   */
  public void nValueSet(int pos, BigDecimal v, int precision, int scale) 
  throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.nValueSet(m_index, pos, v, precision, scale);
  }
  
  /**
   * Returns the timestamp value of the attribute
   * 
   * @param pos
   *          Position of interest
   * @return Long value of timestamp attribute
   * @throws ExecException
   */
  public long tValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.tValueGet(m_index, pos);
  }

  /**
   * Sets the value of the TimeStamp attribute
   * 
   * @param pos
   *          Position of interest
   * @param ts
   *          Timestamp from which value needs to be extracted
   * @throws ExecException
   */
  public void tValueSet(int pos, Timestamp ts) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.tValueSet(m_index, pos, ts.getTime() * 1000000l);
  }

  /**
   * Sets the value of the TimeStamp attribute
   * 
   * @param pos
   *          Position of interest
   * @param ts
   *          Timestamp value which needs to be saved
   * @throws ExecException
   */
  public void tValueSet(int pos, long ts) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.tValueSet(m_index, pos, ts);
  }
  

  @Override
  /**
   * Sets the format value of the timestamp attribute
   * @param position position of interest
   * @param format timestamp format 
   * @throws ExecException
   */
  public void tFormatSet(int position, TimestampFormat format)
      throws ExecException
  {
    setAttrbNullFalse(position);
    // Note: we will not set the timestamp format
  } 
  
  /**
   * Get the timestamp format
   * @param pos position of interest
   * @return
   */
  public TimestampFormat tFormatGet(int pos)
  {
    return TimestampFormat.getDefault();
  }

  /**
   * Sets the value of the interval attribute
   * 
   * @param pos
   *          Position of interest
   * @param interval
   *          Interval value
   * @param format
   *          Interval value format
   * @throws ExecException
   */
  public void vValueSet(int pos, long v, IntervalFormat format) 
    throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.vValueSet(m_index, pos, v, format);
  }
  /**
   * Gets the interval attribute value
   * 
   * @param pos
   *          Position of interval
   * @return Interval value
   * @throws ExecException
   */
  public long vValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.vValueGet(m_index, pos);
  }
  
  /**
   * Sets the value of the interval year to month attribute
   * 
   * @param pos
   *          Position of interest
   * @param interval
   *          Interval value
   * @param format
   *          Interval Value format
   * @throws ExecException
   */
  public void vymValueSet(int pos, long v, IntervalFormat format) 
    throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.vymValueSet(m_index, pos, v, format);
  }
  /**
   * Gets the interval attribute year to month value
   * 
   * @param pos
   *          Position of interval
   * @return Interval value
   * @throws ExecException
   */
  public long vymValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.vymValueGet(m_index, pos);
  }
  

  @Override
  public IntervalFormat vFormatGet(int position) throws ExecException
  {
    // Note: Presently We are not storing Interval Format into disk on spill
    // So we will return a default format when a tuple will be read from the
    // storage
    //return m_page.vFormatGet();
    try
    {
    if(getAttrType(position) == Datatype.INTERVAL)
    {
      return new IntervalFormat(TimeUnit.DAY, TimeUnit.SECOND, 9, 9);
    }
    else if(getAttrType(position) == Datatype.INTERVALYM)
    {
      return new IntervalFormat(TimeUnit.DAY, TimeUnit.SECOND, 9, 9);
    }
    else
      assert false;
    }
    catch(CEPException e)
    {
      // Unreachable as this format is constructed with valid parameters
      assert false;
    }
    return null;
  }
  
  /**
   * Gets the value of an char attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public char[] cValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.cValueGet(m_index, pos);
  }

  /**
   * Gets the length of a char attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int cLengthGet(int pos) throws ExecException
  {
    return m_page.cLengthGet(m_index, pos);
  }

  /**
   * Sets the value of an char attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void cValueSet(int pos, char[] v, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    if (v == null)
    {
      setAttrNull(pos);
      m_page.cLengthSet(m_index, pos, l);
    } else {
      // copy value
      char[] v0 = m_page.cValueGet(m_index, pos);
      if (v0 == null || v0.length < l)
        v0 = new char[l];
      for (int i = 0; i < l; i++)
        v0[i] = v[i];
      m_page.cValueSet(m_index, pos, v0, l);
    }
  }

  protected void cLengthSet(int pos, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.cLengthSet(m_index, pos, l);
  }
  /**
   * Gets the value of an byte attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  public byte[] bValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return m_page.bValueGet(m_index, pos);
  }

  /**
   * Gets the length of a byte attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute length
   * @throws ExecException
   */
  public int bLengthGet(int pos) throws ExecException
  {
    return m_page.bLengthGet(m_index, pos);
  }

  /**
   * Sets the value of an byte attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @param l
   *          Attribute length
   * @throws ExecException
   */
  public void bValueSet(int pos, byte[] v, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    if (v == null)
    {
      setAttrNull(pos);
      m_page.bLengthSet(m_index, pos, l);
    } else {
      // copy value
      byte[] v0 = m_page.bValueGet(m_index, pos);
      if (v0 == null || v0.length < l)
        v0 = new byte[l];
      for (int i = 0; i < l; i++)
        v0[i] = v[i];
      m_page.bValueSet(m_index, pos, v0, l);
    }
  }

  protected void bLengthSet(int pos, int l) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.bLengthSet(m_index, pos, l);
  }

  /**
   * Gets the value of an Object attribute
   * 
   * @param pos
   *          Position of interest
   * @return Attribute value
   * @throws ExecException
   */
  @SuppressWarnings("unchecked")
  public <T> T oValueGet(int pos) throws ExecException
  {
    assert isAttrNull(pos) == false;
    return (T) m_page.oValueGet(m_index, pos);
  }

  /**
   * Sets the value of an Object attribute
   * 
   * @param pos
   *          Position of interest
   * @param v
   *          Attribute value to set
   * @throws ExecException
   */
  public void oValueSet(int pos, Object v) throws ExecException
  {
    setAttrbNullFalse(pos);
    m_page.oValueSet(m_index, pos, v);
  }

  public void copy(ITuple src) throws ExecException
  {
    copy(src, m_page.getNoTypes());
  }

  public void copy(ITuple src, int numAttrs) throws ExecException
  {
    if (numAttrs <= 0)
      numAttrs = m_page.getNoTypes();
    
    byte[] types = m_page.getTypes();
    
    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      if (src.isAttrNull(a))
        setAttrNull(a);
      else
      {
        switch (types[a])
        {
          case PageLayout.INT:
            iValueSet(a, src.iValueGet(a));
            break;
          case PageLayout.LONG:
            lValueSet(a, src.lValueGet(a));
            break;
          case PageLayout.FLOAT:
            fValueSet(a, src.fValueGet(a));
            break;
          case PageLayout.DOUBLE:
            dValueSet(a, src.dValueGet(a));
            break;
          case PageLayout.BIGDECIMAL:
            nValueSet(a, src.nValueGet(a), src.nPrecisionGet(a), src.nScaleGet(a));
            break;
          case PageLayout.VBYTE:
            bValueSet(a, src.bValueGet(a), src.bLengthGet(a));
            break;
          case PageLayout.VCHAR:
            cValueSet(a, src.cValueGet(a), src.cLengthGet(a));
            break;
          case PageLayout.TIME:
            tValueSet(a, src.tValueGet(a));
            tFormatSet(a, src.tFormatGet(a));
            break;
          case PageLayout.OBJ:
            oValueSet(a, src.oValueGet(a));
            break;
          case PageLayout.INTERVAL:
            vValueSet(a, src.vValueGet(a), src.vFormatGet(a));
            break;
          case PageLayout.INTERVALYM:
            vymValueSet(a, src.vymValueGet(a), src.vFormatGet(a));
            break;
          case PageLayout.XML:
            if (src.xIsObj(a))
            {
              try
              {
                // we already checked if it uses object representation
                // so we can safely pass null for ctx.
                xValueSet(a, src.getItem(a, null));
              } catch(Exception e)
              {
                LogUtil.logStackTrace(LoggerType.TRACE, Level.WARNING, e);
              }
            }
            else
            {
              xValueSet(a, src.xValueGet(a), src.xLengthGet(a));
            }
            break;
          case PageLayout.BOOLEAN:
            boolValueSet(a, src.boolValueGet(a));
            break;
          default:
            // Should not come
            assert false;
        }
      }
    }
  }

  public void copy(ITuple src, int[] srcAttrs, int[] destAttrs) throws ExecException
  {
    int numAttrs = srcAttrs.length;
    assert (numAttrs == destAttrs.length);
    
    byte[] types = m_page.getTypes();
    
    // Get the attributes
    for (int a = 0; a < numAttrs; a++)
    {
      int spos = srcAttrs[a];
      int dpos = destAttrs[a];
      
      if (src.isAttrNull(spos))
        setAttrNull(dpos);
      else
      {
        switch (types[dpos])
        {
          case PageLayout.INT:
            iValueSet(dpos, src.iValueGet(spos));
            break;
          case PageLayout.LONG:
            lValueSet(dpos, src.longValueGet(spos));
            break;
          case PageLayout.FLOAT:
            fValueSet(dpos, src.floatValueGet(spos));
            break;
          case PageLayout.DOUBLE:
            dValueSet(dpos, src.doubleValueGet(spos));
            break;
          case PageLayout.BIGDECIMAL:
            BigDecimal val = src.bigDecimalValueGet(spos);
            
            if(src.getAttrType(spos).getKind() == Datatype.BIGDECIMAL.getKind())
              nValueSet(dpos, val, src.nPrecisionGet(spos), src.nScaleGet(spos)); 
            else
              nValueSet(dpos, val, val.precision(), val.scale());
            break;
          case PageLayout.VBYTE:
            bValueSet(dpos, src.bValueGet(spos), src.bLengthGet(spos));
            break;
          case PageLayout.VCHAR:
            cValueSet(dpos, src.cValueGet(spos), src.cLengthGet(spos));
            break;
          case PageLayout.TIME:
            tValueSet(dpos, src.tValueGet(spos));
            tFormatSet(dpos, src.tFormatGet(spos));
            break;
          case PageLayout.OBJ:
            oValueSet(dpos, src.oValueGet(spos));
            break;
          case PageLayout.INTERVAL:
            vValueSet(dpos, src.vValueGet(spos), src.vFormatGet(spos));
            break;
          case PageLayout.INTERVALYM:
            vymValueSet(dpos, src.vymValueGet(spos), src.vFormatGet(spos));
            break;
          case PageLayout.XML:
            if (src.xIsObj(spos))
            {
              try
              {
                // we already checked if it uses object representation
                // so we can safely pass null for ctx.
                xValueSet(dpos, src.getItem(spos, null));
              } catch(Exception e)
              {
                LogUtil.logStackTrace(LoggerType.TRACE, Level.WARNING, e);
              }
            }
            else
            {
              xValueSet(dpos, src.xValueGet(spos), src.xLengthGet(spos));
            }
            break;
          case PageLayout.BOOLEAN:
            boolValueSet(dpos, src.boolValueGet(spos));
            break;
          default:
            // Should not come
            assert false;
        }
      }
    }
  }
  
  public void copyTo(TupleValue s, int numAttrs, TupleSpec attrSpecs, Column inCols[]) 
    throws CEPException
  {
    // Set attributes
    for (int i = 0; i < numAttrs; i++)
    {
      boolean isNull = false;
      AttributeValue outAttr = s.getAttribute(i);
      isNull = isAttrNull(i);
      outAttr.setBNull(isNull);
      if (!isNull)
      {
        int pos = inCols[i].getColnum();
        switch (attrSpecs.getAttrType(i).getKind())
        {
          case INT:
            s.iValueSet(i, iValueGet(pos));
            break;
          case BIGINT:
            s.lValueSet(i, lValueGet(pos));
            break;
          case FLOAT:
            s.fValueSet(i, fValueGet(pos));
            break;
          case DOUBLE:
            s.dValueSet(i, dValueGet(pos));
            break;
          case BIGDECIMAL:
            s.nValueSet(i, nValueGet(pos), nPrecisionGet(pos), nScaleGet(pos));
            break;
          case CHAR:
            s.cValueSet(i, cValueGet(pos));
            s.cLengthSet(i, cLengthGet(pos));
            break;
          case BYTE:
            s.bValueSet(i, bValueGet(pos));
            s.bLengthSet(i, bLengthGet(pos));
            break;
          case TIMESTAMP:
            s.tValueSet(i, tValueGet(pos));
            s.tFormatSet(i, tFormatGet(pos));
            break;
          case INTERVAL:
            s.vValueSet(i, vValueGet(pos), vFormatGet(pos));
            break;
          case INTERVALYM:
            s.vymValueSet(i, vymValueGet(pos), vFormatGet(pos));
            break;
          case XMLTYPE:
          {
            char[] xv = xValueGet(pos);
            s.xValueSet(i, xv);
          }
            break;
          case BOOLEAN:
            s.boolValueSet(i, boolValueGet(pos));
            break;
          case OBJECT:
            s.oValueSet(i, oValueGet(pos));
            break;
          default:
            assert false;
        }
      }
    }
  }

  public void copyFrom(TupleValue s, int numAttrs, TupleSpec attrSpecs) 
    throws CEPException
  {
    // Set attributes
    for (int i = 0; i < numAttrs; i++)
    {
      boolean isNull = false;
      AttributeValue inpAttr = s.getAttribute(i);
      isNull = inpAttr.isBNull();
      if (isNull) setAttrNull(i);
      else this.setAttrbNullFalse(i);
      if (!isNull)
      {
        switch (attrSpecs.getAttrType(i).getKind())
        {
          case INT:
            iValueSet(i, s.iValueGet(i));
            break;
          case BIGINT:
            lValueSet(i, s.lValueGet(i));
            break;
          case FLOAT:
            fValueSet(i, s.fValueGet(i));
            break;
          case DOUBLE:
            dValueSet(i, s.dValueGet(i));
            break;
          case BIGDECIMAL:
            nValueSet(i, s.nValueGet(i), s.nPrecisionGet(i), s.nScaleGet(i));
            break;
          case CHAR:
            cValueSet(i, s.cValueGet(i), s.cLengthGet(i));
            break;
          case BYTE:
            bValueSet(i, s.bValueGet(i), s.bLengthGet(i));
            break;
          case TIMESTAMP:
            tValueSet(i, s.tValueGet(i));
            tFormatSet(i, s.tFormatGet(i));
            break;
          case INTERVAL:
            vValueSet(i, s.intervalValGet(i), s.vFormatGet(i));
            break;
          case INTERVALYM:
            vymValueSet(i, s.intervalYMValGet(i), s.vFormatGet(i));
            break;
          case XMLTYPE:
            xValueSet(i, s.xValueGet(i), s.xLengthGet(i));
            break;
          case BOOLEAN:
            boolValueSet(i, s.boolValueGet(i));
            break;
          case OBJECT:
            oValueSet(i, s.oValueGet(i));
            break;
          default:
            assert false;
        }
      }
    }
  }

  public boolean equals(ITuple e)
  {
    return (getId() == e.getId());
  }


  @Override
  public boolean compare(ITuple src) throws ExecException
  {
    return compare(src, null);
  }
  
  public boolean compare(ITuple src, int[] skipPos) throws ExecException
  {
    short nAttribs = m_page.getNoTypes();
    byte[] types = m_page.getTypes();
    boolean same = true;

    // Get the attributes
    for (byte a = 0; a < nAttribs; a++)
    {
      if(skipPos != null)
      {
        boolean found = false;
        for(int next: skipPos)
        {
          found = next == a;
          if(found) 
            break;
        }
        if(found)
          continue;
      }
      
      if (isAttrNull(a))
          same = src.isAttrNull(a);
      else
      {
          int ival;
          long lval;
          float fval;
          double dval;
          byte[] bval, bval2;
          char[] cval, cval2;
          Object oval, oval2;
          boolean boolval;
       
          switch (types[a])
          {
            case PageLayout.INT:
              ival = iValueGet(a);
              same = (ival == src.iValueGet(a));
              break;
            case PageLayout.LONG:
              lval = lValueGet(a);
              same = (lval == src.lValueGet(a));
              break;
            case PageLayout.FLOAT:
              fval = fValueGet(a);
              same = (fval == src.fValueGet(a));
              break;
            case PageLayout.DOUBLE:
              dval = dValueGet(a);
              same = (dval == src.dValueGet(a));
              break;
            case PageLayout.BIGDECIMAL:
              same = (nValueGet(a).compareTo(src.nValueGet(a)) == 0);
              break;
            case PageLayout.VBYTE:
              ival = bLengthGet(a);
              same = (ival == src.bLengthGet(a));
              if (!same) break;
              bval = bValueGet(a);
              bval2 = src.bValueGet(a);
              for (int i = 0; i < ival; i++)
              {
                same = (bval[i] == bval2[i]);
                if (!same) break;
              }
              break;
            case PageLayout.VCHAR:
              ival = cLengthGet(a);
              same = (ival == src.cLengthGet(a));
              if (!same) break;
              cval = cValueGet(a);
              cval2 = src.cValueGet(a);
              for (int i = 0; i < ival; i++)
              {
                same = (cval[i] == cval2[i]);
                if (!same) break;
              }
              break;
            case PageLayout.TIME:
              lval = tValueGet(a);
              same = (lval == src.tValueGet(a));
              break;
            case PageLayout.OBJ:
              oval = oValueGet(a);
              oval2 = src.oValueGet(a);
              same = (oval == oval2);
              break;
            case PageLayout.INTERVAL:
              lval = vValueGet(a);
              same = (lval == src.vValueGet(a));
              break;
            case PageLayout.INTERVALYM:
              lval = vymValueGet(a);
              same = (lval == src.vymValueGet(a));
              break;
            case PageLayout.XML:
              ival = xLengthGet(a);
              same = (ival == src.xLengthGet(a));
              if (!same) break;
              cval = xValueGet(a);
              cval2 = src.xValueGet(a);
              for (int i = 0; i < ival; i++)
              {
                same = (cval[i] == cval2[i]);
                if (!same) break;
              }
              break;
            case PageLayout.BOOLEAN:
              boolval = boolValueGet(a);
              same = (boolval == src.boolValueGet(a));
              break;
            default:
              // Should not come
              assert false;
        }
      }
      if (!same) break;
    }
    return same;
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("Tuple(" + getId() + ") '");
    
    if (m_page == null)
      return buf.toString();
      
    try
    {
      String str;
      // Get the attributes
      byte[] types = m_page.getTypes();
      short noTypes = m_page.getNoTypes();
      for (byte a = 0; a < noTypes; a++)
      {
        if (isAttrNull(a))
          buf.append("null");
        else
        {
          switch (types[a])
          {
            case PageLayout.INT:
              buf.append(iValueGet(a));
              break;
            case PageLayout.LONG:
              buf.append(lValueGet(a));
              break;
            case PageLayout.FLOAT:
              buf.append(fValueGet(a));
              break;
            case PageLayout.DOUBLE:
              buf.append(dValueGet(a));
              break;
            case PageLayout.BIGDECIMAL:
              buf.append(nValueGet(a));
              break;
            case PageLayout.VBYTE:
              buf.append("byte[" + bLengthGet(a) + "]");
              break;
            case PageLayout.VCHAR:
            {
              char[] chars = cValueGet(a);
              if (chars == null || chars.length == 0)
                str = "empty";
              else
                str = new String(chars, 0, cLengthGet(a));
              buf.append("char[" + chars.length + "]=" + str);
            }
              break;
            case PageLayout.TIME:
              buf.append(tValueGet(a));
              break;
            case PageLayout.OBJ:
              Object oval = oValueGet(a);
              buf.append("obj=" + ((oval == null) ? "null" : oval.getClass().getSimpleName() + oval.hashCode()));
              break;
            case PageLayout.INTERVAL:
              buf.append(vValueGet(a));
              break;
            case PageLayout.INTERVALYM:
              buf.append(vymValueGet(a));
              break;
            case PageLayout.XML:
            {
              char[] chars = xValueGet(a);
              if (chars == null || chars.length == 0)
                str = "empty";
              else
                str = new String(chars, 0, xLengthGet(a));
              buf.append("xml[" + chars.length + "]=" + str);
            }
              break;
            case PageLayout.BOOLEAN:
              buf.append(boolValueGet(a));
              break;
            default:
              // Should not come
              assert false;
          }
        }
        buf.append(",");
      }
      buf.append("'");
    }
    catch (ExecException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return buf.toString();
  }

  public boolean beval(Datatype type, Op op, int col, int bit, 
                      ITuple other, int col2, int bit2, 
                      Pattern pattern, boolean n2ntrue) 
    throws ExecException
  {
    if (op == Op.IS_NULL)
      return isAttrNull(col);
    
    if(op == Op.IS_NOT_NULL)
      return !isAttrNull(col);

    switch (op.nullType)
    {
     case ANY_N2N:
      if (n2ntrue && isAttrNull(col) && other.isAttrNull(col2)) 
        return true;
      else if(isAttrNull(col) || other.isAttrNull(col2))
        return false;        
      break;
     case ANY:
      if (isAttrNull(col) || other.isAttrNull(col2))
        return false;
      break;
    }

    assert (!isAttrNull(col));
    if (op.args == 2) 
    {
      assert (other != null);
      assert (!other.isAttrNull(col2));
    }
    switch(type.getKind())
    {
      case INT:
      {
        int val1, val2;
        val1 = iValueGet(col);
        val2 = other.iValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BIGINT:
      {
        long val1, val2;
        val1 = lValueGet(col);
        val2 = other.lValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case FLOAT:
      {
        float val1, val2;
        val1 = fValueGet(col);
        val2 = other.fValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case DOUBLE:
      {
        double val1, val2;
        val1 = dValueGet(col);
        val2 = other.dValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BIGDECIMAL:
      {
        BigDecimal val1, val2;
        val1 = nValueGet(col);
        val2 = other.nValueGet(col2);
        switch(op)
        {
          case LT:
            return (val1.compareTo(val2) < 0);
          case LE:
            return (val1.compareTo(val2) <= 0);
          case GT:
            return (val1.compareTo(val2) > 0);
          case GE:
            return (val1.compareTo(val2) >= 0);
          case EQ:
            return (val1.compareTo(val2) == 0);
          case NE:
            return (val1.compareTo(val2) != 0);
          default:
            assert false;
        }
      }
        break;
      
      case TIMESTAMP:
      {
        long val1, val2;
        //TODO: Ensure that timezone doesn't impact the comparison
        val1 = tValueGet(col);
        val2 = other.tValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      
      case BYTE:
      {
        int val = TupleBase.bCompare(bValueGet(col), 
                           bLengthGet(col), 
                           other.bValueGet(col2), 
                           other.bLengthGet(col2));
        switch(op)
        {
          case LT: return (val < 0);
          case LE: return (val <= 0);
          case GT: return (val > 0);
          case GE: return (val >= 0);
          case EQ: return (val == 0);
          case NE: return (val != 0);
          default: assert false;
        }
      }
      break;
      
      case CHAR:
      {
        int val = 0;
        if (op != Op.LIKE)
        {
            val = TupleBase.cCompare(cValueGet(col), 
                           cLengthGet(col), 
                           other.cValueGet(col2), 
                           other.cLengthGet(col2));
        }
        switch(op)
        {
          case LT: return (val < 0);
          case LE: return (val <= 0);
          case GT: return (val > 0);
          case GE: return (val >= 0);
          case EQ: return (val == 0);
          case NE: return (val != 0);
          case LIKE:  //CHR_LIKE
          {
            int len = cLengthGet(col);
            if(len == 0)
              return false;
         
            assert pattern != null;
            Scratch buf = scratchBuf.get();
            StringBuilder charSeq = buf.charSeq;
            charSeq.setLength(len);
         
            char[] c1 = cValueGet(col);
            for(int i=0; i<len; i++)
            {
              charSeq.setCharAt(i, c1[i]);
            }
        
            Matcher matcher = pattern.matcher(charSeq);
            return (matcher.find(0));
          }  
          default: assert false;
        }
      }
      break;
      case INTERVAL:
      {
        long val1, val2;
        val1 = vValueGet(col);
        val2 = other.vValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      case INTERVALYM:
      {
        long val1, val2;
        val1 = vymValueGet(col);
        val2 = other.vymValueGet(col2);
        switch(op)
        {
          case LT: return (val1 < val2);
          case LE: return (val1 <= val2);
          case GT: return (val1 > val2);
          case GE: return (val1 >= val2);
          case EQ: return (val1 == val2);
          case NE: return (val1 != val2);
          default: assert false;
        }
      }
      break;
      case BOOLEAN:
      {
        if (op == Op.EQ)
        {
          boolean val1 = boolValueGet(col);
          boolean val2 = other.boolValueGet(col2);
          return (val1 == val2);
        } else if (op == Op.NE)
        {
          boolean val1 = boolValueGet(col);
          boolean val2 = other.boolValueGet(col2);
          return (val1 != val2);
        }
        byte[] arr1 = bValueGet(col);;
        int bpos = bit/(Constants.BITS_PER_BYTE);
        assert arr1.length >= bpos;
        boolean val1 = ((arr1[bpos] & (1 << (bit % Constants.BITS_PER_BYTE))) != 0);
        boolean val2 = false;
        if (op != Op.NOT)
        {
          byte[] arr2 = other.bValueGet(col2); 
          bpos = bit2/(Constants.BITS_PER_BYTE);
          assert arr2.length >= bpos;
          val2 = ((arr2[bpos] & (1 << (bit2 % Constants.BITS_PER_BYTE))) != 0);
        }
        switch (op)
        {
          case AND: return (val1 && val2);
          case OR: return (val1 || val2);
          case NOT: return (!val1);
          case XOR: return (val1 || val2) && (!(val1 && val2));
          default: assert false;
        }
      }
      break;
      
      case XMLTYPE:
      {
        char[] xval1 =  xValueGet(col);
        char[] xval2 =  other.xValueGet(col2);
       
        boolean same;
        
        switch(op)
        {
          case EQ:
          {
          
            // Due to the way in which a XmltypeAttributeValue and a 
            // XmltypeAttrVal is implemented , 
            // the character array sizes may differ along with the total 
            // contents. We only need to compare the character arrays
            // till their specified length.
            int length1 = xIsObj(col) ? xval1.length : xLengthGet(col);
            int length2 = other.xIsObj(col2) ? xval2.length : other.xLengthGet(col2);
            
            int val = TupleBase.cCompare(xval1, length1, 
                               xval2, length2);
            
            same = val == 0;
            
            return same;
          }
          default:
            assert false;
        }
        
        break;
      }
     
           
    }
    return true;
  }

  public int heval(Datatype dtype, int col, int hash) throws ExecException
  {
    if (!isAttrNull(col))
    {
      switch (dtype.getKind())
      {
        case INT:
          hash = ((hash << 5) + hash) + TupleBase.inthash(iValueGet(col));
          break;

        case BIGINT:
          hash = ((hash << 5) + hash) + TupleBase.longhash(lValueGet(col));
          break;

        case BYTE:
          hash = ((hash << 5) + hash) + (bValueGet(col))[0];
          break;

        case CHAR:
          char cptr[] = cValueGet(col);
          int clen = cLengthGet(col);
          for (int j = 0; j < clen; j++)
            hash = ((hash << 5) + hash) + cptr[j];
          break;

        case FLOAT:
          hash = ((hash << 5) + hash) + TupleBase.inthash((int)fValueGet(col));
          break;
          
        case DOUBLE:
          hash = ((hash << 5) + hash) + TupleBase.inthash((int)dValueGet(col));
          break;
          
        case BIGDECIMAL:
          hash = ((hash << 5) + hash)
               + TupleBase.inthash((int)(nValueGet(col).longValue()));
          break;

        case TIMESTAMP:
          hash = ((hash << 5) + hash) + TupleBase.inthash((int)tValueGet(col));
          break;
          
        case INTERVAL:
          hash = ((hash << 5) + hash) + TupleBase.longhash(vValueGet(col));
          break;
          
        case INTERVALYM:
          hash = ((hash << 5) + hash) + TupleBase.longhash(vymValueGet(col));
          break;
         
        case BOOLEAN:
          hash = ((hash << 5) + hash) +  TupleBase.inthash(boolValueGet(col) ? 1 : 0);
          break;

        case OBJECT:
          hash = ((hash << 5) + hash);
          Object oval = oValueGet(col);
          if (oval != null)
          {
            hash += oval.hashCode();
          }
          break;
               
        default:
          assert false;
      }
    }
    return hash;
  }
  
  public void aeval(Datatype type, Op op, 
        int dcol,
        ITuple s1, int col1, 
        ITuple s2, int col2) 
    throws ExecException
    {
      TuplePtr src1 = (TuplePtr) s1;
      TuplePtr src2 = null;
      if (op == Op.NULL_CPY)
      {
        setAttrNull(dcol);
        return;
      }
      
      //SYSTIME OP has no arguments
      if(op == Op.SYSTIME)
      {
        // Presently As we are dealing with millisecond time value;
        // Result value is set as current system time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        tValueSet(dcol, currentTimeMillis * 1000000l);
        tFormatSet(dcol, TimestampFormat.getDefault());
        return;
      }

      if (op.args == 1)
      {
        if (op.nullType != NullType.NOOP && src1.isAttrNull(col1))
        {
          setAttrNull(dcol);
          return;
        }
        // SYSTIMEWITHTZ represents systimestamp with time zone
        // It accepts timezone as an argument
        if(op == Op.SYSTIMEWITHTZ)
        {          
          // Negative Case Handling: SELECT SYSTIMESTAMP(null) FROM INPUT;
      	  if(src1.isAttrNull(col1))
      	  {
            LogUtil.warning(LoggerType.TRACE,"specified timezone value is null");
            throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, "null");
          }
      	  char[] val1 =  src1.cValueGet(col1);
      	  ZoneId zid = null;
      	  
      	  // check if a valid time zone is provided, else throw exception
      	  try
      	  {
            zid = ZoneId.of(String.valueOf(val1));
          }
      	  catch (DateTimeException e) 
      	  {
            LogUtil.warning(LoggerType.TRACE, e.toString());
            throw new SoftExecException(ExecutionError.INVALID_TIME_ZONE_ID, String.valueOf(val1));
          }
      	  
          long currentTimeMillis = System.currentTimeMillis();        
          tValueSet(dcol, currentTimeMillis * 1000000l);
          tFormatSet(dcol, TimestampFormat.getTimestampWithTz(TimeZone.getTimeZone(zid)));
          return;
        }
      }
            
      
      if (op.args == 2)
      {
        // prepare the second attr for binary op
        src2 = (TuplePtr) s2;
        switch (op.nullType)
        {
          case ANY:
            if (src1.isAttrNull(col1) || src2.isAttrNull(col2))
            {
              setAttrNull(dcol);
              return;
            }
            break;
  
          case BOTH:
            if (src1.isAttrNull(col1) && src2.isAttrNull(col2))
            {
              setAttrNull(dcol);
              return;
            }
            break;
        }  
      }

      // evaluate
      switch(type.getKind())
      {
        case INT:
        {
          int val1 = 0, val2 = 0, dval = 0;
          if (op != Op.CLEN && op != Op.BLEN)
          {
            if (!src1.isAttrNull(col1)) val1 = src1.iValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.iValueGet(col2);
          }
          switch(op)
          {
            case ADD: //INT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //INT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //INT_DIV
              if(val2 == 0)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //INT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL: //INT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INT_CPY
              dval = val1;
              break;
              
            case TO_BIGINT: //INT_TO_BIGINT
              lValueSet(dcol, (long) val1);
              return;

            case TO_BOOLEAN: // INT_TO_BOOLEAN
              boolValueSet(dcol, (val1 != 0));
              return;

            case TO_FLT:   //INT_TO_FLT
              fValueSet(dcol, (float) val1);
              return;
              
            case TO_DBL: //INT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //INT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol,val, val.precision(), val.scale());
              return;
              
            case TO_CHR1: //INT_TO_CHAR
              Scratch buf = scratchBuf.get();
              // Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;              
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              tmpData.append(val1);
              // Read content from tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;              
              
              
            case UMX:   //INT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;

            case AVG:   //INT_AVG
              float fdval;
              if (src1.isAttrNull(col1))
              {
                fdval = (float) val2;
              } else if (src2.isAttrNull(col2))
              {
                fdval = (float) val1;
              } else
              {
                fdval = 
                  (float) (1.0 * val1) / (float) (1.0 * val2);
              }
              fValueSet(dcol, fdval);
              return;
              
            case CLEN:  //CHR_LEN
              // if argument to length(..) is null; then it returns null
              // else if argument to length(..) is empty string; then it returns null
              // else it returns length of argument
              if (src1.isAttrNull(col1) || src1.cLengthGet(col1) == 0){
                setAttrNull(dcol);
                return;
              }
              else
                dval = src1.cLengthGet(col1);
              break;


            case BLEN:  //BYTE_LEN
              // if argument to length(..) is null; then it returns null
              // else if argument to length(..) is empty string; then it returns null
              // else it returns length of argument
              if (src1.isAttrNull(col1) || src1.bLengthGet(col1) == 0) {
                setAttrNull(dcol);
                return;
              }
              else
                dval = src1.bLengthGet(col1);
              break;
              
            case MOD: // INT_MOD
              if(val2 == 0)
                dval = val1;
              else
                dval = val1 % val2;
              break;            

           default: 
              assert false;
          }
          iValueSet(dcol, dval);
        }
        break;

        case BIGINT:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.lValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.lValueGet(col2);
          switch(op)
          {
            case ADD: //BIGINT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //BIGINT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //BIGINT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //BIGINT_DIV
              if(val2 == 0l)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //BIGINT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //BIGINT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case NVL: //BIGINT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case TO_BOOLEAN: // INT_TO_BOOLEAN
              boolValueSet(dcol, (val1 != 0));
              return;

            case TO_FLT: //BIGINT_TO_FLT
              fValueSet(dcol, (float) val1);
              return;
              
            case TO_DBL: //BIGINT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //BIGINT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol,val, val.precision(), val.scale());
              return;
              
            case TO_CHR1: //BIGINT_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
              
            case CPY: //BIGINT_CPY
              dval = val1;
              break;

            case UMX:   //BIGIN_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //BIGINT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
          
            case MOD:  //BIGINT_MOD
              if(val2 == 0L)
                dval = val1;
              else
                dval = val1 % val2;
              break; 

            case AVG:   //BIGINT_AVG
              int ival2 = src2.iValueGet(col2);
              double ddval;
              if (src1.isAttrNull(col1))
              {
                ddval = (double) ival2;
              } else if (src2.isAttrNull(col2))
              {
                ddval = (double) val1;
              } else
              {
                ddval = 
                  (double) (1.0 * val1) / (double) (1.0 * ival2);
              }
              dValueSet(dcol, ddval);
              return;
              
            default: 
              assert false;
          }
          lValueSet(dcol, dval);
        }
        break;

        case FLOAT:
        {
          float val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.floatValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.floatValueGet(col2);
          switch(op)
          {
            case ADD: //FLT_ADD
              dval = val1 + val2; 
              break;
            case SUB: //FLT_SUB
              dval = val1 - val2; 
              break;
            case MUL: //FLT_MUL
              dval = val1 * val2; 
              break;
            case DIV: //FLT_DIV
              if(val2 == 0.0f)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
            case SUM_ADD: //FLT_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //FLT_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case TO_DBL: //FLT_TO_DBL
              dValueSet(dcol, (double) val1);
              return;
              
            case TO_BIGDECIMAL: //FLT_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol,val, val.precision(), val.scale());
              return;
             
            case TO_CHR1: //FLT_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return; 
              
            case NVL: //FLT_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //FLT_CPY
              dval = val1;
              break;
              
            case UMX:   //FLT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //FLT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case AVG:   //FLT_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                dval = (float) ival2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = val1 / (float) (1.0 * ival2);
              }
              break;
              
            case MOD:  //FLOAT_MOD
              if(val2 == 0.0f)
                dval = val1;
              else
                dval = val1 % val2;
              break; 

            default: 
              assert false;
          }
          fValueSet(dcol, dval);
        }
        break;
  
        case DOUBLE:
        {
          double val1 = 0, val2 = 0, dval = 0;
          if (!src1.isAttrNull(col1))
            val1 = src1.doubleValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.doubleValueGet(col2);
          switch(op)
          {
            case ADD: //DBL_ADD
              dval = val1 + val2; 
              break;
            case SUB: //DBL_SUB
              dval = val1 - val2; 
              break;
            case MUL: //DBL_MUL
              dval = val1 * val2; 
              break;
            case DIV: //DBL_DIV
              if(val2 == 0d)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              dval = val1 / val2; 
              break;
              
            case SUM_ADD: //DBL_SUM_ADD
              /*INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //DBL_SUM_SUB
              /*INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              x, if x is non null and y is null
              y, if x is null and y is non null
              null, if x and y are null */
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
              
            case TO_BIGDECIMAL: //DBL_TO_BIGDECIMAL
              BigDecimal val = new BigDecimal(String.valueOf(val1));
              nValueSet(dcol,val, val.precision(), val.scale());
              return;
            
            case TO_CHR1: //DBL_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
              
            case NVL: //DBL_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //DBL_CPY
              dval = val1;
              break;
              
            case UMX:   //DBL_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //DBL_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
              
            case AVG:   //DBL_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                dval = (double) ival2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = val1 / (double) (1.0 * ival2);
              }
              break;
              
            case MOD: //DOUBLE_MOD
              if(val2 == 0d)
                dval = val1;
              else
                dval = val1 % val2;
              break; 
      
            default: 
              assert false;
          }
          dValueSet(dcol, dval);
        }
        break;

        case BIGDECIMAL:
        {
          BigDecimal val1 = BigDecimal.ZERO, val2 = BigDecimal.ZERO,
          nval = BigDecimal.ZERO;
          if (!src1.isAttrNull(col1))
            val1 = src1.bigDecimalValueGet(col1);
          if (src2 != null && op != Op.AVG && !src2.isAttrNull(col2))
            val2 = src2.bigDecimalValueGet(col2);
          switch (op)
          {
            case ADD: // BIGDECIMAL_ADD
              nval = val1.add(val2);
              break;
            case SUB: // BIGDECIMAL_SUB
              nval = val1.subtract(val2);
              break;
            case MUL: // BIGDECIMAL_MUL
              nval = val1.multiply(val2);
              break;
            case DIV: // BIGDECIMAL_DIV
              if(val2.compareTo(BigDecimal.ZERO) == 0)
                throw new ExecException(ExecutionError.DIVIDE_BY_ZERO);
              
              nval = val1.divide(val2, RoundingMode.HALF_UP);
              break;
            case SUM_ADD: // BIGDECIMAL_SUM_ADD
              /*
               * BIGDECIMAL_SUM_ADD(x,y) = x + y if x is non null and y is non null 
               * = x, if x is non null and y is null 
               * = y, if x is null and y is non null
               * = null, if x and y are null
               */
              if (!src1.isAttrNull(col1) && ! src2.isAttrNull(col2))
                nval = val1.add(val2);
              else if (!src1.isAttrNull(col1))
                nval = val1;
              else if (!src2.isAttrNull(col2))
                nval = val2;
              break;
              
            case SUM_SUB: // BIGDECIMAL_SUM_SUB
              /*
               * BIGDECIMAL_SUM_ADD(x,y) = x - y if x is non null and y is non null
               *  x, if x is non null and y is null 
               *  y, if x is null and y is non null
               * null, if x and y are null
               */
              if (!src1.isAttrNull(col1) && ! src2.isAttrNull(col2))
                nval = val1.subtract(val2);
              else if (!src1.isAttrNull(col1))
                nval = val1;
              else
                assert (false);
              break;
              
            case TO_BIGDECIMAL: //BIGDECIMAL_TO_BIGDECIMAL
              nValueSet(dcol,val1, val1.precision(), val1.scale());
              return;
              
            case TO_CHR1: //BIGDECIMAL_TO_CHR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              tmpData.append(val1);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());             
              return;
              
            case NVL: // BIGDECIMAL_NVL
              nval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: // BIGDECIMAL_CPY
              nval = val1;
              break;
              
            case UMX: // BIGDECIMAL_UMX
              if (src1.isAttrNull(col1))
              {
                nval = val2;
              }
              else if (src2.isAttrNull(col2))
              {
                nval = val1;
              }
              else
              {
                nval = (val1.compareTo(val2) < 0) ? val2 : val1;
              }
              break;
              
            case UMN: // BIGDECIMAL_UMN
              if (src1.isAttrNull(col1))
              {
                nval = val2;
              }
              else if (src2.isAttrNull(col2))
              {
                nval = val1;
              }
              else
              {
                nval = (val1.compareTo(val2) > 0) ? val2 : val1;
              }
              break;
              
            case AVG: // BIGDECIMAL_AVG
              int ival2 = src2.iValueGet(col2);
              if (src1.isAttrNull(col1))
              {
                nval = new BigDecimal(ival2);
              }
              else if (src2.isAttrNull(col2))
              {
                nval = val1;
              }
              else
              {
                nval = val1.divide(new BigDecimal(ival2), RoundingMode.HALF_UP);
              }
              break;
              
            case MOD: //BIGDECIMAL_MOD
              if(val2.compareTo(BigDecimal.ZERO) == 0)
                nval = val1;
              else
                nval = val1.remainder(val2);
              break; 
              
            default:
              assert false;
          }
          nValueSet(dcol, nval, 
                    nval.precision(),
                    nval.scale());
        }
        break;
        
        case CHAR:
        {
          char[] val1 = null;
          int len1 = 0;
          char[] val2 = null;
          int len2 = 0;
          boolean destIsChar = true;
          
          if (op != Op.BYT_TO_HEX && !src1.isAttrNull(col1))
          {
            val1 = src1.cValueGet(col1);
            len1 = src1.cLengthGet(col1);
          }
          if (src2 != null && !src2.isAttrNull(col2))
          {
            val2 = src2.cValueGet(col2);
            len2 = src2.cLengthGet(col2);
          }
          char[] dval = null;
          int dlen = 0;
          Scratch buf = scratchBuf.get();
          char[] tempArray = buf.charBuf;
          int i = 0;
          int j = 0;
          switch(op)
          {
            case CPY: //CHAR_CPY
              dval = val1;
              dlen = len1;
              break;
              
            case LOWER: // CHR_LOWER
              for(i=0; i<len1; i++)
                tempArray[i] = Character.toLowerCase(val1[i]);
              dval = tempArray;
              dlen = len1;
              break;
            case UPPER: // CHR_UPPER
              for(i=0; i<len1; i++)
                tempArray[i] = Character.toUpperCase(val1[i]);
              dval = tempArray;
              dlen = len1;
              break;
            case INITCAP:
              boolean isConvertible = true;
              for(i=0; i < len1; i++)
              {
                if(isConvertible && Character.isLetterOrDigit(val1[i]))
                {
                  tempArray[i] = Character.toUpperCase(val1[i]);
                  isConvertible = false;
                }
                else
                {
                  tempArray[i] = Character.toLowerCase(val1[i]);
                  if(!Character.isLetterOrDigit(val1[i]))
                    isConvertible = true;
                }
              }
              dval = tempArray;
              dlen = len1;
              break;
            case LTRIM1:
            case RTRIM1:
              if(op == Op.LTRIM1)
              {
                for(i=0; i < len1 && Character.getType(val1[i]) == 
                  Character.SPACE_SEPARATOR ; i++);
                System.arraycopy(val1, i, tempArray, 0, len1-i);
                dlen = len1-i;
              }
              else if(op == Op.RTRIM1)
              {
                for(i=len1-1; i >= 0 && Character.getType(val1[i]) == 
                  Character.SPACE_SEPARATOR; i--);
                System.arraycopy(val1, 0, tempArray, 0, i+1);
                dlen = i+1;
              }
              dval = tempArray;
              break;
            case LTRIM2:
            case RTRIM2:
              LinkedHashSet<Character> set = buf.charLinkedHashSet;
              for(i = 0; i < len2; i++)
                set.add(val2[i]);
              if(op == Op.LTRIM2)
              {
                for(j = 0; j < len1 && set.contains(val1[j]); j++);
                System.arraycopy(val1, j,tempArray, 0, len1-j);
                dlen = len1-j;
              }
              else if(op == Op.RTRIM2)
              {
                for(j = len1-1 ; j >= 0 && set.contains(val1[j]); j--);
                System.arraycopy(val1, 0, tempArray, 0, j+1);
                dlen = j+1;
              }
              dval = tempArray;
                
              break;
            case SUBSTR:
              i = src2.iValueGet(col2);
              if( i <= len1 && i > 0)
              {
                int attr2val = i;
                System.arraycopy(val1, attr2val-1 , tempArray, 0, len1 - attr2val + 1 );
                dval = tempArray;
                dlen = len1 - attr2val + 1;
              }
              break;
            case LPAD:
            case RPAD:
              i = src2.iValueGet(col2);
              if(i > 0)
              {
                int attr2val     = i;
                final char BLANK = ' '; 
                
                if(attr2val <= len1)
                  System.arraycopy(val1, 0, tempArray, 0, attr2val);
                else
                {
                  if(op == Op.LPAD)
                  {
                    for(i=0; i < attr2val -len1; i++)
                      tempArray[i] = BLANK;
                    System.arraycopy(val1, 0, tempArray, i, len1);
                  }
                  else if(op == Op.RPAD)
                  {
                    System.arraycopy(val1, 0, tempArray, 0, len1);
                    for(i= len1; i < attr2val; i++)
                      tempArray[i] = BLANK;
                  } 
                }
                dval = tempArray;
                dlen = attr2val;
              }
              break;

            case CONCAT:        //CHR_CONCAT
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                for (j = 0; j < len1; j++)
                  tempArray[j] = val1[j];
                for (j = 0; j < len2; j++)
                  tempArray[j + len1] = val2[j];
                dval = tempArray;
                dlen = len1 + len2;
              }
              break;

            case NVL:   //CHR_NVL
              if(src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
              break;

            case BYT_TO_HEX:    //BYT_TO_HEX
             {
                byte[] bval1 = src1.bValueGet(col1);
                int blen1 = src1.bLengthGet(col1);
                try
                {
                  dval = Datatype.byteToHex( bval1, blen1);
                  dlen = dval.length;
                }
                catch(CEPException ce)
                {
                  throw new SoftExecException(ExecutionError.INVALID_ATTR, ce.getMessage());
                }
              }
              break;
              
            case UMX: // CHR_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              } else
              {
                String value1 = new String(val1);
                String value2 = new String(val2);
                if ((value1.compareTo(value2)) < 0)
                {
                  dval = val2;
                  dlen = len2;
                }
                else
                {
                  dval = val1;
                  dlen = len1;
                }
              }             
              break;
              
            case UMN: //CHR_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              } else
              {
                String value1 = new String(val1);
                String value2 = new String(val2);
                if ((value1.compareTo(value2)) > 0)
                {
                  dval = val2;
                  dlen = len2;
                }
                else
                {
                  dval = val1;
                  dlen = len1;
                }
              }             
              break;

            /**
             * Character to Number conversions 
             */
            case TO_INT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Integer dvalue = null;
                try
                {
                 dvalue = Integer.parseInt(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "integer");
                }
                iValueSet(dcol, dvalue);
                
                destIsChar = false;
              }
              break;
              
            case TO_BIGINT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Long dvalue = null;
                try
                {
                 dvalue = Long.parseLong(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, "bigint");
                }
                lValueSet(dcol, dvalue);
                
                destIsChar = false;
              }
              break;
            case TO_FLT:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Float dvalue = null;
                try
                {
                 dvalue = Float.parseFloat(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "float");
                }
                fValueSet(dcol, dvalue);
                
                destIsChar = false;
              }
              break;
            case TO_DBL:
              if (val1 != null)
              {
                String value1 = new String(val1);
                Double dvalue = null;
                try
                {
                 dvalue = Double.parseDouble(value1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, value1,
                      "double");
                }
                dValueSet(dcol, dvalue);
                
                destIsChar = false;
              }
              break;
            case TO_BIGDECIMAL:
              if (val1 != null)
              {
                BigDecimal dvalue = null;
                try
                {
                 dvalue = new BigDecimal(val1);
                }
                catch(NumberFormatException e)
                {
                  LogUtil.fine(LoggerType.TRACE, e.toString());
                  throw new SoftExecException(ExecutionError.INVALID_NUMBER, 
                      new String(val1), "bigdecimal");
                }
                nValueSet(dcol, dvalue, dvalue.precision(), dvalue.scale());
                
                destIsChar = false;
              }
              break;
                
              
            default:
              assert false;
          }
          if(destIsChar)
            cValueSet(dcol, dval, dlen);
        }
        break;
        
        case BYTE:
        {
          byte[] val1 = null;
          int len1 = 0;
          byte[] val2 = null;
          int len2 = 0;
          if (op != Op.HEX_TO_BYT && !src1.isAttrNull(col1))
          {
            val1 = src1.bValueGet(col1);
            len1 = src1.bLengthGet(col1);
          }
          if (src2 != null && !src2.isAttrNull(col2))
          {
            val2 = src2.bValueGet(col2);
            len2 = src2.bLengthGet(col2);
          }
          byte[] dval = null;
          int dlen = 0;
          switch(op)
          {
            case CPY:   //BYT_CPY
              dval = val1;
              dlen = len1;
              break;
              
            case NVL:   //BYT_NVL
              if(src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else
              {
                dval = val1;
                dlen = len1;
              }
              break;

            case CONCAT:        //BYT_CONCAT
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              }
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                Scratch buf = scratchBuf.get();
                byte[] btmpArray = buf.byteBuf;
                for (int j = 0; j < len1; j++)
                  btmpArray[j] = val1[j];
                for (int j = 0; j < len2; j++)
                  btmpArray[j + len1] = val2[j];
                dval = btmpArray;
                dlen = len1 + len2;
              }
              break;

            case HEX_TO_BYT:    //HEX_TO_BYT
              char[] cval1 = src1.cValueGet(col1);
              int clen1 = src1.cLengthGet(col1);
              //TODO: this might lead to loss of exception information
              //possible design change required in exceptions
              try
              {
                dval = Datatype.hexToByte( cval1, clen1);
                dlen = dval.length;
              }
              catch(CEPException e)
              {
                throw new ExecException(ExecutionError.INVALID_ATTR, e.getMessage());
              }
                break;
              
            case  UMX: //BYT_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                int len = len1 < len2 ? len1 : len2;
                int i   = 0;
                for (i = 0; i < len; i++) {
                  if (val1[i] < val2[i]) 
                  {
                    dval = val2;
                    dlen = len2;
                    break;
                  } 
                  else if (val1[i] > val2[i])
                  {
                    dval = val1;
                    dlen = len1;
                    break;
                  }
                }
                if (i == len) // when val1==val2 after loop, value having higher length is bigger. 
                {
                  if (len1 > len2)
                  {
                    dval = val1;
                    dlen = len1;
                  }
                  else
                  {
                    dval = val2;
                    dlen = len2;
                  }
                }
              }         
              break;
              
            case  UMN: //BYT_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                dlen = len2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                dlen = len1;
              }
              else
              {
                int len = len1 < len2 ? len1 : len2;
                int i   = 0;
                for (i = 0; i < len; i++) {
                  if (val1[i] < val2[i]) 
                  {
                    dval = val1;
                    dlen = len1;
                    break;
                  } 
                  else if (val1[i] > val2[i])
                  {
                    dval = val2;
                    dlen = len2;
                    break;
                  }
                }
                if (i == len) // when val1==val2 after loop, value with shorter length is smaller. 
                {
                  if (len1 > len2)
                  {
                    dval = val2;
                    dlen = len2;
                  }
                  else
                  {
                    dval = val1;
                    dlen = len1;
                  }
                }
              }         
              break;

            default:
              assert false;
          }
          bValueSet(dcol, dval, dlen);
        }
        break;
        
        case TIMESTAMP:
        {
          long val1 = 0, val2 = 0, dval = 0;
          String destFormatString = null;
          
          TimestampFormat resultFormat = null;
          TimestampFormat attrFormat1 = null;
          TimestampFormat attrFormat2 = null;
          
          if (op != Op.TIM_ADD && op != Op.TO_TIMESTAMP && !src1.isAttrNull(col1))
          {
            val1 = src1.tValueGet(col1);
            attrFormat1 = src1.tFormatGet(col1);
          }
          if (src2 != null && op != Op.INTERVAL_ADD && op != Op.INTERVAL_SUB && !src2.isAttrNull(col2)
              && op != Op.INTERVALYM_ADD && op != Op.INTERVALYM_SUB && op != Op.TO_CHR2)
          {
            val2 = src2.tValueGet(col2);
            attrFormat2 = src2.tFormatGet(col2);
          }
          if(src2 != null && op == Op.TO_CHR2)
          {
            destFormatString = new String((src2.cValueGet(col2)));
          }
          
          switch(op)
          {
            case INTERVAL_ADD:  //TIM_INTERVAL_ADD
              val2 = src2.vValueGet(col2);
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 + val2;
              resultFormat = attrFormat1;
              break;
            case INTERVAL_SUB:  //TIM_INTERVAL_SUB
              val2 = src2.vValueGet(col2);
              // interval value is in the unit of nanoseconds
              //val2 = val2 / 1000000l;
              dval = val1 - val2;
              resultFormat = attrFormat1;
              break;
              
            case INTERVALYM_ADD:  //TIM_INTERVALYM_ADD
              {
                Long lval2 = src2.vymValueGet(col2);
                Calendar cal = Calendar.getInstance();
                
                // Convert timestamp value into millis (from nanos)
                long val1Millis = val1 / 1000000l;
                long val1Offset = val1 - val1Millis * 1000000l;
                cal.setTimeInMillis(val1Millis);
                
                // interval value is in the unit of months
                cal.add(Calendar.MONTH, lval2.intValue());
                long resultMilis = cal.getTimeInMillis();
                dval = resultMilis * 1000000l + val1Offset;
                resultFormat = attrFormat1;
              }
              break;
             
            case INTERVALYM_SUB:  //TIM_INTERVALYM_SUB
              {
                Long lVal2 = src2.vymValueGet(col2);                
                Calendar cal2 = Calendar.getInstance();
                
                // Convert timestamp value into millis (from nanos)
                long val1Millis = val1 / 1000000l;
                long val1Offset = val1 - val1Millis * 1000000l;
                cal2.setTimeInMillis(val1Millis);
                
                // interval value is in the unit of months
                cal2.add(Calendar.MONTH, 0-lVal2.intValue());
                long resultMillis = cal2.getTimeInMillis();                
                dval = resultMillis * 1000000l + val1Offset;
                resultFormat = attrFormat1;
              }
              break;
              
            case TIM_ADD:  //INTERVAL_TIM_ADD & INTERVALYM_TIM_ADD
              if(src1.getAttrType(col1) == Datatype.INTERVAL)
              {
                val1 = src1.vValueGet(col1);                
                dval = val1 + val2; 
                resultFormat = attrFormat2;
              }
              else 
              {
                assert src1.getAttrType(col1) == Datatype.INTERVALYM;
                Long lval1 = src1.vymValueGet(col1);
                Calendar cal1 = Calendar.getInstance();
                
                // convert timestamp into millis ( from nanos)
                long val2Millis = val2 / 1000000l;
                long val2Offset = val2 - val2Millis * 1000000l;
                cal1.setTimeInMillis(val2Millis);
                
                // add interval to timetamp
                cal1.add(Calendar.MONTH, lval1.intValue());
                
                long resultMillis = cal1.getTimeInMillis();
                dval = resultMillis * 1000000l + val2Offset;
                resultFormat = attrFormat2;
              }              
              break;              
              
            case NVL:   //TIM_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              resultFormat = src1.isAttrNull(col1) ? attrFormat2 : attrFormat1;
              break;
              
            case CPY:   //TIM_CPY
              dval = val1;
              resultFormat = attrFormat1;
              break;
              
            case UMX:   //TIM_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                resultFormat = attrFormat2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                resultFormat = attrFormat1;
              } 
              else
              {
                dval = (val1 < val2) ? val2 : val1;
                resultFormat = (val1 < val2) ? attrFormat2 : attrFormat1;
              }
              break;

            case UMN:   //TIM_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
                resultFormat = attrFormat2;
              } 
              else if (src2.isAttrNull(col2))
              {
                dval = val1;
                resultFormat = attrFormat1;
              } 
              else
              {
                dval = (val1 > val2) ? val2 : val1;
                resultFormat = (val1 > val2) ? attrFormat2 : attrFormat1;
              }
              break;
              
            case TO_BIGINT: //TIMESTAMP_TO_BIGINT
            	lValueSet(dcol, val1);
            	return;
              
            case TO_CHR1:
            case TO_CHR2://TIMESTAMP_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              CEPDateFormat sdf1 = CEPDateFormat.getInstance();
              //sdf1.setLenient(false); ??? what is this for? Lenient is supposed to be used with parsing.
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;            
              
              // Reason for synchronized SimpleDateFormat object sdf1:
              // sdf1 object is oracle.cep.common.Constant.TIMESTAMP_FORMAT
              // sdf1 will be referred by all threads during execution time.           
              String formattedOutStr = null;
              try
              {
                if(destFormatString != null)
                {
                  formattedOutStr = sdf1.format(val1, destFormatString);
                }
                else
                {
                  formattedOutStr = sdf1.format(val1, attrFormat1);
                }
              }
              catch(ParseException e)
              {
                throw new ExecException(ExecutionError.INVALID_TIMEFORMAT);
              }
              
              synchronized(sdf1)
              {
                tmpData.append(formattedOutStr);
              }
              
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          tValueSet(dcol, dval);
          tFormatSet(dcol, resultFormat);
        }
        break;
        
        case INTERVAL:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            if (!src1.isAttrNull(col1))
              val1 = src1.vValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.vValueGet(col2);
          }
          
          // If the second attribute is null, then destination format is 
          // equal to afirst attribute's format
          // Else set destination format to a default format with maximum
          // precisions
          IntervalFormat destinationFormat = null;
          if(src2 != null || op == Op.TIM_SUB)
          {
            try
            {
              destinationFormat 
                = new IntervalFormat(TimeUnit.DAY, TimeUnit.SECOND, 9, 9);              
            } 
            catch (CEPException e)
            {
              // We shouldn't reach here as we are creating destinationFormat
              // with valid parameters
              assert false;
            } 
          }
          else
            destinationFormat = src1.vFormatGet(col1);
          
          switch(op)
          {
            case ADD: //INTERVAL_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVAL_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = src1.tValueGet(col1);
              val2 = src2.tValueGet(col2);
              // Note: dval is in the unit of nanoseconds as val1 and vol2
              // represents timestamp values
              dval = val1 - val2;
              //TODO: Database doesn't allow subtraction between two timestamp
              // values if any of them is having format mentioned.
             
              break;
            /* Commenting this part as database does not support sum(interval)   
            case SUM_ADD: //INTERVAL_SUM_ADD
              //INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              //x, if x is non null and y is null
              //y, if x is null and y is non null
              //null, if x and y are null 
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVAL_SUM_SUB
              //INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              //x, if x is non null and y is null
              //y, if x is null and y is non null
              //null, if x and y are null 
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
            */ 
            case NVL:   //INTERVAL_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INTERVAL_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVAL_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVAL_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
           
            case TO_CHR1: //INTERVAL_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;

              // convert long interval value into String format
              String intervalStringValue = 
                oracle.cep.common.IntervalConverter.getDSInterval(
                  val1,
                  destinationFormat);
              
              tmpData.append(intervalStringValue);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          vValueSet(dcol, dval, destinationFormat);
        }
        break;

        case INTERVALYM:
        {
          long val1 = 0, val2 = 0, dval = 0;
          if (op != Op.TIM_SUB)
          {
            if (!src1.isAttrNull(col1))
              val1 = src1.vymValueGet(col1);
            if (src2 != null && !src2.isAttrNull(col2))
              val2 = src2.vymValueGet(col2);
          }
          
          // If the second attribute is not null or operation is TIM_SUB, then
          // set the destination format to default format with max precision
          // Else set the destination format to first attribute's format
          IntervalFormat destinationFormat = null;
          if(src2 != null || op == Op.TIM_SUB)
          {
            try
            {
              destinationFormat 
                = new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);            
            } 
            catch (CEPException e)
            {
              // this should be reachable as we are creating the destination
              // format with valid parameters
              assert false;
            }
          }
          else
            destinationFormat = src1.vFormatGet(col1);
          
          switch(op)
          {
            case ADD: //INTERVALYM_ADD
              dval = val1 + val2; 
              break;
            case SUB: //INTERVALYM_SUB
              dval = val1 - val2; 
              break;
            case TIM_SUB: //TIM_SUB
              val1 = src1.tValueGet(col1);
              val2 = src2.tValueGet(col2);
              dval = val1 - val2; 
              // Note: dval is in the unit of nanosecond as val1 and vol2
              // represents timestamp values
              // Convert it to number of months
              dval = dval / (30l*24l*3600l*1000000000l);
              break;
            /* Commenting this part as database does not support sum(interval)   
            case SUM_ADD: //INTERVAL_SUM_ADD
              //INT_SUM_ADD(x,y) = x + y if x is non null and y is non null
              //x, if x is non null and y is null
              //y, if x is null and y is non null
              //null, if x and y are null 
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                 dval = val1 + val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else if(!src2.isAttrNull(col2))
                dval = val2;
              break;
              
            case SUM_SUB: //INTERVAL_SUM_SUB
              //INT_SUM_SUB(x,y) = x - y if x is non null and y is non null
              //x, if x is non null and y is null
              //y, if x is null and y is non null
              //null, if x and y are null 
              if (!src1.isAttrNull(col1) && !src2.isAttrNull(col2))
                dval = val1 - val2;
              else if(!src1.isAttrNull(col1))
                dval = val1;
              else
                assert(false);
              break;
            */ 
            case NVL:   //INTERVALYM_NVL
              dval = src1.isAttrNull(col1) ? val2 : val1;
              break;
              
            case CPY: //INTERVALYM_CPY
              dval = val1;
              break;
              
            case UMX:   //INTERVALYM_UMX
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 < val2) ? val2 : val1;
              }
              break;

            case UMN:   //INTERVALYM_UMN
              if (src1.isAttrNull(col1))
              {
                dval = val2;
              } else if (src2.isAttrNull(col2))
              {
                dval = val1;
              } else
              {
                dval = (val1 > val2) ? val2 : val1;
              }
              break;
           
            case TO_CHR1: //INTERVALYM_TO_CHAR
              Scratch buf = scratchBuf.get();
              
              // Allocate and Cleaning temp Buffer
              StringBuilder tmpData = buf.charSeq;
              tmpData.delete(0, tmpData.length());
              
              char[] tmpArray = buf.charBuf;
              
              // convert long interval value into String format
              String intervalStringValue =
                oracle.cep.common.IntervalConverter.getYMInterval(
                  val1,
                  destinationFormat);
                      
              tmpData.append(intervalStringValue);
              //Read tmpData into tmpArray
              tmpData.getChars(0, tmpData.length(), tmpArray, 0);
              cValueSet(dcol, tmpArray, tmpData.length());            
              return;
              
            default: 
              assert false;
          }
          vymValueSet(dcol, dval, destinationFormat);
        }
        break;
        
        case XMLTYPE:
        {
          switch (op)
          {
            case CPY: // XMLTYPE_CPY
              if (src1.xIsObj(col1)) 
              {
                try
                {
                  Object xobj = src1.getItem(col1, null);
                  xValueSet(dcol, xobj);
                }
                catch (Exception e)
                {
                  LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
                }
              } 
              else
              {
                char[] dval = src1.xValueGet(col1);
                int dlen = src1.xLengthGet(col1);
                xValueSet(dcol, dval, dlen);
              }
              break;
              
            case TO_CHR1:// XMLTYPE_TO_CHR
              if (src1.xIsObj(col1)) 
              {
                try
                {
                  Object xobj = src1.getItem(col1, null);
                  cValueSet(dcol, ((ITuple)xobj).xValueGet(col1), 
                                  ((ITuple)xobj).xLengthGet(col1));
                }
                catch (Exception e)
                {
                  LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
                }
              } 
              else
              {
                char[] dval = src1.xValueGet(col1);
                int dlen = src1.xLengthGet(col1);
                cValueSet(dcol, dval, dlen);
              }
                      
              break;
            default:
              assert false;
          }
        }
          break;
          
        case OBJECT:
        {
          switch (op)
          {
            case CPY: // OBJ_CPY
              Object dval = src1.oValueGet(col1);
              oValueSet(dcol, dval);
              break;
            default:
              assert false;
          }
        }
        break;

        case BOOLEAN:
        {
          boolean dval = false;
          switch (op)
          {
            case NVL: // BOOLEAN_NVL
              dval = src1.isAttrNull(col1) ? src2.boolValueGet(col2)
                                           : src1.boolValueGet(col1);
              boolValueSet(dcol, dval);
              break;

            case CPY: // BOOLEAN_CPY
              dval = src1.boolValueGet(col1);
              boolValueSet(dcol, dval);
              break;

            default:
              assert false;
          }
        }
        break;
          
        default:
          assert false;
        break;
      }
  }



public boolean evict() throws ExecException {return false;}

  public synchronized void dump(IDumpContext dumper) 
  {
    TupleBase.dump(dumper, this);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeExternal(ObjectOutput out, IPersistenceContext ctx)
      throws IOException
  {
    writeExternal(out);
  }

  @Override
  public void readExternal(ObjectInput in, IPersistenceContext ctx)
      throws IOException, ClassNotFoundException
  {
    readExternal(in);    
  }

  @Override
  public void setRecovered(boolean flag)
  {
    // Do nothing for paged tuple. 
  }
  
  public boolean isRecovered()
  {
    return false;
  }

  @Override
  public long getTimestamp()
  {
    return Long.MIN_VALUE;
  }

  @Override
  public void setTimestamp(long ts)
  {
    // Do nothing for paged tuple    
  }

  @Override
  public void setLastRecovered(boolean flag)
  {
  }

  @Override
  public boolean isLastRecovered()
  {
    return false;
  }
  
  @Override
  public void setFirstRecovered(boolean flag)
  {   
  }
  
  @Override
  public boolean isFirstRecovered()
  {
    return false;
  }
}

