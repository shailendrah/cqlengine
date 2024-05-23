/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionPreparedStatement.java /main/5 2012/02/22 12:06:19 sbishnoi Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/22/12 - fix apple bug
    sbishnoi    08/28/11 - adding support for interval year to month
    anasrini    12/19/10 - replace eval() with eval(ec)
    sbishnoi    10/05/09 - Creation
 */


package oracle.cep.planmgr.codegen.datasource.table;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.BigDecimalAttributeValue;
import oracle.cep.dataStructures.external.BigintAttributeValue;
import oracle.cep.dataStructures.external.BooleanAttributeValue;
import oracle.cep.dataStructures.external.ByteAttributeValue;
import oracle.cep.dataStructures.external.CharAttributeValue;
import oracle.cep.dataStructures.external.DoubleAttributeValue;
import oracle.cep.dataStructures.external.FloatAttributeValue;
import oracle.cep.dataStructures.external.IntAttributeValue;
import oracle.cep.dataStructures.external.IntervalAttributeValue;
import oracle.cep.dataStructures.external.IntervalYMAttributeValue;
import oracle.cep.dataStructures.external.ObjAttributeValue;
import oracle.cep.dataStructures.external.TimestampAttributeValue;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.XmltypeAttributeValue;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.type.IIterableType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/datasource/table/TableFunctionPreparedStatement.java /main/5 2012/02/22 12:06:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


