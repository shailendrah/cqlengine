/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TDoublyListNodeFactory.java /main/2 2008/10/24 15:50:19 hopark Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TDoublyListNodeFactory.java /main/2 2008/10/24 15:50:19 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.memory;

import oracle.cep.dataStructures.internal.ITupleDoublyListNode;
import oracle.cep.dataStructures.internal.memory.TupleDoublyList.TupleDoublyListNode;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

public class TDoublyListNodeFactory extends AbsAllocator<ITupleDoublyListNode>
{
  public TDoublyListNodeFactory(FactoryManager factoryMgr, int id)
  {
    super(factoryMgr, id, NameSpace.NODE);
  }

  public Object allocBody()
  {
    return new TupleDoublyListNode();
  }
}
