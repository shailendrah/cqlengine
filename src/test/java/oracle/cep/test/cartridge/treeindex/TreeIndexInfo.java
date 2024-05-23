/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexInfo.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexInfo.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexTypeFactory;

class TreeIndexInfo implements IIndexInfo
{
  private TreeIndexFactory         treeIndexFactory;
  private TreeIndexCallbackContext callbackContext;
  private boolean                  exactResults;
  
  TreeIndexInfo(TreeIndexFactory treeIndexFactory,
                TreeIndexOperation op,
                int keyPosition,
                boolean exactResults)
  {
    this.treeIndexFactory = treeIndexFactory;
    this.callbackContext  = new TreeIndexCallbackContext(op, keyPosition);
    this.exactResults     = exactResults;
  }

  public IIndexTypeFactory getIndexTypeFactory()
  {
    return treeIndexFactory;
  }
  
  public Object getIndexCallbackContext()
  {
    return callbackContext;
  }
  
  public boolean areResultsExact()
  {
    return exactResults;
  }
  
}
