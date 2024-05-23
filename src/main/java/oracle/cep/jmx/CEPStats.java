/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStats.java /main/18 2013/10/08 10:15:00 udeshmuk Exp $ */

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
    sbishnoi    07/09/13 - enabling jmx framework
    parujain    11/14/08 - support runtime stats
    hopark      10/28/08 - lazy init
    hopark      10/10/08 - remove statics
    parujain    07/16/08 - jar reorg
    hopark      05/05/08 - remove FullSpillMode
    najain      04/25/08 - add more APIs
    parujain    04/21/08 - concurrency
    hopark      03/08/08 - use getFullSpillMode
    parujain    03/21/08 - support for filters
    parujain    03/13/08 - support for filters
    parujain    10/10/07 - ordered list
    parujain    09/19/07 - Standalone
    parujain    09/12/07 - fix cep-em
    hopark      07/13/07 - dump stack trace on exception
    parujain    05/30/07 - CEPStatistics
    parujain    05/30/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/CEPStats.java /main/18 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.cep.exceptions.CEPException;
import oracle.cep.jmx.factory.CEPStatsRowFactory;
import oracle.cep.jmx.stats.QueryStatsRow;
import oracle.cep.jmx.stats.StreamStatsRow;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.iterator.OperatorStatsIterator;
import oracle.cep.statistics.iterator.QueryStatsIterator;
import oracle.cep.statistics.iterator.StreamStatsIterator;

/**
import java.util.Collections;
import java.util.logging.Level;
import java.util.ConcurrentModificationException;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.metadata.MetadataException;
import oracle.cep.jmx.stats.DBStatsRow;
import oracle.cep.statistics.comparator.StatsComparator;
import oracle.cep.statistics.statsmgr.DBStatsRowManager;
import oracle.cep.jmx.stats.MemoryStatsRow;
import oracle.cep.statistics.statsmgr.MemoryStatsRowManager;
import oracle.cep.statistics.statsmgr.OperatorQueryRowManager;
import oracle.cep.statistics.statsmgr.OperatorRowManager;
import oracle.cep.statistics.statsmgr.OperatorSplRowManager;
import oracle.cep.statistics.statsmgr.OperatorStatsManager;
import oracle.cep.statistics.statsmgr.QuerySplRowManager;
import oracle.cep.statistics.statsmgr.QueryStatsManager;
import oracle.cep.statistics.statsmgr.UserFunctionSplStatsRowManager;
import oracle.cep.statistics.statsmgr.UserFunctionStatsManager;
import oracle.cep.jmx.stats.Column;
import oracle.cep.jmx.stats.FilterCondition;
import oracle.cep.jmx.stats.OperatorStatsRow;
import oracle.cep.statistics.statsmgr.QueryStatsRowManager;
import oracle.cep.statistics.statsmgr.ReaderQueueRowManager;
import oracle.cep.jmx.stats.ReaderQueueStatsRow;
import oracle.cep.jmx.stats.StreamStatsRow;
import oracle.cep.jmx.stats.StoreStatsRow;
import oracle.cep.statistics.statsmgr.StreamStatsRowManager;
import oracle.cep.statistics.statsmgr.StoreStatsManager;
import oracle.cep.jmx.stats.SystemStatsRow;
import oracle.cep.statistics.statsmgr.SystemStatsRowManager;
import oracle.cep.jmx.stats.UserFunctionStatsRow;
import oracle.cep.statistics.statsmgr.UserFunctionStatsRowManager;
import oracle.cep.statistics.statsmgr.WriterQueueRowManager;
import oracle.cep.jmx.stats.WriterQueueStatsRow;
import oracle.cep.jmx.stats.OperatorQueryStatsRow;
*/

public class CEPStats implements CEPStatsMBean
{
    private ExecContext                      m_execContext;
    
