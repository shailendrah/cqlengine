/* $Header: pcbpel/cep/server/src/oracle/cep/jmx/stats/OperatorStatsRow.txt /main/3 2009/01/08 20:48:29 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/06/09 - phyopid
    parujain    11/12/08 - support runtime MBeans
    parujain    10/05/07 - ordering
    parujain    09/12/07 - remove logging
    hopark      07/13/07 - dump stack trace on exception
    parujain    05/31/07 - cep-em integration
    parujain    05/08/07 - add statistics
    najain      10/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/jmx/stats/OperatorStatsRow.txt /main/3 2009/01/08 20:48:29 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jmx.stats;

import java.beans.ConstructorProperties;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import oracle.cep.statistics.IStats;


/**
 * Statistic for the operator. This can be thought of a fixed table with the
 * following columns:
 *
 * ExecOperatorId       : Identifier of the execution operator
 * OutQueueId           : Identifier of the output queue of the operator
 * numOutMessages       : Total number of messages enqueued in the output queue
 * numInMessages        : Total number of messages received from all input queues
 * numExecutions        : Total number of executions
 * totalTime            : Total time spent in executions of this operator
 * startTime            : startTime of the last duration in which operator ran
 * endTime              : endTime of the last duration
 * numInMessagesLatest  : Total number of messages recieved in the last duration
 * numOutMessagesLatest : Total number of messages enqueued in the last duration
 * optTyp               : Type of the operator 
 * optName              : Name of the operator 
 * Percent              : Percent of total time spent in executing this operator
 *
 * @since 1.0
 */

public class OperatorStatsRow extends StatsRow implements IStats
{
  private int  execOperatorId;
  private int  phyOperatorId;
  private int  outQueueId;
  private long numOutMessages;
  private long numInMessages;
  private long numOutHbts;
  private long numInHbts;
  private long numExecutions;
  private long totalTime;
  private long startTime;
  private long endTime;
  private long numInMessagesLatest;
  private long numOutMessagesLatest;
  private String opttyp;
  private String optName;
  private float percent;
  private long cacheMiss;
  private long cacheHit;
  private String cacheName="";
  private boolean isSrcCached;
  long pstmtTTime,pstmtTExec;
  
  
  /** 
   * Construct a OperatorStatsRow instance. 
   * The PropertyNames annotation is used 
   * to be able to construct a OperatorStatsRow instance out of a CompositeData
   * instance. See MXBeans documentation for more details.
   */ 
  @ConstructorProperties({"operatorId", "phyOperatorId", "queueId", "numOutMessages", "numInMessages", "numOutHbts", "numInHbts", "numExecutions",
                          "totalTime", "startTime", "endTime", "numInMessagesLatest", "numOutMessagesLatest",
                          "opttype","optName","percent", "cacheMiss", "cacheHit", "cacheName","isSrcCached","pstmtTExec","pstmtTExec"})
  public OperatorStatsRow(int id, int phyId, int qid, long out, long in, long outHbts, long inHbts, long executions, long time,
                          long start, long end, long inLatest, long outLatest, String typ, 
                          String name, float per,long cmiss, long chit, String cname, boolean cached, long pstmtTTime, long pstmtTExec)
  {
    execOperatorId = id;
    phyOperatorId = phyId;
    outQueueId = qid;
    numOutMessages = out;
    numInMessages = in;
    numOutHbts = outHbts;
    numInHbts = inHbts;
    numExecutions = executions;
    totalTime = time;
    startTime = start;
    endTime = end;
    numInMessagesLatest = inLatest;
    numOutMessagesLatest = outLatest;
    opttyp = typ;
    optName = name;
    percent = per;
    cacheMiss = cmiss;
    cacheHit = chit;
    cacheName = cname;
    isSrcCached = cached;
    this.pstmtTTime  = pstmtTTime;
    this.pstmtTExec = pstmtTExec;    
  }
 
  public int getExecOperatorId()
  {
    return execOperatorId ;
  }
  
  public int getPhyOperatorId()
  {
    return phyOperatorId ;
  }
 
  public long getNumExecutions()
  {
    return numExecutions;
  }

  public long getNumInMessages()
  {
    return numInMessages ;
  }
  
  public long getNumOutMessages()
  {
    return numOutMessages ;
  }

  public long getNumOutHbts()
  {
    return numOutHbts;
  }

