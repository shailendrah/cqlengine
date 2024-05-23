/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Source.java /main/19 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
     base class of all sources -- currently streams, relations and views

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi 09/23/14 - support for partitioned stream
    udeshmuk 08/23/12 - common fields from table and view are in this class
    anasrini 07/25/11 - XbranchMerge anasrini_bug-12640350_ps5 from
                        st_pcbpel_11.1.1.4.0
    anasrini 03/23/11 - add degreeOfParallelism
    anasrini 03/21/11 - add createDDL
    parujain 06/02/09 - fix viewinfo
    parujain 02/13/09 - get types
    parujain 01/14/09 - metadata in-mem
    parujain 09/12/08 - multiple schema support
    skmishra 08/21/08 - imports
    parujain 02/07/08 - parameterizing errors
    parujain 12/03/07 - external relation
    parujain 11/09/07 - External source
    mthatte  08/22/07 - added dummy allocateDescriptor
    hopark   03/21/07 - move the store integration part to CacheObject
    sbishnoi 02/06/07 - modify exception constructor
    parujain 02/02/07 - BDB integration
    parujain 01/11/07 - BDB integration
    parujain 01/09/07 - BDB integration
    parujain 09/11/06 - MDS Integration
    parujain 07/13/06 - check locks 
    parujain 06/29/06 - metadata cleanup 
    najain      05/09/06 - creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Source.java /main/19 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */

package oracle.cep.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;

import oracle.cep.common.Constants;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.CacheObjectType;

/**
 * Common metadata for data sources
 * 
 * @author najain
 */
public class Source extends CacheObject implements Cloneable {
  
  /**
   * Default suid 
   */
  private static final long serialVersionUID = 1L;

  /** attributes */
  private ArrayList<Attribute> attrList;

  /** whether this is a stream */
  private boolean              bStream;
  
  /** whether this is an External view/table */
  protected boolean 		isExternal;

  /** Creation DDL text for the base entity / view provided by user */
  private String                cql;

  /** Degree of parallelism for this entity */
  private int                   dop;

  /** is the source an archived one */
  private boolean               isArchived = false;
  
  /** Is the source a partitioned one */
  private boolean               isPartitioned;
  
  /** applicable for archived source only.
   *  name of the column which acts as an event identifier.
   */
  private String                eventIdColName;
 
  @SuppressWarnings("unchecked")
  public Source clone() throws CloneNotSupportedException {
    Source src = (Source)super.clone();
    src.attrList = (ArrayList<Attribute>)attrList.clone();
    return src;
  }


  /**
   * Constructor
   * @param name Source name
   */
  Source(String name,String schema, CacheObjectType type) {
    super(name, schema, type);

    this.attrList = new ArrayList<Attribute>();
    this.bStream = false;
    this.isExternal = false;
    this.cql = null;
    this.dop = Constants.DEFAULT_DEGREE_OF_PARALLELISM;
  }

 /**
   * Get name
   * 
   * @return name of the source
   */
  public String getName() {
    return (String) getKey();
  }
  
  public boolean isArchived()
  {
    return isArchived;
  }


  public void setIsArchived(boolean isArchived)
  {
    this.isArchived = isArchived;
  }

  public void setEventIdColName(String eventIdColName)
  {
    this.eventIdColName = eventIdColName;
  }
  
  public String getEventIdColName()
  {
    return this.eventIdColName;
  }
  
  /**
   * Get the map of attributes
   * @return Map of attrname and its type
   */
  public LinkedHashMap<String, String> getAttributes()
  {
    LinkedHashMap<String, String> attrs = new LinkedHashMap<String, String>();
    Iterator<Attribute> iter = attrList.iterator();
    while(iter.hasNext())
    {
      Attribute a = iter.next();
      // REVIEW why the implementation type name rather than the datatype name?
      attrs.put(a.getName(), a.getType().getImplementationType().getName());
    }
    return attrs;
  }
  

  /**
   * Add an attribute
   * 
   * @param attr
   *          Attribute metadata
   * @throws MetadataException
   */
  public void addAttribute(Attribute attr) throws MetadataException {
    Iterator<Attribute> i;
    Attribute a;
    
//  Before modifying check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    // Check for duplicates
    i = attrList.iterator();
    for (; i.hasNext();) {
      a = i.next();
      if (a.getName().equals(attr.getName()))
        throw new MetadataException(MetadataError.ATTRIBUTE_ALREADY_EXISTS,
        		                    new Object[]{a.getName()});
    }

    // Set the position and add to list
    attr.setPosition(getNumAttrs());
    attrList.add(attr);
  }

  /**
   * Get number of attributes
   * 
   * @return Number of attributes
   */
  public int getNumAttrs() {
    return attrList.size();
  }
  /**
   * Get attribute by position
   * 
   * @param pos
   *          Attribute position
   * @return Attribute metadata
   * @throws MetadataException
   */
  public Attribute getAttribute(int pos) throws MetadataException {
    // Validate position
    if (pos > attrList.size())
      throw new MetadataException(MetadataError.ATTRIBUTE_NOT_FOUND_AT_DEF_POS, new Object[]{pos});

    // Return attribute
    return attrList.get(pos);
  }

  /**
   * Get attribute by name
   * 
   * @param name
   *        Object  Attribute name
   * @return Attribute metadata
   * @throws MetadataException
   */
  public Attribute getAttribute(String name) throws MetadataException {
    Iterator<Attribute> i;
    Attribute a = null;
    boolean found = false;

    assert name != null;

    // Find attribute
    i = attrList.iterator();
    while (i.hasNext()) {
      a = i.next();
      if (a.getName().equals(name)) {
        found = true;
        break;
      }
    }

    if (!found)
      throw new MetadataException(MetadataError.ATTRIBUTE_NOT_FOUND, new Object[] {name});

    return a;
  }

  /**
   * Getter for bStream in Table
   * 
   * @return Returns the bStream
   */
  public boolean isBStream() {
    return bStream;
  }

  /**
   * Setter for bStream in Table
   * 
   * @param stream
   *          The bStream to set.
   */
  public void setBStream(boolean stream) {
    
//  check whether write lock has been acquired or not.
    assert isWriteable() == true;
    
    bStream = stream;
  }
  
  /** Getter for isExternal */
  public boolean isExternal() 
  {
    return isExternal;
  }

  /**Setter for isExternal */
  public void setExternal(boolean isExternal) 
  {
    this.isExternal = isExternal;
  }

  public void setCql(String str)
  {
    this.cql = str;
  }

  public String getCql()
  {
    return cql;
  }

  public void setDegreeOfParallelism(int dop)
  {
    this.dop = dop;
  }

  public int getDegreeOfParallelism()
  {
    return dop;
  }

  
  public boolean isPartitioned() {
    return isPartitioned;
  }


  public void setPartitioned(boolean isPartitioned) {
    this.isPartitioned = isPartitioned;
  }


  /** required for JDBC -- refer CEPDatabaseMetadata.getTables(), getColumns()
   *  implemented by Table.java, View.java 
   */
  public MetadataDescriptor allocateDescriptor() 
  {
	throw new UnsupportedOperationException("allocateDescriptor() not implemented by Source");
  }
}
