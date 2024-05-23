/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/IndexInfoLocator.java /main/2 2009/12/02 02:35:15 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    11/27/09 - Data cartridge context, default package support
    anasrini    09/10/09 - Creation
    anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/IndexInfoLocator.java /main/2 2009/12/02 02:35:15 alealves Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rtreeindex;

import oracle.cep.extensibility.indexes.IIndexInfoLocator;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;

public class IndexInfoLocator implements IIndexInfoLocator
{
  public IIndexInfo[] getIndexInfo(IUserFunctionMetadata operation,
                                   int  paramPosition, 
                                   ICartridgeContext context)
  {
    if (!(operation instanceof IndexFunctionMetadata))
      return null;

    IndexFunctionMetadata opmd = (IndexFunctionMetadata) operation;
    return opmd.getOp().getIndexInfo(paramPosition, context);
  }
}
