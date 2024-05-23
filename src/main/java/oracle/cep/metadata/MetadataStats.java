/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/MetadataStats.java /main/4 2013/10/08 10:15:00 udeshmuk Exp $ */

/* Copyright (c) 2007, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    07/09/13 - add ArchiverStats
    parujain    04/16/08 - modify stats
    parujain    04/27/07 - Statistics for Stream/Relation
    parujain    04/27/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/MetadataStats.java /main/4 2013/10/08 10:15:00 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

public class MetadataStats {
  
  /** Name - Table Name or Query name */
  private String metaName;
  
  /** Table creation text or Query text */
  private String text;
  
  /** IsStream for Table and isView for Query */
  private boolean isMetadata;

  /** IsPushSrc -only for tables */
  private boolean isPushSrc = false;
  
  /** IsInternal - partition-ordered queries create DDLs using internal schema */
  private boolean isInternal = false;
  
  /** archiverStats - statistics collected for query start time processing */
  private ArchiverStats archiverStats = null;
  
  public boolean isInternal()
  {
    return isInternal;
  }

  public void setInternal(boolean isInternal)
  {
    this.isInternal = isInternal;
  }

  public void setName(String name)
  {
    this.metaName = name;
  }
  
  public void setText(String text)
  {
    this.text = text;
  }
  
  public void setIsMetadata(boolean is)
  {
    this.isMetadata = is;
  }

  public void setIsPushSrc(boolean ispush)
  {
    this.isPushSrc = ispush;
  }
  
  public void setArchiverStats(ArchiverStats archStats)
  {
    this.archiverStats = archStats;
  }
 
  public String getName()
  {
    return metaName;
  }
  
  public String getText()
  {
    return text;
  }
  
  public boolean getIsMetadata()
  {
    return isMetadata;
  }

  public boolean getIsPushSrc()
  {
    return isPushSrc;
  }
  
  public ArchiverStats getArchiverStats()
  {
    return archiverStats;
  }
}

