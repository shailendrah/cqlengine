/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPViewDefnNode.java /main/7 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to a DDL for a view

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/31/12 - view defn to have event identifier column and
                           isArchived flag
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     02/09/09 - adding toQCXml
    parujain    08/11/08 - error offset
    sbishnoi    06/11/08 - modifying constructor to take referencing query text
    sbishnoi    06/07/07 - fix xlint warnings.
    najain      05/09/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPViewDefnNode.java /main/7 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.Iterator;
import java.util.List;

/**
 * Parse tree node corresponding to a DDL for a view
 *
 * @since 1.0
 */
public class CEPViewDefnNode implements CEPParseTreeNode 
{
  /* 
   * The user can create the view in the following ways:
   *   create view V as select .... from S,R... 
   *   create view V(s1, s2, ....) as select .... from S,R...
   *   create view V(s1 int, s2 int, s3 char(20)...) as
   *     select .... from S,R....
   *     
   *   However, currently we do not support the last syntax where an
   *   conversion can be done. This can be supported later.
   */
  
  /** The name of the view */
  protected String name;

  /** The attribute specification list */
  protected CEPAttrSpecNode[] attrSpecList;

  /** The list of view attribute names */
  protected String[] attrNameList;

  /** The query associated with the view */
  protected CEPQueryNode query;
  
  /** Referenced query's text */  
  protected String queryTxt;
  
  /** True if this is an archived view */
  protected boolean isArchived = false;
  
  /** event identifier column name. applicable for only archived view */
  String eventIdColName = null;
  
  protected int startOffset;
  
  protected int endOffset;

  public CEPViewDefnNode() {}
  
  /**
   * Constructor of View Definition Node
   * @param name : name of view
   */
  public CEPViewDefnNode(CEPStringTokenNode token)
  {
    this.name = token.getValue();
    setStartOffset(token.getStartOffset());
    setEndOffset(token.getEndOffset());
  }
  
  
  // Setter Methods  
  /**
   * @param attrName The attrName to set.
   */
  public void setAttrNameList(List<CEPStringTokenNode> attrNameList)
  {
    this.attrNameList = new String[attrNameList.size()];
    int size = attrNameList.size();
    if(size > 0)
      setEndOffset(attrNameList.get(size-1).getEndOffset());
    Iterator<CEPStringTokenNode> iter = attrNameList.iterator();
    int i =0;
    while(iter.hasNext())
    {
      this.attrNameList[i] = iter.next().getValue();
      i++;
    }
  }

  /**
   * @param attrSpec The attrSpec to set.
   */
  public void setAttrSpecList(List<CEPAttrSpecNode> attrSpecList)
  {
    this.attrSpecList = (CEPAttrSpecNode[])(attrSpecList.toArray(new CEPAttrSpecNode[0]));
    if(!attrSpecList.isEmpty())
      setEndOffset(attrSpecList.get(attrSpecList.size()-1).getEndOffset());
  }

  /**
   * Set Parsed Query Node
   * @param query Parsed query node of view's referenced query
   */
  public void setQueryNode(CEPQueryNode query){
    this.query = query;
  }
  
  /**
   * Set Query Text
   * @param queryTxt
   */
  public void setQueryTxt(String queryTxt){
    this.queryTxt = queryTxt;
  }
  
  /**
   * set is archived
   * @param val
   */
  public void setIsArchived(boolean val)
  {
    this.isArchived = val;
  }
  
  /**
   * set event id column name.
   * @param nm
   */
  public void setEventIdColName(String nm)
  {
    this.eventIdColName = nm;
  }
  
  // Getter Methods
  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return Returns the query.
   */
  public CEPQueryNode getQueryNode()
  {
    return query;
  }

  /**
   * @return Returns the attrNameList.
   */
  public String[] getAttrNameList()
  {
    return attrNameList;
  }

  /**
   * @return Returns the attrSpecList.
   */
  public CEPAttrSpecNode[] getAttrSpecList()
  {
    return attrSpecList;
  }
  
  /**
   * Get query-text of the view's reference query 
   * @return view's referenced query Text;
   * e.g. create view v1(..) as select * from S; 
   *      Here queryTxt will be "select * from S" ;;
   *      
   *      create view v1(..) as query q1;
   *      Here queryTxt will be null
   */
  public String getQueryTxt() {
    return queryTxt;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  /**
   * 
   * @return true if this is archived view
   */
  public boolean isArchived()
  {
    return this.isArchived;
  }
  
  /**
   * 
   * @return event identifier column name.
   */
  public String getEventIdColName()
  {
    return this.eventIdColName;
  }
  
  public int toQCXml(StringBuffer queryXml, int operatorID)
  {
    return this.query.toQCXML(queryXml, operatorID);
  }
}
