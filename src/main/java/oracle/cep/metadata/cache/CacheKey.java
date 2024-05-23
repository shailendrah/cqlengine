/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/CacheKey.java /main/3 2009/03/05 11:32:49 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/01/09 - add getNameSpace
    parujain    12/18/08 - bug fix
    parujain    09/19/08 - key for cacheobjects
    parujain    09/19/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/cache/CacheKey.java /main/3 2009/03/05 11:32:49 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CacheKey implements Externalizable {
// Can be either string or id 
  private Object objectName;
  
  // Schema name , Schema will be null in case of objectID
  private String schema;
  
  private NameSpace namespace;
  
  public CacheKey()
  {
      
  }
  
  public CacheKey(Object key, NameSpace name)
  {
    this.objectName = key;
    this.schema = null;
    this.namespace = name;
  }
  
  public CacheKey(Object key, String schema, NameSpace name)
  {
    this.objectName = key;
    this.schema = schema;
    this.namespace = name;
  }
  
  public void setObjectName(Object key)
  {
    this.objectName = key;
  }
  
  public void setSchema(String schema)
  {
    this.schema = schema;
  }
  
  
  public Object getObjectName()
  {
    return this.objectName;
  }
  
  public String getSchema()
  {
    return this.schema;
  }
  
  public NameSpace getNameSpace()
  {
    return namespace;
  }
  
  public int hashCode()
  {
    int hash = 7;
    hash = hash*31 + objectName.hashCode();
    hash = hash*31 + ((schema == null) ? 0 :schema.hashCode());
    hash = hash*31 + namespace.hashCode();
    return hash;
  }
  
  public boolean equals(Object obj)
  {
    if(this == obj)
      return true;
    if((obj == null) || (obj.getClass() != this.getClass()))
      return false;
    CacheKey c = (CacheKey)obj;
    if(!c.objectName.equals(this.objectName))
      return false;
    if(!c.namespace.equals(this.namespace))
      return false;
    return((c.schema == this.schema) || (schema != null && schema.equals(c.schema)));
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(objectName);
      out.writeObject(schema);
      out.writeObject(namespace);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      objectName = in.readObject();
      schema = (String) in.readObject();
      namespace = (NameSpace) in.readObject();
  }
}
