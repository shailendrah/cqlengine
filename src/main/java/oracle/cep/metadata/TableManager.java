/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/TableManager.java /main/55 2014/10/14 06:35:32 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 This class manages the system metadata related to the streams and 
 relations registered with the system

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    09/17/14 - adding a flag to check if input stream or relation is
                        partitioned at source
 udeshmuk    09/09/14 - put isParttioned in stream metadata
 sbishnoi    04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                        st_pcbpel_11.1.1.4.0
 sbishnoi    02/18/13 - allow disable heartbeat timeout
 vikshukl    07/30/12 - archived dimension table
 udeshmuk    04/03/12 - set workerid and txnid in metadata
 udeshmuk    01/08/12 - set timestamp col name and replay clause in metadata
                        for archived stream
 udeshmuk    08/24/11 - propagate archived rel event id column name
 alealves    08/04/11 - XbranchMerge alealves_bug-12791498_cep from main
 anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini    03/29/11 - call addQuery with name
 sborah      03/17/11 - add parallelism degree
 udeshmuk    03/02/11 - support for archived relation.
 sborah      07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                        st_pcbpel_11.1.1.4.0
 sborah      07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
 sborah      06/23/10 - set max rows in external relations
 sbishnoi    06/22/10 - adding method to get threshold value
 parujain    10/02/09 - dependency support
 parujain    09/24/09 - dependency support
 sborah      07/10/09 - support for bigdecimal
 anasrini    02/10/09 - push source now testing
 parujain    01/28/09 - transaction mgmt
 skmishra    12/29/08 - adding isValidate to addQuery API
 hopark      12/02/08 - move LogLevelManager to ExecContext
 sborah      11/24/08 - support for altering base timeline
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    09/12/08 - multiple schema support
 parujain    09/04/08 - offset info
 sbishnoi    07/23/08 - restructuring code and adding comment for derived ts
 hopark      06/18/08 - logging refactor
 hopark      03/17/08 - config reorg
 parujain    05/07/08 - fix lock problem
 udeshmuk    04/27/08 - parameterize error.
 parujain    04/18/08 - qsrc stats
 hopark      04/18/08 - 
 parujain    04/16/08 - modify ispushsrc stats
 hopark      04/16/08 - add qsrc stat
 mthatte     04/09/08 - adding error message for invalid timestamp
 mthatte     04/01/08 - 
 parujain    03/04/08 - functional timestamp
 mthatte     03/19/08 - adding isClientTimeStamped
 mthatte     02/26/08 - parametrizing metadata errors
 parujain    02/07/08 - parameterizing error
 hopark      02/05/08 - fix dump level
 hopark      01/08/08 - metadata logging
 udeshmuk    12/16/07 - propagate timeout field.
 parujain    11/09/07 - External source
 parujain    10/25/07 - db join
 udeshmuk    11/18/07 - set isSystemTimestamped in registerStrmReln.
 sbishnoi    10/28/07 - support for primary key
 mthatte     10/18/07 - removing println
 parujain    10/17/07 - cep-bam integration
 mthatte     10/11/07 - adding isSystemTime()
 parujain    09/25/07 - push source without xml file
 udeshmuk    10/01/07 - bug fix for alter stream/relation statement.
 parujain    09/25/07 - push source without xml file
 mthatte     09/11/07 - Adding getTableByName()
 parujain    06/20/07 - lock not released drop table
 hopark      05/11/07 - remove System.out.println(use java.util.logging instead)
 parujain    05/08/07 - stats enabled/disabled
 parujain    04/26/07 - Stream/Relation creation text
 parujain    02/09/07 - System startup
 sbishnoi    02/06/07 - modify exception constructor
 parujain    01/11/07 - BDB integration
 dlenkov     01/17/07 - fix for push source
 parujain    12/07/06 - Fix systemtimestamp
 dlenkov     12/01/06 - added set silent DDL
 parujain    11/27/06 - Locks mgmt during drop Table
 najain      10/24/06 - integrate with mds
 parujain    10/23/06 - Silent relations
 dlenkov     09/05/06 - drop relation or stream
 najain      08/21/06 - add push source
 parujain    07/10/06 - Namespace Implementation 
 parujain    06/29/06 - metadata cleanup 
 najain      05/15/06 - support views 
 najain      04/06/06 - cleanup
 skaluska    03/22/06 - add table source 
 skaluska    03/16/06 - implementation
 najain      03/08/06 - code cleanup
 anasrini    02/27/06 - Fix NPE issues 
 najain      02/23/06 - minor issues
 anasrini    02/22/06 - add method getAttrtype 
 anasrini    02/21/06 - add getTableId method 
 najain      02/14/06 - minor changes 
 najain      02/13/06 - fill the empty class
 anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/TableManager.java /main/55 2014/10/14 06:35:32 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.metadata;

