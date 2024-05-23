/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPConstExprNode.java /main/5 2009/02/17 17:42:53 hopark Exp $ */

/* Copyright (c) 2005, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/05/09 - throw CEPException
    skmishra    08/21/08 - import, reorg
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - adding abstract getValue()
    anasrini    12/22/05 - parse tree node for a constant 
    anasrini    12/22/05 - parse tree node for a constant 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPConstExprNode.java /main/5 2009/02/17 17:42:53 hopark Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 * Parse tree node for a constant 
 */

public abstract class CEPConstExprNode extends CEPExprNode {
  public abstract Object getValue() throws CEPException;
  
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPConstExprNode other = (CEPConstExprNode) obj;
    
    try
    {
      return getValue().equals(other.getValue());
    }
    catch(CEPException e)
    {
      // CEPException can be thrown in case there is any error in parsing interval string.
      LogUtil.info(LoggerType.TRACE, "Exception while comparing two constant values. value1="+
        this.toString() + ", value2=" + (other != null ? other.toString() : "null") + ", Error Message=" + e.getMessage());
      return false;
    }
  }
}
