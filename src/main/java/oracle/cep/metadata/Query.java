/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Query.java /main/33 2014/10/14 06:35:32 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares Query in package oracle.cep.metadata.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi 09/23/14 - support for partitioned stream
 udeshmuk 09/18/14 - add method to determine if a query is dependent on
                     partitioned stream
 udeshmuk 08/07/13 - set default for isBaseTimeInMilliSeconds to true
 udeshmuk 07/09/13 - add ArchiverStats
 pkali    08/15/12 - included DynamicType meta info
 udeshmuk 06/05/12 - isViewQuery to indicate if this is defining query for a
                     view
 udeshmuk 07/12/11 - call semQuery.isDependentOnArchivedReln
 udeshmuk 05/12/11 - maintain start time for archived relation queries(used for
                     testing only)
 udeshmuk 03/07/11 - archived relations support
 sborah   06/16/10 - set ordering constraint
 parujain 09/24/09 - dependency support
 skmishra 02/13/09 - try/catch in addQueryDestination
 parujain 02/09/09 - remove destination
 parujain 01/14/09 - metadata in-mem
 sborah   11/24/08 - support for altering base timeline
 parujain 11/21/08 - handle constants
 parujain 09/12/08 - multiple schema support
 skmishra 08/21/08 - imports
 parujain 05/05/08 - 
 mthatte  02/26/08 - parametrizing metadata errors
 parujain 02/07/08 - parameterizing error
 hopark   01/17/08 - dump
 hopark   12/27/07 - support xmllog
 sbishnoi 12/18/07 - cleanup
 parujain 11/09/07 - external source
 mthatte  10/26/07 - adding onDemand flag
 mthatte  11/08/07 - jdbc changes
 sbishnoi 11/06/07 - support for update semantics
 mthatte  08/22/07 - 
 parujain 05/09/07 - stats enabled/disabled
 hopark   04/27/07 - use XMLHelper to verify
 hopark   03/21/07 - move the store integration part to CacheObject
 hopark   03/21/07 - make constructor public for unit test
 parujain 03/19/07 - drop window
 parujain 01/31/07 - drop function
 parujain 02/02/07 - startup 
 parujain 01/11/07 - BDB integration
 parujain 01/09/07 - bdb integration
 hopark   12/06/06 - remember logOpt
 parujain 10/24/06 - startup handling
 dlenkov  09/14/06 - referencing queries
 parujain 09/11/06 - MDS integration
 anasrini 09/05/06 - minor fix in setNames
 najain   08/31/06 - add name
 dlenkov  08/18/06 - support for named queries
 parujain 07/13/06 - check locks 
 parujain 06/29/06 - metadata cleanup 
 parujain  06/27/06 - metadata cleanup 
 najain    06/16/06 - bug fix 
 najain    06/13/06 - bug fix 
 najain    06/04/06 - add list of relations 
 najain    05/09/06 - compile breakup 
 najain    03/30/06 - add setDestination 
 skaluska  03/13/06 - Creation
 skaluska  03/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Query.java /main/33 2014/10/14 06:35:32 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.common.Datatype;
import oracle.cep.common.OrderingKind;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicTypeSystem;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpable;
import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.semantic.Expr;
import oracle.cep.semantic.SemQuery;
import oracle.cep.util.XMLHelper;
import oracle.xml.parser.v2.XMLDocument;

/**
 * Query
 * 
 * @author skaluska
 */
@DumpDesc(autoFields=true,
          attribTags={"Id", "Name"}, 
          attribVals={"getId", "getRName"})
public class Query extends CacheObject implements IDumpable, Cloneable
{

  /**
   * 
   */
  private static final long        serialVersionUID = 1L;

  /** semantic analysis tree */
  private transient SemQuery       semQuery;

  /** logical plan */
  private transient LogOpt         logOpt;

  /** output names */
  private String[]                 names;

  private Datatype[]                  types;
  
  /** current state */
  private QueryState               state;

  /** desired state */
  // This state is useful during startup where it will be different
  // from current state. In all other cases both will be same. This is
  // required esp. for views where dependent queries needs to restarted.
  private QueryState               desiredState;