import java.util.logging.Level;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.RangeConverter;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.interfaces.InterfaceException;
import oracle.cep.interfaces.input.QueueSource;
import oracle.cep.interfaces.input.QueueSourceStat;
import oracle.cep.interfaces.input.TableSource;
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
import oracle.cep.metadata.cache.Cache;
import oracle.cep.metadata.cache.CacheLock;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.metadata.cache.Descriptor;
import oracle.cep.parser.CEPAddPushSourceNode;
import oracle.cep.parser.CEPAddTableSourceNode;
import oracle.cep.parser.CEPAttrSpecNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPRelOrStreamRefNode;
import oracle.cep.parser.CEPSFWQueryNode;
import oracle.cep.parser.CEPSetParallelismDegreeNode;
import oracle.cep.parser.CEPSetSilentNode;
import oracle.cep.parser.CEPTableDefnNode;
import oracle.cep.parser.CEPTableMonitorNode;
import oracle.cep.parser.CEPTimeSpecNode;
import oracle.cep.service.ExecContext;
import oracle.cep.transaction.ITransaction;

/**
 * This class manages the system metadata related to the streams and relations
 * registered with the system
 * 
 * @since 1.0
 */
@DumpDesc(evPinLevel = LogLevel.MTABLE_ARG, dumpLevel = LogLevel.MTABLE_INFO, verboseDumpLevel = LogLevel.MTABLE_LOCKINFO)
public class TableManager extends SourceManager implements ILoggable
{
  /**
   * Constructor for TableManager. The constructor has been kept private
   * intentionally so that no-one can create a new instance of a TableManager.
   * This way, only a single instance of the TableManager is present, and it can
   * be accessed globally via TableManager.getTableManager().
   */
  public TableManager(ExecContext ec, Cache cache)
  {
    super(ec, cache);
  }

  public void init(ConfigManager cfg)
  {
  }

  /**
   * Register a new stream in the system. Each stream is identified internally
   * in the system by a unique integer identifier, which is returned by this
   * function call.
   * @param t
   *                Stream definition
   * @param cql
   *                Stream creation text
   * 
   * @return Stream id
   * @throws MetadataException
   */
  public int registerStream(CEPTableDefnNode t, String cql, String schema)
      throws MetadataException
  {
    return registerStrmReln(t, true, cql, schema);
  }

  /**
   * Register a new relation in the system. Each relation is identified
   * internally in the system by a unique integer identifier, which is returned
   * by this function call.
   * @param s
   *                Relation definition
   * @param cql
   *                Relation creation text
   * 
   * @return Relation id
   * @throws MetadataException
   */
  public int registerRelation(CEPTableDefnNode s, String cql, String schema)
      throws MetadataException, CEPException
  {
    return registerStrmReln(s, false, cql, schema);
  }

