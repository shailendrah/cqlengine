/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/UserFunctionManager.java /main/41 2012/05/09 06:42:13 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    UserFunctionManager is the manager for user-defined functions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/30/12 - add bi sql equivalent
    anasrini    09/09/11 - XbranchMerge anasrini_bug-12943064_ps5 from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    06/22/11 - set sqlequivalent for simple function
    parujain    10/02/09 - Dependency map
    sborah      07/10/09 - support for bigdecimal
    alealves    02/02/09 - support for user function instances in addition to class name
    parujain    01/28/09 - Transaction mgmt
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      12/05/08 - add Id in FuncDesc
    hopark      11/06/08 - add registerFunc
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    10/01/08 - drop schema
    parujain    09/12/08 - multiple schema support
    parujain    09/04/08 - maintain offset
    hopark      06/18/08 - logging refactor
    udeshmuk    05/12/08 - register aggr functions as well in simple function
                           set to support type conversion.
    hopark      03/26/08 - server reorg
    parujain    05/07/08 - fix lock problem
    parujain    05/07/08 - fix lock problem
    parujain    04/01/08 - get all queryids
    sbishnoi    03/31/08 - modifying registerfunction to add class loader as an
                           argument
    udeshmuk    03/11/08 - remove the static block used for initialization
    mthatte     02/26/08 - parametrizing metadata errors
    udeshmuk    02/12/08 - support for all nulls as function arguments.
    parujain    02/07/08 - parameterizing errors
    udeshmuk    02/05/08 - parameterize error.
    hopark      02/05/08 - fix dump level
    hopark      01/08/08 - metadata logging
    udeshmuk    01/11/08 - handle NULL arguments.
    udeshmuk    09/20/07 - Modifying registerAggrFunction to handle the builtin
                           aggr functions.
    parujain    06/21/07 - release read lock
    parujain    05/02/07 - getFunction method for stats
    hopark      03/21/07 - storage re-org
    sbishnoi    03/07/07 - modify Type conversion overloading
    sbishnoi    02/06/07 - modify exception constructor
    parujain    01/31/07 - drop function
    parujain    01/12/07 - BDB integration
    parujain    11/29/06 - Add Constant
    parujain    11/21/06 - Type conversion overloading
    dlenkov     10/30/06 - overload resolution
    parujain    10/24/06 - merge conflicts
    najain      10/23/06 - remove installation
    dlenkov     10/12/06 - more builtin functions
    parujain    10/11/06 - Operator built-in functions
    anasrini    10/09/06 - support for SYSTIMESTAMP
    parujain    09/25/06 - NVL implementation
    parujain    09/21/06 - To_timestamp built-in function
    najain      09/21/06 - support function overloading
    najain      09/13/06 - add builtin user functions
    parujain    07/14/06 - moved CacheObjectManager 
    anasrini    06/27/06 - Support for user aggregation functions 
    parujain    06/27/06 - metadata cleanup 
    anasrini    06/12/06 - support for functions 
    najain      04/28/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/UserFunctionManager.java /main/41 2012/05/09 06:42:13 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */

package oracle.cep.metadata;

import java.util.HashMap;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.install.Install;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.factory.AggrFunctionFactory;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.parser.CEPAggrFnDefnNode;
import oracle.cep.parser.CEPAttrSpecNode;
import oracle.cep.parser.CEPFunctionDefnNode;
import oracle.cep.parser.CEPFunctionRefNode;
import oracle.cep.semantic.TypeConverter;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IUserFunctionLocator;
import oracle.cep.transaction.ITransaction;

/**
 * UserFunctionManager is the manager for all types of user-defined functions
 * i.e. for both single element as well as aggregate functions
 *
 * @author najain
 * @since 1.0
 */
@DumpDesc(evPinLevel=LogLevel.MUSERFUNC_ARG,
          dumpLevel=LogLevel.MUSERFUNC_INFO,
          verboseDumpLevel=LogLevel.MUSERFUNC_LOCKINFO)