    // All the Stats Managers
    private QueryStatsIterator            m_qryMgr = null;
    private StreamStatsIterator           m_strmMgr = null;
    /**
    private MemoryStatsRowManager            m_memMgr = null;    
    private QuerySplRowManager               m_qrySpMgr = null;
    private OperatorRowManager               m_opMgr = null;
    private OperatorSplRowManager            m_opSpMgr = null;
    private DBStatsRowManager                m_dbMgr = null;
    private UserFunctionStatsRowManager      m_fnMgr = null;
    private UserFunctionSplStatsRowManager   m_fnSpMgr = null;
    private ReaderQueueRowManager            m_readerMgr = null;
    private WriterQueueRowManager            m_writerMgr = null;
    private OperatorQueryRowManager          m_opQMgr = null;
    private SystemStatsRowManager            m_sysMgr = null;
    private StreamStatsRowManager            m_strmMgr = null;
    private StoreStatsManager                m_storeMgr = null;
    */
    // All the Comparators
    /**
    private StatsComparator                  m_queryComp;
    private StatsComparator                  m_strmComp;
    private StatsComparator                  m_operatorComp;
    private StatsComparator                  m_funcComp;
    */
    private CEPStatsRowFactory            m_factory;
    
    public CEPStats(ExecContext ec)
    {
      super();
      m_execContext = ec;
      
      // Initialie the comparators
      /**
      m_queryComp    = new StatsComparator();
      m_strmComp     = new StatsComparator();
      m_operatorComp = new StatsComparator();
      m_funcComp     = new StatsComparator();
      */
      m_factory      = new CEPStatsRowFactory();
    }

    /**
    public List<MemoryStatsRow> getMemoryStats() 
    {
      MemoryStatsRowManager memMgr = getMemMgr();    
    synchronized(memMgr)
    {
      List<MemoryStatsRow> memStats  = new ArrayList<MemoryStatsRow>();
      
      memMgr.init();
      while(true)
      {
        MemoryStatsRow stat = (MemoryStatsRow)memMgr.getNext();
        if(stat == null)
          break;
        memStats.add(stat);
      }
      memMgr.close();
   
      return memStats;
    }
    }
    */
    /**
    private boolean collectQueryStats(FilterCondition[] filter, List<QueryStatsRow> queryStats)
    {
      queryStats.clear();
      QueryStatsManager qMgr = getQryMgr();
      if(QueryStatsManager.isSpecialCase(filter))
        qMgr = getQrySpMgr();
      qMgr.init(filter);
      while(true)
      {
        QueryStatsRow stat = (QueryStatsRow)qMgr.getNext();
        if(stat == null)
          break;
        queryStats.add(stat);
       }
      qMgr.close();
      return true;
    }*/

    /**
    public List<QueryStatsRow> getQueryStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems)
    {
      QueryStatsRowManager qryMgr = getQryMgr();
     synchronized(qryMgr)
     {
      List<QueryStatsRow> queryStats    = new ArrayList<QueryStatsRow>();
      boolean success = false;
     
      //Error conditions should return empty list
      if(offset < 0)
        return queryStats;
      if(numItems <= 0)
        return queryStats;
      
      while(!success)
      {
        try {
         success = collectQueryStats(filter, queryStats);
        } catch (ConcurrentModificationException ex)
        { //Since list got modified so collect stats again after clearing the already collected ones.
        }
      }      

      if(sortColumn == null)
        sortColumn = Column.QUERY_PERCENT;  //default sort column
      m_queryComp.setColumn(sortColumn);
      if(queryStats.size() > 1)
        Collections.sort(queryStats, m_queryComp);
      
      
      int size = queryStats.size();
      // send empty list since there is nothing to be send
      if(offset >= size)
      {
        queryStats.clear();
        return queryStats;
      } 
      // list to be sent is shorter than number of items to be sent
      else if((offset + numItems) > size)
      {
        queryStats.subList(0, offset).clear();
        return queryStats;
      }
      return queryStats.subList(offset, (offset+numItems));
     }
    }
    */
    
