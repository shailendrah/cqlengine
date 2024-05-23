/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/ExternalSynopsisImpl.java /main/13 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/31/15 - if error occurs while getting scan at runtime handle
                           it gracefully
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    09/28/12 - passing cause in the error message
    anasrini    12/19/10 - replace eval() with eval(ec)
    sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                           st_pcbpel_11.1.1.4.0
    sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
    sbishnoi    06/22/10 - including threshold params in external synopsis
                           iterator construction
    sbishnoi    03/02/10 - adding setScan
    sbishnoi    10/01/09 - table function support
    sbishnoi    12/03/08 - support for generic data source
    hopark      12/02/08 - move LogLevelManaer to ExecContext
    hopark      06/19/08 - logging refactor
    hopark      12/27/07 - support xmllog
    parujain    11/16/07 - External Synopsis
    parujain    11/16/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/synopses/ExternalSynopsisImpl.java /main/13 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.synopses;

import java.sql.ResultSet;
import java.sql.SQLRecoverableException;
import java.util.Iterator;
import java.util.logging.Level;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.execution.SoftExecException;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.extensibility.datasource.IExternalHasResultSet;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement.*;
import oracle.cep.planmgr.codegen.ConnectionRecoveryContext;
import oracle.cep.planmgr.codegen.datasource.table.TableFunctionPreparedStatement;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.service.ExecContext;
import oracle.cep.memmgr.IPinnable;

import static oracle.cep.extensibility.datasource.IExternalPreparedStatement.NO_OF_EXECUTION;
import static oracle.cep.extensibility.datasource.IExternalPreparedStatement.RUNNING_EXEC_TIME;

/**
 * ExternalSynopsisImpl
 * 
 * @author parujain
 */
@DumpDesc(attribTags={"Id", "PhyId", "StubId"}, 
          attribVals={"getId", "getPhyId", "getStubId"},
          infoLevel=LogLevel.SYNOPSIS_INFO,
          evPinLevel=LogLevel.SYNOPSIS_TUPLE_PINNED,
          evUnpinLevel=LogLevel.SYNOPSIS_TUPLE_UNPINNED,
          dumpLevel=LogLevel.SYNOPSIS_DUMP,
          verboseDumpLevel=LogLevel.SYNOPSIS_DUMPELEMS)
