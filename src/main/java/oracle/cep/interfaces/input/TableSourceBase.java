/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/TableSourceBase.java /main/45 2015/02/06 15:09:31 sbishnoi Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Convenient base class implementation of the TableSource interface

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/20/15 - bug 20241138
    udeshmuk    07/23/13 - bug 16813624: use useMillisTs config param so that
                           system timestamped sources use
                           System.currentTimeMillis()
    udeshmuk    05/21/13 - bug 16820093 - make getCorrectsystemtime public
    sbishnoi    03/08/13 - fix heartbeat propagation for archiver sources
    vikshukl    07/30/12 - archived dimension relation
    sbishnoi    03/11/12 - fix archiver tuples system timetsamp value
    sbishnoi    10/03/11 - changing format to intervalformat
    sbishnoi    08/28/11 - adding support for interval year to month
    udeshmuk    05/12/11 - use currentTimeMilis for archived relation only
    udeshmuk    04/18/11 - temporarily use system.currentTimeMillis in
                           getCorrectSystemTime
    anasrini    03/21/11 - add getName
    hopark      10/30/09 - support large string
    sborah      07/03/09 - support for bigdecimal
    hopark      03/05/09 - add opaque type
    anasrini    05/08/09 - system timestamped source lineage
    sbishnoi    04/08/09 - setting TotalOrderingGuarantee to TRUE if
                           systemtimestamped input
    hopark      02/17/09 - support boolean as external datatype
    anasrini    02/13/09 - remove allowEnqueue, add hbtTimeoutReminder
    anasrini    02/13/09 - add supportsPushEmulation
    sbishnoi    01/28/09 - total ordering optimization
    udeshmuk    01/16/09 - ensure total ordering for system ts case
    hopark      12/04/08 - add toString
    hopark      10/15/08 - TupleValue refactoring
    hopark      10/09/08 - remove statics
    hopark      09/04/08 - fix timestamp order
    sbishnoi    07/29/08 - support for nanosecond
    mthatte     04/22/08 - removed isBnull from TupleValue cnstructor
    mthatte     04/02/08 - adding isderivedTS
    udeshmuk    02/28/08 - change to allocate tuple.
    udeshmuk    01/30/08 - support for double data type.
    sbishnoi    01/21/08 - make it public
    udeshmuk    01/17/08 - change in the way of setting timestamp of tuple
    udeshmuk    12/17/07 - add getHeartbeatTime method.
    najain      10/19/07 - add xmltype
    udeshmuk    11/22/07 - add isSystemTimestamped flag and
                           getters and setters for it and insertTimestamp() method.
    parujain    10/17/07 - cep-bam integration
    parujain    06/08/07 - support byte
    najain      03/12/07 - bug fix
    najain      02/08/07 - coverage
    hopark      12/06/06 - add bigint datatype
    najain      11/07/06 - add oldTs
    parujain    10/13/06 - Interval datatype
    anasrini    09/12/06 - Base class implementing TableSource
    anasrini    09/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/input/TableSourceBase.java /main/45 2015/02/06 15:09:31 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.interfaces.input;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.IntervalFormat;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.BigDecimalAttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.IntervalYMAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.service.ExecContext;

/**
 * Convenient base class implementation of the TableSource interface
 *
 * @since 1.0
 */

public abstract class TableSourceBase implements TableSource
{
  protected ExecContext execContext;

  /** The name of this TableSource */
  protected String name;
  
  /** Metadata of the attributes **/
  protected AttributeMetadata[] attrMetadata;
  
  /** Lengths of attributes */
  protected String[]   attrNames;

  /** Number of attributes */
  protected int        numAttrs;

  /** Tuple value to be returned */
  protected TupleValue tuple;

  /** oldest timestamp */
  protected long  oldTs;
  
  /** Whether stream or relation */
  protected boolean    isStream;
  
  /** is the source system timestamped */
  protected boolean     isSystemTimeStamped;
  
