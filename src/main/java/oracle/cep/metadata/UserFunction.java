/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/UserFunction.java /main/17 2011/09/11 11:26:45 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Base class metadata object for user defined functions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    09/09/11 - XbranchMerge anasrini_bug-12943064_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    09/07/11 - separate builtIn and nativeImpl concepts
    parujain    10/02/09 - dependency map
    parujain    01/14/09 - metadata in-mem
    parujain    09/12/08 - multiple schema support
    skmishra    08/21/08 - imports
    parujain    02/07/08 - parameterizing errors
    hopark      01/17/08 - dump
    mthatte     08/22/07 - 
    parujain    05/02/07 - function creation text
    hopark      03/21/07 - move the store integration part to CacheObject
    parujain    01/30/07 - drop function
    parujain    02/02/07 - BDB integration
    parujain    01/11/07 - BDB integration
    najain      09/13/06 - add built-in
    parujain    09/11/06 - MDS Integration
    parujain    07/13/06 - check locks 
    anasrini    07/06/06 - Support for aggregate functions 
    parujain    06/29/06 - metadata cleanup 
    anasrini    06/12/06 - support for user functions 
    najain      04/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/UserFunction.java /main/16 2009/11/23 21:21:22 parujain Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpable;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;


/**
 * Base class metadata object for user defined functions
 * 
 * @author najain
 * @since 1.0
 */

@DumpDesc(autoFields=true,
          attribTags={"Id", "Key"}, 
          attribVals={"getId", "getKey"})
public abstract class UserFunction extends CacheObject 
  implements IUserFunctionMetadata, IDumpable, Cloneable{

  public static final String DUMMY = "$dummy";
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** parameter specification list */
  private List<Attribute> paramList;

  /** return type of the function */
  private Datatype        returnType;

  /** implementation class name */
  private String          implClassName;

  private boolean         builtIn;
  
  /** UserDefined Functions creation text */
  private String          creationTxt;

  /** implementation instance name */
  private String          implInstanceName;

  /**
   * Constructor for UserFunction
   * 
   * @param name
   *          UserFunction name
   */
  protected UserFunction(String name, String schema, CacheObjectType type) {
    super(name, schema, type);
    this.paramList = new ArrayList<Attribute>();
    builtIn = false;
    creationTxt = null;
  }
  
  @SuppressWarnings("unchecked")
  public UserFunction clone() throws CloneNotSupportedException {
    UserFunction func = (UserFunction)super.clone();
    func.paramList = (ArrayList<Attribute>)((ArrayList<Attribute>)paramList).clone();
    return func;
  }


  /**
   * Get function name
   * 
   * @return Function name
   */
  @Override
  public String getName() 
  {
    return (String) getKey();
  }
  
  public void setCreationText(String text)
  {
    creationTxt = text;
  }
  
  public String getCreationText()
  {
    return creationTxt;
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
   * Get parameter by name
   * 
   * @param name
   *          parameter name
   * @return parameter metadata
   * @throws MetadataException
   */
  public Attribute getParam(String name) throws MetadataException {
    Iterator<Attribute> i;
    Attribute           a = null;
    boolean             found = false;

    assert name != null;

    // Find attribute
    i = paramList.iterator();
    while (i.hasNext()) {
      a = i.next();
      if (a.getName().equals(name)) {
        found = true;
        break;
      }
    }

    if (!found)
      throw new MetadataException(MetadataError.PARAMETER_NOT_FOUND, new Object[]{name});

    return a;
  }

  /** 
   * Does this funciton have a native implementation as opposed to 
   * implementation via a call to a class implementing extensible Function 
   * interface
   * @return true iff this function has a native implementation
   */
  public boolean isImplNative() {
    if (implClassName != null && implClassName.equalsIgnoreCase(DUMMY))
      return true;

    return false;
  }

  /** 
   * Check whether the function is a builtIn function
   * @return whether the function is a builtIn function
   */
  public boolean isBuiltIn() {
    return builtIn;
  }

  /** 
   * Set whether the function is a builtIn function
   * @param builtIn whether the function is a builtIn function
   */
  public void setBuiltIn(boolean builtIn) {
    
    // check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.builtIn = builtIn;
  }

  /** 
   * Set the return type of the function
   * @param returnType the return type of the function
   */
  public void setReturnType(Datatype returnType) {
    
    // check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.returnType = returnType;
  }

  /** 
   * Get the return type of the function
   * @return the return type of the function
   */
  public Datatype getReturnType() {
    return returnType;
  }

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
  
  public void setImplInstanceName(String implInstanceName) {
    //  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.implInstanceName = implInstanceName;
  }
  
  public String getImplInstanceName() {
    return implInstanceName;
  }
  
}
