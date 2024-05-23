/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/TestSelect.java /main/3 2011/10/03 01:51:59 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    08/05/11 - Creation
 */

/**
 *  @version $Header: TestSelect.java 05-aug-2011.12:52:02 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;


public class TestSelect
    implements SingleElementFunction
{
    public static int count = 0;
    public Object execute(Object args[])
        throws UDFException
    {
        count++;
        if(count==1)
        {
          System.out.println("testFunction() is returning 1");
          return new Integer(1);
        }
        else
        {
          System.out.println("testFunction() is returning 0");
          return new Integer(0);
        }
    }

    public TestSelect()
    {
    }
}

