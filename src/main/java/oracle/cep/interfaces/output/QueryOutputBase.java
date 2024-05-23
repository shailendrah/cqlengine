/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutputBase.java /main/11 2010/01/05 07:19:30 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    A base class implementation of the QueryOutput interface

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/06/09 - adding default implementation for batching api
    sborah      10/12/09 - support for bigdecimal
    sbishnoi    06/22/09 - implementing putNext to output a collection of
                           tuples
    hopark      02/06/09 - move setEPR to QueryOutput
    hopark      12/04/08 - add toString
    hopark      10/10/08 - remove statics
    sbishnoi    08/04/08 - support for nanosecond
    sbishnoi    03/10/08 - adding primary key information
    sbishnoi    02/18/08 - added epr
    parujain    10/18/07 - cep-bam integration
    anasrini    10/24/06 - use TupleValueHelper
    dlenkov     10/18/06 - byte data type support
    parujain    10/06/06 - interval datatype
    anasrini    09/05/06 - support for attr name
    anasrini    08/18/06 - Base class that implements QueryOutput
    anasrini    08/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutputBase.java /main/11 2010/01/05 07:19:30 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.interfaces.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.service.ExecContext;


/**
 * A base class implementation of the QueryOutput interface
 *
 * @since 1.0
 */

public abstract class QueryOutputBase implements QueryOutput 
{
  protected ExecContext execContext;
  
  protected AttributeMetadata[] attrMetadata;

  /** Lengths of attributes */
  protected String[]   attrNames;

  /** Number of attributes */
  protected int        numAttrs;

  /** Whether stream or relation */
  protected boolean    isStream;
  
  /** Arguments passed via EPR*/
  protected String[]   eprArgs;

  /** Primary key Attributes*/
  protected ArrayList<Integer> primaryKeyAttrPos;
  
  /** Flag to check if Primary key exists */
  protected boolean isPrimaryKeyExist;
  
  /** Array of flags to check which attribute is a primary key attribute */
  protected boolean[] isPrimaryKeyAttr;
  
  /** flag to check whether we should convert time-stamp unit from
   *  nanosecond to millisecond */
  protected boolean convertTs;
  
  /** flag to check if output batching is enabled */
  protected boolean isBatchOutput;   
  
  /** batch number of current output tuple */
  protected long    currentBatchNumber;
  
  protected QueryOutputBase(ExecContext ec) 
  {
    execContext = ec;
    isStream = true;
    currentBatchNumber = 1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.output.QueryOutput#setNumAttrs(int)
   */
  public void setNumAttrs(int numAttrs)
  {
    this.numAttrs     = numAttrs;
    this.attrMetadata = new AttributeMetadata[numAttrs];
    attrNames         = new String[numAttrs];
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.interfaces.output.QueryOutput#setAttrInfo(int,
   *      oracle.cep.common.Datatype, int)
   */
  public void setAttrInfo(int attrPos, String attrName,
                          AttributeMetadata attrMetadata)
  {
    assert attrPos < numAttrs;
   
    // Set attribute name
    attrNames[attrPos]         = attrName;
    
    this.attrMetadata[attrPos] = attrMetadata;
  }
 
  public void setIsStream(boolean isStrm)
  {
    this.isStream = isStrm;
  }
  
  @Override
  public void setEprArgs(String[] eprArgs)
  {
    this.eprArgs = new String[eprArgs.length];
    System.arraycopy(eprArgs, 0, this.eprArgs, 0, eprArgs.length);
  }
  
  public void setPrimarKeyAttrList(ArrayList<Integer> primaryKeyAttrPos)
  {
    this.primaryKeyAttrPos = primaryKeyAttrPos;
    isPrimaryKeyExist = 
      (primaryKeyAttrPos == null || primaryKeyAttrPos.size() == 0) 
      ? false : true;
    isPrimaryKeyAttr = new boolean[numAttrs];
    Arrays.fill(isPrimaryKeyAttr, false);
    // Populate primary key flag array if any primary key exists
    if(isPrimaryKeyExist)
      populatePKeyFlags();
    
  }
  
  private void populatePKeyFlags() {
    for(int i = 0 ; i < primaryKeyAttrPos.size(); i++)
      isPrimaryKeyAttr[primaryKeyAttrPos.get(i)] = true;
  }

  /**
   * Set a flag whether we should convert time stamp unit before writing
   * into a file
   * @param convertTs
   */
  public void setConvertTs(boolean convertTs) {
    this.convertTs = convertTs; 
  }

  protected String toString(String info)
  {
    StringBuilder sinfo = new StringBuilder();
    sinfo.append(info);
    sinfo.append(" : ");
    sinfo.append(isStream ? "stream":"relation");
    sinfo.append(" ( ");
    for (int i = 0; i < numAttrs; i++)
    {
      sinfo.append( attrNames[i] );
      sinfo.append(" ");
      sinfo.append(attrMetadata[i].getDatatype().name());
      if (i < (numAttrs-1))
        sinfo.append(", ");
    }
    sinfo.append(" ) ");
    if (isPrimaryKeyExist)
    {
      sinfo.append("primaryKeys(");
      for (Integer pos : primaryKeyAttrPos)
      {
        sinfo.append(pos);
        sinfo.append(" ");
      }
      sinfo.append("primaryKeys)");
    }
    if (convertTs) sinfo.append(" converted ");
    return sinfo.toString();
  }
 
  /**
   * Default implementation of 
   * QueryOutput.putNext(Collection,Collection,Collection)
   */
  public void putNext(Collection<TupleValue> insertTuples,
                      Collection<TupleValue> deleteTuples,
		      Collection<TupleValue> updateTuples)
    throws CEPException
  {
     java.util.Iterator<TupleValue> iter = null;
     if(insertTuples != null)
     {
       iter = insertTuples.iterator();
       while(iter.hasNext())
       {
         putNext(iter.next(), QueueElement.Kind.E_PLUS);
       }
     }

     if(deleteTuples != null)
     {
       iter = deleteTuples.iterator();
       while(iter.hasNext())
       {
         putNext(iter.next(), QueueElement.Kind.E_MINUS);
       }
     }

     if(updateTuples != null)
     {
       iter = updateTuples.iterator();
       while(iter.hasNext())
       {
         putNext(iter.next(), QueueElement.Kind.E_UPDATE);
       }
     }
     
  }
  
  public void setBatchOutputTuples(boolean isBatchOutput)
  {
    this.isBatchOutput = isBatchOutput;
  }
  
  public void incrementBatchNumber()
  {
    currentBatchNumber++;    
  }
}


