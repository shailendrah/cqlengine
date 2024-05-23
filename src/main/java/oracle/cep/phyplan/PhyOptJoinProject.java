/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoinProject.java /main/21 2015/05/10 20:30:30 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
     Join Project Physical Operator in the package oracle.cep.phyplan

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/10/15 - ensure exact word replacement, prefix, infix, suffix
                           should not be replaced
    udeshmuk    12/04/14 - support outer join in archiver queries
    udeshmuk    07/08/13 - bug 17054511: change the query construction code to
                           handle relation names prefix of each other
    udeshmuk    05/28/13 - replace relation name followed by (.) with alias
                           followed by (.)
    vikshukl    04/17/13 - pass input operator to
                           isDependentOnChildSynAndStore()
    vikshukl    02/26/13 - shorten alias prefix in join operators
    udeshmuk    11/09/12 - set projTypes correctly
    udeshmuk    08/15/12 - add archived relation methods
    udeshmuk    10/20/11 - API for knowing if this operator uses child's
                           synopsis
    sbishnoi    03/11/10 - fix sharing issue in table function join
    sbishnoi    12/28/09 - table function followup
    sborah      10/07/09 - bigdecimal support
    sbishnoi    05/26/09 - ansi syntax support for outer join
    sborah      04/19/09 - reorganize sharing hash
    sborah      03/17/09 - define sharingHash
    hopark      10/09/08 - remove statics
    parujain    12/19/07 - inner and outer
    hopark      10/25/07 - set synopsis
    najain      02/27/07 - bug fix
    parujain    12/20/06 - operator sharing
    rkomurav    09/13/06 - physynpos OO restructuring
    rkomurav    08/28/06 - add genXMLPlan2
    rkomurav    08/20/06 - adding toString
    najain      06/16/06 - cleanup
    najain      04/20/06 - bug fix 
    najain      04/06/06 - cleanup
    anasrini    04/06/06 - constructor cleanup 
    najain      03/24/06 - cleanup
    skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
    najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoinProject.java /main/21 2015/05/10 20:30:30 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.OuterJoinType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;

/**
 * Join Project Physical Operator 
 */
public class PhyOptJoinProject extends PhyOptJoinBase {
  
  /** Project expressions */
  Expr[] projs;
 
  /** Join predicate */
  private LinkedList<BoolExpr> preds;
  
  /** PhySynopsis for the inner relation */
  public static final int INNERSYN_INDEX = 0;
  
  /** PhySynopsis for the outer relation */
  public static final int OUTERSYN_INDEX = 1;
  
  /** PhySynopsis for output (required to generate MINUS elements) */
  public static final int JOINSYN_INDEX = 2;
  
  /** type of outer join*/
  private OuterJoinType  outerJoinType;
  
  /** flag to check whether this operator is joining with a table function */
  private boolean isTableFunctionExternalJoin;
  
  /** table function details */
  private TableFunctionInfo tableFunctionInfo;

  private String leftSideAlias;

  private String rightSideAlias;

  private StringBuffer selectClause;

  private StringBuffer whereClause;
  
  /**
   * @return the tableFunctionInfo
   */
  public TableFunctionInfo getTableFunctionInfo()
  {
    return tableFunctionInfo;
  }

  /**
   * @param tableFunctionInfo the tableFunctionInfo to set
   */
  public void setTableFunctionInfo(TableFunctionInfo tableFunctionInfo)
  {
    this.tableFunctionInfo = tableFunctionInfo;
    this.setTableFunctionExternalJoin(tableFunctionInfo != null);
      
  }

  /**
   * @return Returns the join predicates
   */
  public LinkedList<BoolExpr> getPreds() {
    return preds;
  }
  
  /**
   * @param preds join predicates
   */
  public void setPreds(LinkedList<BoolExpr> preds) 
  {
    this.preds = preds;
  }
  
  /**
   * @return Returns the project expressions
   */
  public Expr[] getProjs() {
    return projs;
  }
  
