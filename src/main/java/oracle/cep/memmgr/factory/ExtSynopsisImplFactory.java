/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/ExtSynopsisImplFactory.java /main/3 2009/03/12 22:23:25 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/12/09 - fix synchronization
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    parujain    11/16/07 - External synopsis
    parujain    11/16/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/ExtSynopsisImplFactory.java /main/3 2009/03/12 22:23:25 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.synopses.ExternalSynopsisImpl;
import oracle.cep.memmgr.ObjectFactory;
import oracle.cep.memmgr.ObjectFactoryContext;

public class ExtSynopsisImplFactory extends ObjectFactory
{

   @Override
   public Object allocate(ObjectFactoryContext ctx) throws CEPException 
   {
      // check if something is available in the freelist, otherwise allocate
      // a new one
      //if (freeList.isEmpty())
        return new ExternalSynopsisImpl(ctx.getExecContext());
      /*
       * 
      else
      {
        Object obj = freeList.removeFirst();
        assert obj instanceof ExternalSynopsisImpl;
	//    ((ExternalSynopsisImpl)obj).init();
	    return (ExternalSynopsisImpl)obj;
	  }
	*/	
	}

	@Override
	public void free(ObjectFactoryContext ctx) throws CEPException {
     Object obj = ctx.getObject();
     assert obj instanceof ExternalSynopsisImpl;
    // ((ExternalSynopsisImpl)obj).remove();
      //freeList.add(obj);
		
	}

	@Override
	public boolean isPrimary() {
      return false;
	}  

}
