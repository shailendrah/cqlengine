/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOpt.java /main/61 2013/10/21 19:47:35 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Physical Operators in the plan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    10/14/13 - don't set dimension flag for operators above view root
 vikshukl    07/02/13 - include Unmesh' comments from previous review
 vikshukl    07/01/13 - move isParentJoinView here, cleanup commented code
 vikshukl    04/17/13 - pass input operator to isDependentOnChildSynAndStore()
 udeshmuk    08/07/12 - view sharing related to archived reln
 vikshukl    08/28/12 - add methods to support query gen in join
 vikshukl    08/07/12 - archived dimension flag
 udeshmuk    07/06/12 - add fields related to buffer operators in the output
 udeshmuk    06/22/12 - remove archiver query logging. done in querymgr now.
 udeshmuk    06/01/12 - add canBeShared flag and use it in determining
                        sharability
 udeshmuk    05/09/12 - fields and methods for identifying the connector
                        operator between local and global plan
 udeshmuk    04/16/12 - getters and setters for workerId and txnId
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 udeshmuk    09/16/11 - replace # by _ in the generated archiver query
 udeshmuk    08/25/11 - propagate event identifier col name for archived rel
 anasrini    07/19/11 - XbranchMerge anasrini_bug-12752107_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    07/15/11 - buffering not required for binary op if any input is
                        external
 udeshmuk    06/19/11 - stabilize State initialization framework
 anasrini    03/28/11 - add mergeIntoGlobalPlan
 udeshmuk    03/26/11 - archived reln - state initialization support
 sbishnoi    04/26/10 - adding requireHbtTimeout flag
 parujain    10/01/09 - dependency map
 hopark      10/06/09 - Fix NPE
 sbishnoi    09/30/09 - table function support
 sborah      07/15/09 - support for bigdecimal
 sborah      06/12/09 - Memory Optimization
 parujain    05/29/09 - maintain Ids in PlanManager
 sbishnoi    05/14/09 - keep a unique set of system timestamped sources
 parujain    05/04/09 - lifecycle mgmt of queues
 anasrini    05/07/09 - system ts lineage
 sborah      04/19/09 - reorganize sharing hash
 sborah      03/20/09 - overload schema and copy methods.
 sborah      03/04/09 - modify getSharingHash()
 sborah      12/19/08 - enable local sharing of ops
 udeshmuk    12/06/08 - initialize id.
 sbishnoi    12/03/08 - support for generic data source; adding generic
                        External connection
 sborah      12/16/08 - handle constants
 sborah      11/27/08 - getSharingHash()
 anasrini    11/07/08 - support for direct interop
 hopark      10/07/08 - use execContext to remove statics
 anasrini    09/16/08 - method createBinarySchema with numLeft and numRight
                        args
 anasrini    09/16/08 - out schema should be based on equivalent logOpt
 udeshmuk    05/27/08 - change getXMLPlan2 to add a list of query ids
 parujain    05/09/08 - fix viewrelnsrc drop
 sbishnoi    04/04/08 - adding isView information into visualizer plan,
 rkomurav    02/28/08 - parameterize error
 hopark      02/07/08 - fix index logging
 parujain    11/26/07 - Connection to connect to external source
 parujain    11/15/07 - external source
 hopark      10/25/07 - add getSynopsis
 sbishnoi    07/12/07 - add isView Flag
 parujain    06/21/07 - shared view during drop problem
 hopark      06/05/07 - add visitor
 parujain    04/17/07 - remove assert
 parujain    04/09/07 - bug fix
 parujain    02/12/07 - addToSched Sched not instantiated
 parujain    02/06/07 - fix delete
 parujain    01/16/07 - remove output for views
 parujain    01/08/07 - fix csfb test case errors
 parujain    12/15/06 - operator sharing
 parujain    12/01/06 - remove Objects from PlanManager
 hopark      11/17/06 - bug 5583899 : removed input/outputs from ExecOpt
 najain      10/17/06 - add getQryIds
 rkomurav    09/11/06 - cleanup for xmldump
 rkomurav    08/22/06 - XML_visualiser
 anasrini    08/24/06 - include id in toString
 anasrini    08/03/06 - Remove getSharingReq
 najain      07/31/06 - silent relations
 najain      06/27/06 - free objects bug 
 najain      06/20/06 - add delete 
 najain      06/19/06 - add list of queryIds 
 najain      06/21/06 - query deletion support 
 najain      06/16/06 - add ref-count 
 najain      05/25/06 - bug fix 
 najain      05/05/06 - sharing support 
 najain      05/04/06 - add state 
 najain      04/03/06 - implementation
 najain      04/06/06 - cleanup
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - cleanup
 skaluska    04/05/06 - implementation
 najain      04/03/06 - cleanup
 najain      03/24/06 - cleanup
 najain      03/22/06 - add constructors etc.
 anasrini    03/22/06 - Implement DAGNode 
 najain      03/20/06 - ad getInputIndex
 anasrini    03/16/06 - use PhyQueue instead of Queue 
 najain      03/15/06 - misc.
 skaluska    03/13/06 - misc.
 najain      02/23/06 - add copy
 skaluska    02/15/06 - cleanup PhyStore/ExecStore 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOpt.java /main/61 2013/10/21 19:47:35 vikshukl Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.phyplan;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.OrderingKind;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalDataSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.memmgr.ObjectFactoryContext;
import oracle.cep.memmgr.ObjectManager;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryDeletionContext;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.planmgr.IPlanVisitable;
import oracle.cep.planmgr.IPlanVisitor;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;
import oracle.cep.util.DAGNode;

/**
 * Physical Operator Class Definition
 */
public abstract class PhyOpt implements DAGNode, IPlanVisitable
{
  protected ExecContext         execContext;
  
  /** phyopt id */
  private int                   id = -1;
  
  /** state */
  private PhyOptState           state;

  /** Type of operator */
  private PhyOptKind            operatorKind;

  
  /** Number of attributes in the output schema of the operator */
  private int                   numAttrs;

  /** The hash value of the operator used to compute whether
  it can be shared or not
  */
  private Integer               sharingHash;
  
  /** Metadata of the attributes in the output schema */
  private AttributeMetadata[]   attrMetadata;

  /** Does the operator produce a stream */
  private boolean               isStream;

  /** Operators reading off from this operator */
  private BitSet                activeOutputs;

  private ArrayList<PhyOpt>     outputs;

  /** number of output operators */
  private int                   numOutputs;

  /** Operators which form the input to this operator */
  private PhyOpt[]              inputs;

  /** number of input operators */
  private int                   numInputs;

  /** Stores that allocate tuples in various inputs */
  private PhyStore[]            inStores;

  /** Store for the allocation of space for the output tuples */
  private PhyStore              store;

  /** Synopses */ 
  private PhySynopsis[]         synopses;
  
  /** Input queues for each input */
  private PhyQueue[]            inQueues;

  /** The (single) output queue */
  private PhySharedQueueWriter  outQueue;

  /** Instantiated operator */
  private ExecOpt               instOp;
  
  /** This is used when we find out the globalEquivalent of operator */
  /** Generally it will be null, only non-null when finding operator 
      which can performs the same functionality */
  private PhyOpt                globalEquiv;

  /** Stores all the outputs of the operator */
  private HashMap<Integer,List<PhyOpt>> 
                                outputsIndex;
  
  /** list of queryIds */
  LinkedList<Integer>           qryIds;

  LinkedList<Object>            allObjs; 
  
  /** list of source op ids i.e. view, reln or strm */
  LinkedList<Integer>           srcIds;

  private PhyOptOutputIter      iter;

  /* does the operator only depend on silent Relations */
  boolean                       silentRelns;
 
  // If it is the Source operator like RelSource, ViewRelnSrc
  boolean                       isSource;
  
  /* given operator is a root operator of a query which generates a view */
  boolean                       isView;

  /* whether given operator is part of the global plan operators index */
  private boolean               isGlobalPlanOp;

  // The list of silent Relations that the operator depends on: This is needed
  // to propagate the heartbeat in case of a stall or a silent relation.
  // Currently, silent streams/relations are not handled, only static relations
  // (one for which the time is not specifed) and handled appropriately.
  LinkedList<PhyOpt>            inputRelns;

