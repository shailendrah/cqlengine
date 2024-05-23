/* $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XML2ColtInstaller.java /main/5 2012/03/31 10:59:35 apiper Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    09/02/08 - 
    sbishnoi    04/21/08 - changing package definition
    mthatte     10/11/07 - Removing semi-colons
    sbishnoi    08/08/07 - changed arguments to main
    sbishnoi    06/25/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/colt/src/oracle/cep/tools/colt/functions/XML2ColtInstaller.java /main/5 2012/03/31 10:59:35 apiper Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
/**
 * Generates the ColtInstall.java file that contains DDL's for creating
 * functions of the colt library supported by CEP
 */
package oracle.cep.tools.colt.functions;

import java.io.File;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class XML2ColtInstaller {
  
  public XML2ColtInstaller() {
  }

  private void transform(File xmlFile, File xslFile, File outputFile)
    throws TransformerException 
  {

    Source xmlSource  = new StreamSource(xmlFile);
    Source xslSource  = new StreamSource(xslFile); 
    Result result     = new StreamResult(outputFile);

    TransformerFactory factory     = TransformerFactory.newInstance();
    Transformer        transformer = factory.newTransformer(xslSource);
    
    transformer.transform(xmlSource, result);
  }  

  public static void main(String[] args) 
    throws javax.xml.transform.TransformerException 
  {
		System.setProperty("javax.xml.transform.TransformerFactory","oracle.xml.jaxp.JXSAXTransformerFactory");    
		String impl = System.getProperty("javax.xml.transform.TransformerFactory");//,"net.sf.saxon.TransformerFactoryImpl");    
		System.out.println("TransformerImpl= " + impl);
    XML2ColtInstaller coltInst   = new XML2ColtInstaller();
    File              xslFile    = new File(args[0]);
    File              xmlFile    = new File(args[1]);
    File              outputFile = new File(args[2]);
    
    if (outputFile.getParentFile() != null) {
        outputFile.getParentFile().mkdirs();
    }
    coltInst.transform(xmlFile, xslFile, outputFile);   
  }
}
