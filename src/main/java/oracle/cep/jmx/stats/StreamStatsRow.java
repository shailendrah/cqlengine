/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/StreamStatsRow.java /main/3 2013/10/08 10:15:01 udeshmuk Exp $ */

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
    sbishnoi    07/10/13 - enable jmx framework
    parujain    11/12/08 - support runtime MBeans
    parujain    03/13/08 - Add stats
    parujain    10/05/07 - ordering
    parujain    09/12/07 - remove logging
    hopark      07/13/07 - dump stack trace on exception
    parujain    05/31/07 - cep-em integration
    parujain    04/26/07 - Stream-wide statistics
    parujain    04/26/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/StreamStatsRow.java /main/3 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx.stats;

import java.beans.ConstructorProperties;

//import javax.management.openmbean.CompositeType;
//import javax.management.openmbean.OpenDataException;
//import javax.management.openmbean.OpenType;
//import javax.management.openmbean.SimpleType;
//import javax.management.openmbean.CompositeData;
//import javax.management.openmbean.CompositeDataSupport;

import oracle.cep.statistics.IStats;


/**
 * Statistics for Streams: This can be thought of a fixed table with the
 * following columns:
 *
 * StreamId            : Stream Identifier
 * StreamName          : Stream Name
 * IsStream            : Is Stream or Not(Relation)
 * Text                : Stream creation text
 * NumInMessages       : Number of tuples processed by stream
 * startTime           : start time of the last duration when stream operator ran
 * endTime             : end time of the last duration of observation
 * NumInMessagesLatest : Number of tuples processed during past second
 * avgLatency          : Average Latency
 * inputRate           : Messages processed per second
 *
 */

public class StreamStatsRow extends StatsRow implements IStats
{
  
  private int streamId;
  private int operatorId;
  private String streamName;
  private boolean isStream;
  private String text;
  private long numInMessages;
  private long startTime;
  private long endTime;
  private long numInMessagesLatest;
  private float avgLatency;
  private float inputRate;
  private float percent;
  private boolean isPushSrc;
  private long totalTuplesInMemory;
  private long totalTuplesOnDisk;
  private float hitRatio;
  private long totalTime;
  private boolean isArchived;
  
   /** 
     * Construct a StreamStatsRow instance. 
     * The PropertyNames annotation is used 
     * to be able to construct a StreamStatsRow instance out of a CompositeData
     * instance. See MXBeans documentation for more details.
     */ 
    @ConstructorProperties({"streamId", "operatorId", "streamName", "isStream", 
                            "text", "numInMessages", "startTime",
                            "endTime", "numInMessagesLatest",
                            "avgLatency", "inputRate","percent",
                            "isPushSrc", "totalTuplesInMemory", "totalTuplesOnDisk", "hitRatio",
                            "isArchived", "totalTime"})
  public StreamStatsRow(int id, int opId, String name, boolean is, String txt, 
                        long numIn, long start, long end, long numInLatest, 
                        float avg, float rate, float per, boolean ispush, 
                        long mem, long disk, float hit, boolean isArchived,
                        long totalTime)
  { 
    streamId = id;
    operatorId = opId;
    streamName = name;
    isStream = is;
    text = txt;
    numInMessages = numIn;
    startTime = start;
    endTime = end;
    numInMessagesLatest = numInLatest;
    avgLatency = avg;
    inputRate = rate;
    percent = per;
    isPushSrc = ispush;
    totalTuplesInMemory = mem;
    totalTuplesOnDisk = disk;
    hitRatio = hit;
    this.isArchived = isArchived;
    this.totalTime = totalTime;
  }

  public int getStreamId()
  {
    return streamId;
  }

  public int getOperatorId()
  {
    return operatorId;
  }

  public String getStreamName()
  {
    return streamName;
  }

  public boolean getIsStream()
  {
    return isStream;
  }
  
  public String getText()
  {
    return text;
  }
  
  public long getNumInMessages()
  {
    return numInMessages;
  }

  public long getNumInMessagesLatest()
  {
    return numInMessagesLatest;
  }

  public float getAvgLatency()
  {
    return avgLatency;
  }

  public float getInputRate()
  {
    return inputRate;
  }