  /**
   * The set of system timestamped base (not view) sources 
   * (streams and non-external relations that are part of the lineage of this
   * operator.
   *
   * This is maintained so that in the case of binary operators, when one
   * input (say outer) is waiting for progress of time on the 
   * other side (inner), then a request is made (via the scheduler) to the 
   * system timestamped sources in the lineage of the inner input to send
   * "as soon as possible" a heartbeat or an event at the desired time 
   * (time of waiting outer input + 1)
   *
   * Currrently this optimization works only in the DirectInterop mode
   */
  protected Set<PhyOpt>         systsSourceLineage;

  /**
   * For binary operators, this is systsSourceLineage of the left (outer)
   * input.
   */
  protected Set<PhyOpt>         outerSystsSourceLineage;

  /**
   * For binary operators, this is systsSourceLineage of the right (inner)
   * input.
   */
  protected Set<PhyOpt>         innerSystsSourceLineage;
  
  /**
   * For binary operators, this is complete lineage of the left and right
   * inputs
   */
  protected Set<PhyOpt>         fullSourceLineage;

  
   /** 
    * Is there an external pull entity (relation or table function)
    * in the child tree of this operator (including this operator)
    */
  boolean                       isExternal;
  

  /**
   * whether one of the input to this operator is based on archived
   * dimension
   */
  boolean                       isArchivedDim;


  /**
   * Is this a pull operator
   */
  boolean                       isPullOperator;

  /** Connection to external source like database */
  // Today external connection is only relevant for external relations
  // We are keeping in PhyOpt instead of PhyOptRelnSrc so that we can
  // use it when there is a condition on relation source. Today we are 
  // not pushing down this condition but tomorrow we might want to do.
  IExternalConnection          extConnection;
  
  IExternalDataSource          extDataSource;
 
  /** a flag to check if this operator needs heartbeat timeout */ 
  private boolean              isHbtTimeoutRequired;

  /** flag determines if ordering constraints on operator */
  private OrderingKind         orderingConstraint = OrderingKind.TOTAL_ORDER;
  
  /** indicates whether state initialization processing is done */
  private boolean              stateInitializationDone = true;
  
  /** Used in ArchiverQuery based approach of archiver query formation */
  protected String             archiverSQL = null;
  
  /** ArchiverQuery in object form */
  protected ArchiverQuery      archiverQuery = null;
  
  /**
   * Used in subquery based approach of constructing the archiver query 
   */
  protected String             outputSQL = null;
  
  /** 
   * Used in subquery based approach of constructing the archiver query.
   * Name of the archiver corresponding to the archived relation , if any,
   * which is present below this operator in the query plan.
   */
  private String               archiverName = null;
  
  /**
   * Column name of event identifier column.
   * Used in archived relation only.
   */
  private String               eventIdColName = null;
  
  /**
   * Column number of event identifier column.
   * Used in archived relation only.
   */
  private int                  eventIdColNum = -1; 
  
  /**
   * Column name of worker identifier column.
   * Used in archived relation only.
   */
  private String               workerIdColName = null;
  
  /**
   * Column number of worker identifier column.
   * Used in archived relation only.
   */
  private int                  workerIdColNum = -1; 
  
  /**
   * Column name of txn identifier column.
   * Used in archived relation only.
   */
  private String               txnIdColName = null;
  
  /**
   * Column number of txn identifier column.
   * Used in archived relation only.
   */
  private int                  txnIdColNum = -1; 
  
  /**
   * Boolean to indicate if event id column has been added to 
   * the project clause of outputSQL of this query. If this is 
   * TRUE then it would be the first in the project list.
   */
  private boolean              eventIdColAddedToProjClause = false;
  
  /** 
   * Used in subquery based approach of constructing the archiver query.
   * To indicate if the outputSQL should be executed against the archiver.
   */
  protected   boolean          isQueryOperator = false;
  
  /** 
   * boolean to indicate if left side queue acts as connector between
   * global plan and local plan. For unary operator only this variable
   * is useful.
   */
  private boolean              isLHSConnector = false;
  
  /**
   * boolean to indicate if right side queue acts as connector between
   * global plan local plan. For unary operator this would never be 
   * used.
   */
  private boolean              isRHSConnector = false;

  /**
   * true if this operator can be connector between the global plan and 
   * local plan
   */
  private boolean canBeConnectorOperator = true;
  
  /** 
   * true if this operator can participte in sharing algorithm.
   * An operator in a query dependent on archived relation/stream can
   * participate in sharing algorithm only if it is below query operator
   * in the query plan. Only source operators are exception to this rule.
   * i.e. they can be shared even if they are query operators 
   */
  private boolean canBeShared = true;
  
  /**
   * Project clause entries (applicable only for archiver query).
   * This is needed because at various places we have to use project clause  
   * of child operator SQL.
   */
  protected List<String> projEntries = null;
  
  /**
   * Datatype of project entries in the archiver query 
   */
  protected List<Datatype> projTypes = null;

  /**
   * List containing the outputs that expect this operator to maintain 
   * a buffer (used only in archived relation setup).
   */
  private List<PhyOpt> outputsExpectingBuffer = null;
  
  /**
   * This is related to view sharing that are based on archived relation/stream
   * If true then this operator won't be considered while finding the query
   * operator since it is either below view root or it is a view root.
   */
  private boolean isBelowViewRootInclusive = true;
  
  public List<String> getArchiverProjEntries()
  {
    return projEntries;
  }
  
  public List<Datatype> getArchiverProjTypes()
  {
    return projTypes;
  }
  
  public void setArchiverProjEntries(List<String> projEntries)
  {
    this.projEntries = projEntries;
  }
  
  public void setArchiverProjTypes(List<Datatype> projTypes)
  {
    this.projTypes = projTypes;
  }
  
  public int getProjectClauseLength()
  {
    if(isQueryOperator)
    {
      return projEntries.size();
    }
    return -1;
  }

  public int getProjectClauseStartIdx()
  {
    if(isQueryOperator)
    {
      int index = outputSQL.indexOf("select ");
      return index+"select ".length();
    }
    return -1;
  }
  
  public int getProjectClauseEndIdx()
  {
    if(isQueryOperator)
    {
      int index = outputSQL.indexOf(" from");
      return index + 1;
    }
    return -1;
  }
  
  public int getNumParamsForArchiver()
  {
    if(isQueryOperator)
    {
      int startIdx = 0;
      int count = 0;
      while(startIdx < outputSQL.length())
      {
        startIdx = outputSQL.indexOf('?', startIdx);
        if(startIdx  == -1)
          break;
        else
        {
          count++;
          startIdx++;
        }
      }
      return count;
    }
    return -1;
  }
  
  /**
   * Set the flag that will enable querying state from archiver.
   */
  public void setIsQueryOperator(boolean val)
  {
    this.isQueryOperator = val;
    if(val)
    {
      if(this.outputSQL != null)
        this.outputSQL = this.outputSQL.replace(" $ ", " FROM ");
    }
  } 
 
  /**
   * returns true if this opt is identified as query operator
   * false otherwise.
   */
  public boolean isQueryOperator()
  {
    return isQueryOperator;
  }

  /**
   * setter for archiver name
   * @param archName archiver name
   */
  public void setArchiverName(String archName)
  {
    this.archiverName = archName;  
  }
  
  /**
   * This method checks whether one of the parents of this operator a join or 
   * a joinproject operator, and is also view root. 
   * 
   * This method is very important for special join processing during
   * state initialization as certain things are done only for regular joins and
   * not for special joins (like allocating buffer operators, query operators,
   * allocating synposis on the fact side and allocating the output synopsis
   * for the special join)
   * 
   * @param opt given operator (most likely source, buffer and select)
   * @return true if the above conditions are true
   * 
   */
  public boolean isParentJoinView(PhyOpt opt) 
  {    
    if ((opt instanceof PhyOptJoin) ||
        (opt instanceof PhyOptJoinProject))
    {
      if (opt.getIsView() && opt.isArchivedDim()) 
      {
        return true;
      }
    }
    
    // recurse on all output operators 
    PhyOptOutputIter iter = opt.getOutputsIter();
    boolean joinviewroot = false;
    for (int i=0; i < opt.getNumOutputs(); i++)
    {
      PhyOpt op;
      try 
      {
        op = iter.getNext();
      } 
      catch (PhysicalPlanException e) 
      {
        return false;
      }
      
      joinviewroot = isParentJoinView(op);
      
      if (joinviewroot)
        return true;
    }     
    return false; 
  }
  
  
  
