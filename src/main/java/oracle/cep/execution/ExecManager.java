/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/ExecManager.java /main/52 2015/04/23 20:56:23 kmulay Exp $ */

/* Copyright (c) 2006, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares ExecManager in package oracle.cep.execution.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    03/09/16 - add execueDML with iterable
 udeshmuk  05/21/13 - bug 16820093 - add new runoperator method
 vikshukl  12/20/12 - handleFault: separate query names by ,
 alealves  08/04/11 - directInterop does not need notEmpty signal
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/23/10 - propagate hb
 sbishnoi  09/19/10 - XbranchMerge sbishnoi_bug-10068411_ps3 from
                      st_pcbpel_11.1.1.4.0
 sbishnoi  09/03/10 - support input batching
 sbishnoi  02/02/10 - fix NPE bug 9273983
 sbishnoi  02/01/10 - fix NPE bug
 sbishnoi  12/09/09 - output batching support
 parujain  06/01/09 - remove activeOperators
 parujain  05/29/09 - fix ids
 hopark    05/20/09 - fix stale table source
 parujain  05/04/09 - lifecycle management
 parujain  04/16/09 - lists maintenance
 sbishnoi  03/30/09 - adding a hashMap to map tablesource locations to
                      tablesource names
 sborah    03/29/09 - change ArrayLists to LinkedLists
 hopark    03/01/09 - add close
 sborah    02/16/09 - add log for queries stopped
 anasrini  02/10/09 - notify sched on source op drop
 parujain  02/09/09 - execution error
 parujain  01/29/09 - transaction mgmt
 anasrini  01/28/09 - runOperator and handleException
 udeshmuk  01/13/09 - add to the beginning of freeList of opArray
 udeshmuk  12/03/08 - optimize list operations.
 hopark    11/28/08 - use CEPDateFormat
 anasrini  11/10/08 - remove static from locks and condition var
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 parujain  09/23/08 - multiple schema
 udeshmuk  09/17/08 - using toString method of ExecStats in printSched
 udeshmuk  09/17/08 - 
 anasrini  09/12/08 - printSched method - print exec stats
 parujain  05/21/08 - fix diffs
 parujain  05/08/08 - fix drop objects problem
 najain    04/25/08 - add addStore
 sbishnoi  04/22/08 - add printSched
 parujain  04/18/08 - qsrcstats
 hopark    02/25/08 - support paged queue
 hopark    02/05/08 - parameterized error
 udeshmuk  12/17/07 - remove synchronized modifier from insert methods, move it
                      to putNext in Queue source.
 parujain  11/01/07 - unsubscribe output
 parujain  09/26/07 - epr for push source
 mthatte   09/07/07 - Using universal Timestamp format
 mthatte   09/05/07 - Generalizing ElementKind to support TupleKind
 dlenkov   08/07/07 - fixed processElementNode
 najain    07/12/07 - cleanup
 parujain  04/26/07 - add Source Operator iterator
 najain    04/25/07 - add insert with TupleValue
 parujain  04/23/07 - runtime exception handling for multithreaded
 parujain  02/13/07 - addOperator to scheduler
 parujain  02/09/07 - system startup
 parujain  01/12/07 - fix bug
 najain    10/29/06 - insert for Element input
 najain    11/07/06 - bug fix
 najain    10/29/06 - insert for Element input
 najain    10/17/06 - keep a list of queues
 najain    08/29/06 - add pushSources
 najain    08/14/06 - handle silent relations
 najain    07/31/06 - handle silent relations
 najain    05/11/06 - set output driver 
 najain    04/27/06 - add user-defined functions 
 najain    04/06/06 - cleanup
 najain    03/30/06 - add Scheduler 
 skaluska  03/24/06 - implementation
 skaluska  03/23/06 - Creation
 skaluska  03/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/ExecManager.java /main/52 2015/04/23 20:56:23 kmulay Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.execution.operators.ExecOptType;
import oracle.cep.execution.operators.ExecSourceOpt;
import oracle.cep.execution.operators.ExecStats;
import oracle.cep.execution.operators.RelSource;
import oracle.cep.execution.scheduler.Scheduler;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.interfaces.input.QueueSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Destination;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptOutput;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IFaultHandler;
import oracle.cep.service.IServerContext;
import oracle.cep.transaction.ITransaction;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * ExecManager
 * 
 * @author skaluska
 */
public class ExecManager
{
  private ExecContext execContext;
  private AtomicInteger nextExecOptId = new AtomicInteger(0);

  
  /**
   * This variable keeps track of all the operators that encountered
   * a "hard" exception during the current round of processing
   * This list should be cleared before initiating a new run
   *
   * This is a ThreadLocal to support concurrent execution of
   * operators on different threads.
   */
  private class ErrOpt
  {
    ErrOpt(Throwable exception, ExecOpt execOpt)
    {
      this.exception = exception;
      this.execOpt = execOpt;
    }
    
    Throwable exception;
    ExecOpt execOpt;
  }
  
  private ThreadLocal<List<ErrOpt>> opsWithErrors =
    new ThreadLocal<List<ErrOpt>>(){
      @Override protected LinkedList<ErrOpt> initialValue() 
      {
        return new LinkedList<ErrOpt>();
      }
    };
    
  private boolean directInterop;
 
  private static class VersionedTableSource implements IServerContext, Externalizable
  {
    int         id;
    int         version;
    TableSource tblSource;

    // List of other table sources which are having same schema name but 
    // different suffix. These suffix were generated by CQL engine internally
    List<VersionedTableSource> otherTableSources;   
   
    @SuppressWarnings("unused")
    public VersionedTableSource()
    {
    }

    public VersionedTableSource(int id, int version, TableSource tblSource)
    {
      this.id = id;
      this.version = version;
      this.tblSource = tblSource;
      this.otherTableSources = null;
    }

    public VersionedTableSource(VersionedTableSource other)
    {
      this.id = other.id;
      this.version = other.version;
      this.tblSource = other.tblSource;
    }

    public void setId(int id) { this.id = id; }
    public int getId() {return id;}

    public int getVersion() {return version;}
    public void setVersion(int version) {this.version = version;}

    public TableSource getTblSource() {return this.tblSource;}
    public void setTblSource(TableSource src) {this.tblSource = src;}

    public List<VersionedTableSource> getOtherTableSources() 
    {
      return this.otherTableSources;
    }

