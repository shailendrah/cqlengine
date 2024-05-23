/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/IndexInfo.java /main/1 2009/10/30 15:55:04 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/14/09 - add a boolean for exact results.
    anasrini    09/10/09 - Creation
    anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/spatial/src/main/java/com/oracle/cep/cartridge/spatial/rtreeindex/IndexInfo.java /main/1 2009/10/30 15:55:04 hopark Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.spatial.rtreeindex;

import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexTypeFactory;

class IndexInfo implements IIndexInfo
{
  private IndexFactory         indexFactory;
  private OpBase                operator;
  private boolean                  exactResults;
  
  IndexInfo(IndexFactory treeIndexFactory,
                OpBase op,
                boolean exactResults)
  {
    this.indexFactory = treeIndexFactory;
    this.operator  = op;
    this.exactResults     = exactResults;
  }

  public IIndexTypeFactory getIndexTypeFactory()
  {
    return indexFactory;
  }
  
  public Object getIndexCallbackContext()
  {
    return operator;
  }

  public boolean areResultsExact()
  {
    return exactResults;
  }
  
}
