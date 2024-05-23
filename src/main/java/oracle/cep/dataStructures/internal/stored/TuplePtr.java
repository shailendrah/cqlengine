/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/stored/TuplePtr.java /main/29 2009/11/21 07:38:16 hopark Exp $ */

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
   sborah      10/29/09 - support for bigdecimal
   hopark      05/18/09 - fix toString
   hopark      10/10/08 - remove statics
   hopark      05/16/08 - fix xmltuple copy
   hopark      02/27/08 - fix tuple serialization
   hopark      01/26/08 - fix pinned tuple
   hopark      12/27/07 - support xmllog
    hopark      12/04/07 - nodefac life cycle
    hopark      11/12/07 - set refcount
    hopark      11/02/07 - add serialVersionUID
    hopark      10/03/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/stored/TuplePtr.java /main/29 2009/11/21 07:38:16 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal.stored;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IPage;
import oracle.cep.memmgr.MemStat;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.memmgr.PageLayout;
import oracle.cep.memmgr.factory.stored.TupleFactory;
import oracle.cep.service.CEPManager;
import oracle.cep.util.DebugUtil;
import oracle.cep.dataStructures.internal.memory.TupleBase;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/stored/TuplePtr.java /main/29 2009/11/21 07:38:16 hopark Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id", "Hashcode"}, 
    attribVals={"getId", "hashCode"})
