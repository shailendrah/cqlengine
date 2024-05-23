/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Table.java /main/34 2013/04/30 11:44:35 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Relation in package oracle.cep.metadata.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi 04/07/13 - XbranchMerge sbishnoi_bug-15962405_ps6 from
                     st_pcbpel_11.1.1.4.0
 sbishnoi 02/18/13 - allow disable heartbeat timeout
 udeshmuk 08/23/12 - move fields common to table and view in superclass
 vikshukl 07/30/12 - archived dimension table
 udeshmuk 04/03/12 - getter setter for worker id and txn id
 udeshmuk 01/08/12 - set timestamp col name and range/row value
 udeshmuk 09/15/11 - remove prints
 udeshmuk 08/24/11 - add a field for archived rel event id column name
 anasrini 04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini 03/21/11 - move cql to Source
 sborah   03/17/11 - add parallelismDegree expr
 udeshmuk 03/02/11 - support for archived relation.
 sborah   07/18/10 - XbranchMerge sborah_bug-9536720_ps3_11.1.1.4.0 from
                     st_pcbpel_11.1.1.4.0
 sborah   07/17/10 - XbranchMerge sborah_bug-9536720_ps3 from main
 sborah   06/23/10 - set max rows in external relations
 sbishnoi 06/22/10 - adding threshold for number of rows on external relation
 parujain 10/02/09 - dependency support
 parujain 09/24/09 - dependency support
 skmishra 02/13/09 - try/catch in addQueryDestination
 parujain 01/14/09 - metadata in-mem
 sborah   11/24/08 - support for altering base timeline
 parujain 11/21/08 - handle constants
 parujain 09/12/08 - multiple schema support
 skmishra 08/21/08 - import MetadataDescriptor
 parujain 04/18/08 - qsrc stats
 hopark   04/16/08 - add qsrc stat
 parujain 03/04/08 - functional timestamp
 mthatte  02/26/08 - parametrizing metadata errors
 hopark   01/17/08 - dump
 udeshmuk 12/16/07 - add field for storing timeout.
 mthatte  11/26/07 - changing TYPE in descriptor to TABLE for ODI
 udeshmuk 11/18/07 - add isSystemTimestamped boolean flag.
 mthatte  11/07/07 - minor fix for allocatedescriptor
 sbishnoi 10/28/07 - support for primary key
 mthatte  10/11/07 - adding isSystemTime()
 mthatte  08/23/07 - Adding isSystem to identify system tables.
 mthatte  08/22/07 - Implement MetadataDescriptor interface
 parujain 05/08/07 - set systemtimestamped for stats
 parujain 04/26/07 - Stream/Relation creation text
 hopark   03/21/07 - make constructor public for unit test
 parujain 02/02/07 - BDB Integration
 parujain 01/11/07 - BDB integration
 parujain 01/09/07 - BDB integration
 hopark   11/07/06 - check if source is already set
 parujain 10/23/06 - Silent relations
 parujain 09/12/06 - MDS Integration
 dlenkov   09/07/06 - fix query references and source initialization
 najain    08/21/06 - add push source
 najain    07/28/06 - handle static relations 
 parujain  07/13/06 - check locks 
 parujain  06/29/06 - metadata cleanup 
 anasrini  03/27/06 - set position of attribute in addAttribute 
 najain    03/23/06 - ensure that the table source is 
 skaluska  03/22/06 - add table source 
 skaluska  03/10/06 - Creation
 skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Table.java /main/34 2013/04/30 11:44:35 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

import java.util.ArrayList;
import java.util.HashSet;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpable;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.util.XMLHelper;
import oracle.xml.parser.v2.XMLDocument;

/**
 * Common metadata for relations and streams
 * 
 * @author skaluska
 */
@DumpDesc(autoFields=true,
          attribTags={"Id", "Key"}, 
          attribVals={"getId", "getKey"})
public class Table extends Source implements IDumpable, Cloneable
{
  /**
   * 
   */
  private static final long     serialVersionUID = 1L;

  /** EPR for the source */
  private String                source;

  /** is silent relation */
  private boolean               isSilent;
  
  /** is the source system timestamped */
  private boolean               isSystemTimestamped = false;
  
  /** is this archived relation a dimension */
  private boolean               isDimension = false;
  /** applicable for archived source only.
   * name of the archiver for this archived source. 
   */
  private String                archiverName;

  /** applicable for archived source only.
   *  entity name to which this archived source maps.
   */
  private String                entityName;
   
  /** applicable for archived source only.
   *  name of the column which acts as an worker identifier.
   */
  private String                workerIdColName;
  
  /** applicable for archived source only.
   *  name of the column which acts as an transaction identifier.
   */
  private String                txnIdColName;
  
  /**
   * applicable for archived source only.
   * tells whether replay clause uses range or rows.
   */
  private boolean               isRange;
  
