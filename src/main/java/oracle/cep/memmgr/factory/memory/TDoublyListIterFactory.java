/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TDoublyListIterFactory.java /main/2 2008/10/24 15:50:20 hopark Exp $ */

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
    hopark      10/23/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TDoublyListIterFactory.java /main/2 2008/10/24 15:50:20 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.memory;

import oracle.cep.dataStructures.internal.ITupleDoublyListIter;
import oracle.cep.dataStructures.internal.memory.TupleDoublyList.TupleDoublyListIter;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/TDoublyListIterFactory.java /main/2 2008/10/24 15:50:20 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
public class TDoublyListIterFactory 
  extends AbsAllocator<ITupleDoublyListIter> {
  /**
   * Constructor for DoublyListIterFactory
   * 
   */
  public TDoublyListIterFactory(FactoryManager factoryMgr, int id) {
    super(factoryMgr, id);
  }

  public Object allocBody() throws ExecException
  {
    return new TupleDoublyListIter();
  }

}

