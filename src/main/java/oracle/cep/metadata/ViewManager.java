/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ViewManager.java /main/44 2014/10/14 06:35:34 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi   09/23/14 - support for partitioned stream
 udeshmuk   08/31/12 - archived view handling
 udeshmuk   06/05/12 - mark view as archived source if the defining query is
                       dependent on archived sources
 alealves   10/28/11 - XbranchMerge alealves_bug-12630784_ps5 from
                       st_pcbpel_11.1.1.4.0
 anasrini   03/29/11 - set view creation DDL text
 sborah     03/17/11 - add ordering constraint expr
 sborah     03/15/11 - add ordering constraint to view
 parujain   09/24/09 - dependency support
 sborah     07/10/09 - support for bigdecimal
 parujain   02/13/09 - get types
 parujain   01/28/09 - transaction mgmt
 parujain   01/07/09 - redesign force stop/drop
 sborah     01/06/09 - handle constants.
 skmishra   12/29/08 - adding isValidate to addQuery API
 sborah     12/22/08 - support for view without schema
 parujain   11/24/08 - redesign drop
 hopark     12/02/08 - move LogLevelManager to ExecContext
 hopark     10/09/08 - remove statics
 hopark     10/07/08 - use execContext to remove statics
 parujain   10/01/08 - drop schema support
 parujain   09/12/08 - multiple schema support and drop view bug fix
 parujain   09/04/08 - maintain offset
 sbishnoi   08/18/08 - 
 sborah     08/11/08 - correcting spelling
 sborah     08/07/08 - modifying VIEW_ATTRIBUTE_NUMBER_MISMATCH error message
 hopark     06/18/08 - logging refactor
 sbishnoi   06/11/08 - modified way to retrieve view query text in parsing time
 parujain   05/21/08 - 
 hopark     03/26/08 - server reorg
 parujain   05/05/08 - lock problem
 parujain   04/29/08 - 
 parujain   05/07/08 - fix problems
 parujain   05/05/08 - lock problem
 parujain   04/29/08 - 
 sbishnoi   03/24/08 - modifying LogUtil.trace for dropView
 mthatte    02/26/08 - parametrizing metadata errors
 parujain   02/07/08 - parameterizing error
 hopark     02/05/08 - fix dump level
 hopark     01/08/08 - metadata logging
 parujain   11/09/07 - External source
 mthatte    10/26/07 - support for onDemand reln
 mthatte    11/08/07 - adding getViewByName()
 mthatte    07/17/07 - bug 6206009
 parujain   06/21/07 - fix locks
 parujain   05/18/07 - bug fix
 parujain   04/13/07 - runtime exception handling
 hopark     03/21/07 - storage re-org
 parujain   01/11/07 - BDB integration
 parujain   11/27/06 - Locks mgmt during drop Table
 dlenkov    09/13/06 - drop view
 parujain   09/05/06 - bug 5461058
 dlenkov    08/30/06 - query references
 parujain   08/18/06 - support views with same query
 parujain   07/14/06 - check locks 
 parujain   07/10/06 - Namespace Implementation 
 parujain   06/28/06 - metadata cleanup
 najain     05/17/06 - view support 
 najain     05/15/06 - more methods 
 najain     05/09/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/ViewManager.java /main/44 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.HashMap;
import java.util.ArrayList;

import oracle.cep.common.Constants;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.parser.CEPAttrSpecNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.CEPQueryRefNode;
import oracle.cep.parser.CEPQueryRefKind;
import oracle.cep.parser.CEPViewDefnNode;
import oracle.cep.parser.CEPViewOrderingConstraintNode;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.common.OrderingKind;
import oracle.cep.semantic.Expr;
import oracle.cep.service.ExecContext;
import oracle.cep.storage.IStorageContext;
import oracle.cep.transaction.ITransaction;
import static oracle.cep.common.Constants.CQL_THIS_POINTER;

/**
 * This class manages the system metadata related to the views registered with
 * the system
 * 
 * @since 1.0
 */
