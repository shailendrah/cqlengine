/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/DependencyManager.java /main/1 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/21/09 - dependency manager
    parujain    09/21/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/DependencyManager.java /main/1 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.ChangeType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

public class DependencyManager extends CacheObjectManager
{

  private ExecContext execContext;
  
  /** key is masterId, Value is for every type list of all the dependents */
  private HashMap<Integer, HashMap<DependencyType, HashSet<Integer>>> mastersMap;
  
  /** key is dependentId, Value is for every Type list of all the masters I refer */
  private HashMap<Integer, HashMap<DependencyType, HashSet<Integer>>> dependentsMap;
  
  public static EnumMap<CacheObjectType, DependencyType>  dependencyMap;
  
  static
  {
    dependencyMap = new EnumMap<CacheObjectType, DependencyType>
                         (CacheObjectType.class);
    dependencyMap.put(CacheObjectType.TABLE, DependencyType.TABLE);
    dependencyMap.put(CacheObjectType.VIEW, DependencyType.VIEW);
    dependencyMap.put(CacheObjectType.QUERY, DependencyType.QUERY);
    dependencyMap.put(CacheObjectType.WINDOW, DependencyType.WINDOW);
    dependencyMap.put(CacheObjectType.SINGLE_FUNCTION, DependencyType.FUNCTION);
    dependencyMap.put(CacheObjectType.AGGR_FUNCTION, DependencyType.FUNCTION);
  }
  
  public static DependencyType getDependencyType(CacheObjectType type)
  {
    return dependencyMap.get(type);
  }
  
  public DependencyManager(ExecContext ec, Cache cache) {
    super(ec.getServiceManager(), cache);
    execContext = ec;
    mastersMap = new HashMap<Integer, HashMap<DependencyType, HashSet<Integer>>>();
    dependentsMap = new HashMap<Integer, HashMap<DependencyType, HashSet<Integer>>>();
  }
  
  public void init(ConfigManager cfg)
  {
  }
  
  private String getDependentKey(int mid, int did)
  {
    String key = String.valueOf(mid);
    key = key.concat(".");
    key = key.concat(String.valueOf(did));
    return key;
  }
  
  /**
   * This will be called by the respective managers when 
   * creating a new object and refers another object
   * @param masterId  
   *                Master Identifier
   * @param dependentId  
   *                  Dependent Identifier
   * @param mType 
   *             Master CacheObjectType
   * @param dType
   *             Dependent CacheObjectType
   * @param schema
   *             Schema as part of which object gets created
   * @throws MetadataException
   */
  public void addDependency(int masterId, int dependentId, 
                            DependencyType mType, DependencyType dType,
                            String schema)
  throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    String key = getDependentKey(masterId, dependentId);
    
    // This will create a Dependency Object and if already exists then 
    // will return null
    l = createObject(txn, key, schema, CacheObjectType.DEPENDENCY, null);
    
    if(l == null)
      return ;
    
    Dependency dependency = (Dependency)l.getObj();
    dependency.setMasterId(masterId);
    dependency.setMasterType(mType);
    dependency.setDependentId(dependentId);
    dependency.setDependentType(dType);
    
