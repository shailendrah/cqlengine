/* $Header: pcbpel/cep/tools/src/oracle/cep/tools/colt/TransformerHelper.java /main/2 2008/09/10 14:06:33 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    09/02/08 - fix for FileNotFoundException
    sbishnoi    04/21/08 - Creation
 */
package oracle.cep.tools.colt;

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 *  @version $Header: pcbpel/cep/tools/src/oracle/cep/tools/colt/TransformerHelper.java /main/2 2008/09/10 14:06:33 skmishra Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class TransformerHelper
{
  private Source xmlSource;
  private Source xslSource;
  private Result result;
  private Transformer transformer;
  
  public TransformerHelper(){}
  
  public TransformerHelper(File xmlFile, File xslFile, File outputFile)
    throws TransformerException 
  {
    this.xmlSource  = new StreamSource(xmlFile);
    this.xslSource  = new StreamSource(xslFile); 
    this.result     = new StreamResult(outputFile.toURI().getPath());
    System.setProperty("javax.xml.transform.TransformerFactory","oracle.xml.jaxp.JXSAXTransformerFactory");
		setTransformer();
  }
  
  private void setTransformer() throws TransformerException 
  {
    TransformerFactory factory     = TransformerFactory.newInstance();
		this.transformer = factory.newTransformer(xslSource);
  }
  
  
  public void transform()  throws TransformerException 
  {
    assert transformer != null;
    transformer.transform(xmlSource, result);
  } 
  
  public void setParameter(String name, Object value)
  {
    assert transformer != null;
    transformer.setParameter(name, value);
  }
  
}
