/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSystemNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    parujain    08/11/08 - error offset
    parujain    02/09/07 - system config
    dlenkov     12/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPSystemNode.java /main/4 2008/08/25 19:27:24 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to system DDL
 *
 * @since 1.0
 */

public class CEPSystemNode implements CEPParseTreeNode {

  /** Kind of system setting */
  protected CEPSystemKind sysKind;

  /** string associated with system parameter setting */
  protected String value;
  
  protected int startOffset;
  
  protected int endOffset;
  
  public CEPSystemNode(CEPSystemKind kind, CEPConstExprNode cn)
  {
    this.sysKind = kind;
    
    if (cn instanceof CEPIntConstExprNode) {
      CEPIntConstExprNode icn = (CEPIntConstExprNode)cn;
      Integer int_val = new Integer( (icn).getValue());
      this.value = int_val.toString();
      setStartOffset(icn.getStartOffset());
      setEndOffset(icn.getEndOffset());
    }
    else if (cn instanceof CEPStringConstExprNode)
    {
      CEPStringConstExprNode scn = (CEPStringConstExprNode)cn;
      this.value = (scn).getValue();
      setStartOffset(scn.getStartOffset());
      setEndOffset(scn.getEndOffset());
    }
    
  }
  
  public CEPSystemKind getSystemKind()
  {
    return sysKind;
  }
  
  public String getValue()
  {
    return value;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  
}
