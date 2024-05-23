/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Window.java /main/10 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/24/09 - dependency support
    parujain    01/14/09 - metadata in-mem
    parujain    09/12/08 - multiple schema support
    skmishra    08/21/08 - imports
    parujain    02/07/08 - parameterizing errors
    hopark      01/17/08 - dump
    mthatte     08/22/07 - 
    hopark      03/21/07 - move the store integration part to CacheObject
    parujain    03/19/07 - drop window
    parujain    03/05/07 - Window Object
    parujain    03/05/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Window.java /main/10 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpable;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

@DumpDesc(autoFields=true,
          attribTags={"Id", "Key"}, 
          attribVals={"getId", "getKey"})
public class Window extends CacheObject implements IDumpable, Cloneable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /** parameter specification list */
  private List<Attribute> paramList;
  
  /** implementation class name */
  private String          implClassName;
  
  /** List of query ids using this window */ 
 // protected LinkedList<Integer> destQueries;
  
  public Window(String name, String schema) {
    super(name, schema,CacheObjectType.WINDOW);
    this.paramList = new ArrayList<Attribute>();
  //  destQueries = new LinkedList<Integer>();
  }
  
  @SuppressWarnings("unchecked")
  public Window clone() throws CloneNotSupportedException {
    Window window = (Window)super.clone();
 //   window.destQueries = (LinkedList<Integer>)destQueries.clone();
    window.paramList = (ArrayList<Attribute>)((ArrayList<Attribute>)paramList).clone();
    return window;
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
   * Add a parameter
   * 
   * @param param
   *          Parameter metadata
   * @throws MetadataException
   */
  public void addParam(Attribute param) throws MetadataException {
    Iterator<Attribute> i;
    Attribute           a;
    
    // check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    // Check for duplicates
    i = paramList.iterator();
    for (; i.hasNext();) {
      a = i.next();
      if (a.getName().equals(param.getName()))
        throw new MetadataException(MetadataError.PARAMETER_ALREADY_EXISTS,
        		                    new Object[]{a.getName()});
    }

    // Set the position and add to list
    param.setPosition(getNumParams());
    paramList.add(param);
  }
  
  /**
   * Get number of parameters
   * 
   * @return Number of parameters
   */
  public int getNumParams() {
    return paramList.size();
  }
  
  /**
   * Get parameter by position
   * 
   * @param pos
   *          Parameter position
   * @return Parameter metadata
   * @throws MetadataException
   */
  public Attribute getParam(int pos) throws MetadataException {
    // Validate position
    if (pos > paramList.size())
      throw new MetadataException(MetadataError.PARAMETER_NOT_FOUND_AT_DEF_POS, new Object[]{pos});

    // Return attribute
    return paramList.get(pos);
  }
  
  /**
   * Add a queryid which is using this window
   * 
   * @param queryId
   *              Queryid using this window
   */
//  public void addDestQuery(int queryId) {
//    Iterator<Integer> iter = destQueries.iterator();
//    while(iter.hasNext())
//    {
//      int val = iter.next().intValue();
//      if(val == queryId)
//        return;
//    }
//    destQueries.add(new Integer(queryId));
//  }
  
  /**
   * Removes a queryid while drop query
   * 
   * @param queryId
   *             Id of the query getting dropped
   * @return TRUE/FALSE
   */
//  public boolean removeDestQuery(int queryId) {
//    Iterator<Integer> iter = destQueries.iterator();
//    while(iter.hasNext())
//    {
//      int val = iter.next().intValue();
//      if(val == queryId)
//      {
//        iter.remove();
//        return true;  //successfully removed
//      }
//    }
//    return false;
//  }
  
  /**
   * Set the name of the implementation class
   * @param implClassName the name of implementation class
   */
  public void setImplClassName(String implClassName) {
    
//  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.implClassName = implClassName;
  }

  /**
   * Get the name of the implementation class
   * @return the name of implementation class
   */
  public String getImplClassName() {
    return implClassName;
  }

public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Not supported by: " + this.getClass());
}
}
