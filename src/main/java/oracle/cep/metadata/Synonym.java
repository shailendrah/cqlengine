/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Synonym.java /main/1 2010/01/06 20:33:11 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    11/23/09 - synonym metadata object
    parujain    11/23/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Synonym.java /main/1 2010/01/06 20:33:11 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.logging.IDumpable;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

public class Synonym extends CacheObject implements IDumpable, Cloneable
{
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private String synonym;

  private String target;
  
  private SynonymType type;

  @SuppressWarnings("unchecked")
  public Synonym clone() throws CloneNotSupportedException {
    Synonym syn = (Synonym)super.clone();
    return syn;
  }

  
  public Synonym(String syn, String schema)
  {
    super(syn, schema, CacheObjectType.SYNONYM);
    synonym = syn;
    target = null;
  }

  public void setSynonymType(SynonymType type)
  {
    this.type = type;  
  }
  
  public void setTarget(String trgt)
  {
    target = trgt;
  }
  
  public String getSynonym()
  {
    return synonym;
  }
  
  public String getTarget()
  {
    return target;
  }
  
  public SynonymType getSynonymType()
  {
    return type;
  }

@Override
public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
	// TODO Auto-generated method stub
	return null;
}
}
