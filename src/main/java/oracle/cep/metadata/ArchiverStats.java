/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ArchiverStats.java /main/1 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/09/13 - stats related to archived relation framework - start
                           time
    udeshmuk    07/09/13 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ArchiverStats.java /main/1 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

public class ArchiverStats
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

  public ArchiverStats()
  {
    this.queryOpsIdentificationAndConstructionTime = 0L;
    this.archiverQueryExecTime = 0L;
    this.conversionOfResultSetToTuplesTime = 0L;
    this.snapshotPropagationTime = 0L;
    this.totalStartTime = 0L;
    this.numRecordsReturned = 0L;
  }
  
  public boolean isInitialized()
  {
    return queryOpsIdentificationAndConstructionTime > 0L ||
           archiverQueryExecTime > 0L ||
           conversionOfResultSetToTuplesTime > 0L ||
           snapshotPropagationTime > 0L ||
           numRecordsReturned > 0L;
  }

  /**
   * @return the queryOpsIdentificationAndConstructionTime
   */
  public long getQueryOpsIdentificationAndConstructionTime() {
    return queryOpsIdentificationAndConstructionTime;
  }

  /**
   * @param queryOpsIdentificationAndConstructionTime the queryOpsIdentificationAndConstructionTime to set
   */
  public void setQueryOpsIdentificationAndConstructionTime(
      long queryOpsIdentificationAndConstructionTime) {
    this.queryOpsIdentificationAndConstructionTime = queryOpsIdentificationAndConstructionTime;
  }

  /**
   * @return the archiverQueryExecTime
   */
  public long getArchiverQueryExecTime() {
    return archiverQueryExecTime;
  }

  /**
   * @param archiverQueryExecTime the archiverQueryExecTime to set
   */
  public void setArchiverQueryExecTime(long archiverQueryExecTime) {
    this.archiverQueryExecTime = archiverQueryExecTime;
  }

  /**
   * @return the conversionOfResultSetToTuplesTime
   */
  public long getConversionOfResultSetToTuplesTime() {
    return conversionOfResultSetToTuplesTime;
  }

  /**
   * @param conversionOfResultSetToTuplesTime the conversionOfResultSetToTuplesTime to set
   */
  public void setConversionOfResultSetToTuplesTime(
      long conversionOfResultSetToTuplesTime) {
    this.conversionOfResultSetToTuplesTime = conversionOfResultSetToTuplesTime;
  }

  /**
   * @return the snapshotPropagationTime
   */
  public long getSnapshotPropagationTime() {
    return snapshotPropagationTime;
  }

  /**
   * @param snapshotPropagationTime the snapshotPropagationTime to set
   */
  public void setSnapshotPropagationTime(long snapshotPropagationTime) {
    this.snapshotPropagationTime = snapshotPropagationTime;
  }

  /**
   * @return the totalStartTime
   */
  public long getTotalStartTime() {
    return totalStartTime;
  }

  /**
   * @param totalStartTime the totalStartTime to set
   */
  public void setTotalStartTime(long totalStartTime) {
    this.totalStartTime = totalStartTime;
  }

  /**
   * @return the numRecordsReturned
   */
  public long getNumRecordsReturned() {
    return numRecordsReturned;
  }

  /**
   * @param numRecordsReturned the numRecordsReturned to set
   */
  public void setNumRecordsReturned(long numRecordsReturned) {
    this.numRecordsReturned = numRecordsReturned;
  }
  
}