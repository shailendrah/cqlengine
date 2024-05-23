/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/ArchiverQuery.java /main/1 2011/05/18 04:38:12 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/29/11 - Creation
 */

/**
 *  @version $Header: ArchiverQuery.java 29-mar-2011.10:41:06 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.extensibility.datasource.IArchiver;

public class ArchiverQuery
{
  private StringBuffer selectClause;
  private StringBuffer fromClause;
  private StringBuffer whereClause;
  private StringBuffer groupByClause;
  private StringBuffer orderByClause;
  private StringBuffer havingClause;
  private boolean isDistinct;
  private List<IArchiver> archivers;
  
  public ArchiverQuery()
  {
    selectClause = null;
    fromClause   = null;
    whereClause  = null;
    groupByClause = null;
    orderByClause = null;
    havingClause  = null;
    isDistinct = false;
    archivers = null;
  }
  
  public StringBuffer getSelectClause() {
    return selectClause;
  }

  public void setSelectClause(StringBuffer selectClause) {
    if(selectClause != null)
    {
      if(this.selectClause != null)
        this.selectClause.append(", "+selectClause.toString());
      else
        this.selectClause = selectClause;
    }
  }

  public StringBuffer getFromClause() {
    return fromClause;
  }

  public void setFromClause(StringBuffer fromClause) {
    if (fromClause != null) {
      if (this.fromClause != null)
        this.fromClause.append(", " + fromClause.toString());
      else
        this.fromClause = fromClause;
    }
  }

  public StringBuffer getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(StringBuffer whereClause) {
    if(whereClause != null)
    {
      if(this.whereClause != null)
        this.whereClause.append(" AND "+whereClause.toString());
      else
        this.whereClause = whereClause;
    }
  }

  public StringBuffer getGroupByClause() {
    return groupByClause;
  }

  public void setGroupByClause(StringBuffer groupByClause) {
    if(groupByClause != null)
    {
      if(this.groupByClause != null)
        this.groupByClause.append(", "+groupByClause.toString());
      else
        this.groupByClause = groupByClause;
    }
  }

  public StringBuffer getOrderByClause() {
    return orderByClause;
  }

  public void setOrderByClause(StringBuffer orderByClause) {
    if(orderByClause != null)
    {
      if(this.orderByClause != null)
        this.orderByClause.append(", "+orderByClause.toString());
      else
        this.orderByClause = orderByClause;
    }
  }

  public StringBuffer getHavingClause() {
    return havingClause;
  }

  public void setHavingClause(StringBuffer havingClause) {
    if(havingClause != null)
    {
      if(this.havingClause != null)
        this.havingClause.append(" AND "+havingClause.toString());
      else
        this.havingClause = havingClause;
    }
  }

  public boolean isDistinct() {
    return isDistinct;
  }

  public void setDistinct(boolean isDistinct) {
    this.isDistinct = isDistinct;
  }

  public void setArchivers(IArchiver archiver)
  {
    if(archivers == null)
    {
      archivers = new ArrayList<IArchiver>();
    }
    archivers.add(archiver);
  }
  
  public void setArchivers(List<IArchiver> archivers)
  {
    if(this.archivers != null)
      this.archivers.addAll(archivers);
    else
      this.archivers = archivers;
  }
  
  public List<IArchiver> getArchivers()
  {
    return archivers;
  }
  
  public void addDetails(ArchiverQuery query)
  {
    setSelectClause(query.getSelectClause());
    setFromClause(query.getFromClause());
    setWhereClause(query.getWhereClause());
    setGroupByClause(query.getGroupByClause());
     setHavingClause(query.getHavingClause());
    setOrderByClause(query.getOrderByClause());
    this.isDistinct = query.isDistinct();
    setArchivers(query.getArchivers());
  }
  
  public String toString()
  {
    StringBuffer sql = new StringBuffer("SELECT ");
    
    if(selectClause == null)
      sql.append(" * ");
    else
    {
      if(isDistinct)
        sql.append(" DISTINCT "+selectClause.toString());
      else
        sql.append(selectClause);  
    }
    
    assert fromClause != null;
    sql.append(" FROM "+fromClause);
    
    if(whereClause != null)
      sql.append(" WHERE "+whereClause);
    
    if(groupByClause != null)
      sql.append(" GROUP BY "+groupByClause);
    
    if(havingClause != null)
      sql.append(" HAVING "+ havingClause);
    
    if(orderByClause != null)
      sql.append(" ORDER BY "+orderByClause);
    
    System.out.println("Generated Archiver Query : "+sql.toString());
    return sql.toString();
  }
}
