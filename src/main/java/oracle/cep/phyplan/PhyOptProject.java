/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptProject.java /main/31 2012/09/25 06:20:29 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Project Physical Operator in the package oracle.cep.phyplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/13/12 - implement canConstructQuery()
 udeshmuk    05/10/12 - use cql query aliases whenever available in archiver
                        query generation
 udeshmuk    08/26/11 - propagate event identifier col name for archived rel
 udeshmuk    06/29/11 - support for archived relation
 udeshmuk    06/28/11 - support for archived relation
 udeshmuk    06/20/11 - reflect the changed method names of archived relation
 udeshmuk    03/28/11 - archived relation support
 sborah      10/05/09 - bigdecimal support
 sborah      04/28/09 - use getLength
 sborah      03/17/09 - define sharingHash
 sborah      03/06/09 - altering getSharingHash()
 hopark      02/17/09 - support boolean as external datatype
 hopark      02/05/09 - support obj type
 hopark      10/07/08 - use execContext to remove statics
 anasrini    09/14/08 - Add isUseless method
 skmishra    06/18/08 - 
 mthatte     05/02/08 - adding special case for XML_CONCAT, PARSE, COMMENT, CDATA
 parujain    04/28/08 - xmlelement support
 udeshmuk    01/30/08 - support for double data type.
 najain      10/19/07 - support xmltype
 hopark      10/25/07 - set synopsis
 mthatte     11/01/07 - using Datatype.getLength()
 mthatte     10/11/07 - using Datatype.getLength()
 parujain    07/06/07 - fix case bug
 sbishnoi    02/27/07 - support for char constants
 parujain    12/18/06 - operator sharing
 hopark      11/16/06 - add bigint datatype
 parujain    10/09/06 - Interval Datatype
 rkomurav    09/12/06 - PhySynPos OO restructuring
 najain      09/11/06 - support ||
 rkomurav    08/23/06 - add getXMLPlan2
 parujain    08/11/06 - timestamp const length
 parujain    08/10/06 - timestamp datatype
 anasrini    06/20/06 - support for function expressions 
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 anasrini    03/29/06 - misc.
 najain      03/24/06 - cleanup
 najain      03/20/06 - cleaunup - change LogOptProject bexpr to a arraylist
 anasrini    03/14/06 - add method getExprs 
 skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
 najain      02/10/06 - Creation

 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptProject.java /main/31 2012/09/25 06:20:29 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedList;

import oracle.cep.extensibility.expr.ExprKind;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.service.ExecContext;

/**
 * Project Physical Operator
 */
public class PhyOptProject extends PhyOpt {
  /** project expressions */
  Expr[] projs;
  private StringBuffer select = null;
  private String[] aliases = null;
  
  /** 
   * Boolean to indicate if this operator should be exempted from useless
   * project optimization.
   * If this project is a part of archived relation based query then 
   * we should have this variable set to true.
   * If the project is added for aggr distinct then also it is set to true.
   * It is set to true because if this project is removed in the useless
   * project optimization then we cannot correctly construct the archiver 
   * query for the operator above it.
   * e.g. distinct above project could have unwanted items in select list
   * or the aliases given by the project would be missing
   */
  private boolean isExemptFromUselessOpt = false;
  