  /**
   * @param projs  Project expressions
   */
  public void setProjs(Expr[] projs) 
  {
    this.projs = projs;
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    /** Used to compute the sharing hash values */
    String projList = "";
    String predList = "";
    
    // compute the projList
    projList = getExpressionList(projs);
    // compute the predList 
    predList = getExpressionList(preds);
    
    // compute the outer join type
    String outerJoinType 
      = (this.getOuterJoinType() != null) ? 
          getOuterJoinType().toString() : 
          "";
    
    String tableFunctionInfoSuffix = 
      (tableFunctionInfo != null) ? ("#" + tableFunctionInfo.toString()) :
                                    ("");
    // compute the sharing Hash value
    return (this.getOperatorKind() + "#"
          + projList + "#"
          + predList + "#"
          + outerJoinType +
          tableFunctionInfoSuffix);
    
  }
  
  /**
   * @return Returns the innerSyn.
   */
  public PhySynopsis getInnerSyn() {
    return getSynopsis(INNERSYN_INDEX);
  }
  
  /**
   * @param innerSyn The innerSyn to set.
   */
  public void setInnerSyn(PhySynopsis innerSyn) {
    setSynopsis(INNERSYN_INDEX, innerSyn);
  }
  
  /**
   * @return Returns the outerSyn.
   */
  public PhySynopsis getOuterSyn() {
    return getSynopsis(OUTERSYN_INDEX);
  }
  
  
  /**
   * @param outerSyn The outerSyn to set.
   */
  public void setOuterSyn(PhySynopsis outerSyn) {
    setSynopsis(OUTERSYN_INDEX, outerSyn);
  }
  
  /**
   * @return Returns the joinSyn.
   */
  public PhySynopsis getJoinSyn() {
    return getSynopsis(JOINSYN_INDEX);
  }
  
  /**
   * @param joinSyn The joinSyn to set.
   */
  public void setJoinSyn(PhySynopsis joinSyn) {
    setSynopsis(JOINSYN_INDEX, joinSyn);
  }
  
