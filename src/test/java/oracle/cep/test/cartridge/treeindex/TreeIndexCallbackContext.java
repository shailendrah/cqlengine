/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexCallbackContext.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $ */

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
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/cartridge/treeindex/TreeIndexCallbackContext.java /main/1 2009/09/22 06:58:20 udeshmuk Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.cartridge.treeindex;

class TreeIndexCallbackContext
{
  private TreeIndexOperation op;
  private int                keyPosition;

  TreeIndexCallbackContext(TreeIndexOperation op, int keyPosition)
  {
    this.op          = op;
    this.keyPosition = keyPosition;
  }

  TreeIndexOperation getOp()
  {
    return op;
  }

  int getKeyPosition()
  {
    return keyPosition;
  }

}

