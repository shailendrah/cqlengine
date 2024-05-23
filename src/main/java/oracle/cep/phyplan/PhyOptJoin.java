/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoin.java /main/24 2015/05/10 20:30:30 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Join Physical Operator in the package oracle.cep.phyplan

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
 vikshukl    06/12/13 - isDependentOnChildSynAndStore check if part of view,
                        otherwise return true always
 udeshmuk    05/27/13 - replace relation name followed by (.) with alias
                        followed by (.)
 udeshmuk    05/06/13 - fix identifier too long : #16766051
 vikshukl    04/17/13 - change isDependentOnChildSynAndStore to account for
                        special join
 vikshukl    02/26/13 - shorten alias prefix in join operators
 udeshmuk    11/12/12 - populate projTypes correctly
 udeshmuk    08/15/12 - add archived relation methods
 vikshukl    08/14/12 - set archived dimension stuff
 vikshukl    08/01/12 - treat archived dimensions differently
 udeshmuk    10/20/11 - API for knowing if this operator uses child's synopsis
 sbishnoi    03/11/10 - fix sharing issue in table function join
 sbishnoi    12/28/09 - table function followup
 sbishnoi    05/26/09 - support for ansi outer join syntax
 sborah      04/19/09 - reorganize sharing hash
 sborah      03/17/09 - define sharingHash
 hopark      10/09/08 - remove statics
 anasrini    09/16/08 - derive output schema from equivalent logical op
 parujain    12/19/07 - inner and outer
 parujain    11/15/07 - external source
 hopark      10/25/07 - set synopsis
 parujain    12/20/06 - operator sharing
 rkomurav    09/13/06 - physynpos OO restructuring
 rkomurav    08/22/06 - XML_visualiser
 rkomurav    08/17/06 - xmldump
 najain      06/21/06 - cleanup
 najain      06/16/06 - cleanup
 najain      05/25/06 - bug fix 
 najain      05/05/06 - sharing support 
 najain      04/20/06 - bug fix 
 najain      04/13/06 - add setSharedStore 
 najain      04/03/06 - implementation
 najain      04/06/06 - cleanup
 anasrini    04/06/06 - constructor cleanup 
 najain      03/24/06 - cleanup
 skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
 najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptJoin.java /main/24 2015/05/10 20:30:30 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptCross;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Constants;
import oracle.cep.common.OuterJoinType;
import oracle.cep.exceptions.CEPException;


/**
 * Join Physical Operator
 */
public class PhyOptJoin extends PhyOptJoinBase {
  
  /**
   * My output schema is concatenation of first numOuterAttrs from left input
   * and numInnerAttrs from right input. (Assert(numOuterAttrs + numInnerAttrs ==
   * numAttrs). We store this information at construction time since the schemas
   * of the input operators can later "expand"
   */
  int         numOuterAttrs;
  
  int         numInnerAttrs;
    
  /** Join predicate */
  private LinkedList<BoolExpr>   preds;
  
  /** Synopsis for the inner relation */
  public static final int INNERSYN_INDEX = 0;
  
  /** Synopsis for the outer relation */
  public static final int OUTERSYN_INDEX = 1;
  
  /** Synopsis for output (required to generate MINUS elements) */
  public static final int JOINSYN_INDEX = 2;
  
  /** type of outer join*/
  private OuterJoinType  outerJoinType;
  
  /** flag to check whether this operator is joining with a table function */
  private boolean isTableFunctionExternalJoin;
  
  /** table function details */
  private TableFunctionInfo tableFunctionInfo;

  private Map<String, String> attrToAliasMap = null;
  
  public Map<String, String> getAttrToAliasMap()
  {
    return this.attrToAliasMap;  
  }

  /**
   * @return Returns the innerSyn.
   */
  public PhySynopsis getInnerSyn() {
    return getSynopsis(INNERSYN_INDEX);
  }
  
