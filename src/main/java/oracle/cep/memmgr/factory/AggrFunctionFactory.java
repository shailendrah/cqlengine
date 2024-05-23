/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/factory/AggrFunctionFactory.java /main/12 2012/02/24 11:44:51 alealves Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory to create and manage aggregation function handler instances

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/22/10 - XbranchMerge sbishnoi_bug-10136798_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/22/10 - XbranchMerge sbishnoi_bug-10132979_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/22/10 - fixng NPE which came while using pattern in EPN(bug:10136798)
    sbishnoi    09/20/10 - using getHandler in freeAggr call
    alealves    02/02/09 - new constructor with param for iAggrFactory instance
    hopark      10/10/08 - remove statics
    sbishnoi    04/02/08 - modifying class.forName to incorporate Class Loader
                           information
    hopark      02/28/08 - resurrect refcnt
    parujain    02/07/08 - parameterizing errors
    hopark      12/07/07 - cleanup spill
    hopark      09/19/07 - add memstat
    hopark      03/13/07 - moved to memmgr.factory
    anasrini    07/16/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/memmgr/factory/AggrFunctionFactory.java /main/12 2012/02/24 11:44:51 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.memmgr.factory;

import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.exceptions.UDAError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.execution.ExecException;
import oracle.cep.memmgr.AbsAllocator;
import oracle.cep.memmgr.AggrFunctionStorageElement;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.service.IUserFunctionLocator;

/**
 * Factory to create and manage aggregation function handler instances
 * <p>
 * In the GroupAggr operator, one instance of a handler is needed per
 * aggregate function for each group. This is because each handler will
 * maintain context to perform incremental aggregate computation for that
 * group.
 * <p>
 * A handler instance will need to be created when a new group is introduced
 * in the output and will need to be released when a group is removed from
 * the output.
 * <p>
 * This factory helps in efficiently handling these allocation and deallocation
 * requests.
 *
 * @since 1.0
 */

public class AggrFunctionFactory extends AbsAllocator<IAggrFunction> 
  implements IAggrFnFactory {

  /** The factory object for the specific aggregation function */
  private IAggrFnFactory iAggrFactory;

  /**
   * Constructor
   * @param fm TODO
   * @param iAggrFactoryClassName fully qualified java class name of
   *                              the factory for the aggregation function
 * @param iAggrFactoryInstanceName 
   */
  public AggrFunctionFactory(FactoryManager fm, String iAggrFactoryClassName, 
          String iAggrFactoryInstanceName) 
    throws CEPException 
  {
    super(fm);
    
    if (iAggrFactoryClassName != null) 
    {
      Class<?> cf;
      
      // Instantiate the factory object for the aggregation function
      try 
      {
        // First try with own class-loader, as it could be a built-in class.
        // If it fails, then try the Thread's CCL.
        cf = Class.forName(iAggrFactoryClassName);
      } 
      catch(ClassNotFoundException cnf) 
      {
        try 
        {
          // First try own class-loader, as it could be a built-in class.
          cf = Class.forName(iAggrFactoryClassName, true,
                 Thread.currentThread().getContextClassLoader());
        } 
        catch(ClassNotFoundException cnf2) 
        {  
          throw new MetadataException(MetadataError.FUNCTION_IMPL_CLASS_NOT_FOUND,
                                    new Object[]{iAggrFactoryClassName});
        }
      } 

      try 
      {
        iAggrFactory = (IAggrFnFactory) cf.newInstance();
      } 
      catch(Exception e) 
      {
        throw new MetadataException(MetadataError.INVALID_IMPL_CLASS_FOR_FUNCTION,
                                    e, new Object[]{iAggrFactoryClassName} );
      }
    }
    else
    {
      assert iAggrFactoryInstanceName != null;
      
      IUserFunctionLocator userFunctionLocator =
        factoryMgr.getServiceManager().getConfigMgr().getUserFunctionLocator();
      
      if (userFunctionLocator == null) 
      {
        LogUtil.warning(LoggerType.TRACE, "user function locator service " +
                            "feature not provided in the environement");
        throw new CEPException(
              InterfaceError.USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT);
      }
      else
      {
        iAggrFactory = 
          userFunctionLocator.getUserAggrFunction(iAggrFactoryInstanceName);
        
        if (iAggrFactory == null) {
          throw new
            MetadataException(MetadataError.FUNCTION_IMPL_INSTANCE_NOT_FOUND, 
                         new Object[]{iAggrFactoryInstanceName});
          
        }
      }
    }
  }
  
  public AggrFunctionFactory(FactoryManager fm, IAggrFnFactory iAggrFactory) 
  throws CEPException 
  {
    super(fm);
    this.iAggrFactory = iAggrFactory;
  }

  public AggrFunctionStorageElement allocBody() 
    throws ExecException {
    
    IAggrFunction af;
    try {
      af = iAggrFactory.newAggrFunctionHandler();
      return new AggrFunctionStorageElement(af);
    }
    catch (UDAException u) {
      throw new ExecException(ExecutionError.AGGR_FN_HANDLER_ALLOCATION_ERROR,
                              u);
    }
  }

  public IAggrFunction newAggrFunctionHandler() throws UDAException {
    try {
      return allocate();
    } 
    catch(ExecException e) {
      throw new UDAException(UDAError.HANDLER_STORAGE_ELEMENT_ALLOCATION_ERROR,
                             e);
    }
  }

  public void freeAggrFunctionHandler(IAggrFunction handler) throws UDAException
  {
    AggrFunctionStorageElement afse;

    afse = (AggrFunctionStorageElement)handler;
 
    if(afse != null)
    {
      iAggrFactory.freeAggrFunctionHandler(afse.getHandler());
      release(afse);
    }
  }
}
