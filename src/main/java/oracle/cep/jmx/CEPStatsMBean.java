/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStatsMBean.java /main/1 2013/10/08 10:15:01 udeshmuk Exp $ */

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
    sbishnoi    07/09/13 - enablign jmx framework
    parujain    07/16/08 - jar reorg
    najain      04/25/08 - add more APIs
    parujain    03/13/08 - Support for filters
    parujain    10/11/07 - add api for spill or mem mode
    parujain    09/12/07 - fix cep-em
    parujain    05/30/07 - CEPMBean interface
    parujain    05/30/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStatsMBean.java /main/1 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx;

import java.util.List;

import oracle.cep.jmx.stats.QueryStatsRow;
/**
import oracle.cep.jmx.stats.Column;
import oracle.cep.jmx.stats.DBStatsRow;
import oracle.cep.jmx.stats.FilterCondition;
import oracle.cep.jmx.stats.MemoryStatsRow;
import oracle.cep.jmx.stats.OperatorStatsRow;

import oracle.cep.jmx.stats.ReaderQueueStatsRow;
import oracle.cep.jmx.stats.StreamStatsRow;
import oracle.cep.jmx.stats.StoreStatsRow;
import oracle.cep.jmx.stats.SystemStatsRow;
import oracle.cep.jmx.stats.UserFunctionStatsRow;
import oracle.cep.jmx.stats.WriterQueueStatsRow;
import oracle.cep.jmx.stats.OperatorQueryStatsRow;
*/
public interface CEPStatsMBean{
    
    public String getQueryStats();
    public String getStreamStats();
    /**
    public List<MemoryStatsRow> getMemoryStats(); 
    public List<QueryStatsRow> getQueryStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) ;
    public List<StreamStatsRow> getStreamStats(FilterCondition[] filter, Column column, int offset, int numItems) ;
    public List<StreamStatsRow> getStreamStats();
    public List<StoreStatsRow> getStoreStats();
    public List<OperatorStatsRow> getOperatorStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) ;
    public List<OperatorStatsRow> getOperatorStats();
    public List<DBStatsRow> getDBStats() ;
    public List<UserFunctionStatsRow> getUserFuncStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) ;
    public List<UserFunctionStatsRow> getUserFuncStats();
    public List<ReaderQueueStatsRow> getReaderQueueStats() ;
    public List<WriterQueueStatsRow> getWriterQueueStats() ;
    public List<OperatorQueryStatsRow> getOperatorQueueStats() ;
    public List<SystemStatsRow> getSystemStats() ;
    public boolean getIsMemoryMode();
    public FilterCondition getFilterCondition();
    */

}