  /** List of EPRs for the destination */
  private LinkedList<Destination>  extDests;

  /* List of Primary key attributes in output relation of this query*/
  private LinkedList<String>       outputConstraintAttrs;
  
  /** List of referenced relations */
  private LinkedList<IdStub>       refRelns;

  /** Query text */
  private String                   cqlTxt;

  boolean                          isNamed;
  
  boolean                          isStatsEnabled;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   */
  private boolean                  isBaseTimelineMillisecond = true;
  
  
  /** Whether this query references an onDemand table / view */
  boolean                          isRefExternal;
  
  /** flag to check if primary key exists or not*/
  boolean                          isPrimaryKeyExist;
  
  private AtomicInteger            lastDestId;
  
  /** User specified ordering constraint */
  private OrderingKind             userOrderingConstraint;

  /** 
   * System determined ordering constraint. 
   * This is the ordering constraint that is determined by the system
   * for this query based on the query definition and the user
   * specifed ordering constraint.
   * In general, this will be equal to or stricter than the user
   * specified ordering constraint.
   */
  private OrderingKind             systemOrderingConstraint;

  /**
   * The partition parallel parser expression node associated with
   * a PARTITION_ORDERED user ordering constraint
   */
  private CEPExprNode              partitionParallelExprNode;
  
  /**
   * Start time of the query. 
   * Used only for the purpose of regression testing of archived relation
   * based queries.
   */
  private long                     startTime;
  
  private boolean                  isDependentOnArchivedReln = false;
  
  /**
   * True if this is a defining query for a view.
   * Used in archived relation context to determine if we want to
   * engage in state initialization or not.
   */
  private boolean                  isViewQuery = false;
  
  /**
   * Dynamic type system are used to create dynamic type at runtime.
   * It is used as part of internal cartridge.
   */
  private DynamicTypeSystem        dynamicTypeSystem = null;
  
  /** Holds the dynamic type names generated for this query */
  private List<String>             dynamicTypeNames  = null;
  
  /** Stats collected for start time processing of the query */
  private ArchiverStats             archiverStats = null;

  /** returns true if query is dependent on partition stream */
  private boolean                   isDependentOnPartnStream = false;
 
  @SuppressWarnings("unchecked")
  public Query clone() throws CloneNotSupportedException
  {
     Query q                 = (Query)super.clone();
     q.extDests              = (LinkedList<Destination>)extDests.clone();
     q.refRelns              = (LinkedList<IdStub>)refRelns.clone();
     q.outputConstraintAttrs = 
       (LinkedList<String>)outputConstraintAttrs.clone();
     q.partitionParallelExprNode = partitionParallelExprNode;

     return q;
  }


  /**
   * Constructor for Query
   *
   * @param name
   *          Query name
   * @param cql
   *          Query text
   */
  public Query(String name, String schema, String cql)
  {
    super(name, schema, CacheObjectType.QUERY);
    refRelns                  = new LinkedList<IdStub>();
    extDests                  = new LinkedList<Destination>();
    cqlTxt                    = cql;
    isNamed                   = true;
    isStatsEnabled            = false;
    isBaseTimelineMillisecond = true;
    isRefExternal             = false;
    outputConstraintAttrs     = new LinkedList<String>();
    isPrimaryKeyExist         = false;
    lastDestId                = new AtomicInteger(0);
    // default constraint
    userOrderingConstraint    = OrderingKind.TOTAL_ORDER;
    systemOrderingConstraint  = OrderingKind.TOTAL_ORDER;
    partitionParallelExprNode = null;
    startTime                 = Long.MIN_VALUE;
    archiverStats             = null;
  }

