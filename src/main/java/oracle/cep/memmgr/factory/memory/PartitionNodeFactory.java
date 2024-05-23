/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/PartitionNodeFactory.java /main/2 2008/10/24 15:50:19 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/10/08 - remove statics
 hopark      09/18/07 - set namespace
 hopark      08/27/07 - ListNode moved in to List
 najain      06/29/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/PartitionNodeFactory.java /main/2 2008/10/24 15:50:19 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.memory;

import oracle.cep.dataStructures.internal.memory.Partition.PartitionNode;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

public class PartitionNodeFactory extends AbsAllocator<PartitionNode>
{
  public PartitionNodeFactory(FactoryManager factoryMgr, int id)
  {
    super(factoryMgr, id, NameSpace.NODE);
  }

  public Object allocBody()
  {
    return new PartitionNode();
  }
}


