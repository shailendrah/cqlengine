/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSource.java /main/7 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/17/12 - provide fully qualified name wherever possible
    vikshukl    08/29/12 - use fully qualified names
    pkali       04/04/12 - included datatype arg in AttrNamed instance
    vikshukl    08/25/11 - subquery support
    sborah      04/24/11 - support for external relation
    anasrini    03/29/11 - set isView
    anasrini    03/24/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSource.java /main/7 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.metadata.MetadataException;
import oracle.cep.service.ExecContext;

/**
 * Abstract base class for logical layer source operators
 * Currently, the 3 extending logical operators are LogOptStrmSrc,
 * LogOptRelnSrc and LogOptSubquerySrc.
 * 
 * For inline subqueries, lot of the on-disk/cache metadata stuff is not
 * relevant. i.e. they are transient and exist within the scope of a single
 * query only. 
 *
 * In the logical layer, views and base entities are not differentiated
 */
public abstract class LogOptSource extends LogOpt 
{

  /** Symbol Table Variable Identifier for this alias */
  protected int varId; 

  /** Metadata layer entity identifier */
  protected int entityId;

  /** Entity Name */
  protected String entityName;

  /** create DDL text used to create this stream */
  private String createDDL;
  
  /** add source DDL text used to provide the source for this stream */
  private String source;
  
  /** The degree of parallelism for this stream */
  private int degreeOfParallelism;

  /** Is this source a stream or a relation ? */
  private boolean isSourceStream;

  /** Does this source correspond to a view or a base entity ? */
  private boolean isView;
  

  // CONSTRUCTORS

  public LogOptSource()
  {
    super();
  }

  public LogOptSource(LogOptKind operatorKind)
  {
    super(operatorKind);
  }

  public LogOptSource(ExecContext ec, LogOptKind operatorKind, int varId,
                      int entityId, boolean isStream)
    throws LogicalPlanException
  {
    super(operatorKind);

    int numOutAttrs;

    this.varId = varId;
    this.entityId = entityId;
    this.isSourceStream = isStream;

    try {
      entityName          = ec.getTableMgr().getTableName(entityId);
      createDDL           = ec.getTableMgr().getCreateDDL(entityId);
      degreeOfParallelism = ec.getTableMgr().getDegreeOfParallelism(entityId);
    }
    catch(MetadataException ex)
    {
      throw new LogicalPlanException(LogicalPlanError.TABLE_NOT_FOUND,
                                   new Object[]{entityName});
    }

    // Determine if this corresponds to a view or a base entity
    isView = false;
    try
    {
      ec.getViewMgr().getView(entityId);
      isView = true;
    } catch (MetadataException e)
    {
      /*
      if (e.getErrorCode() != MetadataError.INVALID_VIEW_IDENTIFIER)
        throw e;
      */
    }

    if (!isView)
    {
      try {
        source = ec.getTableMgr().getTableSource(entityId);
      }
      catch(MetadataException ex)
      {
        throw new LogicalPlanException(LogicalPlanError.TABLE_NOT_FOUND,
                                       new Object[]{entityName});
      }
    }

    try {
      // Number of attributes in the entity's  schema
      numOutAttrs = ec.getTableMgr().getNumAttrs(entityId);
    } catch (MetadataException ex) {
      throw new LogicalPlanException(LogicalPlanError.TABLE_ATTR_NOT_FOUND,
                                   new Object[]{entityName});
    }

    // Determine the output schema
    // if Stream, Include ELEMENT_TIME pseudo column
    super.setNumOutAttrs(isStream ? numOutAttrs+1 : numOutAttrs);
    ArrayList<Attr> attr = getOutAttrs();

    try {
      for (int a = 0; a < numOutAttrs; a++) {
        Datatype dt = ec.getTableMgr().getAttrType( entityId, a);
        String name = ec.getTableMgr().getAttrName(entityId, a);
        AttrNamed outAttr = new AttrNamed(dt);

        // TODO: later not sure if operatorKind needs to be stored.
        outAttr.setVarId(varId);
        outAttr.setAttrId(a);
        outAttr.setActualName(entityName+"."+name);
        attr.set(a, outAttr);
      }
    } catch (MetadataException ex) {
        throw new LogicalPlanException(LogicalPlanError.TABLE_ATTR_NOT_FOUND,
                                     new Object[]{entityName});
      }

    // Handle ELEMENT_TIME
    if (isStream) 
    {
      AttrNamed outAttr = new AttrNamed(Datatype.BIGINT);
      outAttr.setVarId(varId);
      outAttr.setAttrId(numOutAttrs);
      attr.set(numOutAttrs, outAttr);
    }
    this.setOutAttrs(attr);

  }

  // GETTERS and SETTERS

  public String getSource()
  {
    return source;
  }

  public boolean isSourceStream()
  {
    return isSourceStream;
  }

  public boolean isView()
  {
    return isView;
  }

  public int getEntityId() 
  {
    return entityId;
  }

  public void setEntityId(int entityId) 
  {
    this.entityId = entityId;
  }
  
  public void setEntityName(String name)
  {
    this.entityName = name;
  }
  
  public String getEntityName()
  {
    return this.entityName;
  }

  public int getVarId() 
  {
    return varId;
  }

  public void setVarId(int varId) 
  {
    this.varId = varId;
  }

  public int getDegreeOfParallelism()
  {
    return degreeOfParallelism;
  }

  public String getCreateDDL()
  {
    return createDDL;
  }

  @Override
  public void setSourceLineages()
  {
    if(sourceLineage == null)
    {
      sourceLineage = new ArrayList<LogOpt>();
      sourceLineage.add(this);
    }
  }
}