  /**
   * Constructor for Query
   * 
   * @param cql
   *          Query text
   */
  Query(String cql, String schema)
  {
    super(cql, schema, CacheObjectType.QUERY);
    refRelns                  = new LinkedList<IdStub>();
    extDests                  = new LinkedList<Destination>();
    cqlTxt                    = cql;
    isNamed                   = false;
    isStatsEnabled            = false;
    isBaseTimelineMillisecond = true;
    outputConstraintAttrs     = new LinkedList<String>();
    isPrimaryKeyExist         = false;
    lastDestId                = new AtomicInteger(0);
    // default constraint
    userOrderingConstraint    = OrderingKind.TOTAL_ORDER;
    systemOrderingConstraint  = OrderingKind.TOTAL_ORDER;
    partitionParallelExprNode = null;
    startTime                 = Long.MIN_VALUE;
    archiverStats             = null;
  }

  /**
   * Get the query text
   * 
   * @return Query text
   */
  public String getText()
  {
    return cqlTxt;
  }

  /**
   * Set the query text
   * 
   * Set the Query text
   */
  public void setText(String cql)
  {
    cqlTxt = cql;
  }

  /**
   * Get the query name
   * 
   * @return Query name
   */
  public String getName()  
  {
    return (String) this.getKey();
  }

  /**
   * Get isNamed
   * 
   * @return isNamed
   */
  public boolean getIsNamed()
  {
    return isNamed;
  }

  /**
   * Set isNamed
   */
  public void setIsNamed(boolean is)
  {
    assert this.isWriteable() == true;
    isNamed = is;
  }
  
  /** Does this query reference an onDemand reln.? */
  public boolean isRefExternal() 
  {
    return isRefExternal;
  }

  /** Setter for isRefExternal */
  public void setRefExternal(boolean isRefExternal) 
  {
    assert this.isWriteable() == true;
    this.isRefExternal = isRefExternal;
  }
  
  /**
   * Sets the stats to be enabled/disabled 
   *  
   * @param isStatsEnabled 
   *          whether Enabled/Disabled
   */
  public void setIsStatsEnabled(boolean isStatsEnabled)
  {
    assert this.isWriteable() == true;
    this.isStatsEnabled = isStatsEnabled;
    this.isBaseTimelineMillisecond = true;
  }


  /**
   * Sets the stats to be enabled/disabled 
   * and the base timeline to millisecond/nanosecond
   *  
   * @param isStatsEnabled 
   *          whether Enabled/Disabled
   * 
   * @param isBaseTimelineMillisecond 
   *                         whether millisecond/nanosecond
   */
  public void setIsStatsEnabled(boolean isStatsEnabled, 
                          boolean isBaseTimelineMillisecond)
  {
    assert this.isWriteable() == true;
    this.isStatsEnabled = isStatsEnabled;
    this.isBaseTimelineMillisecond = isBaseTimelineMillisecond;
  }
  
  /**
   * Returns whether stats are enabled/disabled
   * @return Returns whether stats are enabled/disabled
   */
  public boolean getIsStatsEnabled()
  {
    return this.isStatsEnabled;
  }

  /**
   * @return the isBaseTimelineMillisecond
   */
  public boolean getIsBaseTimelineMillisecond()
  {
    return isBaseTimelineMillisecond;
  }

    
  /**
   * Getter for state in Query
   * 
   * @return Returns the state
   */
  public QueryState getState()
  {
    return state;
  }

  /**
   * Getter of Desired state in Query
   * 
   * @return Returns the Desired Final state
   */
  public QueryState getDesiredState()
  {
    return desiredState;
  }

  /**
   * Setter for state in Query
   * 
   * @param state
   *          The state to set.
   */
  public void setState(QueryState state)
  {
    // check whether write lock for this object has been acquired or not.
    assert this.isWriteable() == true;

    this.state = state;
  }

  public void setDesiredState(QueryState state)
  {
    assert this.isWriteable() == true;

    this.desiredState = state;
  }

  /**
   * @return Returns the semQuery.
   */
  public SemQuery getSemQuery()
  {
    return semQuery;
  }

  /**
   * @param semQuery
   *          The semQuery to set.
   */
  public void setSemQuery(SemQuery semQuery)
  {
    // check whether write lock for this object has been acquired or not.
    assert isWriteable() == true;
    
    this.semQuery = semQuery;
    if(semQuery != null)
    {
      isDependentOnArchivedReln = semQuery.isDependentOnArchivedRelation();
      isDependentOnPartnStream = semQuery.isDependentOnPartnStream();
    }
  }

