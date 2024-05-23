/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/QueryStatsRow.java /main/3 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/09/13 - enable jmx framework
    parujain    11/12/08 - support runtime MBeans
    parujain    10/05/07 - ordering
    parujain    09/12/07 - remove logging
    hopark      07/13/07 - dump stack trace on exception
    parujain    05/31/07 - cep-em integration
    parujain    04/26/07 - Query-wide statistics
    parujain    04/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/QueryStatsRow.java /main/3 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
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
 * Statistics for Query: This can be thought of a fixed table with the
 * following columns:
 *
 * QueryId              : Query or View identifier of query
 * QueryText            : Query text
 * ObjectName           : Name or query or view
 * IsView               : Is View or not
 * NumOutMessages       : No of output messages output by query
 * startTime            : startTime of the last duration 
 * endTime              : endTime of the last duration
 * NumOutMessagesLatest : No of tuples output during past second
 * NumExecutions        : No of executions
 * TotalTime            : Total time
 * AvgLatency           : Average latency
 * Percent              : Percent of total time in executing this query
 *
 */

public class QueryStatsRow extends StatsRow implements IStats
{
  
  private int queryId;
  private String queryTxt;
  private String objectName;
  private boolean isView;
  private long numOutMessages;
  private long numOutHeartbeats;
  private long startTime;
  private long endTime;
  private long numOutMessagesLatest;
  private long numExecutions;
  private long totalTime;
  private float avgLatency;
  private float percent;
  
  /** 
   * Construct a QueryStatsRow instance. 
   * The PropertyNames annotation is used 
   * to be able to construct a QueryStatsRow instance out of a CompositeData
   * instance. See MXBeans documentation for more details.
   */ 
  @ConstructorProperties({"queryId","queryTxt","objectName","isView","numOutMessages","numOutHeartbeats","startTime","endTime",
                          "numOutMessagesLatest","numExecutions","totalTime","avgLatency","percent"})
  public QueryStatsRow(int id, String txt, String name, boolean is, long numOut, long numHbts, long start, long end,
                       long latest, long executions, long time, float avg, float per)
  {
    queryId = id;
    queryTxt = txt;
    objectName = name;
    isView = is;
    numOutMessages = numOut;
    numOutHeartbeats = numHbts;
    startTime = start;
    endTime = end;
    numOutMessagesLatest = latest;
    numExecutions = executions;
    totalTime = time;
    avgLatency = avg;
    percent = per;
  }

  public int getQueryId()
  {
    return queryId;
  }
  
  public String getQueryText()
  {
    return queryTxt ;
  }
  
  public String getObjectName()
  {
    return objectName ;
  }
  
  public boolean getIsView()
  {
    return isView ;
  }
  
  public long getNumOutMessages()
  {
    return numOutMessages ;
  }
  
  public long getNumOutHeartbeats()
  {
    return numOutHeartbeats;
  }
  
  public long getNumOutMessagesLatest()
  {
    return numOutMessagesLatest ;
  }
  
  public long getStartTime()
  {
    return startTime ;
  }
  
  public long getEndTime()
  {
    return endTime;
  }
  
  public long getNumExecutions()
  {
    return numExecutions ;
  }
  
  public long getTotalTime()
  {
    return totalTime ;
  }
  
  public float getAvgLatency()
  {
    return avgLatency;
  }
  
