/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/CodeGenHelper.java /main/12 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Class containing methods to help in code generation

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/13/12 - improved timestamp support to include timezone
 sbishnoi    10/03/11 - changing format to intervalformat
 udeshmuk    09/15/11 - fix the problem in converting the resultset to tuples
 sbishnoi    08/29/11 - support for interval year to month
 udeshmuk    08/25/11 - first column of the resultset should be event
                        identifier column
 udeshmuk    04/16/11 - archived relation support; adding helper method to get
                        tuple from resultset
 anasrini    03/28/11 - support for reInstantiation
 sborah      10/14/09 - support for bigdecimal
 parujain    03/19/09 - stateless server
 hopark      10/10/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 hopark      03/11/08 - fix xml spill
 najain      05/04/06 - sharing support 
 najain      04/03/06 - cleanup
 najain      03/30/06 - bug fix 
 anasrini    03/24/06 - method to dump the execution objects instantiated 
 anasrini    03/23/06 - bug fix 
 anasrini    03/22/06 - make it public 
 anasrini    03/15/06 - Creation
 anasrini    03/15/06 - Creation
 anasrini    03/15/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/CodeGenHelper.java /main/5 2009/11/09 10:10:59 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptState;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DAGHelper;
import oracle.cep.util.DAGNode;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;

/**
 * Class containing methods to help in code generation
 * 
 * @since 1.0
 */

public class CodeGenHelper
{

  public static ExecOpt instantiate(ExecContext ec, Query query, PhyOpt rootOp)
    throws CEPException
  {
    // Iterate through the physical plan that is a DAG (directed acyclic graph)
    // from sources -sink (ascending topological sorted order).
    // Each operator is visited once
    // Instantiate the operator - This involves
    // Creating the execution layer representation of the operator
    // Creating the required exec layer rep of stores
    // Creating the required exec layer rep of synopsis
    // Linking synopsis and stores
    // Creating the necessary queues (exec layer rep)
    // Linking queues with each other and the source and dest operators

    // The advantage of this traversal is that when a node n is being visited
    // all the nodes from which node n has in-edges have already been
    // visited and all the execution layer structures corresponding to those
    // nodes are already present. This helps in performing the necessary
    // linking also as part of the same single pass throught the DAG.

    assert rootOp != null;

    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(rootOp);
    for (DAGNode op : nodes)
    {
      PhyOpt opt = (PhyOpt) op;
      if (opt.getState() == PhyOptState.REINST)
      {
        OptInst.reInstOp(ec, query, opt);
        opt.setState(PhyOptState.INST);
      }
      else if (opt.getState() != PhyOptState.INST)
      {
        OptInst.instOp(ec, query, opt);
        opt.setState(PhyOptState.INST);
      }

      // do any work that needs to be done
      OptInst.qryProcessOp(ec, query, opt);
    }
    return rootOp.getInstOp();
  }

  public static TupleSpec getTupleSpec(ExecContext ec, PhyOpt op)
    throws CEPException
  {
    TupleSpec ts;
    int numAttrs = op.getNumAttrs();
  
    FactoryManager factoryMgr = ec.getServiceManager().getFactoryManager();
    ts = new TupleSpec(factoryMgr.getNextId());
    for (int i = 0; i < numAttrs; i++)
    {
      if (op.getAttrTypes(i) == Datatype.XMLTYPE)
      {
       ts.addManagedObj(Datatype.XMLTYPE);
      }
      else
      {
        ts.addAttr(op.getAttrMetadata(i));
      }
    }

    return ts;
  }

  /**
   * This method will return a dump of the execution objects that have been
   * instantiated for the input plan
   * 
   * @param rootOp
   *          the physical operator that is an output of a query. In this case,
   *          the plan belongs to this query
   * @return a dump of the execution objects that have been instantiated for the
   *         input
   */
  public static String dump(PhyOpt rootOp) throws CEPException
  {

    StringBuffer sb = new StringBuffer();

    assert rootOp != null;
    ArrayList<DAGNode> nodes = DAGHelper.getTopologicalSort(rootOp);

    sb.append("<ExecutionObjects>");
    for (DAGNode op : nodes)
    {
      ExecOpt execop = ((PhyOpt) op).getInstOp();
      assert execop != null : op.getClass().getName();
      sb.append(execop.toString());
    }
    sb.append("</ExecutionObjects>");

    return sb.toString();
  }
  