  public PhyOptJoinProject(ExecContext ec) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_JOIN_PROJECT);
  }
  
  public boolean getSharedSynType(int idx) {
    assert getNumInputs() > idx;
    assert getNumInputs() == 2;
    return true;
  }
  
  /**
   * The synopsis of the store needs to be shared with its inputs.
   */
  public void synStoreReq()
  {
    PhyStore outerStore = getInputs()[Constants.OUTER].getSharedRelStore();
    assert outerStore != null;
    PhySynopsis outerSyn = getOuterSyn();
    outerSyn.makeStub(outerStore);
    
    PhyStore innerStore = getInputs()[Constants.INNER].getSharedRelStore();
    assert innerStore != null;
    PhySynopsis innerSyn = getInnerSyn();
    innerSyn.makeStub(innerStore);
  }
  
  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    // FIXME: This may need a change. In the BPM context, the RHS of the join 
    //        may maintain synopsis but LHS won't. So if the right input is
    //        select/project/relnsrc then a buffer op needs to be added on RHS
    //        of the join but not on the LHS.
    //        So we may have to provide input number or operator as a param so
    //        as to differentiate the two cases.    
    if (this.isArchivedDim())  // this is a special join operator
    {
      if (input.isArchivedDim())
      {
        if (!(input instanceof PhyOptJoin || 
            input instanceof PhyOptJoinProject))
        {
          // this is a special join and the input is dimension but not a join
          // operator which could happen when a join is an input to another join
          return true;
        }
        else 
        {
          // If the input is another 'dimension' join, then no need to allocate
          // a buffer operator. This is to avoid state initialization
          return false;
        } 
      }
      else 
        return false;
    } 
    else
      return true;
  }
  
  public boolean canBeQueryOperator() throws CEPException
  {
    return false;
  }
  
  public boolean isStateFul()
  {
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    //if the query being started is not a view defn query
    //then there is no need to construct query.
    if(!q.isViewQuery())
      return false;
    
    //First check if both inputs of the join can construct their queries
    PhyOpt[] children = this.getInputs();
    assert children != null;
    if(children[0].getOutputSQL() == null)
      return false;
    if(children[1].getOutputSQL() == null)
      return false;
    
    this.projEntries = new LinkedList<String>();
    this.projTypes = new LinkedList<Datatype>();
  
    //allocate aliases array - used if this is view root
    //contains the view attr names.
    String[] aliases = new String[this.projs.length];
    if(this.getIsView())
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
    
    // 16397808: shorten alias prefix to avoid "identifier too long" 
    // errors. 
    leftSideAlias  = "L_" + this.getId();
    rightSideAlias = "R_" + this.getId();
    
    //Here, we want fully qualified attr name. If not available return false.
    
    boolean oldVal = this.execContext.shouldReturnFullyQualifiedAttrName();
    this.execContext.setReturnFullyQualifiedAttrName(true);
    
    assert outerAttrs != null : "outerattrs should not be null";
    assert innerAttrs != null : "innerAttrs should not be null";
    
    int aliasIndex = 0;
    selectClause = new StringBuffer();
    whereClause = new StringBuffer();
    
    if(!(children[0] instanceof PhyOptJoin))
    {
      //Know the left side table name.
      int pos = -1;
      String leftSideTabName = null;
      String leftAttrFullName = 
        this.outerAttrs.get(0).getSQLEquivalent(this.execContext);
      if(leftAttrFullName != null)
      {
        pos = leftAttrFullName.lastIndexOf('.');
        leftSideTabName = leftAttrFullName.substring(0, pos);
      }
      else
        return false;
          
      //Know the right side table name.
      pos = -1;
      String rightSideTabName = null;
      String rightAttrFullName = 
        this.innerAttrs.get(0).getSQLEquivalent(this.execContext);
      if(rightAttrFullName != null)
      {
        pos = rightAttrFullName.lastIndexOf('.');
        rightSideTabName = rightAttrFullName.substring(0, pos);
      }
      else
         return false;
      
      boolean commaRequired = false;
      for(Expr projExpr : projs)
      {
        if(commaRequired)
          selectClause.append(", ");
        String temp = projExpr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        String leftReplaced = temp.replaceAll(leftSideTabName+"\\.", leftSideAlias+".");
        String projEntry = 
          leftReplaced.replaceAll(rightSideTabName+"\\.", rightSideAlias+".");
        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = this.getOptName()+"_alias_"+aliasIndex;
        selectClause.append(projEntry+" as "+alias);
        projEntries.add(projEntry+" as "+alias);
        projTypes.add(projExpr.getType());
        commaRequired = true;
      }
      
    
      //convert predicates to SQL equivalent
      boolean andRequired = false;
      for(BoolExpr pred : preds)
      {
        if(andRequired)
          whereClause.append(" and ");
        String temp = pred.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        String leftReplaced = temp.replaceAll(leftSideTabName+"\\.", leftSideAlias+".");
        String wherePred = 
          leftReplaced.replaceAll(rightSideTabName+"\\.", rightSideAlias+"."); 
        whereClause.append(wherePred);
        andRequired = true;
      }
     
    }
    else
    {
      //this joinproject is part of join hierarchy
      //assumption : this is view root. i.e. no hierarchy of join projects
      //can't think of a case where we will have hierarchy of join projects
      //FIXME: groupaggr with distinct would have such hierarchy. 
      //Not handled for now. - groupaggr with distinct cannot be in a view defn query
      //so should not be a problem.
      
      //Know the right side table name.
      int pos = -1;
      String rightSideTabName = null;
      String rightAttrFullName = 
        this.innerAttrs.get(0).getSQLEquivalent(this.execContext);
      if(rightAttrFullName != null)
      {
        pos = rightAttrFullName.lastIndexOf('.');
        rightSideTabName = rightAttrFullName.substring(0, pos);
      }
      else
         return false;
      
      boolean commaRequired = false;
      PhyOptJoin childJoin = (PhyOptJoin)children[0];
      
      for(Expr projExpr : projs)
      {
        if(commaRequired)
          selectClause.append(", ");
        String temp = projExpr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        Set<String> keys = childJoin.getAttrToAliasMap().keySet();
        String projEntry = null;
        if((keys != null) && (!keys.isEmpty()))
        {
          for(String key : keys)
          {
            projEntry = 
              temp.replaceAll("\\b"+key+"\\b", childJoin.getAttrToAliasMap().get(key));
            temp = projEntry;
          }
          projEntry = temp.replaceAll(rightSideTabName+"\\.", rightSideAlias+".");
        }
        
        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = this.getOptName()+"_alias_"+aliasIndex;
        selectClause.append(projEntry+" as "+alias);
        projEntries.add(projEntry+" as "+alias);
        projTypes.add(projExpr.getType());
        commaRequired = true;
      }
      
      whereClause = new StringBuffer();
      boolean andRequired = false;
      for(BoolExpr pred : preds)
      {
        if(andRequired)
          whereClause.append(" and ");
        String temp = pred.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        Set<String> keys = childJoin.getAttrToAliasMap().keySet();
        String wherePred = null;
        if((keys != null) && (!keys.isEmpty()))
        {
          for(String key : keys)
          {
            wherePred = 
              temp.replaceAll("\\b"+key+"\\b", childJoin.getAttrToAliasMap().get(key));
            temp = wherePred;
          }
          wherePred = temp.replaceAll(rightSideTabName+"\\.", rightSideAlias+".");
        }
        
        whereClause.append(wherePred);
        andRequired = true;
      }
      
    }
    //reset to old value
    this.execContext.setReturnFullyQualifiedAttrName(oldVal);
    
    return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    PhyOpt[] inputs = this.getInputs();

    //assumption : archiver name is same on both sides
    this.setArchiverName(inputs[0].getArchiverName());
    //We do not need to set event identifier column name and position
    //This is because the method will be called only in case this opt
    //is a part of view definition query.
    //There are two possibilities 
    //1) this opt is a root :isView would be true and canConstructQuery
    //method must have set the eventIdColName and eventIdColNum.
    //2) this opt is NOT a root : The event identifier column won't be needed
    //as this opt cannot actually query. The actual view root will get
    //the name and position from view metadata.
    StringBuffer query = new StringBuffer();
    query.append("select ");
    query.append(selectClause);
    query.append(" from ");
    String outerJoinType  = null;
      
    if(this.getOuterJoinType() != null)
    {
      if(this.getOuterJoinType() == OuterJoinType.LEFT_OUTER)
        outerJoinType = " LEFT OUTER JOIN ";
      else if(this.getOuterJoinType() == OuterJoinType.RIGHT_OUTER)
        outerJoinType = " RIGHT OUTER JOIN ";
      else
        outerJoinType = " FULL OUTER JOIN ";
    }
    String fromClauseSeparator = ", ";
    if(outerJoinType != null)
      fromClauseSeparator = outerJoinType;
    query.append("("+inputs[0].getOutputSQL()+") "+leftSideAlias+fromClauseSeparator);
    query.append("("+inputs[1].getOutputSQL()+") "+rightSideAlias);
    if(whereClause.length() > 0)
    {
      if(outerJoinType == null)
      {  
        query.append(" where ");
        query.append(" "+whereClause);
      }
      else
      {
        query.append(" ON ");
        query.append(" "+whereClause);
      }
    }
    this.setOutputSQL(query.toString());
  }
  
  // toString override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<PhysicalOperatorJoinProject>");
    sb.append(super.toString());
    
    if (preds.size() != 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
    }
    
    if (projs.length != 0) {
      sb.append("<Projection>");
      for (int i = 0; i < projs.length; i++)
        sb.append(projs[i].toString());
      sb.append("</Projection>");
    }
    
    if(this.getOuterJoinType()!= null)
    {
      sb.append("<IsANSIOuterJoin>Yes</IsANSIOuterJoin>");
      sb.append("<OuterJoinType>");
      sb.append(this.getOuterJoinType() );
      sb.append("</OuterJoinType>");
    }
    else
      sb.append("<IsANSIOuterJoin>No</IsANSIOuterJoin>");
    
    sb.append("<InnerSynopsis>");
    PhySynopsis innerSyn = getInnerSyn();
    sb.append(innerSyn.toString());
    sb.append("</InnerSynopsis>");
    
    sb.append("<OuterSynopsis>");
    PhySynopsis outerSyn = getOuterSyn();
    sb.append(outerSyn.toString());
    sb.append("</OuterSynopsis>");
    
    PhySynopsis joinSyn = getJoinSyn();
    if(joinSyn != null) {
      sb.append("<JoinSynopsis>");
      sb.append(joinSyn.toString());
      sb.append("</JoinSynopsis>");
    }
    sb.append("<IsTableFunctionExternalJoin value=\"" + 
        isTableFunctionExternalJoin() + "\" />");
    
    sb.append("</PhysicalOperatorJoinProject>");
    return sb.toString();
  }
  
//Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append("<name> Join </name>\n");
    xml.append("<lname> Binary Join </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append("<property name = \"Join Predicate\" value = \"");
    if (preds.size() != 0) {
      for (i = 0; i < (preds.size() - 1); i++) {
        xml.append(preds.get(i).getXMLPlan2());
        xml.append(", ");
      }
      xml.append(preds.get(i).getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    
    xml.append("<property name = \"Project List\" value = \"");
    if(projs.length != 0) {
      for(i = 0; i < (projs.length - 1); i++) {
        xml.append(projs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(projs[i].getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    xml.append(
        "<property name = \"Is Table Function External Join\" value = \"" +
        isTableFunctionExternalJoin() + "\"/>\n");
    
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    PhySynopsis innerSyn = getInnerSyn();
    assert(innerSyn == syn || getOuterSyn() == syn);
    if(innerSyn == syn)
      return PhySynPos.RIGHT.getName();
    else
      return PhySynPos.LEFT.getName();
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptJoinProject))
      return false;
    
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
    
    PhyOptJoinProject joinOpt = (PhyOptJoinProject)opt;
    
    assert joinOpt.getOperatorKind() == PhyOptKind.PO_JOIN_PROJECT;
    
    if(joinOpt.isTableFunctionExternalJoin() ^ this.isTableFunctionExternalJoin())
      return false;
    
    if(joinOpt.getNumInputs() != this.getNumInputs())
      return false;
    
    if(joinOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    if(joinOpt.preds.size() != preds.size())
      return false;
    
    if(joinOpt.projs.length != projs.length)
      return false;
    
    if(!(joinOpt.getOuterJoinType() == this.getOuterJoinType()))
      return false;
    
    if(!(compareProjectExpr(joinOpt)))
      return false;
    
    return compareJoinBoolExpr(joinOpt);
    
  }
  
  private boolean compareJoinBoolExpr(PhyOptJoinProject joinOpt)
  {
    Iterator<BoolExpr> iter1 = preds.iterator();
    Iterator<BoolExpr> iter2 = joinOpt.preds.iterator();
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
  
  private boolean compareProjectExpr(PhyOptJoinProject opt)
  {
    for(int i=0; i < getNumAttrs(); i++)
    {
      
      if(projs[i].getKind() != opt.projs[i].getKind())
        return false;
      
      if(!projs[i].getType().equals(opt.projs[i].getType()))
        return false;
      
      if(this.getAttrLen(i) != opt.getAttrLen(i))
        return false;
      
      if(! projs[i].equals(opt.projs[i]))
        return false;
    }
    return true;
  }

  /**
   * @return the outerJoinType
   */
  public OuterJoinType getOuterJoinType()
  {
    return outerJoinType;
  }

  /**
   * @param outerJoinType the outerJoinType to set
   */
  public void setOuterJoinType(OuterJoinType outerJoinType)
  {
    this.outerJoinType = outerJoinType;
  }
  
  /**
   * @return the isTableFunctionExternalJoin
   */
  public boolean isTableFunctionExternalJoin()
  {
    return isTableFunctionExternalJoin;
  }

  /**
   * @param isTableFunctionExternalJoin the isTableFunctionExternalJoin to set
   */
  public void setTableFunctionExternalJoin(boolean isTableFunctionExternalJoin)
  {
    this.isTableFunctionExternalJoin = isTableFunctionExternalJoin;
  }
  
}