  /**
   * @return Returns the logical plan.
   */
  public LogOpt getLogPlan()
  {
    return logOpt;
  }

  /**
   * @param PhyOpt
   *          The logical plan to set.
   */
  public void setLogPlan(LogOpt logOpt)
  {
    // check whether write lock for this object has been acquired or not.
    assert isWriteable() == true;

    this.logOpt = logOpt;
  }

  public String[] getNames()
  {
    return names;
  }

  public void setNames()
  {
    // check whether write lock for this object has been acquired or not.
    assert isWriteable() == true;
    assert semQuery != null;

    ArrayList<Expr> projExprs = semQuery.getSelectListExprs();
    int numProjExprs = semQuery.getSelectListSize();

    names = new String[numProjExprs];
    types = new Datatype[numProjExprs];
    
    for(int i = 0;i < numProjExprs; i++)
    {
      names[i] = projExprs.get(i).getName();
      types[i] = projExprs.get(i).getReturnType();
    }
  }

  public Datatype[] getTypes()
  {
    return types;
  }

  /**
   * @return whether relnId is present or not
   */
  public boolean isRefRelnPresent(int id)
  {
    Iterator<IdStub> iter = refRelns.iterator();

    while (iter.hasNext())
    {
      IdStub elem = iter.next();

      if (elem.getRelnId() == id)
        return true;
    }

    return false;
  }

  /**
   * @return number of readers for the specified relnId
   * @throws MetadataException
   */
  public BitSet getListReaders(int id) throws MetadataException
  {
    Iterator<IdStub> iter = refRelns.iterator();

    while (iter.hasNext())
    {
      IdStub elem = iter.next();

      if (elem.getRelnId() == id)
        return elem.getReaders();
    }

    throw new MetadataException(MetadataError.RELATION_NOT_FOUND, new Object[]{id});
  }

  /**
   * @return Returns the refRelns.
   */
  public List<IdStub> getRefRelns()
  {
    return refRelns;
  }

  /**
   * @param refRelnId
   *          The identifier of the view to add it to the list
   */
  public void addRefReln(int relnId, BitSet readers)
  {
    //  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    if(!isRefRelnPresent(relnId))
      refRelns.add(new IdStub(relnId, readers));
  }

  /**
   * @return Returns the external destinations.
   */
  public List<Destination> getExtDests()
  {
    return extDests;
  }
  
  public void removeDestination(Destination epr)
  {
    assert isWriteable() == true;
    
    Iterator<Destination> itr = extDests.iterator();
    while(itr.hasNext())
    {
      Destination dest = itr.next();
      if(dest.equals(epr))
      {
        itr.remove();
        break;
      }
    }
  }

  /**
   * @param epr
   *          The EPR of the destination to be added
   */
  public void addExtDest(Destination epr)
    throws MetadataException
  {
    //  check whether write lock has been acquired or not.
    assert isWriteable() == true;

    // Condition: If Given EPR has isUpdateSemantic = true; then
    // query should have a primary key defined over its output columns
    if(epr.getIsUpdateSemantics() && !(isPrimaryKeyExist))
      throw new MetadataException(MetadataError.PRIMARY_KEY_NOT_DEFINED, new Object[]{this.getName()});
    
    extDests.add(epr);
  }

  /**
   * Setter for destination for a Query
   * 
   * @param dest
   *          The destination to set.
   */
  public void addQueryDestination(Destination dest) throws MetadataException
  {
    try 
    {
      XMLDocument doc = XMLHelper.verifyDocument(dest.getExtDest());
      dest.setDestId(lastDestId.incrementAndGet());
      addExtDest(dest);
    }
    catch(CEPException e)
    {
      throw new MetadataException(MetadataError.INVALID_XML_ERROR);
    }
  }

  public MetadataDescriptor allocateDescriptor()
  {
      TableMetadataDescriptor desc = new TableMetadataDescriptor(this.getName(),"QUERY");
      desc.setRemarkText(this.getText());
      return desc;
  }
  
