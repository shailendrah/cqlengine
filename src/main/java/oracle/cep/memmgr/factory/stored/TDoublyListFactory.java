/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/stored/TDoublyListFactory.java /main/2 2008/10/24 15:50:20 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      01/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/stored/TDoublyListFactory.java /main/2 2008/10/24 15:50:20 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.stored;

import oracle.cep.dataStructures.internal.ITupleDoublyList;
import oracle.cep.dataStructures.internal.stored.TupleDoublyList;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

public class TDoublyListFactory 
  extends AbsAllocator<ITupleDoublyList> 
{
  public TDoublyListFactory(FactoryManager factoryMgr, int id) 
  {
    super(factoryMgr, id);
  }
  
  public Object allocBody() 
  {
    return new TupleDoublyList();
  }
}
  