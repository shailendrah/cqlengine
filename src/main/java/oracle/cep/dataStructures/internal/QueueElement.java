/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/QueueElement.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares Element in package oracle.cep.execution.queues.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  04/17/12 - add snapshotId methods
 anasrini  03/22/11 - setTupleValue
 udeshmuk  01/16/09 - total ordering optimization
 hopark    01/31/08 - queue optimization
 hopark    01/26/08 - fix dump
 hopark    01/01/08 - support xmllog
 sbishnoi  11/27/07 - support for update semantics; add E_UPDATE
 hopark    10/25/07 - add equals
 hopark    09/11/07 - use ITuplePtr
 hopark    06/19/07 - cleanup
 hopark    05/28/07 - logging support
 najain    03/14/07 - Cleanup
 najain    03/12/07 - bug fix
 najain    01/04/07 - spill over support
 najain    07/05/06 - cleanup
 skaluska  04/04/06 - add copyTs 
 skaluska  02/07/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/QueueElement.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.logging.DumpDesc;

/**
 *() @author skaluska
 */
@DumpDesc(attribTags={"Kind", "Ts"}, 
    attribVals={"@kind", "@ts"},
    valueTags={"Tuple"},
    values={"getPinnedTuple"})
public interface QueueElement extends IPersistable
{
  public enum Kind
  {
    E_PLUS, E_MINUS, E_HEARTBEAT, E_UPDATE;

    public static Kind fromOrdinal(int ord)
    {
      Kind[] vals = Kind.values();
      assert (ord >= 0 && ord < vals.length) : "unknown ordinal";
      return vals[ord];
    }
  }

  /**
   * treat it as a fresh Element
   */
  void clear();

  /**
   * Copy Element - this is a shallow copy
   * 
   * @param e
   *          Element to copy
   */
  void copy(QueueElement e);

  /**
   * @return Returns the kind.
   */
  Kind getKind();

  /**
   * @return Returns the ts.
   */
  long getTs();

  /**
   * @return Returns the tuple.
   */
  ITuplePtr getTuple();

  /**
   * @return Returns the tupleValue.
   */
  TupleValue getTupleValue();

  /**
   * Get pinned tuple
   */
  public ITuple getPinnedTuple();
  
  /**
   * @param kind
   *          The kind to set.
   */
  void setKind(Kind kind);

  /**
   * @param ts
   *          The ts to set.
   */
  void setTs(long ts);

  /**
   * @param tuple
   *          The tuple to set.
   */
  void setTuple(ITuplePtr tuple);

  void setTupleValue(TupleValue tupleValue);
  
  void heartBeat(long t);

  boolean getTotalOrderingGuarantee();

  void setTotalOrderingGuarantee(boolean isGuaranteed);

  long getSnapshotId();

  void setSnapshotId(long snapshotId);
  
}