    public QueryStatsRow getQueryStats(String schema, String queryId)
    {
      QueryStatsIterator queryStatsMgr = new QueryStatsIterator(m_execContext);
      queryStatsMgr.setStatsRowFactory(m_factory);
      return (QueryStatsRow) queryStatsMgr.getQueryStats(schema,queryId);
    }
    //TODO: Remove this api once EM stops using it
    public String getQueryStats()
    {
     StringBuffer sb = new StringBuffer();
     QueryStatsIterator qryMgr = getQryMgr();
     synchronized(qryMgr)
     {
      //List<QueryStatsRow> queryStats = new ArrayList<QueryStatsRow>();
      List<String> queryStats = new ArrayList<String>();
      qryMgr.init();
      while(true)
      {
        QueryStatsRow stat = null;
        try 
        {
          stat = (QueryStatsRow)qryMgr.getNext();
        } 
        catch (CEPException e) {
          LogUtil.fine(LoggerType.TRACE, "Error while retrieving query stats. " + e.getMessage());
          LogUtil.logStackTrace(e);
        }
        if(stat == null)
          break;
        //queryStats.add(stat);
        //queryStats.add(stat.toString());
        sb.append("\n" + stat.toString());
      }
      qryMgr.close();
      //m_queryComp.setColumn(Column.QUERY_PERCENT);
      //if(queryStats.size() > 1)
      //  Collections.sort(queryStats, m_queryComp);
      //return queryStats;
      return sb.toString();
     }
    }
    /**
    private boolean collectStreamStats(FilterCondition[] filter,List<StreamStatsRow> strmStats)
    {
      strmStats.clear();
      StreamStatsRowManager strmMgr = getStrmMgr();
      strmMgr.init(filter);
      while(true)
      {
        try{
        StreamStatsRow stat = (StreamStatsRow)strmMgr.getNext();
        if(stat == null)
          break;
        strmStats.add(stat);
        
        }catch(MetadataException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e); 
        }
      }
      strmMgr.close();
      return true;
    }
    */
    /**
    public List<StreamStatsRow> getStreamStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) 
    {
    StreamStatsRowManager strmMgr = getStrmMgr();
    synchronized(strmMgr)
    {
     List<StreamStatsRow> strmStats = new ArrayList<StreamStatsRow>();
     boolean success = false;
      if(offset < 0)
        return strmStats;
      if(numItems <= 0)
        return strmStats;
      
      while(!success)
      {
        try{
          success = collectStreamStats(filter, strmStats);
        }catch (ConcurrentModificationException ex)
        { //Since list got modified so collect stats again after clearing the already collected ones.
        }
      }
      
      if(sortColumn == null)
        sortColumn = Column.STREAM_PERCENT;
      m_strmComp.setColumn(sortColumn);
      if(strmStats.size() > 1)
        Collections.sort(strmStats, m_strmComp);
      
      int size = strmStats.size();
      // send empty list since there is nothing to be send
      if(offset >= size)
      {
        strmStats.clear();
        return strmStats;
      } 
      // list to be sent is shorter than number of items to be sent
      else if((offset + numItems ) > size)
      {
        strmStats.subList(0, offset).clear();
        return strmStats;
      }
      return strmStats.subList(offset, (offset+numItems));
    }
    }
    */
    /**
    public List<StoreStatsRow> getStoreStats()
    {
      StoreStatsManager storeMgr = getStoreMgr();
      synchronized(storeMgr)
      {
	List <StoreStatsRow> storeStats = new ArrayList<StoreStatsRow>();
      
	storeMgr.init();
	while(true)
	{
	  StoreStatsRow stat = (StoreStatsRow)storeMgr.getNext();
	  if (stat == null)
	    break;
	  storeStats.add(stat);
	}
	storeMgr.close();
	return storeStats;
      }
    }
    */
 
    //TODO: Remove this api once EM stops using it
    public String getStreamStats()
    {
      StreamStatsIterator strmMgr = getStrmMgr();
      StringBuffer sb = null;
      synchronized(strmMgr)
      {
        sb = new StringBuffer();
        List<StreamStatsRow> strmStats = new ArrayList<StreamStatsRow>();
        strmMgr.init();
        while(true)
        {
          try
          {
            StreamStatsRow stat = (StreamStatsRow)strmMgr.getNext();
            if(stat == null)
              break;
            //strmStats.add(stat);
            sb.append("\n" + stat.toString());
          }
          catch(CEPException e)
          {
            LogUtil.fine(LoggerType.TRACE, e.getMessage()); 
        }
      }
      strmMgr.close();
//      m_strmComp.setColumn(Column.STREAM_PERCENT);
//      if(strmStats.size() > 1)
//        Collections.sort(strmStats, m_strmComp);
      return sb.toString();
    }
    }
    