  public float getPercent()
  {
    return percent ;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<QueryStatistics>");
    //sb.append("<Query id =\"" + queryId + "\" >");
    //sb.append("<Query Text =\"" + queryTxt + "\" >");
    sb.append("<Query Name =\"" + objectName + "\" >");
    sb.append("<Is View =\"" + isView + "\" >");
    sb.append("<Number of Output Messages =\"" + numOutMessages + "\" >");
    //sb.append("<Start time =\"" + startTime + "\" >");
    //sb.append("<End time =\"" + endTime + "\" >");
    //sb.append("<Number of Out Messages Latest =\"" + numOutMessagesLatest + "\" >");
    //sb.append("<Number of Executions =\"" + numExecutions + "\" >");
    long totalTimeInSeconds = totalTime/1000000000l;
    long outputDataRate = totalTimeInSeconds > 0 
                          ? numOutMessages/totalTimeInSeconds : 0;
    sb.append("<Total Execution Time(in seconds) =\"" + totalTimeInSeconds + "\" >");
    sb.append("<Output Data Rate=\"" + outputDataRate);
    //sb.append("<Average Latency =\"" + avgLatency + "\" >");
    //sb.append("<Percent =\"" + percent + "\" >");
    sb.append("</QueryStatistics>");
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
   *  public static QueryStatsRow from(CompositeData cd);
   *
   */

  // The corresponding CompositeType for this class
  private static CompositeType cType_= null;

  private static final String[] itemNames_= { "queryId","queryTxt","objectName","isView","numOutMessages",
                                              "startTime","endTime", "numOutMessagesLatest","numExecutions",
                                              "totalTime","avgLatency","percent"};
  static{
    try
    {
      OpenType[] itemTypes = {SimpleType.INTEGER,
                              SimpleType.STRING,
                              SimpleType.STRING,
                              SimpleType.BOOLEAN,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.LONG,
                              SimpleType.FLOAT,
                              SimpleType.FLOAT
                             };
      cType_ = new CompositeType("oracle.cep.statistics.QueryStatsRow",
                                 "oracle.cep.statistics.QueryStatsRow",
                                 itemNames_,
                                 itemNames_,
                                 itemTypes); 
    }
    catch( OpenDataException ode)
    {
      
    }
  }
  
  /**
   * Returns the CompositeType that describes this model
   * specific class
   */
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
   */
   public CompositeData toCompositeData(CompositeType ct) {
     Object[] itemValues = {new Integer(this.getQueryId()),
                            new String(this.getQueryText()),
                            new String(this.getObjectName()),
                            new Boolean(this.getIsView()),
                            new Long(this.getNumOutMessages()),
                            new Long(this.getStartTime()),
                            new Long(this.getEndTime()),
                            new Long(this.getNumOutMessagesLatest()),
                            new Long(this.getNumExecutions()),
                            new Long(this.getTotalTime()),
                            new Float(this.getAvgLatency()),
                            new Float(this.getPercent())};
     CompositeData cData= null;
     try {
        cData= new CompositeDataSupport(cType_, itemNames_, itemValues);

    }
    catch( OpenDataException ode) {
      
    }
    return cData;
   }
   
   /**
    * Create an instance of the model specific class out of
    * an associated CompositeData instance
    
    public static QueryStatsRow from(CompositeData cd) {
      if (cd==null)
        return null;
      
      return new QueryStatsRow(((Integer)cd.get("queryId")).intValue(),
                               ((String)cd.get("queryTxt")).toString(),
                               ((String)cd.get("objectName")).toString(),
                               ((Boolean)cd.get("isView")).booleanValue(),
                               ((Long)cd.get("numOutMessages")).longValue(),
                               ((Long)cd.get("startTime")).longValue(),
                               ((Long)cd.get("endTime")).longValue(),
                               ((Long)cd.get("numOutMessagesLatest")).longValue(),
                               ((Long)cd.get("numExecutions")).longValue(),
                               ((Long)cd.get("totalTime")).longValue(),
                               ((Float)cd.get("avgLatency")).floatValue(),
                               ((Float)cd.get("percent")).floatValue());
    }
    */
/*
@Override
public boolean getBooleanValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.QUERY_IS_VIEW_CODE : return this.isView;
  }
	return false;
}

@Override
public float getFloatValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.QUERY_PERCENT_CODE : return this.percent;
    case ColumnCodes.QUERY_AVG_LATENCY_CODE : return this.avgLatency;
  }
	return Float.MIN_VALUE;
}

@Override
public int getIntValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.QUERY_ID_CODE : return this.queryId;
    case ColumnCodes.QUERY_OPERATOR_ID_CODE :
    case ColumnCodes.QUERY_USER_FUNC_ID_CODE :
  }
	return Integer.MIN_VALUE;
}

@Override
public long getLongValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.QUERY_NUM_OUT_MSGS_CODE : return this.numOutMessages;
    case ColumnCodes.QUERY_NUM_OUT_MSGS_LATEST_CODE :return this.numOutMessagesLatest;
    case ColumnCodes.QUERY_TOTAL_TIME_CODE :return this.totalTime;
    case ColumnCodes.QUERY_NUM_EXECUTIONS_CODE :  return this.numExecutions;
    case ColumnCodes.QUERY_START_TIME_CODE :  return this.startTime;
    case ColumnCodes.QUERY_END_TIME_CODE :  return this.endTime;
  }
	return Long.MIN_VALUE;
}

@Override
public String getStringValue(Column c) {
  int colcode = c.getColCode();
  switch(colcode)
  {
    case ColumnCodes.QUERY_TEXT_CODE : return this.queryTxt;
    case ColumnCodes.QUERY_OBJECT_NAME_CODE : return this.objectName;
  }
	return null;
}
*/
}
