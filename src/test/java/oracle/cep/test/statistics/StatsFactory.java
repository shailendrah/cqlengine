package oracle.cep.test.statistics;

import oracle.cep.statistics.IStatsFactory;
import oracle.cep.statistics.IStats;

public class StatsFactory implements IStatsFactory
{
  @Override
  public IStats createDBStat(String loc, long cache, long log,
                             long misses, int requests)
  {
    return new DBStat(loc, cache, log, misses, requests);
  }

  @Override
  public IStats createMemoryStat(String type, float hitRatio) {
    return new MemoryStat(type, hitRatio); 
  }

  @Override
  public IStats createOperatorQueryStat(int opId, int qid) {
    return new OperatorQueryStat(opId, qid);
  }
  
  @Override
  public IStats createOperatorStat(int id, int phyId, int qid, long out, long in,long outHbts, long inHbts,
                long executions, long time, long start, long end, long inLatest,
                long outLatest, String typ, String name, float per, long cmiss, long chit, String cname, boolean cached, long pstmtTTime, long pstmtTExec ) {
    return new OperatorStat(id, phyId, qid, out, in, executions, time, start, 
                            end, inLatest, outLatest, typ, name, per,cmiss,chit,cname,cached,pstmtTTime,pstmtTExec);
  }
  
  @Override
  public IStats createQueryStat(int id, String txt, String name,
                boolean isMetadata, boolean isInternal, long numOut, long numHbts, long start, long end, long latest,
                long executions, long time, float avg, float per, String orderingConstraint) {
    return new QueryStat(id, txt, name, isMetadata, isInternal, numOut, start, end, latest,
                         executions, time, avg, per, orderingConstraint);
  }

  @Override
  public IStats createReaderQueueStat(int id, int opId, int id2,
          int dequeued, int posDequeued, int negDequeued, int hbDequeued,
          int present, int posPresent, int negPresent, int hbPresent,
          long tsLast, long tsLastPos, long tsLastNeg, long tsLasthb) {
    return new ReaderQueueStat(id, opId, id2, dequeued, posDequeued, negDequeued,
                               hbDequeued, present, posPresent, negPresent, hbPresent,
                               tsLast, tsLastPos, tsLastNeg, tsLasthb);
  }
  
  @Override
  public IStats createStoreStat(int id, int execId, int numElems)
  {
    return new StoreStat(id, execId, numElems); 
  }

  @Override
  public IStats createStreamStat(int id, int opId, String name,
               boolean is, String txt, long numIn, long start, long end,
               long numInLatest, float avg, float rate, float per, boolean ispush,
               long mem, long disk, float hit, boolean isArchived, long totalTime) {
    return new StreamStat(id, opId, name, is, txt, numIn, start, end, 
                          numInLatest, avg, rate, per, ispush, mem, disk, hit);
  }

  @Override
  public IStats createSystemStat(long free, long max, long tim,
                                 long total, long used, int num)
  {
    return new SystemStat(free, max, tim, total, used, num);
  }

  @Override
  public IStats createUserFunctionStat(String name, int fnId,
                boolean is, String txt, String fn, int num, long tim) {
    return new UserFunctionStat(name, fnId, is, txt, fn, num, tim);
  }
  
  @Override
  public IStats createWriterQueueStat(int id, int opId, int msgsPres,
                int positivePres, int negativePres, int hbPres, int msgs,
                int positive, int negative, int hb, long lastenq, long lastpos,
                long lastneg, long lasthb)
  {
    return new WriterQueueStat(id, opId, msgsPres, positivePres, negativePres, 
                                hbPres, msgs, positive, negative, hb, lastenq,
                                lastpos, lastneg, lasthb);
  }

  @Override
  public IStats createArchiverBasedQueryStat(int id, String txt, String name,
      boolean isMetadata, boolean isInternal, long numOut, long start,
      long end, long latest, long executions, long time, float avg, float per,
      String orderingKind, long queryOpsIdentificationAndConstructionTime,
      long archiverQueryExecTime, long conversionOfResultSetToTuplesTime,
      long snapshotPropagationTime, long totalStartTime, long numRecordsReturned) 
  {
    return null;
  }
                                             
                                            
}
