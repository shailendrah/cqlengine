/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/statistics/IStatsFactory.java /main/5 2013/10/08 10:15:01 udeshmuk Exp $ */

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
    sbishnoi    07/10/13 - modifying stream stats params
    parujain    01/06/09 - add execopid
    parujain    11/25/08 - stats factory
    parujain    11/25/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/statistics/IStatsFactory.java /main/5 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.statistics;


public interface IStatsFactory
{
   IStats createDBStat(String loc, long cache, long log, long misses,
                       int requests);
   IStats createMemoryStat(String type, float hitratio);
   IStats createOperatorQueryStat(int opId, int qid);
   IStats createOperatorStat(int id, int phyId, int qid, long out, long in, long outHbts, long inHbts,
	                     long executions, long time,long start,
                             long end, long inLatest, long outLatest,
                             String typ, String name, float per, long cmiss, long chit,String cname, boolean cached, long pstmtTTime, long pstmtTExec);
   IStats createQueryStat(int id, String txt, String name, boolean isMetadata, boolean isInternal,
                          long numOut, long numHbts, long start, long end,long latest,
                          long executions, long time, float avg, float per, String orderingKind);
   IStats createArchiverBasedQueryStat(int id, String txt, String name, 
                                       boolean isMetadata, boolean isInternal,
                                       long numOut, long start, long end,
                                       long latest, long executions, long time, 
                                       float avg, float per, String orderingKind,
                                       long queryOpsIdentificationAndConstructionTime,
                                       long archiverQueryExecTime, 
                                       long conversionOfResultSetToTuplesTime, 
                                       long snapshotPropagationTime, 
                                       long totalStartTime, 
                                       long numRecordsReturned);
   IStats createReaderQueueStat(int id, int opId, int rId, int dequeued, 
                             int posDequeued, int negDequeued,int hbDequeued, 
                             int present, int posPresent, int negPresent, 
                             int hbPresent,long tsLast, long tsLastPos, 
                             long tsLastNeg, long tsLasthb);
   IStats createStoreStat(int id, int execId, int numElems);
   IStats createStreamStat(int id, int opId, String name, boolean is,
	                   String txt, long numIn, long start, long end,
	                   long numInLatest, float avg, float rate, 
	                   float per, boolean ispush, long mem, 
	                   long disk, float hit, boolean isArchived, long totalTime);
   IStats createSystemStat(long free, long max, long tim, long total,
                           long used, int num);
   IStats createUserFunctionStat(String name, int fnId, boolean is, 
                                 String txt,String fn,int num,long tim);
   IStats createWriterQueueStat(int id, int opId, int msgsPres, 
                                int positivePres,int negativePres, 
                                int hbPres, int msgs,int positive,
                                int negative, int hb, long lastenq,
                                long lastpos, long lastneg, long lasthb);
}
