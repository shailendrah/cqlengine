/* $Header: CaseInstr.java 07-sep-2007.21:57:23 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/07/07 - eval refactor
    parujain    04/05/07 - Simple CASE
    parujain    03/30/07 - CASE Instruction
    parujain    03/30/07 - Creation
 */

/**
 *  @version $Header: CaseInstr.java 07-sep-2007.21:57:23 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

public class CaseInstr{
  public IAEval compEval;
  
  public CaseCondition[] conditions;
  
  public IAEval elseResult;
  
  public CaseInstr(int num)
  {
    compEval = null;
    conditions = new CaseCondition[num];
    elseResult = null;
  }
  
  public int getNumConditions()
  {
    return conditions.length;
  }
  
  public IAEval getElseResult()
  {
    return elseResult;
  }
  
//toString
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<CaseInstr>");
    for(int i=0; i<conditions.length; i++)
    {
      sb.append(conditions[i].condition.toString());
      if(conditions[i].result != null)
        sb.append(conditions[i].result.toString());
    }
    if(elseResult != null)
    {
      sb.append(elseResult.toString());
    }
    sb.append("</CaseInstr>");
    return sb.toString();
  }
  
}
