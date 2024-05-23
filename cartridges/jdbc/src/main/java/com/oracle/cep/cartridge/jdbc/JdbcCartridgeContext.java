/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeContext.java /main/18 2015/11/04 04:57:20 udeshmuk Exp $ */

/* Copyright (c) 2010, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/02/15 - add renewconnection bug 21678893
    sbishnoi    02/27/14 - bug 18318316
    udeshmuk    10/28/10 - XbranchMerge udeshmuk_fix_timestamp_issue from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    10/28/10 - handle java.sql.timestamp similar to
                           java.math.BigDecimal
    sbishnoi    09/27/10 - XbranchMerge sbishnoi_bug-10145105_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/26/10 - adding BIGDECIMAL as compatible with OBJECT
    sbishnoi    08/22/10 - XbranchMerge sbishnoi_bug-10044740_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    08/10/10 - XbranchMerge udeshmuk_jdbccartridge_errortranslation
                           from main
    udeshmuk    08/06/10 - translation of error msg
    udeshmuk    08/05/10 - XbranchMerge udeshmuk_bug-9946995_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    08/03/10 - implement bundle listener
    udeshmuk    07/26/10 - XbranchMerge udeshmuk_bug-9916489_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    07/23/10 - XbranchMerge udeshmuk_bug-9916298_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    07/22/10 - replace ConfigurationException calls by
                           JdbcCartridggeException calls
    udeshmuk    07/19/10 - change isCompatible to allow timestamp as java.sql.Timestamp
    udeshmuk    07/08/10 - XbranchMerge udeshmuk_bug-9885070_ps3 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    07/08/10 - fix NPE in 9885070
    udeshmuk    06/16/10 - throw error for wrong param in SQL query
    udeshmuk    06/15/10 - do not use eventtyperepo, support CQL types
    udeshmuk    06/11/10 - support cql types for function parameter
    udeshmuk    05/10/10 - allow return type in the function
    udeshmuk    04/27/10 - move config from epn to app config
    udeshmuk    03/03/10 - replace the datasource accessing code with newer
                           code
    udeshmuk    01/19/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/JdbcCartridgeContext.java /main/18 2015/11/04 04:57:20 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.MessageFormat;

import javax.sql.DataSource;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.extensibility.type.IType.Kind;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ICartridgeRegistry;

import org.osgi.framework.BundleContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.context.BundleContextAware;

import com.bea.wlevs.ede.api.ConfigurationException;
import com.bea.wlevs.configuration.Activate;
import com.bea.wlevs.configuration.Prepare;
import com.bea.wlevs.configuration.Rollback;
import com.bea.wlevs.util.Service;
import com.bea.wlevs.util.ErrorMessage;
import com.bea.wlevs.util.ExceptionUtils;

import weblogic.i18n.Localizer;
import weblogic.i18n.logging.Loggable; 
import weblogic.i18ntools.L10nLookup;

public class JdbcCartridgeContext implements DisposableBean,
  BundleContextAware 
{
  //Constants used while creating the property map
  public static final String CONTEXT_NAME = "CONTEXT_NAME";
  public static final String DATA_SOURCE_NAME = "DS_NAME";
  public static final String DATA_SOURCE_OBJ = "DS_OBJ";
  public static final String FUNC_MAP = "FUNC_MAP";
  public static final String TYPE_MAP = "TYPE_MAP";
  
  //FIXME: SQL non-quoted identifiers can have A-Z, a-z, 0-9, _, $, # as per doc.
  //But for function params which will eventually get replaced by ? , I don't
  //see much value in allowing $ and # too. So excluded currently.
  private static final String JDBC_VALID_PARAM_PATTERN = ":[A-Za-z0-9_]+";
    
  private static Pattern validParam = Pattern.compile(JDBC_VALID_PARAM_PATTERN);
  
  static final String LOCALIZER_CLASS =
    "com.oracle.cep.cartridge.jdbc.JdbcCartridgeLogLocalizerDetail";
 
  static final ClassLoader CLASS_LOADER = 
    JdbcCartridgeContext.class.getClassLoader();
    
  //ensures context is registered only once
  private boolean isCtxRegistered;
    
  private BundleContext bundleContext;
  
  private com.bea.wlevs.configuration.internal.Configuration m_configuration;
 
  private ICartridgeRegistry m_registry;
  
  private String contextName;
  
  private String dataSourceName;

  private String symbolicName;
  
  private DataSource dataSource;
  
  private Connection connection;
  
  private Map<String, IUserFunctionMetadata> funcMap;
  
  private Map<String, IType> typesMap;
  
  private CartridgeContextImpl cartridgeCtx;

  public JdbcCartridgeContext()
  {
    isCtxRegistered = false;
    bundleContext   = null;
    m_configuration = null;
    contextName     = null;
    dataSourceName  = null;
    symbolicName    = null;
    dataSource      = null;
    connection      = null;
    funcMap         = new HashMap<String, IUserFunctionMetadata>();
    typesMap        = new HashMap<String, IType>();
    cartridgeCtx    = null;
  }
   
  //set by the BeanFactory
  public void setContextName(String contextName)
  {
    this.contextName = contextName;
  }

  public String getContextName()
  {
    return this.contextName;
  }

  /**
   * This method uses @Service annotation to get the configuration service.
   * This annotation ensures that Spring sets the variable m_configuration
   * at the time of creating bean with appropriate value.
   * @param configuration - server configuration
   */
  @Service
  public void setConfiguration(com.bea.wlevs.configuration.internal.Configuration configuration)
  {
    m_configuration = configuration;
  }
  
  /**
   * This is a method of BundleContextAware interface. 
   * This is also called by Spring on its own to set the bundleContext reference
   * @param bundleContext The bundle context object
   */
  public void setBundleContext(BundleContext bundleContext)
  {
    this.bundleContext = bundleContext;
  }
  
  @Prepare
  public void checkCartridgeContextConfig(JdbcCartridgeContextConfig ctxConfig)
    throws Exception
  {
    //Process only if the ctx is not registered already
    //Only first time it will execute. Further changes to config won't have 
    //any effect. No dynamic change of configuration support as of now.
    if((ctxConfig != null) && (!isCtxRegistered))
    {
      //check for data-source string sanity and set dataSourceName
      if((!ctxConfig.isSetDataSource()) || 
         ((dataSourceName=ctxConfig.getDataSource()).equals(""))) 
      {
        Loggable l = JdbcCartridgeLogger.nullDataSourceValueLoggable();
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                  CLASS_LOADER
                                                          );
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
	throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }
     
      //now check functions
      List<JdbcCartridgeFunctionConfig> funcList = ctxConfig.getFunction();
      for(JdbcCartridgeFunctionConfig fConfig : funcList)
      {
        processFunctionConfig(fConfig);
      }
    }
  }
  
  private CartridgeContextImpl getCartridgeContext()
  {
    if(cartridgeCtx == null)
    {
      cartridgeCtx = new CartridgeContextImpl(getSymbolicName());
    }
    return cartridgeCtx;
  }
  
  private IFieldMetadata findMatchingFieldInType(String aliasName, 
  		                                           IComplexType compType)
    throws MetadataNotFoundException, AmbiguousMetadataException
  {
  	List<IFieldMetadata> matchedMeta = new ArrayList<IFieldMetadata>();
  	
  	for(IFieldMetadata fMeta : compType.getFields())
  	{
  		if(fMeta.getName().equalsIgnoreCase(aliasName))
  			matchedMeta.add(fMeta);
  	}
  	
  	if(matchedMeta.size() == 0)
  	  throw new MetadataNotFoundException(contextName, aliasName +
  	  		                                " not found in type " +
  	  		                                compType.name());
  	else if(matchedMeta.size() == 1)
  		return matchedMeta.get(0);
  	else //more than one matches
  	{ //try for an exact match
  		int idx = -1;
  		for(int i=0; i < matchedMeta.size(); i++)
  		{
  			IFieldMetadata fMeta = matchedMeta.get(i);
  		  if(fMeta.getName().equals(aliasName))
  			{ //exact match
  				if(idx == -1)
  				  idx = i;
  				else // multiple exact matches
  					throw new AmbiguousMetadataException(contextName, "Multiple "+
  					                                     "fields match "+ aliasName +
  					                                     " in type "+compType.name());
  			}
  		}
  		
  		if(idx == -1)
  			throw new MetadataNotFoundException(contextName, aliasName +
  			                                    " not found in type " +
  			                                    compType.name());
  		else
  			return matchedMeta.get(idx);
  	}
  }
 
  private void processFunctionConfig(JdbcCartridgeFunctionConfig fConfig) 
    throws SQLException 
  {
    String funcName = fConfig.getName().trim();
   
    List<JdbcCartridgeFuncParamConfig> fnParams = fConfig.getParam();
    
    //Mapping of parameter name to it's position in the fn arg list
    Map<String, Integer> paramNameToFnArgListPosMap = 
      new HashMap<String, Integer>();
    
    //Info abt the parameters of the function
    List<JdbcCartridgeFuncParamMetadata> paramInfos = 
      new ArrayList<JdbcCartridgeFuncParamMetadata>();
    
    //mapKey will be the function signature
    String mapKey = funcName;
    for(int i=0; i < fnParams.size(); i++)
    {
      JdbcCartridgeFuncParamConfig pConfig = 
        (JdbcCartridgeFuncParamConfig) fnParams.get(i);
      mapKey = mapKey + processParamConfig(funcName, pConfig, paramInfos,
                                           paramNameToFnArgListPosMap, i);
    }    

    /*
     * We currently support ONLY java extensible types.
     * Since we cannot get hold of ExecContext we could not call
     * CartridgeHelper's getType() API.
     */
    //Get the java type system.
    m_registry = JdbcCartridge.getCartridgeRegistry();
    ITypeLocator javaTypeLocator = m_registry.getJavaTypeSystem();
    assert javaTypeLocator != null : "could not access java type system";
    
    //Create a collection type as the return type of the function and
    //find the component type from return-component-type field.
    
    String returnCompTypeName = fConfig.getReturnComponentType().trim();
      
    //Verify the type is allowed native type and if it
    //contains @ then the link name is java
    String[] names = returnCompTypeName.split("@");
    if(((names.length == 2) && (names[1].equalsIgnoreCase("java"))
       ||(names.length == 1))
      &&(!names[0].equalsIgnoreCase("interval"))
      &&(!names[0].equalsIgnoreCase("intervalym"))
      &&(!names[0].equalsIgnoreCase("void"))
      &&(!names[0].equalsIgnoreCase("unknown"))
      &&(!names[0].equalsIgnoreCase("xmltype"))
      &&(!names[0].equalsIgnoreCase("object"))
      )
    {
      returnCompTypeName = names[0];
    }
    else
    {
      Loggable l = 
        JdbcCartridgeLogger.invalidReturnComponentTypeLoggable(returnCompTypeName,
	                                                       funcName);
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER,
							returnCompTypeName,
							funcName
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    }
    //Return type can either be a native CQL type or some java type
    //Check if it is a native type first
    IType returnCompType = 
      Datatype.getTypeFromCqlName(returnCompTypeName.toLowerCase());
    if(returnCompType == null)
    { //lookup in java type system - no need to change to lower/upper case here
      try
      {
        returnCompType = javaTypeLocator.getType(returnCompTypeName, 
                                                 getCartridgeContext());
      }
      catch(MetadataNotFoundException me)
      {
        Loggable l =
	  JdbcCartridgeLogger.invalidReturnComponentTypeLoggable(returnCompTypeName, 
	                                                         funcName);
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                  CLASS_LOADER,
							  returnCompTypeName,
							  funcName
                                                          );
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
	throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }
      catch(AmbiguousMetadataException ae)
      {
        Loggable l = 
	  JdbcCartridgeLogger.ambiguousReturnComponentTypeLoggable(returnCompTypeName, 
	                                                           funcName);
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                  CLASS_LOADER,
							  returnCompTypeName,
							  funcName
                                                          );
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
	throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }
    }
    
    assert returnCompType != null;     
        
    String collTypeName = contextName+mapKey+"coll";
    
    JdbcCollectionType retType = 
      new JdbcCollectionType(collTypeName, returnCompType, contextName);
    
    typesMap.put(returnCompType.name(), returnCompType);
    typesMap.put(collTypeName, retType);
    
    //Process SQL
    String sql = fConfig.getSql();
    LogUtil.info(LoggerType.TRACE, "Provided SQL: "+ sql);
    //Contains the mapping of prepStmt params with the function params.
    //Entry at index 'i' in this list, points to the metadata of that function
    //parameter which is the ith parameter for the prepStmt.
    List<Integer> prepStmtParamToFnParamMapping = new ArrayList<Integer>();
    
    String replacedSql = processSQL(funcName, sql, 
                                    prepStmtParamToFnParamMapping,
                                    paramNameToFnArgListPosMap);
    LogUtil.info(LoggerType.TRACE, "Processed SQL: "+replacedSql);
    
    if(funcMap.containsKey(mapKey))
    {
      Loggable l = 
        JdbcCartridgeLogger.duplicateFunctionSignatureLoggable(funcName);
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER,
							funcName
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    }
    
    Map<String, IFieldMetadata> fieldMapping = null;
    
    //Create prepstmt if possible and do validations related to SQL
    PreparedStatement prepStmt = null;
    connectToDB();
    if(connection != null)
    { //When called from IDE, connection will be null, 
      //so this code won't execute.
      
      prepStmt = connection.prepareStatement(replacedSql);
      ResultSetMetaData rsMeta = prepStmt.getMetaData();
      int numCols = rsMeta.getColumnCount();
      /*
       * Here we want to create a mapping between the alias name 
       * specified in SQL query defining a function and the field
       * of the complex type representing it's returnComponentType.
       */
      if(returnCompType instanceof IComplexType)
      {
        IComplexType compType = (IComplexType) returnCompType;
        fieldMapping = new HashMap<String, IFieldMetadata>();
        
        //Loop index starts from 1
        for(int i=1; i <= numCols; i++)
        {
          String propName = rsMeta.getColumnLabel(i);
          IFieldMetadata fieldMeta = null;
          try
          {
            fieldMeta = findMatchingFieldInType(propName, compType);
          }
          catch(MetadataNotFoundException me)
          {
	    Loggable l =
	      JdbcCartridgeLogger.missingFieldInReturnCompTypeLoggable(propName, 
	                                                               funcName);
            ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                      CLASS_LOADER,
							      propName,
							      funcName
                                                              );
            LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
            throw new JdbcCartridgeException(err.getDetailedMessage(),
	                                     contextName);
          }
          catch(AmbiguousMetadataException ae)
          {
	    Loggable l =
	      JdbcCartridgeLogger.ambiguousFieldInReturnCompTypeLoggable(funcName, 
	                                                                 propName);
            ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                      CLASS_LOADER,
							      funcName, propName
                                                              );
            LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
            throw new JdbcCartridgeException(err.getDetailedMessage(),
	                                     contextName);
          }
          
          Kind expectedKind = fieldMeta.getType().getKind();
	  Kind argumentKind = null;
          if(Datatype.valueOf(rsMeta.getColumnType(i)) != null)
	    argumentKind = (Datatype.valueOf(rsMeta.getColumnType(i))).kind;

	  if(argumentKind == null)  
	  {
	    Loggable l = 
	      JdbcCartridgeLogger.unsupportedSQLTypeForSelectAliasLoggable(rsMeta.getColumnTypeName(i),
	                                                                   propName, 
                                                                           funcName);
            ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                              CLASS_LOADER,
							      rsMeta.getColumnTypeName(i),
							      propName,
							      funcName
                                                              );
            LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
            throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
	  }

          if(fieldMeta.getType().name() != null && 
             fieldMeta.getType().name().equals("java.math.BigDecimal"))
            expectedKind = Kind.BIGDECIMAL;
	    
          if(fieldMeta.getType().name() != null && 
             fieldMeta.getType().name().equals("java.sql.Timestamp"))
            expectedKind = Kind.TIMESTAMP;
	    
          if(!isCompatible(expectedKind, argumentKind))
          {
	    Loggable l =
	      JdbcCartridgeLogger.mismatchedTypesForSelectAliasLoggable(expectedKind.name(),
                                                                        argumentKind.name(),
			                                                propName, funcName);
            ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                              CLASS_LOADER,
							      expectedKind.name(), 
							      argumentKind.name(),
							      propName, funcName
                                                              );
            LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
            throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
          }
          else
            fieldMapping.put(propName, fieldMeta);
        } //end for
      }
      else
      { //return-component-type is native CQL type
        //no need to match alias name here.
        fieldMapping = null;
        if(numCols != 1)
	{
	  Loggable l = 
	    JdbcCartridgeLogger.incorrectUsageOfNativeCQLTypeLoggable(funcName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                            CLASS_LOADER,
							    funcName
                                                            );
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
	}
        Kind expectedKind = returnCompType.getKind();
	Kind argumentKind = null;
        if(Datatype.valueOf(rsMeta.getColumnType(1)) != null)
	  argumentKind = (Datatype.valueOf(rsMeta.getColumnType(1))).kind;
	if(argumentKind == null) 
	{
	  Loggable l
	    = JdbcCartridgeLogger.unsupportedSQLTypeForSelectAliasLoggable(rsMeta.getColumnTypeName(1),
                                                                           rsMeta.getColumnName(1), 
				                                           funcName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                    CLASS_LOADER,
							    rsMeta.getColumnTypeName(1),
							    rsMeta.getColumnName(1),
							    funcName
                                                            );
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
	}
        if(!isCompatible(expectedKind, argumentKind))
        {
	  Loggable l =
	    JdbcCartridgeLogger.typeMismatchForSelectListAliasLoggable(expectedKind.name(), 
	                                                               argumentKind.name(), 
								       funcName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                    CLASS_LOADER,
							    expectedKind.name(),
							    argumentKind.name(),
							    funcName
                                                            );
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
        }
      }
    }
    
    funcMap.put(mapKey, 
                new JdbcCartridgeFunctionMetadata(funcName, retType, 
                                                  replacedSql, paramInfos,
                                                  prepStmtParamToFnParamMapping,
                                                  contextName,
                                                  connection,
                                                  fieldMapping,
                                                  getCartridgeContext(),
                                                  this
                                                  )
               );
  }
  
  private String processParamConfig(String funcName,
    JdbcCartridgeFuncParamConfig pConfig,
    List<JdbcCartridgeFuncParamMetadata> paramInfos,
    Map<String, Integer> paramNameToFnArgListPosMap, 
    int fnArgListPos)
  {
    String paramName = pConfig.getName().trim();
    String paramType = pConfig.getType().trim();
      
    if(!(validParam.matcher(":"+paramName).matches()))
    {
      Loggable l = 
        JdbcCartridgeLogger.invalidParamNameLoggable(paramName, funcName); 
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER,
						        paramName, funcName
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    } 
    //Get the corresponding CQL type. Only native types allowed for now.
    Datatype cqlType = Datatype.getTypeFromCqlName(paramType.toLowerCase());
    
    if((cqlType == null) 
      || (paramType.equalsIgnoreCase("object"))
      || (paramType.equalsIgnoreCase("interval"))
      || (paramType.equalsIgnoreCase("intervalym"))
      || (paramType.equalsIgnoreCase("void"))
      || (paramType.equalsIgnoreCase("xmltype"))
      || (paramType.equalsIgnoreCase("unknown"))
      )
    {
      Loggable l = 
        JdbcCartridgeLogger.invalidParamTypeLoggable(paramType, funcName); 
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER,
							paramType,
							funcName
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    }      
    
    paramInfos.add(new JdbcCartridgeFuncParamMetadata(paramName, cqlType));

    paramNameToFnArgListPosMap.put(":"+paramName, fnArgListPos);
    //return cqltype name to be used for forming signature
    return cqlType.typeName;
  }
  
  private String processSQL(String funcName, String sql, 
                            List<Integer> finalMapping,
                            Map<String, Integer> nameToFnArgListPosMap)
  {
    Matcher m = validParam.matcher(sql);
    StringBuffer sb = new StringBuffer();
    while(m.find())
    {
      String currParam = m.group();
      Integer in = nameToFnArgListPosMap.get(currParam);
      if(in != null)
      {
        m.appendReplacement(sb, "?"); 
        finalMapping.add(in);
      }
      else
      {
        Loggable l = 
          JdbcCartridgeLogger.invalidParamInSQLLoggable(funcName, currParam);
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                  CLASS_LOADER,
							  funcName, currParam
                                                          );
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
	throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }
    }
    
    m.appendTail(sb);
    
    //If the sql string did not have any params while the function expects them
    //raise an error.
    if(nameToFnArgListPosMap.size() > 0)
    {
      if(sb.toString().equals(sql))
      {
        Loggable l =
	  JdbcCartridgeLogger.noParamReferredLoggable(funcName, nameToFnArgListPosMap.size());
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                  CLASS_LOADER,
							  funcName,
							  nameToFnArgListPosMap.size()
                                                          );
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
	throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }
    }
    return sb.toString();
  }
  
  @Activate
  public void activateCartridgeContext(JdbcCartridgeContextConfig ctxConfig) 
    throws Exception 
  {
    if((ctxConfig != null) && (!isCtxRegistered))
    {
      validateAndRegisterCartridgeContext();
    }
  }
  
  @Rollback
  public void rollbackContextConfigChange(JdbcCartridgeContextConfig ctxConfig)
  {
    //Currently we do not support dynamic config update.
    //So @prepare can throw exception only when called for the first time
    //and this is only time this method will be called.
    //Just verifying that by the assertion below.
    assert isCtxRegistered == false;
    bundleContext   = null;
    m_configuration = null;
    contextName     = null;
    dataSourceName  = null;
    symbolicName    = null;
    dataSource      = null;
    connection      = null;
    funcMap         = null;
    typesMap        = null;
  }

  public void setSymbolicName(String symbolicName)
  {
    this.symbolicName = symbolicName;
  }

  /*
   * Returns the application name.
   * Method added for IDE integration as bundleContext
   * will be null at that time. We send the CARTRIDGE_NAME
   * as appname when the bundle context is null.
   */
  private String getSymbolicName()
  {
    if(symbolicName == null)
    {
      assert bundleContext != null : "bundleContext was null";
      return bundleContext.getBundle().getSymbolicName();
    }
    
    return symbolicName;
  }
  
  /*
   * If called from IDE env sets connection to null.
   * Otherwise, if connection is null then creates a new one.
   * If it is already non-null then does nothing.
   */
  private void connectToDB() throws SQLException 
  {
    //In IDE env bundleContext would be null and we don't have db access.
    if(bundleContext == null)
    {
      connection = null;
    }
    else //non-IDE env
    {
      if(connection == null)
      {
        assert m_configuration != null : "Configuration is not set";
        try {
            dataSource = getDataSource(dataSourceName, m_configuration);
        } catch(ConfigurationException e) {
            dataSource = null; // It is dealt with below
        }

        if(dataSource == null)
        {
	  Loggable l = 
	    JdbcCartridgeLogger.invalidDataSourceValueLoggable(dataSourceName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                    CLASS_LOADER,
							    dataSourceName
                                                            );
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
        }    
    
        //Get the connection 
        connection = dataSource.getConnection();
        if(connection == null)
        {
	  Loggable l =
	    JdbcCartridgeLogger.nullConnectionObtainedLoggable(dataSourceName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                            CLASS_LOADER,
							    dataSourceName);
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
        }
      }
    }
  }

  protected boolean renewConnection()
  {
    long retryInterval = 3000l;
    boolean returnVal = false;

    try
    {
      if(connection != null) 
        connection.close();
      connection = null;

      LogUtil.info(LoggerType.TRACE, "Waiting for "+retryInterval +" milliseconds"
        +" before attempting to reconnect to the database");
      Thread.currentThread().sleep(retryInterval);

      if(dataSource == null)
      {
        assert m_configuration != null : "Configuration is not set";
        try {
            dataSource = getDataSource(dataSourceName, m_configuration);
        } catch(ConfigurationException e) {
            dataSource = null; // It is dealt with below
        }

        if(dataSource == null)
        {
	  Loggable l = 
	    JdbcCartridgeLogger.invalidDataSourceValueLoggable(dataSourceName);
          ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
	                                                    CLASS_LOADER,
							    dataSourceName
                                                            );
          LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
          throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
        }    
      }

      // At this point datasource is never null  
      // Get the connection 
      connection = dataSource.getConnection();
      if(connection == null)
      {
        Loggable l =
          JdbcCartridgeLogger.nullConnectionObtainedLoggable(dataSourceName);
        ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                          CLASS_LOADER,
                                                          dataSourceName);
        LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
        throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
      }

      //Connection is available here so set it in the function metadata
      Iterator<IUserFunctionMetadata> funcIter = funcMap.values().iterator();
      while(funcIter.hasNext())
      {
        JdbcCartridgeFunctionMetadata fMeta = (JdbcCartridgeFunctionMetadata) funcIter.next();
        fMeta.setConnection(connection);
      }
      LogUtil.info(LoggerType.TRACE, "Set the new connection in "+funcMap.values().size() +" functions");
      returnVal = true;
    }
    catch(SQLException se)
    {
      LogUtil.warning(LoggerType.TRACE, "Encountered exception in renewConnection() "+se.getMessage());
      //if connection is null then dataSource.getConnection might have caused the issue
      if(connection == null)
        returnVal = renewConnection();
      else
        returnVal = false;
    }
    catch(InterruptedException ie)
    {
      LogUtil.warning(LoggerType.TRACE, "Interrupted while waiting to renew connection");
      returnVal = false;
    }
    return returnVal;
  }

  static DataSource getDataSource(String inputDataSourceName,
              com.bea.wlevs.configuration.internal.Configuration configuration)
      throws ConfigurationException
  {
        Object serviceObject = null;
      
        try 
        {
          //Get DataSource dynamically to avoid dragging in CE datasource 
	  //bundles.
          Class dataSourceServiceClass = 
	    Class.forName("com.bea.core.datasource.DataSourceService");
      
          serviceObject = 
            configuration.getConfiguredServerService(dataSourceServiceClass, 
                                                       inputDataSourceName, 
                                                       "com.bea.core.datasource.ce",
                                                       "OSGI-INF/datasource.xml",
                                                       "Data Source", 1000);
        
          if(serviceObject != null)
          {
            Method m = dataSourceServiceClass.getMethod("getDataSource");
            return (DataSource)m.invoke(serviceObject);
          }  
          else
              throw new ConfigurationException("Could not find datasource: " + inputDataSourceName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
  }
  
  private void validateAndRegisterCartridgeContext() throws SQLException,
                                                            CartridgeException
  {
    /*
     * Flow:
     * 1. Get the cartridgeRegistry
     * 2. Create property map
     * 3. Register the cartridge context
     */  
    assert contextName  != null : "context name not set";
    assert dataSourceName != null : "data source name not set";
    assert funcMap != null : "function map not set";
        
    //Get the cartridgeRegistry
    m_registry = JdbcCartridge.getCartridgeRegistry();
    if(m_registry == null)
    {
      Loggable l = JdbcCartridgeLogger.cartridgeRegistryNotFoundLoggable();
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    }
       
    //Construct property map
    Map<String, Object> props = new HashMap<String, Object>();
    props.put(CONTEXT_NAME, contextName);
    props.put(DATA_SOURCE_NAME, dataSourceName);
    props.put(DATA_SOURCE_OBJ, dataSource);
    props.put(FUNC_MAP, funcMap);
    props.put(TYPE_MAP, typesMap);
    
    //Register the cartridge appl context
    m_registry.registerApplicationContext(getSymbolicName(),
                                          contextName,
                                          JdbcCartridge.CARTRIDGE_NAME, 
                                          props);
    
    //set the isCtxRegistered to true
    isCtxRegistered = true;

    if(bundleContext != null)
    { //non-IDE environment
      JdbcCartridge.addAppCartridgeContext(bundleContext.getBundle().getBundleId(),
                                           this);
    }
    
    LogUtil.info(LoggerType.CUSTOMER, "registerApplicationContext ("+
                                       getSymbolicName()+
                                       ", "+contextName+", "+
                                       JdbcCartridge.CARTRIDGE_NAME+");");
  }
  
  @Override
  public void destroy() throws Exception 
  {
    if(bundleContext == null)
    { //IDE environment calls this method and in that env bundleContext is null.
      //So we continue to keep the application context removal code here.
      unregisterApplicationContext(getSymbolicName());
    }
  }

  public void unregisterApplicationContext(String appName) 
    throws SQLException, CartridgeException
  {
    //Get the cartridgeRegistry
    ICartridgeRegistry m_registry = JdbcCartridge.getCartridgeRegistry();
    if(m_registry == null)
    {
      Loggable l =
        JdbcCartridgeLogger.cartridgeRegistryNotFoundLoggable();
      ErrorMessage err = ExceptionUtils.getErrorMessage(l, LOCALIZER_CLASS, 
                                                        CLASS_LOADER
                                                        );
      LogUtil.severe(LoggerType.CUSTOMER, err.getMessage());
      throw new JdbcCartridgeException(err.getDetailedMessage(),contextName);
    }
    
    if(isCtxRegistered)
      m_registry.unregisterApplicationContext(JdbcCartridge.CARTRIDGE_NAME,
                                              appName,
                                              contextName);
   
    LogUtil.info(LoggerType.CUSTOMER,
               "unregisterApplicationContext("+ JdbcCartridge.CARTRIDGE_NAME+
               ", "+appName+", "+contextName+");");
    
    if(connection != null)
      connection.close();
    isCtxRegistered = false;
  }
  
  private boolean isCompatible(Kind expectedKind, Kind argumentKind)
  {
    switch(expectedKind)
    {
      case INT:
      case BIGINT:
      case FLOAT:
      case DOUBLE:
      case BIGDECIMAL:
        if((argumentKind == Kind.INT)
	  || (argumentKind == Kind.BIGINT)
	  || (argumentKind == Kind.FLOAT)
	  || (argumentKind == Kind.DOUBLE)
	  || (argumentKind == Kind.BIGDECIMAL)
	  )
	  return true;
	else
	  return false;
      case BOOLEAN:
        if(argumentKind == Kind.BOOLEAN)
          return true;
        else
          return false;
      case CHAR:
        if(argumentKind == Kind.CHAR)
          return true;
        else 
          return false;
      case BYTE:
        if(argumentKind == Kind.BYTE)
          return true;
        else 
          return false;
      case TIMESTAMP:
        if(argumentKind == Kind.TIMESTAMP)
          return true;
        else
          return false;
      case OBJECT:
        if(argumentKind == Kind.OBJECT)
          return true;
	else
          return false;
      case XMLTYPE:
      case INTERVAL:
      case VOID:
      case UNKNOWN:
      default:
        return false;   //FIXME: should xml & interval be allowed?
    }
  }
  
  private final static class CartridgeContextImpl implements ICartridgeContext
  {
    private final HashMap<String, Object> properties;
    private final String applicationName;
    
    CartridgeContextImpl(String applicationName) 
    {
      this.applicationName = applicationName;
      properties = new HashMap<String,Object>(0);
    }
    
    @Override
    public String getApplicationName()
    {
      return applicationName;
    }


    @Override
    public Map<String, Object> getProperties()
    {
      return properties;
    }
  }

}
