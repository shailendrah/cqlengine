/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSelect.java /main/16 2013/08/14 21:15:53 vikshukl Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    06/17/13 - fix sharing
 vikshukl    08/28/12 - propagate the name
 udeshmuk    08/13/12 - implement canConstructQuery()
 vikshukl    08/07/12 - archived dimension propagation
 udeshmuk    05/17/12 - Use child SQL and append WHERE clause
 udeshmuk    08/28/11 - propagate event identifier col name for archived rel
 udeshmuk    06/26/11 - support for archived relation
 udeshmuk    06/23/11 - enhance archived relation support
 udeshmuk    06/20/11 - reflect the changed method names of archived relation
 udeshmuk    03/28/11 - archived relation support
 sbishnoi    05/04/09 - typo: getSignature
 sborah      04/19/09 - reorganize sharing hash
 sborah      03/17/09 - define sharingHash
 sborah      03/05/09 - altering getSharingHash()
 hopark      10/09/08 - remove statics
 anasrini    09/16/08 - numAttrs should be based on equivalent LogOpt
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/28/06 - adadd genXMLPlan2
 anasrini    08/03/06 - add an output synopsis and store
 dlenkov     04/24/06 - 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      04/04/06 - merge conflicts 
 anasrini    03/30/06 - support for select operator 
 najain      03/24/06 - cleanup
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSelect.java /main/16 2013/08/14 21:15:53 vikshukl Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.phyplan;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.logplan.LogOpt;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;


/**
 * Select Physical Operator
 *
 * @since 1.0 
 */
public class PhyOptSelect extends PhyOpt {

  private LinkedList<BoolExpr> preds;
  
  private StringBuffer         select = null;

  private String[] aliases = null;

  /**
   * Constructor
   * @param ec TODO
   * @param input the physical operator that is the input to this operator
   * @param logop the equivalent logical operator
   */
  public PhyOptSelect(ExecContext ec, PhyOpt input, LogOpt logop)
    throws PhysicalPlanException 
  {
    super(ec, PhyOptKind.PO_SELECT, input, logop, true, true);
    this.isArchivedDim = logop.isArchivedDim();
    // allocate the list to hold the atomic predicates
    preds = new LinkedList<BoolExpr>();
  }

  /**
   * Add an atomic predicate
   * <p>
   * An atomic predicate is a boolean expression that does not involve
   * and logical operators such as AND. 
   * @param pred the atomic predicate to be added
   */
  public void addAtomicPred(BoolExpr pred) 
  {
    preds.add(pred);
  }

  /**
   * Add a list of predicates
   * <p>
   * @param apreds a list of predicates to be added
   */
  public void appendPreds( LinkedList<BoolExpr> apreds) 
  {
    preds.addAll(apreds);
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return (this.getOperatorKind() + "#"
          + getExpressionList(preds));
  }

  /**
   * @param outSyn The outSyn to set.
   */
  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }

  /**
   * Get preds
   * @return the list of predicates
   */
  public LinkedList<BoolExpr> getPreds() {
      return preds;
  }

  /**
   * Get the predicate 
   * @return the predicate in the form of an array of atomic predicates
   */
  public BoolExpr[] getPredicate() {
    return preds.toArray(new BoolExpr[0]);
  }

  /**
   * Get the out synopsis
   * @return the out synopsis
   */
  public PhySynopsis getOutSyn() {
    return getSynopsis(0);
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<Select>");
    sb.append(super.toString());

    if (preds.size() != 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
    }

    sb.append("</Select>");
    return sb.toString();
  }
  
//Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append("<name> Select </name>\n");
    xml.append("<lname> Selection </lname>\n");
    xml.append(super.getXMLPlan2());
    assert(preds != null);
    xml.append("<property name = \"Predicate\" value = \"");
    if (preds.size() != 0) {
      for (i = 0; i < (preds.size() - 1); i++) {
        xml.append(preds.get(i).getXMLPlan2());
        xml.append(", ");
      }
      xml.append(preds.get(i).getXMLPlan2());
    }
    xml.append("\"/>\n");
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    assert(getOutSyn() == syn);
    return PhySynPos.RIGHT.getName();
  }

  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptSelect))
      return false;
  
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptSelect selectOpt = (PhyOptSelect)opt;
  
    assert selectOpt.getOperatorKind() == PhyOptKind.PO_SELECT;
  
    if(selectOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(selectOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    if(selectOpt.preds.size() != preds.size())
      return false;
    
    if (selectOpt.isArchivedDim() || this.isArchivedDim())
      return false;
    
    return compareSelectBoolExpr(selectOpt);
    
  }

  private boolean compareSelectBoolExpr(PhyOptSelect selectOpt)
  {
    Iterator<BoolExpr> iter1 = preds.iterator();
    Iterator<BoolExpr> iter2 = selectOpt.preds.iterator();
    while(iter1.hasNext())
    {
      assert iter2.hasNext();
      BoolExpr expr1 = (BoolExpr)iter1.next();
      BoolExpr expr2 = (BoolExpr)iter2.next();
      if(!(expr1.equals(expr2)))
        return false;
      
    }
    return true;
  }
  
  //archived relation support related
  public boolean isStateFul()
  {
    return false;
  }

  public boolean canBeQueryOperator() throws CEPException
  {
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    //first check if input SQL is null, if so return false.
    PhyOpt[] inputs = this.getInputs();
    if(inputs[0].getOutputSQL() == null)
      return false;
    
    boolean andRequired = false;
    select = new StringBuffer();
    for(BoolExpr pred : preds)
    {
      if(andRequired)
        select.append(" AND ");
      
      String temp = pred.getSQLEquivalent(this.execContext);
      if(temp == null)
      {
        select = null;
        return false;
      }
      else
        select.append(temp);
      
      andRequired = true;
    }
    
    //FIXME: what happens if project is input? do we need to remove extra event id?
    aliases   = new String[this.getInputs()[0].getArchiverProjEntries().size()];
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
      this.setEventIdColName(children[0].getEventIdColName());
      this.setEventIdColNum(children[0].getEventIdColNum());
      this.setEventIdColAddedToProjClause(
        children[0].isEventIdColAddedToProjClause());
    }
    
    //get project clause in child sql 
    StringBuffer projectAliases = new StringBuffer();
    List<String> childProjEntries = children[0].getArchiverProjEntries();
    for(int i=0; i < childProjEntries.size(); i++)
    {
      String[] prjEntry = childProjEntries.get(i).split(" as ");
      
      if(this.isView)
      {
        projectAliases.append(prjEntry[1]+" as "+aliases[i]);
        projEntries.add(prjEntry[1]+" as "+aliases[i]);
      }
      else
      {
        projectAliases.append(prjEntry[1]+" as "+prjEntry[1]);
        projEntries.add(prjEntry[1]+" as "+prjEntry[1]);
      }
      
      if(i != childProjEntries.size() - 1)
        projectAliases.append(" , ");
    }
    //copy over the projtypes in child
    this.projTypes.addAll(children[0].getArchiverProjTypes());
    
    StringBuffer query = new StringBuffer("select "+projectAliases.toString()+" from ( ");
    query.append(children[0].getOutputSQL()+" ) "+this.getOptName());
    if((select != null) && (select.length() > 0))
      query.append(" where "+select.toString());
    this.setOutputSQL(query.toString());
  }
}
