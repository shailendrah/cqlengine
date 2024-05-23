/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/AggFunction.java /main/14 2009/11/23 21:21:22 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Metadata object for aggregate functions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    10/02/09 - dependency
    alealves    02/02/09 - read only from backend if class-name is provided
    parujain    01/14/09 - metadata in-mem
    hopark      10/10/08 - remove statics
    parujain    09/12/08 - multiple schema support
    skmishra    08/21/08 - imports
    mthatte     08/22/07 - 
    hopark      03/21/07 - move the store integration code to CacheObject
    parujain    02/02/07 - BDB integration
    parujain    01/11/07 - BDB integration
    parujain    01/09/07 - bdb integration
    parujain    10/20/06 - MDS integration
    anasrini    07/17/06 - add getAggrFactory, setAggrFactory 
    anasrini    07/06/06 - Creation
    anasrini    07/06/06 - Creation
    anasrini    07/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/AggFunction.java /main/14 2009/11/23 21:21:22 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.metadata;

import java.sql.DatabaseMetaData;
import java.util.logging.Level;

import oracle.cep.common.Datatype;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.ProcedureMetadataDescriptor;
import oracle.cep.exceptions.CEPException;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunctionMetadata;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.factory.AggrFunctionFactory;
import oracle.cep.metadata.cache.CacheObjectType;

/**
 * Metadata object for aggregate functions
 *
 * @author anasrini
 * @since 1.0
 */

public class AggFunction extends UserFunction 
  implements IAggrFunctionMetadata, Cloneable {
  
 /**
   *  
   */
  private static final long serialVersionUID = 1L;

/** Does it support incremental computation */
  protected boolean supportsIncremental;

  /** The handler factory for this aggregation function */
  protected transient IAggrFnFactory factory;

  /**
   * Constructor for AggFunction
   * 
   * @param name name of the aggregate function
   */
  protected AggFunction(String name, String schema) {
    super(name, schema, CacheObjectType.AGGR_FUNCTION);
  }
 
  public AggFunction clone() throws CloneNotSupportedException {
    AggFunction fn = (AggFunction)super.clone();
    return fn;
  }
  

  // Setter methods

  /**returnType
   * Set whether this function supports an incremental method of computation
   * @param supportsIncremental true if this aggregation function supports
   *                            incremental computation else false
   */
  public void setSupportsIncremental(boolean supportsIncremental) {
    this.supportsIncremental = supportsIncremental;
  }

  /**
   * Set the aggregation function handler factory
   * @param factory the aggregation function handler factory
   */
  public void setAggrFactory(IAggrFnFactory factory) {
    this.factory = factory;
  }

  // Getter methods

  /**
   * Does this aggregation function support incremental computation
   * @return true if this aggregation function supports incremental
   *         computation else false
   */
  public boolean supportsIncremental() {
    return supportsIncremental;
  }

  /**
   * Get the aggregation function handler factory
   * @return the aggregation function handler factory
   */
  public IAggrFnFactory getAggrFactory() {
    return factory;
  }
  
  /**
   * Additional processing to be done to the read object
   * Object is read from backend
   */
  public void processReadObject(FactoryManager factoryMgr)
  {
    // Only try to instantiate factory if class name is provided.
    if (getImplClassName() != null) {
      // FIXME seems like we always need to use this...
      IAggrFnFactory       iAggrFactory = null;
      try {
        iAggrFactory = new AggrFunctionFactory(factoryMgr, getImplClassName(), getImplInstanceName());
      } catch (CEPException e) {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
      }
      setAggrFactory(iAggrFactory);
    }
  }

  public MetadataDescriptor allocateDescriptor()
		throws UnsupportedOperationException {
	Datatype dt = this.getReturnType();
	int returnType = DatabaseMetaData.procedureResultUnknown;
	if(dt.equals(Datatype.VOID))
		returnType=DatabaseMetaData.procedureNoResult;
	else
		returnType=DatabaseMetaData.procedureReturnsResult;
	
	return new ProcedureMetadataDescriptor(this.getName(),returnType);
  }

}
