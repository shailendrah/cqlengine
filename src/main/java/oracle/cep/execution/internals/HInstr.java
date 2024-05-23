/* $Header: HInstr.java 06-sep-2007.15:41:45 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares HInstr in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    09/06/07 - optimize
    najain    03/12/07 - bug fix
    skaluska  03/01/06 - Creation
    skaluska  03/01/06 - Creation
 */

/**
 *  @version $Header: HInstr.java 06-sep-2007.15:41:45 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.common.Datatype;

/**
 * HInstr
 *
 * @author skaluska
 */
public class HInstr
{
  /** type */
  public Datatype type;
  /** argument */
  public int      r;
  public Column   c;

  /**
   * Constructor for HInstr
   * @param type Datatype
   * @param r Role
   * @param c Column
   */
  public HInstr(Datatype type, int r, Column c)
  {
    // TODO Auto-generated constructor stub

    this.type = type;
    this.r = r;
    this.c = c;
  }
}
