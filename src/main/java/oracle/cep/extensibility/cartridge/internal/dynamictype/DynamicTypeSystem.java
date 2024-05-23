/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeSystem.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/04/12 - Creation
 */

package oracle.cep.extensibility.cartridge.internal.dynamictype;

import oracle.cep.extensibility.type.IType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicTypeSystem.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public interface DynamicTypeSystem
{

  public IType createType();
  
  public IType dropType(String typeName);
}