    //update the tables
    addDependency(dependency);
    return;
    
  }
  
  /**
   * Remove all Dependencies given a dependent id
   * @param depId
   *             DependentId
   * @param schema
   *              Schema
   * @throws MetadataException
   */
  public void removeAllDependencies(int depId, DependencyType depType,
                                    String schema)
  throws MetadataException
  {
    // Get all masters
    LinkedList<Integer> masterIds = getAllMasters(depId);
    Iterator<Integer> iter = masterIds.iterator();
    while(iter.hasNext())
    {
      int mid = iter.next().intValue();
      removeDependency(mid, depId, schema);
      updateMastersMapOnDelete(mid, depId, depType);
    }
    updateDependentsMapOnDelete(depId);
  }
  
  private void removeDependency(int masterId, int dependentId,
                              String schema)
  throws MetadataException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    String key = getDependentKey(masterId, dependentId);
    int id;
    
    l = findCache(txn, new Descriptor(key, CacheObjectType.DEPENDENCY, 
	                                  schema, null), false);
    if(l == null)
    {
      // TODO: throw the exception
      System.out.println("Dependency object not found");
    }
    
    id = l.getObj().getId();
    release(txn, l);
    
    Locks locks = null;
    locks = deleteCache(txn, id);
    
    if(locks == null)
    {
      //TODO: throw the exception
      System.out.println("Object not found for deletion");
    }
    
    l = null;
    l = locks.objLock;
  
  }
  
  /**
   * DDL executed was a failure so undo the dependency tables.
   * If Created then remove the entries else re-add the dependency
   * entries from the tables.
   * @param dependency
   */
  @Override
  public void onRollback(CacheObject depends)
  {
    Dependency dependency = (Dependency)depends;
    if(dependency.getChange() == ChangeType.CREATED)
      removeDependency(dependency);
    else if(dependency.getChange() == ChangeType.DELETED)
      addDependency(dependency);
  }
  
  private void addDependency(Dependency dependency)
  {
    HashMap<DependencyType, HashSet<Integer>> mMap = 
          mastersMap.get(dependency.getMasterId());
    if(mMap == null)
    {
      mMap = new HashMap<DependencyType, HashSet<Integer>>();
      mastersMap.put(dependency.getMasterId(), mMap);
    }
    HashMap<DependencyType, HashSet<Integer>> dMap = 
          dependentsMap.get(dependency.getDependentId());
   
    if(dMap == null)
    {
      dMap = new HashMap<DependencyType, HashSet<Integer>>();
      dependentsMap.put(dependency.getDependentId(), dMap);
    }

    HashSet<Integer> dependentsList = mMap.get(dependency.getDependentType());
    if(dependentsList == null)
    {
      dependentsList = new HashSet<Integer>();
      mMap.put(dependency.getDependentType(), dependentsList);
    }
    if(!dependentsList.contains(dependency.getDependentId()))
      dependentsList.add(dependency.getDependentId());

    HashSet<Integer> mastersList = dMap.get(dependency.getMasterType());
    if(mastersList == null)
    {
      mastersList = new HashSet<Integer>();
      dMap.put(dependency.getMasterType(), mastersList);
    }
    if(!mastersList.contains(dependency.getMasterId()))
      mastersList.add(dependency.getMasterId());
  }
  
  private void updateMastersMapOnDelete(int mastersId, int depId, 
                                        DependencyType depType)
  {
    HashMap<DependencyType, HashSet<Integer>> mMap =
            mastersMap.get(mastersId);
    if(mMap != null)
	{
	  HashSet<Integer> dependentsSet = mMap.get(depType);
	  if(dependentsSet != null)
	  {
        dependentsSet.remove(depId);
       
	    // If list is empty then no dependent of that type
	    if(dependentsSet.isEmpty())
	      mMap.remove(depType);
	  }
	  // If map is now empty then no one currently depends on the master
	  if(mMap.isEmpty())
	  {
	    mastersMap.remove(mastersId);
	  }
	}
  }
  
  private void updateDependentsMapOnDelete(int depId)
  {
    HashMap<DependencyType, HashSet<Integer>> dMap =
            dependentsMap.get(depId);
    if(dMap != null)
    {
      dependentsMap.remove(depId);
    }
  }
 
  // called only on rollback where we deal with single cacheobject at a time
  // for drop DDLs we will remove all dependencies together so will not call
  // this method 
  private void removeDependency(Dependency dependency)
  {
    HashMap<DependencyType, HashSet<Integer>> mMap = 
      mastersMap.get(dependency.getMasterId());
	
    HashMap<DependencyType, HashSet<Integer>> dMap = 
      dependentsMap.get(dependency.getDependentId());

    if(mMap != null)
    {
      HashSet<Integer> dependentsSet = mMap.get(dependency.getDependentType());
      if(dependentsSet != null)
      {
        dependentsSet.remove(dependency.getDependentId());
       
        // If list is empty then no dependent of that type
        if(dependentsSet.isEmpty())
          mMap.remove(dependency.getDependentType());
      }
     // If map is now empty then no one currently depends on the master
      if(mMap.isEmpty())
      {
        mastersMap.remove(dependency.getMasterId());
      }
    }
	
    if(dMap != null)
    {
      HashSet<Integer> mastersSet = dMap.get(dependency.getMasterType());
      if(mastersSet != null)
      {
        mastersSet.remove(dependency.getMasterId());
        if(mastersSet.isEmpty())
        {
          dMap.remove(dependency.getMasterType());
        }
      }
      if(dMap.isEmpty())
        dependentsMap.remove(dependency.getDependentId());
    }

  }
  
  
  /**
   * Given the masterId get all the dependents of a particular type
   * 
   * @param masterId
   *                Metadata level Id of the Master Object
   * @param dType
   *            DependencyType of the dependent
   * @return  List of identifiers of all the dependents
   */
  public Integer[] getDependents(Integer masterId, DependencyType dType)
  {
    HashMap<DependencyType, HashSet<Integer>> mMap = mastersMap.get(masterId);
    if(mMap != null)
    {
      HashSet<Integer> dependents = mMap.get(dType);
      if(dependents != null)
        return (Integer[])(dependents.toArray(new Integer[0]));
    }
    
    return null;
  }
  
  /**
   * Is any dependent of particular type present
   * 
   * @param masterId
   *                Metadata level id of master Object
   * @param dType
   *            DependencyType of the dependent
   * @return  true if anyone depends
   */
  public boolean isAnyDependentPresent(Integer masterId, DependencyType dType)
  {
    HashMap<DependencyType, HashSet<Integer>> mMap = mastersMap.get(masterId);
    if(mMap != null)
    {
      HashSet<Integer> dependents = mMap.get(dType);
      if((dependents != null) && (dependents.size() > 0))
        return true;
    }
    return false;
  }
  
  /**
   * Given the dependentId, get all the MasterIds of a particular type
   * 
   * @param depId
   *             DependentId
   * @param mType
   *            DependencyType of the master
   * @return List of Master's identifiers
   */
  public Integer[] getMasters(Integer depId, DependencyType mType)
  {
    HashMap<DependencyType, HashSet<Integer>> dMap = dependentsMap.get(depId);
    if(dMap != null) 
    {
      HashSet<Integer> masters = dMap.get(mType);
      if(masters != null)
        return (Integer[])(masters.toArray(new Integer[0]));
    }
    
    return null;
  }
  
  /**
   *  Given the dependentId, is there any master present
   *  OR Is dependent depends on any master? 
   *  
   * @param depId
   *             DependentId
   * @param mType
   *            DependentType of the master
   * @return  true if depends on any master of particular type
   */
  public boolean isAnyMasterPresent(Integer depId, DependencyType mType)
  {
    HashMap<DependencyType, HashSet<Integer>> dMap = dependentsMap.get(depId);
    if(dMap != null)
    {
      HashSet<Integer> masters = dMap.get(mType);
      if((masters != null) && (masters.size() > 0))
        return true;
    }
    return false;
  }
 
  /**
   * Get all the dependents of a Master
   * 
   * @param masterId 
   *                MasterId
   * @return  List of dependent Ids
   */
  public LinkedList<Integer> getAllDependents(Integer masterId)
  {
    LinkedList<Integer>  list = new LinkedList<Integer>();
    HashMap<DependencyType, HashSet<Integer>> mMap = mastersMap.get(masterId);
    if(mMap != null)
    {
      for(DependencyType type: DependencyType.values())
      {
        HashSet<Integer> l = mMap.get(type);
        if((l != null) && (!l.isEmpty()))
          list.addAll(l);
      }
    }
    return list;
  }
  
  /**
   * Get all the Ids of referred objects by a dependent
   * 
   * @param depId
   *             DependentId
   * @return List of ids of referred objects
   */
  public LinkedList<Integer> getAllMasters(Integer depId)
  {
    LinkedList<Integer> list = new LinkedList<Integer>();
    HashMap<DependencyType, HashSet<Integer>> dMap = dependentsMap.get(depId);
    if(dMap != null)
    {
      for(DependencyType type: DependencyType.values())
      {
        HashSet<Integer> l = dMap.get(type);
        if(l != null)
          list.addAll(l);
      }
    }
    return list;
  }
  
  
  /**
   * Informs whether any dependent is present or not.
   * 
   * @param masterId
   *                MasterId
   * @return
   *        Whether dependents are present or not
   */
  public boolean areDependentsPresent(Integer masterId)
  {
    boolean present = false;
    HashMap<DependencyType, HashSet<Integer>> mMap = mastersMap.get(masterId);
    if(mMap != null)
    {
      for(DependencyType type: DependencyType.values())
      {
        HashSet<Integer> l = mMap.get(type);
        if(l != null)
        {
          if(!l.isEmpty()) 	
          {
            present = true;
            break;
          }
        }
      }
    }
    return present;
  }
}