public class UserFunctionManager extends CacheObjectManager 
implements ILoggable
{ 
  /**
   * Hashmap to maintain mapping of function class and StaticMetadata object.
   * String - Name of the function class acts as a key. All functions with same
   * function name and same number of arguments belong to one function class. 
   * In order to avoid repeating of same information for functions like 
   * plus(int,int), plus(float,float) etc. we create a single StaticMetadata
   * object which can be accessed by the key 'plus2' where 2 is the number 
   * of arguments.
   * StaticMetadata - object containing required information
   */
  private HashMap<String,StaticMetadata> staticMetadataMap;
  
  private int[] cost;

  private ExecContext execContext;
  
 /** The initial size of the array */
 /** This is not a boundary condition */
  public  final int START_SIZE = 2;
  
  public static class FuncDesc
  {
    static int s_nextId = 0;
    int         id;
    String      name;
    String      className;
    Datatype    returnType;
    Datatype[]  argTypes;
    String      sqlEquivalent;
    String      biSqlEquivalent;
    public FuncDesc(String n, Datatype[] atypes, Datatype rtyp, String cls,
                    String sqlEquivalent, String biSqlEquivalent)
    {
      id = s_nextId++;
      name = n;
      className = cls;
      returnType = rtyp;
      argTypes = atypes;
      this.sqlEquivalent = sqlEquivalent;
      this.biSqlEquivalent = biSqlEquivalent;
    }
    
    public boolean isAggrFunc() {return false;}
    public int getId() {return id;}
  }
  
  public static class AggrFuncDesc extends FuncDesc
  {
    boolean incremental;
    public AggrFuncDesc(String n, Datatype[] atypes, Datatype rtyp, String cls, boolean incremental)
    {
      super(n, atypes, rtyp, cls, null, null);
      this.incremental = incremental;
    }
    public boolean isAggrFunc() {return true;}
    public boolean isIncremental() {return incremental;}
  }
  
  /**
   * Constructor for UserFunctionManager. The constructor has been kept private
   * intentionally so that no-one can create a new instance of a
   * UserFunctionManager. This way, only a single instance of the
   * UserFunctionManager is present, and it can be accessed globally via
   * UserFunctionManager.getUserFunctionManager().
   */
  public UserFunctionManager(ExecContext ec, Cache cache)
  {
    super(ec.getServiceManager(), cache);
    execContext = ec;
    cost = new int[START_SIZE];
    staticMetadataMap = new HashMap<String, StaticMetadata>();
  }
  
  /**
   * Creates staticMetadata objects hashmap
   */
  public void initialize()
  {
    /*
     * We don't call ColtInstall's populateStaticMetadata because as of now
     * we don't want to create staticMetadata objects for colt functions.
     */
    Install.populateStaticMetadata(execContext);
  }
  
  public void addStaticMetadataObject(String funcName, int numParams, StaticMetadata sm)
  {
    String funcClassName = funcName.concat(Integer.toString(numParams));
    staticMetadataMap.put(funcClassName, sm);  
  }
  
  public StaticMetadata getMetadataObject(String funcName, int numParams)
  {
    String funcClassName = funcName.concat(Integer.toString(numParams));
    if (staticMetadataMap.containsKey(funcClassName))
      return (StaticMetadata)staticMetadataMap.get(funcClassName);
    else 
      return null;
  }
  
  public boolean getIsResultNull(String funcName, int length) 
  {
    boolean isResultNull = false;
    
    StaticMetadata sm = getMetadataObject(funcName, length);
    if (sm != null)
      isResultNull = sm.getIsResultNull();
    
    return isResultNull;
  }
  
  public static String getUniqueFunctionName(String funcName, Datatype[] dt)
  {
    int len = 0;
    
    // name of the function
    len += funcName.length();

    // (
    len += 1;

    // parameters
    for (int i = 0; i < dt.length; i++)
    {
      // datatype of the parameter
      len += dt[i].toString().length();

      // , for all the parameters except the last one
      if (i != dt.length-1)
        len += 1;
    }

    // )
    len += 1;

    char[] uniqName = new char[len];
    int pos = 0;

    for (int i = 0; i < funcName.length(); i++)
      uniqName[pos++] = funcName.charAt(i);

    uniqName[pos++] = '(';

    for (int i = 0; i < dt.length; i++)
    {
      String str = dt[i].toString();

      for (int j = 0; j < str.length(); j++)
       uniqName[pos++] = str.charAt(j);

      if (i != dt.length-1)
        uniqName[pos++] = ',';
    }

    uniqName[pos] = ')';
    return new String(uniqName);
  }
  
  /**
   * Register a new simple function
   * 
   * @param fdn
   *          Definition
   * @return Id
   * @throws MetadataException if meta-data is missing or invalid
   * @throws CEPException if userFunctionLocator is not found for implementation function instances.
   */
  public int registerSimpleFunction( CEPFunctionDefnNode fdn, String cql, 
        String schema) throws CEPException 
  {
    return registerSimpleFunction(fdn, null, cql, schema);
  }
  
  public int registerSimpleFunction( FuncDesc fdn, 
      String schema) throws CEPException 
  {
    String cql = fdn.name; //TODO do we need this?
    return registerSimpleFunction(null, fdn, cql, schema);
  }
  
  private int registerSimpleFunction(CEPFunctionDefnNode fdn,
                                     FuncDesc funcDesc, 
                                     String cql, String schema) 
  throws CEPException 
  {
    String funcName;
    Datatype[] dts;
    Datatype returnType;
    String implClassName;
    String implInstanceName;
    int startOffset;
    int endOffset;
    ITransaction txn = execContext.getTransaction();
    
    if (fdn != null)
    {
        funcName = fdn.getName();
        returnType = fdn.getReturnType();
        implClassName = fdn.getImplClassName();
        implInstanceName = fdn.getImplInstanceName();
        startOffset = fdn.getStartOffset();
        endOffset = fdn.getEndOffset();
        // parameters
        CEPAttrSpecNode[] specs = fdn.getParamSpecList();
        dts = new Datatype[specs.length];
        for (int i = 0; i < specs.length; i++)
        dts[i] = specs[i].getDatatype();
    }
    else
    {
        funcName = funcDesc.name;
        dts = funcDesc.argTypes;
        returnType = funcDesc.returnType;
        implClassName = funcDesc.className;
        // Invoked from seeding. In this case, a implClassName is always used instead of a implInstanceName.
        implInstanceName = null;
        startOffset = 0;
        endOffset = 0;
    }
    
    LogLevelManager.trace(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_CREATE, this, cql, funcName);

    String uniqName = getUniqueFunctionName( funcName, dts);
    
    CacheLock l = null;
    int fnId = -1;
   
    // Check if any function of the same name exists
    UserFunction uf = null;
    try {
      uf = getFunction(uniqName, schema);
    }
    catch (MetadataException me) {
      me.setStartOffset(startOffset);
      me.setEndOffset(endOffset);
      if (!(me.getErrorCode() == MetadataError.FUNCTION_NOT_FOUND))
        throw me;
    }
    if (uf != null)
      throw new MetadataException( MetadataError.FUNCTION_ALREADY_EXISTS,
          new Object[] {funcName});

    // Check for duplicate simple function name and create
    l = createObject(txn, uniqName, schema, CacheObjectType.SINGLE_FUNCTION, null);

    if (l == null) {
      throw new MetadataException(MetadataError.FUNCTION_ALREADY_EXISTS,
          startOffset, endOffset,
          new Object[] {funcName});
    }

    // Initialize
    SimpleFunction fn = (SimpleFunction) l.getObj();
    fnId = fn.getId();
    fn.setCreationText(cql);

    // call the common setup
    if (fdn != null)
    {
      addParams( fn, fdn);
    }
    else
    {
      addParams( fn, dts);
    }
    // Set the return type
    fn.setReturnType(returnType);

    // Set whether this is built in
    if (schema.equals(execContext.getServiceSchema(Constants.DEFAULT_SCHEMA)))
      fn.setBuiltIn(true);

    //Set the sqlEquivalent if funcDesc is available, otherwise it will be null
    if(funcDesc != null)
    {
      fn.setSQLEquivalent(funcDesc.sqlEquivalent);
      fn.setBISQLEquivalent(funcDesc.biSqlEquivalent);
    }
    
    setImplFunction(fdn, implClassName, implInstanceName, startOffset,
        endOffset, fn);

    try {
      registerSimpleFunctionSet(txn, funcName,schema, fnId);
    }catch(MetadataException me)
    {
      me.setStartOffset(startOffset);
      me.setEndOffset(endOffset);
      throw me;
    }

    return fnId;
  }
  
  public void registerSimpleFunctionSet(ITransaction txn, 
                       String funcName, String schema, int fnId)
  throws MetadataException
  {
    CacheLock l = null;
    l = findCache(txn,
        new Descriptor( funcName, CacheObjectType.SIMPLE_FUNCTION_SET, 
            schema, null), true);
    if (l == null) {

      l = createObject(txn, funcName, schema, CacheObjectType.SIMPLE_FUNCTION_SET, null);
    }

    SimpleFunctionSet fns = (SimpleFunctionSet) l.getObj();

    fns.addFunc(fnId); 

    return;
  }

  /**
   * Register a new aggregate function in the system.
   * 
   * @param fdn
   *          Definition
   * @return Id
   * @throws MetadataException
   */
  public int registerAggrFunction(CEPAggrFnDefnNode fdn, String cql, 
                                  String schema) 
    throws CEPException 
  {
    return registerAggrFunction(fdn, null, cql, schema);
  }
  
  public int registerAggrFunction(AggrFuncDesc fdn, String schema) 
    throws CEPException 
  {
    return registerAggrFunction( null, fdn, fdn.name, schema);
  }
  
  private int registerAggrFunction(CEPAggrFnDefnNode fdn, 
                                   AggrFuncDesc funcDesc, String cql,
                                   String schema)
  throws CEPException 
  {
    String funcName;
    Datatype[] dts; 
    Datatype returnType; 
    String implClassName;
    String implInstanceName;
    boolean supportsIncremental;
    int startOffset;
    int endOffset;
    ITransaction txn = execContext.getTransaction();

    if (fdn != null)
    {
      funcName = fdn.getName();
      returnType = fdn.getReturnType();
      implClassName = fdn.getImplClassName();
      implInstanceName = fdn.getImplInstanceName();
      supportsIncremental = fdn.supportsIncremental();
      startOffset = fdn.getStartOffset();
      endOffset = fdn.getEndOffset();

      CEPAttrSpecNode[] specs = fdn.getParamSpecList();
      dts = new Datatype[specs.length];
      for (int i = 0; i < specs.length; i++)
        dts[i] = specs[i].getDatatype();
    }
    else
    {
      funcName = funcDesc.name;
      dts = funcDesc.argTypes;
      returnType = funcDesc.returnType;
      implClassName = funcDesc.className;
      // Invoked from seeding. In this case, a implClassName is always used instead of a implInstanceName.      
      implInstanceName = null;
      supportsIncremental = funcDesc.incremental;
      startOffset = 0;
      endOffset = 0;
    }

    CacheLock    l = null;
    AggFunction  fn;
    UserFunction uf = null;
    int          fnId;
    String uniqName = getUniqueFunctionName(funcName, dts);


    // Check if a simple function of the same name exists
    try {
      uf = getFunction(uniqName, schema);
    }
    catch (MetadataException me) {
      me.setStartOffset(startOffset);
      me.setEndOffset(endOffset);
      if (!(me.getErrorCode() == MetadataError.FUNCTION_NOT_FOUND))
        throw me;
    }
    if (uf != null)
      throw new MetadataException(MetadataError.FUNCTION_ALREADY_EXISTS,
          startOffset, endOffset,
          new Object[] {funcName});

    // Check for duplicate aggregate function name and create
    l = createObject(txn, uniqName, schema, CacheObjectType.AGGR_FUNCTION, null);

    if (l == null)
    {
      throw new MetadataException(MetadataError.FUNCTION_ALREADY_EXISTS,
          startOffset, endOffset,
          new Object[] {funcName});
    }

    // Initialize
    fn   = (AggFunction) l.getObj();
    fnId = fn.getId();
    fn.setCreationText(cql);

    // call the common setup
    if (fdn != null)
    {
      addParams( fn, fdn);
    }
    else
    {
      addParams( fn, dts);
    }
    // Set the return type
    fn.setReturnType(returnType);

    // Incremental Computation
    fn.setSupportsIncremental(supportsIncremental);

    // Set whether this is built in
    if (schema.equals(execContext.getServiceSchema(Constants.DEFAULT_SCHEMA)))
      fn.setBuiltIn(true);

    setAggrImplFunction(fdn, implClassName, implInstanceName, startOffset,
        endOffset, fn);

    // call this for aggr function as well since we need to support type conversion
    try{
      registerSimpleFunctionSet(txn, funcName, schema, fnId);
    }catch(MetadataException e)
    {
      e.setStartOffset(startOffset);
      e.setEndOffset(endOffset);
      throw e;
    }

    return fnId;
  }
  
  private void setImplFunction(CEPFunctionDefnNode fdn, String implClassName,
                               String implInstanceName, int startOffset,
                               int endOffset, SimpleFunction fn)
    throws CEPException
  {
    SingleElementFunction f = null;
    
    if (implClassName != null) 
    {
      // Set the implementation class name
      fn.setImplClassName(implClassName);
      
      // The native implemented functions are created with class $dummy
      if (!(implClassName.equalsIgnoreCase(UserFunction.DUMMY))) 
      {
        try 
        {
          Class<?> cf = instantiateClass(implClassName, startOffset,
                                         endOffset);
          f = (SingleElementFunction) cf.newInstance();
        } 
        catch (CEPException ce)
        {
          throw ce;
        }
        catch (Exception e) 
        {
          throw new
            MetadataException( MetadataError.INVALID_IMPL_CLASS_FOR_FUNCTION, e,
                fdn.getStartOffset(), fdn.getEndOffset(),
                new Object[]{fdn.getImplClassName()});
        }        
      }
    } 
    else 
    {
      assert implInstanceName != null;
      
      fn.setImplInstanceName(implInstanceName);
      
      IUserFunctionLocator userFunctionLocator =
        execContext.getServiceManager().getConfigMgr().getUserFunctionLocator();
      
      if (userFunctionLocator == null) {
        LogUtil.warning(LoggerType.TRACE, "user function locator service " +
                            "feature not provided in the environement");
        throw new CEPException(
              InterfaceError.USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT);
      }
      
      f = userFunctionLocator.getUserFunction(implInstanceName);
      
      if (f == null) {
        throw new
          MetadataException(MetadataError.FUNCTION_IMPL_INSTANCE_NOT_FOUND, 
                       fdn.getStartOffset(), fdn.getEndOffset(),
                       new Object[]{implInstanceName});
        
      }
    }

    fn.setImplClass(f);
  }  

  private void setAggrImplFunction(CEPAggrFnDefnNode fdn, String implClassName,
      String implInstanceName, int startOffset, int endOffset, AggFunction fn) 
  throws CEPException
  {
    if (implClassName != null) {
      // Set the implementation class name
      fn.setImplClassName(implClassName);
      
      // The native implemented functions are created with class $dummy
      if (!(implClassName.equalsIgnoreCase(UserFunction.DUMMY))) 
      {
        // Validate that the implementation class implements IAggrFnFactory
        try 
        {
          Class<?> cf = instantiateClass(implClassName, startOffset, 
                                         endOffset);          
          
          @SuppressWarnings("unused")
          IAggrFnFactory aggrFnFact = (IAggrFnFactory) cf.newInstance();
        } 
        catch(CEPException ce)
        {
          throw ce;
        }
        catch(Exception e) 
        {
          throw
          new MetadataException(MetadataError.INVALID_IMPL_CLASS_FOR_FUNCTION,
              e, startOffset, endOffset,
              new Object[]{implClassName});
        }
      }
    } 
    else {
      assert implInstanceName != null;

      fn.setImplInstanceName(implInstanceName);
      
      IUserFunctionLocator userFunctionLocator =
        execContext.getServiceManager().getConfigMgr().getUserFunctionLocator();
      
      if (userFunctionLocator == null) {
        LogUtil.warning(LoggerType.TRACE, "user function locator service " +
                            "feature not provided in the environement");
        throw new CEPException(
              InterfaceError.USERFUNC_LOCATOR_NOT_SUPPORTED_IN_THIS_ENVIRONMENT);
      }
      
      IAggrFnFactory iAggrFactory = 
        userFunctionLocator.getUserAggrFunction(implInstanceName);
      
      if (iAggrFactory == null) {
        throw new
          MetadataException(MetadataError.FUNCTION_IMPL_INSTANCE_NOT_FOUND, 
                       fdn.getStartOffset(), fdn.getEndOffset(),
                       new Object[]{implInstanceName});
        
      } else {
        FactoryManager factoryManager = 
          execContext.getServiceManager().getFactoryManager();
        
        // Need to wrap user-defined factory with our memory manager's factory mechanism.
        fn.setAggrFactory(new AggrFunctionFactory(factoryManager, iAggrFactory));
      }
    }
  }
  
  private void addParams(UserFunction fn, CEPFunctionDefnNode fdn) 
    throws MetadataException
  {
    CEPAttrSpecNode[] paramSpecList;
    CEPAttrSpecNode   attrSpec;
    int               numParams;

    // Set the parameter list
    paramSpecList = fdn.getParamSpecList();
    numParams     = fdn.getNumParams();
    for(int i=0; i<numParams; i++) {
      attrSpec = paramSpecList[i];
      try{
      fn.addParam(new Attribute(attrSpec.getName(), attrSpec.getAttributeMetadata()));
      }catch(MetadataException me)
      {
    	me.setStartOffset(attrSpec.getStartOffset());
    	me.setEndOffset(attrSpec.getEndOffset());
    	throw me;
      }
    }
  }

  private void addParams(UserFunction fn, Datatype[] dts) 
    throws MetadataException
  {
    int numParams     = dts.length;
    for(int i=0; i<numParams; i++) {
      try
      {
        fn.addParam(new Attribute("c"+i, new AttributeMetadata(dts[i], 0, dts[i].getPrecision(), 0)) ); //FIXME. lengths[i]
      }catch(MetadataException me)
      {
        throw me;
      }
    }
  }
  
  public void dropFunction(CEPFunctionRefNode frn, String schema) 
     throws MetadataException
  {
    ITransaction txn = execContext.getTransaction();
    
     //  parameters
    CEPAttrSpecNode[] specs = frn.getParamSpecList();
    Datatype[] dt = new Datatype[specs.length];
    for (int i = 0; i < specs.length; i++)
      dt[i] = specs[i].getDatatype();
    

    LogLevelManager.trace(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_DELETE, this, frn.getName());
    
    Locks locks = null;
    Locks sfs_locks = null;
    CacheLock l = null;
    CacheLock set_lock = null;
    
   
    String uniqName = getUniqueFunctionName(frn.getName(), dt);
    
    UserFunction uf = null;
    uf = getFunction( uniqName, schema);
    
    //If the function to be dropped does not exist
    if(uf == null)
      throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND,
                                  frn.getStartOffset(), frn.getEndOffset(),
        		                  new Object[]{frn.getName()});
    
    if(uf.isBuiltIn())
      throw new MetadataException(MetadataError.CANNOT_DROP_BUILTIN_FUNCTION,
                                  frn.getStartOffset(), frn.getEndOffset(),
                                  new Object[] {frn.getName()});
    
  //  if(execContext.getDependencyMgr().isAnyDependentPresent
  //                                    (uf.getId(), DependencyType.QUERY))
    if(execContext.getDependencyMgr().areDependentsPresent(uf.getId()))	
      throw new MetadataException(MetadataError.CANNOT_DROP_FUNCTION_QUERY_EXISTS,
                                  frn.getStartOffset(), frn.getEndOffset(),
                                  new Object[] {frn.getName()});
    
    locks = deleteCache(txn, uf.getId());
    if(locks == null)
      throw new MetadataException(MetadataError.INVALID_FUNCTION_IDENTIFIER,
                                  frn.getStartOffset(), frn.getEndOffset(),
                                  new Object[]{uf.getId()});
    
    l = locks.objLock;
    uf = null;
    uf = (UserFunction)l.getObj();
      
    // remove any statistics if maintained by ExecStatManager
    execContext.getExecStatsMgr().removeFuncStats(uf.getId());
    
    set_lock = findCache(txn,
        new Descriptor( frn.getName(), CacheObjectType.SIMPLE_FUNCTION_SET, 
                        schema, null), true);
    
    if(set_lock != null)
    {      
      SimpleFunctionSet sfs = (SimpleFunctionSet) set_lock.getObj();
      try {
        sfs.removeFunc(uf.getId());
      }catch(MetadataException me)
      {
        me.setStartOffset(frn.getStartOffset());
        me.setEndOffset(frn.getEndOffset());
        throw me;
      }
      if(sfs.isEmpty())
      {
        int sfs_id = sfs.getId();
        sfs_locks = deleteCache(txn, sfs_id);   
        set_lock = sfs_locks.objLock;
      }
    }
    
  }
  
  /**
   * Get the function metadata object given its internal identifier.
   * <p>
   * The function object returned could correspond to either a single element
   * function or aggregate function
   * 
   * @param id
   *          internal identifier of the function
   * @return the function metadata object
   * @throws MetadataException
   */
  public UserFunction getFunction( int id) throws MetadataException
  {
    CacheLock    l   = null;
    UserFunction fn  = null;
    ITransaction txn = execContext.getTransaction();
    try
    {
      // Get name
      l = findCache(txn, id, false, CacheObjectType.SINGLE_FUNCTION);
      if(l == null) {
        l = findCache(txn, id, false, CacheObjectType.AGGR_FUNCTION); 
        if (l == null) {
          throw 
            new MetadataException(MetadataError.INVALID_FUNCTION_IDENTIFIER,
            		              new Object[]{id});
        }
      }
      fn = (UserFunction) l.getObj();
    }
    finally
    {
      // Release Read lock
      if (l != null)
        release(txn, l);
    }

    return fn;
  }

    
  /**
   * Get function metadata object given its name.
   * <p>
   * The function object returned could correspond to either a single element
   * function or aggregate function
   * 
   * @param name
   *          Function name
   * @return the function metadata object
   * @throws MetadataException
   */
  public UserFunction getFunction(String name, 
		                 String schema) throws MetadataException
  {
    CacheLock    l = null;
    UserFunction fn;
    ITransaction txn = execContext.getTransaction();

    try
    {
      // Get cache object
      l = findCache(txn, new Descriptor(name, CacheObjectType.SINGLE_FUNCTION, 
                                  schema, null), false);

      if (l == null) {
        l = findCache(txn, new Descriptor(name, CacheObjectType.AGGR_FUNCTION,
                                  schema, null), false);
        if (l == null) {
            return null;
        }
      }
      
      // Get function
      fn = (UserFunction) l.getObj();
    }
    finally
    {
      // Release read lock
      if (l != null)
        release(txn,l);
    }

    return fn;
  }

  /**
   * Get simple function metadata object with formal parameters
   * given its name and parameter datatypes.
   * 
   * @param name
   *          Function name
   * @param dts
   *          parameter datatypes
   * @return the function metadata object
   * @throws MetadataException
   */
  public UserFunction getValidFunction(String name, Datatype[] dts, 
                                       String schema,
                                       boolean isAggrAllowed,
                                       boolean allInputsNull)
    throws MetadataException, CEPException {

    CacheLock l = null;
    UserFunction fn;
    String fullName = getUniqueFunctionName( name, dts);
    
    ITransaction txn = execContext.getTransaction();
    
    // Get cache object
    try
    {
      l = findCache(txn,
                  new Descriptor( fullName, CacheObjectType.AGGR_FUNCTION, 
                		  schema, null), false);
      if (l != null && !isAggrAllowed)
        throw new CEPException(SemanticError.AGGR_FN_NOT_ALLOWED_HERE,
                               new Object[]{fullName});
  
      if (l == null)
        l = findCache(txn,
              new Descriptor( fullName, CacheObjectType.SINGLE_FUNCTION, 
            		  schema,null), false);
  
      if (l == null) {
        CacheLock cl = null;
        cl = findCache(txn,
             new Descriptor( name, CacheObjectType.SIMPLE_FUNCTION_SET, 
            		 schema, null), false);
        // In case of exception show name with the input parameter sent 
        if (cl == null)
            throw new MetadataException( MetadataError.FUNCTION_NOT_FOUND, new Object[]{fullName});
  
        SimpleFunctionSet fns = (SimpleFunctionSet)cl.getObj();
        Datatype[] fdts=null;
        try
        {
          fdts = findFunc(fns, dts);
        }
        catch(CEPException e)
        {
          if (e.getErrorCode().equals(SemanticError.TOO_MANY_FUNCS_TYPE_CONVERSION_ERROR) &&
              allInputsNull)
          {
            StaticMetadata sm = this.getMetadataObject(name, dts.length); 
            if (sm != null) //get the signature to be used
              fdts = sm.getSignature();
            else // no static meta-data present
              throw e;
          }
          else throw e;
        }
        finally
        {
          // release read lock
          if(cl != null)
            release(txn, cl);
        }
        
        fullName = getUniqueFunctionName(name, fdts);
        l = findCache(txn,
              new Descriptor(fullName, CacheObjectType.SINGLE_FUNCTION,
            		  schema, null), false);
        
        if(l == null)
        {// function is aggr function
          //This check is done before finding the function in the cache because we are sure that 
          //the function will be found as fullname is constructed as per the parameter types 
          //returned by findFunc()
          if(!isAggrAllowed)
            throw new CEPException(SemanticError.AGGR_FN_NOT_ALLOWED_HERE,
                                   new Object[]{fullName});
          l = findCache(txn,
                new Descriptor(fullName, CacheObjectType.AGGR_FUNCTION, 
                        schema, null), false);
        }
      }
      // Get function
      fn = (UserFunction) l.getObj();
    }
    finally
    {
      // Release read lock
      if (l != null)
        release(txn,l);
    }

    return fn;
  }

  /**
   * Get function internal identifier given its name
   * 
   * @param name
   *          Function name
   *          <p>
   *          The function object returned could correspond to either a single
   *          element function or aggregate function
   * 
   * @return function internal identifier
   * @throws MetadataException
   */
  public int getFunctionId(String name, 
		               String schema) throws MetadataException
  {
    UserFunction fn;

    fn = getFunction(name, schema);
    return fn.getId();
  }

  /**
   * Get metadata object for a single element function given its internal
   * identifier
   * 
   * @param id
   *          internal identifier of the function
   * @return the metadata object corresponding to the single element function
   * @throws MetadataException
   */
  public SimpleFunction getSimpleFunction(int id) throws MetadataException {
    
    UserFunction fn = getFunction(id);
    if (fn.getType() == CacheObjectType.SINGLE_FUNCTION)
      return (SimpleFunction)fn;
    else
      throw 
        new MetadataException(MetadataError.INVALID_FUNCTION_IDENTIFIER,
        		              new Object[]{id});
  }
  
  /**
   * Get metadata object for either simple or aggregate function 
   * given its internal identifier
   * 
   * @param id
   *          internal identifier of the desired function
   * @return the metadata object corresponding to the function
   * @throws MetadataException
   */
  public UserFunction getSimpleOrAggFunction(int id) throws MetadataException
  {
     UserFunction fn = null;
    
    fn = getFunction( id);
    
    return fn;
  }

 /**
   * Get metadata object for an aggregate function given its internal identifier
   * 
   * @param id
   *          internal identifier of the desired aggregate function
   * @return the metadata object corresponding to the aggregate function
   * @throws MetadataException
   */
  public AggFunction getAggrFunction( int id) throws MetadataException {
    
    UserFunction fn = getFunction(id);
    if (fn.getType() == CacheObjectType.AGGR_FUNCTION)
      return (AggFunction)fn;
    else
      throw 
        new MetadataException(MetadataError.INVALID_FUNCTION_IDENTIFIER,
        		              new Object[]{id});
  }
  
  // drop Function as part of drop schema
  // here it will remove only function and not function set
  public void dropSchemaFunction(String fname, String schema) 
     throws MetadataException
  {
	ITransaction txn = execContext.getTransaction();
    
    LogLevelManager.trace(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_DELETE, this, fname);
    
    Locks locks = null;
    CacheLock l = null;
    
    UserFunction uf = null;
    uf = getFunction( fname, schema);
    
    //If the function to be dropped does not exist
    if(uf == null)
      throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND,
                                  new Object[]{fname});
    
    if(uf.isBuiltIn())
      throw new MetadataException(MetadataError.CANNOT_DROP_BUILTIN_FUNCTION,
                                  new Object[] {fname});
    
    if(execContext.getDependencyMgr().isAnyDependentPresent
                                      (uf.getId(), DependencyType.QUERY))
      throw new MetadataException(MetadataError.CANNOT_DROP_FUNCTION_QUERY_EXISTS,
                                  new Object[] {fname});
    
    locks = deleteCache(txn, uf.getId());
    if(locks == null)
      throw new MetadataException(MetadataError.INVALID_FUNCTION_IDENTIFIER,
                                  new Object[]{uf.getId()});
 
    l = locks.objLock;
      
    uf = null;
    uf = (UserFunction)l.getObj();
    
    // remove any statistics if maintained by ExecStatManager
    execContext.getExecStatsMgr().removeFuncStats(uf.getId());
     
     
  }

  // drop function set as part of drop schema
  public void dropSchemaFunctionSet(String name, String schema) 
     throws MetadataException
  {
	ITransaction txn = execContext.getTransaction();
    
    LogLevelManager.trace(LogArea.METADATA_USERFUNC, LogEvent.MUSERFUNC_DELETE, this, name);
    
    Locks sfs_locks = null;
    CacheLock set_lock = null;
    
    set_lock = findCache(txn,
          new Descriptor( name, CacheObjectType.SIMPLE_FUNCTION_SET, 
        		 schema, null), true);
    
      if(set_lock != null)
      {
        SimpleFunctionSet sfs = (SimpleFunctionSet) set_lock.getObj();
        int sfs_id = sfs.getId();
        
        sfs_locks = deleteCache(txn, sfs_id);   
        set_lock = sfs_locks.objLock;
      }
    
  }

  
  /**
   * Finds an overloaded function in the list.
   * Returns Datatype array containing data types of overloaded function.
   * @params 
   */
