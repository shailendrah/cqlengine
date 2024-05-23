/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSubquerySrc.java /main/2 2013/11/27 21:53:23 sbishnoi Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    11/18/13 - bug 17709899
    vikshukl    08/25/11 - subquery support
    vikshukl    08/25/11 - Creation
 */
package oracle.cep.phyplan;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptSubquerySrc;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptSubquerySrc.java /main/2 2013/11/27 21:53:23 sbishnoi Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptSubquerySrc extends PhyOpt 
{
  /** Alias name for the sub query */
  private String alias;
  
  /** Symbol Table Variable Identifier for this alias */
  private int varId;
  
  /** Constructor for subquery physical operator 
   * @param ec    Execution context
   * @param input Physical input operator
   * @param logop Logical subquery operator
   * @throws PhysicalPlanException
   */
  public PhyOptSubquerySrc(ExecContext ec, PhyOpt input, LogOpt logop) 
    throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_SUBQUERY_SRC, input, logop, true, true);
    LogOptSubquerySrc logSubquerySrc = (LogOptSubquerySrc)logop;
    
    boolean isElementTimeRequired = input.getIsStream();
    if(isElementTimeRequired)
    {
      AttributeMetadata[] existingAttrMetadata = this.getAttrMetadata();
      int numAttrs = existingAttrMetadata.length;
      setNumAttrs(numAttrs+1);
      for(int i = 0; i < numAttrs; i++)
      {
        setAttrMetadata(i, existingAttrMetadata[i]);
      }
      // Set datatype and length for ELEMENT_TIME pseudo column
      StreamPseudoColumn elemTime = StreamPseudoColumn.ELEMENT_TIME;
      setAttrMetadata(numAttrs, 
                      new AttributeMetadata(elemTime.getColumnType(), 
                                            elemTime.getColumnLen(),
                                            elemTime.getColumnType().getPrecision(),
                                            0));    
    }
    
    setAlias(logSubquerySrc.getEntityName());
    setVarId(logSubquerySrc.getVarId());
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public void setVarId(int varId) {
    this.varId = varId;
  }

  public String getAlias() {
    return alias;
  }

  public int getVarId() {
    return varId;
  }

  @Override
  public String getRelnSynPos(PhySynopsis syn) {
    return null;
  }

//toString method override
 public String toString() {
   StringBuilder sb = new StringBuilder();

   sb.append("<PhysicalOperatorSubQuerySource>");
   sb.append(super.toString());

   sb.append("<SubQuerySource alias=\"" + this.alias + "\" />");
   sb.append("<SubQuerySource varId=\"" + this.varId + "\" />");
   
   sb.append("</PhysicalOperatorSubQuerySource>");
   return sb.toString();
 }
 
 //Generate and return visualiser compatible XML plan
 public String getXMLPlan2() throws CEPException {
   StringBuilder xml = new StringBuilder();
   xml.append("<name> SubQuerySource </name>\n");
   xml.append("<lname> SubQuerySource </lname>\n");
   xml.append(super.getXMLPlan2());
   xml.append("<property name = \"alias\" value = \"");
   if(this.alias != null) 
     xml.append(this.alias);
   else
     xml.append("null");
   xml.append("\"/>\n");
   xml.append("<property name = \"varId\" value = \"");
   xml.append(this.varId);
   xml.append("\"/>\n");
   return xml.toString();
 }
 
 /**
  * This method tells whether the two operators are partially equivalent or not
  */
 public boolean isPartialEquivalent(PhyOpt opt)
 {
   if(!(opt instanceof PhyOptSubquerySrc))
     return false;
   
   // this is to avoid finding the same operator in PlanManager list
   if(opt.getId() == this.getId())
     return false;
   
   PhyOpt childPhyOpt = this.getInput(0);
   PhyOpt paramChildPhyOpt = opt.getInput(0);
   
   PhyOptSubquerySrc phyOpt = (PhyOptSubquerySrc)opt;
   
   assert phyOpt.getOperatorKind() == PhyOptKind.PO_SUBQUERY_SRC;
 
   if(phyOpt.getNumInputs() != this.getNumInputs())
     return false;

   if((this.getAlias() != null && phyOpt.getAlias() == null) ||
       (this.getAlias() == null && phyOpt.getAlias() != null)) 
     return false;
   
   if(phyOpt.getAlias() != null && !phyOpt.getAlias().equals(this.getAlias()))
     return false;
   
   if(phyOpt.getVarId() != this.getVarId())
     return false;
   
   if(!childPhyOpt.isPartialEquivalent(paramChildPhyOpt))
     return false;
   
   return true;
 }
 
 public boolean equals(Object opt)
 {
   if(opt == null)
     return false;
   
   if(!(opt instanceof PhyOptSubquerySrc))
   {
     return super.equals(opt);
   }
   
   PhyOptSubquerySrc phyOpt = (PhyOptSubquerySrc)opt;
   
   if(phyOpt.getNumInputs() != this.getNumInputs())
     return false;

   if((this.getAlias() != null && phyOpt.getAlias() == null) ||
       (this.getAlias() == null && phyOpt.getAlias() != null)) 
     return false;
   
   if(phyOpt.getAlias() != null && !phyOpt.getAlias().equals(this.getAlias()))
     return false;
   
   if(phyOpt.getVarId() != this.getVarId())
     return false;
   
   return true;
 }
}