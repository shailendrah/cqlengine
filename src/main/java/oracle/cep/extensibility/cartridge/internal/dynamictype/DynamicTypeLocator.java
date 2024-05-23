/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeLocator.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/01/12 - Creation
 */
package oracle.cep.extensibility.cartridge.internal.dynamictype;

import java.util.HashMap;
import java.util.Map;

import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeLocator.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DynamicTypeLocator implements ITypeLocator, DynamicTypeSystem
{
  /** A Map from type name to IType object */
  private Map<String, IType> typeMap;
  
  /** current type counter*/
  public static long instanceCounter;
  
  /**
   * Construct Dynamic Type Locator
   */
  public DynamicTypeLocator()
  {
    typeMap = new HashMap<String,IType>();
    instanceCounter = 1l;
  }
  
  @Override
  public IType getType(String extensibleTypeName, ICartridgeContext context)
      throws MetadataNotFoundException, AmbiguousMetadataException
  {
    // Find the type with given extensible type name
    IType dynamicType = typeMap.get(extensibleTypeName);
   
    return dynamicType;
  }

  @Override
  public IArrayType getArrayType(String componentExtensibleTypeName,
      ICartridgeContext context) throws MetadataNotFoundException,
      AmbiguousMetadataException
  {
    return null;
  }

  @Override
  public synchronized IType createType()
  {
    String extensibleTypeName = "Type" + instanceCounter;    
    IType dynType = new DynamicDataType(extensibleTypeName);
    LogUtil.fine(LoggerType.TRACE, "A new internal dynamic type " + 
                                      extensibleTypeName + 
                                      "@DynamicType is added.");
    typeMap.put(extensibleTypeName, dynType);
    instanceCounter++;
    return dynType;
  }

  @Override
  public synchronized IType dropType(String typeName)
  {
    if(typeMap.containsKey(typeName))
    {
      IType removed = typeMap.remove(typeName);
      LogUtil.info(LoggerType.CUSTOMER, "An Existing Dynamic Type " + 
                   typeName + "@DynamicType is removed.");
      return removed;
    }
    LogUtil.info(LoggerType.CUSTOMER, "Deletion failed for a dynamic type " + 
        typeName + "@DynamicType. Given type doesn't exist");
    return null;
  }
  
}
