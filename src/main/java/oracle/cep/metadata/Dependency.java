/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Dependency.java /main/1 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/21/09 - dependency object
    parujain    09/21/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Dependency.java /main/1 2009/11/23 21:21:22 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.logging.DumpDesc;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

/**
 * Class defining the relationship between two metadata objects
 * if they have a dependency relationship.
 * @author parujain
 *
 */
@DumpDesc(autoFields=true,
        attribTags={"Id", "Key"}, 
        attribVals={"getId", "getKey"})
public class Dependency extends CacheObject implements Cloneable
{

  private static final long serialVersionUID = 1L;
  /** Metadata level identifier of the object being referred */
  private int masterId;
  
  /** Metadata level identifier of the object which depends on the master (who refers) */
  private int dependentId;
  
  /** Type of the object being referred */
  private DependencyType masterType;
  
  /** Type of the object who is dependent on master */
  private DependencyType dependentType;
  
  // Ex: q0 - select * from S, then id of S is masterId and id of q0 is dependentId
  // q0 refers S
  
  public Dependency(String key, String schema)
  {
    super(key, schema, CacheObjectType.DEPENDENCY);
  }
  
  public void setMasterId(int mid)
  {
    this.masterId = mid;
  }
  
  public int getMasterId()
  {
    return this.masterId;  
  }
  
  public void setDependentId(int did)
  {
    this.dependentId = did;
  }
  
  public int getDependentId()
  {
    return this.dependentId;
  }
  
  public void setMasterType(DependencyType type)
  {
    this.masterType = type;
  }
  
  public DependencyType getMasterType()
  {
    return this.masterType;
  }
  
  public void setDependentType(DependencyType type)
  {
    this.dependentType = type;
  }
  
  public DependencyType getDependentType()
  {
    return this.dependentType;
  }
  
  @Override
  public MetadataDescriptor allocateDescriptor()
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
                 "allocateDescriptor() not implemented by Dependency");
  }
  
  public Dependency clone() throws CloneNotSupportedException 
  {
    Dependency dep = (Dependency)super.clone();
    return dep;
  }
  
}