  /**
   * getter for archiver name
   * @return archiver name
   */
  public String getArchiverName()
  {
    return this.archiverName;
  }
  
  public String getEventIdColName()
  {
    return this.eventIdColName;  
  }
  
  public void setEventIdColName(String nm)
  {
    this.eventIdColName = nm;
  }
  
  public int getEventIdColNum()
  {
    return this.eventIdColNum;
  }
  
  public void setEventIdColNum(int colNum)
  {
    this.eventIdColNum = colNum;
  }
  
  public String getWorkerIdColName()
  {
    return this.workerIdColName;  
  }
  
  public void setWorkerIdColName(String nm)
  {
    this.workerIdColName = nm;
  }
  
  public int getWorkerIdColNum()
  {
    return this.workerIdColNum;
  }
  
  public void setWorkerIdColNum(int colNum)
  {
    this.workerIdColNum = colNum;
  }
  
  public String getTxnIdColName()
  {
    return this.txnIdColName;  
  }
  
  public void setTxnIdColName(String nm)
  {
    this.txnIdColName = nm;
  }
  
  public int getTxnIdColNum()
  {
    return this.txnIdColNum;
  }
  
  public void setTxnIdColNum(int colNum)
  {
    this.txnIdColNum = colNum;
  }
  
  public boolean isEventIdColAddedToProjClause()
  {
    return this.eventIdColAddedToProjClause;
  }
  
  public void setEventIdColAddedToProjClause(boolean val)
  {
    this.eventIdColAddedToProjClause = val;
  }

  /**
   * Sets the output sql
   */
  public void setOutputSQL(String sql)
  {
    if(sql != null)
      this.outputSQL = sql.replace('#', '_');
    else
      this.outputSQL = null;
  }

  /**
   * The method returns the SQL that represents the output of this phyopt.
   */
  public String getOutputSQL()
  {
    return this.outputSQL;
  }
  
  /**
   * Constructs the queries (to be issued against the archiver) 
   * used to initialize the state.
   */
  public void constructState()
  {
    this.archiverSQL = archiverQuery.toString();
  }
  
  /**
   * Populates the archiver query field with 
   * - details from the archiverQuery objects in children
   * - own details
   */
  public void updateArchiverQuery() throws CEPException
  {
    return; //default implementation
  }
  
  /**
   * Populates archiver query details from children
   */
  protected void getArchiverQueryDetailsFromChildren()
  {
    this.archiverQuery = new ArchiverQuery();
    PhyOpt[] children = this.getInputs();
    if(numInputs > 0)
    {
      for(int i=0; i < children.length; i++)
        this.archiverQuery.addDetails(children[i].getArchiverQuery());  
    }
  }
  
  /**
   * Get the archiverQuery object
   */
  public ArchiverQuery getArchiverQuery()
  {
    return this.archiverQuery;
  }
  
  /**
   * Get archiver query string
   * @return string representing the query to be sent to archiver,
   *         null if not initialized
   */
  public String getArchiverQueryString()
  {
    return this.archiverSQL;
  }
  
  /**
   * @return true if the state initialization processing is already done
   *         false otherwise
   */
  public boolean isStateInitializationDone()
  {
    return stateInitializationDone;
  }
  
  /**
   * @param val - value indicating whether state initialization has been done
   */
  public void setStateInitializationDone(boolean val)
  {
    this.stateInitializationDone = val;
  }
  
  /**
   * @return true if the state can be constructed by querying archiver
   */
  public boolean canBeQueryOperator() throws CEPException
  {
    //default is false;
    return false;
  }
  
  /**
   * @return true if the operator can construct its SQL
   * Besides other things, the methods overriding the default implementation
   * should ensure that they check that the children operator SQL is not
   * null. If it is null then method should return false immediately.
   */
  public boolean canConstructQuery(Query q) throws CEPException
  {
    //default is false
    return false;
  }
  
  /**
   * @return true if the operator maintains state
   */
  public boolean isStateFul()
  {
    //default is false, operators that do maintain state will override this.
    return false;
  }
  
  /**
   * Returns true if this operator needs heartbeat timeout  
   * @return
   */
  public boolean isHbtTimeoutRequired()
  {
    return isHbtTimeoutRequired;
  }

  /**
   * Set whether thisoeprator needs heartbeat timeout
   * @param isHbtTimeoutRequired
   */
  public void setHbtTimeoutRequired(boolean isHbtTimeoutRequired)
  {
    this.isHbtTimeoutRequired = isHbtTimeoutRequired;
  }

  public IExternalConnection getExtConnection()
  {
    return extConnection;
  }
  
  public void setExtConnection(IExternalConnection paramExtConnection)
  {
    extConnection = paramExtConnection;
  }  

  public boolean isSilentRelnDep()
  {
    return silentRelns; 
  }

  public void setSilentRelnDep()
  {
    silentRelns = true;
  }

  public boolean isExternal() 
  {
    return isExternal;
  }
  
  public boolean isArchivedDim() 
  {
    return isArchivedDim;
  }

  public void setArchivedDim(boolean isArchivedDim) 
  {
    this.isArchivedDim = isArchivedDim;
  }

  public boolean isPullOperator() 
  {
    return isPullOperator;
  }

  public boolean getIsGlobalPlanOp()
  {
    return this.isGlobalPlanOp;
  } 

  public void setIsGlobalPlanOp(boolean isGlobalPlanOp)
  {
    this.isGlobalPlanOp = isGlobalPlanOp;
  }

  public void setExternal(boolean isExternal) 
  {
    this.isExternal = isExternal;
  }

  public void setPullOperator(boolean isPullOperator) 
  {
    this.isPullOperator = isPullOperator;
  }

  public LinkedList<PhyOpt> getSilentRelnDep()
  {
    return inputRelns;
  }

  protected HashMap<Integer,List<PhyOpt>> getOutputsIndex()
  {
    return this.outputsIndex;
  }
  
  public void addSilentRelnDep(LinkedList<PhyOpt> input)
  {
    if (inputRelns == null)
      inputRelns = new LinkedList<PhyOpt>();

    inputRelns.addAll(input);
  }

  public void addSilentRelnDep(PhyOpt input)
  {
    if (inputRelns == null)
      inputRelns = new LinkedList<PhyOpt>();

    inputRelns.add(input);
  }

  public Set<PhyOpt> getSystsSourceLineage()
  {
    return systsSourceLineage;
  }

  public Set<PhyOpt> getOuterSystsSourceLineage()
  {
    return outerSystsSourceLineage;
  }

  public Set<PhyOpt> getInnerSystsSourceLineage()
  {
    return innerSystsSourceLineage;
  }
  
  public Set<PhyOpt> getFullSourceLineage()
  {
    return fullSourceLineage;
  }

  private void setDefaults()
  {
    numAttrs       = 0;
    attrMetadata   = null;
    isStream       = false;
    outputs        = null;
    numOutputs     = 0;
    inputs         = null;
    numInputs      = 0;
    inStores       = null;
    store          = null;
    inQueues       = null;
    outQueue       = null;
    instOp         = null;
    state          = PhyOptState.INIT;
    qryIds         = new LinkedList<Integer>();
    srcIds         = new LinkedList<Integer>();
    allObjs        = new LinkedList<Object>();
    activeOutputs  = new BitSet();
    iter           = new PhyOptOutputIter(this);
    silentRelns    = false;
    globalEquiv    = null;
    isSource       = false;
    isView         = false;
    synopses       = null;
    isExternal     = false;
    isArchivedDim  = false;
    isPullOperator = false;
    extConnection  = null;
    sharingHash    = null;
    outputsIndex   = new HashMap<Integer,List<PhyOpt>>();
    isGlobalPlanOp = false;
    stateInitializationDone = true;

    // Related to system timestamped source lineage
    systsSourceLineage      = null; 
    outerSystsSourceLineage = null; 
    innerSystsSourceLineage = null; 
    fullSourceLineage       = null; 
    // set default value to false
    isHbtTimeoutRequired    = false;
   }

