/* $Header: pcbpel/cep/test/src/oracle/cep/test/statistics/TestStat.java /main/1 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    04/22/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/statistics/TestStat.java /main/1 2009/05/12 19:25:47 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.statistics;

import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStats;
import oracle.cep.statistics.IStatsFactory;
import oracle.cep.statistics.iterator.ReaderQueueStatsIterator;
import oracle.cep.statistics.iterator.StoreStatsIterator;
import oracle.cep.statistics.iterator.WriterQueueStatsIterator;
import oracle.cep.test.InterpDrv;
import junit.framework.TestCase;

public class TestStat extends TestCase
{
	InterpDrv m_driver = null;
	ExecContext m_execContext = null;
	IStatsFactory m_factory = null;
	  
	  public TestStat(String name)
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
	    	  ConfigManager cfg = new ConfigManager();
	    	  cfg.setSchedOnNewThread(false);
	    	  cfg.setDirectInterop(true);
	    	  cfg.setIsRegressPushMode(true);
	          if (!m_driver.setUp(cfg)) fail();
	          m_execContext = m_driver.getExecContext();
	         // m_execContext.getServiceManager().getConfigMgr().setSchedOnNewThread(true);
	         
	          System.out.println("TestStat: initialize queries");
	          for (String qry : s_queries) {
	            StringBuffer buf = new StringBuffer();
	          //  buf.append("<CEP><CEP_DDL> ");
	            buf.append(qry);
	         //   buf.append(" </CEP_DDL>");
	        //    buf.append("</CEP>\n");

	            System.out.println(buf.toString());
	            m_driver.setCqlX(buf.toString());
	          }
	          
	          m_factory = new StatsFactory();
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
	  
	  static final String s_queries[] =
	  {
		"register relation Acct(ACCT_INTRL_ID CHAR(50), MANTAS_ACCT_BUS_TYPE_CD CHAR(20), ACCT_EFCTV_RISK_NB INTEGER) ",
		" alter relation Acct add source \"<EndPointReference><Address>file://@TEST_DATA@/inpMantasAcct.txt</Address></EndPointReference>\"",
		"register relation AcctAddr(ACCT_INTRL_ID CHAR(50), ADDR_CNTRY_CD CHAR(2)) ",
		"alter relation AcctAddr add source \"<EndPointReference><Address>file://@TEST_DATA@/inpMantasAcctAddr.txt</Address></EndPointReference>\"",
		"register stream CashTrxn(TxnId integer, ACCT_INTRL_ID CHAR(50), TRXN_BASE_AM integer, CSTM_1_TX CHAR(2), DBT_CDT_CD CHAR(20), MANTAS_TRXN_CHANL_CD CHAR(20), TRXN_LOC_ADDR_SEQ_ID integer)",
		 "alter stream CashTrxn add source \"<EndPointReference><Address>file://@TEST_DATA@/inpMantasCashTrxn.txt</Address></EndPointReference>\"",
		 "create view ValidAccounts(ACCT_INTRL_ID) as select ACCT_INTRL_ID from Acct where (((MANTAS_ACCT_BUS_TYPE_CD = \"RBK\") OR (MANTAS_ACCT_BUS_TYPE_CD = \"RBR\")) AND (ACCT_EFCTV_RISK_NB != 2))",
          "create view ValidCashTrxn(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from CashTrxn where DBT_CDT_CD = \"D\" and MANTAS_TRXN_CHANL_CD = \"ATM\"",
         "create view ValidCashTrxn1(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select ValidCashTrxn.TxnId, ValidCashTrxn.ACCT_INTRL_ID, ValidCashTrxn.TRXN_BASE_AM, ValidCashTrxn.CSTM_1_TX, ValidCashTrxn.TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn[NOW], ValidAccounts where ValidCashTrxn.ACCT_INTRL_ID = ValidAccounts.ACCT_INTRL_ID ",

		  "create view ValidCashTrxn2(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as IStream(select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn1) ",

		  " create view Dummy1(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as select ValidCashTrxn2.TxnId, ValidCashTrxn2.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[range 48 hours], AcctAddr where ValidCashTrxn2.ACCT_INTRL_ID = AcctAddr.ACCT_INTRL_ID",
		  "create view Dummy2(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[range 48 hours] ",

		  "create view ValidCashForeignTxn(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as Dummy2 except Dummy1",

		  "create view viewq1(ACCT_INTRL_ID, sumForeignTxns) as IStream(select ACCT_INTRL_ID, sum(TRXN_BASE_AM) as frgnTxn from ValidCashForeignTxn group by ACCT_INTRL_ID having sum(TRXN_BASE_AM) > 1000) ",

		  "create query q1 as select * from viewq1 ",
		  " alter query q1 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ1.txt</Address></EndPointReference>\"",
		  " alter query q1 start ",

		  "create query q1Txns as IStream(select TxnId, ValidCashForeignTxn.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from viewq1[NOW], ValidCashForeignTxn where viewq1.ACCT_INTRL_ID = ValidCashForeignTxn.ACCT_INTRL_ID)",

		  " alter query q1Txns add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ1Txns.txt</Address></EndPointReference>\"",
		  " alter query q1Txns start ",
		  "create view q2Cond1(ACCT_INTRL_ID, sumForeign) as select ACCT_INTRL_ID, sum(TRXN_BASE_AM) from ValidCashForeignTxn group by ACCT_INTRL_ID having (sum(TRXN_BASE_AM) * 100) >= (1000 * 60) ",

		  "create view q2Cond2_1(countForeign, ACCT_INTRL_ID) as select distinct ADDR_CNTRY_CD, ACCT_INTRL_ID from ValidCashForeignTxn ",

		  "create view q2Cond2(ACCT_INTRL_ID, countForeign) as select ACCT_INTRL_ID, count(*) from q2Cond2_1 group by ACCT_INTRL_ID having count(*) >= 2 ",

		  "create view q2Cond3_1(ACCT_INTRL_ID, foreignLocn) as select distinct ACCT_INTRL_ID, TRXN_LOC_ADDR_SEQ_ID from ValidCashForeignTxn ",

		  "create view q2Cond3(ACCT_INTRL_ID, countForeignLocn) as select ACCT_INTRL_ID, count(foreignLocn) from q2Cond3_1 group by ACCT_INTRL_ID ",

		  "create view q2Cond12(ACCT_INTRL_ID, sumForeign, countForeign) as select q2Cond1.ACCT_INTRL_ID, q2Cond1.sumForeign, q2Cond2.countForeign from q2Cond1, q2Cond2 where q2Cond1.ACCT_INTRL_ID = q2Cond2.ACCT_INTRL_ID ",

		  "create view viewq2(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as IStream(select q2Cond12.ACCT_INTRL_ID, q2Cond12.sumForeign, q2Cond12.countForeign, q2Cond3.countForeignLocn from q2Cond12, q2Cond3 where q2Cond12.ACCT_INTRL_ID = q2Cond3.ACCT_INTRL_ID) ",

		  "create query q2 as select * from viewq2 ",
		  " alter query q2 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ2.txt</Address></EndPointReference>\"",
		  "alter query q2 start ",

		  "create query q2Txns as IStream(select TxnId, ValidCashForeignTxn.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from viewq2[NOW], ValidCashForeignTxn where viewq2.ACCT_INTRL_ID = ValidCashForeignTxn.ACCT_INTRL_ID)",

		  " alter query q2Txns add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ2Txns.txt</Address></EndPointReference>\"",
		 "alter query q2Txns start ",


		  "create view LoopDummy1(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as select ValidCashTrxn2.TxnId, ValidCashTrxn2.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[range 60 minutes], AcctAddr where ValidCashTrxn2.ACCT_INTRL_ID = AcctAddr.ACCT_INTRL_ID",
		  "create view LoopDummy2(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID) as select TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, CSTM_1_TX, TRXN_LOC_ADDR_SEQ_ID from ValidCashTrxn2[range 60 minutes] ",

		  "create view ValidLoopCashForeignTxn(TxnId, ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID) as LoopDummy2 except LoopDummy1",

		  "create view q3Cond1(ACCT_INTRL_ID, sumForeign) as select ACCT_INTRL_ID, sum(TRXN_BASE_AM) from ValidLoopCashForeignTxn group by ACCT_INTRL_ID having (sum(TRXN_BASE_AM) * 100) >= (500 * 60) ",

		 "create view q3Cond2_1(countForeign, ACCT_INTRL_ID) as select distinct ADDR_CNTRY_CD, ACCT_INTRL_ID from ValidLoopCashForeignTxn ",

		 "create view q3Cond2(ACCT_INTRL_ID, countForeign) as select ACCT_INTRL_ID, count(*) from q3Cond2_1 group by ACCT_INTRL_ID having count(*) >= 2 ",

		 "create view q3Cond12(ACCT_INTRL_ID, sumForeign, countForeign) as select q3Cond1.ACCT_INTRL_ID, q3Cond1.sumForeign, q3Cond2.countForeign from q3Cond1, q3Cond2 where q3Cond1.ACCT_INTRL_ID = q3Cond2.ACCT_INTRL_ID ",

		  "create view q3Cond3_1(ACCT_INTRL_ID, TRXN_LOC_ADDR_SEQ_ID) as select distinct ACCT_INTRL_ID, TRXN_LOC_ADDR_SEQ_ID from ValidLoopCashForeignTxn ",

		  "create view q3Cond3(ACCT_INTRL_ID, countForeignLocn) as select ACCT_INTRL_ID, count(TRXN_LOC_ADDR_SEQ_ID) from q3Cond3_1 group by ACCT_INTRL_ID ",

		  "create view viewq3(ACCT_INTRL_ID, sumForeign, countForeign, countForeignLocn) as IStream(select q3Cond12.ACCT_INTRL_ID, q3Cond12.sumForeign, q3Cond12.countForeign, q3Cond3.countForeignLocn from q3Cond12, q3Cond3 where q3Cond12.ACCT_INTRL_ID = q3Cond3.ACCT_INTRL_ID) ",

		  "create query q3  as select * from viewq3 ",
		  " alter query q3 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ3.txt</Address></EndPointReference>\"",
		  "alter query q3 start ",

		 "create query q3Txns as IStream(select TxnId, ValidLoopCashForeignTxn.ACCT_INTRL_ID, TRXN_BASE_AM, ADDR_CNTRY_CD, TRXN_LOC_ADDR_SEQ_ID from viewq3[NOW], ValidLoopCashForeignTxn where viewq3.ACCT_INTRL_ID = ValidLoopCashForeignTxn.ACCT_INTRL_ID)",
		  "alter query q3Txns add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMantasQ3Txns.txt</Address></EndPointReference>\"",
		  " alter query q3Txns start "
	  };
	  
	  public void testStatistics()
	  {
	     SchedRunner run = new SchedRunner(m_driver);
	     Thread t = new Thread(run, "sched");
	     t.start();
	     System.out.println("started new thread");
	     try{
	     Thread.currentThread().sleep(10);
	       t.join();
	     }catch(Exception e)
	     {
	    	 
	     }
	     System.out.println("READER QUEUE STATS");
	     ReaderQueueStatsIterator r_itr = new ReaderQueueStatsIterator(m_execContext);
	     if(r_itr != null)
	     {
	       r_itr.setStatsRowFactory(m_factory);
	       r_itr.init();
	       try{
	         IStats stat = r_itr.getNext();
	         while(stat != null)
	           stat = r_itr.getNext();
	       }
	       catch(Exception e)
	       {
	       
	       }
	       r_itr.close();
	     }
	     
	     System.out.println("WRITER QUEUE STATS");
	     WriterQueueStatsIterator w_itr = new WriterQueueStatsIterator(m_execContext);
	     if(w_itr != null)
	     {
	       w_itr.setStatsRowFactory(m_factory);
	       w_itr.init();
	       try{
	         IStats stat = w_itr.getNext();
	         while(stat != null)
	           stat = w_itr.getNext();
	       }
	       catch(Exception e)
	       {
	       
	       }
	       w_itr.close();
	     }
	     
		 System.out.println("STORE STATS ");
		  StoreStatsIterator store_itr = new StoreStatsIterator(m_execContext);
		  if(store_itr != null)
		  {
            store_itr.setStatsRowFactory(m_factory);
            store_itr.init();
            try{
            IStats stat = store_itr.getNext();
            while(stat != null)
              stat = store_itr.getNext();
            }catch(Exception e)
            {
           
            }
            store_itr.close();
		  }
		  
	  }
	  
	  public static class SchedRunner implements Runnable{
	    InterpDrv driver;
	    public SchedRunner(InterpDrv m_driver)
	    {
	      driver = m_driver;
	    }

		@Override
		public void run() {
	     try{
	       System.out.println("starting sched");
	       driver.setCqlX("alter system run");
	     }catch(Exception e)
	     {
	    	 
	     }
		}
	  }
	  
	  public void main(String[] args)
	  {
	      junit.textui.TestRunner.run(TestStat.class);
	  }
}
