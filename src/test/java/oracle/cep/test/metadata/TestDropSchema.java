/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestDropSchema.java /main/3 2010/05/27 09:44:55 parujain Exp $ */

/* Copyright (c) 2008, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    10/01/08 - drop schema
    parujain    10/01/08 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/metadata/TestDropSchema.java /main/3 2010/05/27 09:44:55 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.metadata;

import java.util.LinkedList;

import junit.framework.TestCase;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.metadata.cache.NameSpace;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.storage.IStorage;
import oracle.cep.storage.IStorageContext;
import oracle.cep.test.InterpDrv;
import oracle.cep.exceptions.CEPException;
public class TestDropSchema extends TestCase
{
  ExecContext m_execContext;
  InterpDrv m_driver = null;
  
  public TestDropSchema(String name)
  {
    super(name);
  }
  
  /**
   * Sets up the test fixture.
   * (Called before every test case method.)
   */
  public void setUp()
  {
      m_driver = InterpDrv.getInstance();
      try {
          if (!m_driver.setUp(null)) fail();
          m_execContext = m_driver.getExecContext();
      } catch (Exception e) 
      {
          System.out.println(e);
      }
      
      // Further initalization
  }

  /**
   * Tears down the test fixture.
   * (Called after every test case method.)
   */
  public void tearDown()
  {
      // Destroy previous run status..
      m_driver.tearDown();
      m_driver = null;
  }
  
  protected static class SchemaDesc
  {
    String ddl;
    String schema; 
    
    SchemaDesc(String cql, String schema)
    {
      this.ddl = cql;
      this.schema = schema;
    }
  }
  
  public boolean executeDDL(String ddl) throws CEPException
  {
      CommandInterpreter cmd = m_execContext.getCmdInt();
      Command c = m_execContext.getCmd(); 
      c.setCql(ddl);
      cmd.execute(c);
      
      System.out.println("Executed " + "\"" + ddl + "\"");
      if (c.isBSuccess()) {
          String res = c.getErrorMsg();
          if (res == null) 
          {
              System.out.println("Success");
          } else {
              System.out.println(res);
          }
      } else {
          System.out.println("Error: " + c.getErrorMsg());        
      }
      return c.isBSuccess();
  }
 // This is mantasin.cqlx  
  protected static SchemaDesc[] s_schema1_ddls = {
   new SchemaDesc(" register relation Acct(ACCT_INTRL_ID CHAR(50), MANTAS_ACCT_BUS_TYPE_CD CHAR(20), ACCT_EFCTV_RISK_NB INTEGER) ", "schema1"),
 new SchemaDesc(" register relation AcctAddr(ACCT_INTRL_ID CHAR(50), ADDR_CNTRY_CD CHAR(2)) ", "schema1"),
 new SchemaDesc(" register stream CashTrxn(TxnId integer, ACCT_INTRL_ID CHAR(50), TRXN_BASE_AM integer, CSTM_1_TX CHAR(2), DBT_CDT_CD CHAR(20), MANTAS_TRXN_CHANL_CD CHAR(20), TRXN_LOC_ADDR_SEQ_ID integer) ", "schema1"),
 new SchemaDesc("create view ValidAccounts(ACCT_INTRL_ID) as select ACCT_INTRL_ID from Acct where (((MANTAS_ACCT_BUS_TYPE_CD = \"RBK\") OR (MANTAS_ACCT_BUS_TYPE_CD = \"RBR\")) AND (ACCT_EFCTV_RISK_NB != 2))", "schema1"),
 new SchemaDesc("create view ValidCashTrxn(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from CashTrxn where DBT_CDT_CD = \"D\" and MANTAS_TRXN_CHANL_CD = \"ATM\"", "schema1"),
 new SchemaDesc(" create view ValidCashTrxn1(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select * from ValidCashTrxn[NOW]", "schema1"),
 new SchemaDesc(" create view ValidCashTrxn2(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as RStream(ValidCashTrxn1 IN ValidAccounts)", "schema1"),
 new SchemaDesc("create view Dummy1(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as select ValidCashTrxn2.TxnId, ValidCashTrxn2.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[NOW], AcctAddr where ValidCashTrxn2.ACCT_INTRL_ID = AcctAddr.ACCT_INTRL_ID", "schema1"),
 new SchemaDesc("create view Dummy2(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[NOW] ", "schema1"),
 new SchemaDesc("create view ValidCashForeignTxn(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as RStream(Dummy2 except Dummy1)", "schema1"),
 new SchemaDesc(" create view validcashforeigntxn48hrs(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as select * from ValidCashForeignTxn[range 48 hours] ", "schema1"),
 new SchemaDesc("create view viewq1(ACCT_INTRL_ID, sumForeignTxns) as IStream(select ACCT_INTRL_ID, sum(TRXN_BASE_AM) as frgnTxn from validcashforeigntxn48hrs group by ACCT_INTRL_ID having sum(TRXN_BASE_AM) > 1000) ", "schema1"),
 new SchemaDesc("create query q1 as select * from viewq1 ", "schema1"),
 new SchemaDesc(" create view viewq1now(ACCT_INTRL_ID, sumForeignTxns) as select * from viewq1[NOW] ", "schema1"),
 new SchemaDesc(" create query q1Txns as IStream(validcashforeigntxn48hrs IN viewq1now)", "schema1"),
 new SchemaDesc(" create view viewq2(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as IStream(select ACCT_INTRL_ID, sum(TRXN_BASE_AM), count(distinct ADDR_CNTRY_CD), count(distinct TRXN_LOC_ADDR_SEQ_ID) from validcashforeigntxn48hrs group by ACCT_INTRL_ID having (sum(TRXN_BASE_AM) * 100) >= (1000 * 60) and (count(distinct ADDR_CNTRY_CD) >= 2)) ", "schema1"),
 new SchemaDesc("create query q2 as select * from viewq2 ", "schema1"),
 new SchemaDesc(" create view viewq2now(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as select * from viewq2[NOW] ", "schema1"),
 new SchemaDesc("create query q2Txns as IStream(validcashforeigntxn48hrs IN viewq2now)", "schema1"),
 new SchemaDesc(" create view validcashforeigntxn60min(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as select * from ValidCashForeignTxn[range 60 minutes] ", "schema1"),
 new SchemaDesc(" create view viewq3(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as IStream(select ACCT_INTRL_ID, sum(TRXN_BASE_AM), count(distinct ADDR_CNTRY_CD), count(distinct TRXN_LOC_ADDR_SEQ_ID) from validcashforeigntxn60min group by ACCT_INTRL_ID having (sum(TRXN_BASE_AM)*100) >= (500*60) and (count(distinct ADDR_CNTRY_CD) >= 2))", "schema1"),
 new SchemaDesc("create query q3  as select * from viewq3 ", "schema1"),
 new SchemaDesc(" create view viewq3now(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as select * from viewq3[NOW]", "schema1"),
 new SchemaDesc("create query q3Txns as IStream(validcashforeigntxn60min IN viewq3now)", "schema1"),
 new SchemaDesc("create window range_slide1(winrange int, winslide int) implement using \"oracle.cep.test.userfunctions.TkRangeSlide\"", "schema1"), 
 new SchemaDesc("create window range_slide(winrange int, winslide int) implement using \"oracle.cep.test.userfunctions.TkRangeSlide\"", "schema1"), 
 new SchemaDesc("create function concat2(c1 char, c2 char) return char as language java name \"oracle.cep.test.userfunctions.TkUsrConcat\"", "schema1"),
 new SchemaDesc("create function concat3(c1 char, c2 char) return char as language java name \"oracle.cep.test.userfunctions.TkUsrConcat\"", "schema1"),
 new SchemaDesc("create function fib(n int) return int as language java name \"oracle.cep.test.userfunctions.TkUsrFib\"", "schema1"),
 new SchemaDesc("create function fib(n float) return int as language java name \"oracle.cep.test.userfunctions.TkUsrFib2\"", "schema1"), 
  };
  
  protected void runDDLs(SchemaDesc[] ddls)
  {
    int excount = 0;
    for(SchemaDesc desc: ddls)
    {
      m_execContext.setSchema(desc.schema);
       try{
    	 String execddl = desc.ddl;
         int index = execddl.indexOf("schema"); 
         if(index != -1)
           m_execContext.dropSchema(desc.schema, true);
         else
           executeDDL(execddl);
       }catch(CEPException e)
       {
    	 System.out.println("GOT EXCEPTION :" + e.getMessage());
    	 excount++;
       }
    }
    assertEquals(excount, 0);
  }
  
  protected void dropSchema(String[] schemas)
  {
    int excount = 0;
    for (int i = 0; i < schemas.length; i++)
    {
      String schema = schemas[i];
      try
      {
       // executeDDL("drop schema " + schema);
        m_execContext.dropSchema(schema, true);
      }catch(CEPException e)
      {
        System.out.println("GOT EXCEPTION :" + e.getMessage());
        excount++;
      }
    }
    assertEquals(excount, 0);
  }
  
  protected void verify(String[] schemas, int[] objcounts)
  {
    for (int i = 0; i < schemas.length; i++)
    {
      String schemaName = schemas[i];
      IStorage storage = m_execContext.getServiceManager().getStorageManager().getMetadataStorage();
      LinkedList<CacheObject> list = new LinkedList<CacheObject>();
      NameSpace[] nSp = NameSpace.values();
      for(NameSpace ns : nSp)
      {
        if (ns == NameSpace.OBJECTID || ns == NameSpace.SYSTEM) continue;
        IStorageContext ctx = storage.initQuery(ns.name(), schemaName);
        CacheObject obj = (CacheObject)storage.getNextRecord(ctx);
        while(obj != null)
        {
          list.add(obj);
          obj = (CacheObject)storage.getNextRecord(ctx);
        }
      }  
      int objCount = (objcounts == null ? 0:objcounts[i]);
      if (list.size() != objCount)
      {
        System.out.println(schemaName + " has " + list.size() + " objects: " + objCount + " expected.");
        for (CacheObject obj : list)
        {
          System.out.println(obj.toString());
        }
      }
      assertEquals(objCount, list.size());
    }
  }
  
  public void testDropSchema()
  {
    String[] schemas = {"schema1"};
    runDDLs(s_schema1_ddls);
    dropSchema(schemas);
    verify(schemas, null);
  }
    
  public void main(String[] args)
  {
      junit.textui.TestRunner.run(TestDropSchema.class);
  }
}
