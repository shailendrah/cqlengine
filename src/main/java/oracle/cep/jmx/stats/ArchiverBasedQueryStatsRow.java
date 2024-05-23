/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/ArchiverBasedQueryStatsRow.java /main/1 2013/10/08 10:15:01 udeshmuk Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/11/13 - Creation
 */

package oracle.cep.jmx.stats;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/jmx/stats/ArchiverBasedQueryStatsRow.java /main/1 2013/10/08 10:15:01 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class ArchiverBasedQueryStatsRow extends QueryStatsRow
{
  /**
   * Time required to identify query operators and construct
   * their archiver queries and a final combined archiver query.
   */
  private long queryOpsIdentificationAndConstructionTime;
  
  /**
   * Time taken by the backing store to execute the archiver query
   * and return results.
   */
  private long archiverQueryExecTime;
  
  /**
   * Time required to convert resultset returned by the archiver query
   * into list of tuples for all the query operators.
   */
  private long conversionOfResultSetToTuplesTime;
  
  /**
   * Time taken by all the query operators to propagate their snapshot
   * output.
   */
  private long snapshotPropagationTime;
  
  /**
   * Overall time taken to start the query.
   * Time taken by the 'alter query <qName> start' DDL.
   */
  private long totalStartTime;
  
  /**
   * Total number of records returned by the archiver query including
   * snapshot records received after querying BEAM_TRANSACTION_CONTEXT table.
   */
  private long numRecordsReturned;
  
  public ArchiverBasedQueryStatsRow(int id, String txt, String name, 
      boolean isMetadata, boolean isInternal, long numOut, long numOutHbts, long start, long end,
      long latest, long executions, long time, float avg, float per, 
      String orderingKind, long queryOpsIdentificationAndConstructionTime,
      long archiverQueryExecTime, long conversionOfResultSetToTuplesTime, 
      long snapshotPropagationTime, long totalStartTime, long numRecordsReturned)
  {
    super(id, txt, name, isMetadata, numOut, numOutHbts, start, end, 
        latest, executions, time, avg, per);
    this.queryOpsIdentificationAndConstructionTime = queryOpsIdentificationAndConstructionTime;
    this.archiverQueryExecTime = archiverQueryExecTime;
    this.conversionOfResultSetToTuplesTime = conversionOfResultSetToTuplesTime;
    this.snapshotPropagationTime = snapshotPropagationTime;
    this.totalStartTime = totalStartTime;
    this.numRecordsReturned = numRecordsReturned;
  }

  public long getQueryOpsIdentificationAndConstructionTime() {
    return queryOpsIdentificationAndConstructionTime;
  }

  public void setQueryOpsIdentificationAndConstructionTime(
      long queryOpsIdentificationAndConstructionTime) {
    this.queryOpsIdentificationAndConstructionTime = queryOpsIdentificationAndConstructionTime;
  }

  public long getArchiverQueryExecTime() {
    return archiverQueryExecTime;
  }

  public void setArchiverQueryExecTime(long archiverQueryExecTime) {
    this.archiverQueryExecTime = archiverQueryExecTime;
  }

  public long getConversionOfResultSetToTuplesTime() {
    return conversionOfResultSetToTuplesTime;
  }

  public void setConversionOfResultSetToTuplesTime(
      long conversionOfResultSetToTuplesTime) {
    this.conversionOfResultSetToTuplesTime = conversionOfResultSetToTuplesTime;
  }

  public long getSnapshotPropagationTime() {
    return snapshotPropagationTime;
  }

  public void setSnapshotPropagationTime(long snapshotPropagationTime) {
    this.snapshotPropagationTime = snapshotPropagationTime;
  }

  public long getTotalStartTime() {
    return totalStartTime;
  }

  public void setTotalStartTime(long totalStartTime) {
    this.totalStartTime = totalStartTime;
  }

  public long getNumRecordsReturned() {
    return numRecordsReturned;
  }

  public void setNumRecordsReturned(long numRecordsReturned) {
    this.numRecordsReturned = numRecordsReturned;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    sb.append("<ArchiverStatistics>");
    sb.append("<Num Archiver Records =\"" + numRecordsReturned + "\" >");
    sb.append("<Total Query Start Time =\"" + totalStartTime + "\" >");
    sb.append("<Query Operator Identification Time =\"" + queryOpsIdentificationAndConstructionTime + "\" >");
    sb.append("<Archiver Query Exec time =\"" + archiverQueryExecTime + "\" >");
    sb.append("<Time to convert result to tuples=\"" + conversionOfResultSetToTuplesTime + "\" >");
    sb.append("<Snapshot Propagation Time=\"" + snapshotPropagationTime + "\" >");
    sb.append("</ArchiverStatistics>");
    return sb.toString();
  }
}