    public void setOtherTableSources(List<VersionedTableSource> other)
    {
      this.otherTableSources=other;
      if(other != null && other.size()>0)
        setHasMultipleTableSourcesForOneStreamFlag(true);
    }

    private void setHasMultipleTableSourcesForOneStreamFlag(boolean flag)
    {
      // Set the flag for the outer table source
      this.tblSource.setHasMultileTableSourcesForOneStream(flag);

      // Set the flag for other table sources in the composite versioned
      // table source
      Iterator<VersionedTableSource> iter = this.otherTableSources.iterator();
      while(iter.hasNext())
      {
        VersionedTableSource nextObj = iter.next();
        nextObj.getTblSource().setHasMultileTableSourcesForOneStream(flag);
      }
    }
    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
      id = in.readInt();
      version = in.readInt();
      //TODO: Is it sufficient to read the list as an object from the ObjectInput
      //stream ??
      otherTableSources = (List<VersionedTableSource>)in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
      out.writeInt(id);
      out.writeInt(version);
      out.writeObject(otherTableSources); 
    }

    // Print for debug
    public void print() 
    {    
      System.out.println("************************* VersionedTableSource ***********************");
      System.out.println("  Id:" + getId());
      System.out.println("  Version:" + getVersion());
      System.out.println("  TblSource:" + getTblSource() + "\n");
      if(this.otherTableSources != null) {
      Iterator<VersionedTableSource> iter = this.otherTableSources.iterator();
      while(iter.hasNext())
      {
        VersionedTableSource next = iter.next();
        System.out.println("  Id:" + next.getId());
        System.out.println("  Version:" + next.getVersion());
        System.out.println("  TblSource:" + next.getTblSource() + "\n");
      }}
      System.out.println("********************************************");
    }
  };
  
  /**
   * Constructor for ExecManager. The constructor has been kept private
   * intentionally so that no-one can create a new instance of a ExecManager.
   * This way, only a single instance of the ExecManager is present, and it can
   * be accessed globally via ExecManager.getExecManager().
   */
  public ExecManager(ExecContext ec)
  {
    execContext         = ec;
    
    op_array            = new HashMap<Integer,ExecOpt>();
    tbl_array_index     = new HashMap<Integer,Integer>();
    tblname_array_index = new HashMap<TblKey, Integer>();
    alternate_schema_index = new HashMap<String, List<String>>();
    tableSourceArray    = new ArrayList<VersionedTableSource>();
    freeTableSourceArrayIndex = new LinkedList<Integer>();
    sourceOps           = new LinkedList<ExecOpt>();
    op_lock             = new ReentrantLock(true);
    notEmpty            = op_lock.newCondition();
    lock                = new ReentrantReadWriteLock();
    tableSourceVersion  = 0;
    directInterop       = ec.getServiceManager().getConfigMgr().getDirectInterop();
   
  }
  
  public void close()
  {
    // clear the circular references..
    execContext   = null;
    opsWithErrors = null;
  }
  
  public int getNextExecOptId()
  {
    return nextExecOptId.getAndIncrement();
  }
  
  private class TblKey
  {
    private static final long serialVersionUID = 1L;

    String tblName;
    String tblSchema;
    
    TblKey(String name, String sch)
    {
      this.tblName = name;
      this.tblSchema = sch;
    }
    
    public int hashCode()
    {
      return this.toString().hashCode();
    }
    
    public String toString()
    {
      return tblSchema+"."+tblName;
    }
    
    public boolean equals(Object o)
    {
      TblKey other = (TblKey)o;
      
      if(other == null)
      {
        return false;
      }
      
      if(tblName == null)
      {
        if(other.tblName != null)
          return false;
      }
      if(tblSchema == null)
      {
        if(other.tblSchema != null)
          return false;
      }
      if(other.tblName == null)
      {
        if(tblName != null)
          return false;
      }
      if(other.tblSchema == null)
      {
        if(tblSchema != null)
          return false;
      }
      
      return ((this.tblName.equals(other.tblName)) && 
              (this.tblSchema.equals(other.tblSchema)));
    }
  }


  // Currently used only by DirectInterop scheduler
  public void addToErrorOpsList(Throwable exception, ExecOpt execop)
  {
    if (LogUtil.isFineEnabled(LoggerType.TRACE))
    {
      LogUtil.fine(LoggerType.TRACE, "Error in execution operator detected: " + exception.toString());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, exception);
    }
    
    if(opsWithErrors != null)
      opsWithErrors.get().add(new ErrOpt(exception, execop));
  }

  public void runOperator(ExecSourceOpt execop, TupleValue data,
                          boolean overrideTime)
  {
    execContext.getPlanMgr().getLock().readLock().lock();
    if(opsWithErrors != null)
      opsWithErrors.get().clear();
    
    try 
    {
      // Note that there is a race condition with a scheduler like the
      // FIFOScheduler. Thr run of an operator may end up deleting
      // many other operators (due to an exception prompting a stopping
      // of the query).
      // Concurrently, the scheduler could have determined these other 
      // operators for subsequent execution
      // So check that the operator is valid before executing

      if (execop.getValid())
      {
        if((data != null) && overrideTime)
          data.setTime(execop.getSource().getCorrectSystemTime());
        execop.run();
      }
    }
    catch(ExecException e)
    {
      addToErrorOpsList(e, e.op);
    }
    catch(Throwable t)
    {
      addToErrorOpsList(t, execop); // in this case, we add the source operator
    }
    finally 
    {
      execContext.getPlanMgr().getLock().readLock().unlock();
      if (opsWithErrors != null && opsWithErrors.get().size() > 0)
      {
        handleFault(opsWithErrors.get());
      }

      // DO THESE STEPS LAST
      
      //clear the isScheduled flag
      execop.isScheduled.set(false);
      //decrement the counter in SM
      execContext.getSchedMgr().opRunningOrScheduledCnt.decrementAndGet();
    }
  }
  
  public void runOperator(ExecOpt execop)
  {
    execContext.getPlanMgr().getLock().readLock().lock();
    if(opsWithErrors != null)
      opsWithErrors.get().clear();
    
    try 
    {
      // Note that there is a race condition with a scheduler like the
      // FIFOScheduler. Thr run of an operator may end up deleting
      // many other operators (due to an exception prompting a stopping
      // of the query).
      // Concurrently, the scheduler could have determined these other 
      // operators for subsequent execution
      // So check that the operator is valid before executing

      if (execop.getValid())
      {
        execop.run();
      }
    }
    catch(ExecException e)
    {
      addToErrorOpsList(e, e.op);
    }
    catch(Throwable t)
    {
      addToErrorOpsList(t, execop); // in this case, we add the source operator
    }
    finally 
    {
      execContext.getPlanMgr().getLock().readLock().unlock();
      if (opsWithErrors != null && opsWithErrors.get().size() > 0)
      {
        handleFault(opsWithErrors.get());
      }

      // DO THESE STEPS LAST
      
      //clear the isScheduled flag
      execop.isScheduled.set(false);
      //decrement the counter in SM
      execContext.getSchedMgr().opRunningOrScheduledCnt.decrementAndGet();
    }
  }
  
  public void runOperator(ExecSourceOpt execop, TupleValue data)
  {
    // REVIEW we should come up with a new approach for handling changes to the query tree
    execContext.getPlanMgr().getLock().readLock().lock();
    
    if(opsWithErrors != null)
      opsWithErrors.get().clear();
    
    try 
    {
      // Note that there is a race condition with a scheduler like the
      // FIFOScheduler. Thr run of an operator may end up deleting
      // many other operators (due to an exception prompting a stopping
      // of the query).
      // Concurrently, the scheduler could have determined these other 
      // operators for subsequent execution
      // So check that the operator is valid before executing

      if (execop.getValid())
      {        
        execop.run(data);
      }
    }
    catch(ExecException e)
    {
      addToErrorOpsList(e, e.op); // use the offending downstream operator
    }
    catch(Throwable t) 
    {
      addToErrorOpsList(t, execop); // in this case, we add the source operator
    }
    finally 
    {
      execContext.getPlanMgr().getLock().readLock().unlock();
      if (opsWithErrors != null && opsWithErrors.get().size() > 0)
      {
        handleFault(opsWithErrors.get());
      }

      // DO THESE STEPS LAST
      
      //clear the isScheduled flag
      execop.isScheduled.set(false);
      //decrement the counter in SM
      execContext.getSchedMgr().opRunningOrScheduledCnt.decrementAndGet();
    }  
  }
  
  private void handleFault(List<ErrOpt> errOpts)
  {
    IFaultHandler faultHandler =
      execContext.getServiceManager().getConfigMgr().getFaultHandler();
    
    // INVARIANT
    //  - One exception per PhyOpt
    //  (if multiple beans connected to a single channel throw exceptions, then they get condensed into a single
    //   exception in the channel. if multiple beans connected to separate channels throw exceptions, then each
    //   exception is associated to a different output physical operator)
    //  - a PhyOpt may be part of more than one Query, therefore the 'handling' of a single exception by the client
    //   may cause multiple queries to be compensated (e.g. re-started). 
    //  - a Query contains at most one faulty PhyOpt, because processing stops as soon as an exception is raised,
    //   therefore we don't need to make sure all faulty PhyOpts are handled before trying to compensate a Query.
    //  
    
    // Only log if no fault handler is present. If fault handler is present, then user has a chance to log
    //  errors
    Map<Integer, Object> phyOptToCompensate = defaultFaultHandler(errOpts, faultHandler == null);
    
    if (faultHandler != null)
    {
      Set<ExecOpt> execOptToCompensate = new HashSet<ExecOpt>();
      
      for (ErrOpt errOpt : errOpts)
      {
        try 
        {
          faultHandler.handleFault(errOpt.exception, 
              execContext.getServiceName(),
              buildContext(errOpt.execOpt.getPhyOptId(), 
                  phyOptToCompensate.get(errOpt.execOpt.getPhyOptId())));
          
          execOptToCompensate.add(errOpt.execOpt);
        }
        catch (Throwable ut)
        {
          LogUtil.warning(LoggerType.TRACE, "External fault handler failed: " + ut.toString());
          LogUtil.logStackTrace(LoggerType.TRACE, Level.WARNING, ut);
        }
      }
      
      if (execOptToCompensate.size() > 0)
      {
        compensateDefaultFaultHandler(execOptToCompensate, phyOptToCompensate);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private String buildContext(Integer phyOptId, Object context) throws MetadataException
  {
    StringBuilder builder = new StringBuilder();
    builder.append("phyOpt:");
    builder.append(phyOptId);
    builder.append("; queries:");
    
    List<Integer> qIds = null;
    if (context instanceof List)
    {
      qIds = (List<Integer>) context; 
    }
    else 
    {
      int qid = ((PhyOptOutput) context).getQueryId();
      qIds = new ArrayList<Integer>(1);
      qIds.add(qid);
    }
    
    // REVIEW seems awfully a lot of stuff to do just to get the query name...
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);

    try 
    {
      for (int i=0; i < qIds.size(); i++)
      {
        Query query = execContext.getQueryMgr().getQuery(qIds.get(i));
        builder.append(query.getName());

        if (i < qIds.size()-1) // don't put ',' after the last item
          builder.append(',');
      }
      txn.commit(execContext);
    }
    finally 
    {
      execContext.setTransaction(null);
    }
    
    return builder.toString();
  }

  private void compensateDefaultFaultHandler(Set<ExecOpt> execOptToCompensate, 
      Map<Integer, Object> phyOptToCompensate)
  {
    try
    {
      execContext.getPlanMgr().getLock().writeLock().lock();
      
      // Need to restart in the reverse order of the stop.
      ExecOpt[] execOpts = execOptToCompensate.toArray(new ExecOpt[]{});
      for (int i = execOpts.length - 1; i >= 0; i--)
      {
        ITransaction txn = execContext.getTransactionMgr().begin();
        execContext.setTransaction(txn);

        ExecOpt execOpt = execOpts[i];

        try
        {
          int phyid = execOpt.getPhyOptId();

          Object compensateContext = phyOptToCompensate.get(phyid);
          
          if (compensateContext instanceof List)
          {
            @SuppressWarnings("unchecked")
            List<Integer> qidList = (List<Integer>) compensateContext;
            
            Integer[] qidArray = qidList.toArray(new Integer[]{});
            for (int j = qidArray.length - 1; j >= 0; j--)
            {
              int qid = qidArray[j];
              
              //Log the list of queries to be started.
              Query stoppedQuery = execContext.getQueryMgr().getQuery(qid);
              LogUtil.info(LoggerType.TRACE, 
                  "Compensating by trying to start query : "+stoppedQuery.getName());
                if(!stoppedQuery.getName().startsWith(Constants.CQL_RESERVED_PREFIX))
                     execContext.getQueryMgr().startQuery(qid);
            }
          } else 
          {
            PhyOptOutput opt = (PhyOptOutput) compensateContext;
            
            // REVIEW The 'destination' object is still present in the PhyOpt, however
            //  this is somewhat of a side-effect to the fact that we don't clean it when removing...
            Destination dest = opt.getDestination();
            int qid = opt.getQueryId();

            Query query = execContext.getQueryMgr().getQuery(qid);
            
            assert !isKnownDestination(query, dest);
            
            LogUtil.info(LoggerType.TRACE, 
                "Compensating by trying to add destination to query : "+ query.getName());
            
            execContext.getQueryMgr().addQueryDestination(qid, dest);
          }
          
           txn.commit(execContext);
        }
        catch (CEPException e)
        {
          txn.rollback(execContext);
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        }
        finally
        {
          execContext.setTransaction(null);
        }
      }
    } 
    finally 
    {
      execContext.getPlanMgr().getLock().writeLock().unlock();
    }
  }
 
  private boolean isKnownDestination(Query query,
      Destination dest)
  {
    List<Destination> knownDestinations =
      query.getExtDests();

    boolean known = false;
    
    for (Destination existing : knownDestinations)
    {
      // INVARIANT new destinations will always have a different Object reference
      if (dest == existing)
      {
        known = true;
        break;
      }
    }

    return known;
  }

  private Map<Integer, Object> defaultFaultHandler(List<ErrOpt> errOpts, boolean logError)
  {
    ExecOpt           execop;
    Set<Integer> stoppedQueries = new HashSet<Integer>();
    
    Map<Integer, Object> impactedPhyOpts = 
      new HashMap<Integer, Object>(errOpts.size());
    
    // Next set the operator to invalid and potentially stop the query.
    try
    {
      execContext.getPlanMgr().getLock().writeLock().lock();

      Iterator<ErrOpt> errOpsIter = errOpts.iterator();
      ITransaction txn;
      while (errOpsIter.hasNext())
      {
        ErrOpt errOpt = errOpsIter.next();
        List<Integer>     qids = null;
        execop = errOpt.execOpt;
        Throwable optException = errOpt.exception;

        int phyid = execop.getPhyOptId();
        boolean stopQuery = true;

        txn = execContext.getTransactionMgr().begin();
        execContext.setTransaction(txn);

        // First log exception...
        if (logError)
          logCustomerException(optException);
        
        try
        {
          // if output operator then check if it is the case of multiple outputs.
          // If yes, the just drop the output else the query should be stopped.
          PhyOpt opt = execContext.getPlanMgr().getPhyOpt(phyid);
          
          if(execop.getOpttyp() == ExecOptType.EXEC_OUTPUT)
          {
            if(execContext.getPlanMgr().hasQueryMultipleOutputs(phyid))
            {
              // call query manager to drop the output
              execContext.getQueryMgr().dropQueryDestination(opt);
              stopQuery = false;
              
              impactedPhyOpts.put(phyid, opt);
            }
          }
          
          if(stopQuery)
          {
            qids = opt.getQryIds();
        
            stoppedQueries.addAll(qids);

            // We need to make a copy because the list is changed when the queries
            //  are stopped.
            impactedPhyOpts.put(phyid, new ArrayList<Integer>(qids)); 
          }
          
          txn.commit(execContext);
        }
        catch(CEPException e)
        {
          txn.rollback(execContext);
        }
        finally
        {
          execContext.setTransaction(null);
        }
      }

      assert stoppedQueries != null;
      Iterator<Integer> iter = stoppedQueries.iterator();
      while(iter.hasNext())
      {
        txn = execContext.getTransactionMgr().begin();
        execContext.setTransaction(txn);
        try
        {
          int qid = iter.next().intValue();

          //Log the list of queries to be stopped.
          Query stoppedQuery 
          = execContext.getQueryMgr().getQuery(qid);
          LogUtil.info(LoggerType.TRACE, 
              "Trying to stop query : "+stoppedQuery.getName());
          
          execContext.getQueryMgr().stopQuery(qid);
          txn.commit(execContext);
        }
        catch (CEPException e)
        {
          txn.rollback(execContext);
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        }
        finally
        {
          execContext.setTransaction(null);
        }
      }
    }
    finally {
      execContext.getPlanMgr().getLock().writeLock().unlock();
    }
    
    return impactedPhyOpts;
  }

  private void logCustomerException(Throwable currentException)
  {
    // Get the error stack into the string format
    StringWriter stackTrace = new StringWriter();
    PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
    
    currentException.printStackTrace(stackTraceWriter);
    LogUtil.severe(LoggerType.CUSTOMER, "RunTime Exception Stack " + 
                   stackTrace.toString());
  }

  /** scheduler */
  private Scheduler                     sched;

  /* TODO: add output manager */

  /** execution operators sources */
  //this list cannot contain nulls in between so kept as is
  private LinkedList<ExecOpt>           sourceOps;
  
  /** operators array */
  private HashMap<Integer, ExecOpt>     op_array; 

  /** table source info */
  private HashMap<Integer, Integer>     tbl_array_index;
  private HashMap<TblKey, Integer>      tblname_array_index;

  /** A map of application schema to all internally generated schemas */
  private HashMap<String, List<String>> alternate_schema_index;

  private ArrayList<VersionedTableSource>        tableSourceArray;
  private LinkedList<Integer>    freeTableSourceArrayIndex;
  private int                           tableSourceVersion;
  
 // private HashMap<Integer, UserFunc>    userFuncMap;

  /**
   * global lock for exclusion. This lock is used during generation of timeStamp
   * for static relations.
   */
  private ReentrantReadWriteLock lock;

  /** Lock for operators */
  private ReentrantLock      op_lock ;
  
  private Condition          notEmpty;
  
  /**
   * Getter for lock in ExecManager
   * 
   * @return Returns the lock
   */
  public ReentrantReadWriteLock getLock()
  {
    return lock;
  }



  public void addSourceOp(ExecOpt e)
  {
    if(e.getSourcOpsPos()==-1)
    {
      sourceOps.add(e);
      e.setSourceOpsPos(sourceOps.size()-1);
      execContext.getSchedMgr().notifyOnSourceOpAddition(e);
    }
  }

  public void dropSourceOp(ExecOpt e)
  {
    if(e.getSourcOpsPos()!=-1)
    {
      e.deleteOp();
      sourceOps.remove(e);
      e.setSourceOpsPos(-1);
      execContext.getSchedMgr().notifyOnSourceOpDrop(e);
    }
  }
  
  public long getMaxSourceTime()
  {
    long max = 0;
    lock.writeLock().lock();
    Iterator<ExecOpt> iter = sourceOps.iterator();
    while (iter.hasNext())
    {
      ExecOpt eOp = iter.next();
      long eMax = eOp.getMaxTime();
      if (eMax > max)
        max = eMax;
    }
    lock.writeLock().unlock();
    return max;
  }

  public long getMaxSourceTime(LinkedList<RelSource> lst)
  {
    assert !lst.isEmpty();
    long max = 0;

    getLock().writeLock().lock();

    Iterator<RelSource> iter = lst.iterator();
    while (iter.hasNext())
    {
      RelSource rel = iter.next();
      if (max < rel.getMaxTime())
        max = rel.getMaxTime();
    }
    getLock().writeLock().unlock();
    return max;
  }

  /**
   * @return Returns the scheduler
   */
  public Scheduler getSched()
  {
    return sched;
  }

  public void setSched(Scheduler sched)
  {
    this.sched = sched;
  }

  public Iterator<ExecOpt> getExecOpIterator()
  {
    return op_array.values().iterator();
  }
  
  
  public Iterator<ExecOpt> getSourceOpIterator()
  {
    return sourceOps.iterator();
  }
 
  public Collection<ExecOpt> getOperatorArray()
  {
    return op_array.values();
  }
  
  public ReentrantLock getOperatorLock()
  {
    return op_lock;
  }
  
  public Condition getOperatorLockCondition()
  {
    return notEmpty;
  }
  
  /**
   * Add operator
   * 
   * @param e
   *          Operator to be added
   */
  public synchronized boolean addOp(Integer pos, ExecOpt e)
  { 
    if(e.getOpArrayPos() != -1)
      return false;
    
    op_array.put(pos, e);
    e.setOpArrayPos(pos);

    if (!directInterop)
    {
      op_lock.lock();
      notEmpty.signal();
      op_lock.unlock();
    }

    return true;
  }
  
  /**
   * Remove the specified operator
   * 
   * @param e
   *          Operator to be removed
   */
  public synchronized void removeOp(ExecOpt e)
  {
    int loc = e.getOpArrayPos();
    if(loc == -1){
      return;
    } 
    
    // Remove from list
    op_array.remove(loc);      
    // Mark as invalid and remove from the hashmap maintained
    e.setValid(false);
    // If it is Source Operator then also remove from sourceOp list
    dropSourceOp(e); 

    // Call execOp.deleteOp() if not a source op
    // For source ops, dropSourceOp() takes care of this
    if (e.getSourcOpsPos() != -1)
    {
      e.deleteOp();
    }
    e.setOpArrayPos(-1);
  }


  /**
   * Get Already instantiated TableSource for table with specified id
   * No need to instantiate a new one
   * 
   * @param id
   *          Table Id
   * @return TableSource
   * @throws CEPException
   */
  public synchronized TableSource getInstantiatedTableSource(int id) 
    throws CEPException
  {
    Integer loc = tbl_array_index.get(id);
    
    if (loc == null)
      return null;

    return tableSourceArray.get(loc).tblSource;
  }

  /**
   * Get already instantiated TableSource for table with specified tableName
   * and schema name
   * @param tableName parameter table name
   * @param schemaName parameter schema name
   * @return TableSource object corresponding to the parameters
   */
  public synchronized TableSource getInstantiatedTableSource(String tableName,
                                                             String schemaName) 
    
  {
    Integer loc = tblname_array_index.get(new TblKey(tableName, schemaName));

    if (loc == null)
      return null;

    VersionedTableSource v = tableSourceArray.get(loc);
    return v.tblSource;
  }

  /**
   * Get the alternate schemas which are generated internally corresponding
   * to given schema name.
   * @param schemaName name of schema
   * @return list of schema names
   */ 
  public synchronized List<String> getAlternateSchemaNames(String schemaName)
  {
    int len = schemaName.length();
    if(schemaName.charAt(len-1) == '.')
      schemaName = schemaName.substring(0,len-1);
    return alternate_schema_index.get(schemaName);
  }

  /**
   * Get the composite server context which consists of all table sources
   * corresponding to the input list of schema names.
   * @param tableName name of stream or relation
   * @param schemaNames list of schemas
   * @return a composite VersionedTableSource if multiple entries found for the
   *         given schema names;
   *         null if no entries for given schema names
   */ 
  public synchronized IServerContext getLookUpId(String tableName, List<String> schemaNames)
  {
    boolean isFirstSchema = true;
    List<VersionedTableSource> tblSources = new ArrayList<VersionedTableSource>();
    for(String nextSchema : schemaNames)
    {
      Integer loc = tblname_array_index.get(new TblKey(tableName, nextSchema));
      if(loc != null)
      {
        VersionedTableSource next = tableSourceArray.get(loc);
        if(next != null)
          tblSources.add(new VersionedTableSource(next));
      }
    }
    // Return null if there is no corresponding VersionedTableSource found 
    // for given schema names
    if(tblSources.size() == 0)
      return null;

    // Return a single instance of VersionedTableSource if only one schema name
    // matches. 
    if(tblSources.size() == 1)
      return tblSources.get(0);
    
    // Create a composite table source
    VersionedTableSource compositeTableSource 
      = new VersionedTableSource(tblSources.get(0));
    tblSources.remove(0);
    compositeTableSource.setOtherTableSources(tblSources);

    return compositeTableSource;
  }

  public synchronized IServerContext getLookUpId(String tableName, String schemaName)
  {
    Integer loc =  tblname_array_index.get(new TblKey(tableName, schemaName));
    if (loc == null)
      return null;
    VersionedTableSource v = tableSourceArray.get(loc);
    return v;
  }

  /**
   * Get TableSource for table with specified id
   * @param id
   *          Table Id
   * @return TableSource
   * @throws CEPException
   */
  public synchronized TableSource getTableSource(int id) throws CEPException
  { 
    assert execContext.getTransaction() != null;

    Integer loc = tbl_array_index.get(id);
    TableSource s;
  
    // Need to create one
    if (loc == null)
    {
      s = execContext.getInterfaceMgr().getTableSource(id);
      String name     = execContext.getTableMgr().getTable(id).getName();
      String schema   = execContext.getSchema();
      
      loc = -1;
      //if freelist is not empty select free position from free list
      if(!freeTableSourceArrayIndex.isEmpty())
        loc = freeTableSourceArrayIndex.removeFirst();

      tableSourceVersion++;
      if (loc == -1)
      { // Append this TableSource object in the tableSourceArray array list
        VersionedTableSource v = new VersionedTableSource(loc, tableSourceVersion, s); 
        tableSourceArray.add(v);
        loc = tableSourceArray.size() - 1;
        v.setId(loc);
      }
      else
      { // Add this TableSource object at free position in tableSourceArray array list
        tableSourceArray.set(loc, new VersionedTableSource(loc, tableSourceVersion, s)); 
      }
   
      // Update both indices
      tbl_array_index.put(id, loc);
      tblname_array_index.put(new TblKey(name, schema), loc);
      
      // Populate a map from an application schema to internal schemas
      updateAlternateSchemaMap(schema, true);
    }
    else
    {
      s = tableSourceArray.get(loc).tblSource;
    }
    return s;
  }
 
  private synchronized void updateAlternateSchemaMap(String schema, boolean isAdd)
    throws CEPException
  {
      String patternStr = Constants.CQL_RESERVED_PREFIX.substring(0,Constants.CQL_RESERVED_PREFIX.length()-1);
      String pattern = "." + patternStr + "\\W";
      String[] dividedSchema = schema.split(pattern);
      if(dividedSchema.length == 2)
      {
        // Internally generated schema
        String schemaPrefix = dividedSchema[0];
        String schemaSuffix = Constants.CQL_RESERVED_PREFIX + dividedSchema[1];
        List<String> schemaNames = alternate_schema_index.get(schemaPrefix);
        if(!isAdd)
        {
          if(schemaNames != null)
          {
            schemaNames.remove(schema);
            LogUtil.fine(LoggerType.TRACE, 
              "Removing following schema from alternate schema index:" + schema);
          }
        }
        else
        {
          if(schemaNames == null)
          {
            schemaNames = new ArrayList<String>();
            alternate_schema_index.put(schemaPrefix, schemaNames);
            LogUtil.fine(LoggerType.TRACE, 
              "Adding following schema to alternate schema index:" + schema +
              " with prefix key=" + schemaPrefix);
          }
          if(!schemaNames.contains(schema))
          {
            schemaNames.add(schema);
            LogUtil.fine(LoggerType.TRACE, 
              "Adding following schema from alternate schema index:" + schema +
              " with prefix key=" + schemaPrefix);
          }
        }
      }
  } 
  
  public synchronized void removeTableSource(int id) throws CEPException
  {
    assert execContext.getTransaction() != null;
    String name     = execContext.getTableMgr().getTable(id).getName();
    String schema   = execContext.getSchema();

    Integer loc = tbl_array_index.get(id);
    if(loc != null)
    {
      tableSourceArray.set(loc, null);
      freeTableSourceArrayIndex.add(loc);
    }
    tbl_array_index.remove(id);
    tblname_array_index.remove(new TblKey(name, schema));
    updateAlternateSchemaMap(schema, false);
    LogUtil.info(LoggerType.TRACE,"Removing table source id:" + id + " name:" + name + " schema:" + schema + " loc:" + loc);
  }

  /**
   * Get QueryOutput for table with specified id
   * 
   * @param id
   *          Query Id
   * @param epr
   *          destination for the query
   * @return QueryOutput
   * @throws CEPException
   */
  public synchronized QueryOutput getQueryOutput(int id, String epr)
      throws CEPException
  {

    return execContext.getInterfaceMgr().getQueryOutput(id, epr);
  }
  
  /**
   * Get Query metadata object for given query id
   * @param qid Internal Id for query object
   * @return Query metadata
   */
  public synchronized Query getQuery(int qid)
  {
    ITransaction txn = execContext.getTransactionMgr().begin();
    execContext.setTransaction(txn);
    
    Query query = null;
    try 
    {
      query = execContext.getQueryMgr().getQuery(qid);
      txn.commit(execContext);
    } 
    catch (MetadataException e) 
    {
      txn.rollback(execContext);
    }
    finally
    {
      execContext.setTransaction(null);
    }
    return query;
  }
  
  /**
   * Get Query name for given query id
   * @param qid internal id for query object
   * @return name of query as provided by REGISTER QUERY DDL
   */
  public synchronized String getQueryName(int qid)
  {
    Query metadataObj = getQuery(qid);
    if(metadataObj != null)
      return metadataObj.getName();
    else
      return null;
  }
  
  /**
   * Get QueryOutput for table with specified id
   * 
   * @param id
   *          Query Id
   * @param epr
   *          destination for the query
   * @param batchOutput
   *          flag to check if batching required
   * @return QueryOutput
   * @throws CEPException
   */
  public synchronized QueryOutput getQueryOutput(int id, String epr, boolean isBatchOutput)
      throws CEPException
  {

    return execContext.getInterfaceMgr().getQueryOutput(id, epr, isBatchOutput);
  }
  
  /**
   * Get QueryOutput for table with specified id
   * 
   * @param id
   *          Query Id
   * @param epr
   *          destination for the query
   * @param batchOutput
   *          flag to check if batching required
   * @param propagateHb
   *          flag to check if propagation of hb required
   * @return QueryOutput
   * @throws CEPException
   */
  public synchronized QueryOutput getQueryOutput(int id, String epr, 
                                                 boolean isBatchOutput,
						 boolean propagateHb)
      throws CEPException
  {

    return execContext.getInterfaceMgr().getQueryOutput(id, epr, isBatchOutput,
                                                        propagateHb);
  }
  
  /**
   * Remove the Queryoutput for the query with specific epr
   * 
   * @param id
   *          Query id
   * @param epr
   *          destination for the query
   * @throws CEPException
   */
  public synchronized void removeQueryOutput(int id, String epr)
      throws CEPException
  {
    execContext.getInterfaceMgr().removeQueryOutput(id, epr);
  }

  private static String processChildNode(Node child) throws CEPException
  {
    if (child.getNodeType() == Node.TEXT_NODE)
      return processTextNode((Text)child);
    else if (child.getNodeType() == Node.ELEMENT_NODE)
      return processElementNode((Element)child);
    else 
      throw new CEPException(InterfaceError.XML_FORMAT_ERROR);
  }

  private static String processTextNode(Text node)
  {
    return node.getNodeValue();
  }

  private static String processElementNode(Element node) throws CEPException
  {
    String elem = new String("<");
    // elem = elem.concat(node.getTagName());
    elem = elem.concat(node.getLocalName());
    elem = elem.concat(">");
    
    NodeList lst = node.getChildNodes();
    for (int i = 0; i < lst.getLength(); i++)
      elem = elem.concat(processChildNode(lst.item(i)));

    elem = elem.concat("</");
    elem = elem.concat(node.getLocalName());
    elem = elem.concat(">");
    return elem;
  }

  public void insert(String data, String name, String schema) 
    throws CEPException
  {
    TableSource s = getInstantiatedTableSource(name, schema);
    
    insert(data, s, name);
  }

  public void insert(TupleValue elem, String name, String schema) 
    throws CEPException
  {
    TableSource s = getInstantiatedTableSource(name, schema);
    insertBase(null, elem, elem.getObjectName(), s);
  }

  public void insert(TupleValue elem, int id) 
    throws CEPException
  {
    TableSource s = getInstantiatedTableSource(id);
    insertBase(null, elem, elem.getObjectName(), s);
  }
 
  /**
   * Inserts a batch of input tuples
   * @param elemBatch batch of input tuples
   * @param name 
   * @param schema
   */ 
  public void insert(Iterator<TupleValue> elemBatch, String name, String schema) 
    throws CEPException
  {
    TableSource s = getInstantiatedTableSource(name, schema);
    boolean done = false;
    while(!done)
    {
      try
      {
        insertBase(elemBatch, null, name, s);
      }
      catch(CEPException e)
      {
        // In case of a fault, query gets restarted and gets a new queue source.
        // This will make existing queue source as stale source.
        // In that case, we should reinstantiate the queue source for the input.
        // After getting new queue source, we have to process only those input
        // events which has not been processed so far.
        // pendingData is the last tuple which was being processed while this
        // exception was thrown.
        if(e.getErrorCode() == InterfaceError.STALE_QUEUE_SOURCE)
        {
          TupleValue pendingData = ((QueueSource)s).getPendingData(); 
          s = getInstantiatedTableSource(name, schema);
          LogUtil.info(LoggerType.TRACE, "While batch in progress, Reset Queue Source is complete for source=" + name + " schema=" + schema + ". Remaining Batch will be processed by new queue source.");
          ((QueueSource)s).setPendingData(pendingData);
          continue;
        }
        else
          throw e;
      }
      done = true; 
    }
  }


  public void insert(Iterator<TupleValue> elemBatch, int id) 
    throws CEPException
  {
    TableSource s = getInstantiatedTableSource(id);
    // Get object name
    boolean done = false;
    while(!done)
    {
      try
      {
        insertBase(elemBatch, null, null, s);
      }
      catch(CEPException e)
      {
        // In case of a fault, query gets restarted and gets a new queue source.
        // This will make existing queue source as stale source.
        // In that case, we should reinstantiate the queue source for the input.
        // After getting new queue source, we have to process only those input
        // events which has not been processed so far.
        // pendingData is the last tuple which was being processed while this
        // exception was thrown.
        if(e.getErrorCode() == InterfaceError.STALE_QUEUE_SOURCE)
        {
          TupleValue pendingData = ((QueueSource)s).getPendingData(); 
          s = getInstantiatedTableSource(id);
          LogUtil.info(LoggerType.TRACE, "While batch in progress, Reset Queue Source is complete for source-id=" + id + ". Remaining Batch will be processed by new queue source.");
          ((QueueSource)s).setPendingData(pendingData);
          continue;
        }
        else
          throw e;
      }
      done = true; 
    }
  }

  /**
   * Insert given input to table source
   * @param elemBatch batch of input tuples
   * @param elem input element
   * @param objectName name of object
   * @param s input table source
   */
  private void insertBase(Iterator<TupleValue> elemBatch, 
                          TupleValue elem,
                          String objectName,
                          TableSource s)
    throws CEPException
  {
    insertBase(elemBatch, elem, objectName, s, true);
  }

  /**
   * Insert given input to table source
   * @param elemBatch batch of input tuples
   * @param elem input element
   * @param objectName name of object
   * @param s input table source
   * @param overrideTime flag to determine whether timetstamp of element 
   * will be overwritten
   */
  private void insertBase(Iterator<TupleValue> elemBatch, 
                          TupleValue elem,
                          String objectName,
                          TableSource s,
                          boolean overrideTime)
    throws CEPException
  {
    if (s == null || !(s instanceof QueueSource))
    {
      String sourceName = null;
      if(objectName == null)
        sourceName = getObjectName(elemBatch, elem);
      else
        sourceName = objectName;
    
      String elementName = (elemBatch != null) ? "input batch": elem.toString();

      throw new ExecException(ExecutionError.PUSH_SRC_NOT_INITIALIZED,
                              sourceName,
                              elementName);
    }
   
    if(overrideTime)
    { 
      if(elemBatch != null)
        ((QueueSource)s).putNext(elemBatch);
      else
        ((QueueSource)s).putNext(elem);
    }
    else
    {
      if(elemBatch != null)
        ((QueueSource)s).putNext(elemBatch, false);
      else
        ((QueueSource)s).putNext(elem,false);
    }
 
    if (!directInterop)
    {
      op_lock.lock();
      notEmpty.signal();
      op_lock.unlock();
    }
  }
  
  public void insert(Element elem, String name, String schema) throws CEPException
  {
    String data = processElementNode(elem);
    insert(data, name, schema);
  }

  public void insert(Element elem, int id) throws CEPException
  {
    insert(processElementNode(elem), id);
  }

  public void insert(String data, int id) throws CEPException
  {
    TableSource s = getInstantiatedTableSource(id);
    insert(data, s, Integer.toString(id));
    return;
  }
  
  private void insert(String data, TableSource s, String sourceName)
    throws CEPException
  {
    if(s == null || !(s instanceof QueueSource))
      throw new ExecException(ExecutionError.PUSH_SRC_NOT_INITIALIZED,
                              sourceName,
                              data);
    
    ((QueueSource)s).putNext(data);
    op_lock.lock();
    notEmpty.signal();
    op_lock.unlock();

    return;
  }
  
  /**
   * Insert given tuple value into the table source
   * @param elem element which will be inserted
   * @param lookupId location of the pointer to tableSource
   * @throws CEPException
   */
  public void insertFast(TupleValue elem, IServerContext serverContext) 
    throws CEPException
  {
    insertFastBase(elem, null, elem.getObjectName(), serverContext); 
  }

  public void insertFast(Iterator<TupleValue> elemBatch, 
                         IServerContext serverContext)
    throws CEPException
  {
    insertFastBase(null, elemBatch, null, serverContext);
  }

  /**
   * Insert given input tuple/batch of tuples into a table source
   * @param elem input element
   * @param elemBatch batch of input elements
   * @param objectName name of object
   * @param serverContext lookup id
   */
  private void insertFastBase(TupleValue elem,
                             Iterator<TupleValue> elemBatch,
                             String objectName,
                             IServerContext serverContext) throws CEPException
  {
    if (serverContext == null)
    {
      LogUtil.warning(LoggerType.TRACE, "insertFast on uninitialized table source("
        + objectName +")");
      return;
    }
    assert(serverContext instanceof VersionedTableSource);
    VersionedTableSource ctx = (VersionedTableSource) serverContext;

    if(objectName == null)
        objectName = getObjectName(elemBatch, elem);

    // Check and obtain whether there are multiple stream sources registered
    // on this stream.
    List<TableSource> tblSources = hasMultipleTableSources(ctx, objectName);
    
    boolean hasMultipleTableSources = tblSources != null;

    if(hasMultipleTableSources)
    {
      // Disable requests for heartbeat from all queue sources until we push
      // the input event (or batch of events) to all queue sources.
      // After pushing it to all queue sources, we will enable the hbt requests
      // again.
      setRequestForHeartbeat(tblSources, false); 

      Iterator<TableSource> iter = tblSources.iterator();
      boolean isFirstTblSource = true;
      TableSource next = null;
      while(iter.hasNext())
      {
        next = iter.next();
        // In first queue source, we will determine the timestamp of input event
        // or batch of events. This timestamp will be set into TupleValue objects.
        // For remaining queuesources, we will invoke the putNext() with override
        // set to false as timestamp is already set in the events.
        if(isFirstTblSource)
        {
          if(elemBatch != null)
            insertBase(elemBatch, null, objectName, next);
          else
            insertBase(null, elem, objectName, next);
          isFirstTblSource = false;
        }
        else
        {
          if(elemBatch != null)
            insertBase(elemBatch, null, objectName, next, false);
          else
            insertBase(null, elem, objectName, next, false);
        }        

        // Enable the hbt for queue sources one by one
        setRequestForHeartbeat(next, true); 
      }
    }
    else
    {   
      int lookupId = ctx.getId();
      int version = ctx.getVersion();
      VersionedTableSource s = tableSourceArray.get(lookupId);
    
      // Check1: tablesource shouldn't be null;
      // Reason: If the last inserted event has caused a failure and in result of
      //  the failure, cqlengine might have dropped the query; so the tableSource
      //  might have been removed while procssing this tuple.

      // Check2: tablesource versions should match the context version
      // Reason: If last few events have caused a failure and recreation of
      //  prepared statement; It might be possible that newly created table source
      //  is not on the same position in the table source array. 
      //  To prevent the case of referring to wrong table source location, version
      //  will be maintained for each table source object so that events should
      //  point to correct table source
      if (s== null || s.version != version)
      {
        throw new InterfaceException(InterfaceError.STALE_TABLE_SOURCE, objectName);
      }

      if(elemBatch != null)
        insertBase(elemBatch, null, objectName, s.tblSource);  
      else 
        insertBase(null, elem, objectName, s.tblSource);   
    }
  }

  
  private List<TableSource> hasMultipleTableSources(VersionedTableSource ctx, String objectName)
    throws InterfaceException
  {
    int lookupId = ctx.getId();
    int version = ctx.getVersion();
    VersionedTableSource s = tableSourceArray.get(lookupId);

    if (s== null || s.version != version)
    {
      throw new InterfaceException(InterfaceError.STALE_TABLE_SOURCE, objectName);
    }

    List<VersionedTableSource> vts = ctx.getOtherTableSources(); 

    if(vts == null)
    {
      return null;
    }
    else
    {
      List<TableSource> tblSources = new ArrayList<TableSource>();
      tblSources.add(s.tblSource);

      Iterator<VersionedTableSource> vtsIter = vts.iterator();      
      VersionedTableSource nextVTS = null;
      while(vtsIter.hasNext())
      {
        nextVTS = vtsIter.next(); 
        lookupId = nextVTS.getId();
        version = nextVTS.getVersion();
        s = tableSourceArray.get(lookupId);

        if (s== null || s.version != version)
        {
          throw new InterfaceException(InterfaceError.STALE_TABLE_SOURCE, objectName);
        }
        tblSources.add(s.tblSource);
      }
      return tblSources;
    }
  }

  /**
   * Enable or Disable the Request for heartbeat for the given collection of
   * TableSource objects.
   * @param tblSources list of TableSource objects
   * @param flag if true, enable request for heratbeats for given source objects
   *           if false, disable request for heartbeats for given source objects
   */
  private void setRequestForHeartbeat(List<TableSource> tblSources, boolean flag)
  {
    Iterator<TableSource> iter = tblSources.iterator();
    while(iter.hasNext())
    {
      TableSource tblSrc = iter.next(); 
      setRequestForHeartbeat(tblSrc, flag);
    }
  }

  /**
   * Enable or Disable the Request for heartbeat for the given collection of
   * TableSource objects.
   * @param tblSrc TableSource object whose hbtRequest lock is need to be acquired
   * @param flag if true, enable request for heratbeats for given source objects
   *           if false, disable request for heartbeats for given source objects
   */
  private void setRequestForHeartbeat(TableSource tblSrc, boolean flag)
  {
    if(flag)
      tblSrc.getRequestForHeartbeatLock().unlock();
    else
      tblSrc.getRequestForHeartbeatLock().lock();
  }

  public void printSched()
  {
    Iterator<ExecOpt>  itr = getOperatorArray().iterator();
    LogUtil.info(LoggerType.TRACE, "Operator running times : ");
    while (itr.hasNext())
    {
      ExecOpt op = itr.next();
      if (op != null)
      {
        ExecStats stats = op.getStats();
        LogUtil.info(LoggerType.TRACE, op.getOptName() + "," + op.getOpttyp() +
                           "," + stats.toString());
      }
    }
  }

  private String getObjectName(Iterator<TupleValue> elemBatch,
                               TupleValue elem)
  {
    if(elem != null)
      return elem.getObjectName();
    else if(elemBatch != null)
    {
      if(elemBatch.hasNext())
      {
        TupleValue data = elemBatch.next();
        return data.getObjectName();
      }
      else
        return "";
    }
    else
      return "";
  }
}