    /**
    private boolean collectOperatorStats(FilterCondition[] filter, List<OperatorStatsRow> opStats)
    {
      opStats.clear();
      OperatorStatsManager opStatMgr = getOpMgr();
      if(OperatorStatsManager.isSpecialCase(filter))
        opStatMgr = getOpSpMgr();
      
      opStatMgr.init(filter);
      while(true)
      {
        OperatorStatsRow stat = (OperatorStatsRow)opStatMgr.getNext();
        if(stat == null)
          break;
        opStats.add(stat);
       
      }
      opStatMgr.close();
      return true;
    }
     */
    /**
    public List<OperatorStatsRow> getOperatorStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) 
    {
      OperatorStatsManager opMgr = getOpMgr();
    synchronized(opMgr)
    {
      List<OperatorStatsRow> opStats = new ArrayList<OperatorStatsRow>();
      boolean success = false;

       if(offset < 0)
         return opStats;
       if(numItems <= 0)
         return opStats;

       while(!success)
       {       
         try{
           success = collectOperatorStats(filter, opStats);
         }catch (ConcurrentModificationException ex)
         { //Since list got modified so collect stats again after clearing the already collected ones.
         }
       }
       
       if(sortColumn == null)
         sortColumn = Column.OPERATOR_PERCENT;
       m_operatorComp.setColumn(sortColumn);
       if(opStats.size() > 1)
         Collections.sort(opStats, m_operatorComp);
       
       int size = opStats.size();
       // send empty list since there is nothing to be send
       if(offset >= size)
       {
         opStats.clear();
         return opStats;
       } 
       // list to be sent is shorter than number of items to be sent
       else if((offset + numItems) > size)
       {
         opStats.subList(0, offset).clear();
         return opStats;
       }
       return opStats.subList(offset, (offset+numItems));
    }
    }
    */
    public Map<String,Object> getOperatorStatus(String schema, String queryId)
    {
      OperatorStatsIterator opStatsMgr = new OperatorStatsIterator(m_execContext);
      opStatsMgr.setStatsRowFactory(m_factory);
      return opStatsMgr.getOperatorStats(schema,queryId);
    }
    /**
   //TODO: Remove this api once EM stops using it 
    public List<OperatorStatsRow> getOperatorStats() 
    {
      OperatorStatsManager opMgr = getOpMgr();
      synchronized(opMgr)
      {
        List<OperatorStatsRow> opStats = new ArrayList<OperatorStatsRow>();
      
        opMgr.init();
        while(true)
        {
           OperatorStatsRow stat = (OperatorStatsRow)opMgr.getNext();
          if(stat == null)
            break;
          opStats.add(stat);
        }
        opMgr.close();
        m_operatorComp.setColumn(Column.OPERATOR_PERCENT);
        if(opStats.size() > 1)
          Collections.sort(opStats, m_operatorComp);
        return opStats;
      }
    }
    */
    /**
    public List<DBStatsRow> getDBStats() 
    {
      DBStatsRowManager dbMgr = getDbMgr(); 
    synchronized(dbMgr)
    {
      List<DBStatsRow>  dbStats = new ArrayList<DBStatsRow>();
      
      dbMgr.init();
      DBStatsRow stat = (DBStatsRow)dbMgr.getNext();
      if(stat != null)
        dbStats.add(stat);
      dbMgr.close();
      return dbStats;
    }
    }

    private boolean collectUserFuncStats(FilterCondition[] filter, List<UserFunctionStatsRow> userFuncStats)
    {
      userFuncStats.clear();
      UserFunctionStatsManager fMgr;
      if(UserFunctionStatsManager.isSpecialCase(filter))
        fMgr = getFnSpMgr();
      else
        fMgr = getFnMgr();
      fMgr.init(filter);
      while(true)
      {
        try{
        UserFunctionStatsRow stat = (UserFunctionStatsRow)fMgr.getNext();
        if(stat == null)
          break;
        userFuncStats.add(stat);
         
        }catch(MetadataException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);  
        }
      }
      fMgr.close();
      return true;
    }
    
    public List<UserFunctionStatsRow> getUserFuncStats(FilterCondition[] filter, Column sortColumn, int offset, int numItems) 
    {
      UserFunctionStatsManager fnMgr = getFnMgr();
      synchronized(fnMgr)
      {
        List<UserFunctionStatsRow>  userFuncStats = new ArrayList<UserFunctionStatsRow>();
    	boolean success = false;

    	if(offset < 0)
          return userFuncStats;
        if(numItems <= 0)
          return userFuncStats;
        
        while(!success)
        {
          try{
            success = collectUserFuncStats(filter, userFuncStats);
          }catch (ConcurrentModificationException ex)
          { //Since list got modified so collect stats again after clearing the already collected ones.
          }
        }
        
        if(sortColumn == null)
          sortColumn = Column.FUNCTION_TOTAL_TIME;
        m_funcComp.setColumn(sortColumn);
        if(userFuncStats.size() > 1)
          Collections.sort(userFuncStats, m_funcComp);
        
        int size = userFuncStats.size();
        // send empty list since there is nothing to be send
        if(offset >= size)
        {
          userFuncStats.clear();
          return userFuncStats;
        } 
        // list to be sent is shorter than number of items to be sent
        else if((offset + numItems ) > size)
        {
          userFuncStats.subList(0, offset).clear();
          return userFuncStats;
        }
        return userFuncStats.subList(offset, (offset+numItems));
      }
    }
    
    // TODO: Remove this api once EM stops using it
    public List<UserFunctionStatsRow> getUserFuncStats()
    {
      UserFunctionStatsManager fnMgr = getFnMgr();
    synchronized(fnMgr)
    {
      List<UserFunctionStatsRow>  userFuncStats = new ArrayList<UserFunctionStatsRow>();
     
      fnMgr.init();
      while(true)
      {
        try{
        UserFunctionStatsRow stat = (UserFunctionStatsRow)fnMgr.getNext();
        if(stat == null)
          break;
        userFuncStats.add(stat);
        }catch(MetadataException e)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);  
        }
      }
      fnMgr.close();
      m_funcComp.setColumn(Column.FUNCTION_TOTAL_TIME);
      if(userFuncStats.size() > 1)
        Collections.sort(userFuncStats, m_funcComp);
	    return userFuncStats;
    }
    }

    private boolean collectReaderQueueStats(List<ReaderQueueStatsRow> readerQStats)
    {
      readerQStats.clear();
      ReaderQueueRowManager readerMgr = getReaderMgr();
      readerMgr.init();
      while (true)
      {
        ReaderQueueStatsRow stat = (ReaderQueueStatsRow)readerMgr.getNext();
        if (stat == null)
          break;
        readerQStats.add(stat);
      }
      readerMgr.close();
      return true;
    }
    
    public List<ReaderQueueStatsRow> getReaderQueueStats() 
    {
      ReaderQueueRowManager readerMgr = getReaderMgr();
    synchronized(readerMgr)
    {
      List<ReaderQueueStatsRow> readerQStats = new ArrayList<ReaderQueueStatsRow>();
      boolean success = false;
    
      while(!success)
      {
        try{
          success = collectReaderQueueStats(readerQStats);
        }catch (ConcurrentModificationException ex)
        { //Since list got modified so collect stats again after clearing the already collected ones.
        }
      }
     
	  return readerQStats;
    }
    }
    
    private boolean collectWriterQueueStats(List<WriterQueueStatsRow> writerQStats)
    {
      writerQStats.clear();
      WriterQueueRowManager writerMgr = getWriterMgr();
      writerMgr.init();
      while (true)
      {
        WriterQueueStatsRow stat = (WriterQueueStatsRow)writerMgr.getNext();
        if (stat == null)
          break;
        writerQStats.add(stat);
      }
      writerMgr.close();
      return true;
    }

    public List<WriterQueueStatsRow> getWriterQueueStats() 
    {
      WriterQueueRowManager writerMgr = getWriterMgr();
     synchronized(writerMgr)
     { List<WriterQueueStatsRow> writerQStats = new ArrayList<WriterQueueStatsRow>();
       boolean success = false;

       while(!success)
       {
    	  try{
            success = collectWriterQueueStats(writerQStats);
          }catch (ConcurrentModificationException ex)
          { //Since list got modified so collect stats again after clearing the already collected ones.
          }
       }
	  return writerQStats;
     }
    }
    
    // TODO : Remove the API once EM desupports it
    public List<OperatorQueryStatsRow> getOperatorQueueStats() 
    {
      OperatorQueryRowManager opQMgr = getOpQMgr();
    synchronized(opQMgr)
    { 
      List<OperatorQueryStatsRow> opQStats = new ArrayList<OperatorQueryStatsRow>();
      
      opQMgr.init();
      while (true)
      {
        OperatorQueryStatsRow stat = (OperatorQueryStatsRow)opQMgr.getNext();
        if (stat == null)
          break;
        opQStats.add(stat);
      }
      opQMgr.close();
      return opQStats;
    }
    }
    public List<SystemStatsRow> getSystemStats() 
    {
      SystemStatsRowManager sysMgr = getSysMgr();
    synchronized(sysMgr)
    {
      List<SystemStatsRow> sysStats = new ArrayList<SystemStatsRow>();
      
      sysMgr.init();
      SystemStatsRow stat = (SystemStatsRow)sysMgr.getNext();
      if(stat != null)
        sysStats.add(stat);
      sysMgr.close();
      return sysStats;
    }
    }
    */
    /**
    public boolean getIsMemoryMode()
    {
      IEvictPolicy evPolicy = m_cepMgr.getEvictPolicy();
      return (evPolicy == null || !evPolicy.isFullSpill());
    }*/

//	public FilterCondition getFilterCondition() {
//	  return new FilterCondition();
//	}

