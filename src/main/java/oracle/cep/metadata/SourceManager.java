/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SourceManager.java /main/18 2014/10/14 06:35:32 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi 09/23/14 - support for partitioned stream
 udeshmuk 09/18/14 - add isPartnStream
 vikshukl 07/30/12 - archived dimension relation
 udeshmuk 06/05/12 - return isArchived for view as well
 udeshmuk 07/12/11 - archived relation support
 anasrini 03/21/11 - add method to getCreateDDL
 sborah   07/15/09 - support for bigdecimal
 parujain 01/28/09 - transaction mgmt
 hopark   10/10/08 - remove statics
 hopark   10/09/08 - remove statics
 hopark   10/07/08 - use execContext to remove statics
 parujain 09/12/08 - multiple schema support
 hopark   03/26/08 - server reorg
 parujain 05/07/08 - lock problem
 sbishnoi 05/07/08 - fix lock issue
 parujain 05/07/08 - lock problem
 sbishnoi 05/07/08 - fix lock issue
 mthatte  04/14/08 - adding isTableObject
 mthatte  02/26/08 - parametrizing metadata errors
 parujain 02/07/08 - parameterizing error
 parujain 11/09/07 - external source
 mthatte  10/30/07 - adding isOnDemand(int id)
 parujain 06/21/07 - release read lock
 hopark   03/21/07 - storage re-org
 parujain 01/12/07 - BDB integration
 anasrini 09/13/06 - add getAttrNames
 najain   08/28/06 - add getAttrName
 parujain 07/14/06 - moved CacheObjectManager 
 parujain 07/10/06 - Namespace Implementation 
 parujain 06/29/06 - metadata cleanup 
 najain   05/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SourceManager.java /main/18 2014/10/14 06:35:32 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;


/**
 * This class manages the system metadata for all source managers (which
 * includes TableManager and ViewManager currently)
 *
 * @since 1.0
 */
public abstract class SourceManager extends CacheObjectManager
{
  protected ExecContext execContext;
  
  SourceManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    execContext = ec;
  }
  
  public boolean isPartnStream(int id) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isPartnStream = false;
        
    try
    {
      // Lookup object
      l = findCache(txn, id, false);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
                                            new Object[]{id});
      if(l.getObj().getType() == CacheObjectType.TABLE)
      {
        Table t = (Table) l.getObj();
        isPartnStream = t.isPartitioned();
      }
      if(l.getObj().getType() == CacheObjectType.VIEW)
      {
        View v = (View) l.getObj();
        isPartnStream = v.isPartitioned();
      }
      
    }
    finally
    {
      // Release
      if (l != null )
        release(txn, l);
    }
  
    return isPartnStream;
  }
  
  public boolean isArchived(int id) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isArchived = false;
        
    try
    {
      // Lookup object
      l = findCache(txn, id, false);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
                                            new Object[]{id});
      //FIXME: If cache object is a VIEW then we can return isArchived as false
      //       Confirm.
     
      if(l.getObj().getType() == CacheObjectType.TABLE)
      {
        Table t = (Table) l.getObj();
        isArchived = t.isArchived();
      }    
      else if(l.getObj().getType() == CacheObjectType.VIEW)
      {
        View v = (View) l.getObj();
        isArchived = v.isArchived();
      }
      
    }
    finally
    {
      // Release
      if (l != null )
        release(txn, l);
    }
    
    return isArchived;
  }
 
  /**
   * Return whether the archived relation is a dimension.
   * @param id
   * @return
   * @throws MetadataException
   */
  public boolean isDimension(int id) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isDimension = false;        
    try
    {
      // Lookup object
      l = findCache(txn, id, false);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
                                            new Object[]{id});

      if (l.getObj().getType() == CacheObjectType.TABLE)
      {
        Table t = (Table) l.getObj();
        isDimension = t.isDimension();
      }    
      else if (l.getObj().getType() == CacheObjectType.VIEW)
      {
        View v = (View) l.getObj();
        isDimension = v.isDimension();
      }      
    }
    finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }
    
    return isDimension;
  }
  
  public boolean isTableObject(int id) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isTableObj = false;
        
    try
    {
      // Lookup object
      l = findCache(txn, id, false);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
                                            new Object[]{id});

      isTableObj = (l.getObj().getType() == CacheObjectType.TABLE);
      
    }
    finally
    {
      // Release
      if (l != null )
        release(txn, l);
    }
    
    return isTableObj;

  }
  /**
   * Get Stream/Relation/View Id
   * 
   * @param name
   *          Stream/Relation name
   * @return Id
   * @throws MetadataException
   */
  public int getId(String name, String schema) throws MetadataException
  {
    CacheLock l = null;
    
    ITransaction txn = execContext.getTransaction();
  
    // Namespace is always Source
     l = findCache(txn, name, schema, NameSpace.SOURCE, false );
     
    // Object not found. Throw exception with name. 
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {name});
      
    try
      {
        // Get the id
        int id = ((Source) l.getObj()).getId();
     return id;
    }
    finally {
        // release read lock acquired
      if(l != null)
        release(txn, l);
      }

    
    
  }

  public String getTableName(int id) throws MetadataException
  {
    CacheLock l = null;
    Source src;
    String tblName;
    ITransaction txn = execContext.getTransaction();
    
    // Lock Table
    l = findCache(txn, id, false);

    // If object not found then throw exception
    if (l == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
      		                    new Object[]{id});

    src = (Source) l.getObj();
    tblName  = src.getName();
    release(txn, l);
   
    return tblName;
  }


  public int getDegreeOfParallelism(int id) throws MetadataException
  {
    CacheLock l = null;
    Source src;
    int dop;
    ITransaction txn = execContext.getTransaction();
    
    // Lock Table
    l = findCache(txn, id, false);

    // If object not found then throw exception
    if (l == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
      		                    new Object[]{id});

    src = (Source) l.getObj();
    dop  = src.getDegreeOfParallelism();
    release(txn, l);
   
    return dop;
  }

  public String getCreateDDL(int id) throws MetadataException
  {
    CacheLock l = null;
    Source src;
    String createDDL;
    ITransaction txn = execContext.getTransaction();
    
    // Lock Table
    l = findCache(txn, id, false);

    // If object not found then throw exception
    if (l == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
      		                    new Object[]{id});

    src = (Source) l.getObj();
    createDDL  = src.getCql();
    release(txn, l);
   
    return createDDL;
  }


  /**
   * Get number of attributes for the specified object
   * 
   * @param id
   *          Object id
   * @return Number of attributes
   * @throws MetadataException
   */
  public int getNumAttrs(int id) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();

    
    l = findCache(txn, id,false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});
    try
      {
        // Get the number of attributes
        int numAttrs = ((Source) l.getObj()).getNumAttrs();
      return numAttrs;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }

    
    
    
  }

  /**
   * Get Attribute Id
   * 
   * @param id
   *          Object id for stream or relation
   * @param attrName
   *          Attribute name
   * @return attribute id
   * @throws MetadataException
   */
  public int getAttrId(int id, String attrName) throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();

    l = findCache(txn, id,false);

    //  Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {attrName});  

    try{
        // Get the position of the attribute
      int attrPos = ((Source) l.getObj()).getAttribute(attrName).getPosition();        
      return attrPos;
    }    
    finally
    {
        // Release read lock
      if(l != null)
        release(txn, l);
      }
    
    
  }

  /**
   * Get attribute type
   * 
   * @param id
   *          Object id for the object (stream/relation/view)
   * @param attrId
   *          Attribute id
   * @return Attribute datatype
   * @throws MetadataException
   */
  public Datatype getAttrType(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
    
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found. Throw exception with id since name is unknown.
    if(l == null)  
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});
    
    try
      {
        // Get the datatype of the attribute
      Datatype attrType = ((Source) l.getObj()).getAttribute(attrId).getType();
      return attrType;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }
  }

  /**
   * Get attribute name
   * 
   * @param id
   *          Object id for the object (stream/relation/view)
   * @param attrId
   *          Attribute id
   * @return Attribute name
   * @throws MetadataException
   */
  public String getAttrName(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
    
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found. Throw an exception with the id, at this place we don't know
    // the name of the object
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});
    
    try
      {
        // Get the datatype of the attribute
      String attrname = ((Source) l.getObj()).getAttribute(attrId).getName();
      return attrname;
    }
    finally{
        // Release read lock
      if(l != null)
        release(txn, l);
      }
  }

  /**
   * Get attribute name
   * 
   * @param id
   *          Object id for the object (stream/relation/view)
   * @return array of attribute names
   * @throws MetadataException
   */
  public String[] getAttrNames(int id) throws MetadataException
  {
    CacheLock l = null;
    
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});
    
    try
      {
        Source   s         = (Source) l.getObj();
        int      numAttrs  = s.getNumAttrs();
        String[] attrNames = new String[numAttrs];

        for (int i=0; i<numAttrs; i++)
          attrNames[i] = s.getAttribute(i).getName();

      return attrNames;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }
  }

  /**
   * Get max length for specified Attribute
   * 
   * @param id
   *          object id for the object (stream/relation/view)
   * @param attrId
   *          attribute id
   * @return maximum length for attribute
   * @throws MetadataException
   */
  public int getAttrLen(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
  
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {"id = " + id});      
    
    try
      {
        // Get the maximum length of the attribute
        int attrLen = ((Source) l.getObj()).getAttribute(attrId).getMaxLength();
      return attrLen;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }

    
  }
  
  /**
   * Get the precision value for specified Attribute
   * 
   * @param id
   *          object id for the object (stream/relation/view)
   * @param attrId
   *          attribute id
   * @return precision value for attribute
   * @throws MetadataException
   */
  public AttributeMetadata getAttrMetadata(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
  
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {"id = " + id});      
    
    try
      {
        // Get the precision of the attribute
      AttributeMetadata attrMetadata = ((Source) l.getObj()).getAttribute(attrId).getAttrMetadata();
      return attrMetadata;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }
  }
  
  
  /**
   * Get the precision value for specified Attribute
   * 
   * @param id
   *          object id for the object (stream/relation/view)
   * @param attrId
   *          attribute id
   * @return precision value for attribute
   * @throws MetadataException
   */
  public int getAttrPrecision(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
  
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {"id = " + id});      
    
    try
      {
        // Get the precision of the attribute
        int attrPrecision = ((Source) l.getObj()).getAttribute(attrId).getPrecision();
      return attrPrecision;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }
  }
  
  /**
   * Get the precision value for specified Attribute
   * 
   * @param id
   *          object id for the object (stream/relation/view)
   * @param attrId
   *          attribute id
   * @return precision value for attribute
   * @throws MetadataException
   */
  public int getAttrScale(int id, int attrId) throws MetadataException
  {
    CacheLock l = null;
  
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id, false);
    
    // Object not found
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {"id = " + id});      
    
    try
      {
        // Get the precision of the attribute
        int attrScale = ((Source) l.getObj()).getAttribute(attrId).getScale();
      return attrScale;
    }
    finally {
        // Release read lock
      if(l != null)
        release(txn, l);
      }

    
  }

  

  /**
   * Is specified id for a Stream
   * 
   * @param id
   *          Object id
   * @return true if stream else false
   * @throws MetadataException
   */
  public boolean isStream(int id) throws MetadataException
  {
    CacheLock l = null;
    
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, id,false);
   
    // Object not found. Throw exception with id, since name unknown.
    if(l == null)
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});
   
    try
      {
        // Get the maximum length of the attribute
      boolean isStream = ((Source) l.getObj()).isBStream();
      return isStream;
    }
    finally{
        // Release read lock
      if(l != null)
        release(txn, l);
      }
      
    
  }
  
  /**
   * Finds whether the table/view with this id is on demand or not
   * @param id the id of the table/view
   * @return whether the table/view is External or not
   */
  
  public boolean isExternal(String name, String schema) throws MetadataException {
    //id might be a view or table, try both.
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isExternal = false;
    
      //is it a view?
      l = findCache(txn, name, schema, NameSpace.SOURCE, false);
    	  //Object not found. Throw exception with name.
    if (l == null)  
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND,
                                  new Object[] {name});

    try 
    {
      isExternal = ((Source)l.getObj()).isExternal();
      return isExternal;
     }
     finally {
      // Release locks
       if (l != null)
         release(txn, l);
       }
     }  

  
  
  /**
   * Finds whether the table/view with this id is on demand or not
   * @param id the id of the table/view
   * @return whether the table/view is External or not
   */
  
  public boolean isExternal(int id) throws MetadataException 
  {
    //id might be a view or table, try both.
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    boolean isExternal = false;
    
      //is it a view?
      l = findCache(txn, id, false);
   
    	  //Object not found. Throw exception with id since name is unknown.
    if (l == null)    
      throw new MetadataException(MetadataError.OBJECT_NOT_FOUND, 
                                  new Object[] {"id = " + id});    
    
    try 
    {      
      isExternal = ((Source)l.getObj()).isExternal();
      return isExternal;
      }
    finally 
    {
      // Release the locks
      if (l != null)
      release(txn, l);
    }
  }  
    
 }
