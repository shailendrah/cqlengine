/* $Header: pcbpel/cep/server/src/oracle/cep/service/CEPLoadParser.java /main/2 2009/05/11 09:35:04 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/07/09 - support utf-8
    hopark      11/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/service/CEPLoadParser.java /main/2 2009/05/11 09:35:04 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class CEPLoadParser
{
  static final String s_schmaLoc = "/resources/CEPLoad.xsd";
  CEP.CEPLOAD m_load = null;
  CEP.CEPUNLOAD m_unload = null;
  List<String> m_ddls = null;
  ValidationEvent[] m_validationEvents = null;
  
  static class CEPLoadSchema
  {
    static Schema s_schema = null;
    public static Schema getSchema()
    {
      if (s_schema != null)
        return s_schema;

      SchemaFactory schemaFactory = SchemaFactory
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // load the schema from the jar file CEPLoadSchema belongs.
      InputStream stream = CEPLoadParser.class.getResourceAsStream(s_schmaLoc);
      if (stream != null)
      {
        try
        {
          s_schema = schemaFactory.newSchema(new StreamSource(stream));
        }
        catch(Exception se)
        {
          LogUtil.warning(LoggerType.TRACE, "Failed to load cep schema from " + s_schmaLoc + "\n" + se.toString());
        }
      }
      return s_schema;
    }
  }
  
  public CEPLoadParser()
  {
  }
  
  public List<String> getLoadDDLs()
  {
    List<String> r = null;
    if (m_load != null)
      r = m_load.getCEPDDL();
    if (r == null)
      r = m_ddls;
    return r;
  }
  
  public List<String> getUnloadDDLs()
  {
    return m_unload.getCEPDDL();
  }
  
  public ValidationEvent[] getValidationEvents()
  {
    return m_validationEvents;
  }
  
  private boolean parse(Object src, boolean retry)
  {
    m_validationEvents = null;
    ValidationEventCollector collector = new ValidationEventCollector();
    JAXBContext jc = null;
    try
    {
      jc = JAXBContext.newInstance( "oracle.cep.service" );
    }
    catch(JAXBException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      LogUtil.logStackTrace(e);
      return false;
    }
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    try 
      {
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      }
    catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) 
      {
        throw new RuntimeException(e.getStackTrace().toString());
      }

    int nopass = (retry ? 2 : 1);
    for (int pass = 0 ; pass < nopass; pass++)
    {
      try
      {
        Unmarshaller u = jc.createUnmarshaller();
        u.setEventHandler(collector);
        if (pass == 0)
        {
          Schema schema = CEPLoadSchema.getSchema();
          if (schema != null)
          {
            u.setSchema(schema);
          }
          else
          {
            nopass = 1;
          }
        }
        CEP load = null;
        if (src instanceof InputStream)
        {
          Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource((InputStream) src));
          load = (CEP) u.unmarshal(xmlSource);
        }
        else if (src instanceof Document)
        {
          load = (CEP) u.unmarshal((Document) src);
        }
        if (load != null)
        {
          m_load = load.getCEPLOAD();
          m_unload = load.getCEPUNLOAD();
          m_ddls = load.getCEPDDL();
          return true;
        }
      }
      catch(UnmarshalException e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
        if (pass == 0 && nopass==2)
          LogUtil.info(LoggerType.TRACE, "second try");
      }
      catch(JAXBException e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
        if (pass == 0 && nopass==2)
          LogUtil.info(LoggerType.TRACE, "second try");
      }
      catch(ParserConfigurationException e)
      {
        LogUtil.warning(LoggerType.TRACE, e.toString());
      }
      catch(SAXException e)
      {
         LogUtil.warning(LoggerType.TRACE, e.toString());
      }
      if (pass == 0)
      {
        m_validationEvents = collector.getEvents();
        for (ValidationEvent event : m_validationEvents) {
            if (event.getSeverity() == ValidationEvent.WARNING) 
            {
              LogUtil.warning(LoggerType.CUSTOMER, event.getMessage() + "\n" + 
              ((event.getLinkedException() != null) ? event.getLinkedException().toString():""));
            }
            else
            {
              LogUtil.severe(LoggerType.CUSTOMER, event.getMessage() + "\n" + 
              ((event.getLinkedException() != null) ? event.getLinkedException().toString():""));
            }
        }
      }
    }
    return false;
  }
  
  public boolean parseDocument(Document doc, boolean retry)
  {
    return parse(doc, retry);
  }
    
  public boolean parseStream(InputStream stream, boolean retry)
  {
    return parse(stream, retry);
  }
     
  public boolean parseStr(String xml, boolean retry)
  {
    try
    {
      ByteArrayInputStream bs = new ByteArrayInputStream(xml.getBytes("UTF8"));
      return parse(bs, retry);
    }catch(UnsupportedEncodingException e)
    {
      LogUtil.severe(LoggerType.TRACE, "Unsupported encoding " + e.toString());
      return false;
    }
  }
  
  public boolean parseFile(String fileName, boolean retry)
  {
    try
    {
      FileInputStream s = new FileInputStream(fileName);
      return parse(s, retry);
    }
    catch(FileNotFoundException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
      LogUtil.logStackTrace(e);
      return false;
    }
  }
}