    /**
    private MemoryStatsRowManager getMemMgr()
    {
      if (m_memMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_memMgr    = new MemoryStatsRowManager(ec, m_factory);
      }
      return m_memMgr;
    }
    */
   
    private QueryStatsIterator getQryMgr()
    {
      if (m_qryMgr == null)
      {
        m_qryMgr = new QueryStatsIterator(m_execContext);
        m_qryMgr.setStatsRowFactory(m_factory);
      }
      return m_qryMgr;
    }
    
    private StreamStatsIterator getStrmMgr()
    {
      if (m_strmMgr == null)
      {
        m_strmMgr   = new StreamStatsIterator(m_execContext);
        m_strmMgr.setStatsRowFactory(m_factory);
      }
      return m_strmMgr;
    }    
     /**
    private QuerySplRowManager getQrySpMgr()
    {
      if (m_qrySpMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_qrySpMgr  = new QuerySplRowManager(ec, m_factory);
      }
      return m_qrySpMgr;
    }

    private OperatorRowManager getOpMgr()
    {
      if (m_opMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_opMgr     = new OperatorRowManager(ec, m_factory);
      }
      return m_opMgr;
    }

    private OperatorSplRowManager getOpSpMgr()
    {
      if (m_opSpMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_opSpMgr   = new OperatorSplRowManager(ec, m_factory);
      }
      return m_opSpMgr;
    }
        
    private DBStatsRowManager getDbMgr()
    {
      if (m_dbMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_dbMgr = new DBStatsRowManager(ec, m_factory);
      }
      return m_dbMgr;
    }
           
    private UserFunctionStatsRowManager getFnMgr()
    {
      if (m_fnMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_fnMgr = new UserFunctionStatsRowManager(ec, m_factory);
      }
      return m_fnMgr;
    }
      
    private UserFunctionSplStatsRowManager getFnSpMgr()
    {
      if (m_fnSpMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_fnSpMgr = new UserFunctionSplStatsRowManager(ec, m_factory);
      }
      return m_fnSpMgr;
    }
              
    private ReaderQueueRowManager getReaderMgr()
    {
      if (m_readerMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_readerMgr = new ReaderQueueRowManager(ec, m_factory);
      }
      return m_readerMgr;
    }
    
    private WriterQueueRowManager getWriterMgr()
    {
      if (m_writerMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_writerMgr = new WriterQueueRowManager(ec, m_factory);
      }
      return m_writerMgr;
    }
        
    private OperatorQueryRowManager getOpQMgr()
    {
      if (m_opQMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_opQMgr    = new OperatorQueryRowManager(ec, m_factory);
      }
      return m_opQMgr;
    }    
    
    private SystemStatsRowManager getSysMgr()
    {
      if (m_sysMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_sysMgr    = new SystemStatsRowManager(ec, m_factory);
      }
      return m_sysMgr;
    }    
    
    private StreamStatsRowManager getStrmMgr()
    {
      if (m_strmMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_strmMgr   = new StreamStatsRowManager(ec, m_factory);
      }
      return m_strmMgr;
    }    
        
    private StoreStatsManager getStoreMgr()
    {
      if (m_storeMgr == null)
      {
        ExecContext ec = m_cepMgr.getSystemExecContext();
        m_storeMgr  = new StoreStatsManager(ec, m_factory);
      }
      return m_storeMgr;
    }  
    */  
}
