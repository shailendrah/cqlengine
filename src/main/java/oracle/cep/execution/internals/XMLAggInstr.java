/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/XMLAggInstr.java /main/2 2008/09/19 00:00:38 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    this is a structure to hold parameters necessary to evaluate 
    an xmlagg instruction at runtime. the params are populated 
    at compile time

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/14/08 - removing xmlagginppos
    skmishra    07/17/08 - adding order by
    udeshmuk    06/30/08 - 
    skmishra    06/16/08 - used to process xmlagg instrs
    skmishra    06/16/08 - Creation
 */

package oracle.cep.execution.internals;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/XMLAggInstr.java /main/2 2008/09/19 00:00:38 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLAggInstr
{
  //input role, pos
  public int argPos; 
  public int argRole;
  
  //output roles, pos
  public int oldOutputRole;
  public int newOutputRole;
  public int aggrIndex;
  
  public int xmlAggIndexPos;
  
  public XMLAggInstr(int argPos, int argRole, int aggrIndex, int oldOutPutRole, int newOutputRole, int xmlAggIndexPos)
  {
    super();
    this.argPos = argPos;
    this.argRole = argRole;
    this.oldOutputRole = oldOutPutRole;
    this.newOutputRole = newOutputRole;
    this.aggrIndex = aggrIndex;
    this.xmlAggIndexPos = xmlAggIndexPos;
  }
}