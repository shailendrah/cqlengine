/* $Header: ColtAggrTestGenerator.java 21-apr-2008.21:45:19 sbishnoi Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

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
 *  @version $Header: ColtAggrTestGenerator.java 21-apr-2008.21:45:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.tools.colt.aggr;

import java.io.File;
import oracle.cep.tools.colt.TransformerHelper;

public class ColtAggrTestGenerator{
  
  public static void main(String[] args)
   throws javax.xml.transform.TransformerException
  {
    File              xslFile    = new File(args[0]);
    File              xmlFile    = new File(args[1]);
    File              outputFile = new File(args[2]);
    TransformerHelper transformer = new TransformerHelper(xmlFile, xslFile, outputFile);
    transformer.transform();
  }  
}
