/* $Header: LineageStore.java 17-dec-2007.16:25:11 parujain Exp $ */

/*
 DESCRIPTION
 Declares LineageStore in package oracle.cep.execution.stores.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  12/17/07 - db-join
    hopark    06/11/07 - logging - remove ExecContext
    hopark    05/24/07 - logging support
    najain    03/14/07 - cleanup
    najain    01/04/07 - spill over support
    parujain  12/07/06 - propagating relation
    najain    06/28/06 - add removeStub 
    najain    03/09/06 - complete
    skaluska  02/16/06 - Creation
    skaluska  02/16/06 - Creation
 */

/**
 *  @version $Header: LineageStore.java 17-dec-2007.16:25:11 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.ExecException;
import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * LineageStore
 *
 * @author skaluska
 */
public interface LineageStore {
  /**
   * Insert a tuple into the lineage synopsis stubId
   * @param tuple Tuple to be inserted
   * @param lineage Tuple lineage
   * @param stubId Lineage synopsis stubId
   */
  void insertTuple_l(ITuplePtr tuple, ITuplePtr[] lineage, int stubId) throws ExecException;

  /**
   * Delete a tuple from the lineage synopsis stubId
   * @param tuple Tuple to be deleted
   * @param stubId Lineage synopsis stubId
   */
  void deleteTuple_l(ITuplePtr tuple, int stubId) throws ExecException;
 
  /**
   * Get the scan with the specified lineage for the synopsis stubId
   * 
   * @param lineage
   *             Tuple lineage
   * @param stubId
   *             Lineage synopsis stubId
   * @return TupleIterator to scan all the tuples having same lineage
   * @throws ExecException
   */
  public TupleIterator getScan_l(ITuplePtr[] lineage, int stubId) throws ExecException;
  
  public void releaseScan_l(TupleIterator scan) throws ExecException;

  public abstract void removeStub(int stubId) throws ExecException;
  
  public TupleIterator getScan_r(int stubId) throws ExecException;
  
  public void releaseScan_r(TupleIterator iter, int stubId)
  throws ExecException;
}
