/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SimpleFunction.java /main/17 2012/05/09 06:42:13 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Metadata object for single element functions

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    04/30/12 - add BISqlequivalent
 anasrini    09/09/11 - XbranchMerge anasrini_bug-12943064_ps5 from
                        st_pcbpel_11.1.1.4.0
 udeshmuk    06/22/11 - support getSQLEquivalent
 parujain    10/02/09 - dependency
 alealves    02/02/09 - read from back-end only if class-name is provided
 parujain    01/14/09 - metadata in-mem
 hopark      10/10/08 - remove statics
 parujain    09/12/08 - multiple schema support
 skmishra    08/21/08 - imports
 sbishnoi    04/02/08 - modifying class.forName to incorporate ClassLoader
 mthatte     08/22/07 - 
 hopark      03/21/07 - move the store integration code to CacheObject
 parujain    02/02/07 - BDB integration
 parujain    01/11/07 - BDB integration
 parujain    01/09/07 - bdb integration
 parujain    10/20/06 - MDS integration
 anasrini    07/06/06 - Creation
 anasrini    07/06/06 - Creation
 anasrini    07/06/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/SimpleFunction.java /main/17 2012/05/09 06:42:13 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.metadata;

import java.sql.DatabaseMetaData;
import java.util.logging.Level;
import oracle.cep.common.SQLType;
import oracle.cep.common.Datatype;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.ProcedureMetadataDescriptor;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.service.IUserFunctionLocator;

/**
 * Metadata object for single element functions
 * 
 * @author anasrini
 * @since 1.0
 */

public class SimpleFunction extends UserFunction implements
    ISimpleFunctionMetadata, Cloneable
{

  /**
   * 
   */
  private static final long               serialVersionUID = 1L;
  /** the instance of the implementation class */
  private transient SingleElementFunction implClass;
  
  /** name of the sql function which is equivalent to this function. */
  private String sqlEquivalent = null;

  /** name of the bi sql function which is equivalent to this CQL funciton */
  private String biSqlEquivalent = null;

  /**
   * Constructor for SimpleFunction
   * 
   * @param name
   *          name of the single element function
   */
  SimpleFunction(String name, String schema)
  {
    super(name, schema, CacheObjectType.SINGLE_FUNCTION);
    sqlEquivalent = null;
    biSqlEquivalent = null;
  }

  public SimpleFunction clone() throws CloneNotSupportedException
  {
    SimpleFunction fn = (SimpleFunction) super.clone();
    return fn;
  }

  /**
   * Set the instance of the implementation class
   * 
   * @param implClass
   *          the implementation class instance
   */
  public void setImplClass(SingleElementFunction implClass)
  {
    this.implClass = implClass;
  }

  /**
   * Get the implementation class instance
   * 
   * @return the implementation class instance
   */
  public SingleElementFunction getImplClass()
  {
    return implClass;
  }

  /**
   * getter for sqlEquivalent
   * @return Returns sql equivalent if any for this function null otherwise
   */
  public String getSQLEquivalent(SQLType sqlMode)
  {
    if(sqlMode == SQLType.BI)
      return biSqlEquivalent;
    else
      return sqlEquivalent;
  }
  
  /**
   * setter for sqlEquivalent
   * @param sqlEquivalent name of the sql equivalent of this function
   */
  public void setSQLEquivalent(String sqlEquivalent)
  {
    this.sqlEquivalent = sqlEquivalent;
  }

  /**
   * setter for biSqlEquivalent
   * @param biSqlEquivalent name of the sql equivalent of this function
   */
  public void setBISQLEquivalent(String biSqlEquivalent)
  {
    this.biSqlEquivalent = biSqlEquivalent;
  }

  /**
   * Additional processing to be done to the read object Object is read from
   * backend
   */
  public void processReadObject(FactoryManager factoryMgr)
  {
    if (!isImplNative())
    {
      if(getImplClassName() != null)
      {
        SingleElementFunction f = null;
        try {
          Class<?> cf = null;
          
          try {
            // First try with own class-loader, as it could be a built-in class.
            // If it fails, then try the Thread's CCL.
            cf = Class.forName(getImplClassName());
          } catch (ClassNotFoundException cnf)
          {
            try {
              cf = Class.forName(getImplClassName(), true, Thread.currentThread()
                  .getContextClassLoader());
            } catch (ClassNotFoundException cnf2) {
              LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, cnf2);
            }
          }
          
          if (cf != null)
            f = (SingleElementFunction) cf.newInstance();
        } catch (Exception e) {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        }

        setImplClass(f);
      } else if (getImplInstanceName() != null) {
        // This is not expected to fail, as we already have retrieved the
        // implementation class
        // when first creating this object.
        // FIXME if it does fail, then it will cause a NPE when evaluating the
        // expression.
        // In the future, we should try to handle this exceptional case sooner.
        IUserFunctionLocator userFunctionLocator = factoryMgr
            .getServiceManager().getConfigMgr().getUserFunctionLocator();

        if (userFunctionLocator != null) {
          SingleElementFunction function = userFunctionLocator
              .getUserFunction(getImplInstanceName());

          if (function != null) {
            setImplClass(function);
          } else {
            LogUtil.warning(LoggerType.TRACE,
                "fail to locate user function instance for '"
                    + getImplInstanceName() + "'");
          }
        } else {
          LogUtil.warning(LoggerType.TRACE,
              "fail to retrieve user function locator in this environment");
        }
      }
    }
  }

  public MetadataDescriptor allocateDescriptor()
      throws UnsupportedOperationException
  {
    Datatype dt = this.getReturnType();
    int returnType = DatabaseMetaData.procedureResultUnknown;
    if (dt.equals(Datatype.VOID))
      returnType = DatabaseMetaData.procedureNoResult;
    else
      returnType = DatabaseMetaData.procedureReturnsResult;

    return new ProcedureMetadataDescriptor(this.getName(), returnType);
  }
}
