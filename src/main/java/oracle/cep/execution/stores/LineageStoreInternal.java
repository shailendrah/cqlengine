package oracle.cep.execution.stores;

import java.util.ArrayList;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Column;

/**
 * This API is for restoring lineage store for HA processing.
 * @author sbishnoi
 *
 */
public interface LineageStoreInternal
{
  /**
   * Insert Tuple into Lineage Store which are recovered during snapshot load
   * @param tuple
   * @param lineage
   * @param stubId
   * @throws ExecException
   */
  void insertTuple_l(ITuplePtr tuple, long[] lineage, int stubId) throws ExecException;
  
  /**
   * Return number of lineage tuples
   * @return
   */
  public int getNumLins();
  
  /**
   * Return the lineage columns
   * @return
   */
  public ArrayList<Column> getColLineage();
}