  public static List<ITuplePtr> getTuplesFromResultSet(
    ResultSet resultSet,
    TupleSpec tupSpec,
    IAllocator<ITuplePtr> factory,
    String eventIdColName,
    int eventIdColNum,
    boolean eventIdColAdded
    ) throws CEPException
  {
    List<ITuplePtr> outputTuples = new LinkedList<ITuplePtr>();
    
    // Iterate through the result set
    try
    {
      ResultSetMetaData resultMeta = resultSet.getMetaData();
  
      while(resultSet.next())
      {
        ITuplePtr tPtr = (ITuplePtr)factory.allocate(); 
  
        if(tPtr != null)
        {
          ITuple t = tPtr.pinTuple(IPinnable.WRITE);
          
          int idx = 0;
          if(eventIdColAdded)
          {
            assert resultMeta.getColumnName(idx+1).
              equalsIgnoreCase(eventIdColName);
            t.setId(resultSet.getLong(idx+1));
            idx = 1;
          }
          else
          {
            if(eventIdColNum != -1)
            {
              assert resultMeta.getColumnName(eventIdColNum+1).
                equalsIgnoreCase(eventIdColName);
              t.setId(resultSet.getLong(eventIdColNum+1));
            }
            idx = 0;
          }
                   
          for(int i=0; i < resultMeta.getColumnCount() - idx; i++)
          {
            int resultSetIdx = i+idx+1;
            switch(tupSpec.getAttrType(i).getKind())
            {
              case INT: 
                int j = resultSet.getInt(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.iValueSet(i, j);
                break;
  
              case BOOLEAN: 
                boolean bv = resultSet.getBoolean(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.boolValueSet(i, bv);
                break;
  
              case BIGINT: 
                Long l = resultSet.getLong(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.lValueSet(i, l);
                break;
  
              case CHAR: 
                String s = resultSet.getString(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.cValueSet(i, s.toCharArray(), s.length());
                break;
              case FLOAT:
                float f = resultSet.getFloat(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.fValueSet(i, f);
                break;
  
              case DOUBLE: 
                double d = resultSet.getDouble(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.dValueSet(i, d);
                break;
  
              case BIGDECIMAL: 
                BigDecimal bd = resultSet.getBigDecimal(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.nValueSet(i, bd, bd.precision(), bd.scale());
                break;
  
              case BYTE: 
                byte[] b = resultSet.getBytes(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.bValueSet(i, b, b.length);
                break;
  
              case TIMESTAMP: 
                Timestamp ti = resultSet.getTimestamp(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                {
                  t.tValueSet(i, ti);
                  t.tFormatSet(i, 
                              tupSpec.getAttrMetadata(i).getTimestampFormat());
                }
                //Issue with timestamp with TZ
                //It looks like we need to use pstmt.setString for setting timestamp.
                //However, resultSet.getTimestamp() looks fine.
                /*                  
                  String sval = resultSet.getString(i+1);
                    if(resultSet.wasNull())
                        t.setAttrNull(i);
                    else
                    {
                        TimeZone tz = CEPDateFormat.getInstance().getDefaultTimeZone();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d HH.mm.ss.S z");
                        df.setLenient(false);
                        df.setTimeZone(tz);
                      Date date = df.parse(sval);
                      t.tValueSet(i, date.getTime());
                    }
                 */
  
                break;
  
              case INTERVAL: 
                String interval = resultSet.getString(resultSetIdx);
                IntervalFormat format = tupSpec.getAttrMetadata(i).getIntervalFormat();
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.vValueSet(i, 
                   IntervalConverter.parseDToSIntervalString(interval,format),
                   format);                
                break;
                
              case INTERVALYM: 
                String intervalym = resultSet.getString(resultSetIdx);
                IntervalFormat fmt = tupSpec.getAttrMetadata(i).getIntervalFormat();
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.vymValueSet(i, 
                    IntervalConverter.parseYToMIntervalString(intervalym,fmt),
                    fmt);
                break;
  
              case OBJECT: 
                Object object = resultSet.getObject(resultSetIdx);
  
                if(resultSet.wasNull())
                  t.setAttrNull(i);
                else
                  t.oValueSet(i, object);
                break;
  
              default:
            }
          }
          tPtr.unpinTuple();       
          outputTuples.add(tPtr);
        }
      }
    }
    catch(SQLException se)
    {
      outputTuples = null;
      if(se.getCause() != null)
        throw new ExecException(ExecutionError.ARCHIVER_QUERY_RESULTSET_ACCESS_ERROR,
                                se.getCause(),
                                new Object[]{se.getMessage()});
      else
        throw new ExecException(ExecutionError.ARCHIVER_QUERY_RESULTSET_ACCESS_ERROR, 
                                new Object[]{se.getMessage()});
    }
    return outputTuples;
  }
}
