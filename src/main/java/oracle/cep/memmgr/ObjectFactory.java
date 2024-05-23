/* $Header: ObjectFactory.java 27-jun-2006.13:00:59 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      06/27/06 - freelist
    najain      06/16/06 - Creation
 */

/**
 *  @version $Header: ObjectFactory.java 27-jun-2006.13:00:59 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import oracle.cep.exceptions.CEPException;
import java.util.LinkedList;

/**
 * Base Factory class for all Object Allocations
 *
 * @since 1.0
 */

public abstract class ObjectFactory 
{
  protected LinkedList<Object> freeList;
  
  /***  

  This can be an optimization later on. If the source (store) of a secondary 
  object (synopsis) is being deleted, there is no need to delete the secondary
  object (synopsis) - it will be automatically deleted when the primary
  object (store) gets deleted.

  abstract boolean allInputsPrivate();

  ***/


  public abstract Object allocate(ObjectFactoryContext ctx)
    throws CEPException;

  public abstract void   free(ObjectFactoryContext ctx)
    throws CEPException;

  public abstract boolean isPrimary();

  public ObjectFactory()
  {
    freeList = new LinkedList<Object>();
  }
}