  protected PhyOpt(ExecContext ec, PhyOptKind operatorKind)
  {
    this.execContext  = ec;
    this.operatorKind = operatorKind;
    this.id           = ec.getPlanMgr().getNextPhyOptId();
    
    execContext.getPlanMgr().addPhyOpt(this);
    setDefaults();
  }
  
  public PhyOpt getGlobalEquiv()
  {
    return globalEquiv;
  }
  
  public void setGlobalEquiv(PhyOpt globalEquiv)
  {
    this.globalEquiv = globalEquiv;
  }

  /**
   * Constructor for physical operators that have a single input and whose
   * schema matches that of its input
   * 
   * @param operatorKind
   *          the type of the operator
   * @param input
   *          the single input to this operator
   * @param logopt 
   *          the logical operator equivalent to this physical opt
   * @param sameSchema
   *          true iff this has the same schema as its input
   * @param sameStream
   *          true iff the nature of output (stream or relation) of this
   *          operator exactly matches its input
   */
  protected PhyOpt(ExecContext ec, PhyOptKind operatorKind, PhyOpt input, 
                   LogOpt logopt,
                   boolean sameSchema, boolean sameStream) 
    throws PhysicalPlanException
  {

    this(ec, operatorKind);

    if (sameSchema)
    {
      // output schema = input schema
      if (logopt == null)
        copy(input);
      else
        copy(input, logopt);
    }

    if (sameStream)
    {
      // output is a stream iff input is a stream
      setIsStream(input.getIsStream());
    }
    
    // is operator's input (and hence this operator) dependent on archived dim?
    
    // set dimension flag only for operators view root and below.
    // isView is set only for view root and not for other operators above
    // view root, but since the first operators above view root will have
    // false, others will have it set to false too.
    setArchivedDim(!input.getIsView() && input.isArchivedDim());
    
    // Setup inputs and outputs
    setNumInputs(1);
    getInputs()[0] = input;
    input.addOutput(this);
  }

