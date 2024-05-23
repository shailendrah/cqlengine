/* $Header: pcbpel/cep/test/src/oracle/cep/test/metadata/TestDropSchema1.java /main/1 2008/10/07 18:26:23 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    10/02/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/metadata/TestDropSchema1.java /main/1 2008/10/07 18:26:23 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.metadata;

import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.CEPManager;
import oracle.cep.test.InterpDrv;
import oracle.cep.exceptions.CEPException;

public class TestDropSchema1 extends TestDropSchema
{
  public TestDropSchema1(String name)
  {
    super(name);
  }
  

 // This is tkcsfb2.cqlx  
 // First create, then drop schema, then add them again to verify they actually got dropped
  private static SchemaDesc[] s_schema2_ddls = {
  new SchemaDesc("register stream TradeInputs(tradeId integer, tradeVolume integer, tradeSymbol char(4), tradeType integer) ", "schema2"),
   new SchemaDesc("register stream TradeUpdates(tradeId integer, statusCode integer) ", "schema2"),
   new SchemaDesc("register stream TradeMatched(tradeId integer) ", "schema2"),
   new SchemaDesc("register relation TradeTypeCodes(tradeType char(10), code integer) ", "schema2"),
   new SchemaDesc("register relation TradeStatusCodes(statusCode char(20), code integer) ", "schema2"),
   new SchemaDesc("register relation Dual(c1 integer) ", "schema2"),
   new SchemaDesc("register view CutOffTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as DStream(select * from TradeInputs[range 20 seconds]) ", "schema2"),
   new SchemaDesc("register view FineTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(select c.tradeId, c.tradeVolume, c.tradeSymbol, c.tradeType from CutOffTrades[now] as c, TradeMatched[range 20 seconds] as t where c.tradeId = t.tradeId) ", "schema2"),
   new SchemaDesc("register view Dummy1(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from CutOffTrades[NOW] ", "schema2"),
   new SchemaDesc("register view Dummy2(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from FineTrades[NOW] ", "schema2"),
   new SchemaDesc("register view FailedTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(_Dummy1 except Dummy2) ", "schema2"),
   new SchemaDesc("register view TotalFineTrades(c1) as select count(*) from FineTrades[range 5 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view TotalFailedByNonZeroType(tradeType, total) as select tradeType, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeType ", "schema2"),
   new SchemaDesc("register view Dummy6(tradeType) as select tradeType from TotalFailedByNonZeroType ", "schema2"),
   new SchemaDesc("register view Dummy7(tradeType) as select code from TradeTypeCodes ", "schema2"),
   new SchemaDesc("register view Dummy8(tradeType) as Dummy7 except Dummy6 ", "schema2"),
   new SchemaDesc("register view Dummy9(tradeType, total) as select tradeType, 0 from Dummy8 ", "schema2"),
   new SchemaDesc("register view TotalFailedByType(tradeType, total) as TotalFailedByNonZeroType union all Dummy9 ", "schema2"),
   new SchemaDesc("register view TotalFailedTrades(total, dummy) as select sum(total), count(*) from TotalFailedByType ", "schema2"),
   new SchemaDesc("register view TotalTrades(c1) as select fine.c1+failed.total from TotalFineTrades as fine, TotalFailedTrades as failed ", "schema2"),
   new SchemaDesc("register view Dummy4(buyCount, sellCount) as select a.total, b.total from TotalFailedByType as a, TotalFailedByType as b where a.tradeType = 0 and b.tradeType = 1 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByTypes(buyCount, sellCount, exchgCount) as select b.buyCount, b.sellCount, a.total from TotalFailedByType as a, Dummy4 as b where a.tradeType = 2 ", "schema2"),
   new SchemaDesc("register view FailedTradesStatus(tradeId, statusCode) as IStream(select f.tradeId, u.statusCode from FailedTrades[NOW] as f, TradeUpdates[partition by u.tradeId rows 1] as u where f.tradeId = u.tradeId) ", "schema2"),
   new SchemaDesc("register view TotalFailedNonZeroByStatus12(statusCode, total) as select statusCode, count(*) from FailedTradesStatus[range 5 minutes slide 5 minutes] group by statusCode ", "schema2"),
   new SchemaDesc("register view Dummy10(statusCode) as select statusCode from TotalFailedNonZeroByStatus12 ", "schema2"),
   new SchemaDesc("register view Dummy11(statusCode) as select code from TradeStatusCodes where code = 1 ", "schema2"),
   new SchemaDesc("register view Dummy12(statusCode) as select code from TradeStatusCodes where code = 2 ", "schema2"),
   new SchemaDesc("register view Dummy13(statusCode) as Dummy11 union all Dummy12 ", "schema2"),
   new SchemaDesc("register view Dummy14(statusCode) as Dummy13 except Dummy10 ", "schema2"),
   new SchemaDesc("register view Dummy15(statusCode, total) as select statusCode, 0 from Dummy14 ", "schema2"),
   new SchemaDesc("register view TotalFailedStatus12(statusCode, total) as TotalFailedNonZeroByStatus12 union all Dummy15 ", "schema2"),
   new SchemaDesc("register view Dummy18(total, dummy) as select sum(total), count(*) from TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("register view TotalFailedStatus0(statusCode, total) as select 0, allRec.total - status12.total from TotalFailedTrades as allRec, Dummy18 as status12 ", "schema2"),
   new SchemaDesc("register view TotalFailedByStatus(statusCode, total) as TotalFailedStatus0 union all TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("register view Dummy5(status0, count0, status1, count1) as select a.statusCode, a.total, b.statusCode, b.total from TotalFailedByStatus as a, TotalFailedByStatus as b where a.statusCode = 0 and b.statusCode = 1 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByStatus(status0, count0, status1, count1, status2, count2) as select b.status0, b.count0, b.status1, b.count1, a.statusCode, a.total from TotalFailedByStatus as a, Dummy5 as b where a.statusCode = 2 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByStatusType(buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select a.buyCount, a.sellCount, a.exchgCount, b.count0, b.count1, b.count2 from TotalFailuresByTypes as a, TotalFailuresByStatus as b ", "schema2"),
   new SchemaDesc("register view TotalReq1(totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select t.c1, a.buyCount+a.sellCount+a.exchgCount, a.buyCount, a.sellCount, a.exchgCount, a.noackCount, a.recvdCount, a.processedCount from TotalFailuresByStatusType as a, TotalTrades as t ", "schema2"),
   new SchemaDesc("register view TradeStatus0(tradeId, statusCode) as select tradeId, 0 from TradeInputs ", "schema2"),
   new SchemaDesc("register view TradeStatus3(tradeId, statusCode) as select tradeId, 3 from TradeMatched ", "schema2"),
   new SchemaDesc("register view TradeStatus03(tradeId, statusCode) as TradeStatus0 union all TradeStatus3 ", "schema2"),
   new SchemaDesc("register view TradeStatusStreamDup(tradeId, statusCode) as TradeStatus03 union all TradeUpdates ", "schema2"),
   new SchemaDesc("register view TradeStatus(tradeId, statusCode) as select * from TradeStatusStreamDup[partition by tradeId rows 1] ", "schema2"),
   new SchemaDesc("register view TradeStatusStream(tradeId, statusCode) as IStream(select * from TradeStatus) ", "schema2"),
   new SchemaDesc("register view CutOff3(tradeId, statusCode) as DStream(select tradeId, statusCode from TradeStatusStream[range 3 seconds] where statusCode < 3)", "schema2"),
   new SchemaDesc("register view Failed3(tradeId, statusCode) as IStream(select a.tradeId, a.statusCode from CutOff3[NOW] as a, TradeStatus as b where a.tradeId = b.tradeId and a.statusCode = b.statusCode) ", "schema2"),
   new SchemaDesc("register view TotalCutOff3(c1) as select count(*) from CutOff3[range 10 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view TotalFailed3(c1) as select count(*) from Failed3[range 10 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view Req1(total, totalFailed, buy, sell, exchg, trade_noack, trade_recvd, trade_processing) as IStream(select * from TotalReq1) ", "schema2"),
   new SchemaDesc("register view Req2(tickerSymbol, symbolCount) as IStream(select tradeSymbol, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeSymbol) ", "schema2"),
   new SchemaDesc("register view Req3(totalCount, failedCount) as IStream(select t.c1, t.c1-f.c1 from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1)  ", "schema2"),
   new SchemaDesc("register view Req4(totalCount, failedCount) as IStream(select t.c1, f.c1 from TotalCutOff3 as t, TotalFailed3 as f where f.c1*100 > 10*t.c1) ", "schema2"),
   new SchemaDesc("create query q201 as select * from TotalFailuresByTypes ", "schema2"),
   new SchemaDesc("create query q202 as select * from TotalFailuresByStatus ", "schema2"),
   new SchemaDesc("create query q203 as select * from TotalFailuresByStatusType ", "schema2"),
   new SchemaDesc("create query q204 as select * from TotalFailedByStatus ", "schema2"),
   new SchemaDesc("create query q205 as select * from FailedTradesStatus ", "schema2"),
   new SchemaDesc("create query q206 as select * from TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("create query q207 as select * from TotalFailedStatus0 ", "schema2"),
   new SchemaDesc("create query q208 as select * from Req1 ", "schema2"),
   new SchemaDesc("create query q209 as select * from Req2 ", "schema2"),
   new SchemaDesc("create query q210 as select * from Req3 ", "schema2"),
   new SchemaDesc("create query q211 as select * from Req4 ", "schema2"),
   new SchemaDesc("drop schema schema2", "schema2"),
   new SchemaDesc("register stream TradeInputs(tradeId integer, tradeVolume integer, tradeSymbol char(4), tradeType integer) ", "schema2"),
   new SchemaDesc("register stream TradeUpdates(tradeId integer, statusCode integer) ", "schema2"),
   new SchemaDesc("register stream TradeMatched(tradeId integer) ", "schema2"),
   new SchemaDesc("register relation TradeTypeCodes(tradeType char(10), code integer) ", "schema2"),
   new SchemaDesc("register relation TradeStatusCodes(statusCode char(20), code integer) ", "schema2"),
   new SchemaDesc("register relation Dual(c1 integer) ", "schema2"),
   new SchemaDesc("register view CutOffTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as DStream(select * from TradeInputs[range 20 seconds]) ", "schema2"),
   new SchemaDesc("register view FineTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(select c.tradeId, c.tradeVolume, c.tradeSymbol, c.tradeType from CutOffTrades[now] as c, TradeMatched[range 20 seconds] as t where c.tradeId = t.tradeId) ", "schema2"),
   new SchemaDesc("register view Dummy1(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from CutOffTrades[NOW] ", "schema2"),
   new SchemaDesc("register view Dummy2(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from FineTrades[NOW] ", "schema2"),
   new SchemaDesc("register view FailedTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(_Dummy1 except Dummy2) ", "schema2"),
   new SchemaDesc("register view TotalFineTrades(c1) as select count(*) from FineTrades[range 5 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view TotalFailedByNonZeroType(tradeType, total) as select tradeType, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeType ", "schema2"),
   new SchemaDesc("register view Dummy6(tradeType) as select tradeType from TotalFailedByNonZeroType ", "schema2"),
   new SchemaDesc("register view Dummy7(tradeType) as select code from TradeTypeCodes ", "schema2"),
   new SchemaDesc("register view Dummy8(tradeType) as Dummy7 except Dummy6 ", "schema2"),
   new SchemaDesc("register view Dummy9(tradeType, total) as select tradeType, 0 from Dummy8 ", "schema2"),
   new SchemaDesc("register view TotalFailedByType(tradeType, total) as TotalFailedByNonZeroType union all Dummy9 ", "schema2"),
   new SchemaDesc("register view TotalFailedTrades(total, dummy) as select sum(total), count(*) from TotalFailedByType ", "schema2"),
   new SchemaDesc("register view TotalTrades(c1) as select fine.c1+failed.total from TotalFineTrades as fine, TotalFailedTrades as failed ", "schema2"),
   new SchemaDesc("register view Dummy4(buyCount, sellCount) as select a.total, b.total from TotalFailedByType as a, TotalFailedByType as b where a.tradeType = 0 and b.tradeType = 1 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByTypes(buyCount, sellCount, exchgCount) as select b.buyCount, b.sellCount, a.total from TotalFailedByType as a, Dummy4 as b where a.tradeType = 2 ", "schema2"),
   new SchemaDesc("register view FailedTradesStatus(tradeId, statusCode) as IStream(select f.tradeId, u.statusCode from FailedTrades[NOW] as f, TradeUpdates[partition by u.tradeId rows 1] as u where f.tradeId = u.tradeId) ", "schema2"),
   new SchemaDesc("register view TotalFailedNonZeroByStatus12(statusCode, total) as select statusCode, count(*) from FailedTradesStatus[range 5 minutes slide 5 minutes] group by statusCode ", "schema2"),
   new SchemaDesc("register view Dummy10(statusCode) as select statusCode from TotalFailedNonZeroByStatus12 ", "schema2"),
   new SchemaDesc("register view Dummy11(statusCode) as select code from TradeStatusCodes where code = 1 ", "schema2"),
   new SchemaDesc("register view Dummy12(statusCode) as select code from TradeStatusCodes where code = 2 ", "schema2"),
   new SchemaDesc("register view Dummy13(statusCode) as Dummy11 union all Dummy12 ", "schema2"),
   new SchemaDesc("register view Dummy14(statusCode) as Dummy13 except Dummy10 ", "schema2"),
   new SchemaDesc("register view Dummy15(statusCode, total) as select statusCode, 0 from Dummy14 ", "schema2"),
   new SchemaDesc("register view TotalFailedStatus12(statusCode, total) as TotalFailedNonZeroByStatus12 union all Dummy15 ", "schema2"),
   new SchemaDesc("register view Dummy18(total, dummy) as select sum(total), count(*) from TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("register view TotalFailedStatus0(statusCode, total) as select 0, allRec.total - status12.total from TotalFailedTrades as allRec, Dummy18 as status12 ", "schema2"),
   new SchemaDesc("register view TotalFailedByStatus(statusCode, total) as TotalFailedStatus0 union all TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("register view Dummy5(status0, count0, status1, count1) as select a.statusCode, a.total, b.statusCode, b.total from TotalFailedByStatus as a, TotalFailedByStatus as b where a.statusCode = 0 and b.statusCode = 1 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByStatus(status0, count0, status1, count1, status2, count2) as select b.status0, b.count0, b.status1, b.count1, a.statusCode, a.total from TotalFailedByStatus as a, Dummy5 as b where a.statusCode = 2 ", "schema2"),
   new SchemaDesc("register view TotalFailuresByStatusType(buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select a.buyCount, a.sellCount, a.exchgCount, b.count0, b.count1, b.count2 from TotalFailuresByTypes as a, TotalFailuresByStatus as b ", "schema2"),
   new SchemaDesc("register view TotalReq1(totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select t.c1, a.buyCount+a.sellCount+a.exchgCount, a.buyCount, a.sellCount, a.exchgCount, a.noackCount, a.recvdCount, a.processedCount from TotalFailuresByStatusType as a, TotalTrades as t ", "schema2"),
   new SchemaDesc("register view TradeStatus0(tradeId, statusCode) as select tradeId, 0 from TradeInputs ", "schema2"),
   new SchemaDesc("register view TradeStatus3(tradeId, statusCode) as select tradeId, 3 from TradeMatched ", "schema2"),
   new SchemaDesc("register view TradeStatus03(tradeId, statusCode) as TradeStatus0 union all TradeStatus3 ", "schema2"),
   new SchemaDesc("register view TradeStatusStreamDup(tradeId, statusCode) as TradeStatus03 union all TradeUpdates ", "schema2"),
   new SchemaDesc("register view TradeStatus(tradeId, statusCode) as select * from TradeStatusStreamDup[partition by tradeId rows 1] ", "schema2"),
   new SchemaDesc("register view TradeStatusStream(tradeId, statusCode) as IStream(select * from TradeStatus) ", "schema2"),
   new SchemaDesc("register view CutOff3(tradeId, statusCode) as DStream(select tradeId, statusCode from TradeStatusStream[range 3 seconds] where statusCode < 3)", "schema2"),
   new SchemaDesc("register view Failed3(tradeId, statusCode) as IStream(select a.tradeId, a.statusCode from CutOff3[NOW] as a, TradeStatus as b where a.tradeId = b.tradeId and a.statusCode = b.statusCode) ", "schema2"),
   new SchemaDesc("register view TotalCutOff3(c1) as select count(*) from CutOff3[range 10 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view TotalFailed3(c1) as select count(*) from Failed3[range 10 minutes slide 5 minutes] ", "schema2"),
   new SchemaDesc("register view Req1(total, totalFailed, buy, sell, exchg, trade_noack, trade_recvd, trade_processing) as IStream(select * from TotalReq1) ", "schema2"),
   new SchemaDesc("register view Req2(tickerSymbol, symbolCount) as IStream(select tradeSymbol, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeSymbol) ", "schema2"),
   new SchemaDesc("register view Req3(totalCount, failedCount) as IStream(select t.c1, t.c1-f.c1 from TotalTrades as t, TotalFineTrades as f where f.c1*100 < 95*t.c1)  ", "schema2"),
   new SchemaDesc("register view Req4(totalCount, failedCount) as IStream(select t.c1, f.c1 from TotalCutOff3 as t, TotalFailed3 as f where f.c1*100 > 10*t.c1) ", "schema2"),
   new SchemaDesc("create query q201 as select * from TotalFailuresByTypes ", "schema2"),
   new SchemaDesc("create query q202 as select * from TotalFailuresByStatus ", "schema2"),
   new SchemaDesc("create query q203 as select * from TotalFailuresByStatusType ", "schema2"),
   new SchemaDesc("create query q204 as select * from TotalFailedByStatus ", "schema2"),
   new SchemaDesc("create query q205 as select * from FailedTradesStatus ", "schema2"),
   new SchemaDesc("create query q206 as select * from TotalFailedStatus12 ", "schema2"),
   new SchemaDesc("create query q207 as select * from TotalFailedStatus0 ", "schema2"),
   new SchemaDesc("create query q208 as select * from Req1 ", "schema2"),
   new SchemaDesc("create query q209 as select * from Req2 ", "schema2"),
   new SchemaDesc("create query q210 as select * from Req3 ", "schema2"),
   new SchemaDesc("create query q211 as select * from Req4 ", "schema2"),
  };
  
  public void testDropSchema1()
  {
    String[] schemas = {"schema2"};
    runDDLs(s_schema2_ddls);
    dropSchema(schemas);
    verify(schemas, null);
  }
  
  public void testDropSchema2()
  {
    runDDLs(s_schema1_ddls);
    runDDLs(s_schema2_ddls);
    String[] schemas = {"schema1", "schema2"};
    dropSchema(schemas);
    verify(schemas, null);
  }
    
  public void main(String[] args)
  {
      junit.textui.TestRunner.run(TestDropSchema1.class);
  }
}