  /**
   * Register a new stream/relation in the system.
   * @param s
   *                Definition
   * @param isStream
   *                true if stream
   * 
   * @return Id
   * @throws MetadataException
   */
  private int registerStrmReln(CEPTableDefnNode s, boolean isStream, String cql, 
		     String schema)
      throws MetadataException
  {
    CacheLock l = null;
    Table table;
    int tableId = -1;
    String streamName = s.getName();
    ITransaction txn = execContext.getTransaction();
    
    LogLevelManager.trace(LogArea.METADATA_TABLE, LogEvent.MTABLE_CREATE, 
                          this, cql, isStream, s.getName());

    // Create the object
    // l will be null if there is already an object with same name in the
    // namespace
    l = createObject(txn, streamName, schema, CacheObjectType.TABLE, null);
    
    // Check for duplicate table/view name and create
    if (l == null)
    {
      throw new MetadataException(MetadataError.TABLE_ALREADY_EXISTS,
          s.getStartOffset(), s.getEndOffset(),
          new Object[] { streamName });
    }
      
    // Initialize
    table = (Table) l.getObj();
    table.setBStream(isStream);
    table.setIsSilent(s.getIsSilent());
    table.setIsSystemTimeStamped(s.getIsSystemTimestamped());
    table.setIsArchived(s.isArchived());
    table.setIsDimension(s.isDimension());
       
    table.setDegreeOfParallelism(
        execContext.getServiceManager().getConfigMgr().getDegreeOfParallelism());

    // Set whether the stream is defined as partitioned using DDL as 
    // "CREATE PARTITIONED STREAM.."
    if(isStream)
      table.setPartitioned(s.isPartitioned());
      
    if(s.isArchived())
    {
      table.setArchiverName(s.getArchiverName());
      table.setEntityName(s.getEntityName());
    }
      
    table.setCql(cql);
    table.setExternal(s.isExternal());
    for (int i = 0; i < s.getAttrSpecList().length; i++)
    {
      CEPAttrSpecNode attr = s.getAttrSpecList()[i];
       
      Attribute a = new Attribute(attr.getName(), attr.getAttributeMetadata());
      try 
      {
        table.addAttribute(a);
      }catch(MetadataException me)
      {
        me.setStartOffset(attr.getStartOffset());
        me.setEndOffset(attr.getEndOffset());
        throw me;
      }
    }
    // Add Primary key Attributes
    if (s.getIsPrimaryKeyExist() && !isStream)
    {
      assert table.getIsPrimaryKeyExist() == false;
      try {
        table.setPrimaryKeyAttrList(s.getPrimaryKeyConstraintNode()
            .getColumns());
      }catch(MetadataException me)
      {
        me.setStartOffset(s.getPrimaryKeyConstraintNode().getStartOffset());
        me.setEndOffset(s.getPrimaryKeyConstraintNode().getEndOffset());
        throw me;
      }
      table.setIsPrimaryKeyExist(true);
    }

    //check if event identifier is a valid column and is of type bigint
    if(s.isArchived() && (s.getEventIdColName() != null))
    {
      Attribute idAttr = table.getAttribute(s.getEventIdColName());
      if(idAttr.getType() != Datatype.BIGINT)
        throw new MetadataException(MetadataError.INCORRECT_TYPE_FOR_COLUMN,
            new Object[] {"event",s.getEventIdColName()});
          
      table.setEventIdColName(s.getEventIdColName());
    }
      
    //check if worker identifier is a valid column and is of type bigint
    if(s.isArchived() && (s.getWorkerIdColName() != null))
    {
      Attribute idAttr = table.getAttribute(s.getWorkerIdColName());
      if(idAttr.getType() != Datatype.BIGINT)
        throw new MetadataException(MetadataError.INCORRECT_TYPE_FOR_COLUMN,
            new Object[] {"worker",s.getWorkerIdColName()});

      table.setWorkerIdColName(s.getWorkerIdColName());
    }

    //check if txn identifier is a valid column and is of type bigint
    if(s.isArchived() && (s.getTxnIdColName() != null))
    {
      Attribute idAttr = table.getAttribute(s.getTxnIdColName());
      if(idAttr.getType() != Datatype.BIGINT)
        throw new MetadataException(MetadataError.INCORRECT_TYPE_FOR_COLUMN,
            new Object[] {"transaction",s.getTxnIdColName()});

      table.setTxnIdColName(s.getTxnIdColName());
    }

    if(s.isArchived() && s.isStreamDefn())
    { //archived stream
      if(s.getTimestampColumn() != null)
      {
        Attribute tsAttr = table.getAttribute(s.getTimestampColumn());
        if((tsAttr.getType() != Datatype.TIMESTAMP) &&
            (tsAttr.getType() != Datatype.BIGINT))
          throw new MetadataException(MetadataError.INCORRECT_TYPE_FOR_TIMESTAMP_COLUMN,
              new Object[] {s.getTimestampColumn()});
        table.setTimestampColName(s.getTimestampColumn());
      }

      if(s.getReplayClause() != null)
      {
        table.setIsReplayRange(s.getReplayClause().isReplayRange());
        if(s.getReplayClause().isReplayRange())
        { //replay clause is specified as range
          CEPTimeSpecNode tRange = s.getReplayClause().getRange();
          table.setReplayRange(
              RangeConverter.interpRange(tRange.getAmount(),
                  tRange.getTimeUnit())
          );
        }
        else
        {
          //replay clause is specified as rows
          table.setReplayRows(s.getReplayClause().getNumRows());
        }
      }
    }
      
    if (s.getTimeStampExpr() != null)
    {
      table.setDerivedTs(s.getTimeStampExpr().toString());
    }
    tableId = table.getId();

    if (s.getTimeStampExpr() != null)
    {
      validateTsExpr(table, schema);
    }

    return tableId;
  }