  /**
   * applicable for archived stream only.
   * number of rows specified in the replay clause.
   */
  private int                   replayRows;
  
  /**
   * applicable for archived stream only.
   * range in nanoseconds specified in the replay clause.
   */
  private long                  replayRange;
  
  /**
   * applicable for archived stream only.
   * name of column specified as timestamp column.
   */
  private String                tsColName;
  
  /** is Statistics enabled */
  private boolean               isStatsEnabled;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   * */
  private boolean               isBaseTimelineMillisecond;
  
  /** has an push event source */
  boolean                       pushSource;

  /** Is this a system table? */
  boolean                       isSystem;
  
  /** Is the timestamp derived */
  boolean                       isDerivedTs;
  
  /** String for derived Ts */
  private String                derivedTs;
  
  /** Parsed Expr for derived Ts */
  private transient CEPExprNode derivedTsExpr;
  
  /** Primary Key Columns */
  private ArrayList<Attribute>  primaryKeyAttrList;
  
  /** flag to check if primary key exists or not*/
  boolean                       isPrimaryKeyExist;
  
  /** timeout duration for heartbeat clause */
  long timeOut;
 
  /** is explicit heartbeat timeout set or disabled */ 
  boolean isExplicitTimeout;

  /** threshold for the number of allowed external rows */
  private long                  externalRowsThreshold;
  
   
  @SuppressWarnings("unchecked")
  public Table clone() throws CloneNotSupportedException {
    Table tbl = (Table)super.clone();
    if(primaryKeyAttrList != null)
      tbl.primaryKeyAttrList = (ArrayList<Attribute>)primaryKeyAttrList.clone();
    else
      tbl.primaryKeyAttrList = null;
    return tbl;
  }
  

  /**
   * Constructor for Table
   * 
   * @param name
   *                Table name
   */
  public Table(String name, String schema)
  {
    super(name, schema, CacheObjectType.TABLE);
    source                    = null;
    isBaseTimelineMillisecond = false;
    pushSource                = false;
    isSystem                  = false;
    primaryKeyAttrList        = null;
    isPrimaryKeyExist         = false;
    timeOut                   = -1;
    isExplicitTimeout         = false;
    derivedTs                 = null;
    isDerivedTs               = false;
    derivedTsExpr             = null;
    replayRows                = 0;
    replayRange               = 0;
    isRange                   = true;
    externalRowsThreshold     = Long.MIN_VALUE;
  }
  
  public long getExternalRowsThreshold()
  {
    if(this.isExternal)
      return externalRowsThreshold;
    else
      return Long.MIN_VALUE;
  }


  public void setExternalRowsThreshold(long externalRowsThreshold)
  {
    this.externalRowsThreshold = externalRowsThreshold;
  }


  public void setDerivedTs(String ts)
  {
    this.derivedTs = ts;
    if(ts != null)
      this.isDerivedTs = true;
  }
  
  public void setDerivedTsExpr(CEPExprNode expr)
  {
    this.derivedTsExpr = expr;
  }
  
  public void setIsSystemTimeStamped(boolean isSystemTimestamped)
  {
    this.isSystemTimestamped = isSystemTimestamped;  
  }
  

  public boolean isDimension()
  {
    return isDimension;
  }

  public void setIsDimension(boolean isDimension)
  {
    this.isDimension = isDimension;
  }
  
  public String getArchiverName()
  {
    return archiverName;
  }

  public void setArchiverName(String archiverName)
  {
    this.archiverName = archiverName;
  }

  public String getEntityName()
  {
    return entityName;
  }

  public void setEntityName(String entityName)
  {
    this.entityName = entityName;
  }
  
  public void setWorkerIdColName(String workerIdColName)
  {
    this.workerIdColName = workerIdColName;
  }
  
  public String getWorkerIdColName()
  {
    return this.workerIdColName;
  }
  
  public void setTxnIdColName(String txnIdColName)
  {
    this.txnIdColName = txnIdColName;
  }
  
  public String getTxnIdColName()
  {
    return this.txnIdColName;
  }
  
  public void setTimestampColName(String tName)
  {
    this.tsColName = tName;
  }

  public String getTimestampColName()
  {
    return this.tsColName;
  }
  
  public long getReplayRange()
  {
    return this.replayRange;
  }
  
  public void setReplayRange(long tRange)
  {
    this.replayRange = tRange;
  }
  
  public int getReplayRows()
  {
    return this.replayRows;
  }
  
  public void setReplayRows(int rows)
  {
    this.replayRows = rows;
  }
  
  public boolean isReplayRange()
  {
    return isRange;
  }
  
  public void setIsReplayRange(boolean val)
  {
    this.isRange = val;
  }
  
  public boolean getIsSilent()
  {
    return isSilent;
  }

  public void setIsSilent(boolean isSilent)
  {
    assert !isSilent || !isBStream();
    this.isSilent = isSilent;
  }

  public void setTimeout(long timeOut)
  {
    this.timeOut = timeOut;
  }

