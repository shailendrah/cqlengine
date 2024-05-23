/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptValueWin.java /main/19 2013/05/07 18:03:18 sbishnoi Exp $ */

/* Copyright (c) 2008, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    04/17/13 - pass input operator to
                           isDependentOnChildSynAndStore()
    udeshmuk    08/13/12 - implement canConstructQuery()
    udeshmuk    05/24/12 - getSnapshotTime and getColType methods added
    udeshmuk    05/17/12 - use child SQL and append WHERE clause
    sbishnoi    04/04/12 - use the config parameter to get the target sql type
    udeshmuk    10/20/11 - API for knowing if this operator uses child's
                           synopsis
    udeshmuk    10/03/11 - currentHour and currentPeriod query generation
    udeshmuk    09/18/11 - divide rangeVal by 10^9 instead of 1000 as the scale
                           has changed to nanos
    udeshmuk    08/28/11 - propagate event identifier col name for archived rel
    udeshmuk    06/21/11 - use getSQLEquivalent
    udeshmuk    06/20/11 - reflect the changed method names of archived
                           relation
    hopark      04/29/11 - fix SQL_TSI_SECOND
    sbishnoi    04/11/11 - support for archived relation
    sbishnoi    03/01/11 - adding flag for relation window
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/10/09 - modify getSharingHash()
    hopark      10/09/08 - remove statics
    parujain    07/07/08 - Value based windows
    parujain    07/07/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptValueWin.java /main/19 2013/05/07 18:03:18 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.ArithOp;
import oracle.cep.common.CompOp;
import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.SQLType;
import oracle.cep.common.ValueWindowType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.DependencyType;
import oracle.cep.metadata.Query;
import oracle.cep.phyplan.window.PhyValueWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.service.ExecContext;


/**
 * Value based Window Physical Operator 
 */
public class PhyOptValueWin extends PhyOpt {
 
  /** Window specifications */
  private PhyWinSpec winSpec;
  
  /** Flag to check if the input is a relation */
  private boolean isWindowOverRelation;

  private String[] aliases = null;
  
  private static final String SPACE_CONSTANT = " ";

  /** Synopsis for the window */
  
  public PhyWinSpec getWinSpec() {
    return winSpec;
  }

