/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFactory.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    09/10/09 - Creation
    anasrini    09/10/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexFactory.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

import oracle.cep.extensibility.indexes.IIndexTypeFactory;
import oracle.cep.extensibility.indexes.IIndex;

class TreeIndexFactory implements IIndexTypeFactory
{
  public IIndex create(Object[] args)
  {
    return new TreeIndex();
  }

  public void drop(IIndex index)
  {
  }
}
