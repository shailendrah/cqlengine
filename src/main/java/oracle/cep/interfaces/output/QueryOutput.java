/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutput.java /main/11 2010/01/05 07:19:30 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares QueryOutput in package oracle.cep.interfaces.output.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  12/06/09 - output batching
    sborah    10/12/09 - support for bigdecimal
    sbishnoi  06/22/09 - adding putNext to output a collection of tuples
    hopark    02/06/09 - move setEPR to QueryOutput
    sbishnoi  08/04/08 - support for nanosecond
    sbishnoi  03/19/08 - adding primary key information
    hopark    10/30/07 - remove IQueueElement
    parujain  10/18/07 - cep-bam integration
    najain    03/12/07 - bug fix
    anasrini  09/05/06 - 
    parujain  08/04/06 - Datatype Timestamp
    skaluska  04/04/06 - add kind to putNext 
    skaluska  03/27/06 - implementation
    skaluska  02/23/06 - Creation
    skaluska  02/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutput.java /main/11 2010/01/05 07:19:30 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces.output;

import java.util.ArrayList;
import java.util.Collection;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;

/**
 * Interface  that  the  STREAM  server  uses  to  produce  query  output.
 * Different kinds of outputs (e.g., write  to a file, write to a network)
 * can be obtained by extending this class in appropriate ways.
 * 
 * QueryOutput supports the method putNext(), which is used by teh server
 * to push the next output tuple to the output.
 *
 * Before pushing any tuples to the output, the server first indicates the
 * schema of the output tuples using setNumAttrs & setAttrInfo method
 * calls.  The method start() is called before the first putNext call:
 * this cen be used by the object to perform various initializations,
 * resources allocation etc.  
 *
 * @author skaluska
 */
public interface QueryOutput
{
  /**
   * Set the number of attributes in the schema of the tuples flowing
   * through this object.  Along with setAttrInfo, used to specify the
   * schema of the tuples flowing through.
   *
   * @param       numAttrs      number of attributes in the schema
   */
  public void setNumAttrs(int numAttrs);

   
  /**
   * Set the information about one particular attribute in the schema of
   * the output tuples.
   *
   * @param   attrPos           position of the attribute among
   *                            attributes in the schema
   * @param   attrMetadata      metadata of this attribute, ie, its datatype
   *                            , length, precision and scale
   */
  public void setAttrInfo(int attrPos, String attrName, 
                          AttributeMetadata attrMetadata);
  /**
   * Set whether the source is a stream or relation
   * 
   * @param isStrm
   *             True if stream else false if relation
   */
  public void setIsStream(boolean isStrm);

  /**
   * Signal that putNext method will be invoked ...
   * @throws CEPException 
   */

  public void start() throws CEPException;

  /**
   * Sets EPR arguments
   * It's from epr using '<Arguments>" tag and set to the QueryOutput instance.
   * @param eprArgs
   */
  public void setEprArgs(String[] eprArgs);
  
  /**
   * The next output in the output
   * @param  tuple            Tuple value
   * @param  k                Kind of tuple
   * @throws CEPException 
   */
  public void putNext(TupleValue tuple, QueueElement.Kind k) throws CEPException;
  
    
  /**
   * Next set of output tuples
   * @param insertTuples
   * @param deleteTuples
   * @param updateTuples
   * @throws CEPException
   */
  public void putNext(Collection<TupleValue> insertTuples,
                      Collection<TupleValue> deleteTuples,
		      Collection<TupleValue> updateTuples)
    throws CEPException;

  /**
   * Signal that no more putNext calls will be invoked
   * @throws CEPException 
   */
  public void end() throws CEPException;
  
  /**
   * Set Primary Key Attribute Position List
   * @param primaryKeyAttrPos
   */
  public void setPrimarKeyAttrList(ArrayList<Integer> primaryKeyAttrPos);
  
  /**
   * Set a flag to check whether time stamp unit should be converted prior
   * to write on output destination
   * @param convertTs
   */
  public void setConvertTs(boolean convertTs);
  
  /**
   * Set whether there is batching enabled for output tuples
   * @param isBatchOutput flag to set whether output tuples are batched or not
   */
  public void setBatchOutputTuples(boolean isBatchOutput);
  
  /** Increment the batch number*/
  public void incrementBatchNumber();
  
}