  /**
   * Constructor for physical operators that have a single input and whose
   * schema matches that of its input
   * 
   * @param operatorKind
   *          the type of the operator
   * @param input
   *          the single input to this operator
   * @param sameSchema
   *          true iff this has the same schema as its input
   * @param sameStream
   *          true iff the nature of output (stream or relation) of this
   *          operator exactly matches its input
   */
  protected PhyOpt(ExecContext ec, PhyOptKind operatorKind, PhyOpt input, boolean sameSchema, 
                   boolean sameStream) 
    throws PhysicalPlanException
  {
    this(ec, operatorKind, input, null, sameSchema, sameStream);
  }

  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    return false;
  }
  
  /**
   * Checks whether global plan operator can be shared or not
   * If Both are completely equivalent then set globalEquivalent
   * to the global plan operator
   * 
   * @param opt
   *          Operator which will be shared with this operator
   * @return
   *       whether shareable or not
   */
  public boolean isCompleteEquiv(PhyOpt opt)
  {
    //At this point numInputs should be same and the types should also match.
    //The check for equivalence of operatorkind encompasses both requirements.
    if(opt.getOperatorKind() == this.getOperatorKind())
    {
      if (opt.getOrderingConstraint() != this.getOrderingConstraint())
        return false;
      
      for(int i = 0; i < numInputs; i++)
      {
        if(inputs[i].globalEquiv == null)
          return false;
      
        if(inputs[i].globalEquiv != opt.inputs[i])
          return false;
      }
      
      this.globalEquiv = opt;
      
      return true;
    }
    else 
      return false;
  }

  /**
   * This method is invoked when a local plan operator is being merged with
   * its global equivalent in the global plan.
   * This method will be invoked on the matching global plan operator with
   * the local plan operator passed in as an argument.
   * @param localOpt the local operator being merged into the global plan
   * @return true iff this requires the corresponding global plan operator
   *         to be reinstantiated
   */
  public boolean mergeIntoGlobalPlan(PhyOpt localOpt)
  {
    return false;
  }
   
   public PhyOpt fixRoot() throws PhysicalPlanException
   {
     PhyOpt opt = this.globalEquiv;
     opt.addQueryIds(qryIds);
     return opt;
   }
   
   
  /**
   * Fix the inputs
   *
   */
  public boolean fixInputs() throws PhysicalPlanException
  {
    
    for(int i = 0; i < numInputs; i++)
    {
      PhyOpt opt = inputs[i].globalEquiv;
      // if the input is being shared , then do the updates
      if(opt != inputs[i])
      {
        // add the query ids to the operator getting shared
        opt.addQueryIds(this.qryIds);
        
        opt.addOutput(this);
 
        inputs[i] = opt;
      }
      else
      {
        // update the index of output operators as it was not 
        // updated when the local plan was created.
        // The index contains only those operators which are part 
        // of the global plan.
        opt.addOutputToIndex(this);
      }
    }
    setSourceLineages();
    return true;
  }

  public void setSourceLineages()
  {
    // At this point in time also setup the system timestamped source
    // lineage
    for(int i = 0; i < numInputs; i++)
    {
      if(inputs[i].getSystsSourceLineage() != null)
      {
        if(inputs[i].getSystsSourceLineage().size() > 0)
        {
          // lazy initialization
          if(systsSourceLineage == null)
            systsSourceLineage = new LinkedHashSet<PhyOpt>();

          systsSourceLineage.addAll(inputs[i].getSystsSourceLineage());
        }
      }
      
      if(inputs[i].getFullSourceLineage() != null)
      {
        if(inputs[i].getFullSourceLineage().size() > 0)
        {
          // lazy initialization
          if(fullSourceLineage == null)
            fullSourceLineage = new LinkedHashSet<PhyOpt>();

          fullSourceLineage.addAll(inputs[i].getFullSourceLineage());
        }
      }
      
    }

    if (numInputs == 2)
    {
      // For binary operators, populate outer and inner inputs' lineage also
      if(inputs[0].getSystsSourceLineage() != null)
      {
        if(inputs[0].getSystsSourceLineage().size() > 0)
        {
          // lazy initialization
          if(outerSystsSourceLineage == null)
            outerSystsSourceLineage = new LinkedHashSet<PhyOpt>();

          outerSystsSourceLineage.addAll(inputs[0].getSystsSourceLineage());
        }
      }
      
      if(inputs[1].getSystsSourceLineage() != null)
      {
        if(inputs[1].getSystsSourceLineage().size() > 0)
        {
          // lazy initialization
          if(innerSystsSourceLineage == null)
            innerSystsSourceLineage = new LinkedHashSet<PhyOpt>();

          innerSystsSourceLineage.addAll(inputs[1].getSystsSourceLineage());
        }
      }
      
    }
  }

  /**
   * Removes the operator from PlanManager
   * @param opt
   */
  public void removePhyOp() throws PhysicalPlanException
  {
    // if child is a view don't remove child
    if(!this.isSource)
    {
      for(int i = 0; i < this.numInputs; i++)
      {
        PhyOptOutputIter iter = this.inputs[i].getOutputsIter();
        while(true)
        {
          PhyOpt op = iter.getNext();
          if(op == this)
          {
            iter.remove();
            break;
          }
          else if(op == null)
            break;
        }
      }
     // remove from the list maintained by PlanManager
      execContext.getPlanMgr().removePhyOpt(this);
    }
    
  }
  
  public boolean isNew()
  {
    return qryIds.size() == 0;
  }

  public void addQryId(Integer id)
  {
    addQryId(id, false);
  }
  
  /**
   * @return Returns the qryIds.
   */
  public LinkedList<Integer> getQryIds()
  {
    return qryIds;
  }

  public synchronized void addQryId(Integer id, boolean recurse)
  {
    if(!findQueryId(id))
      qryIds.add(id);
    
    // if child is a view then queryids already present
//    if(isSource == true)
//      recurse = false;

    if (recurse)
      for (int i = 0; i < numInputs; i++)
        inputs[i].addQryId(id, recurse);
  }
  
  public boolean findQueryId(Integer id)
  {
    int qid = id.intValue();
    Iterator<Integer> iter = qryIds.iterator();
    while(iter.hasNext())
    {
      int queryid = iter.next().intValue();
      if(queryid == qid)
        return true;
    }
      return false;
  }
  
  public void addSourceId(int src_id)
  {
    Iterator<Integer> iter = srcIds.iterator();
    while(iter.hasNext())
    {
      int sid = iter.next().intValue();
      if(sid == src_id)
        return;
    }
    srcIds.add(new Integer(src_id));
  }
  
  public boolean deleteSourceId(int src_id)
  {
    Iterator<Integer> iter = srcIds.iterator();
    while(iter.hasNext())
    {
      if(iter.next().intValue() == src_id)
      {
        iter.remove();
        return true;
      }
    }
    return false;
  }
  
  public synchronized void addQueryIds(LinkedList<Integer> qIds)
  {
    Iterator<Integer> iter = qIds.iterator();
    while (iter.hasNext())
    {
      addQryId(iter.next(), true);
    }
  }

  /**
   * Returns whether queryid got deleted or not
   * @param id 
   *          Query identifier
   * @return true/false
   */
  public boolean delQryId(int id)
  {
    Iterator<Integer> iter = qryIds.iterator();
    while (iter.hasNext())
    {
      int qid = iter.next().intValue();
      if (id == qid)
      {
        iter.remove();
        return true;
      }
    }
    return false;
  }

  private boolean elemInList(int id, List<Integer> idList)
  {
    Iterator<Integer> iter = idList.iterator();
    while (iter.hasNext())
    {
      int elem = iter.next().intValue();
      if (elem == id)
        return true;
    }

    return false;
  }

  public boolean refersElseQid(List<Integer> idList)
  {
    Iterator<Integer> iter = qryIds.iterator();
    while (iter.hasNext())
    {
      int qid = iter.next().intValue();
      if (!elemInList(qid, idList))
        return true;
    }
    return false;
  }

  public boolean refersElseQid(int myId, List<Integer> idList)
  {
    Iterator<Integer> iter = qryIds.iterator();
    while (iter.hasNext())
    {
      int qid = iter.next().intValue();
      if ((qid != myId) && !elemInList(qid, idList))
        return true;
    }
    return false;
  }

  public void addAllObj(Object obj)
  {
    allObjs.add(obj);
  }

  public LinkedList<Object> getAllObj()
  {
    return allObjs;
  }

  public int getAttrLen(int i)
  {
    if(attrMetadata[i] != null)
      return attrMetadata[i].getLength();
    
    if(attrMetadata == null)
      System.out.println("ERROR !!!");
    return -1;
  }
  
  public void setAttrLen(int i, int length)
  {
    if(attrMetadata[i] == null)
    {
      System.out.println("ERROR !!!! setAttrLen : phyOpt");
      return;
    }
     attrMetadata[i].setLength(length); 
  }
  
  public Datatype getAttrTypes(int i)
  {
    return attrMetadata[i].getDatatype();
  }
  
  public void setAttrTypes(int i, Datatype type)
  {
    if(attrMetadata[i] == null)
    {
      attrMetadata[i] = new AttributeMetadata(type);
    }
      attrMetadata[i].setDatatype(type);
  }
  
  public int getAttrPrecision(int i)
  {
    if(attrMetadata[i] != null)
      return attrMetadata[i].getPrecision();
    
    return -1;
  }
  
  public int getAttrScale(int i)
  {
    if(attrMetadata[i] != null)
      return attrMetadata[i].getScale();
    
    return -1;
  }
  
  public AttributeMetadata[] getAttrMetadata()
  {
    return this.attrMetadata;
  }
  
  public AttributeMetadata getAttrMetadata(int i)
  {
    return this.attrMetadata[i];
  }
  
  public void setAttrMetadata(AttributeMetadata[] attr)
  {
    this.attrMetadata = attr;
  }
  
  public void setAttrMetadata(int i, AttributeMetadata attr)
  {
    this.attrMetadata[i] = attr;
  }

  
  public int getId()
  {
    return id;
  }


  public String getOptName()
  {
    return getOperatorKind() + "#" + getId();
  }

  /**
   * @return Returns the state.
   */
  public PhyOptState getState()
  {
    return state;
  }

  /**
   * @param state
   *          The state to set.
   */
  public void setState(PhyOptState state)
  {
    this.state = state;
  }

  public PhyOpt getInput(int inputNo)
  {
    return inputs[inputNo];
  }

  public PhyOpt[] getInputs()
  {
    return inputs;
  }

  public void setInputs(PhyOpt[] inputs)
  {
    this.inputs = inputs;
  }
  
  public void setInput(int inputPos, PhyOpt op)
  {
    assert ((inputPos >= 0) && (inputPos < inputs.length)) :
      "setInput on phyopt called with invalid index";
    this.inputs[inputPos] = op;
  }

  public PhyQueue[] getInQueues()
  {
    return inQueues;
  }

  public void setInQueues(PhyQueue[] inQueues)
  {
    this.inQueues = inQueues;
  }

  public ExecOpt getInstOp()
  {
    return instOp;
  }

  public void setInstOp(ExecOpt instOp)
  {
    this.instOp = instOp;
  }

  public PhyStore[] getInStores()
  {
    return inStores;
  }

  public void setInStores(PhyStore[] inStores)
  {
    this.inStores = inStores;
  }

  public boolean getIsStream()
  {
    return isStream;
  }

  public void setIsStream(boolean isStream)
  {
    this.isStream = isStream;
  }

  public boolean getIsSource()
  {
    return this.isSource;
  }
  
  public void setIsSource(boolean src)
  {
    this.isSource = src;
  }
  
  public boolean getIsView()
  {
    return isView;
  }
  
  public void setIsView(boolean isView)
  {
    this.isView = isView;
  }

  public int getNumAttrs()
  {
    return numAttrs;
  }

  public void setNumAttrs(int numAttrs)
  {
    this.numAttrs      = numAttrs;
    this.attrMetadata  = new AttributeMetadata[numAttrs];
  }

  public int getNumInputs()
  {
    return numInputs;
  }

  public void setNumInputs(int numInputs)
  {
    this.numInputs = numInputs;
    inputs = new PhyOpt[numInputs];
    inStores = new PhyStore[numInputs];
    for (int i = 0; i < numInputs; i++)
      inStores[i] = null;
    inQueues = new PhyQueue[numInputs];
    for (int i = 0; i < numInputs; i++)
      inQueues[i] = null;
  }

  public int getNumOutputs()
  {
    return numOutputs;
  }

  public PhyOptKind getOperatorKind()
  {
    return operatorKind;
  }

  public ArrayList getOutputs()
  {
    return outputs;
  }

  /**
   * Returns a list of all output operators which are similar
   * to the operator prototype passed
   * @param prototype
   *                 the operator whose matches has to be found
   */
  public List<PhyOpt> getPossibleOutputMatches(PhyOpt prototype) 
  {
    /**
     * Get the list from the index maintained by its global 
     * Equivalent operator
     */  
    PhyOpt globalEquiv = this.getGlobalEquiv();   
    return globalEquiv.getOutputsIndex().get(prototype.getSharingHash());
  }
  
  /**
   * Computes the Hash value of the phyOpts
   * which is used while computing whether the
   * operator can be shared or not.
   * @return  
   *        The hash value of the phyopt
   * */
  public final int getSharingHash()
  {
    if(sharingHash == null)
      sharingHash    = getSignature().hashCode();
    
    return sharingHash;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return this.getOperatorKind() + "#" 
         + this.getNumAttrs();
  }
  
  /**
   * This method returns true if and only if this operator requires
   * its input(s) to be buffered completely.
   *
   * If this returns false, then this operator requires only the
   * latest input element to be stored, since it guarantees to consume
   * the input element each time it is called. Further, with direct interop,
   * since each enqueue will result in the operator being invoked immediately
   * (before the next input is enqueued), this suffices
   *
   * On the other hand, there are some operators, like binary operators,
   * that cannot guarantee to consume an input element each time it is called.
   * This can happen since binary operators are required to consume elements
   * across both their inputs in non-decreasing timestamp order. Since the
   * two inputs are independent, and if their speed varies, one may be ahead
   * of the other. Thus, this requires input buffering.
   */
  public boolean requiresBufferedInput()
  {
    // Currently, only binary operators whose inputs are not pull operators
    // require input buffering
    return (numInputs == 2 && !getInput(0).isPullOperator() 
            && !getInput(1).isPullOperator() );
  }

  public PhySharedQueueWriter getOutQueue()
  {
    return outQueue;
  }

  public void setOutQueue(PhySharedQueueWriter outQueue)
  {
    this.outQueue = outQueue;
  }

  public PhyStore getStore()
  {
    return store;
  }

  public void setStore(PhyStore store)
  {
    this.store = store;
  }

  public PhySynopsis[] getSynopses() {return synopses;}
  
  public PhySynopsis   getSynopsis(int idx)
  {
    if (synopses == null || synopses.length <= idx)
      return null;
    return synopses[idx];
  }
 
  public void setSynopsis(int idx, PhySynopsis synopsis)
  {
    PhySynopsis[] old = synopses;
    int sz = synopses == null ? 0 : synopses.length;
    if (sz == 0 || sz <= idx)
    {
      synopses = new PhySynopsis[idx + 1];
      if (old != null)
      {
        System.arraycopy(old, 0, synopses, 0, sz);
      }
    }
    synopses[idx] = synopsis;
  }
  
  private boolean canOptDelete(int qid)
  {
    Iterator<Integer> iter = qryIds.iterator();

    while (iter.hasNext())
    {
      int id = iter.next().intValue();
      if (qid != id)
        return false;
    }

    return true;
  }

  void freeObjects() throws CEPException
  {
    // Go over the list of objects allocated, and free them
    Iterator<Object> iter = allObjs.iterator();

    ObjectFactoryContext allCtx = new ObjectFactoryContext(execContext);
    // first free all the secondary objects
    while (iter.hasNext())
    {
      Object obj = iter.next();
      allCtx.setObjectType(obj.getClass().getName());
      allCtx.setOpt(this);
      allCtx.setObject(obj);
      if (!ObjectManager.isPrimary(allCtx))
      {
        ObjectManager.free(allCtx);
        iter.remove();
      }
    }

    // In the second pass, free all the primary objects
    iter = allObjs.iterator();

    // first free all the secondary objects
    while (iter.hasNext())
    {
      Object obj = iter.next();
      allCtx.setObjectType(obj.getClass().getName());
      allCtx.setOpt(this);
      allCtx.setObject(obj);
      assert (ObjectManager.isPrimary(allCtx) == true);
      ObjectManager.free(allCtx);
      iter.remove();
    }
    
  }
  
  public void freeStore()
  {
   // remove physical store if present
    if(store != null)
      execContext.getPlanMgr().removePhyStore(store.getId(), store, this);
  }
  
  public void freeSynopsis()
  {
    execContext.getPlanMgr().removePhysicalSynopsis(this);
  }
  
  public void freeInQueues()
  {
    if(this.inQueues != null)
    {
      for(int i=0; i<inQueues.length; i++)
        execContext.getPlanMgr().removePhyQueueReader(inQueues[i]);
    }
  }
  
  public void freeOutQueue()
  {
    if(outQueue != null)
      execContext.getPlanMgr().removePhyQueueWriter(outQueue);
  }

  public boolean delete(QueryDeletionContext ctx) throws CEPException
  {
    int qryId = ctx.getQuery().getId();
    // This can happen if any query, source, view etc is shared within the same query
    boolean ifdeleted = delQryId(qryId);
    
    if(isSource)
    {
      // It is either a view or a relation or a stream
      if(isView)
      {
        // get all dest viewids of the query
     //   List<Integer> destViews = ctx.getQuery().getDestViews();
        Integer[] destViews = execContext.getDependencyMgr().
                              getDependents(qryId, DependencyType.VIEW);
        if(destViews != null)
        {
          for(int i=0; i<destViews.length; i++)
          {
            int vid = destViews[i].intValue();
            boolean wasPresent = this.deleteSourceId(vid);
            // wasPresent will not be true if it is not the source operator for this view
            if(wasPresent)
              execContext.getPlanMgr().dropSourceOp(vid, this);
          }
        }
        // its no longer source of any view
        if(this.srcIds.isEmpty())
          isView = false;
      }
      if (qryIds.size() == 0 && ifdeleted)
      {
        Iterator<Integer> src_iter = this.srcIds.iterator();
        while(src_iter.hasNext())
        {
          int sid = src_iter.next().intValue();
          execContext.getPlanMgr().dropSourceOp(sid, this);
        }
        this.srcIds.clear();
        isSource = false;
        isView = false;
        // If it is a table function operator, clear the entries from table
        // function source list
        if(this instanceof PhyOptTableFunctionRelnSrc)
          execContext.getPlanMgr().dropTableFunctionOperator(this);
      }
    }
    
    // Dont do anything since operators below have already been traversed once
    if(!ifdeleted)
      return ifdeleted;

    if (qryIds.size() == 0)
    {
      freeObjects();
      freeStore();
      freeSynopsis();
      freeInQueues();
      freeOutQueue();
      
      if(!isSource && this.isHbtTimeoutRequired())
      {
        Collection<PhyOpt> sysTsSources = this.getSystsSourceLineage();
        // There are no system timestamped input sources
        if(sysTsSources != null && !sysTsSources.isEmpty())
        {
          Iterator<PhyOpt> iter = sysTsSources.iterator();
          while(iter.hasNext())
          {
            PhyOpt op = iter.next();            
            op.removeFromTimeOutOpList(this);
          }
        }
        
      }
      
      // remove PhyOpt from the list only if it is not shared
      execContext.getPlanMgr().removePhyOpt(this);
      
      // If the a particular input is not getting deleted, correct this output
      for (int i = 0; i < numInputs; i++)
      {
        if (!inputs[i].canOptDelete(qryId))
        {          
          PhyOptOutputIter iter = inputs[i].getOutputsIter();
          while (true)
          {
            PhyOpt op = iter.getNext();
            assert op != null;
            if (op == this)
            {
              iter.remove();
              break;
            }
          }
        }
      }
    }

    for(int i = 0; i < numInputs; i++)
    {
      inputs[i].delete(ctx);
    }
    
    return ifdeleted;
  }

  public PhyOptOutputIter getOutputsIter()
  {
    if (numOutputs == 0)
      return null;
    return new PhyOptOutputIter(this, outputs, activeOutputs);
    
  }

  public void addOutput(PhyOpt opt) throws PhysicalPlanException
  {
    // Allocate outputs the first time 
    if (numOutputs == 0)
      outputs = new ExpandableArray<PhyOpt>(Constants.INTIAL_NUM_OUT_BRANCHING);

    // Get next unused reader
    int outputNo = activeOutputs.nextClearBit(0);
    
    activeOutputs.set(outputNo);
    
    outputs.set(outputNo,opt);
    
    numOutputs++;
    
    // update the HashMap with the new output operator
    // An output operator is added to the index only when it
    // is a part of the globaL Plan.
    // An operator in the global plan has itself as its 
    // Global Equivalent
    if(opt.getGlobalEquiv() == opt)
      addOutputToIndex(opt);
  }

  /**
   * Adds the operator to the Index of all output operators
   * @param o
   *        physical operator to be added to the Index
   */
  private void addOutputToIndex(PhyOpt o)
  {
    List<PhyOpt> opList = outputsIndex.get(o.getSharingHash());
    if(opList == null)
    {
      opList = new LinkedList<PhyOpt>();
      outputsIndex.put(o.getSharingHash(), opList);
    }
    
    if(!opList.contains(o))
      opList.add(o);
    
  }
  
  void removeOutput(int pos) throws PhysicalPlanException
  {
    assert activeOutputs.get(pos) == true;
    assert outputs.get(pos) != null;

    //  update the HashMap of the output operators
    removeOutputFromIndex(outputs.get(pos));   
    
   
    outputs.set(pos, null);
    activeOutputs.clear(pos);
    numOutputs--;
  }
  
  /**
   * Removes the operator from the Index of all output operators 
   * @param o
   *        physical operator to be removed to the Index
   */
  private void removeOutputFromIndex(PhyOpt o)
  {
    List<PhyOpt> opList = outputsIndex.get(o.getSharingHash());
    if(opList == null)
    {
      return;
    }
    opList.remove(o);
   
    if(opList.size() == 0)
      outputsIndex.remove(o.getSharingHash());
  }
  
  public void copy(PhyOpt src)
  {
    copy(src, src.getNumAttrs(), false);
  }

  public void copy(PhyOpt src, boolean hasElementTime)
  {
    copy(src, src.getNumAttrs(), hasElementTime);
  }
  
  public void copy(PhyOpt src, LogOpt logopt)
  {
    copy(src, logopt.getNumOutAttrs(), false);
  }

  private void copy(PhyOpt src, int numAttribs, boolean hasElementTime)
  {
    assert src.getNumAttrs() > 0;
    
    if(hasElementTime)
      numAttrs = numAttribs + 1; // for the element_time
    else
    {
      numAttrs = numAttribs;

      // In general, numAttribs (arg passed in) may be less than
      // equal to that of the input. This can happen due to certain
      // optimizations by the optimizer, such as, for example, removing
      // "useless" projects, where a project is only passing through a 
      // "prefix" of its input's out attributes
      assert numAttrs <= src.getNumAttrs();
    }
    
    // Currently, allocate the attributes length and types in this
    // function. Not sure, if we would have to use MAX_ATTRS equivalent
    // in the constructor eventually.
    
    attrMetadata = new AttributeMetadata[numAttrs];

    
    if(hasElementTime)
    {
      // Set Attribute type and length from input operator
      // except ELEMENT_TIME column 
      for (int a = 0; a < numAttrs - 1; a++)
      {
        attrMetadata[a] = src.getAttrMetadata(a);
      }
      
      // Set Attribute type and length for ELEMENT_TIME pseudo column
      StreamPseudoColumn elemTime = StreamPseudoColumn.ELEMENT_TIME;
      
      attrMetadata[numAttrs - 1]  = new AttributeMetadata(elemTime.getColumnType(), 
          elemTime.getColumnLen(), elemTime.getColumnType().getPrecision(), 0);
    }
    else
    {
      for (int a = 0; a < numAttrs; a++)
      {
        attrMetadata[a] = src.getAttrMetadata(a);
      }
    }
  }

  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperator id=\"" + getId() + "\" >");
    sb.append("<OperatorKind operatorKind=\"" + operatorKind + "\" />");

    sb.append("<NumberOfAttrs numAttrs=\"" + numAttrs + "\" />");

    // Process attribute datatypes
    if (attrMetadata.length != 0)
    {
      sb.append("<AttributeDatatypes>");
      for (int i = 0; i < attrMetadata.length; i++)
      {
        sb.append("<Datatype>");
        sb.append((attrMetadata[i].getDatatype()).toString());
        sb.append("</Datatype>");
      }
      sb.append("</AttributeDatatypes>");
    }

    // Process attribute datatypes
    if (attrMetadata.length != 0)
    {
      sb.append("<AttributeDatatypes>");
      for (int i = 0; i < attrMetadata.length; i++)
        sb.append("<AttrLength attrLen=\"" + attrMetadata[i].getLength() 
                   + "\" />");
      sb.append("</AttributeDatatypes>");
    }

    if (isStream == true)
      sb.append("<Stream>true</Stream>");
    else
      sb.append("<Stream>false</Stream>");

    sb.append("<NumberOfInputs numInputs=\"" + numInputs + "\" />");
    sb.append("<NumberOfOutputs numOutputs=\"" + numOutputs + "\" />");

    // Process Inputs
    if (numInputs != 0)
    {
      sb.append("<Inputs>");
      for (int i = 0; i < numInputs; i++)
        sb.append(inputs[i].toString());
      sb.append("</Inputs>");
    }

    // Right now the execution related stuff is not dumped

    sb.append("</PhysicalOperator>");
    return sb.toString();
  }
  
  // Visualiser Compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    // view information
    xml.append("<property name = \"Is View\" value = \"");
    xml.append(this.getIsView());
    xml.append("\"/>");
    xml.append("<property name = \"Ordering Constraint\" value = \"");
    xml.append(this.getOrderingConstraint().toString());
    xml.append("\"/>");
    xml.append("<property name = \"QryIdList\" value = \"");
    String qryIdList="";
    if(qryIds != null)
    {
      Iterator<Integer> iter = qryIds.iterator();
      while(iter.hasNext())
      {
        int queryid = iter.next().intValue();
        qryIdList = qryIdList + queryid;
        if(iter.hasNext())
          qryIdList = qryIdList + ", ";
      }
    }
    else 
      qryIdList = "Null";
    xml.append(qryIdList);
    xml.append("\"/>");
    
    for(int input = 0; input < numInputs; input++) {
      xml.append("<input queue = \"");
      if(inQueues[input].getQueueKind() == PhyQueueKind.READER_Q) {
        PhySharedQueueReader reader = (PhySharedQueueReader)inQueues[input];
        xml.append(reader.writer.id);
      }
      else {
        xml.append(inQueues[input].id);
      }
      xml.append("\"> ");
      xml.append(inputs[input].getId());
      xml.append(" </input>\n");
    }
    return xml.toString();
  }
  
  /**
   * Getter for the position of the Relation Synopsis for the Physical Operators
   * which are supposed to have one. Other Synopsis Kinds have pre defined position.
   *
   * @param synopsis for which position is needed
   * @return the position of the synopsis
   */
  public abstract String getRelnSynPos(PhySynopsis syn); 

  public int getInputIndex(PhyOpt parent)
  {
    assert parent != null;

    for (int c = 0; c < parent.getNumInputs(); c++)
      if (parent.getInputs()[c] == this)
        return c;

    // should not come here
    assert false;
    return 0;
  }

  public int getOutputIndex(PhyOpt parent) throws PhysicalPlanException
  {
    assert parent != null;

    assert numOutputs != 0;
    iter.initialize(outputs, activeOutputs);

    for (int p = 0; p < numOutputs; p++)
    {
      PhyOpt out = iter.getNext();
      assert out != null;

      if (out == this)
        return p;
    }

    // should not come here
    assert false;
    return 0;
  }

  // The physical operators who need a shared synopsis override this method
  public boolean getSharedSynType(int idx)
  {
    return false;
  }

  /**
   * Get the shared relation store that this operator is willing to share.
   * 
   * @return the shared relation store that this operator is willing to share
   */
  public PhyStore getSharedRelStore()
  {
    return store;
  }

  /**
   * The synopsis of the store needs to be shared with its inputs.
   */
  public void synStoreReq()
  {
    return;
  }

  // Implementation of DAGNode Interface
  @SuppressWarnings("unchecked")
  public ArrayList<DAGNode> getOutNeighbours()
  {
    return getOutputs();
  }

  public DAGNode[] getInNeighbours()
  {
    return getInputs();
  }

  public int getOutDegree()
  {
    return numOutputs;
  }

  public int getInDegree()
  {
    return numInputs;
  }

  public void createBinarySchema(PhyOpt op1, PhyOpt op2)
  {
    createBinarySchema(op1, op1.getNumAttrs(), op2, op2.getNumAttrs());
  }
  
  public void createBinarySchema(PhyOpt op1, int numLeft, 
                                 PhyOpt op2, int numRight)
  {
    createBinarySchema(op1, numLeft, op2, numRight, false);
  }
                               

  public void createBinarySchema(PhyOpt op1, int numLeft, 
                                 PhyOpt op2, int numRight,
                                 boolean hasElementTime)
  {
    int i1 = numLeft;
    int i2 = numRight;

    if(hasElementTime)
      setNumAttrs(i1 + i2 + 1); // for the element time column
    else
      setNumAttrs(i1 + i2);

    //Datatype[] arrTyp = op1.getAttrTypes();
    //int[] arrLen = op1.getAttrLen();
    AttributeMetadata[] attrMetadata = op1.getAttrMetadata();

    for (int a = 0; a < i1; a++)
    {
      /*attrTypes[a] = arrTyp[a];
      attrLen[a] = arrLen[a];*/
      this.attrMetadata[a] = attrMetadata[a]; 
    }

    /*arrTyp = op2.getAttrTypes();
    arrLen = op2.getAttrLen();*/
   attrMetadata = op2.getAttrMetadata();

    for (int a = 0; a < i2; a++)
    {
      /*attrTypes[i1 + a] = arrTyp[a];
      attrLen[i1 + a] = arrLen[a];*/
      this.attrMetadata[i1 + a] = attrMetadata[a]; 
    }
    
    if(hasElementTime)
    {
      // Set Attribute type and length for ELEMENT_TIME pseudo column
      StreamPseudoColumn elemTime = StreamPseudoColumn.ELEMENT_TIME;
      /*attrTypes[i1 + i2]    = elemTime.getColumnType();
      attrLen[i1 + i2]      = elemTime.getColumnLen();  */
      this.attrMetadata[i1 + i2] = new AttributeMetadata
                                         (elemTime.getColumnType(),
                                          elemTime.getColumnLen(), 
                                          elemTime.getColumnType().getPrecision(), 
                                          0); 
    }
  }
  
  /**
   * 
   * link the store to the synopsis
   */
  public void linkSynStore()
  {
    return;
  }
  
  /**
   * @return true if this operator dependent on synopsis/store of child(ren)
   */
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    // default - assume operator is not dependent on synopsis and/or store of 
    // child(ren)
    return false;
  }

  /**
   * Add the execution operator to the specified ExecManager list
   * 
   * @throws ExecException
   */
  public synchronized void addOp() throws ExecException
  {
    for (int i = 0; i < numInputs; i++) 
    {
      inputs[i].addOp();
    }
    instOp.addExecOp();
  }

  /**
   *
   * @param visitor
   */
  public void accept(IPlanVisitor visitor) 
  {
    visitor.visit(this);    
    for(int input = 0; input < numInputs; input++) 
    {
      if (visitor.canVisit(IPlanVisitor.ObjType.SUB_QUEUE))
          inQueues[input].accept(visitor);
      if (visitor.canVisit(IPlanVisitor.ObjType.INPUT_OPERATOR))
        inputs[input].accept(visitor);
    }
    if (outQueue != null && visitor.canVisit(IPlanVisitor.ObjType.SUB_QUEUE))
      outQueue.accept(visitor);
    PlanManager pm = execContext.getPlanMgr();
    if (visitor.canVisit(IPlanVisitor.ObjType.SUB_SYNOPSIS) ||
        visitor.canVisit(IPlanVisitor.ObjType.SUB_INDEX))
    {
      Collection<PhySynopsis> listPhySyn = pm.getActivePhySyn();
      for (PhySynopsis syn : listPhySyn) 
      {
        if (syn == null) continue;
        if (syn.getOwnOp() == this) 
        {
          syn.accept(visitor);
        }
      }
    }
    if (visitor.canVisit(IPlanVisitor.ObjType.SUB_STORE) ||
        visitor.canVisit(IPlanVisitor.ObjType.SUB_INDEX))
    {
      Collection<PhyStore> listPhyStore = pm.getActivePhyStore();    
      for (PhyStore store : listPhyStore) 
      {
        if (store == null) continue;
        if (store.getOwnOp() == this) 
        {
          store.accept(visitor);
        }
      }
    }
  }
  
  /**
   * Generates a Regular expression in String format which represents
   * the given list of expressions. 
   * 
   * @param exprList The list of Expressions which are to traversed
   * @return A String regular expression of the Boolean expressions.
   */
  protected static String getExpressionList(Expr[] exprList)
  {
    // Return null; If no predicate
    if(exprList == null)
    {
      return "null";
    }
    
    // Return null; If no predicate
    if(exprList.length == 0)
    {
      return "null";
    }
    
    StringBuilder regExpression = new StringBuilder();   
    boolean commaRequired = false;
    
    for(Expr expr : exprList)
    {
      // The conditions are linked to each other with a comma
      if(commaRequired)
        regExpression.append(",");
     
      regExpression.append(expr.getSignature());
      
      commaRequired = true;
     
   }
    
    LogUtil.info(LoggerType.TRACE, "FilterList created is [" + 
        regExpression.toString() + "]");
    
    // convert to lower case to avoid problems due to different case
    return ("[" + regExpression.toString() + "]").toLowerCase();
  }

  /**
   * Generates a Regular expression in String format which represents
   * the given list of expressions. It traverses each of the expression 
   * trees in a pre-order manner and creates a 
   * parenthesized representation of it.
   * 
   * @param exprList The list of Boolean Expressions which are to traversed
   * @return A String regular expression of the Boolean expressions.
   */
  protected static String getExpressionList(
                          LinkedList<BoolExpr> exprList)
  { 
    if(exprList == null)
      return "null";

    // Return null; If no predicate
    if(exprList.isEmpty())
      return "null";

    StringBuilder regExpression = new StringBuilder();   
    Iterator<BoolExpr> iter = exprList.iterator();
    BoolExpr expr = iter.next();
    boolean flag = true;
    while(flag)
    {
      regExpression.append(expr.getSignature());
      
      // The conditions are linked to each other with a comma
      if(iter.hasNext())
      {
        expr = iter.next();
        regExpression.append(",");
      }
      else
        flag = false;
    }

    LogUtil.info(LoggerType.TRACE, "regExpression created is [" + 
        regExpression.toString() + "]");

    // convert to lower case to avoid problems due to different case
    return ("[" + regExpression.toString() + "]").toLowerCase();
  }
  
  /**
   * @return the extDataSource
   */
  public IExternalDataSource getExtDataSource()
  {
    return extDataSource;
  }

  /**
   * @param extDataSource the extDataSource to set
   */
  public void setExtDataSource(IExternalDataSource extDataSource)
  {
    this.extDataSource = extDataSource;
  }
  
  /**
   * Add the given operator into the list of operators which
   * needs heartbeat from this stream source
   * @param phyOptId
   */
  public void addToTimeOutOpList(PhyOpt phyOptId)
  {
    // Nothing to do here; Only PhyOptStreamSrc and PhyOptRelnSrc will act
    // Reason to add empty method: 
    //  No need of casting to specific operator required if
    //  we allow any other kind of source operator to send hbts
  }
  
  /**
   * Remove the given operator from the list of operators which
   * needs heartbeat from this stream source
   * @param phyOptId
   */
  public void removeFromTimeOutOpList(PhyOpt phyOp)
  {  
    // Nothing to do here; Only PhyOptStreamSrc and PhyOptRelnSrc will act
    // Reason to add empty method: 
    //  No need of casting to specific operator required if
    //  we allow any other kind of source operator to send hbts
  }

  public void setOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.orderingConstraint = orderingConstraint;
  }
  
  public OrderingKind getOrderingConstraint()
  {
    return this.orderingConstraint;
  }
  
  public void setLHSConnector(boolean val)
  {
    this.isLHSConnector = val;
  }
  
  public boolean isLHSConnector()
  {
    return this.isLHSConnector;
  }
  
  public void setRHSConnector(boolean val)
  {
    this.isRHSConnector = val;
  }
  
  public boolean isRHSConnector()
  {
    return this.isRHSConnector;
  }

  public void setCanBeConnectorOperator(boolean b)
  {
    this.canBeConnectorOperator = b;
  }
  
  public boolean canBeConnectorOperator()
  {
    return this.canBeConnectorOperator;
  }
  
  public void setCanBeShared(boolean b)
  {
    this.canBeShared = b;
  }
  
  public boolean canBeShared()
  {
    return this.canBeShared;
  }
  
  public boolean isBufferOpNeeded()
  {
    if((outputsExpectingBuffer != null)
        && (outputsExpectingBuffer.size() > 0))
      return true;
    else
      return false;
  }
  
  public List<PhyOpt> getOutputsExpectingBuffer()
  {
    return this.outputsExpectingBuffer;
  }
  
  public void setOutputsExpectingBuffer(List<PhyOpt> l)
  {
    this.outputsExpectingBuffer = l;
  }
  
  public boolean isBelowViewRootInclusive()
  {
    return this.isBelowViewRootInclusive;
  }
  
  public void setIsBelowViewRootInclusive(boolean val)
  {
    this.isBelowViewRootInclusive = val;
  }
}
