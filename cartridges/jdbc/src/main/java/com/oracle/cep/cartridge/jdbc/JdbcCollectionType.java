/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCollectionType.java /main/3 2010/06/29 09:16:03 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    06/18/10 - Allow component type to be IType
    udeshmuk    05/21/10 - use of list of lists in list of maps
    udeshmuk    01/17/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCollectionType.java /main/3 2010/06/29 09:16:03 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IIterableType;
import oracle.cep.extensibility.type.IType;

public class JdbcCollectionType extends Datatype implements IIterableType
{
  private String cartridgeContextName;
  
  private IType compType;
  
  public JdbcCollectionType(String name, IType compType, 
                            String ctxName)
  {
    super(name, java.util.Map.class);
    this.compType = compType;
    this.cartridgeContextName = ctxName;
  }
  
  public IType getComponentType()
  {
    return compType;
  }
  
  @Override
  public Iterator<Object> iterator(Object obj)
  {
    return ((List<Object>)obj).iterator();
  }

  @Override
  public Iterator<IType> iterator(Object obj, IType componentType) 
    throws ClassCastException
  {
    if(!componentType.equals(compType))
    {
      throw new ClassCastException("Cannot create iterator. Provided and " +
        "actual component types don't match for "+typeName+"@"+
        cartridgeContextName);
    }

    if(obj != null)
    {
      List<IType> records = (List<IType>)obj;    
      return records.iterator();
    }
    else //empty list iterator
    {
      return new ArrayList<IType>().iterator(); 
    }
  } 
  
}
