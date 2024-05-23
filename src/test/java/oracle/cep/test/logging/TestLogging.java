/* $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestLogging.java /main/17 2009/05/12 19:25:47 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    05/08/09 - lifecycle mgmt
 hopark      12/02/08 - move LogLevelManaer to ExecContext
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    09/24/08 - multiple schema
 hopark      06/18/08 - logging refactor
 hopark      05/12/08 - user singleton LogLevelManager
 hopark      03/26/08 - server reorg
 hopark      02/07/08 - fix operator area dif
 hopark      12/27/07 - support xmllog
 hopark      12/19/07 - fix drop ddl
 hopark      11/02/07 - remove semicolon
 hopark      06/28/07 - support plan change
 parujain    06/26/07 - fix activate
 hopark      05/29/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/logging/TestLogging.java /main/17 2009/05/12 19:25:47 parujain Exp $
 *  @author  hopark
 *  @since   release specific (what release of product did this appear in)
 */
package

oracle.cep.test.logging;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogLevelManager;
import oracle.cep.logging.ILoggable;
import oracle.cep.logging.Levels;
import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.PlanMonitor;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryManager;
import oracle.cep.phyplan.PhyIndex;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhySharedQueueReader;
import oracle.cep.phyplan.PhySharedQueueWriter;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhyStoreKind;
import oracle.cep.phyplan.PhySynopsis;
import oracle.cep.phyplan.SynopsisKind;
import oracle.cep.planmgr.IPlanVisitor;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.test.InterpDrv;
import oracle.cep.util.ArrayUtil;
import oracle.cep.util.CSVUtil;
import oracle.cep.util.EnumUtil;
import oracle.cep.util.StringUtil;
import oracle.cep.common.Constants;

