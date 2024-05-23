/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBooleanConstExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 skmishra    02/18/09 - remove toString
 skmishra    08/21/08 - import, reorg
 mthatte     04/07/08 - adding toString()
 mthatte     03/28/08 - returning Object in getValue()
 mthatte     01/14/08 - 
 najain      01/02/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPBooleanConstExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.StringTokenizer;

public class CEPBooleanConstExprNode extends CEPConstExprNode
{
  private boolean val;

  public CEPBooleanConstExprNode(String str)
  {
    if (str.equalsIgnoreCase("true"))
      val = true;
    else
    {
      assert str.equalsIgnoreCase("false");
      val = false;
    }
  }

  /**
   * Returns the long value to the other layers.
   * 
   * @return Interval in milliseconds
   */
  public Boolean getValue()
  {
    return new Boolean(val);
  }

  public String getExpression()
  {
    if (val)
      return " true ";
    return " false ";
  }
  
  public String toString()
  {
    if (alias != null)
    {
      if (val)
        return " true AS " + alias;
      return " false AS " + alias;
    }

    else
    {
      if (val)
        return " true ";
      return " false ";
    }
  }
}
