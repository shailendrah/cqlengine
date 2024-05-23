/* $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkUsrXmlType.java /main/1 2009/03/13 13:23:43 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      03/01/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkUsrXmlType.java /main/1 2009/03/13 13:23:43 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrXmlType implements SingleElementFunction 
{
  public Object execute(Object[] args) throws UDFException
  {
    String arg = (String)args[0];
    StringBuilder xml = new StringBuilder();
    
    xml.append("<CustomHeader>");
    xml.append(arg);
    xml.append("</CustomHeader>");
    
    return xml.toString();
  }
  
}