  // Because of locking problems and semantic layer trying to load the metadata
  // we need to add the table and drop in case of error.
  public void validateTsExpr(Table table, String schema) throws MetadataException
  {
    // Prepare a dummy query to get semantic derived timestamp expression and
    // Validate its return type
    String qry = "select " + table.getDerivedTs() + " from " + table.getName();
    
    boolean isReturnTypeValid = false;
    
    try
    {
      QueryManager qryMgr = execContext.getQueryMgr();
      String qname = Constants.CQL_RESERVED_PREFIX + table.getName();
      int qryid = qryMgr.addNamedQuery(qname, qry, schema, null);
      // Check if derivedTS Expression evaluates to INT or BIGINT or TIMESTAMP
      isReturnTypeValid = qryMgr.validateReturnsTS(qryid);

      if (isReturnTypeValid)
      {
        CEPParseTreeNode node = qryMgr.getParser()
            .parseCommand(execContext, qry);
        assert node instanceof CEPSFWQueryNode;
        CEPSFWQueryNode qryNode = (CEPSFWQueryNode) node;
        table.setDerivedTsExpr(
            qryNode.getSelectClause().getSelectListExprs()[0]);
        qryMgr.dropQuery(qryid);
      }

      else
      {
        qryMgr.dropQuery(qryid);
        throw new MetadataException(MetadataError.INVALID_DERIVED_TIMESTAMP);
      }
    } catch (CEPException ce)
    {
      throw new MetadataException(MetadataError.INVALID_DERIVED_TIMESTAMP,
          new Object[]
          { table.getName() });
    }
  }

 
  private void unRegisterStrmReln(ITransaction txn, int id,
      boolean isStream) throws MetadataException
  {
    Locks locks = null;

    LogLevelManager.trace(LogArea.METADATA_TABLE, LogEvent.MTABLE_DELETE, this,
        isStream, id);
 
      // Delete the object
      // l will be null if no object existed
      locks = deleteCache(txn, id);

      // If object not found throw the appropriate exception
      if (locks == null)
      {
        if (isStream)
          throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
              new Object[]
              { id });
        else
          throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
              new Object[]
              { id });
      }
  
  }

  /**
   * Gets the table with this name
   * 
   * @param tableName
   * @return Table object
   */
  public Table getTableByName(String tableName, String schema)
  {

    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
    Table tbl = null;
    try
    {
      
      l = findCache(txn, new Descriptor(tableName, CacheObjectType.TABLE,
            schema, null), false);
      if (l == null)
      {
        // For now don't care if it is a table or a relation.
        // Only need to know whether it is found or not
        throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
            new Object[]
            { tableName });
      }
      tbl = (Table)l.getObj();
      return tbl;
    } catch (MetadataException me)
    {
      LogUtil.info(LoggerType.TRACE, "Could not find table!");
      return null;
    }
    finally
    {
       if(l != null)
        release(txn, l);
    }

    
  }
  
  /**
   * Drop the table
   * @param n
   * @throws CEPException
   */
  public void dropTable(CEPRelOrStreamRefNode n, String schema) throws CEPException
  {
    try{
      dropTable( n.getName(), schema, n.getIsStream());
    }
    catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }

  /**
   * Drop relation or stream
   */
  public void dropTable(String name, String schema, boolean isStream) throws CEPException
  {

    int id;
    CacheLock l = null;
    ITransaction txn = execContext.getTransaction();
   
  
      l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE, 
    		           schema, null), false);

      if (l == null)
      {
        if (isStream)
          throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
              new Object[]
              { name });
        else
          throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
              new Object[]
              { name });
      }

      Table tbl = (Table) l.getObj();
    //  if (execContext.getDependencyMgr().isAnyDependentPresent
   //                            (tbl.getId(), DependencyType.QUERY))
      if(execContext.getDependencyMgr().areDependentsPresent(tbl.getId()))
      {
        release(txn,l);
        throw new MetadataException(MetadataError.CANNOT_DROP_TABLE,
            new Object[]
            { name });
      }
      id = l.getObj().getId();
      release(txn, l);
      // Mark l as null so that if unregister throws an error we dont try to
      // release the lock again
      l = null;
      unRegisterStrmReln(txn, id, isStream);
      String source = tbl.getSource();
      if (source != null)
        // unsubscribe the source
        execContext.getInterfaceMgr().removeTableSource(tbl.getId(),
            source);
 
  }

  /**
   * Add a source for a stream.
   * 
   * @param name
   *                Stream name
   * @param source
   *                EPR for source
   * @throws MetadataException
   */
  public void addStreamSource(String name, String schema, String source)
      throws MetadataException, CEPException
  {
    addTableSource(name,schema, source, true, false);
  }

  /**
   * Add a push source for a stream.
   * 
   * @param name
   *                Stream name
   * @throws MetadataException
   */
  public void addStreamPushSource(String name, String schema)
               throws MetadataException, CEPException
  {
    addTableSource(name,schema, null, true, true);
  }

  /**
   * Add a source for a relation.
   * 
   * @param name
   *                Relation name
   * @param source
   *                EPR for source
   * @throws MetadataException
   */
  public void addRelationSource(String name, String schema, String source)
      throws MetadataException, CEPException
  {
    addTableSource(name, schema, source, false, false);
  }

  /**
   * Add a push source for a relation.
   * 
   * @param name
   *                Relation name
   * @throws MetadataException
   */
  public void addRelationPushSource(String name, String schema) 
                   throws MetadataException, CEPException
  {
    addTableSource(name, schema, null, false, true);
  }

  /**
   * Add a source for a stream/relation.
   * 
   * @param name
   *                Stream/Relation name
   * @param source
   *                EPR for source
   * @param isStream
   *                is it a stream or a relation
   * @param pushSource
   *                is it a push source
   * @throws MetadataException
   */

  public void addTableSource(String name, String schema, String source, 
              boolean isStream) throws MetadataException, CEPException
  {
    addTableSource(name, schema, source, isStream, (source == null));
  }
  
  public void addTableSource(CEPAddTableSourceNode n, String schema) throws CEPException
  {
    try{
       addTableSource( n.getName(), schema, n.getSource(), n.getIsStream(), n.getIsPush());
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }

  public void addTableSource(String name, String schema, String source,
	  boolean isStream, boolean pushSource) 
      throws MetadataException, CEPException
  {
    assert ((source != null) ^ (pushSource == true));
    // this assert?
    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();
 
    // Lock table
    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE, 
                                      schema, null), true);

    // If object not found throw the exception
    if (l == null)
    {
      if (isStream == true)
        throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
            new Object[]
            { name });
      else
        throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
            new Object[]
            { name });
    }
    
    table = (Table) l.getObj();

    // bug fix
    if (isStream && !(table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_STREAM_ERROR, new Object[]
      { name });
    if (!isStream && (table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_RELATION_ERROR, new Object[]
      { name });

    if (pushSource)
      table.setPushEventSource();
    else
      table.setSource(source);

  }
  
  public void setParallelismDegree(CEPSetParallelismDegreeNode n,
                                   String schema) 
  throws CEPException
  {
    try
    {
      setParallelismDegree(n.getName(), schema, n.getParallelismDegree(), 
                           n.isStream());
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }
  
  
  private void setParallelismDegree(String name, String schema, 
                                    int parallelismDegree, boolean isStream) 
  throws MetadataException, CEPException
  {
    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();
 
    // Lock table
    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE, 
                                      schema, null), true);

    // If object not found throw the exception
    if (l == null)
    {
      if (isStream == true)
        throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
            new Object[]
            { name });
      else
        throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
            new Object[]
            { name });
    }
    
    table = (Table) l.getObj();

    if (isStream && !(table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_STREAM_ERROR, new Object[]
      { name });
    if (!isStream && (table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_RELATION_ERROR, new Object[]
      { name });

    table.setDegreeOfParallelism(parallelismDegree);
  
  }

  /**
   * Add a source for a push source and read data
   * @param n
   *        Push source node containing push source
   * @throws CEPException
   */
  public void pushSourceNow(CEPAddPushSourceNode n, String schema) throws CEPException
  {
    try{
      pushSourceNow(n.getName(), schema, n.getSource());
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;	
    }
  }
  /**
   * Add a source for a push source and read the data.
   * @param name
   *                Stream name
   * @param source
   *                epr for the push source
   * 
   * @throws MetadataException
   */

  public void pushSourceNow(String name, String schema, String source) throws CEPException
  {
    int tableId = getId(name, schema);
    TableSource src = execContext.getInterfaceMgr().getTableSource(tableId, source);

    if (src.getInnerSource() != null)
      src = src.getInnerSource();

    int numAttrs = getNumAttrs(tableId);
    src.setNumAttrs(numAttrs);
    for (int i = 0; i < numAttrs; i++)
    {
      src.setAttrInfo(i, getAttrName(tableId, i), getAttrMetadata(tableId, i));
      src.setIsStream(isStream(tableId));
    }
    src.start();
    // flag is true till you have more inputs
    boolean flag = true;
    TupleValue t = null;

    while (flag)
    {
      try
      {
        t = src.getNext();
        if (t != null)
        {
          TupleValue t2 = t.clone();
          execContext.getExecMgr().insert(t2, tableId);
        } else
          flag = false;
      } catch (Exception e)
      {
        if (e instanceof InterfaceException)
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
          continue;
        } else
        {
          LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
          src.end();
          // unsubscribe the source
          execContext.getInterfaceMgr().removeTableSource(tableId,
              source);
          throw new MetadataException(MetadataError.INVALID_XML_ERROR,
              new Object[]
              { source, name });
        }
      }
    }
    src.end();
    // unsubscribe the source
    execContext.getInterfaceMgr().removeTableSource(tableId, source);
  }
  
  public void setTableStats(CEPTableMonitorNode n, String schema)
  throws MetadataException
  {
    try
    {
      setTableStats(n.getName(),schema, n.getIsEnabled(), 
                                    n.getIsBaseTimelineMillisecond());
    }
    catch(MetadataException me)
    {
      me.setStartOffset(n.getStartOffset());
      me.setEndOffset(n.getEndOffset());
      throw me;
    }
  }

  public void setTableStats(String name, String schema, boolean isenabled,
                            boolean isBaseTimelineMillisecond) 
  throws MetadataException
  {

    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();

    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE,
                  schema, null), true);

    // If object not found throw the exception
    if (l == null)
      throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
          new Object[]{ name });
      
    table = (Table) l.getObj();
    // True for Streams
    assert table.isBStream() == true;
    if (table.isDerivedTs())
      throw new MetadataException(
          MetadataError.CANNOT_ENABLE_STATS_FOR_DERIVED_TS, new Object[]
          { table.getName() });
    if (isenabled)
    {
      table.setIsStatsEnabled(isenabled, isBaseTimelineMillisecond);
    }
    else
    {
      // if we are disabling the table then no query on top of this table
      // should
      // be enabled
      Integer[] destQueries = execContext.getDependencyMgr().
                              getDependents(table.getId(), DependencyType.QUERY);
      if(destQueries != null)
      {
        for(int i=0; i<destQueries.length; i++)
        {
          Query qry = execContext.getQueryMgr().getQuery(
                                      destQueries[i].intValue());
          if (qry.isStatsEnabled)
          {
            throw new MetadataException(MetadataError.CANNOT_DISABLE_TABLE,
                new Object[]{ table.getName(), qry.getName() });
          }
        }
      }
       table.setIsStatsEnabled(isenabled, isBaseTimelineMillisecond);
        
      }
       
      //set flag for the execution operator
      execContext.getPlanMgr().setTableStats(isenabled, isBaseTimelineMillisecond, table.getId());
 
  }

  public void setTableSilent(CEPSetSilentNode n, String schema)
      throws MetadataException
  {

    CacheLock l = null;
    Table table;
    
    String name = n.getName();
    boolean isStream = n.getIsStream();
    ITransaction txn = execContext.getTransaction();

    // Lock table
    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE, 
                  schema, null), true);

    // If object not found throw the exception
    if (l == null)
    {
      if (isStream == true)
        throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
                                   n.getStartOffset(), n.getEndOffset(),
                                   new Object[]{ name });
      else
        throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
                                    n.getStartOffset(), n.getEndOffset(),
                                    new Object[]{ name });
    }
      
    table = (Table) l.getObj();
    table.setIsSilent(isStream);

  }

  /**
   * Get the source for the specified table
   * 
   * @param id
   *                Object id
   * @return EPR for the table source
   * @throws MetadataException
   */
  public String getTableSource(int id) throws MetadataException
  {
    CacheLock l = null;
    Table table;
    String src;
    ITransaction txn = execContext.getTransaction();

    try
    {
      // Lock Table
      l = findCache(txn, id, false, CacheObjectType.TABLE);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
            new Object[]
            { id });

      table = (Table) l.getObj();
      src = table.getSource();
      
    } finally
    {
      // Release
      if (l != null )
        release(txn, l);
     
    }

    return src;
  }
  /**
   * Get the source for the specified table
   * 
   * @param id
   *                Object id
   * @return EPR for the table source
   * @throws MetadataException
   */
  public void setTableIsSilent(int id, boolean flag) throws MetadataException
  {
    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();

    // Lock Table
    l = findCache(txn, id, true, CacheObjectType.TABLE);

    // If object not found then throw exception
    if (l == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
          new Object[]{ id });

    table = (Table) l.getObj();
    table.setIsSilent(flag);

  }

  /**
   * Is the source for the specified table push source
   * 
   * @param id
   *                Object id
   * @return EPR for the table source
   * @throws MetadataException
   */
  public boolean isTablePushSource(int id) throws MetadataException
  {
    CacheLock l = null;
    Table table;
    boolean pushSrc;
    ITransaction txn = execContext.getTransaction();

    try
    {
      // Lock Table
      l = findCache(txn, id, false, CacheObjectType.TABLE);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
            new Object[]
            { id });

      table = (Table) l.getObj();
      pushSrc = table.getPushEventSource();
     
    } finally
    {
      // Release
      if (l != null)
        release(txn, l);
    
    }

    return pushSrc;
  }
  public MetadataStats getTableStats(int id) throws MetadataException
  {

    Table table = getTable(id);
    MetadataStats stats = new MetadataStats();
    stats.setName(table.getName());
    stats.setText(table.getCql());
    stats.setIsMetadata(table.isBStream());
    stats.setIsPushSrc(table.getPushEventSource());
    return stats;

  }
  public QueueSourceStat getQueueSourceStat(int id) throws MetadataException
  {
    Table table = getTable(id);
    if(table.getPushEventSource())
    {
      try {
      QueueSource s = (QueueSource)execContext.getExecMgr().getInstantiatedTableSource(id);
      if(s == null)
        return new QueueSourceStat();
      else
        return s.getStat();
     }catch(CEPException e)
     {
       return new QueueSourceStat();
     }
    } 
    else
     return new QueueSourceStat();
     
  }
  
  /**
   * Get the isSystemTimeStamped flag
   * @param id
   *                table identifier
   * 
   * @return boolean true if table is declared as systemtimestamped, false
   *         otherwise
   * @throws MetadataException
   */

  public boolean isSystemTimestamped(int id) throws MetadataException
  {
    Table t = getTable(id);
    if (t == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
          new Object[]
          { id });
    else
      return t.isSystemTimestamped();
  }

  /**
   * Evaluates isSystemTimeStamped || isSilent || isDerivedTs
   * 
   * @param id
   *                table identifier
   * @return boolean true if table is declared as systemtimestamped || silent ||
   *         derivedTS, false otherwise
   * @throws MetadataException
   */

  public boolean isClientTimeStamped(String tableName, String schema) 
     throws MetadataException
  {
    Table t = getTableByName(tableName, schema);
    if (t == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
          new Object[]
          { tableName });
    if (t.isSystemTimestamped() || t.getIsSilent() || t.isDerivedTs())
      return false;
    return true;
  }


  public Table getTable(int id)
      throws MetadataException
  {
    CacheLock l = null;
    Table table = null;
    ITransaction txn = execContext.getTransaction();

    try
    {
      // Get Read lock
      l = findCache(txn, id, false, CacheObjectType.TABLE);

      // If object not found then throw exception
      if (l == null)
        throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
            new Object[]
            { id });

      table = (Table) l.getObj();

      if (table.isDerivedTs && (table.getDerivedTsExpr() == null))
        loadTsExpr(table);
    } finally
    {
      // Release
      if (l != null)
        release(txn, l);
    }

    return table;
  }
  

  private void loadTsExpr(Table table) throws MetadataException
  {
    String qry = "select " + table.getDerivedTs() + " from " + table.getName();
    try
    {
      CEPParseTreeNode parseTree = execContext.getQueryMgr().getParser()
          .parseCommand(execContext, qry);
      CEPSFWQueryNode qryNode = (CEPSFWQueryNode) parseTree;
      table.setDerivedTsExpr(qryNode.getSelectClause().getSelectListExprs()[0]);
    } catch (CEPException ce)
    {
      throw new MetadataException(MetadataError.INVALID_DERIVED_TIMESTAMP,
          new Object[]
          { table.getName() });
    }
  }
  
  /**
   * Alter the Max Rows for External relations. This value determines the 
   * maximum number of rows to be fetched from an external relation during one 
   * lookup
   * @param name    The name of the external relation
   * @param schema  The schema
   * @param maxRows The value of max rows to be set
   * @throws MetadataException
   * @throws CEPException
   */
  public void alterMaxRowsForExternalRelation(String name, String schema,
                                              long externalRowsThreshold)
  throws MetadataException, CEPException
  {
    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();
    
    // Lock table
    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE,
                  schema, null), true);
    
    // If object not found throw the exception
    if (l == null)
      throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
                                    new Object[]{name});
    
    table = (Table) l.getObj();
    
    // check if object is indeed relation
    if (table.isBStream())
      throw new CEPException(SemanticError.NOT_A_RELATION_ERROR,
                              new Object[]{name});
    
    // check if table is an external relation
    if(!table.isExternal)
      throw new CEPException(SemanticError.UNKNOWN_TABLE_ERROR, 
                               new Object[]{name});
    
    if(externalRowsThreshold < 1)
      throw new CEPException(SemanticError.INVALID_MAX_ROWS_VALUE, 
                             new Object[]{externalRowsThreshold});
    
    // set the max rows value for external relations
    table.setExternalRowsThreshold(externalRowsThreshold);
   
  }

  public void alterHeartbeatTimeout(String name, String schema, boolean isStream,
      long duration, boolean isSetStmt) throws MetadataException, CEPException
  {
    CacheLock l = null;
    Table table;
    ITransaction txn = execContext.getTransaction();
   
    if (isSetStmt && (duration <= 0))
      throw new CEPException(SemanticError.INVALID_TIMEOUT_VALUE, 
                             new Object[]{duration});
    // Lock table
    l = findCache(txn, new Descriptor(name, CacheObjectType.TABLE,
                  schema, null), true);

    // If object not found throw the exception
    if (l == null)
    {
      if (isStream == true)
        throw new MetadataException(MetadataError.STREAM_NOT_FOUND,
                                    new Object[]{name});
      else
        throw new MetadataException(MetadataError.RELATION_NOT_FOUND,
                                    new Object[]{name});
    }
      
    table = (Table) l.getObj();
    // check if object is indeed stream
    if (isStream && !(table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_STREAM_ERROR,
                             new Object[]{name});
    // check if object is indeed relation
    if (!isStream && (table.isBStream()))
      throw new CEPException(SemanticError.NOT_A_RELATION_ERROR,
                              new Object[]{name});
    // check if source is declared as system timestamped
    if (!(table.isSystemTimestamped()))
      throw new MetadataException(MetadataError.NOT_A_SYSTS_SOURCE,
                                  new Object[]{name});

    // set the timeout value
    table.setTimeout(duration);
    table.setIsExplicitTimeout(true);
  }

  public int getTargetId()
  {
    return 0;
  }

  public String getTargetName()
  {
    return "TableManager";
  }

  public int getTargetType()
  {
    return 0;
  }

  public ILogLevelManager getLogLevelManager()
  {
    return execContext.getLogLevelManager();
  }
    
  public void trace(IDumpContext dumper, ILogEvent event, int level,
      Object[] args)
  {
    // All levels are handled by the default implementation.
    // MWINDOW_INFO - dumps using the fields specified in DumpDesc annotation
    // MWINDOW_LOCKINFO - handled by overriden dump method in this class
  }

  public void dump(IDumpContext dump)
  {
    super.dump(dump, LogTags.TAG_TABLES, CacheObjectType.TABLE);
  }
  
  /**
   * Returns teh maximum allowed external rows for this table id
   * this table id should be corresponding to an external relation
   * else it will return -1
   * @param id
   * @return
   * @throws MetadataException
   */
  public long getExternalRowsThreshold(int id) throws MetadataException
  {
    Table t = getTable(id);
    if (t == null)
      throw new MetadataException(MetadataError.INVALID_TABLE_IDENTIFIER,
          new Object[]
          { id });
    else
      return t.getExternalRowsThreshold();
  }
  
  public boolean isPartitioned(int id) throws MetadataException
  {
    Table src = getTable(id);
    return src.isPartitioned();
  }

  
  
}