  /** does the source have a derived */
  protected boolean     isDerivedTimeStamped;
  
  /** is the source silent */
  protected boolean     isSilent;

  /** last assigned timestamp */
  protected long        lastAssigned = 0;
  
  /** 
   *  flag to check whether we should convert time-stamp unit
   *  from millisecond to nanosecond
   */
  protected boolean    convertTs;

  /** The execution operator that this corresponds to */
  protected ExecOpt    orderedExecOp;
  protected ExecOpt    unorderedExecOp;
  
  /** The actual source which is feeding data to this source */
  protected TableSource innerSource;

  private boolean isPropagateHeartbeatForUnorderedOperators;
  
  /** indicates whether the source is archived or not */
  private boolean isArchived;
  
  /** indicates whether the source is an archived relation or not */
  private boolean isDimension;

  /** a lock object wihch has to be acquired prior to sending a heartbeat
   *  in this queue source.
   */ 
  protected ReentrantLock hbtRequestLock;

  /** A flag to check if there are multiple table source objects exist for
    * a single stream. This will happen in the cases where we are joining
    * a partitioned stream with non-partitioned stream.
    * For each non-partitioned stream, there will be N table sources where
    * N is the degree of parallelism of the partitioned stream.
    */
  protected boolean hasMultileTableSourcesForOneStream;
 
  protected TableSourceBase(ExecContext ec) {
    execContext = ec;
    tuple = new TupleValue();
    oldTs = 0;    
    isStream = true;
    isSystemTimeStamped = false;
    isSilent = false;
    convertTs = true;
    innerSource = null;
    name = "";
    isArchived = false;
    isDimension = false;
    hbtRequestLock = new ReentrantLock();
  }

  protected TableSourceBase(ExecContext ec,TableSource innerSource)
  {
    this(ec);
    this.innerSource = innerSource;
    
  }
  
  public void setNumAttrs(int numAttrs)
  {
    this.numAttrs      = numAttrs;
    this.attrMetadata  = new AttributeMetadata[numAttrs];
    this.attrNames     = new String[numAttrs];
    
    if(innerSource != null)
      innerSource.setNumAttrs(numAttrs);
  }

  public void setAttrInfo(int attrPos, String attrName, 
                          AttributeMetadata attrMetadata)
  {    
    assert attrPos < numAttrs;

    this.attrMetadata[attrPos] = attrMetadata;
    
    // Set attribute name
    attrNames[attrPos] = attrName;
    
    if(innerSource != null)
      innerSource.setAttrInfo(attrPos, attrName, attrMetadata);
  }
  
   
  public void setIsStream(boolean isStrm)
  {
    this.isStream = isStrm;
    if(innerSource != null)
      innerSource.setIsStream(isStrm);
  }

  public void start() throws CEPException
  {
    // Allocate tuple based on the schema
    allocateTuple();
  }

  public void end() throws CEPException 
  {
    orderedExecOp = null;
    unorderedExecOp = null;
  }

  public String getName()
  {
    return name;
  }

  public int getNumAttrs()
  {
    assert false;
    return 0;
  }

 /* public Datatype getAttrType(int pos)
  {
    assert false;
    return null;
  }

  public int getAttrLength(int pos)
  {
    assert false;
    return 0;
  }

  
  public int getAttrPrecision(int pos)
  {
    assert false;
    return 0;
  }
  
  public int getAttrScale(int pos)
  {
    assert false;
    return 0;
  }*/
  
