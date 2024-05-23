/* $Header: pcbpel/cep/test/src/oracle/cep/test/execution/xml/TestXML.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      03/06/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/execution/xml/TestXML.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.test.execution.xml;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import oracle.cep.execution.xml.PreparedXQuery;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.execution.xml.XMLSequence;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.server.CEPServerRegistryImpl;
import oracle.cep.service.CEPManager;
import oracle.xml.parser.v2.DOMParser;
import oracle.xquery.XQMesg;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestXML extends TestCase
{
  protected static final int N_QUERIES = 4;
  XmlManager         xmlMgr;  
  
  protected static class QryRes implements Externalizable
  {
    PreparedXQuery query;
    XMLItem            srcItem;
    XMLSequence        qryResult;
    LinkedList<XMLItem> resultItems;
    String             srcStr;
    LinkedList<String> resultStrs;
    public QryRes() {}
    
    public QryRes(PreparedXQuery q, XMLItem src, XMLSequence res)
    {
      query = q;
      srcItem = src;
      qryResult = res;
      resultItems = new LinkedList<XMLItem>();
    }
    
    public QryRes copy()
    {
      QryRes n = new QryRes();
      n.query = query;
      n.srcStr = srcItem.toString();
      n.qryResult = qryResult;
      n.resultStrs = new LinkedList<String>();
      for (XMLItem i : resultItems)
      {
        n.resultStrs.add(i.toString());
      }
      return n;
    }
    
    public void addResultItem(XMLItem item) 
    {
      resultItems.add(item);
    }
    
    public boolean equals(Object o)
    {
      QryRes other = (QryRes) o;
      if (query != other.query) return false;
      if (other.srcItem == null || !srcStr.equals(other.srcItem.toString()))
        return false;
      if (qryResult != other.qryResult) return false;
      if (resultStrs.size() != other.resultItems.size()) return false;
      for (int i = 0; i < resultStrs.size(); i++)
      {
        String ii = this.resultStrs.get(i);
        XMLItem oi = other.resultItems.get(i);
        if (oi == null ) return false;
        String os = oi.toString();
        if (!ii.equals(os)) return false;
      }
      return true;
    }
    
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException
    {
      query = (PreparedXQuery) in.readObject();
      srcItem = (XMLItem) in.readObject();
      qryResult = (XMLSequence) in.readObject();
      int len = in.readInt();
      resultItems = new LinkedList<XMLItem>();
      for (int i = 0; i < len; i++)
      {
        XMLItem item = (XMLItem) in.readObject();
        resultItems.add(item);
      }
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
      out.writeObject(query);
      out.writeObject(srcItem);
      out.writeObject(qryResult);
      int len = resultItems.size();
      out.writeInt(len);
      for (XMLItem i : resultItems)
      {
        out.writeObject(i);
      }
    }
  }
  
  protected static class Qry
  {
    PreparedXQuery query;
    LinkedList<QryRes>    res;
    QryRes curRes;
    Qry()
    {
      res = new LinkedList<QryRes>();
    }
    
    void addResult(XMLItem src, XMLSequence seq) 
    {
      QryRes r = new QryRes(query, src, seq);
      res.add(r); 
      curRes = r;
    }
    void addResultItem(XMLItem item)
    {
      assert (curRes != null);
      curRes.addResultItem(item);
    }
  }
  protected Qry[] queries;
  protected String qrystr0 = "//FILL";
  protected String qrystr1 = "fn:data(../@ID)";
  protected String qrystr2 = "fn:data(@LastShares)";
  protected String qrystr3 = "fn:data(@LastPx)";
  protected String itemstr0 = "<FIXML><ORDER ID=\"A2006101\"><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"300\" LastPx=\"81.92\"/><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"900\" LastPx=\"81.90\"/><FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"800\" LastPx=\"81.96\"/></ORDER></FIXML>";
  protected String itemstr1 = "<FIXML><ORDER ID=\"A2006102\"><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"111\" LastPx=\"11.11\"/><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"222\" LastPx=\"22.22\"/><FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"333\" LastPx=\"33.33\"/></ORDER></FIXML>";
  protected String [] results0 = new String[] {
       "<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"300\" LastPx=\"81.92\"/>\n",
       "<FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"900\" LastPx=\"81.90\"/>\n",
       "<FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"800\" LastPx=\"81.96\"/>\n" };
  protected String [] results1 = new String[] {
       "A2006101\n",
       "A2006101\n",
       "A2006101\n" };
  protected String [] results2 = new String[] {
        "300\n",
        "900\n",
        "800\n" };
  protected String [] results3 = new String[] {
            "81.92\n",
            "81.90\n",
        "81.96\n" };
  
  public TestXML(String name)
  {
    super(name);
    initXMLManager();
  }

  public XmlManager initXMLManager()
  {
    CEPManager.resetInstance();
    CEPManager cepMgr = CEPManager.getInstance();
    ConfigManager cfg = new ConfigManager();
    cepMgr.setConfig(cfg);
    CEPServerRegistryImpl reg = new CEPServerRegistryImpl();
    cepMgr.setServerRegistry(reg);

    try {
    cepMgr.init();
  } catch (Exception e) {
    e.printStackTrace();
  }
    XmlManager xmlMgr = cepMgr.getSystemExecContext().getXmlMgr();
    xmlMgr.setMemMode(true);
    return xmlMgr;
  }

  protected XMLItem getItem(PreparedXQuery xq, String v) throws Exception
  {
    XMLItem item = xq.createItem();

    DOMParser dom = new DOMParser();
    Reader reader = new StringReader(v);
    dom.parse(reader);
    item.setNode(dom.getDocument());
    item.dump();
    return item;
  }
  
  protected String getItemStr(XMLSequence res) throws Exception
  {
    StringWriter wr = new StringWriter();
    PrintWriter wrp = new PrintWriter(wr);
    res.printResult(wrp, XQMesg.newInstance(null));
    wrp.flush();
    wr.flush();
    return wr.toString();
  }

  public void query(String qryStr, String xmlStr, String var, int val, String[] results) throws Exception
  {
    if(xmlMgr == null)
      xmlMgr = initXMLManager();
    PreparedXQuery qry = xmlMgr.createPreparedXQuery(qryStr);
    XMLItem item = getItem(qry, xmlStr);
    qry.setContextItem(item);
    qry.setInt(new QName(var), val);
    XMLSequence res = qry.executeQuery(false);
    
    int pos = 0;
    while (res.next())
    {
      String  result = getItemStr(res);
      System.out.print(pos + " : " + result);
      assertTrue(result.equals(results[pos]));
      pos++;
    }
    assertTrue(pos == results.length);
  }
  
  public void test1() throws Exception
  {
    String qrystr = "for $i in /PDRecord where $i/PDId <= $x return $i/PDName";
    String xmlstr = "<PDRecord><PDId>6</PDId><PDName>hello2</PDName><PDName>hello3</PDName></PDRecord>";
    String results[] = {"<PDName>hello2</PDName>\n", "<PDName>hello3</PDName>\n"};
    query(qrystr, xmlstr, "x", 11, results);
  }
  
  private void waitEnter() throws Exception
  {
    System.out.println( "Press enter to exit." );
    System.out.flush();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    in.readLine();
  }
  
  private static boolean compareStr(String str1, String str2)
  {
	  String s1 = str1.replaceAll("(\\r|\\n)", "");
	  String s2 = str2.replaceAll("(\\r|\\n)", "");
	  return (s1.equals(s2));
  }
  
  public void prepareTest2(String itemstr, boolean verify) throws Exception
  {
     queries = new Qry[N_QUERIES];
     for (int i = 0; i < N_QUERIES; i++)
       queries[i] = new Qry();
     
     if(xmlMgr == null)
        xmlMgr = initXMLManager();
     queries[0].query = xmlMgr.createPreparedXQuery(qrystr0);
     queries[1].query = xmlMgr.createPreparedXQuery(qrystr1);
     queries[2].query = xmlMgr.createPreparedXQuery(qrystr2);
     queries[3].query = xmlMgr.createPreparedXQuery(qrystr3);
     
     XMLItem item0 = getItem(queries[0].query, itemstr);
     queries[0].query.setContextItem(item0);
     XMLSequence res0 = queries[0].query.executeQuery(false);
     queries[0].addResult(item0, res0);
     int poses[] = new int[N_QUERIES];
     Arrays.fill(poses, 0);
     XMLItem item;
     while (res0.next())
     {
       item0 = res0.getItem();
       queries[0].addResultItem(item0);
       String  result = getItemStr(res0);
       System.out.print(poses[0] + " : " + result);
       item0.dump();
       if (verify) 
       {
         String br = results0[poses[0]++];
         assertTrue( compareStr(result, br));
       }
       
       queries[1].query.setContextItem(item0);
       XMLSequence res1 = queries[1].query.executeQuery(false);
       queries[1].addResult(item0, res1);
       while (res1.next())
       {
         item = res1.getItem();
         queries[1].addResultItem(item);
         result = getItemStr(res1);
         
         System.out.print("id="+result);
         item.dump();
         if (verify)
         {
           String br = results1[poses[1]++];
           assertTrue( compareStr(result, br));
         }
       }  

       queries[2].query.setContextItem(item0);
       XMLSequence res2 = queries[2].query.executeQuery(false);
       queries[2].addResult(item0, res2);
       while (res2.next())
       {
         item = res2.getItem();
         queries[2].addResultItem(item);
         result = getItemStr(res2);
         
         System.out.print("LastShares="+result);
         item.dump();
         if (verify) 
         {
           String br = results2[poses[2]++];
           assertTrue( compareStr(result, br));
         }
       }

       queries[3].query.setContextItem(item0);
       XMLSequence res3 = queries[3].query.executeQuery(false);
       queries[3].addResult(item0, res3);
       while (res3.next())
       {
         item = res3.getItem();
         queries[3].addResultItem(item);
         result = getItemStr(res3);
         
         System.out.print("LastPx="+result);
         item.dump();
         if (verify)
         {
           String br = results3[poses[3]++];
           assertTrue( compareStr(result, br));
         }
       }
     }
  }
  
  public void test2() throws Exception
  {
    prepareTest2(itemstr0, true);
    prepareTest2(itemstr1, false);
  }
  
  /*
  public void testMem() throws Exception
  {
     String qrystr0 = "//FILL";
     String qrystr1 = "fn:data(../@ID)";
     String qrystr2 = "fn:data(@LastShares)";
     String qrystr3 = "fn:data(@LastPx)";
     String itemstr0 = "<FIXML><ORDER ID=\"A2006101\"><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"300\" LastPx=\"81.92\"/><FILL ExecType=\"partial\" Sym=\"IBM\" LastShares=\"900\" LastPx=\"81.90\"/><FILL ExecType=\"full\" Sym=\"IBM\" LastShares=\"800\" LastPx=\"81.96\"/></ORDER></FIXML>";
     IPreparedXQuery qry0 = XmlManager.createPreparedXQuery(qrystr0);
     IPreparedXQuery qry1 = XmlManager.createPreparedXQuery(qrystr1);
     IPreparedXQuery qry2 = XmlManager.createPreparedXQuery(qrystr2);
     LinkedList<XMLSequence> results = new LinkedList<XMLSequence>();
     XMLItem item0 = getItem(qry0, itemstr0);
     qry0.setContextItem(item0);
     XMLSequence res0 = qry0.executeQuery(false);
     
     //results.add(res0);
     
     LinkedList<XMLItem> items0 = new LinkedList<XMLItem>();
     int pos = 0;
     while (res0.next())
     {
       XMLItem item = res0.getItem();
       String  result = getItemStr(res0);
       System.out.print(pos + " : " + result);
       items0.add(item);
       pos++;
     }
     assertTrue(items0.size() == 3);
     XMLItem item1 = items0.get(0);
     qry1.setContextItem(item1);
     XMLSequence res1 = qry1.executeQuery(false);
     //results.add(res1);
     
     res1.next();
     String  result = getItemStr(res1);
     //assertTrue(result.equals("A2006101\n"));

     //waitEnter();
     //items0.clear();
     qry0 = null;
     qry1 = null;
     qry2 = null;
     item0 = null;
     item1 = null;
     res0 = null;
     res1 = null;
     Runtime runtime = Runtime.getRuntime();
     for (int i = 0; (i < 100); ++ i)
     {
       runtime.runFinalization();
       runtime.gc();
       Thread.yield();
     }
     waitEnter();
     
  }
  */
  
  public static Test suite()
  {
    if (SINGLE_TEST_NAME != null)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestXML(SINGLE_TEST_NAME));
      return suite;
    } else {
      return new TestSuite(TestXML.class);
    }
  }
  
  public static final String SINGLE_TEST_NAME = "test2";
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestXML.suite());
  }
 }
