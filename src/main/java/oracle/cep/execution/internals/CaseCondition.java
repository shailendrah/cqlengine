/* $Header: CaseCondition.java 07-sep-2007.18:04:13 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      09/07/07 - refactor eval
    parujain    03/30/07 - Case Condition
    parujain    03/30/07 - Creation
 */

/**
 *  @version $Header: CaseCondition.java 07-sep-2007.18:04:13 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

public class CaseCondition{
  public IBEval  condition;
  public IAEval  result;
  
  public CaseCondition()
  {
    condition = null;
    result = null;
  }
  
  public void setCondition(IBEval bval)
  {
    condition = bval;
  }
  
  public void setResult(IAEval res)
  {
    result = res;
  }
}