  public long getNumInHbts()
  {
    return numInHbts;
  }

  public int getOutQueueId()
  {
    return outQueueId;
  }
  
  public long getStartTime()
  {
    return startTime;
  }
  
  public long getEndTime()
  {
    return endTime ;
  }
  
  public long getTotalTime()
  {
    return totalTime ;
  }

  public long getNumInMessagesLatest()
  {
    return numInMessagesLatest ;
  }
  
  public long getNumOutMessagesLatest()
  {
    return numOutMessagesLatest ;
  }
  
  public String getOptName()
  {
    return optName;
  }

  public String getOpttyp()
  {
    return opttyp;
  }
  
  public float getPercent()
  {
    return percent;
  } 
  

	public long getCacheMiss() {
		return cacheMiss;
	}

	public long getCacheHit() {
		return cacheHit;
	}

	public String getCacheName() {
		return cacheName;
	}

	public boolean isSrcCached() {
		return isSrcCached;
	}	

	public long getPstmtTTime() {
		return pstmtTTime;
	}

	public long getPstmtTExec() {
		return pstmtTExec;
	}	

public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<OperatorStatsRow>");
    sb.append("<OperatorId id=\"" + execOperatorId + "\" >");
    sb.append("<PhyOperatorId id=\"" + phyOperatorId + "\" >");
    sb.append("<OperatorType typ=\"" + opttyp + "\" />");
    sb.append("<OperatorName name=\"" + optName + "\" />");
    sb.append("<OutputQueue id=\"" + outQueueId + "\" >");
    sb.append("<NumOutMessages =\"" + numOutMessages + "\" >");
    sb.append("<NumInMessages =\"" + numInMessages + "\" >");
    sb.append("<NumOutHbts =\"" + numOutHbts + "\" >");
    sb.append("<NumInHbts =\"" + numInHbts + "\" >");
    sb.append("<NumExecutions =\"" + numExecutions + "\" >");
    sb.append("<StartTime =\"" + startTime + "\" >");
    sb.append("<EndTime =\"" + endTime + "\" >");
    sb.append("<NumInMessagesLatest =\"" + numInMessagesLatest + "\" >");
    sb.append("<NumOutMessagesLatest =\"" + numOutMessagesLatest + "\" >");
    sb.append("<TotalTime =\"" + totalTime + "\" >");
    sb.append("<Percent =\"" + percent + "\" >");
    sb.append("<CacheMiss =\"" + cacheMiss + "\" >");
    sb.append("<CacheHit =\"" + cacheHit + "\" >");
    sb.append("<CacheName =\"" + cacheName + "\" >");
    sb.append("<SourceCached =\"" + isSrcCached + "\" >");
    sb.append("<PreparedStatementTotalRunningTime =\"" + pstmtTTime + "\" >");
    sb.append("<PreparedStatementQueryExecutions =\"" + pstmtTExec + "\" >");
    sb.append("</OperatorStatsRow>");

