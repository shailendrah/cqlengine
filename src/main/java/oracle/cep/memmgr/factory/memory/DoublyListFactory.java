/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/DoublyListFactory.java /main/5 2008/10/24 15:50:20 hopark Exp $ */

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
 hopark      12/04/07 - do not set nodeFac
 hopark      10/16/07 - use local factories for nodes
 hopark      09/19/07 - add memstat
 najain      06/29/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/memory/DoublyListFactory.java /main/5 2008/10/24 15:50:20 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr.factory.memory;

import oracle.cep.dataStructures.internal.memory.DoublyList;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.FactoryManager;

public class DoublyListFactory<E> extends AbsAllocator<E>
{
  public DoublyListFactory(FactoryManager fm, int id)
  {
    super(fm, id);
  }

  public Object allocBody()
  {
    return new DoublyList<E>();
  }
}
  
  

