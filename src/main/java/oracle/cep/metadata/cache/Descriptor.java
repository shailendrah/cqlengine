/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/Descriptor.java /main/2 2008/09/30 17:08:42 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 Declares Descriptor in package oracle.cep.metadata.cache.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  09/24/08 - multiple schema support
    parujain  09/17/08 - multiple schema support
    skaluska  03/15/06 - Creation
    skaluska  03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/Descriptor.java /main/2 2008/09/30 17:08:42 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

/**
 * Descriptor to specify a cache object
 * 
 * @author skaluska
 */
public class Descriptor
{
  /** key */
  private Object          key;
  /** type */
  private CacheObjectType type;
  /** loadContext */
  private Object          loadContext;
  /** schema */
  private String          schema;

  /**
   * Constructor for Descriptor
   * 
   * @param key
   * @param type
   * @param loadContext
   */
  public Descriptor(Object key, CacheObjectType type, Object loadContext)
  {
    this.key = key;
    this.type = type;
    this.loadContext = loadContext;
    this.schema = null;
  }
  
  /**
   * Constructor for Descriptor
   * 
   * @param key
   * @param type
   * @param schema
   * @param loadContext
   */
  public Descriptor(Object key, CacheObjectType type, String schema, Object loadContext)
  {
    this.key = key;
    this.type = type;
    this.loadContext = loadContext;
    this.schema = schema;
  }
  
  /**
   * Getter for schema in Descriptor
   * 
   * @return Returns the schema
   */
  public String getSchema()
  {
    return schema;
  }
  
  /**
   * Setter for schema in Descriptor
   * 
   * @param schema Schema name
   */
  public void setSchema(String schema)
  {
    this.schema = schema;
  }

  /**
   * Getter for key in Descriptor
   * 
   * @return Returns the key
   */
  public Object getKey()
  {
    return key;
  }

  /**
   * Setter for key in Descriptor
   * 
   * @param key
   *          The key to set.
   */
  public void setKey(Object key)
  {
    this.key = key;
  }

  /**
   * Getter for loadContext in Descriptor
   * 
   * @return Returns the loadContext
   */
  public Object getLoadContext()
  {
    return loadContext;
  }

  /**
   * Setter for loadContext in Descriptor
   * 
   * @param loadContext
   *          The loadContext to set.
   */
  public void setLoadContext(Object loadContext)
  {
    this.loadContext = loadContext;
  }

  /**
   * Getter for type in Descriptor
   * 
   * @return Returns the type
   */
  public CacheObjectType getType()
  {
    return type;
  }

  /**
   * Setter for type in Descriptor
   * 
   * @param type
   *          The type to set.
   */
  public void setType(CacheObjectType type)
  {
    this.type = type;
  }
}