  public float getPercent()
  {
    return percent;
  }

  public long getStartTime()
  {
    return startTime;
  }

  public long getEndTime()
  {
    return endTime;
  }
  
  public boolean getIsPushSrc()
  {
    return this.isPushSrc;
  }
  
  public long getTuplesInMemory()
  {
    return this.totalTuplesInMemory;
  }
  
  public long getTuplesOnDisk()
  {
    return this.totalTuplesOnDisk;
  }
  
  public float getHitRatio()
  {
    return this.hitRatio;
  }
  
  public long getTotalTime() {
    return totalTime;
  }

  public void setTotalTime(long totalTime) {
    this.totalTime = totalTime;
  }

  public boolean isArchived() {
    return isArchived;
  }

  public void setArchived(boolean isArchived) {
    this.isArchived = isArchived;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<StreamStatistics>");
    //sb.append("<Stream Id =\"" + streamId + "\" >");
    //sb.append("<Operator Id =\"" + operatorId + "\" >");
    sb.append("<Stream Name =\"" + streamName + "\" >");
    sb.append("<IsStream =\"" + isStream + "\" >");
    sb.append("<isArchived=\"" + isArchived + "\">");
    //sb.append("<Stream text =\"" + text + "\" >");
    sb.append("<Number of In Messages =\"" + numInMessages + "\" >");
    //sb.append("<Start time =\"" + startTime + "\" >");
    //sb.append("<End Time =\"" + endTime + "\" >");
    sb.append("<Total Time=\"" + totalTime + "\">");
    //sb.append("<Number of In Messages Latest =\"" + numInMessagesLatest + "\" >");
    //sb.append("<Average Latency =\"" + avgLatency + "\" >");
    sb.append("<Input Rate(tuples/second) =\"" + inputRate + "\" >");
    //sb.append("<Percent =\"" + percent + "\" >");
    //sb.append("<isPushSrc =\"" + isPushSrc + "\" >");
    //sb.append("<totalTuplesInMemory =\"" + totalTuplesInMemory + "\" >");
    //sb.append("<totalTuplesondisk =\"" + totalTuplesOnDisk + "\" >");
    //sb.append("<hitRatio =\"" + hitRatio + "\" >");
    sb.append("</StreamStatistics>");
    return sb.toString();
  }
  /**
   /*
     * JDK 5.0 manual conversion methods. The code
     * below would not be required if we could
     * rely on the MXBean framework
     * 
     * THE REQUIRED METHODS ARE:
     *
     *  public static CompositeType toCompositeType();
     *  public CompositeData toCompositeData(CompositeType ct);
     *  public static StreamStatsRow from(CompositeData cd);
     *
     /

    // The corresponding CompositeType for this class
    private static CompositeType cType_= null;

    private static final String[] itemNames_= 
      {"streamId", "operatorId", "streamName", "isStream", "text", 
       "numInMessages", "startTime","endTime", "numInMessagesLatest",
       "avgLatency", "inputRate", "percent", "isPushSrc", "totalTuplesInMemory", "totalTuplesOnDisk", "hitRatio"};
 
    static{
       try 
       {
          OpenType[] itemTypes = {SimpleType.INTEGER,
                                  SimpleType.INTEGER,
                                  SimpleType.STRING,
                                  SimpleType.BOOLEAN,
                                  SimpleType.STRING,
                                  SimpleType.LONG,
                                  SimpleType.LONG,
                                  SimpleType.LONG,
                                  SimpleType.LONG,
                                  SimpleType.FLOAT,
                                  SimpleType.FLOAT,
                                  SimpleType.FLOAT,
                                  SimpleType.BOOLEAN,
                                  SimpleType.LONG,
                                  SimpleType.LONG,
                                  SimpleType.FLOAT};
          cType_ = new CompositeType("oracle.cep.statistics.StreamStatsRow",
                                     "oracle.cep.statistics.StreamStatsRow",
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
     /
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
     /
     public CompositeData toCompositeData(CompositeType ct) {
          Object[] itemValues = {new Integer(this.getStreamId()),
                                 new Integer(this.getOperatorId()),
                                 new String(this.getStreamName()),
                                 new Boolean(this.getIsStream()),
                                 new String(this.getText()),
                                 new Long(this.getNumInMessages()),
                                 new Long(this.getStartTime()),
                                 new Long(this.getEndTime()),
                                 new Long(this.getNumInMessagesLatest()),
                                 new Float(this.getAvgLatency()),
                                 new Float(this.getInputRate()),
                                 new Float(this.getPercent()),
                                 new Boolean(this.getIsPushSrc()),
                                 new Long(this.totalTuplesInMemory),
                                 new Long(this.totalTuplesOnDisk),
                                 new Float(this.hitRatio)};
    
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
     /
     public static StreamStatsRow from(CompositeData cd) {
       if (cd==null)
         return null;

       return new StreamStatsRow(((Integer)cd.get("streamId")).intValue(),
                                 ((Integer)cd.get("operatorId")).intValue(),
                                 ((String)cd.get("streamName")).toString(),
                                 ((Boolean)cd.get("isStream")).booleanValue(),
                                 ((String)cd.get("text")).toString(),
                                 ((Long)cd.get("numInMessages")).longValue(),
                                 ((Long)cd.get("startTime")).longValue(),
                                 ((Long)cd.get("endTime")).longValue(),
                                 ((Long)cd.get("numInMessagesLatest")).longValue(),
                                 ((Float)cd.get("avgLatency")).floatValue(),
                                 ((Float)cd.get("inputRate")).floatValue(),
                                 ((Float)cd.get("percent")).floatValue(),
                                 ((Boolean)cd.get("isPushSrc")).booleanValue(),
                                 ((Long)cd.get("totalTuplesInMemory")).longValue(),
                                 ((Long)cd.get("totalTuplesOnDisk")).longValue(),
                                 ((Float)cd.get("hitRatio")).floatValue()
                                );
     }

	@Override
  public boolean getBooleanValue(Column c) {
    int colcode = c.getColCode();
    switch(colcode)
    {
      case ColumnCodes.STREAM_IS_STREAM_CODE : return this.isStream;
      case ColumnCodes.STREAM_IS_PUSH_SRC_CODE : return this.isPushSrc;
    }
    return false;
  }

  @Override
  public float getFloatValue(Column c) {
    int colcode = c.getColCode();
    switch(colcode)
    {
      case ColumnCodes.STREAM_AVG_LATENCY_CODE : return this.avgLatency;
      case ColumnCodes.STREAM_INPUT_RATE_CODE :  return this.inputRate;
      case ColumnCodes.STREAM_PERCENT_CODE :  return this.percent;
      case ColumnCodes.STREAM_HITRATIO_CODE : return this.hitRatio;
    }
    return Float.MIN_VALUE;
  }

  @Override
  public int getIntValue(Column c) {
    int colcode = c.getColCode();
    switch(colcode)
    {
      case ColumnCodes.STREAM_ID_CODE :
         return this.streamId;
      case ColumnCodes.STREAM_OPERATOR_ID_CODE :
         return this.operatorId;
    }
    return Integer.MIN_VALUE;
  }

  @Override
  public long getLongValue(Column c) {
    int colcode = c.getColCode();
    switch(colcode)
    {
      case ColumnCodes.STREAM_NUM_IN_MSGS_LATEST_CODE : return this.numInMessagesLatest;
      case ColumnCodes.STREAM_NUM_IN_MSGS_CODE : return this.numInMessages;
      case ColumnCodes.STREAM_START_TIME_CODE : return this.startTime;
      case ColumnCodes.STREAM_END_TIME_CODE : return this.endTime;
      case ColumnCodes.STREAM_TOTAL_TUPLES_IN_MEMORY_CODE : return this.totalTuplesInMemory;
      case ColumnCodes.STREAM_TOTAL_TUPLES_ON_DISK_CODE :  return this.totalTuplesOnDisk;
    }
    return Long.MIN_VALUE;
  }

  @Override
  public String getStringValue(Column c) {
   int colcode = c.getColCode();
   switch(colcode)
   {
     case ColumnCodes.STREAM_TEXT_CODE :
        return this.text;
     case ColumnCodes.STREAM_NAME_CODE :
        return this.streamName;
   }
     return null;
  }
  */
}