  /*
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
  
  /**
   * @return Returns the predicates
   */
  public BoolExpr[] getPredicate() {
    return preds.toArray( new BoolExpr[0]);
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
    preds.addAll( apreds);
  }
  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    // compute the outer join type
    String outerJoinType 
    = (this.getOuterJoinType() != null) ? 
        getOuterJoinType().toString() : 
        "";
        
    String tableFunctionInfoSuffix = 
      (tableFunctionInfo != null) ? ("#" + tableFunctionInfo.toString()) :
                                         ("");
        
    return (this.getOperatorKind() + "#"
                        + getExpressionList(preds) + "#" 
                        + outerJoinType + tableFunctionInfoSuffix
                        );
  }
  
  /**
   * Get preds
   * @return the list of predicates
   */
  public LinkedList<BoolExpr> getPreds() {
    return preds;
  }
  
  /**
   * @return Returns the number of OuterAttrs.
   */
  public int getNumOuterAttrs() {
    return numOuterAttrs;
  }
  
  /**
   * @return Returns the number of InnerAttrs.
   */
  public int getNumInnerAttrs() {
    return numInnerAttrs;
  }
  
  /**
   * @param numOuterAttrs The number of OuterAttrs
   */
  public void setNumOuterAttrs(int numOuterAttrs) {
    this.numOuterAttrs = numOuterAttrs;
  }
  
  public PhyOptJoin(ExecContext ec) throws PhysicalPlanException {
    super(ec, PhyOptKind.PO_JOIN);
  }
  
  public PhyOptJoin(ExecContext ec, LogOpt logPlan, PhyOpt[] phyChildPlans)
  throws PhysicalPlanException, MetadataException {
    
    super(ec, PhyOptKind.PO_JOIN);
    assert logPlan != null;
    assert logPlan.getNumInputs() == 2;
    assert phyChildPlans.length == 2;
    assert logPlan instanceof LogOptCross;
    
    setSynopsis(INNERSYN_INDEX, null);
    setSynopsis(OUTERSYN_INDEX, null);
    setSynopsis(JOINSYN_INDEX, null);
    
    LogOpt logOuter = logPlan.getInput(Constants.OUTER);
    LogOpt logInner = logPlan.getInput(Constants.INNER);
    
    numOuterAttrs = logOuter.getNumOutAttrs();
    numInnerAttrs = logInner.getNumOutAttrs();
    
    createBinarySchema(phyChildPlans[Constants.OUTER], numOuterAttrs,
        phyChildPlans[Constants.INNER], numInnerAttrs);
    
    assert numOuterAttrs + numInnerAttrs == getNumAttrs();
    
    // output is a stream iff both inputs are streams
    setIsStream(phyChildPlans[Constants.OUTER].getIsStream()
        && phyChildPlans[Constants.INNER].getIsStream());
    
    //set External
    setExternal(logPlan.isExternal());
    
    // set if dependent on archived relation
    setArchivedDim(logPlan.isArchivedDim());
    
    // inputs;
    setNumInputs(2);
    getInputs()[Constants.OUTER] = phyChildPlans[Constants.OUTER];
    getInputs()[Constants.INNER] = phyChildPlans[Constants.INNER];
    
    phyChildPlans[Constants.OUTER].addOutput(this);
    phyChildPlans[Constants.INNER].addOutput(this);
    
    // Allocate the list to hold the atomic predicates
    preds = new LinkedList<BoolExpr>();
    
    // Initialize the outer join type to null;
    outerJoinType = null;
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
  
  // archived relation infrastructure related methods
  public boolean isDependentOnChildSynAndStore(PhyOpt input) 
  {
    // In the BPM context, the RHS of the join may maintain synopsis but LHS 
    // won't. So if the right input is select/project/relnsrc then a buffer op
    // needs to be added on RHS of the join but not on the LHS.
    // So we may have to provide input number or operator as a parameter so
    // as to differentiate the two cases.
    // Removed isParentJoinView check as it was redundant. 
    // This method is invoked for buffer operator allocation in PlanManager.
    // Before calling this method we clear the dimension flag if the join
    // is not compliant.
    if (this.isArchivedDim()) 
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
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    //If the query being started is not a view defn query
    //then there is no need to construct query
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
    
    //if this operator is a view root then get the attr names of the view
    //and use them as aliases
    //allocate aliases array - if this is view root
    //contains the view attr names otherwise the generated aliases.
    String[] aliases 
      = new String[this.getNumOuterAttrs()+this.getNumInnerAttrs()];
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
    

    // 16397808: shorten the alias prefixes used for disambiguation of left
    // and right attributes. In some cases the identifiers are already too long
    // and adding a long prefix results in 
    leftSideAlias = "L_" + this.getId();
    rightSideAlias = "R_" + this.getId();
    int aliasIndex = 0;
    selectClause = new StringBuffer();
    //Here, we want fully qualified attr name. 
    boolean oldVal = this.execContext.shouldReturnFullyQualifiedAttrName();
    this.execContext.setReturnFullyQualifiedAttrName(true);
    
    /*
     * ASSUMPTION: left-to-right join evaluation.
     * If FROM clause has multiple tables then we join them left-to-right.
     * e.g. FROM r1,r2,r3 will mean Join(Join(r1,r2), r3)
     * Do we allow a different order of evaluation? - Need to verify. 
     */
    
    if(!(children[0] instanceof PhyOptJoin))
    { //either there is no join hierarchy or this is the bottom-most join in
      //that hierarchy
      
      //extract left and right table names.
      String leftSideTabName = null;
      String attrName = outerAttrs.get(0).getSQLEquivalent(this.execContext);
      if(attrName != null)
      {
        int pos = attrName.lastIndexOf('.');
        leftSideTabName = attrName.substring(0, pos);
      }
      else 
        return false;
      
      String rightSideTabName = null;
      attrName = innerAttrs.get(0).getSQLEquivalent(this.execContext);
      if(attrName != null)
      {
        int pos = attrName.lastIndexOf('.');
        rightSideTabName = attrName.substring(0, pos);
      }
      else
        return false;
      
      attrToAliasMap = new HashMap<String, String>();
      
      //iterate through left attrs       
      for(Attr leftattr : outerAttrs)
      {
        String temp = leftattr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        String projEntry = temp.replace(leftSideTabName+".", leftSideAlias+".");

        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = this.getOptName()+"_alias"+aliasIndex;

        selectClause.append(projEntry+" as "+ alias);
        projEntries.add(projEntry+" as "+alias);
        projTypes.add(leftattr.getType());
        attrToAliasMap.put(temp, alias);
        selectClause.append(", ");
      }
      
      //iterate through right attrs
      boolean commaRequired = false;
      for(Attr rightattr : innerAttrs)
      {
        if(commaRequired)
          selectClause.append(", ");
        
        String temp = rightattr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        String projEntry = temp.replace(rightSideTabName+".", rightSideAlias+".");
        
        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = this.getOptName()+"_alias"+aliasIndex;
        
        selectClause.append(projEntry+" as "+ alias);
        projEntries.add(projEntry+" as "+alias);
        projTypes.add(rightattr.getType());
        attrToAliasMap.put(temp, alias);
        commaRequired = true;
      }
      
      
      //convert predicates to SQL equivalent
      whereClause = new StringBuffer();
      boolean andRequired = false;
      for(BoolExpr pred : preds)
      {
        if(andRequired)
          whereClause.append(" and ");
        String temp = pred.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;

        String leftReplaced = temp.replaceAll(leftSideTabName+"\\.", leftSideAlias+".");
        String wherePred = leftReplaced.replaceAll(rightSideTabName+"\\.", rightSideAlias+"."); 
        whereClause.append(wherePred);
        andRequired = true;
      }
     
      
    }
    else
    {
      //join hierarchy present
      PhyOptJoin childJoin = (PhyOptJoin)children[0];
      String attrName = innerAttrs.get(0).getSQLEquivalent(this.execContext);
      String rightSideTabName = null;
      if(attrName != null)
      {
        int pos = attrName.lastIndexOf('.');
        rightSideTabName = attrName.substring(0, pos);
      }
      else
        return false;
      
      //Here we first transform where predicates using the map entries
      //from child. (not the updated map containing current join's entries.)
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
            // using \b will ensure exact word match.
            wherePred = 
              temp.replaceAll("\\b"+key+"\\b", childJoin.getAttrToAliasMap().get(key));
            temp = wherePred;
          }
          // here no need to add \b because we are already looking for tablename appended by .
          wherePred = temp.replaceAll(rightSideTabName+"\\.", rightSideAlias+".");
        }
        
        whereClause.append(wherePred);
        andRequired = true;
      }
      
      //Now form the SELECT clause
      
      //process left side
      for(Attr leftattr : outerAttrs)
      {
        String temp = leftattr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        String projEntry = childJoin.getAttrToAliasMap().get(temp);
        assert projEntry != null;
        
        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = projEntry;
        selectClause.append(projEntry +" as "+alias);
        projEntries.add(projEntry + " as "+alias);
        projTypes.add(leftattr.getType());
        selectClause.append(", ");
      }
      
      //copy over the child join attr alias map
      this.attrToAliasMap = new HashMap<String, String>();
      this.attrToAliasMap.putAll(childJoin.getAttrToAliasMap());      
      
      //process right side
      boolean commaRequired = false;
      for(Attr rightattr : innerAttrs)
      {
        if(commaRequired)
          selectClause.append(", ");
        
        String temp = rightattr.getSQLEquivalent(this.execContext);
        if(temp == null)
          return false;
        
        String projEntry = temp.replace(rightSideTabName+".", rightSideAlias+".");
        
        String alias = aliases[aliasIndex++];
        if(!this.isView)
          alias = this.getOptName()+"_alias"+aliasIndex;
        
        selectClause.append(projEntry +" as "+alias);
        projEntries.add(projEntry+ " as "+alias);
        projTypes.add(rightattr.getType());
        this.attrToAliasMap.put(temp, alias);
        commaRequired = true;
      }
      
      
    }
    
    //reset to old value
    this.execContext.setReturnFullyQualifiedAttrName(oldVal);
    
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
    String outerJoinType=null;
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
      
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<PhysicalOperatorJoin>");
    sb.append(super.toString());
    
    sb.append("<NumberOfInnerAttributes numInner=\"" + numInnerAttrs + "\" />");
    sb.append("<NumberOfOuterAttributes numOuter=\"" + numOuterAttrs + "\" />");
    
    if (preds.size() != 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
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
    sb.append("</PhysicalOperatorJoin>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    int i;
    StringBuilder xml = new StringBuilder();
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
    xml.append(
        "<property name = \"Is Table Function External Join\" value = \"" +
        isTableFunctionExternalJoin() + "\"/>\n");
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    PhySynopsis outerSyn = getOuterSyn();
    assert(outerSyn == syn || getInnerSyn() == syn);
    if(outerSyn == syn)
      return PhySynPos.LEFT.getName();
    else
      return PhySynPos.RIGHT.getName();
  }
  
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptJoin))
      return false;
    
    // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
    
    PhyOptJoin joinOpt = (PhyOptJoin)opt;
    
    assert joinOpt.getOperatorKind() == PhyOptKind.PO_JOIN;
    
    if(joinOpt.isTableFunctionExternalJoin() ^ this.isTableFunctionExternalJoin())
      return false;
    
    if(joinOpt.getNumInputs() != this.getNumInputs())
      return false;
    
    if(joinOpt.getNumAttrs() != this.getNumAttrs())
      return false;
    
    if(joinOpt.numOuterAttrs != this.numOuterAttrs)
      return false;
    
    if(joinOpt.numInnerAttrs != this.numInnerAttrs)
      return false;
    
    if(joinOpt.preds.size() != preds.size())
      return false;
    
    if(!(joinOpt.getOuterJoinType() == this.getOuterJoinType()))
      return false;
    
    return compareJoinBoolExpr(joinOpt);
    
  }
  
  private boolean compareJoinBoolExpr(PhyOptJoin joinOpt)
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