@DumpDesc(evPinLevel=LogLevel.MVIEW_ARG,
          dumpLevel=LogLevel.MVIEW_INFO,
          verboseDumpLevel=LogLevel.MVIEW_LOCKINFO)
public class ViewManager extends SourceManager
implements ILoggable
{
  public ViewManager(ExecContext ec, Cache cache)
  {
     super(ec, cache);
  }

  /**
   * Register a new view in the system.
   * @param cql
   *                cql text
   * @param s
   *                Definition
   * 
   * @return Id
   * @throws CEPException
   */
  public int registerView(String cql, String schema, CEPViewDefnNode vdn)
    throws CEPException
  {
    int viewId;
    CEPQueryNode qNode = vdn.getQueryNode();
    int qid = -1;
    Datatype[] queryOutTypes;
    String viewName = vdn.getName();
    
    LogLevelManager.trace(LogArea.METADATA_VIEW, LogEvent.MVIEW_CREATE, this,
                          cql, vdn.getName());

    ITransaction txn = execContext.getTransaction();
    
    CacheLock l = null;
        
    if (qNode instanceof CEPQueryRefNode) {
 
      CEPQueryRefNode rNode = (CEPQueryRefNode)qNode;
      assert (rNode.getKind() == CEPQueryRefKind.VIEW);
  
      try{
      qid = execContext.getQueryMgr().findQuery( rNode.getName(), schema);
      }catch(CEPException e)
      {
        e.setStartOffset(rNode.getStartOffset());
        e.setEndOffset(rNode.getEndOffset());
        throw e;
      }
    }
    else 
    {   
      //NOTE: Use ViewDefinitionNode to fetch query text
      String qryTxt = vdn.getQueryTxt();
         
      // Register the query associated with the view
      try
      {
        String qname = Constants.CQL_RESERVED_PREFIX + viewName;
        qid = execContext.getQueryMgr().addNamedQuery(qname, qryTxt, schema,
                                                      qNode);
      }
      catch (MetadataException e)
      {
       if(e.getErrorCode() != MetadataError.QUERY_ALREADY_EXISTS) 
         throw (e);
       else
       {
      	try{
           qid = execContext.getQueryMgr().findQuery( qryTxt, schema);
       	}catch(CEPException ce)
       	{
       	  ce.setStartOffset(qNode.getStartOffset());
       	  ce.setEndOffset(qNode.getEndOffset());
      	  throw ce;
      	}
       }
      }
    }
 
    // If the user passed in the attribute datatypes, make sure they match
    CEPAttrSpecNode[] attrSpecList = vdn.getAttrSpecList();
    String[] attrNameList = vdn.getAttrNameList();
 
    try {
      queryOutTypes = execContext.getQueryMgr().getOutTypes( qid);
    }catch(CEPException e)
    {
      e.setStartOffset(qNode.getStartOffset());
      e.setEndOffset(qNode.getEndOffset());
      throw e;
    }
    
    String [] outAttrNameList = execContext.getQueryMgr().getOutNames(qid);

    // Check if _this pointer is present in the out names AND not present in attrList.
    //  If yes, then we must add it as the first attribute of the view,
    //  and only then make sure the attributes are matching properly.
    boolean addThisAttribute = false;
  
    if (attrSpecList != null)
    {
      /*
       * The user specified the view in the following way: create view V(s1
       * int, s2 int, s3 char(20)...) as select .... from S,R....
       * 
       * However, currently we do not support the last syntax where an
       * conversion can be done. This can be supported later.
       */
      int len = attrSpecList.length;
      int outTypeOffset = 0;
      
      if (outAttrNameList[0].endsWith(CQL_THIS_POINTER) && // S._this 
          (!attrSpecList[0].getName().equals(CQL_THIS_POINTER))) 
      {
        outTypeOffset = 1;
        addThisAttribute = true;
      }
      
      assert attrNameList == null;
 
      if ((len + outTypeOffset) != queryOutTypes.length)
        throw new MetadataException(
            MetadataError.VIEW_ATTRIBUTE_NUMBER_MISMATCH,
            vdn.getStartOffset(), vdn.getEndOffset(),
            new Object[]{len, queryOutTypes.length,vdn.getName()});
  
      for (int i = 0; i < len; i++)
      {
        CEPAttrSpecNode attr = attrSpecList[i];
        if (!(attr.getDatatype().isAssignableFrom(queryOutTypes[i + outTypeOffset])))
          throw new MetadataException(
              MetadataError.VIEW_ATTRIBUTE_DATATYPE_MISMATCH, 
              attr.getStartOffset(), attr.getEndOffset(),
              new Object[]{queryOutTypes[i + outTypeOffset].toString(),
                           attr.getDatatype().toString()});
      }
    }
    else if (attrNameList != null)
    {
      /*
       * The user created the view in the following way: create view V(s1, s2,
       * ....) as select .... from S,R...
       */
      int len = attrNameList.length;
      
      if (outAttrNameList[0].endsWith(CQL_THIS_POINTER) && // S._this 
          (!attrNameList[0].equals(CQL_THIS_POINTER))) 
      {
        len++;
        addThisAttribute = true;
      }
 
      if (len != queryOutTypes.length)
        throw new MetadataException(
            MetadataError.VIEW_ATTRIBUTE_NUMBER_MISMATCH,
            vdn.getStartOffset(), vdn.getEndOffset(),
            new Object[]{len, queryOutTypes.length,vdn.getName()});
    }
    /*
     * Else the user created the view in the following way: create view V as
     * select .... from S,R...This is allowed with the condition that each
     * of the exprs in the predicate list has an alias name.
     */
    else 
    {
      attrNameList = outAttrNameList;
      ArrayList<Expr> exprList;
      try
      {
        exprList = execContext.getQueryMgr().getQueryExprs(qid);
      }
      catch(CEPException e)
      {
        e.setStartOffset(qNode.getStartOffset());
        e.setEndOffset(qNode.getEndOffset());
        throw e;
      }

      for(int i = 0; i < exprList.size(); i++)
      {
        if(exprList.get(i).getExprType() != 
           oracle.cep.semantic.ExprType.E_ATTR_REF)
        {            
          if(!exprList.get(i).isUserSpecifiedName())
          {
            throw new MetadataException(MetadataError.ALIAS_NAME_REQUIRED,
                      vdn.getStartOffset(), vdn.getEndOffset(),
                      new Object[]{vdn.getName(), i + 1});
          }
        }
        else
        {
          /**
           * In case the view is created in the following way :
           * create view v1 as select a , b from S
           * then the attribute names need to be changed from S.a , S.b
           * to a and b respectively
           * The algorithm works even in case of statements like 
           * create view v1 as a as First , b as Second from S.
           */
          String aliasName = exprList.get(i).getName();
          aliasName = aliasName.substring(aliasName.lastIndexOf('.') + 1);
          attrNameList[i] = aliasName;
        }
      }
    }

    
    // Check for duplicate view name and create
    // l will be null if object with same name existed
    // in the namespace and schema

    l = createObject(txn, viewName, schema, CacheObjectType.VIEW, null);

    if (l == null)
    {
      throw new MetadataException(MetadataError.VIEW_ALREADY_EXISTS,
                                  vdn.getStartOffset(), vdn.getEndOffset(),
                                  new Object[] {viewName});
    }

    // Initialize
    View view = (View) l.getObj();
    try{
      view.setBStream(execContext.getQueryMgr().isStreamQuery(qid));
    }catch(CEPException e)
    {
      e.setStartOffset(vdn.getStartOffset());
      e.setEndOffset(vdn.getEndOffset());
      throw e;
    }
    view.setQueryId(qid);    
    viewId = view.getId();
    view.setExternal(false);
    view.setDegreeOfParallelism(
        execContext.getServiceManager().getConfigMgr().getDegreeOfParallelism());

    // Set the creation DDL text
    view.setCql(cql);
     
    if (attrSpecList != null)
    {
      if (addThisAttribute)
      {
        addThisAttribute(view, queryOutTypes[0], vdn);
      }
      
      for (int i = 0; i < attrSpecList.length; i++)
      {
        CEPAttrSpecNode attr = attrSpecList[i];
        Attribute a = new Attribute(attr.getName(),
                                    attr.getAttributeMetadata());
        try{
          view.addAttribute(a);
        }catch(MetadataException me)
        {
          me.setStartOffset(attr.getStartOffset());
          me.setEndOffset(attr.getEndOffset());
          throw me;
        }
      }
    }
    else if (attrNameList != null)
    {
      int outTypeOffset = 0;
      
      if (addThisAttribute)
      {
        addThisAttribute(view, queryOutTypes[outTypeOffset++], vdn);
      }
      
      for (int i = 0; i < attrNameList.length; i++)
      {
        String nm = attrNameList[i];
        // TODO:: Pass the length as 0 for now
        // TODO : Pass default precision and scale of 0
        Attribute a = new Attribute(nm, new AttributeMetadata(queryOutTypes[i + outTypeOffset],
                           0, queryOutTypes[i + outTypeOffset].getPrecision(), 0));
        try{
          view.addAttribute(a);
        }catch(MetadataException me)
        {
          me.setStartOffset(vdn.getStartOffset());
          me.setEndOffset(vdn.getEndOffset());
          throw me;
        }
      }
    }
      
    // View is dependent on query, so query is the master, view is dependent
    execContext.getDependencyMgr().addDependency(qid, viewId,
                   DependencyType.QUERY, DependencyType.VIEW, schema);
      
    view.setViewState(ViewState.STOPPED);

    //mark the query as view defn query. primarily used in archived view framework.
    execContext.getQueryMgr().getQuery(qid).setIsViewQuery(true);
    
    boolean isArchived = vdn.isArchived();
    
    if(isArchived)
    {
      view.setIsArchived(true);
      //check that view query is dependent on archived relation
      if(!execContext.getQueryMgr().getQuery(qid).isDependentOnArchivedRelation())
        throw new MetadataException(MetadataError.QUERY_SHOULD_BE_ARCHIVED_DEPENDENT,
                                    new Object[] {vdn.getName()});
     
      //check if eventidcolname is specified and belongs to view schema
      String eidColName = vdn.getEventIdColName();
      
      if(eidColName != null)
      {
        Attribute idAttr = view.getAttribute(eidColName);
        if(idAttr.getType() != Datatype.BIGINT)
          throw new MetadataException(MetadataError.INCORRECT_TYPE_FOR_COLUMN,
                                      new Object[] {"event",eidColName});
      }
      view.setEventIdColName(eidColName);
    }
    else
    {
      //view is not archived
      if(execContext.getQueryMgr().getQuery(qid).isDependentOnArchivedRelation())
        throw new MetadataException(MetadataError.QUERY_SHOULD_NOT_BE_ARCHIVED_DEPENDENT,
                                    new Object[] {vdn.getName()});
      view.setIsArchived(false);
      view.setEventIdColName(null);
    }
  
    // Set a flag in View metadata whether the view query depends on a partitioned
    // stream
    boolean isDependentOnPartnStream 
      = execContext.getQueryMgr().getQuery(qid).isDependentOnPartnStream();
    view.setPartitioned(isDependentOnPartnStream);
    
    // If view depends on a partitioned stream, then set the ordering constraint
    // of view to be PARTITION ORDERED.
    // Please note that this should happen only when the view is registered by
    // a DDL invoked by user and not by an internal DDL from parallelism code.
    if(isDependentOnPartnStream && !execContext.isInternalDDL())
    {
      alterViewOrderingConstraint(execContext, viewName, schema, 
        OrderingKind.PARTITION_ORDERED, null, false); 
    }
    return viewId;
  }

  private void addThisAttribute(View view, Datatype thisType,
      CEPViewDefnNode vdn) throws MetadataException
  {
    Attribute a = new Attribute(CQL_THIS_POINTER, new AttributeMetadata(thisType,
        0, 0, 0));
    
    try
    {
      view.addAttribute(a);
    }
    catch(MetadataException me)
    {
      // REVIEW Would an exception here ever happen?
      me.setStartOffset(vdn.getStartOffset());
      me.setEndOffset(vdn.getEndOffset());
      throw me;
    }
  }
  
  
  public void alterViewOrderingConstraint(ExecContext ec, 
                                          CEPViewOrderingConstraintNode n,
                                          String schema) throws CEPException
  {
    try
    {
      alterViewOrderingConstraint(ec, n.getName(), schema,
                                  n.getOrderingConstraint(), 
                                  n.getParallelPartioningExpr(),
                                  true);
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }
  
  private void alterViewOrderingConstraint(ExecContext ec, String name,
                                           String schema,
                                           OrderingKind orderingConstraint,
                                           CEPExprNode parallelPartioningExpr,
                                           boolean isUserCommand) 
    throws MetadataException, CEPException
  {
      CacheLock l = null;
      View view;
      ITransaction txn = execContext.getTransaction();
   
      // Lock table
      l = findCache(txn, new Descriptor(name, CacheObjectType.VIEW, 
                                        schema, null), true);

      // If object not found throw the exception
      if (l == null)
      {
        throw new MetadataException(MetadataError.VIEW_NOT_FOUND,
            new Object[]{ name });
      }
      
      view = (View) l.getObj();
      
      // If the view is dependent on a partitioned source, then user is not 
      // allowed to alter the ordering constraint
      if(isUserCommand && view.isPartitioned())
      {
        throw new CEPException(MetadataError.CANNOT_ALTER_ORDERING_CONSTRAINT, 
                               name, orderingConstraint);
      }
      
      int queryId = view.getQueryId();
      ec.getQueryMgr().alterOrderingConstraint(queryId, orderingConstraint, 
                                               parallelPartioningExpr);
    }
  
  
  /**
   * Start a view as part of starting a destination query
   * 
   * @param vid
   *           Id of view getting started
   * @param force
   *           Always true since referred query is required to be started 
   *           without external destination
   * @throws CEPException
   */
  public void startView(int vid, boolean force) 
     throws CEPException
  {
    CacheLock l = null;
    View view;
    ITransaction txn = execContext.getTransaction();
    
    l = findCache(txn, vid, true,CacheObjectType.VIEW);
    if(l == null)
      throw new MetadataException(MetadataError.INVALID_VIEW_IDENTIFIER,
                                  new Object[]{vid});
      
    view = (View)l.getObj();
      
    LogLevelManager.trace
           (LogArea.METADATA_VIEW, LogEvent.MVIEW_START, this, view.getName());
      // View already started
    if(view.getViewState() == ViewState.STARTED)
    {
      return;
    }
      
     // View needs to be started 
     execContext.getQueryMgr().startQuery(view.getQueryId(), force);
      
     view.setViewState(ViewState.STARTED);
  }
  
  public void stopView(int vid)
    throws CEPException
  {
    CacheLock l = null;
    View view = null;
    ITransaction txn = execContext.getTransaction();

      l = findCache(txn, vid, true,CacheObjectType.VIEW);
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_VIEW_IDENTIFIER,
                                    new Object[]{vid});
 
      view = (View)l.getObj();
       
      LogLevelManager.trace(LogArea.METADATA_VIEW, LogEvent.MVIEW_STOP, this, view.getName());
      
      // Either the view was never started or is currently stopped
      if(view.getViewState() != ViewState.STARTED)
        return;
      
      view.setViewState(ViewState.STOPPED);
      
      // View Defining query
      int qid = view.getQueryId(); 
      // Query qid can be the defining query of other views as well
      Query q = execContext.getQueryMgr().getQuery(qid);
      Integer[] destViews = execContext.getDependencyMgr().
                            getDependents(q.getId(), DependencyType.VIEW);
   
      // Stop the query(if possible) only if it does not have ext destination
      // otherwise defining query should be stopped independently
      if(q.getExtDests().isEmpty())
      {
        boolean allViewsStopped = true;
        if(destViews != null){
          int i =0;
          while(i<destViews.length && allViewsStopped)
          { // If the view is in started stated then it has not been stopped
            // else if in either created or stopped then its not running
            if(getView(destViews[i]).getViewState() == ViewState.STARTED)
              allViewsStopped = false;
            i++;
          }
        }
        if(allViewsStopped)
        {
          execContext.getQueryMgr().stopQueryInternal(qid);
        }
      }
  }

  /**
   * Drop a view from the system.
   * @param vName
   *                view name
   * 
   * @throws CEPException
   */
  public void dropView( String vName, String schema)
      throws CEPException {

    CacheLock l = null;
    Locks locks = null;
    ITransaction txn = execContext.getTransaction();
    
    LogLevelManager.trace(LogArea.METADATA_VIEW, LogEvent.MVIEW_DELETE, this, vName);

    l = findCache(txn, new Descriptor(
                  vName, CacheObjectType.VIEW, schema, null), false);
       
    if( l== null)
      throw new MetadataException( MetadataError.VIEW_NOT_FOUND,
                                   new Object[]{vName});
  
    View v = (View)l.getObj();
    Integer[] destQueries = execContext.getDependencyMgr().
                                getDependents(v.getId(), DependencyType.QUERY);
    if ((destQueries != null) && (destQueries.length > 0))
    {
      release(txn,l);
      throw new MetadataException( MetadataError.CANNOT_DROP_VIEW,
                                   new Object[]{vName});
    }
  
    int qId = v.getQueryId();
    int vId = l.getObj().getId();
    release(txn, l);
  
    l = null;
    l = findCache(txn, qId, true, CacheObjectType.QUERY);
    if (l == null)
      throw new MetadataException( MetadataError.INVALID_QUERY_IDENTIFIER,
                                   new Object[]{qId});
      
    Query q = (Query) l.getObj();

    boolean isNamed = q.getIsNamed();
      
    execContext.getDependencyMgr().removeAllDependencies(vId, 
                                        DependencyType.VIEW, schema);
  
    if (!isNamed)
    {
      try{
        // If the unnamed queries is being referenced by multiple views
        // then remove from the list of destination queryids
        if(!execContext.getDependencyMgr().isAnyDependentPresent(qId, 
                                                          DependencyType.VIEW))
        {
          execContext.getQueryMgr().dropQuery(qId);
        }
      }catch(CEPException ce)
      {
        if(ce.getErrorCode() != MetadataError.VIEW_DESTINATION_EXISTS)
        {
          throw ce;
        }
      }
    }
        
  
    l = null;
    locks = deleteCache(txn, vId);
    if (locks == null)
      throw new MetadataException( MetadataError.VIEW_NOT_FOUND,
                                   new Object[]{vName});
 
    l = locks.objLock;
   
  }
  
  
  /**
   * Sets the Query id by first finding the view and then setting the query id
   * 
   * @param vid
   *                View id
   * @param qid
   *                Query id
   * @throws MetadataException
   *                 if the View is not found
   */
  public void setQueryId( int vid, int qid) throws MetadataException
  {
    CacheLock l = null;
    View view = null;
    ITransaction txn = execContext.getTransaction();
    
// l is null if already an object with same name existed in namespace and schema
    l = findCache(txn, vid, true,CacheObjectType.VIEW);
    if(l == null)
      throw new MetadataException(MetadataError.INVALID_VIEW_IDENTIFIER,
                                  new Object[]{vid});
      
    view = (View)l.getObj();
    view.setQueryId(qid);
  
  }
  
    
  public View getView(int id) throws MetadataException
  {
    CacheLock l = null;
    View view = null;
    ITransaction txn = execContext.getTransaction();
    
    try
    {
      // l is null if already an object with same name existed in namespace and schema
        l = findCache(txn, id, false,CacheObjectType.VIEW);
        if(l == null)
           throw new MetadataException(MetadataError.INVALID_VIEW_IDENTIFIER,
                                       new Object[]{id});
      
        view = (View)l.getObj();
    
    }
    finally
    {
      if (l != null)
        release(txn, l);
        
    }

    return view;
  }

  /**
   * Get view Id
   * 
   * @param name
   *                View name
   * @return Id
   * @throws MetadataException
   */
  public int getViewId(String name, String schema)
      throws MetadataException
  {
    CacheLock l = null;
    View view;
    int viewId;
    ITransaction txn = execContext.getTransaction();
    try
    {
// l is null if already an object with same name existed in namespace and schema
      l = findCache(txn, new Descriptor
                    (name,CacheObjectType.VIEW,schema,null),false);
    
      if(l == null)
        throw new MetadataException(MetadataError.VIEW_NOT_FOUND, new Object[]{name});

      // Get view id
      view = (View) l.getObj();
      viewId = view.getId();
    }
    finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }

    return viewId;
  }

  /**
   * Get the Logical plan for query corresponding to this view
   *
   * @param vid the view identifier
   * @return The logical plan for query corresponding to this view
   */
  public LogOpt getLogPlan(int vid) throws CEPException
  {
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    
    try 
    {
      l = findCache(txn, vid, false, CacheObjectType.VIEW);
      if(l == null)
        throw new MetadataException(MetadataError.INVALID_VIEW_IDENTIFIER,
                                    new Object[]{vid});
      
      View view = (View)l.getObj();

      // Get the logical plan from the corresponding query
      LogOpt lopt =
        execContext.getQueryMgr().getLogPlan(view.getQueryId());
      
      return lopt;
    }
    finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }
  }

  public HashMap<String, ViewInfo> getViewInfo()
  {
    HashMap<String, ViewInfo> map=
                                 new HashMap<String, ViewInfo>();
    IStorageContext storageCtx = cache.initQuery(NameSpace.SOURCE.toString(), 
                                                 execContext.getSchema());
    CacheObject record = null;
    while(true)
    {
      record = (CacheObject)storage.getNextRecord(storageCtx);
      if(record == null)
        break;
      if(record.getType() == CacheObjectType.VIEW)
      {
        View v = (View)record;
        ViewInfo info = new ViewInfo(v.getAttributes(), v.isBStream(),
                                    (v.getViewState() == ViewState.STARTED));
        map.put(v.getName(), info);
      }
    }
    return map;
  }

  
  public View getViewByName(String viewName, String schema) throws MetadataException
  {

    CacheLock l = null;
    View v;
    ITransaction txn = execContext.getTransaction();
    try {

      l = findCache(txn, new Descriptor(
                        viewName, CacheObjectType.VIEW, schema, null), false);
      

      if( l== null) {
          // For now don't care if it is a table or a relation.
          // Only need to know whether it is found or not
          throw new MetadataException(MetadataError.VIEW_NOT_FOUND, new Object[]{viewName});
      }
      v = (View)l.getObj();
    }
    
    catch(MetadataException me) {
        LogUtil.info(LoggerType.TRACE,"Could not find table!");
        return null;
    }
    finally
    {
      if(l != null)
        release(txn, l);
    }
    
      return v;
  }

  public int getTargetId() {
    return 0;
  }
  
  public String getTargetName() {
    return "ViewManager";
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
    // MVIEW_INFO - dumps using the fields specified in DumpDesc annotation
    // MVIEW_LOCKINFO - handled by overriden dump method in this class
  }
  
  public void dump(IDumpContext dumper) 
  {
    super.dump(dumper, LogTags.TAG_VIEWS, CacheObjectType.VIEW);
  }
    
}

