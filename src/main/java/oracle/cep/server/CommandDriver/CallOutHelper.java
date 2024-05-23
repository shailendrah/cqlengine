/* $Header: CallOutHelper.java 02-apr-2008.01:55:53 sbishnoi Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/02/08 - modifying class.forName to incorporate ClassLoader
                           information
    sbishnoi    02/22/08 - Creation
 */

package oracle.cep.server.CommandDriver;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  @version $Header: CallOutHelper.java 02-apr-2008.01:55:53 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

class CallOutHelper
{
  /**
   * Parse CallOutEPR mentioned in the DDL
   * <DDL>alter system start trusted callout EPR_STRING</DDL> and return 
   * class name and constructor arguments in form of a string array
   * @param epr
   * @return
   * @throws CEPException
   */
  public static String[] parseEpr(String epr) throws CEPException
  {
    DOMParser dp;
    XMLDocument doc;
    
    try
    {
      dp = new DOMParser();
      // create a document from the source
      Reader reader = new StringReader(epr);
      dp.parse(reader);
      doc = dp.getDocument();
    }
    catch (Exception e)
    {
      throw new CEPException(InterfaceError.INVALID_SOURCE, e);
    }

    NodeList eprNodeList 
      = doc.getChildrenByTagName(new String("EndPointReference"));
    
    // Since the document has been validated at insertion time, there should
    // be only 1 such node
    assert eprNodeList.getLength() == 1;
    Node eprNode = eprNodeList.item(0);

    assert eprNode instanceof XMLElement;
    XMLElement eprElem = (XMLElement) eprNode;

    NodeList callOutNameLst 
      = eprElem.getChildrenByTagName(new String("CallOutName"));
    Node callOutNameNode = callOutNameLst.item(0);
    Node callOutNameVal = callOutNameNode.getFirstChild();
    
    ArrayList<String> callOutEPR = new ArrayList<String>();
    callOutEPR.add(callOutNameVal.getNodeValue());
    
    NodeList callOutArgsLst
      = eprElem.getChildrenByTagName(new String("CallOutArguments"));
    Node callOutArgsNode = callOutArgsLst.item(0);
    
    if(callOutArgsNode != null)
    {
      assert callOutArgsNode instanceof XMLElement : callOutArgsNode;
      XMLElement callOutArgElement = (XMLElement)callOutArgsNode;
      
      NodeList callOutArgLst
        = callOutArgElement.getChildrenByTagName("Argument");
      int numArgs = callOutArgLst.getLength();
    
      Node     callOutArgNode = null;
      Node     callOutArgVal  = null;
      
      for(int i =0 ; i < numArgs; i++)
      {
         callOutArgNode = callOutArgLst.item(i);
         callOutArgVal  = callOutArgNode.getFirstChild();
         callOutEPR.add(callOutArgVal.getNodeValue());
      }
    }
    return callOutEPR.toArray(new String[]{});
  }
  
  /**
   * Start a Thread for the given Class Name and
   * will pass given argument via appropriate constructor
   * @param callOutEPR will contain [callOut Name and Arguments] 
   * @throws CEPException
   */
  public static void startCallOut(String[] callOutEPR) throws CEPException
  {
    assert callOutEPR.length > 0 ;
    
    String className = callOutEPR[0];
    int numArgs =  callOutEPR.length - 1;
    
    Object argArray = Array.newInstance(String.class, numArgs);
    System.arraycopy(callOutEPR, 1, argArray, 0, numArgs);
    
    Class<?> targetClass;
    Runnable targetClassInstance = null;
    Thread   newTask = null;
    
    try
    {
      targetClass = Class.forName(className, true,
                      Thread.currentThread().getContextClassLoader());
      if(numArgs == 0)
      {
        targetClassInstance = (Runnable)targetClass.newInstance();
      }
      else if(numArgs > 0)
      {
        Constructor targetConstructor = targetClass.getDeclaredConstructor(String[].class);
        targetConstructor.setAccessible(true);
        targetClassInstance = (Runnable)targetConstructor.newInstance(argArray);
      }
    }
    catch(ClassNotFoundException e) {
      throw new CEPException(InterfaceError.CLASS_NOT_FOUND, 
        new Object[]{className});
    }
    catch(InstantiationException e){
      throw new CEPException(InterfaceError.CLASS_NOT_INSTANTIATED, 
        new Object[]{className});
    }
    catch(IllegalAccessException e){
      throw new CEPException(InterfaceError.CLASS_NOT_ACCESSIBLE,
        new Object[]{className});
    }
    catch(ClassCastException e) {
      throw new CEPException(InterfaceError.CALLOUT_CLASS_NOT_VALID,
        new Object[]{className});
    }
    catch(NoSuchMethodException e){
      throw new CEPException(InterfaceError.CLASS_NOT_INSTANTIATED,
        new Object[]{className});
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    
    if(targetClassInstance != null)
    {
      newTask = new Thread(targetClassInstance);
      newTask.start();
    }
    
  } // end of startCallOut
  
}