  public void setWinSpec(PhyWinSpec spec) 
  {
    this.winSpec = spec;
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
          + winSpec.toString());
  }

  /**
   * Return the win synopsis; WinSynopsis will be used when this operator is a
   * window over stream
   * @return
   */
  public PhySynopsis getWinSyn() {
    return getSynopsis(0);
  }

  public void setWinSyn(PhySynopsis winSyn) {
    setSynopsis(0, winSyn);
  }

  /**
   * Return the lineage synopsis;Lineage synopsis will be used if this is a
   * window over relation
   * @return
   */
  public PhySynopsis getOutputSyn() {
    return getSynopsis(0);
  }
  
  public void setOutputSyn(PhySynopsis outSyn) {
    setSynopsis(0, outSyn);
  }
  
  //link syn and store
  public void linkSynStore() {
    assert !this.isWindowOverRelation;
    PhySynopsis winSyn = getWinSyn();
    winSyn.makeStub(this.getStore());
  }

  @Override
  public boolean isDependentOnChildSynAndStore(PhyOpt input)
  {
    if(!this.isWindowOverRelation)
      return true;
    else
      return false;
  }
  
  public PhyOptValueWin(ExecContext ec, PhyWinSpec windowSpec, PhyOpt[] phyChildPlans)
      throws PhysicalPlanException {

    super(ec, PhyOptKind.PO_VALUE_WIN);
    // Initializations
    setStore(null);
    setInstOp(null);
    setWinSyn(null);

    // window specification:
    setWinSpec(windowSpec);
    
    setWindowOverRelation(!phyChildPlans[0].getIsStream());
    
    // output schema = input schema :: since the instance of the class was 
    // allocated by the InterpreterFactory, we need to copy from the first 
    // child. 
    PhyValueWinSpec spec = (PhyValueWinSpec) winSpec;
    if(isWindowOverRelation && spec.isWindowOnElementTime())
      copy(phyChildPlans[0], true);
    else
      copy(phyChildPlans[0]);

    // output is a relation, not a stream
    setIsStream(false);

    // input:
    setNumInputs(1);
    getInputs()[0] = phyChildPlans[0];

    try 
    {
      phyChildPlans[0].addOutput(this);
    } 
    catch (PhysicalPlanException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      // TODO::: just ignore it for now
    } 
    
   // Set timeout required to true if it is a value window on relation
    if(isWindowOverRelation)
    {
      LogUtil.fine(LoggerType.TRACE, this.getOptName() + " is setting timeout required to true");
      setHbtTimeoutRequired(true); 
    }
  }

  public boolean getSharedSynType(int idx) {
    return true;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorValueWindow>");
    sb.append(super.toString());

    sb.append(winSpec.toString());
    sb.append("<PhysicalSynopsis>");
    if(this.isWindowOverRelation)
    {
      PhySynopsis outSyn = getOutputSyn();
      sb.append(outSyn.toString());
    }
    else
    {
      PhySynopsis winSyn = getWinSyn();
      sb.append(winSyn.toString());
    }    
    sb.append("</PhysicalSynopsis>");
    sb.append("</PhysicalOperatorValueWindow>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    xml.append(super.getXMLPlan2());
    xml.append(winSpec.getXMLPlan2());
    return xml.toString();
  }
  
  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Range window has no Relation Synopsis
    assert(false);
    return null;
  }
  
  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptValueWin))
      return false;
  
    //This is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptValueWin valOpt = (PhyOptValueWin)opt;

    if(this.isWindowOverRelation != valOpt.isWindowOverRelation())
      return false;
  
    assert valOpt.getOperatorKind() == PhyOptKind.PO_VALUE_WIN;
  
    if(valOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(!(this.winSpec.equals(valOpt.winSpec)))
      return false;
    
    return true;
  }

  /**
   * Set whether the input is a relation or not
   * @param isWindowOverRelation is true if the input is a relation
   */
  public void setWindowOverRelation(boolean isWindowOverRelation)
  {
    this.isWindowOverRelation = isWindowOverRelation;
  }

  /**
   * Check if the window is applied over relation input
   * @return true if the input is a relation
   */
  public boolean isWindowOverRelation()
  {
    return isWindowOverRelation;
  }
  
  //archived relation support related
  public boolean isStateFul()
  {
    return true;
  }
  
  public boolean canConstructQuery(Query q) throws CEPException
  {
    if(this.getInputs()[0].getOutputSQL() == null)
      return false;
    
    //FIXME: what happens if project is input? do we need to remove extra event id?
    aliases  = new String[this.getInputs()[0].getArchiverProjEntries().size()];
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
  
  public boolean canBeQueryOperator() throws CEPException
  {
    return true;
  }
  
  public void updateArchiverQuery() throws CEPException
  {
    SQLType targetSQLType = 
        execContext.getServiceManager().getConfigMgr().getTargetSQLType();
    
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
    //  copy over the projtypes in child
    this.projTypes.addAll(children[0].getArchiverProjTypes());
    
    StringBuffer temp = 
      new StringBuffer("select "+projectAliases.toString()+" from ( ");
    temp.append(children[0].getOutputSQL()+" ) "+this.getOptName());
    temp.append(" where ");
   
    PhyValueWinSpec currentWinSpec = (PhyValueWinSpec)winSpec;
    String rangeAttr = currentWinSpec.getColumn().getSQLEquivalent(this.execContext);
    
    StringBuffer whereClause = new StringBuffer();
    if(currentWinSpec.getType() != ValueWindowType.GENERIC)
    {
      // Set WHERE clause for CurrentHour and CurrentPeriod
      whereClause.append(
        "("+rangeAttr + SPACE_CONSTANT + CompOp.GE.getSymbol() + " ?)");
    }
    else
    {
      String rangeVal = null;
      
      if(currentWinSpec.getColumn().getType() == Datatype.TIMESTAMP)
      {
        if(currentWinSpec.isLong())
          rangeVal = 
            String.valueOf(currentWinSpec.getLongConstVal()/1000000000l);
        else
          rangeVal = 
            String.valueOf(currentWinSpec.getDoubleConstVal()/1000000000l);
        /* FOR DATABASE - 
        * we use numtodsinterval() function as timestampadd is not supported.
        * FOR BI ENV -
        * we want to use timestampadd as numtodsinterval is not supported. */
        if (targetSQLType == SQLType.ORACLE)
        {
          //Test env.
          whereClause.append("("+rangeAttr + SPACE_CONSTANT + 
            CompOp.GE.getSymbol() + " ? " + ArithOp.SUB.getSymbol() +
            SPACE_CONSTANT + "numtodsinterval("+ rangeVal+", 'SECOND'))");
        }
        else if(targetSQLType == SQLType.BI)
        {
          whereClause.append("("+rangeAttr + SPACE_CONSTANT + 
            CompOp.GE.getSymbol() + SPACE_CONSTANT + 
            "timestampadd(SQL_TSI_SECOND, -"+rangeVal+", ?))");
        }
      }
      else
      { 
        if(currentWinSpec.isLong())
          rangeVal = 
            String.valueOf(currentWinSpec.getLongConstVal());
        else
          rangeVal = 
            String.valueOf(currentWinSpec.getDoubleConstVal());
        // Here we assume if db/archiver uses long to represent timestamp
        // then the unit is NANOSECONDS.
        whereClause.append("("+rangeAttr + SPACE_CONSTANT +
          CompOp.GE.getSymbol() + SPACE_CONSTANT +
          "?"+ SPACE_CONSTANT + ArithOp.SUB.getSymbol()+ SPACE_CONSTANT +
          rangeVal + ")");
      }
    }  
    temp.append(whereClause);
    this.setOutputSQL(temp.toString());
  }

  /**
   * Returns archiver query parameter type
   * @return type of the parameter
   */
  public Datatype getColType()
  {
    return ((PhyValueWinSpec)winSpec).getColumn().getType();
  }
  
  /**
   * Returns parameter value as per the type of the window
   * @param currentTime - input snapshot time
   * @return - output snapshot time as per the type of the window.
   */
  public long getSnapShotTime(long currentTime)
  {
    PhyValueWinSpec spec = (PhyValueWinSpec) winSpec;
    switch(spec.getType())
    {
    case GENERIC:
      return currentTime;
    case CURRENT_HOUR:
      long numNanosInHour = IntervalConverter.HOUR * 1000000000l;
      long numHours = currentTime / numNanosInHour;
      return numHours * numNanosInHour;
    case CURRENT_PERIOD:
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(currentTime / 1000000l);
      int numHour = cal.get(Calendar.HOUR_OF_DAY);   
      int numMins = cal.get(Calendar.MINUTE);
      int numSecs = cal.get(Calendar.SECOND);
      // Set the time to DD/MM/YY 00:00:00AM
      long dayBaseValue 
        = currentTime - numHour * IntervalConverter.HOUR * 1000000000l
                  - numMins * IntervalConverter.MINUTE * 1000000000l
                  - numSecs * IntervalConverter.SECOND * 1000000000l;
      
      long periodBaseValue 
       = dayBaseValue + spec.getCurrentPeriodStartTime();
      return periodBaseValue;
    }
    return Long.MIN_VALUE;
  }
}
