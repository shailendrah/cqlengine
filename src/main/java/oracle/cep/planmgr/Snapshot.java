/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/Snapshot.java /main/1 2012/06/18 06:29:07 udeshmuk Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/16/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/Snapshot.java /main/1 2012/06/18 06:29:07 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr;

import java.util.Map;
import java.util.HashMap;

public class Snapshot
{
  /** unique snapshotId for this snapshot */
  private long snapshotId;

  /** snapshot information - workerId(BEAM ctx id), txnId(BEAM txn id) mapping */
  private Map<Long, Long> ctxTxnMap;

  public Snapshot()
  {
    this.ctxTxnMap = new HashMap<Long,Long>();
  }

  public long getSnapshotId()
  {
    return this.snapshotId;
  }

  public void setSnapshotId(long newSnapshotId)
  {
    this.snapshotId = newSnapshotId;
  }

  /**
   * Add snapshot info to this snapshot object
   */
  public void addSnapshotInfo(long workerId, long txnId)
  {
    ctxTxnMap.put(workerId, txnId);
  }

  /**
   * The method returns true if the argument <workerId,txnId>
   * is accounted for in this snapshot object.
   */
  public boolean isAccountedForInSnapshot(long workerId, long txnId)
  {
    if(!ctxTxnMap.isEmpty())
    {
      Long mappedTxnId = null;
      if((mappedTxnId = this.ctxTxnMap.get(workerId)) != null)
      {
        //if the argument txn id is smaller than or equal to the mapped txn id
	//then return true.
	//if the argument txn id is greater than the mapped txn id
	//then the mapping for that worker id is obsolete and hence 
	//can be deleted and we should return false. 
        if(mappedTxnId >= txnId)
	  return true;
	else if(mappedTxnId < txnId)
	{
	  ctxTxnMap.remove(workerId);
	}
      }
    }
    return false;
  }

  public boolean hasSnapshotInfo()
  {
    return !ctxTxnMap.isEmpty();
  }
}