  public void setIsExplicitTimeout(boolean flag)
  {
    this.isExplicitTimeout = flag;
  }
  
  public boolean isDerivedTs()
  {
    return this.isDerivedTs;  
  }
  
  public String getDerivedTs()
  {
    return this.derivedTs;
  }
  
  public CEPExprNode getDerivedTsExpr()
  {
    return this.derivedTsExpr;
  }
  
  public long getTimeout()
  {
    return this.timeOut;
  }

  public boolean isExplicitTimeout()
  {
    return this.isExplicitTimeout;
  }
  
  /**
   * Sets the stats to be enabled/disabled 
   * and the base timeline to millisecond/nanosecond
   *  
   * @param isenabled 
   *        whether Enabled/Disabled
   * 
   * @param isBaseTimelineMillisecond  
   *                  whether millisecond/nanosecond
   */
  public void setIsStatsEnabled(boolean isenabled, 
                             boolean isBaseTimelineMillisecond)
  {
    this.isStatsEnabled = isenabled;
    this.isBaseTimelineMillisecond = isBaseTimelineMillisecond;
  }

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
   * Getter for source in Table
   * 
   * @return Returns the source
   */
  public String getSource()
  {
    return source;
  }

  /**
   * Setter for source in Table
   * 
   * @param source
   *                The source to set.
   */
  public void setSource(String source) throws MetadataException
  {
    if (pushSource == true)
      throw new MetadataException(MetadataError.PUSH_SRC_EXISTS, new Object[] {pushSource});

    if (this.source != null)
      throw new MetadataException(MetadataError.PULL_SRC_EXISTS, new Object[] {this.source});

    try{
      XMLDocument doc = XMLHelper.verifyDocument(source);
    }
    
    catch(CEPException e)
    {
      throw new MetadataException(MetadataError.INVALID_XML_ERROR);
    }
    // check whether write lock on this object has been acquired or not.
    assert isWriteable() == true;

    this.source = source;
  }

  public void setPushEventSource() throws MetadataException
  {
    if (source != null)
      throw new MetadataException(MetadataError.PULL_SRC_EXISTS, new Object[] {source});

    pushSource = true;
  }

  public boolean getPushEventSource()
  {
    return pushSource;
  }

  /**
   * Return table metadata as XML
   * 
   * @return XML representation for table metadata
   */
  public String toXml()
  {
    return null;
  }

  public MetadataDescriptor allocateDescriptor()
  {
    String type = "TABLE";
    
    /* Types as named in CEP
    if (this.isBStream())
      type = "STREAM";
    else
      type = "RELATION";
      */
    
    if (this.isSystem)
      throw new UnsupportedOperationException(
          "System tables not returned to client.");
    TableMetadataDescriptor desc = new TableMetadataDescriptor(this.getName(),
        type);
    return desc;
  }
  /**
   * Set isPrimaryKeyExist flag
   * @param isPrimaryKeyExist
   */
  public void setIsPrimaryKeyExist(boolean isPrimaryKeyExist)
  {
    this.isPrimaryKeyExist = isPrimaryKeyExist;
  }

  public boolean isSystem()
  {
    return isSystem;
  }

  public void setSystem(boolean isSystem)
  {
    this.isSystem = isSystem;
  }

  /**
  * return whether this table is systemtimestamped or not
  * @return boolean value indicating whether the source is system timestamped
  */
  public boolean isSystemTimestamped() 
  {
    return isSystemTimestamped;
  }
  /**
   * Set List of Primary Key Attribute
   * @param columns name of Primary key attributes
   * @throws MetadataException
   */
  public void setPrimaryKeyAttrList(String[] columns)
    throws MetadataException
  {
    checkDuplicates(columns);
    primaryKeyAttrList = new ArrayList<Attribute>();
    for(int i= 0; i < columns.length ; i++)
    {
      primaryKeyAttrList.add(this.getAttribute(columns[i]));
    }
  }
  
  /**
   * Get IsPrimaryKeyExist Flag
   * @return true if primary key defined over relation
   *    else false 
   */
  public boolean getIsPrimaryKeyExist()
  {
    return this.isPrimaryKeyExist;
  }
  
  /**
   * Get Primary Key Attribute List
   * @return
   */
  public ArrayList<Attribute> getPrimaryKeyAttrList()
  {
    return this.primaryKeyAttrList;
  }
  
  /**
   * Check If Any Duplicate column name in primary key attributes
   * @param columns
   * @throws MetadataException
   */
  public void checkDuplicates(String[] columns)
  throws MetadataException
  {
    HashSet<String> tempListSet = new HashSet<String>();
    for(int i = 0; i < columns.length; i++)
    {
      if(!tempListSet.add(columns[i]))
        throw new MetadataException(MetadataError.DUPLICATE_COLUMN_NAME,
                                    new Object[]{columns[i]});
    }
  }
  
}
