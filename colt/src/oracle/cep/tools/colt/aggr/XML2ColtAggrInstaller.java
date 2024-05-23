/* $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/aggr/XML2ColtAggrInstaller.java /main/2 2012/03/31 10:59:35 apiper Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/21/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/aggr/XML2ColtAggrInstaller.java /main/2 2012/03/31 10:59:35 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools.colt.aggr;

import java.io.File;

import oracle.cep.tools.colt.TransformerHelper;

public class XML2ColtAggrInstaller
{
  public static void main(String args[]) 
    throws javax.xml.transform.TransformerException
  {
    File              xslFile    = new File(args[0]);
    File              xmlFile    = new File(args[1]);
    File              outputFile = new File(args[2]);
    if (outputFile.getParentFile() != null) {
        outputFile.getParentFile().mkdirs();
    }
    TransformerHelper transformer = new TransformerHelper(xmlFile, xslFile, outputFile);
    transformer.transform();
  }
}
