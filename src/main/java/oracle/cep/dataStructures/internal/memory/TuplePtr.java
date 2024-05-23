/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

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
 udeshmuk    09/11/12 - add snapshotid
 hopark      04/09/09 - add copy
 hopark      02/18/09 - change copy api
 hopark      02/28/08 - fix tuple serialization
 hopark      12/26/07 - support xmllog
 hopark      12/06/07 - cleanup spill
 hopark      09/30/07 - setReferent api change
 hopark      08/28/07 - remove pin/unpin
 hopark      07/26/07 - use ITuple for DynTuple
 hopark      07/12/07 - add compare
 hopark      06/19/07 - cleanup
 hopark      05/28/07 - logging support
 hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
 najain      04/10/07 - 
 hopark      03/29/07 - added refcount debug facility
 hopark      03/21/07 - add pin
 najain      03/14/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.snapshot.IPersistenceContext;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;

/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/memory/TuplePtr.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 * @author hopark
 * @since release specific (what release of product did this appear in)
 */
@DumpDesc(attribTags={"Id"}, 
          attribVals={"getId"})
public class TuplePtr implements ITuplePtr
{
  protected ITuple         m_ref;
  
  /**
   * timestamp of the member ITuple.
   * It is necessary to use timestamp variable for persisting TuplePtr objects
   * which belongs to a window synopsis of range window.
   * Because range window synopsis saves records in window store with record's 
   * timestamp values. This is required in the algorithm of determining tuples in
   * range window.
   */
  protected long           timestamp;
  
  /** A Flag to mark whether this is a tuple recovered during snapshot load */
  protected boolean        isRecovered;
  
  /** A Flag to mark whether this is last recovered tuple during snapshot load*/
  protected boolean        isLastRecovered;
  
  /** A Flag to mark whether this is first recovered tuple during snapshot load*/
  protected boolean        isFirstRecovered;

  /** 
   * Empty Argument Constructor.
   * Used to create object instance while de-serialization
   */
  public TuplePtr()
  {
    isRecovered = false;
    isLastRecovered = false;
    isFirstRecovered = false;
    timestamp = Long.MIN_VALUE;
  }
  
  public TuplePtr(ITuple referent)
  {
    m_ref = referent;
    isRecovered = false;
    timestamp = Long.MIN_VALUE;
  }

  public final void clear()
  {
    m_ref = null;
    isRecovered = false;
  }

  /**
   * Peeks the StorageElement without retrieving
   * 
   * @return the Storageelement
   */
  public final ITuple peek()
  {
    return m_ref;
  }

  /**
   * Pins the tuple. If the tuple has swappend out, retreive it from
   * the storage and reset the referent.
   * 
   * @return
   */
  public ITuple pinTuple(int mode) throws ExecException
  {
    return m_ref;
  }

  public void unpinTuple() throws ExecException
  {
  }

  public boolean isTuplePinned() throws ExecException
  {
    return true;
  }
  
  public final void setDirtyTuple() throws ExecException
  {
  }
  
  public final void copy(ITuplePtr srcPtr, int numAttrs) throws ExecException
  {
    TuplePtr src = (TuplePtr) srcPtr;
    m_ref.copy(src.m_ref, numAttrs);
  }

  public final void copy(ITuplePtr srcPtr, int[] srcAttrs, int[] destAttrs) throws ExecException
  {
    TuplePtr src = (TuplePtr) srcPtr;
    m_ref.copy(src.m_ref, srcAttrs, destAttrs);
  }
  
  public long getId()
  {
    long id = 0;
    if (m_ref != null)
    	id = m_ref.getId();
    return id;
  }
  
  public long getSnapshotId()
  {
    if(m_ref != null)
      return m_ref.getSnapshotId();
    else
      return Long.MAX_VALUE;
  }
  
  public void setSnapshotId(long val)
  {
    if(m_ref != null)
      m_ref.setSnapshotId(val);
  }
  
  public boolean equals(Object other)
  {
    if (other instanceof TuplePtr)
    {
      TuplePtr e = (TuplePtr) other;
      return getId() == e.getId();
    }
    return false;
  }

  public final boolean compare(ITuplePtr srcPtr) throws ExecException
  {
    TuplePtr src = (TuplePtr) srcPtr;
    return m_ref.compare(src.m_ref);
  }
  
  public final boolean compare(ITuplePtr srcPtr, int[] skipPos) throws ExecException
  {
    TuplePtr src = (TuplePtr) srcPtr;
    return m_ref.compare(src.m_ref, skipPos);
  }
  
  @Override
  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    if (m_ref == null)
    {
      buff.append("tuple=");
      buff.append(getId());
      buff.append(" ref=null");
    }
    else
    {
      buff.append("tuple=");
      buff.append(m_ref.toString());
    }
    return buff.toString();
  }
  
  public boolean evict() throws ExecException {return false;}
  
  public synchronized void dump(IDumpContext dumper) 
  {
    String tag = LogUtil.beginDumpObj(dumper, this);
    if (m_ref != null)
      m_ref.dump(dumper);
    LogUtil.endDumpObj(dumper, tag);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(m_ref);
    out.writeLong(timestamp);
  }  
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    m_ref = (ITuple) in.readObject();
    timestamp = in.readLong();
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
    this.isRecovered = flag;
  }

  @Override
  public boolean isRecovered()
  {
    return this.isRecovered;
  }

  @Override
  public long getTimestamp()
  {
    return this.timestamp;
  }

  @Override
  public void setTimestamp(long ts)
  {
    this.timestamp = ts;    
  }

  @Override
  public void setLastRecovered(boolean flag)
  {
    this.isLastRecovered = flag;  
  }

  @Override
  public boolean isLastRecovered()
  {
    return this.isLastRecovered;
  }
  
  @Override
  public boolean isFirstRecovered()
  {
    return this.isFirstRecovered;
  }
  
  @Override
  public void setFirstRecovered(boolean flag)
  {
    this.isFirstRecovered = flag;
  }
  
}

