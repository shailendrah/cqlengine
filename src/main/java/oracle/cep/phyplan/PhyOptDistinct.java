/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptDistinct.java /main/11 2012/09/25 06:20:28 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Distinct Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/13/12 - implement canConstructQuery()
 udeshmuk    08/26/11 - propagate event identifier col name for archived rel
 udeshmuk    06/26/11 - support for archived relation
 hopark      10/09/08 - remove statics
 hopark      10/25/07 - set synopsis
 sbishnoi    05/11/07 - modify implementation
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/24/06 - add getOutputSyn
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor celanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Move Synopsis from execution to phyplan 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptDistinct.java /main/11 2012/09/25 06:20:28 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.service.ExecContext;


/**
 * Distinct Physical Operator 
 */
public class PhyOptDistinct extends PhyOpt {
  private int selectStringEndIdx;
  private String projectClauseOfChild;
  private String[] aliases = null;

  public PhyOptDistinct(ExecContext ec, PhyOpt input) 
  throws PhysicalPlanException  
  {
    super(ec, PhyOptKind.PO_DISTINCT, input, true, true);
  }
  
    
  public PhySynopsis getOutputSyn() {
    return getSynopsis(0);
  }
  
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append("<name> DupElim </name>\n");
    xml.append("<lname> Duplicate Elimination-Distinct </lname>\n");
    xml.append(super.getXMLPlan2());
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getOutputSyn() == syn);
      return PhySynPos.OUTPUT.getName();
  }
   
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptDistinct))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptDistinct distinctOpt = (PhyOptDistinct)opt;
  
    assert distinctOpt.getOperatorKind() == PhyOptKind.PO_DISTINCT;
  
    if(distinctOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(distinctOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return true;
    
  }

  //archived relation support related
  public boolean isStateFul()
  {
    return true;
  }
  
  public boolean canBeQueryOperator() throws CEPException 
  {
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    PhyOpt[] children = this.getInputs();
    
    String childQuery = children[0].getOutputSQL();
    if(childQuery == null)
      return false;
    
    //allocate aliases array - used if this is view root
    //contains the view attr names.
    int numEntries = 0;
    if(children[0].isEventIdColAddedToProjClause())
      numEntries = children[0].getArchiverProjEntries().size() - 1;
    else
      numEntries = children[0].getArchiverProjEntries().size();
    aliases  = new String[numEntries];
    if(this.isView)
    {
      Integer[] depViewIds =
        execContext.getDependencyMgr().getDependents(q.getId(), 
                                                     DependencyType.VIEW);
      
      assert depViewIds.length == 1 :"More than one views dependent on query "+
                                     q.getName();
      
      String[] attrNames = 
        execContext.getViewMgr().getAttrNames(depViewIds[0]);
      String eventIdColNm = 
        execContext.getViewMgr().getView(depViewIds[0]).getEventIdColName();
      
      assert eventIdColNm != null : "eventId column name cannot be null"; 
      assert aliases.length == attrNames.length;
      
      this.setEventIdColName(eventIdColNm);
  
      int i=0;
      for(String aName : attrNames)
      {
        if(aName.equalsIgnoreCase(eventIdColNm))
          this.setEventIdColNum(i);
        aliases[i++] = aName;
      }
      //we are not explicitly adding the event id here.
      this.setEventIdColAddedToProjClause(false);
    }
    
    int idx = childQuery.indexOf("select ");
    
    //get the project clause of the existing query for use as groupby clause 
    selectStringEndIdx = idx + "select".length();
    int fromStringStartIdx = childQuery.indexOf(" from ");
    
    projectClauseOfChild 
      = new String(childQuery.substring(selectStringEndIdx, 
                                        fromStringStartIdx));
    
    //If the child operator sql has * in the project clause instead of
    //attribute names then distinct cannot construct its query so return false.
    if(projectClauseOfChild.trim().equals("*"))
      return false;
    else
      return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    this.projEntries = new LinkedList<String>();
    this.projTypes = new LinkedList<Datatype>();
    PhyOpt[] children = this.getInputs();
    this.setArchiverName(children[0].getArchiverName());
    if(!this.isView)
    {  
      //if this operator were a view root event id fields would be set in 
      //canConstructQuery().
      this.setEventIdColName(children[0].getEventIdColName());
      //distinct won't add the event id column to the proj clause
      //of its output SQL query.
      this.setEventIdColAddedToProjClause(false);
      this.setEventIdColNum(-1);
    }
      
    StringBuffer newProjectClause = new StringBuffer("");
    List<String> tempProjEntries = new LinkedList<String>();
    if(children[0].isEventIdColAddedToProjClause())
    {
      //remove the extra explicitly added event id column at the start in the 
      //project clause of child
      tempProjEntries.addAll(children[0].getArchiverProjEntries());
      tempProjEntries.remove(0);
      this.projTypes.addAll(children[0].getArchiverProjTypes());
      this.projTypes.remove(0);
    }
    else
    {
      tempProjEntries.addAll(children[0].getArchiverProjEntries());
      this.projTypes.addAll(children[0].getArchiverProjTypes());
    }
    
    StringBuffer groupBy = new StringBuffer("");
    boolean commaRequired = false;
    int idx = 0;
    for(String proj : tempProjEntries)
    {
      if(commaRequired)
      {
        groupBy.append(", ");
        newProjectClause.append(", ");
      }
      String actualProj = (proj.split(" as "))[0].trim();
      groupBy.append(actualProj);
      //if view root use aliases
      String projEntry = proj;
      if(this.isView)
        projEntry = actualProj +" as "+aliases[idx++];
      newProjectClause.append(projEntry);
      this.projEntries.add(projEntry);
      commaRequired = true;
    }
    
    /*
     * Here we modify child operator's sql.
     * 1. Add distinct at the start of its project clause
     * 2. Add count(*) at the end of the project clause
     * 3. Append a group by clause at the end with group by expressions
     *    being the original project clause of child.
     */
    StringBuffer query = new StringBuffer("select distinct ");
    query.append(newProjectClause+", count(*) as "+this.getOptName()+"_alias0 ");
    this.projEntries.add("count(*) as "+this.getOptName()+"_alias0");
    this.projTypes.add(Datatype.INT);
    // fromStringStartIdx from canConstructQuery cannot be used
    int fromStringStartIdx = children[0].getOutputSQL().indexOf(" from ");
    query.append(children[0].getOutputSQL().substring(fromStringStartIdx));
    query.append(" group by "+groupBy);
    
    this.setOutputSQL(query.toString());
  }
  
  public int getProjectClauseStartIdx()
  {
    if(this.isQueryOperator)
    {
      int index = outputSQL.indexOf("select distinct ");
      return index+"select distinct ".length();
    }
    return -1;
  }
}