public class TuplePtr 
  extends oracle.cep.dataStructures.internal.paged.TuplePtr
  implements Externalizable
{
  /*
   * Why we need pinCount here?
   * We need the pincount for PageTuplePr in stored mode.
   * It's because tuplePtr can be shared.
   * e.g.
   *  The following code sequence will not work if tuplePtr1 == tuplePtr2
   *  tuplePtr1.pin       
   *     tuplePtr2.pin
   *  tuplePtr1.unpin
   *     tuplePtr.access : m_page is null and we will have a NullPointerException
   *     tuplePtr2.unpin
   *     
   *  Why we cannot remove unpinTuple?
   *  We cannot elminate unpin completely, because of circular reference in ListNode.
   *  e.g. If a tuplePtr is stored in a List, it will be stored in a page.
   *  TuplePtr -> Page -> back to TuplePtr
   *  So we need to clear the reference from TuplePtr to Page in order to let Page go.    
   */
  short m_pinCount;
  
  /**
   * Constructor for Tuple
   */
  public TuplePtr()
  {
    super();
    m_pinCount = 0;
  }
  
  public void writeExternal(ObjectOutput stream) throws IOException
  {
    stream.writeLong(m_id);
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      stream.writeInt(m_factory.getId());
      assert (m_pagePtr != null);
      int pageId = m_pagePtr.getPageId();
      stream.writeInt(pageId);
      stream.writeInt(m_index);
    }
    if (DebugUtil.DEBUG_PAGEDTUPLE_SERIALIZATION)
    {
      try
      {
      assert (m_pagePtr != null);
      int pageId = m_pagePtr.getPageId();
      stream.writeInt(pageId);
      stream.writeInt(m_index);
      m_page = m_pagePtr.peek();
      // write all tuple contents to verify on reading.
      short nAttribs = m_page.getNoTypes();
      stream.writeShort(nAttribs);
      byte[] types = m_page.getTypes();
      stream.writeObject(types);
      
      // Get the attributes
      for (short a = 0; a < types.length; a++)
      {
        stream.writeBoolean(isAttrNull(a));
        if (!isAttrNull(a))
        {
            int ival;
            long lval;
            float fval;
            double dval;
            byte[] bval;
            Object oval;
            switch (types[a])
            {
              case PageLayout.INT:
                ival = iValueGet(a);
                stream.writeInt(ival);
                break;
              case PageLayout.LONG:
                lval = lValueGet(a);
                stream.writeLong(lval);
                break;
              case PageLayout.FLOAT:
                fval = fValueGet(a);
                stream.writeFloat(fval);
                break;
              case PageLayout.DOUBLE:
                dval = dValueGet(a);
                stream.writeDouble(dval);
                break;
              case PageLayout.BIGDECIMAL:
                //CHECK !!!
                stream.writeObject(nValueGet(a));
                break;
              case PageLayout.VBYTE:
                ival = bLengthGet(a);
                stream.writeInt(ival);
                bval = bValueGet(a);
                stream.write(bval);
                break;
              case PageLayout.VCHAR:
                ival = cLengthGet(a);
                stream.writeInt(ival);
                String c = new String(cValueGet(a), 0, ival);
                stream.write(c.getBytes(), 0, c.length());
                break;
              case PageLayout.TIME:
                lval = tValueGet(a);
                stream.writeLong(lval);
                break;
              case PageLayout.OBJ:
                oval = oValueGet(a);
                stream.writeObject(oval);
                break;
              case PageLayout.INTERVAL:
                lval = vValueGet(a);
                stream.writeLong(lval);
                break;
              default:
                // Should not come
                assert false;
          }
        }
      }
      }
      catch (ExecException e)
      {
        
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput stream) throws IOException
  {
    m_id = stream.readLong();
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      int facId = stream.readInt();
      FactoryManager factoryMgr = CEPManager.getInstance().getFactoryManager();
      TupleFactory fac = (TupleFactory) factoryMgr.getFac(facId);
      m_factory = fac;
      int pageId = stream.readInt();
      m_index = stream.readInt();
      m_pagePtr = m_factory.getPage(pageId);
    }
    // Note m_page is null at this point.
    // The following pinTuple will set it properly through m_pagePtr.pin.
    if (DebugUtil.DEBUG_PAGEDTUPLE_SERIALIZATION)
    {
      int pageId = stream.readInt();
      m_index = stream.readInt();
      m_pagePtr = m_factory.getPage(pageId);
      assert (pageId == m_pagePtr.getPageId());
      
      try 
      {
      ITuple src = pinTuple(IPinnable.READ); 
      short nAttribs = m_page.getNoTypes();
      short onAttribs = stream.readShort();
      assert (nAttribs == onAttribs);
      byte[] types = m_page.getTypes();
      byte[] otypes = (byte[]) stream.readObject();
      assert (types.length == otypes.length);
      
      // Get the attributes
      for (short a = 0; a < nAttribs; a++)
      {
        assert (types[a] == otypes[a]);
        if (src.isAttrNull(a))
        {
            boolean b = stream.readBoolean();
            assert (b);
        }
        else
        {
            int ival;
            long lval;
            float fval;
            double dval;
            byte[] bval, bval2;
            char[] cval, cval2;
            Object oval, oval2;
            switch (types[a])
            {
              case PageLayout.INT:
                ival = stream.readInt();
                assert (ival == src.iValueGet(a));
                break;
              case PageLayout.LONG:
                lval = stream.readLong();
                assert (lval == src.lValueGet(a));
                break;
              case PageLayout.FLOAT:
                fval = stream.readFloat();
                assert (fval == src.fValueGet(a));
                break;
              case PageLayout.DOUBLE:
                dval = stream.readDouble();
                assert (dval == src.dValueGet(a));
                break;
              case PageLayout.BIGDECIMAL:
                oval = stream.readObject();
                oval2 = src.nValueGet(a);
                if (oval == null)
                  assert (oval == oval2);
                else 
                {
                  assert oval instanceof BigDecimal;  
                  assert (((BigDecimal)oval).compareTo((BigDecimal)oval2) == 0);
                }
                break;
              case PageLayout.VBYTE:
                ival = stream.readInt();
                assert (ival == src.bLengthGet(a));
                if (ival > 0)
                {
                  bval = new byte[ival];
                  java.util.Arrays.fill(bval, (byte) 0);
                  int len = stream.read(bval, 0, ival);
                  assert(len == ival);
                  bval2 = src.bValueGet(a);
                  for (int i = 0; i < ival; i++)
                  {
                   assert (bval[i] == bval2[i]);
                  }
                }
                break;
              case PageLayout.VCHAR:
                ival = stream.readInt();
                assert (ival == src.cLengthGet(a));
                if (ival > 0)
                {
                    bval = new byte[ival];
                    int len = stream.read(bval, 0, ival);
                    assert(len == ival);
                    String c = new String(bval, 0, ival);
                    cval = c.toCharArray();
                    cval2 = src.cValueGet(a);
                    for (int i = 0; i < ival; i++)
                    {
                      assert (cval[i] == cval2[i]);
                    }
                }
                break;
              case PageLayout.TIME:
                lval = stream.readLong();
                assert (lval == src.tValueGet(a));
                break;
              case PageLayout.OBJ:
                oval = stream.readObject();
                oval2 = src.oValueGet(a);
                if (oval == null)
                  assert (oval == oval2);
                else assert (oval.equals(oval2));
                break;
              case PageLayout.INTERVAL:
                lval = stream.readLong();
                assert (lval == src.vValueGet(a));
                break;
              default:
                // Should not come
                assert false;
            }
      } //for
      unpinTuple();
      } //try
      } catch (ClassNotFoundException ec)
      {
        assert false;
      } catch(ExecException e)
      {
        assert false;
      }
    }
  }
  
  
  public synchronized boolean evict() throws ExecException
  {
    boolean b = m_pagePtr.evict(m_factory.getPageManager());
    if (b)
    {
      m_page = null;
    }
    return b;
  }

  /**
   * Pins the StorageElement. If the object has swappend out, retreive it
   * from the storage and reset the referent.
   * @return
   */
  public synchronized ITuple pinTuple(int mode) throws ExecException
  {
    ++m_pinCount;
    if (m_pagePtr == null)
    {
      // The tuple body is not initialized.
      // Let base classs handle it.
      ITuple res = super.pinTuple(mode);
      assert (!m_pagePtr.isEmptySlot());
      return res;
    } 
    boolean hit = true;
    MemStat stat = m_factory.getStat();
    IPage peeked = m_pagePtr.peek();
    if (peeked == null)
    {
      // evicted tuple
      hit = false;
      m_page = m_pagePtr.pin(m_factory.getPageManager(), mode);
    } 
    else
    {
      m_page = peeked;
      if (mode != IPinnable.READ)
        m_pagePtr.setDirty(true);
    }
    if (DebugUtil.DEBUG_TUPLE_REFCOUNT)
    {
      if (m_page == null)
      {
        LogUtil.fine(LoggerType.TRACE, "*** Trying to pinTuple on deleted tuplePtr " + m_pagePtr.toString() );
        m_factory.dumpRefCount(getId());
        assert false : "trying to pinTuple on deleted tuplePtr " + m_pagePtr.toString();
      }
    }
    assert (m_page != null) : "failed to get page " + m_pagePtr.toString(); 
    stat.m_totalPinAccess++;
    if (hit) stat.m_totalPinHit++;
    return this;
  }

  public synchronized void unpinTuple() throws ExecException
  {
    //m_pagePtr.unpin(); unpin operation is removed from IPagePtr
    // we should not hold onto m_page after unpin.
    // Otherwise, the page will not be garbage collected.
    if (--m_pinCount == 0)
      m_page = null;
  }
  
  public boolean isTuplePinned() throws ExecException
  {
    return (m_pinCount > 0);
  }
  
  public void setDirtyTuple() throws ExecException
  {
    m_pagePtr.setDirty(true);
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    IPage peeked = m_pagePtr.peek();
    if (peeked != null)
    {
      super.dump(dumper);
    }
    else
    {
      String tag = LogUtil.beginDumpObj(dumper, this);
      LogUtil.endDumpObj(dumper, tag);
    }
  }
}
