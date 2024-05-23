/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/factory/CEPStatsRowFactory.java /main/3 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
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
    parujain    01/06/09 - phyopid
    parujain    11/13/08 - factory to create stats
    parujain    11/13/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/factory/CEPStatsRowFactory.java /main/3 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx.factory;

import oracle.cep.statistics.IStats;
import oracle.cep.statistics.IStatsFactory;
import oracle.cep.jmx.stats.OperatorStatsRow;
import oracle.cep.jmx.stats.QueryStatsRow;
import oracle.cep.jmx.stats.StreamStatsRow;
import oracle.cep.jmx.stats.ArchiverBasedQueryStatsRow;
/**
import oracle.cep.jmx.stats.DBStatsRow;
import oracle.cep.jmx.stats.MemoryStatsRow;
import oracle.cep.jmx.stats.OperatorQueryStatsRow;
import oracle.cep.jmx.stats.OperatorStatsRow;
import oracle.cep.jmx.stats.ReaderQueueStatsRow;
import oracle.cep.jmx.stats.StoreStatsRow;

import oracle.cep.jmx.stats.SystemStatsRow;
import oracle.cep.jmx.stats.UserFunctionStatsRow;
import oracle.cep.jmx.stats.WriterQueueStatsRow;
*/
public class CEPStatsRowFactory implements IStatsFactory
{

  @Override
  public IStats createDBStat(String loc, long cache, long log,
                                    long misses, int requests) 
  {
    //return new DBStatsRow(loc, cache, log, misses, requests);
    return null;
  }

  @Override
  public IStats createMemoryStat(String type, float hitRatio) {    
    //return new MemoryStatsRow(type, hitRatio);
    return null;
  }

  @Override
  public IStats createOperatorQueryStat(int opId, int qid) {
    //return new OperatorQueryStatsRow(opId, qid);
    return null;
  }

  @Override
  public IStats createOperatorStat(int id, int phyId, int qid, long out, long in,long outHbts, long inHbts,
              long executions, long time, long start, long end, long inLatest,
              long outLatest, String typ, String name, float per, long cmiss, long chit, String cname, boolean cached, long pstmtTTime, long pstmtTExec) {
    return new OperatorStatsRow(id, phyId, qid, out, in, outHbts, inHbts, executions, time, start, end,
                                inLatest, outLatest, typ, name, per,cmiss,chit,cname,cached,pstmtTTime,pstmtTExec);
  }

  @Override
  public IStats createQueryStat(int id, String txt, String name, boolean isMetadata, boolean isInternal,
      long numOut, long numHbts, long start, long end,long latest,
      long executions, long time, float avg, float per, String orderingKind)
  {
    return new QueryStatsRow(id, txt, name, isMetadata, numOut, numHbts, start, end, 
      latest, executions, time, avg, per);
  }
  
  @Override
  public IStats createArchiverBasedQueryStat(int id, String txt, String name, 
    boolean isMetadata, boolean isInternal, long numOut, long start, long end,
    long latest, long executions, long time, float avg, float per, 
    String orderingKind, long queryOpsIdentificationAndConstructionTime,
    long archiverQueryExecTime, long conversionOfResultSetToTuplesTime, 
    long snapshotPropagationTime, long totalStartTime, long numRecordsReturned)
  {
    return new ArchiverBasedQueryStatsRow(id, txt, name, isMetadata, isInternal,
      numOut, 0L, start, end, latest, executions, time, avg, per, orderingKind, 
      queryOpsIdentificationAndConstructionTime,
      archiverQueryExecTime, conversionOfResultSetToTuplesTime, snapshotPropagationTime,
      totalStartTime, numRecordsReturned);
  }
  /**
  public IStats createQueryStat(int id, String txt, String name,
            boolean is, long numOut, long start, long end, long latest,
            long executions, long time, float avg, float per) {
    //return new QueryStatsRow(id, txt, name, is, numOut, start, end, latest,
    //                         executions, time, avg, per);
    return null;
  }
  */
  @Override
  public IStats createReaderQueueStat(int id, int opId, int id2,
        int dequeued, int posDequeued, int negDequeued, int hbDequeued,
        int present, int posPresent, int negPresent, int hbPresent,
        long tsLast, long tsLastPos, long tsLastNeg, long tsLasthb) {
    //return new ReaderQueueStatsRow(id, opId, id2, dequeued, posDequeued,
    //                               negDequeued, hbDequeued, present, posPresent,
    //                               negPresent, hbPresent, tsLast, tsLastPos,
    //                               tsLastNeg, tsLasthb);
    return null;
  }

  @Override
  public IStats createStoreStat(int id, int execId, int numElems) 
  {
    //return new StoreStatsRow(id, execId, numElems);
    return null;
  }

  @Override
  public IStats createStreamStat(int id, int opId, String name,
             boolean is, String txt, long numIn, long start, long end,
             long numInLatest, float avg, float rate, float per, boolean ispush,
             long mem, long disk, float hit, boolean isArchived, long totalTime)
  {
    return new StreamStatsRow(id, opId, name, is, txt, numIn, start, end,
                             numInLatest, avg, rate, per, ispush, mem, disk, hit,
                             isArchived, totalTime);
  }

  @Override
  public IStats createSystemStat(long free, long max, long tim,
                                  long total, long used, int num) 
  {
    //return new SystemStatsRow(free, max, tim, total, used, num);
    return null;
  }

  @Override
  public IStats createUserFunctionStat(String name, int fnId,
               boolean is, String txt, String fn, int num, long tim) {
     //return new UserFunctionStatsRow(name, fnId, is, txt, fn, num, tim);
    return null;
  }

  @Override
  public IStats createWriterQueueStat(int id, int opId, int msgsPres,
             int positivePres, int negativePres, int hbPres, int msgs,
             int positive, int negative, int hb, long lastenq, long lastpos,
             long lastneg, long lasthb) 
  {
    //return new WriterQueueStatsRow(id, opId, msgsPres, positivePres, 
    //                               negativePres, hbPres, msgs, positive,
    //                               negative, hb, lastenq, lastpos, lastneg, 
    //                               lasthb);
    return null;
  }

}