  /**
   * Get OutputConstraints for this query
   * @return
   */
  public LinkedList<String> getOutputConstraintAttrs()
  {
    return this.outputConstraintAttrs;
  }
  
  /**
   * Set OutputConstraint Attributes for this query
   * @param outputConstraintAttrs
   */
  public void setOutputConstraintAttrs(String[] outputConstraintAttrs)
  {
    assert this.isWriteable() == true;
    this.outputConstraintAttrs.clear();
    for(int i= 0; i < outputConstraintAttrs.length ; i++)
    {
      this.outputConstraintAttrs.add(outputConstraintAttrs[i]);
    }
  }
  
  /**
   * Set Primary key flag
   * @param isPrimaryKeyExist
   */
  public void setIsPrimaryKeyExist(boolean isPrimaryKeyExist)
  {
    assert this.isWriteable() == true;
    this.isPrimaryKeyExist = isPrimaryKeyExist;
  }
  
  /**
   * Check whether primary key exists
   * @return
   */
  public boolean getIsPrimaryKeyExist()
  {
    return this.isPrimaryKeyExist;
  }

  public String getRName()
  {
    return (getIsNamed()) ? getName() : "";
  }


  /**
   * @return the user specified ordering constraint
   */
  public OrderingKind getUserOrderingConstraint()
  {
    return userOrderingConstraint;
  }


  /**
   * Setter for the user specified ordering constraint
   * @param orderingConstraint the value to be set
   */
  public void setUserOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.userOrderingConstraint = orderingConstraint;
  }

  /**
   * @return the system determined ordering constraint
   */
  public OrderingKind getSystemOrderingConstraint()
  {
    return systemOrderingConstraint;
  }


  /**
   * Setter for the system determined ordering constraint
   * @param orderingConstraint the value to be set
   */
  public void setSystemOrderingConstraint(OrderingKind orderingConstraint)
  {
    this.systemOrderingConstraint = orderingConstraint;
  }

  /**
   * @return the partition parallel expression parser node
   */
  public CEPExprNode getPartitionParallelExprNode()
  {
    return partitionParallelExprNode;
  }

  /**
   * @param partitionParallelExprNode
   *                    the partition parallel expression parser node
   */
  public void setPartitionParallelExprNode(CEPExprNode
                                           partitionParallelExprNode)
  {
    this.partitionParallelExprNode = partitionParallelExprNode;
  }

  public boolean isDependentOnPartnStream()
  {
    return this.isDependentOnPartnStream;
  }  

  public boolean isDependentOnArchivedRelation()
  {
    return this.isDependentOnArchivedReln;
  }

  public void setQueryStartTime(long startTime)
  {
    this.startTime = startTime;
  }

  public long getQueryStartTime()
  {
    return this.startTime;
  }
  
  public void setIsViewQuery(boolean val)
  {
    this.isViewQuery = val;
  }
  
  public boolean isViewQuery()
  {
    return this.isViewQuery;
  }
  
  public void setDynamicTypeSystem(DynamicTypeSystem dynamicType)
  {
    this.dynamicTypeSystem = dynamicType;
    this.dynamicTypeNames = new ArrayList<String>();
  }
  
  public void addDynamicTypeName(String dynamicTypeName)
  {
    if(this.dynamicTypeSystem != null && this.dynamicTypeNames != null)
    {
      this.dynamicTypeNames.add(dynamicTypeName);
    }
  }
  
  //If the query has the dynamic type meta data then this method 
  //can be used to release those dynamic types during drop query
  public void releaseDynamicTypes()
  {
    if(this.dynamicTypeSystem != null && this.dynamicTypeNames != null)
    {
      for(String typeName : this.dynamicTypeNames)
        this.dynamicTypeSystem.dropType(typeName);
    }
  }
  
  public void setArchiverStats(ArchiverStats archStats)
  {
    this.archiverStats = archStats;
  }
  
  public ArchiverStats getArchiverStats()
  {
    return this.archiverStats;
  }
}
