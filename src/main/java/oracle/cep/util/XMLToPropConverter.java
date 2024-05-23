/* $Header: XMLToPropConverter.java 22-mar-2007.15:17:03 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/22/07 - XML To properties converter
    parujain    03/22/07 - Creation
 */

/**
 *  @version $Header: XMLToPropConverter.java 22-mar-2007.15:17:03 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLToPropConverter extends DefaultHandler {
 private String tempVal;
 private String tempKey;
 private Properties config;

 public XMLToPropConverter() {
   config = new Properties();
 }
 
 public Properties convert(String text)
 {
   SAXParserFactory spf = SAXParserFactory.newInstance();
   try {
       //get a new instance of parser
       SAXParser sp = spf.newSAXParser();
       //parse the file and also register this class for call backs
       sp.parse(new InputSource(new StringReader(text)), this);
   }catch(SAXException se) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, se);
   }catch(ParserConfigurationException pce) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, pce);
   }catch (IOException ie) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ie);
   }
   return config;
 }
  
  public void startElement(String uri, String localName, String qName,
     Attributes attributes) throws SAXException
  {
    tempKey = qName; 
  }
  
  public void characters(char[] ch, int start, int length) 
  throws SAXException 
  {
    tempVal = new String(ch,start,length);
  }
  
  public void endElement(String uri, String localName, String qName) 
  throws SAXException
  {
    if (qName.equals("Config")) return;
    config.setProperty(tempKey, tempVal.trim());
  }  
  
}