  /**
   * Allocate output tuple
   * 
   * @throws CEPException
   */
  private void allocateTuple() throws CEPException
  {
    int i;
    AttributeValue[] attrval;

    // Allocate attributes
    attrval = new AttributeValue[numAttrs];
    for (i = 0; i < numAttrs; i++)
    {
      String attrName = attrNames[i];
      
      switch (attrMetadata[i].getDatatype().getKind())
      {
        case INT:
          attrval[i] = new IntAttributeValue(attrName, 0);
          break;
        case BIGINT:
          attrval[i] = new BigintAttributeValue(attrName, 0);
          break;
        case FLOAT:
          attrval[i] = new FloatAttributeValue(attrName, 0);
          break;
        case DOUBLE:
          attrval[i] = new DoubleAttributeValue(attrName, 0);
          break;
        case BIGDECIMAL:
          attrval[i] = new BigDecimalAttributeValue(attrName, BigDecimal.ZERO);
          break;
        case CHAR:
          attrval[i] = new CharAttributeValue(attrName, null);
          break;
        case XMLTYPE:
          attrval[i] = new XmltypeAttributeValue(attrName);
          break;
        case BYTE:
          attrval[i] = new ByteAttributeValue(attrName, null);
          break;
        case TIMESTAMP:
          attrval[i] = new TimestampAttributeValue(attrName, 0);
          break;
        case INTERVAL:
          attrval[i] = new IntervalAttributeValue(attrName, new String());
          IntervalFormat fmt = attrMetadata[i].getIntervalFormat();
          ((IntervalAttributeValue)attrval[i]).setFormat(fmt);
          break;
        case INTERVALYM:
          IntervalFormat ymfmt = attrMetadata[i].getIntervalFormat();
          attrval[i] = new IntervalYMAttributeValue(attrName, new String(), ymfmt);
          break;
        case OBJECT:
          attrval[i] = new ObjAttributeValue(attrName);
          break;
        case BOOLEAN:
          attrval[i] = new BooleanAttributeValue(attrName, false);
          break;
        default:
          throw new CEPException(InterfaceError.INVALID_ATTR_TYPE, 
            new Object[]{attrMetadata[i].getDatatype()});
      }
    }
    if (isSystemTimeStamped || isSilent)
    {
      tuple = new TupleValue(null, Constants.NULL_TIMESTAMP, attrval, false);
      tuple.setTotalOrderGuarantee(isSystemTimeStamped);
    }
    else
      tuple = new TupleValue(null, 0, attrval, false);

    // The tuples are positive by default
    tuple.setKind(TupleKind.PLUS);
  }

  public boolean isSystemTimeStamped()
  {
    return isSystemTimeStamped;
  }

  public void setSystemTimeStamped(boolean isSystemTimeStamped)
  {
    this.isSystemTimeStamped = isSystemTimeStamped;
  }
  
  public boolean isSilent()
  {
    return isSilent;
  }
  
  public void setSilent(boolean isSilent)
  {
    this.isSilent = isSilent;
  }
  
  public long getCorrectSystemTime()
  {
    // As System Time-stamp will have granularity of nanosecond time
    // We will use System.nanoTime() java method to get appropriate system time
    // except for archived relation case
    long currTs = System.nanoTime();
    
    //Archived sources would always use CurrentTimeMillis irrespective of the
    //config param setting. Review this strategy.
    if(this.isArchived || 
       execContext.getServiceManager().getConfigMgr().getUseMillisTs())
    {
      currTs = System.currentTimeMillis() * 1000000l;
    }
    
    synchronized(this)
    {
        //make sure monotonic increase
        //The VM can only use the time sources available to it. 
        //If CLOCK_MONOTONIC is not supported then there is no monotonic clock available for the VM to use. 
        //On such systems the VM won't comply with the nanoTime spec.
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6458294         
        
        lastAssigned = (currTs > lastAssigned) ? currTs : lastAssigned + 1;

        return lastAssigned;
    }
  }

  /**
   * Recalibrate time for given table source
   * @param time
   * @param updateLastAssignedTs flag to specify whether to update last assigned
   * timestamp.
   * @param updateLastAssignedTs
   * @return
   */
  protected synchronized boolean recalibrateTime(long time, 
                                                 boolean updateLastAssignedTs)
  {
    
    if (lastAssigned >= time)
      return false;
    
    if(updateLastAssignedTs)
      lastAssigned = time;
    
    return true;
  }
  