// This function will be called only when exact match is not found.
  // So there should always be some non-zero cost
  public Datatype[] findFunc( SimpleFunctionSet fs, Datatype[] pdts) 
  throws CEPException{
    boolean ismatch = false;
    int minPos  = -1;
    int len     = 0;
    int numMins = 0;
    
    if(fs.getNumOverloads() > cost.length)
       resizeCostArray(fs.getNumOverloads());
    
    for (int pos = 0; pos < fs.getNumOverloads(); pos++) {

      UserFunction fn = getFunction( fs.getFuncId(pos));
      len = fn.getNumParams();

      //number of parameters should be same
      if(len == pdts.length)
      {
        cost[pos]=0;
        for (int parpos = 0; parpos < len; parpos++) {
           Datatype dtcur = fn.getParam(parpos).getType(); 
           if ((pdts[parpos] != dtcur) && (pdts[parpos] != Datatype.UNKNOWN)) 
           {
             cost[pos] += TypeConverter.getTypeConverter().Trans( pdts[parpos], dtcur);
           }
        }
           
        //check whether newly added cost is minimum or not
        if((minPos == -1) || (cost[minPos] > cost[pos]))
        {
          minPos  = pos;
          numMins = 1;
        }
        else if(cost[minPos] == cost[pos])
        {
          numMins++;
        }
      } 
      
    }
    
    //check whether there are any possible conversions or not
    if(minPos != -1)
      ismatch = cost[minPos] < TypeConverter.INFEASIBLE;
    
    if(!ismatch)
      throw new CEPException(SemanticError.WRONG_NUMBER_OR_TYPES_OF_ARGUMENTS , new Object[] {fs.getName()});
    
    //Verify if There are more than one minimum cost
    //if(numMins > 1)
    //  throw new CEPException(SemanticError.TOO_MANY_FUNCS_TYPE_CONVERSION_ERROR, new Object[] {fs.getName()});
    //Instead of throwing an Exception, we return the type with the first  match.(Bug 25386482) 
        
    UserFunction fn  = getFunction(fs.getFuncId(minPos));
    Datatype[] types = new Datatype[fn.getNumParams()];
    
    for(int i=0; i<fn.getNumParams(); i++)
          types[i] = fn.getParam(i).getType();
    
    return types;
  }
  
  private void resizeCostArray(int newSize)
  {
     int[] newCost = new int[newSize];
     int size = cost.length;
     System.arraycopy(cost, 0, newCost, 0, size);
     cost = newCost;
  }
  
  public int getTargetId() {
    return 0;
  }
  
  public String getTargetName() {
    return "UserFunctionManager";
  }
  
  public int getTargetType() {
    return 0;
  }
  
  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    // All levels are handled by the default implementation.
    // MUSERFUNC_INFO - dumps using the fields specified in DumpDesc annotation
    // MUSERFUNC_LOCKINFO - handled by overriden dump method in this class
  }
  
  public void dump(IDumpContext dumper) 
  {
    super.dump(dumper, LogTags.TAG_SINGLEFUNCS, CacheObjectType.SINGLE_FUNCTION);
    super.dump(dumper, LogTags.TAG_SIMPLEFUNCS, CacheObjectType.SIMPLE_FUNCTION_SET);
    super.dump(dumper, LogTags.TAG_AGGRFUNCS, CacheObjectType.AGGR_FUNCTION);
  }
  
  private Class<?> instantiateClass(String className,
                                    int startOffset, 
                                    int endOffset) 
    throws CEPException
  {
    try 
    {
      // Create the implementation class execution object for validation purpose
      
      // First try with own class-loader, as it could be a built-in class.
      // If it fails, then try the Thread's CCL.
      Class<?> cf = Class.forName(className);
      return cf;
    } 
    catch (ClassNotFoundException cnf) 
    {
      try
      {
        Class<?> cf = Class.forName(className, true, 
            Thread.currentThread().getContextClassLoader());
        return cf;
      }
      catch(ClassNotFoundException cnf_inner)
      {          
       throw new
        MetadataException( MetadataError.FUNCTION_IMPL_CLASS_NOT_FOUND, 
          startOffset, endOffset,
          new Object[]{className});
      }
    }
  }
  
} // end of class-body