public class ExternalSynopsisImpl extends ExecSynopsis implements
    ExternalSynopsis
{
   IAEval  stmtEval;
   
   /** External Prepared Statement*/
   IExternalPreparedStatement extPreparedStmt;
   
   ResultSet     result;   
   
   TupleSpec     spec;
   
   /** factory - this indicates the type of Tuples to be used in the store */
   protected IAllocator<ITuplePtr> factory; 
   
   protected IAllocator<ITuplePtr> srcFactory;
   
   /** evaluator for the set of predicates supported by external connection */
   private IBEval predEval;
   
   /** flag to check whether the given query is having run away predicate */
   private boolean       isRunAwayPredicate;   

   /** maximum number of external relation rows that can be fetched */
   private long          externalRowsThreshold;
   
   /** name of external data source */
   private String        extSourceName;
   
   /** connection recovery context - used for recovering connection when 
    * there is a failure. */
   private ConnectionRecoveryContext connRecContext = null;
      

   public ExternalSynopsisImpl(ExecContext ec) {
     super(ExecSynopsisType.EXTERNAL, ec);
    
   }
   
   public void setEval(IAEval eval)
   {
     this.stmtEval = eval;
   }
   
   public void setFactory(IAllocator<ITuplePtr> fac)
   {
     this.factory = fac;
   }
   
   public void setSourceFactory(IAllocator<ITuplePtr> srcFactory)
   {
     this.srcFactory = srcFactory;
   }
   
   public void setTupleSpec(TupleSpec tspec)
   {
     this.spec = tspec;
   }
   
   public TupleSpec getTupleSpec()
   {
     return this.spec;
   }
   
    /**
    * Set External Prepared Statement object
    * @param pstmt
    */
   public void setExternalPreparedStatement(IExternalPreparedStatement pstmt)
   {
     extPreparedStmt = pstmt;
   }
   
   public void setConnectionRecoveryContext(ConnectionRecoveryContext connRecCtx)
   {
	 this.connRecContext = connRecCtx;
   }
   
   public TupleIterator getScan(IEvalContext evalCtx) throws ExecException
   {
     ITuplePtr            outputTuple = null;
     java.sql.ResultSet   resultSet   = null;
     Iterator<TupleValue> resultIter = null;
     // When external query results in some SQLRecoverableException then 
     // we would be using the boolean to indicate the query needs to be rerun. 
     boolean extQueryExecuted = false;

     do
     {
	   try
	   {
	     // Evaluate the PreparedStatement to fill up ? signs(if any)
	     if(extPreparedStmt instanceof TableFunctionPreparedStatement)
	     {
	       TableFunctionPreparedStatement tfpstmt = 
	         (TableFunctionPreparedStatement)extPreparedStmt;
	
	       // Allocate a tuple and bind to NEW_OUTPUT_ROLE
	       outputTuple = srcFactory.allocate();
	       evalCtx.bind(outputTuple, IEvalContext.NEW_OUTPUT_ROLE);
	       ITuple outTuple = outputTuple.pinTuple(IPinnable.READ);
	       tfpstmt.setOutputTuple(outTuple);
	       resultIter = tfpstmt.executeQuery(evalCtx);
	     }
             else if(extPreparedStmt instanceof IExternalHasResultSet)
	     {
               if(((IExternalHasResultSet)extPreparedStmt).isClosed())
               {
                 LogUtil.warning(LoggerType.TRACE, "PreparedStatement is already closed! Renewing now.. ");
                 releasePreparedStatement(extPreparedStmt);
                 connRecContext.setEc(this.execContext);
		 extPreparedStmt = connRecContext.renewPrepStmt();
		 LogUtil.warning(LoggerType.TRACE, "Renewed the prepstmt with external source.");
               }
	       stmtEval.eval(evalCtx);
	       long startTime = System.nanoTime();
	       resultIter = extPreparedStmt.executeQuery();
	       long endTime = System.nanoTime();
	       //Time is in nanoseconds
	       long duration = (endTime - startTime);	       
	       if(extPreparedStmt.getStat().get(NO_OF_EXECUTION) !=null) {
	    	   Long execNum =  (Long) extPreparedStmt.getStat().get(NO_OF_EXECUTION);	    	   
	    	   extPreparedStmt.getStat().put(NO_OF_EXECUTION,++execNum);	    	  
	       }else {
	    	   extPreparedStmt.getStat().put(NO_OF_EXECUTION,1L);	    	  
	       }
	       if(extPreparedStmt.getStat().get(RUNNING_EXEC_TIME) !=null) {	    	   	    	  
	    	   Long runningTotal =  (Long) extPreparedStmt.getStat().get(RUNNING_EXEC_TIME);	    	   
	    	   extPreparedStmt.getStat().put(RUNNING_EXEC_TIME,runningTotal + duration);	    	   
	       }else {
	    	   extPreparedStmt.getStat().put(RUNNING_EXEC_TIME,duration);	    	  
	       }
	     }
	     else
	     {
	       long startTime = System.nanoTime();
	       stmtEval.eval(evalCtx);
	       resultIter = extPreparedStmt.executeQuery();
	       long endTime = System.nanoTime();
	       //Time is in nanoseconds
	       long duration = (endTime - startTime);	       	
	       if(extPreparedStmt.getStat().get(NO_OF_EXECUTION) !=null) {	    	      	  
	    	   Long execNum =  (Long) extPreparedStmt.getStat().get(NO_OF_EXECUTION);	    	   
	    	   extPreparedStmt.getStat().put(NO_OF_EXECUTION,++execNum);	    	   
	       }else {
	    	   extPreparedStmt.getStat().put(NO_OF_EXECUTION,1L);	    	   
	       }
	       if(extPreparedStmt.getStat().get(RUNNING_EXEC_TIME) !=null) {	    	  
	    	   Long runningTotal =  (Long) extPreparedStmt.getStat().get(RUNNING_EXEC_TIME);	    	  
	    	   extPreparedStmt.getStat().put(RUNNING_EXEC_TIME,(runningTotal + duration));	    	  
	       }else {
	    	   extPreparedStmt.getStat().put(RUNNING_EXEC_TIME,duration);	    	   
	       }	       
	     }
	     extQueryExecuted = true;
	   }
	   catch(Exception e)
	   {		   
	     if(e instanceof SQLRecoverableException)
	     {
	       LogUtil.warning(LoggerType.TRACE, 
		     "Exception occurred while running external query : "+e.getMessage());
		   
	       extQueryExecuted = false;	
	       
	       //set ExecContext here.
		   connRecContext.setEc(this.execContext);
		   
		   LogUtil.warning(LoggerType.TRACE, "Connection rec context: ");
		   LogUtil.warning(LoggerType.TRACE, "op "+connRecContext.getOp().getOptName());
	       LogUtil.warning(LoggerType.TRACE, "ec "+connRecContext.getEc());
		   LogUtil.warning(LoggerType.TRACE, "pred "+connRecContext.getPred());
		   LogUtil.warning(LoggerType.TRACE, "preparedInstr "+connRecContext.getPreparedInstr());
		   LogUtil.warning(LoggerType.TRACE, "extsource "+connRecContext.getExtSource());
		       
		   //1. First release old preparedstmt and connection
		   releasePreparedStatement(extPreparedStmt);
		   connRecContext.getExtSource().closeConnection();
		   LogUtil.warning(LoggerType.TRACE, "Closed the old connection and PreparedStatement");
		       
		   try 
		   {
             //2. Use the connection recovery context to get a new connection.
		     connRecContext.renewConnection();
		     LogUtil.warning(LoggerType.TRACE, "Renewed the connection with external source.");
		       
             //3. Create prepared statement and have it updated in stmtEval
		     extPreparedStmt = connRecContext.renewPrepStmt();
		     LogUtil.warning(LoggerType.TRACE, "Renewed the prepstmt with external source.");
		   }
		   catch(CEPException ce)
		   {
			 //Don't want to loop around in case of other exceptions
		     extQueryExecuted = true;
			 LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
			 throw new ExecException(ExecutionError.ERROR_RUNNING_EXTERNAL_QUERY, getExtSourceName());
		   }
	     }
	     else
	     {
	       //Don't want to loop around in case of other exceptions
	       extQueryExecuted = true;	
	       LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
	       throw new ExecException(ExecutionError.ERROR_RUNNING_EXTERNAL_QUERY, 
	                               getExtSourceName());
	     }
	   }
     } while(!extQueryExecuted);
     
     // Unpin the output tuple
     if(outputTuple != null)
       outputTuple.unpinTuple();
     
     // resultIter == null means 2 possibility
     // 1) Either extPreparedStmt is a JDBCExternalPreparedStmt OR
     // 2) extPreparedStmt is not a JDBCExtPreparedStmt and returns
     //    empty set of rows
     //TODO better handling of these cases.
     if(resultIter == null)
     {
       if(extPreparedStmt instanceof IExternalHasResultSet)
       {
         resultSet = 
           ((IExternalHasResultSet)extPreparedStmt).getResultSet();
         if(resultSet == null)
           return null;
         else
         {
           return new ExternalSynopsisImplIter(factory, resultSet, spec, 
               predEval, evalCtx, isRunAwayPredicate, externalRowsThreshold,
               getExtSourceName());
         }
       }
       else
         return null;
     }
     else
     {
       return new ExternalSynopsisImplIter(factory, resultIter, spec,
           predEval, evalCtx, isRunAwayPredicate, externalRowsThreshold,
           getExtSourceName());
     }       
   }
   
   private void releasePreparedStatement(
     IExternalPreparedStatement extPreparedStmt)
   {
     try {
       extPreparedStmt.close();
       extPreparedStmt = null;
     }
     catch(Exception ex) {/*Ignore this exception*/}
   }
   
   public void releaseScan(TupleIterator iter) throws ExecException
   {
     iter = null;
     result = null;
   }

   public synchronized void dump(IDumpContext dump) 
   {
     String tag = LogUtil.beginDumpObj(dump, this);
     LogUtil.endDumpObj(dump, tag);
   }   

   /**
    * Set the scan predicate eval
    * @param nsPredEvalIn
    */
   public void setScan(IBEval nsPredEvalIn)
   {
     this.predEval = nsPredEvalIn;
   }
   
   public void setRunAwayPredicate(boolean isRunAwayPredicate)
   {
     this.isRunAwayPredicate = isRunAwayPredicate;
   }

   public void setExternalRowsThreshold(long externalRowsThreshold)
   {
     this.externalRowsThreshold = externalRowsThreshold;
   }

  /**
   * @return the extSourceName
   */
  public String getExtSourceName()
  {
    return extSourceName;
  }

  /**
   * @param extSourceName the extSourceName to set
   */
  public void setExtSourceName(String extSourceName)
  {
    this.extSourceName = extSourceName;
  }
}
