/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSubquerySrc.java /main/2 2012/05/02 03:05:56 pkali Exp $ */

/* Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    08/25/11 - Creation
 */

package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;


/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptSubquerySrc.java /main/2 2012/05/02 03:05:56 pkali Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

public class LogOptSubquerySrc extends LogOptSource implements Cloneable 
{
  // Subquery node represents both a relational and a stream source.

  /**
   * Constructor for subquery source operator
   * @param input Logical operator tree representing the subquery
   * @param alias Name of the subquery (must be present)
   * @param varId internal symbol table identifier
   * @throws LogicalPlanException
   */
  public LogOptSubquerySrc(LogOpt input, String alias, int varId) 
    throws LogicalPlanException 
  {
    super(LogOptKind.LO_SUBQUERY_SOURCE); 
    
    this.entityName = alias;
    this.varId = varId;
    this.entityId = -1; // set relation id to -1 as it is an inline view
    this.setIsStream(input.getIsStream());
    
    // determine the output schema
    this.numOutAttrs = input.getNumOutAttrs(); 
    super.setNumOutAttrs(numOutAttrs);    
    ArrayList<Attr> attr = getOutAttrs();        
    // create named attributes. this is needed because of operators
    // like pattern match expect named attributes. Also in cases where a
    // (sub)query has a project list with expressions, we need to create
    // named attribute for the comparison work later.
    for (int a = 0; a < numOutAttrs; a++) 
    {
      AttrNamed outAttr = new AttrNamed(input.getOutAttr(a).getDatatype());
      outAttr.setVarId(varId);
      outAttr.setAttrId(a);
      attr.set(a, outAttr);
    }
    
    this.setExternal(input.isExternal());
    this.setNumInputs(1);
    this.setInput(0, input);
    input.setOutput(this);    
  }  
  
  // toString method override
  public String toString() 
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<SubquerySourceLogicalOperator>");
    // dump common fields
    sb.append(super.toString());
    sb.append("Name = " + entityName);
    sb.append("varId = " + varId);
    sb.append("entityId = " + entityId);
    sb.append("</SubquerySourceLogicalOperator>");
    return sb.toString();
  }

  /**
   * Set source lineages
   */
  public void setSourceLineages()
  {
    if(sourceLineage == null)
    {
      sourceLineage = new ArrayList<LogOpt>();
      for(int i=0; i <numInputs; i++)
      {
        this.getInput(i).setSourceLineages();
        sourceLineage.addAll(getInput(i).getSourceLineage());
      } 
    }
  }
}