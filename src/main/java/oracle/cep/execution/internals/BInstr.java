/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/BInstr.java /main/5 2011/05/26 19:23:39 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares BInstr in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    vikshukl  05/26/11 - XbranchMerge vikshukl_bug-11736605_ps5 from
                         st_pcbpel_11.1.1.4.0
    vikshukl  05/09/11 - init jump location to an invalid value
    hopark    09/07/07 - refactor eval
    hopark    09/05/07 - optimize
    hopark    03/24/07 - check column is null
    najain    03/12/07 - bug fix
    parujain  11/09/06 - Logical Operators implementation
    parujain  10/03/06 - support for Like
    anasrini  03/31/06 - add toString 
    anasrini  03/21/06 - expose fields 
    skaluska  02/12/06 - Creation
    skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: BInstr.java 07-sep-2007.18:04:09 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import java.util.regex.Pattern;

import oracle.cep.execution.internals.memory.EvalContext;

/**
 * @author skaluska
 */
public class BInstr {
  /* operation */
  public BOp    op;
  
  /* operand 1 */
  public int    r1;
  public Column c1;
  public int    b1; // bit position
  public IAEval  e1;

  /* operand 2 */
  public int    r2;
  public Column c2;
  public int    b2; // bit position
  public IAEval  e2;
  
  /* result */
  public int dr;
  public int dc;
  public int db;
  
  /* index in the array to jump to */
  public int addr = -1;

  /* index of the root of the left subtree */
  public int left = -1;
  
  public int[] rows = null;
  public int[] cols = null;
  
  /* Flag indicating whether result bits are valid or not */
  /* Final output will have this flag false */
  public boolean valid;
  
  /* Pattern corresponding to String when Like is used otherwise it is null */
  /* This is done so as to re-use the pattern object for a fixed regex */
  public Pattern pattern;

  public int[] getRows()
  {
    if (rows == null)
    {
      rows = new int[2];
      rows[0] = r1;
      rows[1] = r2;
    }
    return rows;
  }
  
  public int[] getCols()
  {
    if (cols == null)
    {
      cols = new int[2];
      cols[0] = c1.colnum;
      cols[1] = c2.colnum;
    }
    return cols;
  }
  // toString
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<BInstr>");
    sb.append("<Operation op=\"" + op + "\" />");
    sb.append("<Operand r1=\"" + EvalContext.getRoleName(r1)
              + "\" c1=\"" + (c1 == null ? "null":Integer.toString(c1.getColnum())) + "\">");
    if (e1 != null)
      sb.append(e1.toString());
    sb.append("</Operand>");
    sb.append("<Operand r2=\"" + EvalContext.getRoleName(r2)
              + "\" c2=\"" + (c2 == null ? "null":Integer.toString(c2.getColnum())) + "\">");
    if (e2 != null)
      sb.append(e2.toString());
    sb.append("</Operand>");
    sb.append("</BInstr>");
    return sb.toString();
  }
}