  public void insertTimestamp(TupleValue t)
  {
    if (t != null)
    {
      t.setTime(getCorrectSystemTime());
    }
  }
  
  public synchronized long getHeartbeatTime()
  {
    assert this.isSystemTimeStamped == true;
    return getCorrectSystemTime();
  }

  public boolean isDerivedTimeStamped()
  {
    return isDerivedTimeStamped;
  }

  public void setDerivedTimeStamped(boolean isDerivedTimeStamped)
  {
    this.isDerivedTimeStamped = isDerivedTimeStamped;
  }
 
  /**
   * Sets the flag to check whether we should convert time-stamp unit
   */
  public void setConvertTs(boolean convertTs) {
    this.convertTs = convertTs;
  }

  public void hbtTimeoutReminder() {}

  public void requestForHeartbeat(long hbtTime) {}

  /**
   * This method is called to get the instance of Lock object which is
   * required by input thread prior to start pushing heartbeat for a
   * particular input source object.
   */ 
  public Lock getRequestForHeartbeatLock()
  {
    return hbtRequestLock;
  }
 
  /**
   * Returns true if this table source belong to a stream which feeds input
   * events to multiple table sources
   * Return false otherwise;
   */
  public boolean hasMultileTableSourcesForOneStream()
  {
    return hasMultileTableSourcesForOneStream;
  }

  /**
   * Set the flag to true if this table source belong to a stream which feeds
   * input events to multiple table sources
   * Set the flag to false otherwise
   */
  public void setHasMultileTableSourcesForOneStream(boolean flag)
  {
    this.hasMultileTableSourcesForOneStream = flag;
  }

  /**
   * Setter for execution operator for which this is the source
   * @param op execution operator for which this is the source
   */
  public void setExecOp(ExecOpt op) 
  {
    orderedExecOp = op;
    if (op != null)
      name = op.getOptName();
  }
  
  public void setUnorderedExecOp(ExecOpt op)
  {
    unorderedExecOp = op;
    if (op != null)
      name = op.getOptName();
  }
  
  public ExecOpt getExecOp() 
  {
    return orderedExecOp;
  }

  protected ExecOpt getUnorderedExecOp() 
  {
    return unorderedExecOp; 
  }

  public TableSource getInnerSource()
  {
    return innerSource;
  }
  
  public void setPropagateHeartbeatforUnordered(boolean b)
  {
    isPropagateHeartbeatForUnorderedOperators = b;
  }
  
  protected boolean isPropagateHeartbeatforUnordered()
  {
    return isPropagateHeartbeatForUnorderedOperators;
  }

  /**
   * This method is used in REGRESSION testing to support push mode
   * emulation for pull sources
   */
  public boolean supportsPushEmulation()
  {
    return true;
  }
  
  protected String toString(String info)
  {
    StringBuilder sinfo = new StringBuilder();
    sinfo.append(info);
    sinfo.append(" : ");
    sinfo.append(isStream ? "stream":"relation");
    sinfo.append(" ( ");
    for (int i = 0; i < numAttrs; i++)
    {
      sinfo.append( attrNames[i] );
      sinfo.append(" ");
      sinfo.append(attrMetadata[i].getDatatype().name());
      if (i < (numAttrs-1))
        sinfo.append(", ");
    }
    sinfo.append(" ) ");
    if (isSilent) sinfo.append(" silent ");
    if (isSystemTimeStamped) sinfo.append(" derived ");
    if (isSystemTimeStamped) sinfo.append(" system timestamp ");
    if (convertTs) sinfo.append(" converted ");
    return sinfo.toString();
  }
  
  public void run()
  {
    
  }

  public void setIsArchived(boolean isArchived)
  {
    this.isArchived = isArchived;
  }
  
  public boolean isArchived()
  {
    return this.isArchived;
  }
    
  public void setIsDimension(boolean isDimension)
  {
    this.isDimension = isDimension;
  }
}

