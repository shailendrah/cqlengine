/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/SystemObject.java /main/7 2009/01/16 22:55:00 parujain Exp $ */

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
    parujain    01/14/09 - metadata in-mem
    parujain    09/12/08 - multiple schema support
    skmishra    08/21/08 - imports
    hopark      01/18/08 - support dump
    mthatte     08/22/07 - 
    hopark      03/21/07 - move the store integration part to CacheObject
    parujain    02/08/07 - system state object
    parujain    02/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/SystemObject.java /main/7 2009/01/16 22:55:00 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.logging.DumpDesc;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

@DumpDesc(autoFields=true,
          attribTags={"Id", "Key"}, 
          attribVals={"getId", "getKey"})
public class SystemObject extends CacheObject implements Cloneable{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private SystemState state;
  
  SystemObject(String name, String schema)
  {
    super(name, schema, CacheObjectType.SYSTEM_STATE);
    state = SystemState.ZERO;
  }
  
  public SystemObject clone() throws CloneNotSupportedException {
    SystemObject obj = (SystemObject)super.clone();
    return obj;
  }


  public SystemState getState()
  {
    return state;
  }
  
  public void setState(SystemState state)
  {
    this.state = state;
  }

public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
	throw new UnsupportedOperationException("Not supported by: " + this.getClass());
}
}