public class TableFunctionPreparedStatement
  implements IExternalPreparedStatement
{
  /** table function evaluator*/
  IAEval prepStmtEval;
  
  /** Output Tuple which will contain one CQL'IIterable Object */
  ITuple outputTuple;
  
  /** Data type of column */
  Datatype returnCollectionDataType;
  
  /** Data type of each value of the iterable object */
  Datatype componentType;
  
  /** alias for table function output column */
  String columnAlias;
  
  /** alias for table function relation*/
  String tableAlias;
  
  /**
   * Constructor
   * @param stmtEval
   * @param relName
   * @param relAttrs
   * @param columnDatatype
   * @param componentType
   */
  public TableFunctionPreparedStatement(IAEval stmtEval, 
                                        String relName, 
                                        List<String> relAttrs, 
                                        Datatype returnCollectionDatatype,
                                        Datatype componentType)
  {
    assert relAttrs.size() == 1;
    this.prepStmtEval   = stmtEval;
    this.tableAlias     = relName;   
    this.columnAlias    = relAttrs.get(0);
    this.returnCollectionDataType = returnCollectionDatatype;
    this.componentType  = componentType;
  }

  public Iterator<TupleValue> executeQuery() throws Exception
  {
    assert false : "TableFunctionPreparedStatement needs an evalContext" ;
    return null;
  }
  
  /**
   * Executes the statement and returns an Iterator over result set
   * @param evalContext evaluation context
   * @return an Iterator over a collection of TupleValue objects
   */
  public Iterator<TupleValue> executeQuery(IEvalContext evalContext) 
    throws Exception
  {
    prepStmtEval.eval(evalContext);
    
    assert outputTuple.getNumAttrs() == 1;
    assert outputTuple.getAttrType(0) == oracle.cep.common.Datatype.OBJECT;
    
    Object outVal = outputTuple.oValueGet(0);
    
    LinkedList<TupleValue> outputList = new LinkedList<TupleValue>();
    
    // Semantic Checks have been already done for this requirement
    assert returnCollectionDataType instanceof IIterableType;
    IIterableType colType = (IIterableType)returnCollectionDataType;
    
    Iterator<Object> objIter = null;
    Iterator iter = null;
    
    try
    {
      if(componentType != null)
        iter = colType.iterator(outVal, componentType);
      else
        objIter = colType.iterator(outVal);
    }
    catch(ClassCastException ce)
    {
      throw new ExecException(
        ExecutionError.TABLE_FUNCTION_OUTPUT_TYPE_MISMATCH, componentType);
    }
    
    if(objIter != null)
    {    
      while(objIter.hasNext())
      {
        Object val = objIter.next();
        
        // If val is null, getTupleValue will handle it in special manner
        TupleValue tupVal = getTupleValue(Datatype.OBJECT, 
                                            columnAlias, 
                                            tableAlias, 
                                            val);  
        outputList.add(tupVal);        
      }
    }
    else if(iter != null)
    {
      Object val = null;
      while(iter.hasNext())
      {
        try
        {
          val = iter.next();
        }      
        catch(ClassCastException ce)
        {
          throw new ExecException(
            ExecutionError.TABLE_FUNCTION_OUTPUT_TYPE_MISMATCH, componentType);
        }
        finally
        {
          if(val != null)
          {
            TupleValue tupVal = getTupleValue(componentType, 
                                              columnAlias, 
                                              tableAlias, 
                                              val);  
            outputList.add(tupVal);
          }
        }
      }
    }
    else
      assert false;
    
    return outputList.iterator();
  }
  
  public void setOutputTuple(ITuple outputTuple)
  {
    this.outputTuple = outputTuple;
  }
  
  public TupleValue getTupleValue(Datatype outAttrType, String outAttrName, 
                                  String outRelationName, Object attrValue)
    throws CEPException
  {
    // Allocate attributes
    AttributeValue[] attrval = new AttributeValue[1];    
    try 
    {
      switch (outAttrType.kind)
      {
        case INT:
          if(attrValue == null)
            attrval[0] = new IntAttributeValue(outAttrName);
          else
            attrval[0] = new IntAttributeValue(outAttrName, ((Integer)attrValue));
          break;
        case BOOLEAN:
          if(attrValue == null)
            attrval[0] = new BooleanAttributeValue(outAttrName);
          else          
            attrval[0] = new BooleanAttributeValue(outAttrName, ((Boolean)attrValue));
          break;
        case BIGINT:
          if(attrValue == null)
            attrval[0] = new BigintAttributeValue(outAttrName);
          else          
            attrval[0] = new BigintAttributeValue(outAttrName, ((Long)attrValue));
          break;
        case FLOAT:
          if(attrValue == null)
            attrval[0] = new FloatAttributeValue(outAttrName);
          else        
            attrval[0] = new FloatAttributeValue(outAttrName, ((Float)attrValue));
          break;
        case DOUBLE:
          if(attrValue == null)
            attrval[0] = new DoubleAttributeValue(outAttrName);
          else 
            attrval[0] = new DoubleAttributeValue(outAttrName, ((Double)attrValue));
          break;
        case BIGDECIMAL:
          if(attrValue == null)
            attrval[0] = new BigDecimalAttributeValue(outAttrName);
          else 
            attrval[0] = new BigDecimalAttributeValue(outAttrName, (BigDecimal)attrValue);
          break;
        case CHAR:
          if(attrValue == null)
            attrval[0] = new CharAttributeValue(outAttrName);
          else 
            attrval[0] = new CharAttributeValue(outAttrName, ((String)attrValue).toCharArray());
          break;
        case XMLTYPE:
          if(attrValue == null)
            attrval[0] = new XmltypeAttributeValue(outAttrName);
          else 
            attrval[0] = new XmltypeAttributeValue(outAttrName, ((String)attrValue).toCharArray());
          break;
        case BYTE:
          if(attrValue == null)
            attrval[0] = new ByteAttributeValue(outAttrName);
          else 
          {
            char[] attrVal = ((String)attrValue).toCharArray();
            attrval[0] = new ByteAttributeValue(outAttrName, Datatype.hexToByte(attrVal, attrVal.length));
          }
          break;
        case TIMESTAMP:
          if(attrValue == null)
            attrval[0] = new TimestampAttributeValue(outAttrName);
          else 
            attrval[0] = new TimestampAttributeValue(outAttrName, ((Long)attrValue));
          break;
        case INTERVAL:
          if(attrValue == null)
            attrval[0] = new IntervalAttributeValue(outAttrName);
          else 
            attrval[0] = new IntervalAttributeValue(outAttrName, ((String)attrValue));
          break;
        case INTERVALYM:
          if(attrValue == null)
            attrval[0] = new IntervalYMAttributeValue(outAttrName, null);
          else 
            attrval[0] = new IntervalYMAttributeValue(outAttrName, ((String)attrValue), null);
          break;
        case OBJECT:
          if(attrValue == null)
            attrval[0] = new ObjAttributeValue(outAttrName);
          else 
            attrval[0] = new ObjAttributeValue(outAttrName, attrValue);
          break;
        default:
          assert false;
        
      }
    }
    catch(ClassCastException cce)
    {
      throw new ExecException(ExecutionError.TYPE_MISMATCH, attrValue.getClass(), outAttrType.typeName);
    }

    return new TupleValue(outRelationName, 0, attrval, false);
    
  }
  
  /**
   * Sets the designated parameter to the given Java array of bytes.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setBytes(int paramIndex, byte[] x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java double value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setDouble(int paramIndex, double x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java float value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setFloat(int paramIndex, float x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java int value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setInt(int paramIndex, int x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java long value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setLong(int paramIndex, long x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java boolean value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setBoolean(int paramIndex, boolean x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java BigDecimal value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setBigDecimal(int paramIndex, BigDecimal x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given Java String value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setString(int paramIndex, String x) throws Exception{}
  
  /**
   * Sets the designated parameter to the given java.sql.Timestamp value.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param x the parameter value
   */
  public void setTimestamp(int paramIndex, Timestamp x) throws Exception{}
  
  /**
   * Sets the designated parameter to Null.
   * @param paramIndex the first parameter is 1, the second is 2, ...
   * @param SQLType the parameter value
   */
  public void setNull(int paramIndex, int SQLType) throws Exception{}
  
  /**
   * Release this IExternalPreparedStatement object's resources immediately
   * without wait.
   */
  public void close() throws Exception{}

  @Override
  public Map<String, Object> getStat()
  { 
	return null;
  }

  @Override
  public boolean execOnEachEvt()
  {
	return false;
  }
}
