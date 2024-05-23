/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPrtnWin.java /main/13 2013/09/17 07:25:00 pkali Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
     Partition Window Physical Operator in the package oracle.cep.phyplan

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       09/16/13 - included predicates for partially equivalent
                           checking
    pkali       05/31/12 - added predicate list member
    sbishnoi    12/05/11 - support for variable duration partition window
    sbishnoi    05/04/10 - setting automatic heartbeat timeout
    sborah      04/20/09 - reorganize sharing hash
    sborah      03/10/09 - modify getSharingHash()
    hopark      10/09/08 - remove statics
    hopark      10/25/07 - set synopsis
    hopark      10/12/07 - use winspec
    hopark      07/13/07 - dump stack trace on exception
    hopark      01/10/07 - partwin with range is not equivalent with the one
                           without range
    parujain    12/18/06 - operator sharing
    hopark      12/15/06 - add range
    rkomurav    09/13/06 - PhySynPos OO restructuring
    rkomurav    08/28/06 - add genXMLPlan2
    ayalaman    08/03/06 - partition attribute count
    ayalaman    08/01/06 - partition window implementation
    najain      04/06/06 - cleanup
    anasrini    04/06/06 - constructor cleanup 
    najain      03/24/06 - cleanup
    skaluska    02/15/06 - Cleanup Phy/Exec Synopsis 
    najain      02/10/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptPrtnWin.java /main/13 2013/09/17 07:25:00 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.LinkedList;
import java.util.logging.Level;

import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.window.PhyRngWinSpec;
import oracle.cep.phyplan.window.PhyRowRangeWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.service.ExecContext;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptPrtnWin;

/**
 * Partition Window Physical Operator 
 */
public class PhyOptPrtnWin extends PhyOpt {
  /** Partitioning attributes */
  Attr[] partnAttrs;

  /** number of partition attributes */
  int numPartnAttrs;

  /** Window specifications */
  private PhyRowRangeWinSpec winSpec;

  /** position of attribute having value of expirty Ts */
  private int expTsPos;
  
  /** predicates to evaluate the tuple membership in the window */
  private LinkedList<BoolExpr> preds;
  
  /**
   * Constructor
   * @param ec
   * @param input
   * @throws PhysicalPlanException
   */
  public PhyOptPrtnWin(ExecContext ec, PhyOpt input) 
    throws PhysicalPlanException 
  {
    super(ec, PhyOptKind.PO_PARTN_WIN);
  }

  /**
   * Constructor
   * @param ec
   * @param winSpec
   * @param logPlan
   * @param phyChildPlans
   * @param numPrtnAttrs
   * @param partnAttrs
   * @throws PhysicalPlanException
   */
  public PhyOptPrtnWin(ExecContext ec, 
                       PhyWinSpec winSpec,
                       LogOpt logPlan,
                       PhyOpt[] phyChildPlans, 
                       int numPrtnAttrs, 
                       Attr[] partnAttrs)
    throws PhysicalPlanException 
  {
    super(ec, PhyOptKind.PO_PARTN_WIN);
 
    assert logPlan != null;
    assert logPlan.getNumInputs() == 1;
    assert logPlan instanceof LogOptPrtnWin;

    // Initializations
    setStore(null);
    setInstOp(null);
    setWinSyn(null);
    
    PhyRngWinSpec spec = (PhyRngWinSpec)winSpec;
    setWinSpec(spec);
    
    if(this.isVariableDurationWindow())
    {
      PhyOpt inp = phyChildPlans[0];
      
      // We will add one more attribute in the schema to keep the evaluated
      // expiryTs
      // Output schema of operator = 
      //  {Schema of the input stream + expiryTs }      
      int numOutAttrs = inp.getNumAttrs() + 1;
      
      setNumAttrs(numOutAttrs);
      
      for(int i=0; i < numOutAttrs-1; i++)
      {
        setAttrMetadata(i, inp.getAttrMetadata(i));
      } 
      
      Datatype rangeColumnType = spec.getRangeExpr().getType();
      int rangeColumnLen       = spec.getRangeExpr().getLength();
      int rangeColumnPrecision = rangeColumnType.getPrecision();
      
     AttributeMetadata expTsColumnMetadata = 
       new AttributeMetadata(rangeColumnType, 
                             rangeColumnLen, 
                             rangeColumnPrecision, 
                             0);  
            
      expTsPos = numOutAttrs - 1;
      
      setAttrMetadata(expTsPos, expTsColumnMetadata);
    }
    else
    {
      // output schema = input schema :: since the instance of the class was 
      // allocated by the InterpreterFactory, we need to copy from the first 
      // child. Need to confirm with Anand whether there is a better way of 
      // doing the same.
      copy(phyChildPlans[0]);
    }

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
      // TODO
    }

