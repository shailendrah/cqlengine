/* $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkUsrXmlType2.java /main/2 2009/06/16 10:23:30 mthatte Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     06/15/09 - udf declared with return xmltype should return
                           String
    sborah      03/13/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/userfunctions/TkUsrXmlType2.java /main/2 2009/06/16 10:23:30 mthatte Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrXmlType2 implements SingleElementFunction 
{
  public String execute(Object[] args) throws UDFException
  {
    
     return new String("<foo>bar</foo>");
  }
  
}
