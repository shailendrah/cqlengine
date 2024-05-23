/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexInfoLocator.java /main/2 2009/12/02 02:35:28 alealves Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexInfoLocator.java /main/2 2009/12/02 02:35:28 alealves Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexInfoLocator;

public class TreeIndexInfoLocator implements IIndexInfoLocator
{

  private static TreeIndexFactory treeIndexFactory;
  private static TreeIndexInfo    lessThanInfo0;
  private static TreeIndexInfo    lessThanInfo1;
  private static IIndexInfo[]     lessThanInfo0Arr;
  private static IIndexInfo[]     lessThanInfo1Arr;
  private static TreeIndexInfo    greaterThanInfo0;
  private static TreeIndexInfo    greaterThanInfo1;
  private static IIndexInfo[]     greaterThanInfo0Arr;
  private static IIndexInfo[]     greaterThanInfo1Arr;
  
  static
  {
    treeIndexFactory  = new TreeIndexFactory();
    lessThanInfo0     = new TreeIndexInfo(treeIndexFactory,
                                         TreeIndexOperation.LESS, 0, true);
    lessThanInfo1     = new TreeIndexInfo(treeIndexFactory,
                                         TreeIndexOperation.LESS, 1, true);
    lessThanInfo0Arr  = new IIndexInfo[] { lessThanInfo0 };
    lessThanInfo1Arr  = new IIndexInfo[] { lessThanInfo1 };


    greaterThanInfo0  = new TreeIndexInfo(treeIndexFactory,
                                         TreeIndexOperation.GREATER, 0, false);
    greaterThanInfo1  = new TreeIndexInfo(treeIndexFactory,
                                         TreeIndexOperation.GREATER, 1, false);
    greaterThanInfo0Arr  = new IIndexInfo[] { greaterThanInfo0 };
    greaterThanInfo1Arr  = new IIndexInfo[] { greaterThanInfo1 };
  }

  public IIndexInfo[] getIndexInfo(IUserFunctionMetadata operation,
                                   int  paramPosition, ICartridgeContext context)
  {
    if (!(operation instanceof TreeIndexFunctionMetadata))
      return null;

    TreeIndexFunctionMetadata opmd = (TreeIndexFunctionMetadata) operation;
    TreeIndexOperation        op   = opmd.getOp();
    
    switch(op)
    {
      case LESS:
        if (paramPosition == 0)
          return lessThanInfo1Arr;
        else if (paramPosition == 1)
          return greaterThanInfo0Arr;
        else
          return null;
      case GREATER:
        if (paramPosition == 0)
          return greaterThanInfo1Arr;
        else if (paramPosition == 1)
          return lessThanInfo0Arr;
        else 
          return null;
      default:
        return null;
    }
  }
}