public class TestLogging extends TestCase
  implements PlanMonitor.ILogLevelChgNotifier
{
  enum PlanChgType {
    NONE,
    ADD_QUERY,
    DROP_QUERY
  };
  
  static InterpDrv        s_driver       = null;
  static ExecContext      s_execContext  = null;
  
  static boolean          s_initalized   = false;

  static List<PhyWrapper> s_phyObjs[];
  static int              s_optIds[];
  
  LogLevelManager         m_lm;

  Random                  m_random       = null;
  List<LogLevelChg>       m_logLevelChgs = null;
  boolean                 m_bLogChg = false;
  int                     m_instCount = 0;
  static final String s_S0_Range_1000 = "0";
  static final String s_S1_Range_1000 = "1";
  static final String s_queries[] =
  {
    "as select * from XXS0 [range 1000]",
    "as select * from XXS1 [range 1000]",
  };
  static final String s_baseQueries[] =
  {
    "register stream TradeInputs(tradeId integer, tradeVolume integer, tradeSymbol char(4), tradeType integer)",
    "alter stream TradeInputs add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpTIDataSize1000Rate1.txt</Address> </EndPointReference>\"",
    "register stream TradeUpdates(tradeId integer, statusCode integer)",
    "alter stream TradeUpdates add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpTUDataSize1000Rate1.txt</Address> </EndPointReference>\"",
    "register stream TradeMatched(tradeId integer)",
    "alter stream TradeMatched add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpTMDataSize1000Rate1.txt</Address> </EndPointReference>\"",
    "register relation TradeTypeCodes(tradeType char(10), code integer)",
    "alter stream TradeTypeCodes add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpTT1.txt</Address> </EndPointReference>\"",
    "register relation TradeStatusCodes(statusCode char(20), code integer)",
    "alter stream TradeStatusCodes add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpTS1.txt</Address> </EndPointReference>\"",
    "register relation Dual(c1 integer)",
    "alter stream Dual add source \"<EndPointReference> <Address>file://@TEST_DATA@/inpDual.txt</Address> </EndPointReference>\"",
    "create function seqNo(n int) return int as language java name \"TkUsrSeqNo\"",
    "register view CutOffTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as DStream(select * from TradeInputs[range 20 seconds])",
    "register view FineTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(select c.tradeId, c.tradeVolume, c.tradeSymbol, c.tradeType from CutOffTrades[now] as c, TradeMatched[range 20 seconds] as t where c.tradeId = t.tradeId)",
    "register view Dummy1(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from CutOffTrades[NOW]",
    "register view Dummy2(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from FineTrades[NOW]",
    "register view FailedTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(Dummy1 except Dummy2)",
    "register view TotalFineTrades(c1) as select count(*) from FineTrades[range 5 minutes slide 5 minutes]",
    "register view TotalFailedByNonZeroType(tradeType, total) as select tradeType, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeType",
    "register view Dummy6(tradeType) as select tradeType from TotalFailedByNonZeroType",
    "register view Dummy7(tradeType) as select code from TradeTypeCodes",
    "register view Dummy8(tradeType) as Dummy7 except Dummy6",
    "register view Dummy9(tradeType, total) as select tradeType, 0 from Dummy8",
    "register view TotalFailedByType(tradeType, total) as TotalFailedByNonZeroType union all Dummy9",
    "register view TotalFailedTrades(total, dummy) as select sum(total), count(*) from TotalFailedByType",
    "register view TotalTrades(c1) as select fine.c1+failed.total from TotalFineTrades as fine, TotalFailedTrades as failed",
    "register view Dummy4(buyCount, sellCount) as select a.total, b.total from TotalFailedByType as a, TotalFailedByType as b where a.tradeType = 0 and b.tradeType = 1",
    "register view TotalFailuresByTypes(buyCount, sellCount, exchgCount) as select b.buyCount, b.sellCount, a.total from TotalFailedByType as a, Dummy4 as b where a.tradeType = 2",
    "register view FailedTradesStatus(tradeId, statusCode) as IStream(select f.tradeId, u.statusCode from FailedTrades[NOW] as f, TradeUpdates[partition by u.tradeId rows 1 range 20 seconds] as u where f.tradeId = u.tradeId)",
    "register view TotalFailedNonZeroByStatus12(statusCode, total) as select statusCode, count(*) from FailedTradesStatus[range 5 minutes slide 5 minutes] group by statusCode",
    "register view Dummy10(statusCode) as select statusCode from TotalFailedNonZeroByStatus12",
    "register view Dummy11(statusCode) as select code from TradeStatusCodes where code = 1",
    "register view Dummy12(statusCode) as select code from TradeStatusCodes where code = 2",
    "register view Dummy13(statusCode) as Dummy11 union all Dummy12",
    "register view Dummy14(statusCode) as Dummy13 except Dummy10",
    "register view Dummy15(statusCode, total) as select statusCode, 0 from Dummy14",
    "register view TotalFailedStatus12(statusCode, total) as TotalFailedNonZeroByStatus12 union all Dummy15",
    "register view Dummy18(total, dummy) as select sum(total), count(*) from TotalFailedStatus12",
    "register view TotalFailedStatus0(statusCode, total) as select 0, allRec.total - status12.total from TotalFailedTrades as allRec, Dummy18 as status12",
    "register view TotalFailedByStatus(statusCode, total) as TotalFailedStatus0 union all TotalFailedStatus12",
    "register view Dummy5(status0, count0, status1, count1) as select a.statusCode, a.total, b.statusCode, b.total from TotalFailedByStatus as a, TotalFailedByStatus as b where a.statusCode = 0 and b.statusCode = 1",
    "register view TotalFailuresByStatus(status0, count0, status1, count1, status2, count2) as select b.status0, b.count0, b.status1, b.count1, a.statusCode, a.total from TotalFailedByStatus as a, Dummy5 as b where a.statusCode = 2",
    "register view TotalFailuresByStatusType(buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select a.buyCount, a.sellCount, a.exchgCount, b.count0, b.count1, b.count2 from TotalFailuresByTypes as a, TotalFailuresByStatus as b",
    "register view TotalReq1(totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select t.c1, a.buyCount+a.sellCount+a.exchgCount, a.buyCount, a.sellCount, a.exchgCount, a.noackCount, a.recvdCount, a.processedCount from TotalFailuresByStatusType as a, TotalTrades as t",
    "register view TradeStatus0(tradeId, statusCode) as select tradeId, 0 from TradeInputs",
    "register view TradeStatus3(tradeId, statusCode) as select tradeId, 3 from TradeMatched",
    "register view TradeStatus03(tradeId, statusCode) as TradeStatus0 union all TradeStatus3",
    "register view TradeStatusStreamDup(tradeId, statusCode) as TradeStatus03 union all TradeUpdates",
    "register view TradeStatus(tradeId, statusCode) as select * from TradeStatusStreamDup[partition by tradeId rows 1 range 4 seconds]",
    "register view TradeStatusStream(tradeId, statusCode) as IStream(select * from TradeStatus)",
    "<![CDATA[ register view CutOff3(tradeId, statusCode) as DStream(select tradeId, statusCode from TradeStatusStream[range 3 seconds] where statusCode < 3)]]>",
    "register view Failed3(tradeId, statusCode) as IStream(select a.tradeId, a.statusCode from CutOff3[NOW] as a, TradeStatus as b where a.tradeId = b.tradeId and a.statusCode = b.statusCode)",
    "register view TotalCutOff3(c1) as select count(*) from CutOff3[range 10 minutes slide 5 minutes]",
    "register view TotalFailed3(c1) as select count(*) from Failed3[range 10 minutes slide 5 minutes]",
    "register view Req1(total, totalFailed, buy, sell, exchg, trade_noack, trade_recvd, trade_processing, dummy) as IStream(select totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount, seqNo(0) from TotalReq1)",
    "register view Req2(tickerSymbol, symbolCount, dummy) as IStream(select tradeSymbol, count(*), seqNo(0) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeSymbol)",
    "<![CDATA[ register view Req3(totalCount, failedCount, dummy) as IStream(select t.c1, t.c1-f.c1, seqNo(0) from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1)]]>",
    "register view Req4(totalCount, failedCount, dummy) as IStream(select t.c1, f.c1, seqNo(0) from TotalCutOff3 as t, TotalFailed3 as f where f.c1*100 > 10*t.c1)",
    "create query XXq0 as select total as totalCount, totalFailed as failedCount, buy as buy, sell as sell, exchg as exchg, trade_noack as trade_noack, trade_recvd as trade_recvd, trade_processing as trade_processing from Req1",
    "alter query XXq0 add destination \"<EndPointReference> <Address>file://@TEST_OUTPUT@/outcsfbPR1S1000R1.txt</Address> </EndPointReference>\"",
    "alter query XXq0 start",
    "create query XXq1 as select tickerSymbol as tickerSymbol, symbolCount as symbolCount from Req2",
    "alter query XXq1 add destination \"<EndPointReference> <Address>file://@TEST_OUTPUT@/outcsfbPR2S1000R1.txt</Address> </EndPointReference>\"",
    "alter query XXq1 start",
    "create query XXq2 as select totalCount as totalCount, failedCount as failedCount from Req3",
    "alter query XXq2 add destination \"<EndPointReference> <Address>file://@TEST_OUTPUT@/outcsfbPR3S1000R1.txt</Address> </EndPointReference>\"",
    "alter query XXq2 start",
    "create query XXq3 as select totalCount as totalCount, failedCount as failedCount from Req4",
    "alter query XXq3 add destination \"<EndPointReference> <Address>file://@TEST_OUTPUT@/outcsfbPR4S1000R1.txt</Address> </EndPointReference>\"",
    "alter query XXq3 start",
    "register stream XXS0 (c1 integer, c2 float)",
    "register stream XXS1 (c1 integer, c2 float)",
    "register stream XXScolt (c1 bigint, c2 bigint)",
    "register stream XXS2 (c1 integer, c2 float)",
    "register stream XXS3 (c1 integer, c2 float)",
    "register stream XXS4 (c1 integer, c2 float)",
    "register stream XXS5 (c1 integer, c2 float)",
    "register stream XXS6 (c1 integer, c2 float)",
    "register stream XXS7 (c1 integer, c2 float)",
    "register stream XXS8 (c1 integer, c2 float)",
    "register stream XXS9 (c1 integer, c2 float)",
    "create stream XXS10(c1 integer, c2 char(10))",
    "create stream XXS11(c1 integer, name char(10))",
    "create stream XXS12(c1 integer, c2 float)",
    "register relation XXR1 (d1 integer, d2 float)",
    "register relation XXR6 (d1 integer, d2 timestamp)",
    "create stream XXS15(c1 integer, c2 timestamp)",
    "create stream XXS13(c1 integer, c2 timestamp)",
    "register stream XXS14 (c1 integer, c2 float)",
    "create relation XXR5454682(d1 integer, d2 char(10))",
    "create stream XXSP1(c1 integer, name char(10))",
    "register relation XXRP1(d1 integer, rname char(10))",
    "register stream XXS17(c1 integer, c2 float, c3 timestamp, c4 interval)",
    "create stream XXSCr1(c1 integer, c2 float)",
    "register stream XXS18(c1 integer, c2 float)",
    "create stream XXSTs1(c1 integer, c2 char(20))",
    "create stream XXSTs2(c1 integer, c2 char(20))",
    "create stream XXSNVL(c1 char(20), c2 integer)",
    "create stream XXSTs3(c1 float, c2 integer)",
    "create stream XXSTs4(c1 timestamp, c2 integer)",
    "create stream XXSN1(c1 char(20), c2 integer)",
    "create stream XXSN2(c1 timestamp, c2 integer)",
    "create stream XXSLk1(first1 char(20), last1 char(20))",
    "create stream XXSIn1(c1 integer, c2 interval)",
    "register relation XXRIn1(c1 integer, c2 interval)",
    "create stream XXSTs(c1 timestamp, c2 timestamp)",
    "create stream XXSByt(c1 integer, c2 byte(2))",
    "create window range_slide(winrange int, winslide int) implement using \"oracle.cep.extensibility.windows.builtin.RangeSlide\"",
    "create function fib(n int) return int as language java name \"TkUsrFib\"",
    "create function logfact(n int) return float as language java name \"TkUsrLogFactorial\"",
    "create function binom(n bigint,k bigint) return float as language java name \"TkUsrBinomial\"",
    "create function fib(n float) return int as language java name \"TkUsrFib2\"",
    "create function concat2(c1 char, c2 char) return char as language java name \"TkUsrConcat\"",
    "create function substring(c1 char, first1 int, last1 int) return char as language java name \"TkUsrSubstring\"",
    "create function secondMax(c1 int) return int aggregate using \"TkUsrSecondMax\"",
    "create function var(c1 int) return float aggregate using \"TkUsrVariance\" supports incremental computation",
    "create function bytTest(c1 byte) return byte as language java name \"TkUsrBytManip\"",
    "alter stream XXS0 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS0.txt</Address></EndPointReference>\"",
    "alter stream XXS1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS1.txt</Address></EndPointReference>\"",
    "alter stream XXScolt add source \"<EndPointReference><Address>file://@TEST_DATA@/inpScolt.txt</Address></EndPointReference>\"",
    "alter stream XXS2 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS2.txt</Address></EndPointReference>\"",
    "alter stream XXS3 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS3.txt</Address></EndPointReference>\"",
    "alter stream XXS4 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS4.txt</Address></EndPointReference>\"",
    "alter stream XXS5 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS5.txt</Address></EndPointReference>\"",
    "alter stream XXS6 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS6.txt</Address></EndPointReference>\"",
    "alter stream XXS7 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS7.txt</Address></EndPointReference>\"",
    "alter stream XXS8 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS8.txt</Address></EndPointReference>\"",
    "alter stream XXS9 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS9.txt</Address></EndPointReference>\"",
    "alter stream XXS10 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS10.txt</Address></EndPointReference>\"",
    "alter stream XXS11 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS11.txt</Address></EndPointReference>\"",
    "alter stream XXS12 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS12.txt</Address></EndPointReference>\"",
    "alter relation XXR1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpR1.txt</Address></EndPointReference>\"",
    "alter relation XXR6 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpR6.txt</Address></EndPointReference>\"",
    "alter stream XXS15 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS15.txt</Address></EndPointReference>\"",
    "alter stream XXS13 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS13.txt</Address></EndPointReference>\"",
    "alter stream XXS14 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS14.txt</Address></EndPointReference>\"",
    "alter relation XXR5454682 add source \"<EndPointReference><Address>file://@TEST_DATA@/inp5454682.txt</Address></EndPointReference>\"",
    "alter stream XXSP1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSP1.txt</Address></EndPointReference>\"",
    "alter relation XXRP1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpRP1.txt</Address></EndPointReference>\"",
    "alter stream XXS17 add source push",
    "alter stream XXSCr1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpCr1.txt</Address></EndPointReference>\"",
    "alter stream XXS18 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpS18.txt</Address></EndPointReference>\"",
    "alter stream XXSTs1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSTs1.txt</Address></EndPointReference>\"",
    "alter stream XXSTs2 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSTs2.txt</Address></EndPointReference>\"",
    "alter stream XXSNVL add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSNVL.txt</Address></EndPointReference>\"",
    "alter stream XXSTs3 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSTs3.txt</Address></EndPointReference>\"",
    "alter stream XXSTs4 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSTs4.txt</Address></EndPointReference>\"",
    "alter stream XXSN1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSN1.txt</Address></EndPointReference>\"",
    "alter stream XXSN2 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSN2.txt</Address></EndPointReference>\"",
    "alter stream XXSLk1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSLk1.txt</Address></EndPointReference>\"",
    "alter stream XXSIn1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSIn1.txt</Address></EndPointReference>\"",
    "alter relation XXRIn1 add source \"<EndPointReference><Address>file://@TEST_DATA@/inpRIn1.txt</Address></EndPointReference>\"",
    "alter stream XXSTs add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSTs.txt</Address></EndPointReference>\"",
    "alter stream XXSByt add source \"<EndPointReference><Address>file://@TEST_DATA@/inpSByt.txt</Address></EndPointReference>\"",
    "register view XXv0 (c1 integer, c2 float) as select * from XXS0",
    "register view XXv1 (c1 integer, c2 float) as select * from XXS1",
    "register view XXv2 (c1, c2) as select * from XXS1",
    "register view XXv3 (c1, s2) as select * from XXS1",
    "register view XXv5 (c1, c2) as XXS3 union all XXS4",
    "register view XXv6 (c1 integer, c2 float) as select  *  from   XXS0 [range 1]",
    "register view XXv7 (c1 integer, c2 timestamp) as select * from XXS15",
    "register view XXv8 (c1 integer, c2 float) as select * from XXS0[range 2]",
    "register view XXv9 as select * from XXS15",
    "register view XXv10 as select * from XXS0[rows 500]",
    "register view XXv11 as select * from XXS1[rows 500]",
    "register view XXv12 as select * from XXS2[rows 500]",
    "create query XXq201 as select * from XXS0 [range 1]",
    "create query XXq202 as select * from XXv0",
    "create query XXq204 as select * from XXS2 [rows 5] where XXS2.c1 > 10",
    "create query XXq205 as select * from XXS0 [range 1] as a, XXS0 [range 1] as b where a.c1 = b.c1",
    "create query XXq206 as XXS1 union all XXS2",
    "create query XXq207 as select * from XXS1 [range 5 minutes]",
    "create query XXq208 as select * from XXv5",
    "create query XXq209 as select * from XXS2 [range 5 minutes] where XXS2.c1 > 10",
    "create query XXq210 as select * from XXS3 [range 5 minutes] where XXS3.c1 > 10 and XXS3.c2 > 20.0",
    "create query XXq212 as select * from XXS5 [range 2 minutes], XXS6 [range 2 minutes]",
    "create query XXq213 as IStream(select (c1+10)*2, c2+1.5 from XXS7[range unbounded] where c1 > 10)",
    "create query XXq214 as DStream(select (c1+10)*2, c2+1.5 from XXS8[range unbounded] where c1 > 10)",
    "create query XXq215 as IStream(select * from XXS5 [range 2 minutes], XXS6 [range 2 minutes])",
    "create query XXq216 as DStream(select * from XXS5 [range 2 minutes], XXS6 [range 2 minutes])",
    "create query XXq217 as select * from XXS2 [range 1000], XXS3 [range 1000] where XXS2.c1 = XXS3.c1",
    "create query XXq218 as select * from XXS2 [range 1000], XXS3 [range 1000] where XXS2.c1 > XXS3.c1",
    "create query XXq219 as select * from XXv1",
    "create query XXq220 as select * from XXR1",
    "create query XXq222 as select max(c1) as maxc1, avg(c1) as avgc1 from XXS9[range 10]",
    "create query XXq223 as select c2, min(c1), avg(c1) from XXS9[range 10] group by c2",
    "create query XXq224 as select * from XXS10 where c2 = \"abc\"",
    "create query XXq225 as select c1 as val, fib(c1) as fibc1, fib(fib(fib(c1)+1)+1) as nestedfunc, concat2(c2, c2) as stringfunc, concat2(concat2(c2, c2), concat2(c2, c2)) as nestedstrfunc, substring(concat2(concat2(c2, c2),concat2(c2, c2)), c1+2, c1+3) as difftypesfunc from XXS10 where concat2(c2, \"abc\") = \"abcabc\"",
    "create query XXq227 as select max(c1), secondMax(c1) from XXS9[range 10]",
    "create query XXq228 as select max(c1), secondMax(c1), var(c1) from XXS9[range 10]",
    "create query XXq229 as select c2, max(c1), secondMax(c1) from XXS9[range 10] group by c2",
    "create query XXq230 as select var(c1) from XXS9[range 10]",
    "create query XXq231 as select * from XXS11[range 1]",
    "create query XXq232 as select * from XXv6",
    "create query XXq233 as select * from XXS12 [range 10 slide 5]",
    "create query XXq234 as select * from XXS12 [range 10 slide 10]",
    "create query XXq236 as select * from XXR6",
    "create query XXq237 as select * from XXS13[range 1] as a, XXS14[range 1] as b where a.c1 = b.c1",
    "create query XXq238 as select * from XXS11[range 1000], XXS15[range 1000] where XXS11.c1 > XXS15.c1",
    "create query XXq239 as select * from XXS15 where XXS15.c1 > 2",
    "create query XXq240 as select * from XXS15 where XXS15.c2 = to_timestamp(\"08/07/2006 11:13:48\")",
    "create query XXq241 as select XXS15.c2 from XXS15 where XXS15.c2 > to_timestamp(\"08/07/2006 11:13:48\")",
    "create query XXq242 as select * from XXS13[range 1000], XXS15[range 1000] where XXS13.c2 > XXS15.c2",
    "create query XXq243 as select max(c2) from XXS15[range 5] group by c1",
    "create query XXq244 as select * from XXR5454682",
    "create query XXq245 as XXv10 except XXv11",
    "create query XXq246 as XXv10 except XXv12",
    "create query XXq247 as XXv6 except XXR1",
    "create query XXq248 as XXR1 except XXv6",
    "create query XXq249 as XXR1 union all XXv6",
    "create query XXq250 as XXv6 union all XXv8",
    "create query XXq251 as select * from XXv7",
    "create query XXq252 as select * from XXSP1 [partition by c1 rows 1]",
    "create query XXq253 as select * from XXSP1 [partition by c1 rows 2]",
    "create query XXq254 as select c1, name, rname from XXSP1 [partition by c1 rows 1], XXRP1 where XXSP1.c1 = XXRP1.d1",
    "create query XXq255 as select c1, name, rname from XXSP1 [partition by c1 rows 2], XXRP1 where XXSP1.c1 = XXRP1.d1",
    "create query XXq256 as select * from XXS11[range 1] as a, XXS15[range 1] as b where a.c1 = b.c1",
    "create query XXq257 as select * from XXS0 where XXS0.c1 != 20",
    "create query XXq258 as select * from XXS0[rows 100], XXS1[rows 100] where XXS0.c2 != XXS1.c2",
    "create query XXq259 as select * from XXSP1[rows 100], XXS11[rows 100] where XXSP1.name != XXS11.name",
    "create query XXq260 as select * from XXv9",
    "create query XXq261 as select * from XXS17",
    "create query XXq262 as select length(c2) from XXS10",
    "create query XXq263 as select length(c2 || c2) + 1 from XXS10 where length(c2) = 2",
    "create query XXq264 as select c2 || \"xyz\" from XXS10",
    "create query XXq265 as select c2 || c2 from XXS10",
    "create query XXq266 as select (sum(c1) + max(c1)) *2 from XXS1[range 10]",
    "create query XXq267 as select c2 + avg(c1) from XXS1[range 10] group by c2",
    "create query XXq268 as select sum(c1+4) from XXS1[range 10]",
    "create query XXq269 as select max(c1-(c1*2)) from XXS1[range 10]",
    "create query XXq270 as select secondMax(2*c1) from XXS1[range 10]",
    "create query XXq271 as select sum(c1), min(c1+1), secondMax(c1*2) from XXS2[range 10]",
    "create query XXq272 as select sum(c1+4),max(c1*2) from XXS2[range 10] group by c2",
    "create query XXq273 as select concat(concat(c2, c2), c2) from XXS10",
    "create query XXq274 as select c1, fib(c1), c2, fib(c2) from XXS18",
    "create query XXq275 as select to_timestamp(c2) from XXSTs1",
    "create query XXq276 as select to_timestamp(c2, \"yyMMddHHmmss\") from XXSTs2",
    "create query XXq277 as select * from XXSTs2 where to_timestamp(c2,\"yyMMddHHmmss\") = to_timestamp(\"09/07/2005 10:13:48\")",
    "create query XXq279 as select nvl(c1,5) from XXS11",
    "create query XXq280 as select * from XXS11 where nvl(c1,5) > 3",
    "create query XXq281 as select nvl(c1,\"abcd\") from XXSNVL",
    "create query XXq282 as select nvl(to_float(c1), 5.2) from XXS11",
    "create query XXq283 as select nvl(c1,5) from XXSTs3",
    "create query XXq284 as select nvl(c1,to_timestamp(\"09/07/2005 10:13:48\")) from XXSTs4",
    "create query XXq285 as select XXSTs4.c2 from XXSTs4 where nvl(XXSTs4.c1,to_timestamp(\"09/07/2005 10:13:48\")) = to_timestamp(\"09/07/2005 10:13:48\")",
    "create query XXq286 as select * from XXS15 where c1 is null",
    "create query XXq287 as select * from XXSN1 where c1 is null",
    "create query XXq288 as select * from XXSN2 where c1 is null",
    "create query XXq289 as select * from XXS10 where XXS10.c2 like \"ab\"",
    "create query XXq290 as select * from XXS10 where concat(concat(c2, c2), c2) like \"(abc)+\"",
    "create query XXq291 as select * from XXSLk1 where first1 like \"^Ste(v|ph)en$\"",
    "create query XXq292 as select * from XXS1[range 1] as a, XXS1[range 1] as b, XXS1[range 1] as c where a.c1 = b.c1 and b.c1 = c.c1",
    "create query XXq293 as select * from XXSIn1 where c2 = INTERVAL \"6 1:03:45:100\" DAY TO SECOND",
    "create query XXq294 as select (c2 + INTERVAL \"2 1:03:45:10\" DAY TO SECOND) from XXSIn1",
    "create query XXq295 as select * from XXSIn1 where (c2 + INTERVAL \"2 1:03:45:10\" DAY TO SECOND) > INTERVAL \"6 12:23:45:10\" DAY TO SECOND",
    "create query XXq296 as select * from XXRIn1",
    "create query XXq297 as select XXS15.c2 - XXSIn1.c2 from XXS15[rows 500], XXSIn1[rows 500]",
    "create query XXq298 as select XXS15.c2 + XXSIn1.c2 from XXS15[rows 500], XXSIn1[rows 500]",
    "create query XXq299 as select XXSIn1.c2 + XXS15.c2 from XXS15[rows 500], XXSIn1[rows 500]",
    "create query XXq300 as select XXSTs.c1 - XXSTs.c2 from XXSTs",
    "create query XXq301 as select * from XXS12 [range_slide(10,5)]",
    "create query XXq302 as select bytTest(c2) from XXSByt[range 2]",
    "create query XXq303 as select logfact(c1) from XXS18",
    "create query XXq304 as select binom(c1, c2) from XXScolt",
    "alter query XXq201 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS0.txt</Address></EndPointReference>\"",
    "alter query XXq202 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSv0.txt</Address></EndPointReference>\"",
    "alter query XXq204 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSrow1.txt</Address></EndPointReference>\"",
    "alter query XXq205 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS0J1.txt</Address></EndPointReference>\"",
    "alter query XXq206 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outU1.txt</Address></EndPointReference>\"",
    "alter query XXq207 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS1.txt</Address></EndPointReference>\"",
    "alter query XXq208 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outU2.txt</Address></EndPointReference>\"",
    "alter query XXq209 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS2.txt</Address></EndPointReference>\"",
    "alter query XXq210 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS3.txt</Address></EndPointReference>\"",
    "alter query XXq212 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS5.txt</Address></EndPointReference>\"",
    "alter query XXq213 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSI1.txt</Address></EndPointReference>\"",
    "alter query XXq214 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSD1.txt</Address></EndPointReference>\"",
    "alter query XXq215 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSI2.txt</Address></EndPointReference>\"",
    "alter query XXq216 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSD2.txt</Address></EndPointReference>\"",
    "alter query XXq217 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSJ1.txt</Address></EndPointReference>\"",
    "alter query XXq218 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSJ2.txt</Address></EndPointReference>\"",
    "alter query XXq219 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSV1.txt</Address></EndPointReference>\"",
    "alter query XXq220 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSR1.txt</Address></EndPointReference>\"",
    "alter query XXq222 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outGA1.txt</Address></EndPointReference>\"",
    "alter query XXq223 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outGA2.txt</Address></EndPointReference>\"",
    "alter query XXq224 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS10.txt</Address></EndPointReference>\"",
    "alter query XXq225 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outFn1.txt</Address></EndPointReference>\"",
    "alter query XXq227 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outUDA1.txt</Address></EndPointReference>\"",
    "alter query XXq228 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outUDA2.txt</Address></EndPointReference>\"",
    "alter query XXq229 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outUDA3.txt</Address></EndPointReference>\"",
    "alter query XXq230 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outUDA4.txt</Address></EndPointReference>\"",
    "alter query XXq231 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS11.txt</Address></EndPointReference>\"",
    "alter query XXq232 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSV6.txt</Address></EndPointReference>\"",
    "alter query XXq233 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS12great.txt</Address></EndPointReference>\"",
    "alter query XXq234 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS12equal.txt</Address></EndPointReference>\"",
    "alter query XXq236 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outR6.txt</Address></EndPointReference>\"",
    "alter query XXq237 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS13.txt</Address></EndPointReference>\"",
    "alter query XXq238 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSJ12.txt</Address></EndPointReference>\"",
    "alter query XXq239 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSC12.txt</Address></EndPointReference>\"",
    "alter query XXq240 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS15.txt</Address></EndPointReference>\"",
    "alter query XXq241 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSG15.txt</Address></EndPointReference>\"",
    "alter query XXq242 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSJ15.txt</Address></EndPointReference>\"",
    "alter query XXq243 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSM15.txt</Address></EndPointReference>\"",
    "alter query XXq244 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/out5454682.txt</Address></EndPointReference>\"",
    "alter query XXq245 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outExc1.txt</Address></EndPointReference>\"",
    "alter query XXq246 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outExc2.txt</Address></EndPointReference>\"",
    "alter query XXq247 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outExc3.txt</Address></EndPointReference>\"",
    "alter query XXq248 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outExc4.txt</Address></EndPointReference>\"",
    "alter query XXq249 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outU3.txt</Address></EndPointReference>\"",
    "alter query XXq250 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outU4.txt</Address></EndPointReference>\"",
    "alter query XXq251 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSV15.txt</Address></EndPointReference>\"",
    "alter query XXq252 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSP11r.txt</Address></EndPointReference>\"",
    "alter query XXq253 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSP12r.txt</Address></EndPointReference>\"",
    "alter query XXq254 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSPRJ11r.txt</Address></EndPointReference>\"",
    "alter query XXq255 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSPRJ12r.txt</Address></EndPointReference>\"",
    "alter query XXq256 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSB15.txt</Address></EndPointReference>\"",
    "alter query XXq257 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outINE.txt</Address></EndPointReference>\"",
    "alter query XXq258 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outFNE.txt</Address></EndPointReference>\"",
    "alter query XXq259 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outCNE.txt</Address></EndPointReference>\"",
    "alter query XXq260 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outV9.txt</Address></EndPointReference>\"",
    "alter query XXq261 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outPush1.txt</Address></EndPointReference>\"",
    "alter query XXq262 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLength1.txt</Address></EndPointReference>\"",
    "alter query XXq263 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLength2.txt</Address></EndPointReference>\"",
    "alter query XXq264 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outCon1.txt</Address></EndPointReference>\"",
    "alter query XXq265 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outCon2.txt</Address></EndPointReference>\"",
    "alter query XXq266 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr1.txt</Address></EndPointReference>\"",
    "alter query XXq267 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr2.txt</Address></EndPointReference>\"",
    "alter query XXq268 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr3.txt</Address></EndPointReference>\"",
    "alter query XXq269 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr4.txt</Address></EndPointReference>\"",
    "alter query XXq270 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr5.txt</Address></EndPointReference>\"",
    "alter query XXq271 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr6.txt</Address></EndPointReference>\"",
    "alter query XXq272 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outAggrExpr7.txt</Address></EndPointReference>\"",
    "alter query XXq273 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outCon3.txt</Address></EndPointReference>\"",
    "alter query XXq274 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS18.txt</Address></EndPointReference>\"",
    "alter query XXq275 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSTs1.txt</Address></EndPointReference>\"",
    "alter query XXq276 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSTs2.txt</Address></EndPointReference>\"",
    "alter query XXq277 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSTs21.txt</Address></EndPointReference>\"",
    "alter query XXq279 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvl1.txt</Address></EndPointReference>\"",
    "alter query XXq280 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvlGT.txt</Address></EndPointReference>\"",
    "alter query XXq281 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvlCH.txt</Address></EndPointReference>\"",
    "alter query XXq282 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvlFL.txt</Address></EndPointReference>\"",
    "alter query XXq283 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvlIF.txt</Address></EndPointReference>\"",
    "alter query XXq284 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvlTS.txt</Address></EndPointReference>\"",
    "alter query XXq285 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnvl2.txt</Address></EndPointReference>\"",
    "alter query XXq286 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnull.txt</Address></EndPointReference>\"",
    "alter query XXq287 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnullC.txt</Address></EndPointReference>\"",
    "alter query XXq288 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outnullT.txt</Address></EndPointReference>\"",
    "alter query XXq289 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLike.txt</Address></EndPointReference>\"",
    "alter query XXq290 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLike1.txt</Address></EndPointReference>\"",
    "alter query XXq291 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLike2.txt</Address></EndPointReference>\"",
    "alter query XXq292 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outMJ1.txt</Address></EndPointReference>\"",
    "alter query XXq293 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outSIn1.txt</Address></EndPointReference>\"",
    "alter query XXq294 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outInAr.txt</Address></EndPointReference>\"",
    "alter query XXq295 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outInGT.txt</Address></EndPointReference>\"",
    "alter query XXq296 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outInR.txt</Address></EndPointReference>\"",
    "alter query XXq297 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outTIS.txt</Address></EndPointReference>\"",
    "alter query XXq298 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outTIA.txt</Address></EndPointReference>\"",
    "alter query XXq299 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outITA.txt</Address></EndPointReference>\"",
    "alter query XXq300 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outTTS.txt</Address></EndPointReference>\"",
    "alter query XXq301 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outS12great_rs.txt</Address></EndPointReference>\"",
    "alter query XXq302 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outByt.txt</Address></EndPointReference>\"",
    "alter query XXq303 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outLogFact.txt</Address></EndPointReference>\"",
    "alter query XXq304 add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/outBinom.txt</Address></EndPointReference>\"",
    "alter query XXq201 start",
    "alter query XXq202 start",
    "alter query XXq204 start",
    "alter query XXq205 start",
    "alter query XXq206 start",
    "alter query XXq207 start",
    "alter query XXq208 start",
    "alter query XXq209 start",
    "alter query XXq210 start",
    "alter query XXq212 start",
    "alter query XXq213 start",
    "alter query XXq214 start",
    "alter query XXq215 start",
    "alter query XXq216 start",
    "alter query XXq217 start",
    "alter query XXq218 start",
    "alter query XXq219 start",
    "alter query XXq220 start",
    "alter query XXq222 start",
    "alter query XXq223 start",
    "alter query XXq224 start",
    "alter query XXq225 start",
    "alter query XXq227 start",
    "alter query XXq228 start",
    "alter query XXq229 start",
    "alter query XXq230 start",
    "alter query XXq231 start",
    "alter query XXq232 start",
    "alter query XXq233 start",
    "alter query XXq234 start",
    "alter query XXq236 start",
    "alter query XXq237 start",
    "alter query XXq238 start",
    "alter query XXq239 start",
    "alter query XXq240 start",
    "alter query XXq241 start",
    "alter query XXq242 start",
    "alter query XXq243 start",
    "alter query XXq244 start",
    "alter query XXq245 start",
    "alter query XXq246 start",
    "alter query XXq247 start",
    "alter query XXq248 start",
    "alter query XXq249 start",
    "alter query XXq250 start",
    "alter query XXq251 start",
    "alter query XXq252 start",
    "alter query XXq253 start",
    "alter query XXq254 start",
    "alter query XXq255 start",
    "alter query XXq256 start",
    "alter query XXq257 start",
    "alter query XXq258 start",
    "alter query XXq259 start",
    "alter query XXq260 start",
    "alter query XXq261 start",
    "alter query XXq262 start",
    "alter query XXq263 start",
    "alter query XXq264 start",
    "alter query XXq265 start",
    "alter query XXq266 start",
    "alter query XXq267 start",
    "alter query XXq268 start",
    "alter query XXq269 start",
    "alter query XXq270 start",
    "alter query XXq271 start",
    "alter query XXq272 start",
    "alter query XXq273 start",
    "alter query XXq274 start",
    "alter query XXq275 start",
    "alter query XXq276 start",
    "alter query XXq277 start",
    "alter query XXq279 start",
    "alter query XXq280 start",
    "alter query XXq281 start",
    "alter query XXq282 start",
    "alter query XXq283 start",
    "alter query XXq284 start",
    "alter query XXq285 start",
    "alter query XXq286 start",
    "alter query XXq287 start",
    "alter query XXq288 start",
    "alter query XXq289 start",
    "alter query XXq290 start",
    "alter query XXq291 start",
    "alter query XXq292 start",
    "alter query XXq293 start",
    "alter query XXq294 start",
    "alter query XXq295 start",
    "alter query XXq296 start",
    "alter query XXq297 start",
    "alter query XXq298 start",
    "alter query XXq299 start",
    "alter query XXq300 start",
    "alter query XXq301 start",
    "alter query XXq302 start",
    "alter query XXq303 start",
    "alter query XXq304 start",
  };
  static class PhyWrapper implements ILoggable
  {
    int     m_id;

    int     m_type;

    Object  m_obj;
    
    PhyOpt  m_parent;
    
    boolean m_flag;

    PhyWrapper(PhyOpt opt)
    {
      m_obj = opt;
      m_type = opt.getOperatorKind().ordinal();
      m_id = opt.getId();
      m_parent = null;
    }

    PhyWrapper(PhyStore store)
    {
      m_obj = store;
      m_type = store.getStoreKind().ordinal();
      m_id = store.getId();
      m_parent = store.getOwnOp();
    }

    PhyWrapper(PhySynopsis syn)
    {
      m_obj = syn;
      m_type = syn.getKind().ordinal();
      m_id = syn.getId();
      m_parent = syn.getOwnOp();
    }

    PhyWrapper(PhyQueue q)
    {
      m_obj = q;
      m_type = -1;
      m_id = q.getId();
      if (q instanceof PhySharedQueueReader) {
        PhySharedQueueReader qr = (PhySharedQueueReader) q;
        m_parent = qr.getDestOp();
      } else if (q instanceof PhySharedQueueWriter) {
        PhySharedQueueWriter qw = (PhySharedQueueWriter) q;
        m_parent = qw.getSource();
      }
    }
    
    PhyWrapper(PhyIndex idx, PhyOpt p)
    {
      m_obj = idx;
      m_type = -1;
      m_id = idx.getId();
      m_parent = p;
    }
    
    public int getTargetId()
    {
      return m_id;
    }

    public String getTargetName()
    {
      return StringUtil.getBaseClassName(m_obj);
    }

    public int getTargetType()
    {
      return m_type;
    }

    public ILogLevelManager getLogLevelManager()
    {
      return CEPManager.getInstance().getSystemExecContext().getLogLevelManager();
    }
    
    public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
    {
    }

    public synchronized void dump(IDumpContext dump) 
    {
    }
  }
  
  private static class LogLevelChg
  {
    boolean m_enable;
    ILogArea     m_area;
    List<Integer> m_types;
    int           m_id;
    List<ILogEvent> m_events;
    List<Integer> m_levels;
    
    LogLevelChg(boolean enable, ILogArea area,
      int id, List<ILogEvent> events,
      List<Integer> levels)
    {
      m_enable = enable;
      m_area = area;
      m_id = id;
      m_events = events;
      m_levels = levels;
      if (events == null || 
          events.size()==0 || 
          events.get(0) == null) 
      {
        ILogEvent[] evs = LogEvent.getEvents(area);
        m_events = new ArrayList<ILogEvent>(evs.length);
        for (ILogEvent ev : evs)
        {
          m_events.add(ev); 
        }
      }
    }
    
    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      buf.append(m_enable ? "set " : "clear ");
      buf.append(m_area);
      buf.append(" id=" + m_id);
      buf.append(" events=");
      buf.append(m_events == null ? "" : CSVUtil.fromList(m_events));
      buf.append(" levels=");
      buf.append(m_levels == null ? "" : CSVUtil.fromList(m_levels));
      return buf.toString();
    }
  }
  
  public TestLogging(String name)
  {
    super(name);
    m_random = new Random();
    m_logLevelChgs = new LinkedList<LogLevelChg>();
  }

  /**
   * Sets up the test fixture. (Called before every test case method.)
   */
  @SuppressWarnings("unchecked")
  public void setUp()
  {
    try {
      if (!s_initalized) {
        s_driver = InterpDrv.getInstance();
        try {
          if (!s_driver.setUp(null))
            fail();
        } catch (Exception e) {
          System.out.println("TestLogging:failed to initialize " + e);
        }
        s_initalized = true;
        s_execContext = s_driver.getExecContext();
        System.out.println("TestLogging: initialize queries");
        for (String qry : s_baseQueries) {
          StringBuffer buf = new StringBuffer();
          buf.append("<CEP><CEP_DDL> ");
          buf.append(qry);
          buf.append(" </CEP_DDL>");
          buf.append("</CEP>\n");

          System.out.println(buf.toString());
          s_driver.setCqlX(buf.toString());
        }
      }
      s_phyObjs = new List[LogArea.values().size()]; //unchecked
      PlanManager pm = s_execContext.getPlanMgr();
      Collection<PhyOpt> objs = pm.getActivePhyOpt();
      List<PhyWrapper> qd = new ArrayList<PhyWrapper>(objs.size());
      s_phyObjs[LogArea.QUEUE.getValue()] = qd;
      List<PhyWrapper> d = new ArrayList<PhyWrapper>(objs.size());
      s_phyObjs[LogArea.OPERATOR.getValue()] = d;
      for (PhyOpt opt : objs) {
        if (opt == null)
          continue;
        d.add(new PhyWrapper(opt));
        PhyQueue outQ = opt.getOutQueue();
        if (outQ != null) {
          if (!hasObj(LogArea.QUEUE, outQ)) {
            qd.add(new PhyWrapper(outQ));
          }
        }
        PhyQueue[] inQs = opt.getInQueues();
        if (inQs != null) {
          for (PhyQueue q : inQs) {
            if (!hasObj(LogArea.QUEUE, q)) {
              qd.add(new PhyWrapper(q));
            }
          }
        }
      }
      Collection<PhySynopsis> syns = pm.getActivePhySyn();
      Collection<PhyStore> stores = pm.getActivePhyStore();
      List<PhyWrapper> dx = new ArrayList<PhyWrapper>(syns.size() + stores.size());
      s_phyObjs[LogArea.INDEX.getValue()] = dx;
      d = new ArrayList<PhyWrapper>(syns.size());
      s_phyObjs[LogArea.SYNOPSIS.getValue()] = d;
      for (PhySynopsis syn : syns) {
        if (syn == null)
          continue;
        d.add(new PhyWrapper(syn));
        List<PhyIndex> indexes = syn.getIndexes();
        if (indexes != null) {
          for (PhyIndex idx : indexes) {
            dx.add(new PhyWrapper(idx, syn.getOwnOp()));
          }
        }
      }
      d = new ArrayList<PhyWrapper>(stores.size());
      s_phyObjs[LogArea.STORE.getValue()] = d;
      for (PhyStore store : stores) {
        if (store == null)
          continue;
        d.add(new PhyWrapper(store));
        List<PhyIndex> indexes = store.getIndexes();
        if (indexes != null) {
          for (PhyIndex idx : indexes) {
            dx.add(new PhyWrapper(idx, store.getOwnOp()));
          }
        }
      }
      s_optIds = getIds(LogArea.OPERATOR, -1);

      QueryManager qm = s_execContext.getQueryMgr();
      String s = qm.getXMLPlan2();
      PrintWriter xml = null;
      try
      {
        xml = new PrintWriter("/tmp/XMLVisDump.xml");
        xml.append(s);
        xml.flush();
      }
      catch (IOException e)
      {
        System.out.println("problem with dumping xml");
      }
      finally
      {
        if (xml != null)
          xml.close();
      }

      m_lm = CEPManager.getInstance().getSystemExecContext().getLogLevelManager();
      PlanMonitor planmon = m_lm.getPlanMonitor();
      planmon.addChgNotifier(this);
    } catch (Exception e) {
      System.out.println("TestLogging: init fail " + e.toString());
    }
  }

  /**
   * Tears down the test fixture. (Called after every test case method.)
   */
  public void tearDown()
  {
    LogLevelManager lm = CEPManager.getInstance().getSystemExecContext().getLogLevelManager();
    lm.clear();
  }

  private int[] getIds(ILogArea area, int sz)
  {
    if (area.isGlobal()) 
    {
      int[] r = new int[1];
      r[0] = 0;
      return r;
    }

    List<PhyWrapper> objs = s_phyObjs[area.getValue()];
    if (sz < 0) sz = objs.size();
    int[] objIds = new int[sz];
    int pos = 0;
    for (PhyWrapper o : objs) {
      objIds[pos++] = o.m_id;
      if (pos >= sz) break;
    }
    return objIds;
  }
  
  private int[] listToArray(List<Integer> ids)
  {
    int objIds[] = new int[ids.size()];
    int pos = 0;
    for (Integer id : ids) {
      objIds[pos++] = id;
    }
    return objIds;
  }
  
  private boolean hasObj(ILogArea area, Object obj)
  {
    List<PhyWrapper> objs = s_phyObjs[area.getValue()];
    for (PhyWrapper o : objs) {
      if (o.m_obj == obj) {
        return true;
      }
    }
    return false;
  }
  
  private int[] getIdsForParent(ILogArea area, PhyOpt parent)
  {
    List<PhyWrapper> objs = s_phyObjs[area.getValue()];
    List<Integer> objids = new ArrayList<Integer>(objs.size());
    for (PhyWrapper o : objs) {
      if (o.m_parent == parent) {
        objids.add(o.m_id);
      }
    }
    return listToArray(objids);
  }
  
  private int[] getIdsForTypes(ILogArea area, int[] types)
  {
    List<PhyWrapper> objs = s_phyObjs[area.getValue()];
    List<Integer> ids = new ArrayList<Integer>(objs.size());
    for (PhyWrapper o : objs) {
      for (int type : types) {
        if (o.m_type == type) {
          ids.add(o.m_id);
          break;
        }
      }
    }
    return listToArray(ids);
  }
  
  private int[] getIdsForOpSub(int[] ids, ILogArea subType)
  {
    List<PhyWrapper> objs = s_phyObjs[LogArea.OPERATOR.getValue()];
    List<Integer> objids = new ArrayList<Integer>(objs.size());
    for (PhyWrapper o : objs) {
      for (int id : ids) {
        if (o.m_id == id) {
          int[] subs = null;
          PhyOpt opt = (PhyOpt) o.m_obj;
          subs= getIdsForParent(subType, opt);
          for (int subid : subs)
            objids.add(subid);
          break;
        }
      }
    }
    return listToArray(objids);
  }
  
  class QueryVisitor implements IPlanVisitor
  {
    ILogArea     m_area;
    int[]       m_ids_types;
    boolean     m_checkType;
    boolean     m_has;
    
    QueryVisitor(ILogArea area, int ids_types[], boolean checktype)
    {
      m_area = area;
      m_ids_types = ids_types;
      m_checkType = checktype;
      m_has = false;
    }
    
    public boolean canVisit(ObjType which)
    {
      return m_has == false;
    }
    
    boolean has(int id)
    {
      if (m_ids_types == null) return true;
      for (int i : m_ids_types)
      {
         if (i == id) {
           return true;
         }
      }
      return false;
    }
    
    public void visit(PhyOpt opt)
    {
      if (m_area != LogArea.OPERATOR) return;
      m_has = (m_checkType ?
        has(opt.getOperatorKind().ordinal()) :
        has(opt.getId()) );
      if (m_has)
        System.out.println("found " + opt.getId() + "," + opt.getOperatorKind().ordinal());
    }

    public void visit(PhySynopsis syn)
    {
      if (m_area != LogArea.SYNOPSIS) return;
      m_has = (m_checkType ?
        has(syn.getKind().ordinal()) :
        has(syn.getId()) );
      if (m_has)
        System.out.println("found " + syn.getId() + "," + syn.getKind().ordinal());
    }

    public void visit(PhyStore store)
    {
      if (m_area != LogArea.STORE) return;
      m_has = (m_checkType ?
        has(store.getStoreKind().ordinal()) :
        has(store.getId()) );
      if (m_has)
        System.out.println("found " + store.getId() + "," + store.getStoreKind().ordinal());
    }

    public void visit(PhyQueue queue)
    {
      if (m_area != LogArea.QUEUE) return;
      assert (!m_checkType );
      m_has = has(queue.getId());
      if (m_has)
        System.out.println("found " + queue.getId());
    }

    public void visit(PhyIndex index)
    {
      if (m_area != LogArea.INDEX) return;
      assert (!m_checkType );
      m_has = has(index.getId());
      if (m_has)
        System.out.println("found " + index.getId());
    }
  }  
  
  private String findQuery(ILogArea area, int id_types[], boolean checktype)
  {
    String qname = null;
    PlanManager pm = s_execContext.getPlanMgr();
    QueryManager qm = s_execContext.getQueryMgr();
    QueryVisitor v = new QueryVisitor(area, id_types, checktype);
    ArrayList<Integer> qids= pm.getRootQueryIds();
    String[] ignores = new String[] {
        "XXq0", "XXq1", "XXq2", "XXq3", 
        "XXq201", "XXq202", "XXq212", 
        "XXq219",
        "XXq301", "XXq304",
        "XXqX0", "XXqX1"
    };
    for (Integer qryId : qids)
    {
      Query q = null;
      try {
        q = qm.getQuery(qryId);
      } catch(MetadataException me)
      {
      }
      if (q == null || !q.getIsNamed()) continue;
      String qn = q.getName();
      boolean ignore = false;
      for (String ig : ignores) {
        if (ig.equals(qn)) {
          ignore = true;
          break;
        }
      }
      if (ignore) continue;
      System.out.println("lookup " + qn);
      PhyOpt root = pm.getQueryRootOpt(qryId);
      if (root != null) 
      {
        root.accept(v);
      }
      if (v.m_has) return qn;
    }
    return qname;
  }
  
  private String buildCSV(int[] ar)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < ar.length; i++) {
      if (i > 0)
        buf.append(",");
      buf.append(ar[i]);
    }
    return buf.toString();
  }

  private String buildCSV(ILogArea a, int[] ar)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < ar.length; i++) {
      if (i > 0)
        buf.append(",");
      String name = null;
      if (a == LogArea.OPERATOR) {    
        PhyOptKind opt = EnumUtil.fromOrdinal(PhyOptKind.class, ar[i]);
        name = opt.getName();
        break;
      } else if (a == LogArea.STORE) {
        PhyStoreKind store = EnumUtil.fromOrdinal(PhyStoreKind.class, ar[i]);
        name = store.getName();
        break;
      } else if (a == LogArea.SYNOPSIS) {
        SynopsisKind syn = EnumUtil.fromOrdinal(SynopsisKind.class, ar[i]);
        name = syn.getName();
        break;
      }
      assert (name != null);
      buf.append(name);
    }
    return buf.toString();
  }

  private String buildCSV(ILogEvent[] ar)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < ar.length; i++) {
      if (i > 0)
        buf.append(",");
      buf.append(ar[i].getValue());
    }
    return buf.toString();
  }

  private String buildCSV(String[] ar)
  {
    return CSVUtil.fromArray(ar);
  }

  private List<PhyWrapper> getObjs(ILogArea area, int[] types, int[] ids)
  {
    List<PhyWrapper> src = s_phyObjs[area.getValue()];
    if (src == null)
      return null;
    List<PhyWrapper> d = new ArrayList<PhyWrapper>(src.size());
    for (PhyWrapper o : src) {
      if (types != null) {
        for (int type : types) {
          if (o.m_type == type)
            d.add(o);
        }
      } else if (ids != null) {
        for (int id : ids) {
          if (o.m_id == id)
            d.add(o);
        }
      }
    }
    return d;
  }

  private int[] getOperatorsForTypes(int[] types)
  {
    List<PhyWrapper> objs = getObjs(LogArea.OPERATOR, types, null);
    int[] ret = new int[objs.size()];
    int pos = 0;
    for (PhyWrapper obj : objs)
    {
      ret[pos++] = obj.getTargetId();
    }
    return ret;
  }
  
  static class CheckDesc
  {
    ILogArea m_area;
    int     m_id;
    ILogEvent m_event;
    Levels  m_levels;
    CheckDesc(ILogArea area, int id, ILogEvent event, int[] levels)
    {
      m_area = area;
      m_id = id;
      m_event =event;
      if (levels == null) m_levels = null;
      else {
        m_levels = new Levels();
        for (int level : levels) {;
          m_levels.set(level);
        }
      }
    }
    public void addLevels(CheckDesc other)
    {
      if (other.m_levels != null)
        m_levels.or(other.m_levels);
    }
    public boolean removeLevels(List<Integer> levels)
    {
      for (Integer l :levels)
      {
        m_levels.clear(l);
      }
      return m_levels.isEmpty();
    }
    
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      b.append("check - ");
      b.append(m_area.toString());
      b.append(" id= ");
      b.append(m_id);
      b.append(" event= ");
      b.append(m_event.toString());
      b.append(" levels=");
      b.append(m_levels.toString());
      return b.toString();
    }
    boolean check()
    {
      LogLevelManager lm = CEPManager.getInstance().getSystemExecContext().getLogLevelManager();
      Levels levels = lm.getLevels(m_area, m_id, m_event);
      if (m_levels == null) {
        return (levels == null);
      } else {
        return m_levels.equals(levels);
      } 
    }
  }

  protected boolean isDump()
  {
    return false;
  }
  
  private void set(boolean enable, ILogArea area, int[] types, String[] names,
                   int[] ids,
      ILogEvent[] events, int[] levels) throws CEPException
  {
    String sarea = area.getName();
    String senable = (enable ? "enable" : "disable");
    if (isDump()) senable = "dump";
    StringBuffer sb = new StringBuffer();
    sb.append("<CEP>\n");
    sb.append("<CEP_DDL> alter system ");
    sb.append(senable);
    sb.append(" logging ");
    sb.append(sarea);
    if (types != null) {
      sb.append(" type ");
      sb.append(buildCSV(area, types));
    } else if (ids != null)  {
      sb.append(" identified by ");
      sb.append(buildCSV(ids));
    } else if (names != null) {
      sb.append(" identified by ");
      sb.append(buildCSV(names));
    }
    if (events != null) {
      sb.append(" event ");
      sb.append(buildCSV(events));
    }
    sb.append(" level ");
    sb.append(buildCSV(levels));
    sb.append("</CEP_DDL>\n");
    sb.append("</CEP>\n");
    String q = sb.toString();
    System.out.println(q);
    s_driver.setCqlX(q);
    System.out.println("Result\n"+m_lm.toString());
  }

  private void addCheck(List<CheckDesc> items, CheckDesc item)
  {
    for (CheckDesc i : items) {
      if (i.m_area == item.m_area &&
          i.m_id == item.m_id &&
          i.m_event == item.m_event)
      {
        i.addLevels(item);
        return;
      }
    }
    items.add(item);
  }

  private void addChg(List<CheckDesc> items, LogLevelChg chg)
  {
    if (chg.m_area == LogArea.SYSTEMSTATE) return;
    int[] levels = new int[chg.m_levels.size()];
    int pos = 0;
    for (Integer l : chg.m_levels)
      levels[pos++] = l;
    for (ILogEvent event : chg.m_events) 
    {
      CheckDesc newitem = new CheckDesc(chg.m_area, chg.m_id, event, levels);
      addCheck(items, newitem);
      System.out.println("add " + newitem.toString());
    }
  }

  private void removeChg(List<CheckDesc> items, LogLevelChg chg)
  {
    List<CheckDesc> removes = new LinkedList<CheckDesc>();
    for (ILogEvent event : chg.m_events) 
    {
      for (CheckDesc i : items) {
        if (i.m_area == chg.m_area &&
            i.m_id == chg.m_id &&
            i.m_event == event)
        {
          System.out.println("remove levels " + i.toString() + " " + 
              CSVUtil.fromList(chg.m_levels));
          if (i.removeLevels(chg.m_levels)) 
          {
            removes.add(i);
          }
        }
      }
    }
    for (CheckDesc i : removes)
    {
      System.out.println("remove " + i.toString());
      items.remove(i);
    }
  }
  
  protected void check(List<CheckDesc> items)
  {
    if (isDump())
      return;
    for (CheckDesc item : items)
    {
      //System.out.println(item.toString());
    }
    
    if (m_logLevelChgs.size() > 0)
    {
      // update items
      System.out.println("--- changes of levels");
      for (LogLevelChg chg : m_logLevelChgs)
      {
        System.out.println(chg.toString());
      }
      System.out.println("---");
      for (LogLevelChg chg : m_logLevelChgs)
      {
        if (chg.m_enable) 
        {
          addChg(items, chg);
        } else {
          removeChg(items, chg);  
        }
      }
    }
    
    Collection<ILogArea> areas = LogArea.values();
    BitSet areachks[] = new BitSet[areas.size()];
    for (ILogArea area : areas) {
      areachks[area.getValue()] = new BitSet();
    }
    int count = 0;
    for (CheckDesc item : items) {
      boolean chk = item.check();
      if (!chk) {
        count++;
        System.out.println("fail " + item.toString());
      }
      areachks[item.m_area.getValue()].set(item.m_id);
    }
    assertEquals(0, count);
    count = 0;
    for (int i = 0; i < areas.size(); i++) {
      ILogArea a = LogArea.fromValue(i);
      List<Integer> ids = m_lm.getIds(a);
      if (ids != null && ids.size() > 0) {
        for (Integer id : ids) {
          if (areachks[i] != null && areachks[i].get(id)) continue;
          List<Integer> evs = m_lm.getEvents(a, id);
          if (evs != null && evs.size() > 0) {
            for (Integer ev : evs) {
              ILogEvent event = LogEvent.fromValue(ev);
              Levels l = m_lm.getLevels(a, id, event);
              if (l != null && !l.isEmpty()) {
                StringBuilder b = new StringBuilder();
                b.append("unexpected flag : ");
                b.append(a.toString());
                b.append(" ");
                b.append("id = " + id);
                b.append(" ");
                b.append("ev = " + event.toString());
                for (int level = l.nextSetBit(0); level >= 0; level = l.nextSetBit(level+1))
                {
                  b.append(" ");
                  b.append(level);
                }
                System.out.println(b.toString());
                count++;
              }
            }
          }
        }
      }
    }
    assertEquals(0, count);
  }
  
  private List<CheckDesc> addChecks(List<CheckDesc> chks,
        ILogArea area, int[] chkids, ILogEvent[] evs, 
      int[] levels)
  {
    if (chks == null)
      chks = new LinkedList<CheckDesc>();
    if (levels == null || levels.length==0)
      return chks;
    
    if (chkids == null) {
      chkids = getIds(area, -1);
    }
    if (chkids == null || chkids.length == 0)
      return chks;
    
    if (evs == null) {
      evs = LogEvent.getEvents(area);
    }
    for (int id : chkids) {
      for (ILogEvent ev : evs) {
        addCheck(chks, new CheckDesc(area, id, ev, levels));
      }
    }
    return chks;
  }

  private List<CheckDesc>  addChecksForArea(ILogArea area, int[] chkids, ILogEvent[] evs, 
                        int[] levels)
  {
    List<CheckDesc> chks = addChecks(null, area, chkids, evs, levels);
    return chks;
  }

  private List<CheckDesc> addChecksForOperators(List<CheckDesc> chks,
        int[] opids, ILogEvent[] opevs, int[] oplevels,
            ILogEvent[] subevents, int[] sublevels)
  {
    PlanMonitor pm = m_lm.getPlanMonitor();
    ILogArea areas[] = new ILogArea[] {LogArea.QUEUE, LogArea.STORE, LogArea.SYNOPSIS, LogArea.INDEX};
    for (int level : sublevels) {
      for (ILogEvent event : subevents) {
        for (ILogArea area : areas) {
          int levels[] = pm.getOperatorLevels(event, level, area);
          ILogEvent evs[] = pm.getOperatorEvents(event, area);
          int chkids[] = getIdsForOpSub(opids, area);
          addChecks(chks, area, chkids, evs, levels);
        }
      }
    }
    return addChecks(chks, LogArea.OPERATOR, opids, opevs, oplevels);
  }
  
  private List<CheckDesc> addChecksForOperators(int[] opids, ILogEvent[] opevs, int[] oplevels,
      ILogEvent[] subevents, int[] sublevels)
  {
    List<CheckDesc> chks = new LinkedList<CheckDesc>();
    return addChecksForOperators(chks, opids, opevs, oplevels, subevents, sublevels);
  }
  
  private List<CheckDesc>  addChecksForQueries(int[] qryids, ILogEvent[] opevs, int[] oplevels,
      ILogEvent[] subevents, int[] sublevels)
  {
    PlanMonitor pm = m_lm.getPlanMonitor();
    List<CheckDesc> chks = new LinkedList<CheckDesc>();
    List<Integer> qids = ArrayUtil.fromArray(qryids);
    List<PhyOpt> opts = pm.getOperatorsForQuery(qids);
    int[] opids = new int[opts.size()];
    int pos = 0;
    for (PhyOpt opt : opts) {
      opids[pos++]= opt.getId();
    }
    return addChecksForOperators(chks, opids, opevs, oplevels, subevents, sublevels);
  }

  private void removePlanChange(PlanChgType chgType, String qName) throws CEPException
  {
    if (chgType != PlanChgType.ADD_QUERY) return;
    m_bLogChg = false;
    m_logLevelChgs.clear();
    StringBuffer buf = new StringBuffer();
    buf.append("<CEP>\n");
    String qn = "XXqX" + qName;
    buf.append("<CEP_DDL> drop query "+qn + " </CEP_DDL>\n");
    buf.append("</CEP>\n");
    s_driver.setCqlX(buf.toString());
  }
  
  private void planChange(PlanChgType chgType, String qName) throws CEPException
  {
    m_bLogChg = true;
    m_logLevelChgs.clear();
    StringBuffer buf = new StringBuffer();
    buf.append("<CEP>\n");
    switch (chgType)
    {
    case ADD_QUERY:
    {
      int qid = Integer.parseInt(qName);
      int qidx= (qid % 1000); 
      String qn = "XXqX" + qid;
      buf.append("<CEP_DDL> create query " + qn + " "+ s_queries[qidx] + "</CEP_DDL>\n");
      buf.append("<CEP_DDL> alter query "+qn + " add destination \"<EndPointReference><Address>file://@TEST_OUTPUT@/" + qn + ".txt</Address></EndPointReference>\" </CEP_DDL>\n");
      buf.append("<CEP_DDL> alter query "+qn + " start </CEP_DDL>\n");
    }
      break;
    case DROP_QUERY:
      {
      buf.append("<CEP_DDL> drop query " + qName + "</CEP_DDL>\n");
      }
      break;
    }
    buf.append("</CEP>\n");
    s_driver.setCqlX(buf.toString());
 }

  
  private <T> void selectRandom(T[] values, int no, T[] res)
  {
    int n = values.length;
    for (int i = 0; i < n; i++) 
    {
      int r = i + (int) (Math.random() * (n-i));
      T t = values[i];
      values[i] = values[r];
      values[r] = t;
    }
    for (int i = 0; i < no; i++)
    {
      res[i] = values[i];
    }
  }

  private int[] selectRandom(int[] values, int no)
  {
    if (values == null) return null;
    if (no < 0) no = values.length;
    no = (no > values.length) ? values.length:no;
    Integer[] result = new Integer[no];
    selectRandom(toObjs(values), no, result);
    return fromObjs(result);
  }
  
  private Integer[] toObjs(int[] ia)
  {
    Integer[] oa = new Integer[ia.length];
    for (int i = 0; i <ia.length; i++)
      oa[i] = ia[i];
    return oa;
  }

  private int[] fromObjs(Integer[] oa)
  {
    int[] ia = new int[oa.length];
    for (int i = 0; i <oa.length; i++)
      ia[i] = oa[i];
    return ia;
  }

  private int[] getRandomLevels(ILogArea area, int no)
  {
    int areaLevels[] = LogLevel.getLevels(area);
    if (no < 0) no = areaLevels.length;
    else if (no == 0) {
      no = m_random.nextInt(areaLevels.length) + 1;
    }
    return selectRandom(areaLevels, no);
  }
  
  private ILogEvent[] getRandomEvents(ILogArea area, int no)
  {
    ILogEvent[] areaEvents = LogEvent.getEvents(area);
    no = (no > areaEvents.length) ? areaEvents.length:no;
    ILogEvent[] result = new ILogEvent[no];
    selectRandom(areaEvents, no, result);
    return result;
  }
  
  private int[] getRandomTypes(ILogArea area, int no)
  {
    int[] areaTypes = null;
    if (area == LogArea.OPERATOR)
      areaTypes = EnumUtil.getOrdinals(PhyOptKind.class);
    else if (area == LogArea.SYNOPSIS)
      areaTypes = EnumUtil.getOrdinals(SynopsisKind.class);
    else if (area == LogArea.STORE)
      areaTypes = EnumUtil.getOrdinals(PhyStoreKind.class);
    return selectRandom(areaTypes, no);
  }
  
  private int[] getRandomIds(ILogArea area, int no)
  {
    List<PhyWrapper> src = s_phyObjs[area.getValue()];
    if (src == null)
      return null;
    int[] d = new int[src.size()];
    int pos = 0;
    for (PhyWrapper o : src) {
      d[pos++] = o.getTargetId();
    }
    return selectRandom(d, no);
  }

  private void checkArea_Level(ILogArea area, PlanChgType chgType, String queryName, int no) 
    throws CEPException
  {
    int levels[] = getRandomLevels(area, no);
    ILogEvent events[] = null;
    // 1 : area level
    set(true, area, null, null, null, events, levels);

    List<CheckDesc> chks = addChecksForArea(area, null, null, levels);

    if (chgType != PlanChgType.NONE)
    {  
      if (queryName == null) 
      {
        queryName = findQuery(area, null, false);
      }
      if (queryName != null)
        planChange(chgType, queryName);
    }
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkArea_EventLevel(ILogArea area, PlanChgType chgType, 
                                    String queryName, int no) throws CEPException
  {
    // 2 : area event level
    int levels[] = getRandomLevels(area, no);
    ILogEvent[] events = getRandomEvents(area, 1);
    set(true, area, null, null, null, events, levels);

    List<CheckDesc> chks = addChecksForArea(area, null, events, levels);

    if (chgType != PlanChgType.NONE)
    {  
      if (queryName == null) 
      {
        queryName = findQuery(area, null, false);
      }
      if (queryName != null)
        planChange(chgType, queryName);
    }
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkAreaTypes_Level(ILogArea area, PlanChgType chgType, 
                                    String queryName, int no) 
    throws CEPException
  {
    int levels[] = getRandomLevels(area, no);
    ILogEvent[] events = getRandomEvents(area, 1);
    int types[] = getRandomTypes(area, -1);

    set(true, area, types, null, null, events, levels);

    int ids[] = this.getIdsForTypes(area, types);
    List<CheckDesc> chks = addChecksForArea(area, ids, events, levels);

    if (chgType != PlanChgType.NONE)
    {  
      if (queryName == null) 
      {
        queryName = findQuery(area, types, true);
      }
      if (queryName != null)
        planChange(chgType, queryName);
    }
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkAreaIds_Level(ILogArea area, PlanChgType chgType, 
                                  String queryName, int no) 
    throws CEPException
  {
    int ids[] = getRandomIds(area, 5);
    int levels[] = getRandomLevels(area, no);
    // 5 : area id level
    set(true, area, null, null, ids, null, levels);

    List<CheckDesc> chks = addChecksForArea(area, ids, null, levels);

    if (chgType != PlanChgType.NONE)
    {  
      if (queryName == null) 
      {
        queryName = findQuery(area, ids, false);
      }
      if (queryName != null)
        planChange(chgType, queryName);
    }
    check(chks);
    removePlanChange(chgType, queryName);
}

  private void checkAreaIds_EventLevel(ILogArea area, PlanChgType chgType, String queryName) 
    throws CEPException
  {
    // 6 : area id event level
    ILogEvent[] events = getRandomEvents(area, 2);
    int ids[] = getRandomIds(area, 2);
    int levels[] = getRandomLevels(area, 2);
    set(true, area, null, null, ids, events, levels);

    List<CheckDesc> chks = addChecksForArea(area, ids, events, levels);

    if (chgType != PlanChgType.NONE)
    {  
      if (queryName == null) 
      {
        queryName = findQuery(area, ids, false);
      }
      if (queryName != null)
        planChange(chgType, queryName);
    }
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkOperators_Area(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    ILogArea area = LogArea.OPERATOR;
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_ALL_DS};
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS};
    set(true, area, null, null, null, events, levels);
  
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_RUN_BEGIN};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_ALL_DS};
    List<CheckDesc> chks = addChecksForOperators(s_optIds, opevents, oplevels, subevents, sublevels);

    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkOperators_Types(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    ILogArea area = LogArea.OPERATOR;
    int types[] = {PhyOptKind.PO_RANGE_WIN.ordinal(), PhyOptKind.PO_JOIN.ordinal(), PhyOptKind.PO_OUTPUT.ordinal()};
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_DDL, 
                    LogEvent.OPERATOR_QUEUE_ENQDEQ,
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN
                    };
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS,
                    LogLevel.OPERATOR_STRUCTURES_LEAST};
    set(true, area, types, null, null, events, levels);
  
    int optIds[] = getOperatorsForTypes(types);
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_DDL};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS, LogLevel.OPERATOR_STRUCTURES_LEAST};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_QUEUE_ENQDEQ, LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    List<CheckDesc> chks = addChecksForOperators(optIds, opevents, oplevels, subevents, sublevels);

    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkOperators_Ids(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    ILogArea area = LogArea.OPERATOR;
    int types[] = null;
    int n = s_optIds.length / 2;
    int ids[] = new int[n];
    for (int i = 0; i < n; i++) 
    {
      ids[i] = s_optIds[i*2];
    }
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_DDL, 
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS,
                    LogLevel.OPERATOR_STRUCTURES_LEAST};
    set(true, area, types, null, ids, events, levels);
  
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_DDL};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS, LogLevel.OPERATOR_STRUCTURES_LEAST};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    List<CheckDesc> chks = addChecksForOperators(ids, opevents, oplevels, subevents, sublevels);

    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }


  private void checkQuery(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    //set level for a query
    ILogArea area = LogArea.QUERY;
    int types[] = null;
    QueryManager qm = s_execContext.getQueryMgr();
  
    int queryId = qm.findQuery(queryName, Constants.DEFAULT_SCHEMA);
    int ids[] = new int[1];
    String names[] = new String[1];
    Query q = qm.getQuery(queryId);
    assertNotNull(q);
    names[0] = q.getName();
    ids[0] = q.getId();
  
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_DDL, LogEvent.OPERATOR_QUEUE_ENQDEQ,
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS,
                    LogLevel.OPERATOR_STRUCTURES_LEAST};
    set(true, area, types, names, null, events, levels);
    
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_DDL};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS, LogLevel.OPERATOR_STRUCTURES_LEAST};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_QUEUE_ENQDEQ,
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
  
    List<CheckDesc> chks = addChecksForQueries(ids, opevents, oplevels, subevents, sublevels);
  
    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkQueries(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    PlanManager pm = s_execContext.getPlanMgr();
    ILogArea area = LogArea.QUERY;
    int types[] = null;
    QueryManager qm = s_execContext.getQueryMgr();
    ArrayList<Integer> qids = pm.getRootQueryIds();
    LinkedList<Query> queries = new LinkedList<Query>();
    for (Integer qid : qids) {
      Query q = qm.getQuery(qid);
      if (q.getIsNamed())
      {
        queries.add(q);
      }
    }
    int ids[] = new int[queries.size()];
    String names[] = new String[queries.size()];
    int pos = 0;
    for (Query q : queries) {
      names[pos] = q.getName();
      ids[pos++] = q.getId();
    }
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_DDL, 
                    LogEvent.OPERATOR_QUEUE_ENQDEQ, 
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS,
                    LogLevel.OPERATOR_STRUCTURES_LEAST};
    set(true, area, types, names, null, events, levels);
    
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_DDL};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS, LogLevel.OPERATOR_STRUCTURES_LEAST};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_QUEUE_ENQDEQ, 
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
  
    List<CheckDesc> chks = addChecksForQueries(ids, opevents, oplevels, subevents, sublevels);

    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }

  private void checkAllQueries(PlanChgType chgType, String queryName) 
    throws CEPException
  {
    PlanManager pm = s_execContext.getPlanMgr();
    ILogArea area = LogArea.QUERY;
    ILogEvent events[] = new ILogEvent[] {LogEvent.OPERATOR_DDL, 
                    LogEvent.OPERATOR_QUEUE_ENQDEQ,
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    int levels[] = {LogLevel.OPERATOR_INFO, LogLevel.OPERATOR_STRUCTURES_STATS,
                    LogLevel.OPERATOR_STRUCTURES_LEAST};
    set(true, area, null, null, null, events, levels);
  
    int oplevels[] = {LogLevel.OPERATOR_INFO};
    ILogEvent opevents[] = new ILogEvent[] {LogEvent.OPERATOR_DDL};
    int sublevels[] = {LogLevel.OPERATOR_STRUCTURES_STATS, LogLevel.OPERATOR_STRUCTURES_LEAST};
    ILogEvent subevents[] = new ILogEvent[] {LogEvent.OPERATOR_QUEUE_ENQDEQ,
                    LogEvent.OPERATOR_SYNOPSIS_INSDEL, LogEvent.OPERATOR_INDEX_SCAN};
    ArrayList<Integer> qids = pm.getRootQueryIds();
    int ids[] = new int[qids.size()];
    int pos = 0;
    for (Integer qid : qids) {
      ids[pos++] = qid;
    }
  
    List<CheckDesc> chks = addChecksForQueries(ids, opevents, oplevels, subevents, sublevels);

    if (chgType != PlanChgType.NONE)
      planChange(chgType, queryName);
    check(chks);
    removePlanChange(chgType, queryName);
  }

  public void testStore_Level() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.NONE, null, 1);
  }

  public void testStore_Level0() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.NONE, null, 0);
  }

  public void testStore_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.NONE, null, 1);
  }
  
  public void testStore_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.NONE, null, 0);
  }
    
  public void testStoreTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.STORE, PlanChgType.NONE, null, 1);  
  }

  public void testStoreTypes_EventLevel0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.STORE, PlanChgType.NONE, null, 0);  
  }

  public void testStoreIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.NONE, null, 1);
  }

  public void testStoreIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.NONE, null, 0);
  }

  public void testStoreIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.STORE, PlanChgType.NONE, null);
  }

  public void testQueue_Level() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.NONE, null, 1);
  }

  public void testQueue_Level0() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.NONE, null, 0);
  }

  public void testQueue_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.QUEUE, PlanChgType.NONE, null, 1);
  }
  
  public void testQueue_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.QUEUE, PlanChgType.NONE, null, 0);
  }
  
  public void testQueueIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.NONE, null, 1);
  }

  public void testQueueIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.NONE, null, 0);
  }

  public void testQueueIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.QUEUE, PlanChgType.NONE, null);
  }

  public void testSynopsis_Level() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 1);
  }

  public void testSynopsis_Level0() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 0);
  }

  public void testSynopsis_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.SYNOPSIS, PlanChgType.NONE, null, 1);
  }
  
  public void testSynopsis_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.SYNOPSIS, PlanChgType.NONE, null, 0);
  }
  
  public void testSynopsisTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 1);  
  }

  public void testSynopsisTypes_EventLevel0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 0);  
  }

  public void testSynopsisIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 1);
  }

  public void testSynopsisIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.SYNOPSIS, PlanChgType.NONE, null, 0);
  }

  public void testSynopsisIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.SYNOPSIS, PlanChgType.NONE, null);
  }

  public void testIndex_Level() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.NONE, null, 1);
  }

  public void testIndex_Level0() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.NONE, null, 0);
  }

  public void testIndex_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.NONE, null, 1);
  }
  
  public void testIndex_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.NONE, null, 0);
  }
  
  public void testIndexIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.INDEX, PlanChgType.NONE, null, 1);
  }

  public void testIndexIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.INDEX, PlanChgType.NONE, null, 0);
  }

  public void testIndexIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.INDEX, PlanChgType.NONE, null);
  }
  
  public void testOperator_Area() throws CEPException
  {
    checkOperators_Area(PlanChgType.NONE, null);
  }

  public void testOperator_Types() throws CEPException
  {
    checkOperators_Types(PlanChgType.NONE, null);
  }

  public void testOperator_Ids() throws CEPException
  {
    checkOperators_Ids(PlanChgType.NONE, null);
  }
  
  public void testQuery() throws CEPException
  {
    checkQuery(PlanChgType.NONE, "XXq3");
  }

  public void testAllQueries() throws CEPException
  {
    checkAllQueries(PlanChgType.NONE, null);
  }

  public void testDropQuery_AllQueries() throws CEPException
  {
    checkAllQueries(PlanChgType.DROP_QUERY, "XXq301");
  }

  public void testDropQuery_Querys() throws CEPException
  {
    checkQueries(PlanChgType.DROP_QUERY, "XXq304");
  }

  // query_id, drop query
  public void testDropQuery_QueryId() throws CEPException
  {
    checkQuery(PlanChgType.DROP_QUERY, "XXq3");
if (false) {
 //hard coded check - not necessary.
    ILogEvent[] events_op = new ILogEvent[] { LogEvent.OPERATOR_DDL};
    int ids_op[] ={ 0, 6, 35 };
    int levels_op[] = { 1};
    ILogEvent[] events_q = new ILogEvent[] { LogEvent.QUEUE_ENQUEUE, LogEvent.QUEUE_DEQUEUE};
    int ids_q[] ={ 1, 10, 74 };
    int levels_q[] = { 2,4};
    List<CheckDesc> chks = addChecks(null, LogArea.OPERATOR, ids_op, events_op, levels_op);
    addChecks(chks, LogArea.QUEUE, ids_q, events_q, levels_q);
    check(chks);
}  
  }

  // operator_id, drop query
  public void testDropQuery_OperatorIds() throws CEPException
  {
    checkOperators_Ids(PlanChgType.DROP_QUERY, "XXq1");
  }
  
  
  // queue_id, drop query
  // store_id, drop query
  // operator_type, drop query
  public void testDropQuery_OperatorTypes() throws CEPException
  {
    checkOperators_Types(PlanChgType.DROP_QUERY, "XXq2");
  }
 

  public void testDropQuery_Store_Level() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_Store_Level0() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_Store_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.DROP_QUERY, null, 1);
  }
  
  public void testDropQuery_Store_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.DROP_QUERY, null, 0);
  }
  
  public void testDropQuery_StoreTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 1);  
  }

  public void testDropQuery_StoreTypes_EventLevel0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 0);  
  }

  public void testDropQuery_StoreIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_StoreIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_StoreIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.STORE, PlanChgType.DROP_QUERY, null);
  }

  public void testDropQuery_Queue_Level() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_Queue_Level0() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_Queue_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 1);
  }
  
  public void testDropQuery_Queue_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 0);
  }
  
  public void testDropQuery_QueueIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_QueueIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.DROP_QUERY, null, 0);  
  }

  public void testDropQuery_QueueIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.QUEUE, PlanChgType.DROP_QUERY, null);
  }

  public void testDropQuery_Synopsis_Level() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_Synopsis_Level0() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_Synopsis_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 1);
  }
  
  public void testDropQuery_Synopsis_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 0);
  }
  
  public void testDropQuery_SynopsisTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 1);  
  }

  public void testDropQuery_SynopsisTypes_EventLevel0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 0);  
  }

  public void testDropQuery_SynopsisIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_SynopsisIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_SynopsisIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.SYNOPSIS, PlanChgType.DROP_QUERY, null);
  }

  public void testDropQuery_Index_Level() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_Index_Level0() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_Index_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 1);
  }
  
  public void testDropQuery_Index_EventLevel0() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 0);
  }
  
  public void testDropQuery_IndexIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 1);
  }

  public void testDropQuery_IndexIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.INDEX, PlanChgType.DROP_QUERY, null, 0);
  }

  public void testDropQuery_IndexIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.INDEX, PlanChgType.DROP_QUERY, null);
  }
  /*
   * Gettting assertion with isWritable() in removeDestView.
   * Not sure why it's happening.
   * It must be some setup.
   * Disable the drop queries in the mean time
   */

  public void testAddQuery_AllQueries() throws CEPException
  {
    checkAllQueries(PlanChgType.ADD_QUERY, "0");
  }
  
  public void testAddQuery_Querys() throws CEPException
  {
    checkQueries(PlanChgType.ADD_QUERY, "1000");
  }

  public void testAddQuery_OperatorIds() throws CEPException
  {
    checkOperators_Ids(PlanChgType.ADD_QUERY, "2000");
  }
  
  public void testAddQuery_OperatorTypes() throws CEPException
  {
    checkOperators_Types(PlanChgType.ADD_QUERY, "3000");
  }
 
  public void testAddQuery_Store_Level() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.ADD_QUERY, "4000", 1);
  }
  
  public void testAddQuery_Store_Level0() throws CEPException
  {
    checkArea_Level(LogArea.STORE, PlanChgType.ADD_QUERY, "5000", 0);
  }
  
  public void testAddQuery_Store_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.ADD_QUERY, "6000", 1);
  }
  
  public void testAddQuery_Store_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.ADD_QUERY, "7000", 0);
  }
  
  public void testAddQuery_StoreTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.STORE, PlanChgType.ADD_QUERY, "8000", 1);  
  }

  public void testAddQuery_StoreTypes_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORE, PlanChgType.ADD_QUERY, "9000", 0);
  }

  public void testAddQuery_StoreIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.ADD_QUERY, "10000", 1);
  }

  public void testAddQuery_StoreIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.STORE, PlanChgType.ADD_QUERY, "11000", 0);
  }

  public void testAddQuery_StoreIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.STORE, PlanChgType.ADD_QUERY, "12000");
  }

  public void testAddQuery_Queue_Level() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.ADD_QUERY, "13000", 0);
  }

  public void testAddQuery_Queue_Level0() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.ADD_QUERY, "14000", 0);
  }

  public void testAddQuery_Queue_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.QUEUE, PlanChgType.ADD_QUERY, "15000", 1);
  }
  
  public void testAddQuery_Queue_EventLevel0() throws CEPException
  {
    checkArea_Level(LogArea.QUEUE, PlanChgType.ADD_QUERY, "16000", 0);
  }
  
  public void testAddQuery_QueueIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.ADD_QUERY, "17000", 1);
  }

  public void testAddQuery_QueueIds_Level0() throws CEPException
  {
    checkAreaIds_Level(LogArea.QUEUE, PlanChgType.ADD_QUERY, "18000", 0);
  }

  public void testAddQuery_QueueIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.QUEUE, PlanChgType.ADD_QUERY, "19000");
  }

  public void testAddQuery_Synopsis_Level() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "20000", 1);
  }

  public void testAddQuery_Synopsis_Level0() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "21000", 0);
  }

  public void testAddQuery_Synopsis_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "22000", 1);
  }
  
  public void testAddQuery_Synopsis_EventLevel0() throws CEPException
  {
    checkArea_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "23000", 0);
  }
  
  public void testAddQuery_SynopsisTypes_EventLevel() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "24000", 0);  
  }

  public void testAddQuery_SynopsisTypes_EventLevel0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "25000", 0);  
  }

  public void testAddQuery_SynopsisIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "26000", 1);
  }

  public void testAddQuery_SynopsisIds_Level0() throws CEPException
  {
    checkAreaTypes_Level(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "27000", 0);  
  }

  public void testAddQuery_SynopsisIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.SYNOPSIS, PlanChgType.ADD_QUERY, "28000");
  }

  public void testAddQuery_Index_Level() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.ADD_QUERY, "29000", 1);
  }

  public void testAddQuery_Index_Level0() throws CEPException
  {
    checkArea_Level(LogArea.INDEX, PlanChgType.ADD_QUERY, "30000", 0);
  }

  public void testAddQuery_Index_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.ADD_QUERY, "31000", 1);
  }
  
  public void testAddQuery_Index_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.ADD_QUERY, "32000", 0);
  }
  
  public void testAddQuery_IndexIds_Level() throws CEPException
  {
    checkAreaIds_Level(LogArea.INDEX, PlanChgType.ADD_QUERY, "33000", 1);
  }

  public void testAddQuery_IndexIds_Level0() throws CEPException
  {
    checkArea_EventLevel(LogArea.INDEX, PlanChgType.ADD_QUERY, "34000", 0);
  }

  public void testAddQuery_IndexIds_EventLevel() throws CEPException
  {
    checkAreaIds_EventLevel(LogArea.INDEX, PlanChgType.ADD_QUERY, "35000");
  }

  public void testSpill_Level() throws CEPException
  {
    checkArea_Level(LogArea.SPILL, PlanChgType.NONE, null, 1);
  }

  public void testSpill_Level0() throws CEPException
  {
    checkArea_Level(LogArea.SPILL, PlanChgType.NONE, null, -1);
  }

  public void testSpill_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.SPILL, PlanChgType.NONE, null, 1);
  }
  
  public void testSpill_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.SPILL, PlanChgType.NONE, null, -1);
  }
  
  public void testStorage_Level() throws CEPException
  {
    checkArea_Level(LogArea.STORAGE, PlanChgType.NONE, null, 1);
  }

  public void testStorage_Level0() throws CEPException
  {
    checkArea_EventLevel(LogArea.SPILL, PlanChgType.NONE, null, -1);
  }

  public void testStorage_EventLevel() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORAGE, PlanChgType.NONE, null, 1);
  }
  
  public void testStorage_EventLevel0() throws CEPException
  {
    checkArea_EventLevel(LogArea.STORAGE, PlanChgType.NONE, null, -1);
  }
  
  public void testSysState_Level() throws CEPException
  {
    checkArea_Level(LogArea.SYSTEMSTATE, PlanChgType.NONE, null, 1);
  }

  public void testSysState_Level0() throws CEPException
  {
    checkArea_Level(LogArea.SYSTEMSTATE, PlanChgType.NONE, null, -1);
  }

 /* (non-Javadoc)
  * @see oracle.cep.logging.PlanMonitor.ILogLevelChgNotifier#change(boolean, oracle.cep.logging.ILogArea, java.util.List, java.util.List, java.util.List, java.util.List)
  */
 public void change(boolean enable, ILogArea area, 
       int id, 
       List<ILogEvent> events, List<Integer> levels)
 {
   if (m_bLogChg)
   {
     LogLevelChg chg = new LogLevelChg(enable, area, id, events, levels);
     m_logLevelChgs.add(chg);
   }
 }
 
 public static Test suite()
 {
   if (SINGLE_TEST_NAME != null)
   {
     TestSuite suite = new TestSuite();
     suite.addTest(new TestLogging(SINGLE_TEST_NAME));
     return suite;
   } else {
     return new TestSuite(TestLogging.class);
   }
 }
 
 public static final String SINGLE_TEST_NAME = null;
 
 public static void main(String[] args)
 {
   junit.textui.TestRunner.run(TestLogging.suite());
 }
}
