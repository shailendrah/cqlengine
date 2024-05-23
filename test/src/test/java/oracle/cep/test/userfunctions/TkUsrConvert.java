/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrConvert.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/17/10 - Creation
 */

package oracle.cep.test.userfunctions;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/userfunctions/TkUsrConvert.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */


import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;


public class TkUsrConvert
    implements SingleElementFunction
{

    public Object execute(Object args[])
        throws UDFException
    {
      return args[0];
    }

    public TkUsrConvert()
    {
    }
}