    // set the number of partition attributes 
    this.numPartnAttrs = numPrtnAttrs; 
    this.partnAttrs = partnAttrs; 
    
    
    // Partition window will need a heart beat timeout for a non-zero
    // range values
    if(this.winSpec != null && 
      (this.winSpec.getRangeUnits() > 0 || 
       this.winSpec.isVariableDurationWindow()))
    {
      setHbtTimeoutRequired(true);
    }
  }

  public PhyWinSpec getWinSpec() {
    return winSpec;
  }

  public void setWinSpec(PhyWinSpec spec) 
  {
    this.winSpec = (PhyRowRangeWinSpec) spec;
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
          + this.winSpec.toString() + getExpressionList(preds));
  }
  
  /**
   *  Get the number of partition attributes
   *
   *  @return  the number of partition attributes
   */
  public int getNumPartnAttrs()
  {
    return numPartnAttrs; 
  } 

  /**
   *  Get the partition attributes
   *
   *  @return  an array of partition attributes
   */
  public Attr[] getPartnAttrs()
  {
    return partnAttrs; 
  }

  public boolean hasRange() 
  {
      return winSpec.getRangeUnits() >= 0;
  }
  
  /**
   *  Get the synopsis for the operator
   *
   *  @return  the synopsis for the operator
   */
  public PhySynopsis getSynopsis() 
  {
    return getSynopsis(0);
  }

  /**
   *  Set the synopsis for the operator
   *
   *  @param  par_winSyn  the synopsis for the operator
   */
  public void setWinSyn( PhySynopsis par_winSyn) 
  {
    setSynopsis(0, par_winSyn);
  }
  
  /**
   * Check if the partition window is variable duration
   * @return
   */
  public boolean isVariableDurationWindow()
  {
    if(winSpec instanceof PhyRowRangeWinSpec)
    {
      return ((PhyRowRangeWinSpec)winSpec).isVariableDurationWindow();
    }
    else
      return false;
  }

  /**
   * @return the expTsPos
   */
  public int getExpTsPos()
  {
    return expTsPos;
  }

  /**
   * Set the predicates
   */
  public void setPredicates(LinkedList<BoolExpr> predicates) 
  {
    preds = predicates;
  }
  
  /**
   * Get the predicates
   * @return the list of predicates
   */
  public LinkedList<BoolExpr> getPredicates() 
  {
    return preds;
  }
  
  /**
   * Get the predicate 
   * @return the predicate in the form of an array of atomic predicates
   */
  public BoolExpr[] getPredicate() 
  {
    if(preds != null)
      return preds.toArray(new BoolExpr[0]);
    return null;
  }
  
  /** 
   *  String representation for the operator
   */
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PartitionWindow>");
    sb.append(super.toString());

    sb.append(winSpec.toString());
    PhySynopsis winSyn = getSynopsis();
    if (winSyn != null) {
      sb.append("<PhysicalSynopsis>");
      sb.append(winSyn.toString());
      sb.append("</PhysicalSynopsis>");
    }

    if (preds != null && preds.size() > 0) {
      sb.append("<Predicate>");
      for (int i = 0; i < preds.size(); i++)
        sb.append(preds.get(i).toString());
      sb.append("</Predicate>");
    }
    sb.append("</PartitionWindow>");
    return sb.toString();
  }
  
  //Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException {
    StringBuilder xml = new StringBuilder();
    int i = 0;
    xml.append("<name> PartWin </name>\n");
    xml.append("<lname> Partition Window </lname>\n");
    xml.append(super.getXMLPlan2());
    xml.append(winSpec.getXMLPlan2());
    xml.append("<property name = \"Partn Attrs\" value = \"");
    if(partnAttrs.length != 0) {
      for(i = 0; i < (partnAttrs.length - 1); i++) {
        xml.append(partnAttrs[i].getXMLPlan2());
        xml.append(",");
      }
      xml.append(partnAttrs[i].getXMLPlan2());
    }
    else {
      xml.append("(null)");
    }
    xml.append("\"/>\n");
    
    if (preds != null && preds.size() > 0) {
      xml.append("<property name = \"Predicate\" value = \"");
      if (preds.size() != 0) {
        for (i = 0; i < (preds.size() - 1); i++) {
          xml.append(preds.get(i).getXMLPlan2());
          xml.append(", ");
        }
        xml.append(preds.get(i).getXMLPlan2());
      }
      xml.append("\"/>\n");
    }
    
    return xml.toString();
  }

  //get the Synopsis Position
  public String getRelnSynPos(PhySynopsis syn) {
    // Partition Window has no Relation Synopsis
    assert(false);
    return null;
  }
  

  /**
   * This method tells whether the two operators are partially equivalent or not
   */
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(!(opt instanceof PhyOptPrtnWin))
      return false;
  
   // this is to avoid finding the same operator in PlanManager list
    if(opt.getId() == this.getId())
      return false;
 
    PhyOptPrtnWin prtOpt = (PhyOptPrtnWin)opt;
  
    assert prtOpt.getOperatorKind() == PhyOptKind.PO_PARTN_WIN;
  
    if(prtOpt.getNumInputs() != this.getNumInputs())
      return false;
  
    if(!(this.winSpec.equals(prtOpt.winSpec)))
      return false;
    
    if ((this.numPartnAttrs != prtOpt.numPartnAttrs))
      return false;
    
    for(int i=0; i < numPartnAttrs; i++)
    {
      if(!this.partnAttrs[i].equals(prtOpt.partnAttrs[i]))
        return false;
    }
    
    LinkedList<BoolExpr> optPreds = prtOpt.getPredicates();
    if(preds != null &&  optPreds != null)
    {
      if(preds.size() != optPreds.size())
        return false;
    }
    return true;
  }

}