    return sb.toString();
  }
  
  /*
   * JDK 5.0 manual conversion methods. The code
   * below would not be required if we could
   * rely on the MXBean framework
   * 
   * THE REQUIRED METHODS ARE:
   *
   *  public static CompositeType toCompositeType();
   *  public CompositeData toCompositeData(CompositeType ct);
   *  public static OperatorStatsRow from(CompositeData cd);
   *
   

  // The corresponding CompositeType for this class
  private static CompositeType cType_= null;

  private static final String[] itemNames_=  {"operatorId", "phyOperatorId", "queueId", "numOutMessages", "numInMessages", 
                                              "numExecutions","totalTime", "startTime", "endTime",
                                              "numInMessagesLatest", "numOutMessagesLatest","opttype",
                                              "optName","percent" };
  
  static{
    try
    {
      OpenType[] itemTypes = {SimpleType.INTEGER,
                              SimpleType.INTEGER,
                              SimpleType.INTEGER,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.STRING,
                              SimpleType.STRING,
                              SimpleType.FLOAT
                             };
      
      cType_ = new CompositeType("oracle.cep.statistics.OperatorStatsRow",
                                 "oracle.cep.statistics.OperatorStatsRow",
                                 itemNames_,
                                 itemNames_,
                                 itemTypes);
    }
    catch(OpenDataException ode)
    {
      
    }
  }
  */
  /**
   * Returns the CompositeType that describes this model
   * specific class
   
  public static CompositeType toCompositeType() {
      return cType_;
  }

  /**
   * Convert an instance of this model specific type to 
   * a CompositeData. This ensure that clients that do not
   * have access to the model specific class can still
   * use the MBean. The MXBean framework can perform this
   * conversion automatically. However MXBeans are part of
   * JDK 6.0 and AS11g is required to support JDK 5.0 
   * 
   * @param ct - This parameter is there only for future compatibility reasons
   *             with JDK 6.0. It can be ignored at this point.
   *
   public CompositeData toCompositeData(CompositeType ct) {
     Object[] itemValues = {new Integer(this.getExecOperatorId()),
                            new Integer(this.getPhyOperatorId()),
                            new Integer(this.getOutQueueId()),
                            new Long(this.getNumOutMessages()),
                            new Long(this.getNumInMessages()),
                            new Long(this.getNumExecutions()),
                            new Long(this.getTotalTime()),
                            new Long(this.getStartTime()),
                            new Long(this.getEndTime()),
                            new Long(this.getNumInMessagesLatest()),
                            new Long(this.getNumOutMessagesLatest()),
                            new String(this.getOpttyp()),
                            new String(this.getOptName()),
                            new Float(this.getPercent())
                           };
     CompositeData cData= null;
     try 
     {
        cData= new CompositeDataSupport(cType_, itemNames_, itemValues);
     }
     catch( OpenDataException ode) 
     {
       
     }
     return cData;
   }
   

   /*
    * Create an instance of the model specific class out of
    * an associated CompositeData instance
    *
    public static OperatorStatsRow from(CompositeData cd) {
      if (cd==null)
        return null;
      
      return new OperatorStatsRow(((Integer)cd.get("operatorId")).intValue(),
                                  ((Integer)cd.get("phyOperatorId")).intValue(),
                                  ((Integer)cd.get("queueId")).intValue(),
                                  ((Long)cd.get("numOutMessages")).longValue(),
                                  ((Long)cd.get("numInMessages")).longValue(),
                                  ((Long)cd.get("numExecutions")).longValue(),
                                  ((Long)cd.get("totalTime")).longValue(),
                                  ((Long)cd.get("startTime")).longValue(),
                                  ((Long)cd.get("endTime")).longValue(),
                                  ((Long)cd.get("numInMessagesLatest")).longValue(),
                                  ((Long)cd.get("numOutMessagesLatest")).longValue(),
                                  ((String)cd.get("opttype")).toString(),
                                  ((String)cd.get("optName")).toString(),
                                  ((Float)cd.get("percent")).floatValue());
                                  
    }


@Override
public float getFloatValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.OPERATOR_PERCENT_CODE:
      return this.percent;
      
    default :
   	  assert false;
  }
	return Float.MIN_VALUE;
}

@Override
public int getIntValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.OPERATOR_ID_CODE :
      return this.execOperatorId;
    case ColumnCodes.OPERATOR_QUEUE_ID_CODE :
      return this.outQueueId;
    case ColumnCodes.OPERATOR_QUERY_ID_CODE :
    case ColumnCodes.OPERATOR_STREAM_ID_CODE :
  }
	return Integer.MIN_VALUE;
}

@Override
public long getLongValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.OPERATOR_NUM_OUT_MSGS_CODE :
      return this.numOutMessages;
    case ColumnCodes.OPERATOR_NUM_OUT_MSGS_LATEST_CODE :
      return this.numOutMessagesLatest;
    case ColumnCodes.OPERATOR_IN_MSGS_CODE :
      return this.numInMessages;
    case ColumnCodes.OPERATOR_IN_MSGS_LATEST_CODE :
      return this.numInMessagesLatest;
    case ColumnCodes.OPERATOR_TOTAL_TIME_CODE :
      return this.totalTime;
    case ColumnCodes.OPERATOR_START_TIME_CODE :
      return this.startTime;
    case ColumnCodes.OPERATOR_END_TIME_CODE :
      return this.endTime;
    case ColumnCodes.OPERATOR_NUM_EXECUTIONS_CODE :
      return this.numExecutions;
     
  }
	return Long.MIN_VALUE;
}

@Override
public String getStringValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.OPERATOR_TYPE_CODE :
      return this.opttyp;
    case ColumnCodes.OPERATOR_NAME_CODE :
      return this.optName;
  }
	return null;
}
*/
}

