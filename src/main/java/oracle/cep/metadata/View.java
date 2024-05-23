/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/View.java /main/20 2014/10/14 06:35:34 udeshmuk Exp $ */

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
    sbishnoi 09/23/14 - support for partitioned stream
    udeshmuk 08/23/12 - move fields common to table and view in superclass
    vikshukl 07/31/12 - archived dimension relation
    udeshmuk 06/05/12 - add isArchived
    parujain 09/24/09 - dependency support
    skmishra 01/21/09 - adding getQCXML()
    parujain 01/14/09 - metadata in-mem
    parujain 11/24/08 - support view state
    hopark   10/07/08 - use execContext to remove statics
    parujain 09/12/08 - multiple schema support
    skmishra 08/21/08 - imports
    parujain 05/05/08 - lock problem
    hopark   01/17/08 - dump
    parujain 11/09/07 - External source
    parujain 10/26/07 - handle OnDemand
    mthatte  11/08/07 - jdbc changes
    mthatte  08/22/07 - 
    parujain 04/17/07 - bug fix
    hopark   03/21/07 - storage re-org
    parujain 02/02/07 - BDB integration
    parujain 01/11/07 - BDB integration
    parujain 01/09/07 - BDB integration
    parujain 10/18/06 - interface getDestQueries
    dlenkov  09/14/06 - 
    parujain 09/12/06 - MDS Integration
    parujain 07/13/06 - check locks 
    parujain 06/29/06 - metadata cleanup 
    najain      05/17/06 - view support 
    najain      05/11/06 - support for views 
    najain      05/09/06 - Creation

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/View.java /main/20 2014/10/14 06:35:34 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */

package oracle.cep.metadata;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpable;
import oracle.cep.metadata.cache.CacheObjectType;
import oracle.cep.service.ExecContext;

/**
 * Common metadata for view
 * 
 * @author najain
 */
@DumpDesc(autoFields=true,
          attribTags={"Id", "Key"}, 
          attribVals={"getId", "getKey"})
public class View extends Source implements IDumpable, Cloneable
{

  /**
   * 
   */
  private static final long   serialVersionUID = 1L;

  /** The id of the associated query */
  int                         queryId;

  /** Current View state */
  private ViewState           state;
  
  /** true if view dependent on archived dimension */
  private boolean             isDimension;
  
  /**
   * Constructor for View
   * 
   * @param name
   *          View name
   */
  View(String name, String schema)
  {
    super(name, schema, CacheObjectType.VIEW);
  }

  public View clone() throws CloneNotSupportedException {
    View v = (View)super.clone();
    return v;
  }

  /**
   * Get the id of the associated query
   * @return the id of the associated query
   */
  public int getQueryId()
  {
    return queryId;
  }

  /**
   * Set the id of the associated query
   * @param id the id of the associated query
   */
  public void setQueryId(int queryId)
  {

    //  Before modifying the object check whether write lock has been acquired or not.
    assert isWriteable() == true;

    this.queryId = queryId;
  }
   
  public boolean isDimension()
  {
    return isDimension;
  }
  
 
  public void setIsDimension(boolean dim)
  {
    this.isDimension = dim;
  }
  
  /**
   * Set the current view state
   * @param vs  current view state
   */
  public void setViewState(ViewState vs)
  {
    assert isWriteable() == true;
    this.state = vs;
  }
  
  /**
   * Get the current view state
   * @return  Current view state
   */
  public ViewState getViewState()
  {
    return this.state;
  }

  public MetadataDescriptor allocateDescriptor(ExecContext ec)
  {
    TableMetadataDescriptor desc = new TableMetadataDescriptor(this.getName(),
        "VIEW");
    try{
      int id = this.getId();
      String cql = ec.getQueryMgr().getQuery(id).getText();
      desc.setRemarkText(cql);
    }
    
    catch(MetadataException e) {
      
    }
    return desc;
  }
  
}
