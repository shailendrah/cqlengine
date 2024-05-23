/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/SinglyListIterFactory.java /main/3 2008/10/24 15:50:20 hopark Exp $ */

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
    hopark      09/19/07 - add memstat
    hopark      06/19/07 - cleanup
    najain      03/14/07 - cleanup
    najain      03/12/07 - bug fix
    hopark      01/15/07 - add factory id
    parujain    11/30/06 - Storage mgmt for IDoublyList
    parujain    11/30/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/SinglyListIterFactory.java /main/3 2008/10/24 15:50:20 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.memory;

import oracle.cep.dataStructures.internal.memory.SinglyList.SinglyListIter;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/SinglyListIterFactory.java /main/3 2008/10/24 15:50:20 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
public class SinglyListIterFactory<E> 
  extends AbsAllocator<SinglyListIter<E>> {
  /**
   * Constructor for SinglyListIterFactory
   * 
   */
  public SinglyListIterFactory(FactoryManager factoryMgr, int id) {
    super(factoryMgr, id);
  }

  public SinglyListIter<E> allocBody() throws ExecException
  {
    return new SinglyListIter<E>();
  }

}