  public PhyOptProject(ExecContext ec, PhyOpt input) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_PROJECT, input, true, true);
    
    ExprAttr projExpr;
    projs = new Expr[getNumAttrs()];
    
    for (int a = 0; a < getNumAttrs(); a++) 
    {
      projExpr = new ExprAttr(getAttrTypes(a));
      
      //projExpr.setType(getAttrTypes()[a]);
      projExpr.getAValue().setInput(0);
      projExpr.getAValue().setPos(a);
      projs[a] = projExpr;
    }
   
  }
  
  public PhyOptProject(ExecContext ec, PhyOpt input, Expr[] projs)
    throws PhysicalPlanException 
  {
    
    super(ec, PhyOptKind.PO_PROJECT, input, false, true);
    this.projs = projs;
    
    // output schema
    int numProjExprs = projs == null ? 0 : projs.length;
    setNumAttrs(numProjExprs);
    for (int a = 0; a < numProjExprs; a++) 
    {
      setAttrMetadata(a, new AttributeMetadata(projs[a].getType(), projs[a].getLength(), 
                                               projs[a].getType().getPrecision(), 0));
      
    }
  }
  
  public void setIsExemptFromUselessOpt(boolean val)
  {
    this.isExemptFromUselessOpt = val;
  }
  
  public boolean getIsExemptFromUselessOpt()
  {
    return this.isExemptFromUselessOpt;
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
          + getExpressionList(projs));
  }
 
  /**
  * A Project operator is useless if it only passes a prefix of
  * its input's out attrs.
  * 1.Its predicates do not do any additional computation 
  *   and is simply of type ATTR_REF
  * 2.Its predicates are a prefix subset of the predicates of the child 
  *   operator where the prefix subset is defined as follows.
  *   Let there be n number of predicates in the project operator. 
  *   If every predicate with index j , (0<=j <=n), points to the predicate
  *   with index j of the child operator, then the predicates 
  *   of the project operator form a prefix subset of the predicates of 
  *   the child operator.
  *   
  *   Eg: Parent Project operator has predicates (c1,c2,c3)
  *       Child operator has predicates (c1,c2,c3,c4,c5)
  *   In this case , Parent Project operator is Useless !
  *   If Parent Project operator has predicates (c2,c1,c3)
  *   or (c1/2 ,c2+5,c3) etc , then it is not Useless
  *
  *  @return 
  *       returns whether the project operator is Useless or not.
  */
  public boolean isUseless()
  {
    if(getIsExemptFromUselessOpt())
    {
      return false;
    }
      
    Expr[] exprs = this.getExprs();
    
    if(exprs.length == 0)
      return false;
    
    for(int i=0; i<exprs.length; i++)
    {
      //condition 1: should be of type ATTR_REF
      if(exprs[i].getKind() == ExprKind.ATTR_REF) 
      {
        ExprAttr expAttr = (ExprAttr)exprs[i];
        //condition 2: should be a subset
        if(expAttr.getAValue().getPos() != i)
          return false;
      }
      else
        return false;
    }

    LogUtil.fine(LoggerType.TRACE,"** USELESS Project Op # " + getId() + ", REMOVING **");

    return true;
  }
  
  // getter methods
  
  /**
   * Get the out synopsis
   * 
   * @return the out synopsis
   */
  public PhySynopsis getOutSyn() {
    return getSynopsis(0);
  }
  
  /**
   * Get the project expressions
   * 
   * @return the project expressions
   */
  public Expr[] getExprs() {
    return projs;
  }
  
  /**
   * @param outSyn
   *            The outSyn to set.
   */
  public void setOutSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<PhysicalOperatorProject>");
    sb.append(super.toString());

    PhySynopsis outSyn = getOutSyn();
    if (outSyn != null) {
      sb.append("<PhysicalSynopsis>");
      sb.append(outSyn.toString());
      sb.append("</PhysicalSynopsis>");
    }
    
    if (projs.length != 0) {
      sb.append("<NumofExpressions numExpr=\"" + projs.length + "\" />");
      for (int i = 0; i < projs.length; i++)
        sb.append(projs[i].toString());
    }
    
    sb.append("</PhysicalOperatorProject>");
    return sb.toString();
  }
  
  // Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    int i;
    StringBuilder xml = new StringBuilder();
    xml.append("<name> Project </name>\n");
    xml.append("<lname> Projection </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Project List\" value = \"");
    if (projs.length != 0) {
      for (i = 0; i < (projs.length - 1); i++) {
        xml.append(projs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(projs[i].getXMLPlan2());
    } else {
      xml.append("(null)");
    }
    xml.append("\"/>");
    return xml.toString();
  }
  
  // get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Project has no Relation Synopsis
    assert (false);
    return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or
   * not
   */
  public boolean isPartialEquivalent(PhyOpt opt) {
    if (!(opt instanceof PhyOptProject))
      return false;
    
    // this is to avoid finding the same operator in PlanManager list
    if (opt.getId() == this.getId())
      return false;
    
    PhyOptProject projectOpt = (PhyOptProject) opt;
    
    assert projectOpt.getOperatorKind() == PhyOptKind.PO_PROJECT;
    
    if (projectOpt.getNumInputs() != this.getNumInputs())
      return false;
    
    if (projectOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    return compareProjectExpr(projectOpt);
  }
  
  private boolean compareProjectExpr(PhyOptProject opt) {
    for (int i = 0; i < getNumAttrs(); i++) {
      
      if (projs[i].getKind() != opt.projs[i].getKind())
        return false;
      
      if (projs[i].getType() != opt.projs[i].getType())
        return false;
      
      if (getAttrLen(i) != opt.getAttrLen(i))
        return false;
      
      if (!projs[i].equals(opt.projs[i]))
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
    
    this.projEntries = new LinkedList<String>();
    this.projTypes = new LinkedList<Datatype>();
    if(this.getInputs()[0] instanceof PhyOptProject)
    {
      String[] childAliases = ((PhyOptProject)this.getInputs()[0]).getAliases();
      for(Expr projExpr : projs)
      {
        if(projExpr instanceof ExprAttr)
        {
          ExprAttr attrProjExpr = (ExprAttr) projExpr;
          //lookup expr in child out attrs if alias not present
          attrProjExpr.setActualName(childAliases[attrProjExpr.getAValue().getPos()]);
          
        }
      }
    }
    
    //if view root then get the attr names of the view and use them as aliases
    aliases = new String[projs.length];
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
    
    select = new StringBuffer();
    int aliasIndex = 0;
    if(projs != null)
    {
      boolean commaRequired = false;
      for(int p=0; p < projs.length; p++)
      {
        Expr currExpr = projs[p];
        if(commaRequired)
          select.append(", ");
        String temp = currExpr.getSQLEquivalent(this.execContext);
        if(temp == null)
        {
          select = null;
          return false;
        }
        else
        {
          
          if(currExpr.getAlias() != null)
          { //use the alias in CQL query if it was provided
            if(!this.getIsView())
              aliases[p] = currExpr.getAlias();
            select.append(temp+" as "+aliases[p]);  
            projEntries.add(temp+" as "+aliases[p]);
          }
          else
          { //for attrExpr use the sqlEquivalent and for others generate alias
            if(currExpr.getKind() == ExprKind.ATTR_REF)
            {
              if(!this.getIsView())
                aliases[p] = temp;
              select.append(temp+" as "+aliases[p]);             
              projEntries.add(temp+" as "+aliases[p]);
            }
            else
            {
              if(!this.getIsView())
                aliases[p] = this.getOptName()+"_alias"+aliasIndex++;
              select.append(temp+" as "+aliases[p]);
              projEntries.add(temp+" as "+aliases[p]);
            }
          }
          projTypes.add(currExpr.getType()); 
        }
        
        commaRequired = true;
      }
    }
    else
      return false;
   
    return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    
    PhyOpt[] children = this.getInputs();
    this.setArchiverName(children[0].getArchiverName());
    if(!this.isView)
    {
      this.setEventIdColName(children[0].getEventIdColName());
      //always add a column for event identifier to proj list
      //irrespective of whether it is present in the list or not.
      //the added column will always be first in the project list
      //and will be used only to initialize tuple.Id and won't
      //be a part of tuple.
      this.setEventIdColNum(0);
      this.setEventIdColAddedToProjClause(true);
    }
    
    StringBuffer query = new StringBuffer("select ");
    if(!this.isView)
    {
      query.append(this.getEventIdColName()+" as "+this.getEventIdColName()+", "+select);
      projEntries.add(0, this.getEventIdColName()+" as "+this.getEventIdColName());
      projTypes.add(0, Datatype.BIGINT);
    }
    else
      query.append(select);
    query.append(" from ( "+children[0].getOutputSQL()+" ) "+ this.getOptName());
    this.setOutputSQL(query.toString());
  }
  
  public String[] getAliases()
  {
    return aliases;
  }
}
