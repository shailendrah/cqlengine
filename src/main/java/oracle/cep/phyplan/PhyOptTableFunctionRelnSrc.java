/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptTableFunctionRelnSrc.java /main/4 2011/07/19 05:52:54 anasrini Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/19/11 - XbranchMerge anasrini_bug-12752107_ps5 from
                           st_pcbpel_11.1.1.4.0
    anasrini    07/15/11 - set pullOperator=true
    sbishnoi    07/13/10 - XbranchMerge sbishnoi_bug-9860418_ps3_11.1.1.4.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    07/12/10 - XbranchMerge sbishnoi_bug-9860418_ps3 from main
    sbishnoi    07/08/10 - handling null value for columnType
    sbishnoi    03/10/10 - fix sharing problem
    sbishnoi    12/26/09 - Creation
 */

package oracle.cep.phyplan;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptTableFunctionRelSource;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/PhyOptTableFunctionRelnSrc.java /main/3 2010/07/13 03:55:08 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptTableFunctionRelnSrc extends PhyOpt
{  
  /** relation name */ 
  private String tableAlias;
  
  /** column name */
  private String columnAlias;
  
  /** column data type*/
  private Datatype returnCollectionType;
  
  /** component data type */
  private Datatype componentType;
  
  /** table function logical expression */
  private oracle.cep.logplan.expr.Expr   tableFunctionLogExpr;
  
  /** table function logical expression */  
  private Expr tableFunctionPhyExpr;
  
  
  /**
   * Constructor
   * @param ec ExecContext
   * @param logPlan logical operator
   * @throws PhysicalPlanException
   */
  public PhyOptTableFunctionRelnSrc(ExecContext ec, LogOpt logPlan)
    throws PhysicalPlanException
  {
    super(ec, PhyOptKind.PO_TABLE_FUNCTION);
    
    // Always an external operator
    setExternal(true);   
    setPullOperator(true);
    setIsStream(false);
    setIsView(false);
    setIsSource(true);
    
    assert logPlan instanceof LogOptTableFunctionRelSource;
    LogOptTableFunctionRelSource logOpt 
      = (LogOptTableFunctionRelSource)logPlan;
    
    
    tableAlias  = logOpt.getTableAlias();    
    columnAlias = logOpt.getColumnAlias();    
    componentType = logOpt.getComponentType();
    returnCollectionType  = logOpt.getReturnCollectionType();
    tableFunctionLogExpr = logOpt.getTableFunctionExpr();
    
    // Table function expressions should not be Null
    // Note: Table function expression will be interpreted in Parent Join
    // operator; then join will set this operator's table function phyexpr.
    // We will use TableFunctionHelper to perform this action.
    
    assert tableFunctionLogExpr != null;
    
    
    // schema is same as the log operator; This will always be ONE
    setNumAttrs(logOpt.getNumOutAttrs());
    if(componentType != null)
    {
      setAttrMetadata(0, 
        new AttributeMetadata(componentType, componentType.getLength(), 
                            componentType.getPrecision(), 0));
    }
    else
    {
      setAttrMetadata(0,
          new AttributeMetadata(Datatype.OBJECT, Datatype.OBJECT.getLength(),
              Datatype.OBJECT.getPrecision(), 0));
    }
  }
  
  /**
  * Getter for the position of the Relation Synopsis for the Physical Operators
  * which are supposed to have one. Other Synopsis Kinds have pre defined position.
  *
  * @param synopsis for which position is needed
  * @return null as there is no synopses allocated for table function operator
  */
  public String getRelnSynPos(PhySynopsis syn){ 
    return null;
  }

  /**
   * @return the tableFunctionLogExpr
   */
  public  oracle.cep.logplan.expr.Expr getTableFunctionLogExpr()
  {
    return tableFunctionLogExpr;
  }

  /**
   * @param tableFunctionExpr the tableFunctionLogExpr to set
   */
  public void setTableFunctionLogExpr( 
      oracle.cep.logplan.expr.Expr tableFunctionLogExpr)
  {
    this.tableFunctionLogExpr = tableFunctionLogExpr;
  }
  
  /**
   * @return the tableFunctionPhyExpr
   */
  public  Expr getTableFunctionPhyExpr()
  {
    return tableFunctionPhyExpr;
  }

  /**
   * @param tableFunctionPhyExpr the tableFunctionPhyExpr to set
   */
  public void setTableFunctionPhyExpr(Expr tableFunctionPhyExpr)
  {
    this.tableFunctionPhyExpr = tableFunctionPhyExpr;
  }

  /**
   * @return the tableAlias
   */
  public String getTableAlias()
  {
    return tableAlias;
  }

  /**
   * @return the columnAlias
   */
  public String getColumnAlias()
  {
    return columnAlias;
  }

  
  /**
   * Method to calculate a concise String representation
   * of the Physical operator based on its attributes.
   * @return 
   *      A concise String representation of the Physical operator.
   */
  protected String getSignature()
  {
    return
      "TABLE#" + this.getTableFunctionPhyExpr().getSignature();
    
  }
  
  //toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<PhysicalOperatorTableFunctionRelSource>");
    sb.append(super.toString());
    sb.append("<TableAlias value=" + tableAlias + "/>");
    sb.append("<ColumnAlias value=" + columnAlias + "/>");
    sb.append("<ColumnType value=" + returnCollectionType + "/>");
    sb.append("</PhysicalOperatorTableFunctionRelSource>");
    return sb.toString();
  }
  
  // Create and return visualiser compatible XML Plan
  public String getXMLPlan2() throws CEPException 
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<name> TableFunctionRelSource </name>\n");
    sb.append("<lname> Table Function Relation Source </lname>\n");
    sb.append(super.getXMLPlan2());
    sb.append("<property name = \"Table Function Relation Name\" value = \"");
    sb.append(this.getTableAlias());
    sb.append("\"/>\n");
    sb.append("<property name = \"Column Name\" value = \"");
    sb.append(this.getColumnAlias());
    sb.append("\"/>\n");
    sb.append("<property name = \"Return type\" value = \"");
    sb.append(this.getReturnCollectionType());
    sb.append("\"/>\n");
    
    return sb.toString();
  }

  /**
   * @return the componentType
   */
  public Datatype getComponentType()
  {
    return componentType;
  }

  /**
   * @return the returnCollectionType
   */
  public Datatype getReturnCollectionType()
  {
    return returnCollectionType;
  }
  
  public boolean isPartialEquivalent(PhyOpt opt)
  {
    if(this.getId() == opt.getId())
       return false;
    
    if(!(opt instanceof PhyOptTableFunctionRelnSrc))
      return false;
    
    PhyOptTableFunctionRelnSrc other = (PhyOptTableFunctionRelnSrc)opt;
    
    if(componentType == null)
    {
      if(other.getComponentType() != null)
        return false;
    }
    else if(!this.componentType.equals(other.getComponentType()))
      return false;
    
    if(!this.columnAlias.equalsIgnoreCase(other.getColumnAlias()))
      return false;
    
    if(!this.tableAlias.equalsIgnoreCase(other.getTableAlias()))
      return false;
    
    if(!this.tableFunctionPhyExpr.equals(other.getTableFunctionPhyExpr()))
      return false;
          
    return true;
  }
  
  public void removePhyOp() throws PhysicalPlanException
  {
     execContext.getPlanMgr().dropTableFunctionOperator(this);
     execContext.getPlanMgr().removePhyOpt(this);
  }
}
