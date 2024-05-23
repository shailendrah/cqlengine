/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/SimpleFunctionSet.java /main/9 2009/01/16 22:55:00 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/14/09 - metadata in-mem
    parujain    09/12/08 - multiple schema support
    skmishra    08/21/08 - imports
    mthatte     08/22/07 - 
    hopark      03/21/07 - move the store integration part to CacheObject
    parujain    01/31/07 - drop function
    parujain    02/02/07 - BDB integration
    parujain    01/11/07 - BDB integration
    parujain    01/09/07 - bdb integration
    parujain    11/21/06 - Type conversion overloading
    dlenkov     10/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/SimpleFunctionSet.java /main/9 2009/01/16 22:55:00 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Iterator;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.ProcedureMetadataDescriptor;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

/**
 * Metadata object for simple functions
 * 
 * @author dlenkov
 * @since 1.0
 */

public class SimpleFunctionSet extends CacheObject implements Cloneable {

  
/**
   * 
   */
  private static final long serialVersionUID = 1L;
/** list of (overloaded) functions */
  private ArrayList<Integer> funcs;
  
 
  /**
   * Constructor for SimpleFunctionSet
   * 
   * @params 
   */

  SimpleFunctionSet( String name, String schema) {
    super( name, schema, CacheObjectType.SIMPLE_FUNCTION_SET);
    funcs = new ArrayList<Integer>();
  }
  
  @SuppressWarnings("unchecked")
  public SimpleFunctionSet clone() throws CloneNotSupportedException {
    SimpleFunctionSet set = (SimpleFunctionSet)super.clone();
    set.funcs = (ArrayList<Integer>)funcs.clone();
    return set;
  }


  /**
   * Adds another overloaded function to the list.
   * There is no need to verify the duplicacy, as that
   * will be automatically done when registering any function
   * @params 
   */

  public void addFunc(int fid) throws MetadataException
  {
     funcs.add(new Integer(fid));
  }
  
  public boolean removeFunc(int fid) throws MetadataException
  {
    Iterator<Integer> iter = funcs.iterator();
    while(iter.hasNext())
    {
      if(iter.next().intValue() == fid)
      {
        iter.remove();
        return true;
      }
    }
    return false;
  }
  
  public boolean isEmpty()
  {
    return funcs.isEmpty();
  }

 /**
  * Get the function Id
  * 
  * @param position Position within the List of Function ids
  * @return Function Id
  */ 
  public int getFuncId(int position)
  {
    assert position >= 0;
    return funcs.get(position).intValue();
  }
  /**
   * Get function name
   * 
   * @return Function name
   */
  public String getName() {
    return (String) getKey();
  }

  /**
   * Get the number of overloaded functions
   * 
   * @return Count of overloaded function with a given name
   */
  public int getNumOverloads() {
    return funcs.size();
  }

  public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
		return new ProcedureMetadataDescriptor(this.getName(),DatabaseMetaData.procedureResultUnknown);  
 }
}
