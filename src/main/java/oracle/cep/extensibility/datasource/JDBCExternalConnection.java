/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/JDBCExternalConnection.java /main/8 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/28/11 - support for interval year to month based operations
    sbishnoi    03/31/10 - adding supportsPredicate
    sbishnoi    03/24/10 - adding more capabilities
    sbishnoi    03/05/10 - adding more capabilities
    sbishnoi    02/28/10 - adding capabilities to jdbc external connection
    parujain    03/06/09 - schema.tbl support
    parujain    02/06/09 - bug fix
    sbishnoi    01/02/09 - Creation
 */

package oracle.cep.extensibility.datasource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.*;
import oracle.cep.interfaces.DBHelper;
//import oracle.cep.interfaces.DBHelper;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/JDBCExternalConnection.java /main/7 2010/06/24 06:26:52 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class JDBCExternalConnection implements IExternalConnection
{
  /** java Connection object which is wrapped into JDBCExternalConnection*/
  private java.sql.Connection extConnection;
  
  /** table name for this JDBC connection*/
  private String tableName;
  
  private boolean jdbcPrefMode = false;
  
  private IExternalConverter converter;
  
  /** A Collection of operational capabilities of this connection */
  private static LinkedList<ExternalFunctionMetadata> capabilities = null;
  
  private static ArrayList<Datatype[]> paramDatatypes = null;
  
  static
  {
    initializeCapabilities();
  }
  /**
   * Constructs a Wrapper JDBCExternalConnection Object for java Connection 
   * @param paramExtConnection input java Connection object
   */
  public JDBCExternalConnection(java.sql.Connection paramExtConnection,
                                String tableName)
  {
    extConnection = paramExtConnection;
    this.tableName = tableName;
  }
  
  /**
   * This is to enable performance for jdbc to avoid processing the result set multiple times
   * Constructs a Wrapper JDBCExternalConnection Object for java Connection 
   * @param paramExtConnection input java Connection object
 * @param converter 
   */
  public JDBCExternalConnection(java.sql.Connection paramExtConnection,
                                String tableName,boolean jdbcPrefMode, IExternalConverter converter)
  {
    extConnection = paramExtConnection;
    this.tableName = tableName;
    this.jdbcPrefMode = jdbcPrefMode;
    this.converter = converter;
  }
  
  public java.sql.Connection getJavaSqlConnection()
  {
    return this.extConnection;
  }
  
  /**
   * Creates a IExternalPreparedStatement object for sending SQL statements to
   * the external data source.
   * @param relName name of relation
   * @param pred Predicate Clause
   * @return a new IExternalPreparedStatement object
   */
  public IExternalPreparedStatement prepareStatement(String relName,
	                                              List<String> relAttrs,
                                                    Predicate pred)
    throws java.sql.SQLException
  { 
	
    StringBuilder sql = new StringBuilder();
    sql.append("select ");
    for(int i=0; i<relAttrs.size(); i++)
    {
      if(i > 0)
        sql.append(", ");
      sql.append(relAttrs.get(i));
    }
    // tableName can be either schema.tablename or tablename OR null
    if(tableName != null)
      sql.append(" from " + tableName + " " + relName);
    else
      sql.append(" from " + relName);
    if(pred != null)
     sql.append(" where "+ pred.getPredicateClause()); 
    
    return new JDBCExternalPreparedStatement(
        extConnection.prepareStatement(sql.toString()), 
        sql.toString(),
        jdbcPrefMode,
        converter);
  }
  
  /**
   * Releases all the resources acquired by Connection without any wait.
   */
  public void close() throws java.sql.SQLException
  {
    extConnection.close();
  }

  @Override
  public List<ExternalFunctionMetadata> getCapabilities() throws Exception
  {
    return capabilities; 
  }
  
  /**
   * Prepares a list of operational capabilities for this external connection
   */
  private static void initializeCapabilities()
  {
    capabilities   = new LinkedList<ExternalFunctionMetadata>();
    paramDatatypes = new ArrayList<Datatype[]>();
    
    paramDatatypes.add(new Datatype[]{Datatype.INT, Datatype.INT});
    paramDatatypes.add(new Datatype[]{Datatype.BIGINT, Datatype.BIGINT});
    paramDatatypes.add(new Datatype[]{Datatype.FLOAT, Datatype.FLOAT});
    paramDatatypes.add(new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE});
    paramDatatypes.add(new Datatype[]{Datatype.BYTE, Datatype.BYTE});
    paramDatatypes.add(new Datatype[]{Datatype.BIGDECIMAL, Datatype.BIGDECIMAL});
    paramDatatypes.add(new Datatype[]{Datatype.CHAR, Datatype.CHAR});
    paramDatatypes.add(new Datatype[]{Datatype.BOOLEAN, Datatype.BOOLEAN});
    paramDatatypes.add(new Datatype[]{Datatype.TIMESTAMP, Datatype.TIMESTAMP});
    paramDatatypes.add(new Datatype[]{Datatype.INTERVAL, Datatype.INTERVAL});
    paramDatatypes.add(new Datatype[]{Datatype.INTERVALYM, Datatype.INTERVALYM});
    paramDatatypes.add(new Datatype[]{Datatype.BOOLEAN});
    paramDatatypes.add(new Datatype[]{Datatype.INT});
    paramDatatypes.add(new Datatype[]{Datatype.BIGINT});
    paramDatatypes.add(new Datatype[]{Datatype.FLOAT});
    paramDatatypes.add(new Datatype[]{Datatype.DOUBLE});
    paramDatatypes.add(new Datatype[]{Datatype.BIGDECIMAL});
    paramDatatypes.add(new Datatype[]{Datatype.CHAR});
    paramDatatypes.add(new Datatype[]{Datatype.BYTE});
    paramDatatypes.add(new Datatype[]{Datatype.TIMESTAMP});
    paramDatatypes.add(new Datatype[]{Datatype.INTERVAL});
    paramDatatypes.add(new Datatype[]{Datatype.INTERVALYM});


    // Supported Comparison Operations    
    
    /** Capabilities for Comparison operations */
    addCapabilities(CompOp.EQ.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    addCapabilities(CompOp.NE.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    addCapabilities(CompOp.GT.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    addCapabilities(CompOp.LT.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    addCapabilities(CompOp.LE.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    addCapabilities(CompOp.GE.getFuncName(), new int[]{0,1,2,3,4,5,6,7,8});
    
    /** Capabilities for Arithmetic operations */
    addCapabilities(ArithOp.ADD.getFuncName(), new int[]{0,1,2,3,5});
    addCapabilities(ArithOp.SUB.getFuncName(), new int[]{0,1,2,3,5});
    addCapabilities(ArithOp.DIV.getFuncName(), new int[]{0,1,2,3,5});
    addCapabilities(ArithOp.MUL.getFuncName(), new int[]{0,1,2,3,5});
    
    /** Capabilities for Logical operations */
    addCapabilities(LogicalOp.OR.getFuncName(), new int[]{7});
    addCapabilities(LogicalOp.AND.getFuncName(), new int[]{7});
    addCapabilities(LogicalOp.NOT.getFuncName(), new int[]{10});

    /** Capabilities for Unary operations */
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{11});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{11});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{12});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{12});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{13});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{13});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{14});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{14});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{15});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{15});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{16});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{16});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{17});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{17});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{18});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{18});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{19});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{19});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{20});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{20});
    addCapabilities(UnaryOp.IS_NULL.getSymbol(), new int[]{21});
    addCapabilities(UnaryOp.IS_NOT_NULL.getSymbol(), new int[]{21});
  }  
  
  private static void addCapabilities(String funcName, int[] allowedParamTypes)
  {
    for(int i: allowedParamTypes)
    {
      if(i < paramDatatypes.size())
      {
        Datatype[] paramTypes = paramDatatypes.get(i);
        capabilities.add(new ExternalFunctionMetadata(funcName, paramTypes));
      }
    }
    
  }

	@Override
	public boolean supportsPredicate(Predicate pred) throws Exception {
		return true;
	}

	@Override
	public void validateSchema(int numAttrs,String[] attrNames, AttributeMetadata[] attrMetadata) throws Exception {		
		DBHelper.validateSchema(numAttrs, attrNames, attrMetadata, tableName, getJavaSqlConnection());
	}  
}
