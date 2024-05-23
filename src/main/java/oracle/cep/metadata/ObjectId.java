/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ObjectId.java /main/10 2009/08/31 10:57:00 alealves Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ObjectId in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  01/28/09 - transaction mgmt
 parujain  01/14/09 - metadata in-mem
 parujain  09/24/08 - multiple schema
 parujain  09/12/08 - multiple schema support
 skmishra  08/21/08 - imports
 mthatte   08/23/07 - Setting isSystem during table creation.
 mthatte   08/22/07 - 
 hopark    03/21/07 - move the store integration part to CacheObject
 parujain  02/02/07 - BDB integration
 parujain  01/11/07 - BDB integration
 parujain  01/09/07 - bdb integration
 parujain  09/11/06 - MDS Integration
 parujain  07/13/06 - check locks 
 skaluska  03/12/06 - Creation
 skaluska  03/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ObjectId.java /main/10 2009/08/31 10:57:00 alealves Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.transaction.ITransaction;

/**
 * ObjectId
 *
 * @author skaluska
 */
public class ObjectId extends CacheObject implements Cloneable
{
  
/**
   * 
   */
  private static final long serialVersionUID = 1L;

/** object name */
  private String              objectName;

  /** object type */
  private CacheObjectType     objectType;

  /** next object id sequence */
//  private transient static final String nextId = "CEP$_NEXT_OBJECT_ID";
  private static AtomicInteger    next_object_id= new AtomicInteger(0);

  /**
   * Constructor for ObjectId
   * 
   * @param id
   *          Object id
   * @param schema
   *          Schema name
   * @param nm
   *          Object name
   */
  ObjectId(int id)
  {
    super(new Integer(id), null, CacheObjectType.OBJECTID);
  }
 
  public ObjectId clone() throws CloneNotSupportedException {
    ObjectId obj = (ObjectId)super.clone();
    return obj;
  }


  /**
   * Getter for object id in ObjectId
   * 
   * @return Returns the object id
   */
  public int getId()
  {
    return ((Integer) getKey()).intValue();
  }

  /**
   * Getter for name in ObjectId
   * 
   * @return Returns the name
   */
  public String getObjectName()
  {
    return objectName;
  }

  /**
   * Setter for name in ObjectId
   * 
   * @param name
   *          The name to set.
   */
  public void setObjectName(String name)
  {
//  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.objectName = name;
  }

  /**
   * Getter for object type in ObjectId
   * 
   * @return Returns the type
   */
  public CacheObjectType getObjectType()
  {
    return objectType;
  }

  /**
   * Setter for object type in ObjectId
   * 
   * @param type
   *          The type to set.
   */
  public void setObjectType(CacheObjectType type)
  {
//  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    this.objectType = type;
  }

  /**
   * Get a new object id
   * 
   * @param cache
   *          Cache
   * @param nm
   *          New object name
   * @param t
   *          New object type
   * @return The new object id
   */
  public static int getNewObjectId(ITransaction txn, Cache cache, 
		  String nm, String schema, CacheObjectType t)
  {
    //CacheLock l = null;
    CacheLock idl = null;
   // Table nextObj;
    ObjectId map;
    int nextObjId;

    nextObjId = getNextId();

    // Create the mapping
    idl = cache.create(txn, new Descriptor((Integer) nextObjId,
                       CacheObjectType.OBJECTID, schema, null));
    assert (idl != null);
      
    map = (ObjectId) idl.getObj();
    map.setObjectName(nm);
    map.setObjectType(t);
    map.setSchema(schema);
   
    return nextObjId;
  }
  
  public static int getNextId() {
    return next_object_id.incrementAndGet();
  }
  

  public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Not supported by: " + this.getClass());}

}
