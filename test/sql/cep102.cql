<?xml version="1.0" ?>
   <CEP xmlns="http://xmlns.oracle.com/cep">
        <CEP_LOAD>
             <CEP_DDL> register stream TradeInputs(tradeId integer, tradeVolume integer, tradeSymbol char(4), tradeType integer) </CEP_DDL>
             <CEP_DDL> register stream TradeUpdates(tradeId integer, statusCode integer) </CEP_DDL>
             <CEP_DDL> register stream TradeMatched(tradeId integer) </CEP_DDL>
             <!--
             <CEP_DDL> alter stream TradeInputs add source push </CEP_DDL>
             <CEP_DDL> alter stream TradeUpdates add source push </CEP_DDL>
             <CEP_DDL> alter stream TradeMatched add source push </CEP_DDL>
             -->
             <CEP_DDL> register relation TradeTypeCodes(tradeType char(10), code integer) </CEP_DDL>
             <CEP_DDL> register relation TradeStatusCodes(statusCode char(20), code integer) </CEP_DDL>
             
              <CEP_DDL> <![CDATA[ alter stream TradeInputs add push source "<EndPointReference><Address>file://@TEST_DATA@/tkcep102inpTI.txt</Address></EndPointReference>"]]>  </CEP_DDL>                
              <CEP_DDL> <![CDATA[ alter stream TradeUpdates add push source "<EndPointReference><Address>file://@TEST_DATA@/tkcep102inpTU.txt</Address></EndPointReference>"]]>  </CEP_DDL>
              <CEP_DDL> <![CDATA[ alter stream TradeMatched add push source "<EndPointReference><Address>file://@TEST_DATA@/tkcep102inpTM.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter relation TradeTypeCodes add source "<EndPointReference><Address>file://@TEST_DATA@/tkcep102inpTT1.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter relation TradeStatusCodes add source "<EndPointReference><Address>file://@TEST_DATA@/tkcep102inpTS1.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL> register view CutOffTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as DStream(select * from TradeInputs[range 20 seconds]) </CEP_DDL>
             <CEP_DDL> register view FineTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(select c.tradeId, c.tradeVolume, c.tradeSymbol, c.tradeType from CutOffTrades[now] as c, TradeMatched[range 20 seconds] as t where c.tradeId = t.tradeId) </CEP_DDL>
             <CEP_DDL> register view Dummy1(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from CutOffTrades[NOW] </CEP_DDL>
             <CEP_DDL> register view Dummy2(tradeId, tradeVolume, tradeSymbol, tradeType) as select * from FineTrades[NOW] </CEP_DDL>
             <CEP_DDL> register view FailedTrades(tradeId, tradeVolume, tradeSymbol, tradeType) as IStream(Dummy1 except Dummy2) </CEP_DDL>
             <CEP_DDL> register view TotalFineTrades(c1) as select count(*) from FineTrades[range 5 minutes slide 5 minutes] </CEP_DDL>
             <CEP_DDL> register view TotalFailedByNonZeroType(tradeType, total) as select tradeType, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeType </CEP_DDL>
             <CEP_DDL> register view Dummy6(tradeType) as select tradeType from TotalFailedByNonZeroType </CEP_DDL>
             <CEP_DDL> register view Dummy7(tradeType) as select code from TradeTypeCodes </CEP_DDL>
             <CEP_DDL> register view Dummy8(tradeType) as Dummy7 except Dummy6 </CEP_DDL>
             <CEP_DDL> register view Dummy9(tradeType, total) as select tradeType, 0 from Dummy8 </CEP_DDL>
             <CEP_DDL> register view TotalFailedByType(tradeType, total) as TotalFailedByNonZeroType union all Dummy9 </CEP_DDL>
             <CEP_DDL> register view TotalFailedTrades(total, dummy) as select sum(total), count(*) from TotalFailedByType </CEP_DDL>
             <CEP_DDL> register view TotalTrades(c1) as select fine.c1+failed.total from TotalFineTrades as fine, TotalFailedTrades as failed </CEP_DDL>
             <CEP_DDL> register view Dummy4(buyCount, sellCount) as select a.total, b.total from TotalFailedByType as a, TotalFailedByType as b where a.tradeType = 0 and b.tradeType = 1 </CEP_DDL>
             <CEP_DDL> register view TotalFailuresByTypes(buyCount, sellCount, exchgCount) as select b.buyCount, b.sellCount, a.total from TotalFailedByType as a, Dummy4 as b where a.tradeType = 2 </CEP_DDL>
             <CEP_DDL> register view FailedTradesStatus(tradeId, statusCode) as IStream(select f.tradeId, u.statusCode from FailedTrades[NOW] as f, TradeUpdates[partition by u.tradeId rows 1 range 20 seconds] as u where f.tradeId = u.tradeId) </CEP_DDL>
             <CEP_DDL> register view TotalFailedNonZeroByStatus12(statusCode, total) as select statusCode, count(*) from FailedTradesStatus[range 5 minutes slide 5 minutes] group by statusCode </CEP_DDL>
             <CEP_DDL> register view Dummy10(statusCode) as select statusCode from TotalFailedNonZeroByStatus12 </CEP_DDL>
             <CEP_DDL> register view Dummy11(statusCode) as select code from TradeStatusCodes where code = 1 </CEP_DDL>
             <CEP_DDL> register view Dummy12(statusCode) as select code from TradeStatusCodes where code = 2 </CEP_DDL>
             <CEP_DDL> register view Dummy13(statusCode) as Dummy11 union all Dummy12 </CEP_DDL>
             <CEP_DDL> register view Dummy14(statusCode) as Dummy13 except Dummy10 </CEP_DDL>
             <CEP_DDL> register view Dummy15(statusCode, total) as select statusCode, 0 from Dummy14 </CEP_DDL>
             <CEP_DDL> register view TotalFailedStatus12(statusCode, total) as TotalFailedNonZeroByStatus12 union all Dummy15 </CEP_DDL>
             <CEP_DDL> register view Dummy18(total, dummy) as select sum(total), count(*) from TotalFailedStatus12 </CEP_DDL>
             <CEP_DDL> register view TotalFailedStatus0(statusCode, total) as select 0, allRec.total - status12.total from TotalFailedTrades as allRec, Dummy18 as status12 </CEP_DDL>
             <CEP_DDL> register view TotalFailedByStatus(statusCode, total) as TotalFailedStatus0 union all TotalFailedStatus12 </CEP_DDL>
             <CEP_DDL> register view Dummy5(status0, count0, status1, count1) as select a.statusCode, a.total, b.statusCode, b.total from TotalFailedByStatus as a, TotalFailedByStatus as b where a.statusCode = 0 and b.statusCode = 1 </CEP_DDL>
             <CEP_DDL> register view TotalFailuresByStatus(status0, count0, status1, count1, status2, count2) as select b.status0, b.count0, b.status1, b.count1, a.statusCode, a.total from TotalFailedByStatus as a, Dummy5 as b where a.statusCode = 2 </CEP_DDL>
             <CEP_DDL> register view TotalFailuresByStatusType(buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select a.buyCount, a.sellCount, a.exchgCount, b.count0, b.count1, b.count2 from TotalFailuresByTypes as a, TotalFailuresByStatus as b </CEP_DDL>
             <CEP_DDL> register view TotalReq1(totalCount, totalFailedCount, buyCount, sellCount, exchgCount, noackCount, recvdCount, processedCount) as select t.c1, a.buyCount+a.sellCount+a.exchgCount, a.buyCount, a.sellCount, a.exchgCount, a.noackCount, a.recvdCount, a.processedCount from TotalFailuresByStatusType as a, TotalTrades as t </CEP_DDL>
             <CEP_DDL> register view TradeStatus0(tradeId, statusCode) as select tradeId, 0 from TradeInputs </CEP_DDL>
             <CEP_DDL> register view TradeStatus3(tradeId, statusCode) as select tradeId, 3 from TradeMatched </CEP_DDL>
             <CEP_DDL> register view TradeStatus03(tradeId, statusCode) as TradeStatus0 union all TradeStatus3 </CEP_DDL>
             <CEP_DDL> register view TradeStatusStreamDup(tradeId, statusCode) as TradeStatus03 union all TradeUpdates </CEP_DDL>
             <CEP_DDL> register view TradeStatus(tradeId, statusCode) as select * from TradeStatusStreamDup[partition by tradeId rows 1 range 4 seconds] </CEP_DDL>
             <CEP_DDL> register view TradeStatusStream(tradeId, statusCode) as IStream(select * from TradeStatus) </CEP_DDL>
             <CEP_DDL> register view CutOff3(tradeId, statusCode) as DStream(select tradeId, statusCode from TradeStatusStream[range 3 seconds] where 3 > statusCode) </CEP_DDL>
             <CEP_DDL> register view Failed3(tradeId, statusCode) as IStream(select a.tradeId, a.statusCode from CutOff3[NOW] as a, TradeStatus as b where a.tradeId = b.tradeId and a.statusCode = b.statusCode) </CEP_DDL>
             <CEP_DDL> register view TotalCutOff3(c1) as select count(*) from CutOff3[range 10 minutes slide 5 minutes] </CEP_DDL>
             <CEP_DDL> register view TotalFailed3(c1) as select count(*) from Failed3[range 10 minutes slide 5 minutes] </CEP_DDL>
             <CEP_DDL> register view Req1(total, totalFailed, buy, sell, exchg, trade_noack, trade_recvd, trade_processing) as IStream(select * from TotalReq1) </CEP_DDL>
             <CEP_DDL> register view Req2(tickerSymbol, symbolCount) as IStream(select tradeSymbol, count(*) from FailedTrades[range 5 minutes slide 5 minutes] group by tradeSymbol) </CEP_DDL>
             <CEP_DDL> register view Req3(totalCount, failedCount) as IStream(select t.c1, t.c1-f.c1 from TotalTrades as t, TotalFineTrades as f where 95*t.c1 > f.c1*100)</CEP_DDL>
             <CEP_DDL> register view Req4(totalCount, failedCount) as IStream(select t.c1, f.c1 from TotalCutOff3 as t, TotalFailed3 as f where f.c1*100 > 10*t.c1) </CEP_DDL>
             <CEP_DDL> register query qti as select tradeId , tradeVolume , tradeSymbol , tradeType, 0 as statusCode from TradeInputs  [NOW] </CEP_DDL>
             <CEP_DDL> register query qtu as select tradeId , statusCode  as  statusCode from TradeUpdates [NOW] </CEP_DDL>
             <CEP_DDL> register query qtm as select tradeId, 3 as statusCode from TradeMatched [NOW] </CEP_DDL>
             <CEP_DDL> register query qft as select tradeId, 4 as statusCode from FailedTrades [NOW] </CEP_DDL>
             <CEP_DDL> register query qtf as select tradeId, 5 as statusCode from FineTrades [NOW] </CEP_DDL>
             <CEP_DDL> create query qreq2 as select 2 as reqno, tickerSymbol, symbolCount as totalCount from Req2 </CEP_DDL>
             <CEP_DDL> create query qreq3 as select 3 as reqno, totalCount as totalCount, failedCount, 1 as statusCode from Req3 </CEP_DDL>
             <CEP_DDL> create query qreq4 as select 4 as reqno, totalCount as totalCount, failedCount, 1 as statusCode from Req4 </CEP_DDL>
             <CEP_DDL> create query qreq1 as select 1 as reqno, total as totalCount, totalFailed,  buy, sell, exchg as exchange, trade_noack, trade_recvd, trade_processing, 1 as statusCode from Req1 </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter query qti add destination "<EndPointReference> <Address>file://@TEST_OUTPUT@/CEPMonitorTI.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter query qtu add destination "<EndPointReference> <Address>file://@TEST_OUTPUT@/CEPMonitorTU.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter query qtm add destination "<EndPointReference> <Address>file://@TEST_OUTPUT@/CEPMonitorTM.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL>  <![CDATA[ alter query qreq1 add destination "<EndPointReference> <Address>file://@TEST_OUTPUT@/CEPMonitorR1.txt</Address></EndPointReference>"]]>  </CEP_DDL>
             <CEP_DDL> alter query qti start </CEP_DDL>
             <CEP_DDL> alter query qtu start </CEP_DDL>
             <CEP_DDL> alter query qtm start </CEP_DDL>
             <CEP_DDL> alter query qreq1 start </CEP_DDL>
             <CEP_DDL> alter system run</CEP_DDL>
   </CEP_LOAD>
        <CEP_UNLOAD>
             <CEP_DDL> drop query qreq1 </CEP_DDL>
             <CEP_DDL> drop query qti </CEP_DDL>
             <CEP_DDL> drop query qtu </CEP_DDL>
             <CEP_DDL> drop query qtm </CEP_DDL>
             <CEP_DDL> drop view Req4 </CEP_DDL>
             <CEP_DDL> drop view Req3 </CEP_DDL>
             <CEP_DDL> drop view Req2 </CEP_DDL>
             <CEP_DDL> drop view Req1 </CEP_DDL>
             <CEP_DDL> drop view TotalFailed3 </CEP_DDL>
             <CEP_DDL> drop view TotalCutOff3 </CEP_DDL>
             <CEP_DDL> drop view Failed3 </CEP_DDL>
             <CEP_DDL> drop view CutOff3 </CEP_DDL>
             <CEP_DDL> drop view TradeStatusStream </CEP_DDL>
             <CEP_DDL> drop view TradeStatus </CEP_DDL>
             <CEP_DDL> drop view TradeStatusStreamDup </CEP_DDL>
             <CEP_DDL> drop view TradeStatus03 </CEP_DDL>
             <CEP_DDL> drop view TradeStatus3 </CEP_DDL>
             <CEP_DDL> drop view TradeStatus0 </CEP_DDL>
             <CEP_DDL> drop view TotalReq1 </CEP_DDL>
             <CEP_DDL> drop view TotalFailuresByStatusType </CEP_DDL>
             <CEP_DDL> drop view TotalFailuresByStatus </CEP_DDL>
             <CEP_DDL> drop view Dummy5 </CEP_DDL>
             <CEP_DDL> drop view TotalFailedByStatus </CEP_DDL>
             <CEP_DDL> drop view TotalFailedStatus0 </CEP_DDL>
             <CEP_DDL> drop view Dummy18 </CEP_DDL>
             <CEP_DDL> drop view TotalFailedStatus12 </CEP_DDL>
             <CEP_DDL> drop view Dummy15 </CEP_DDL>
             <CEP_DDL> drop view Dummy14 </CEP_DDL>
             <CEP_DDL> drop view Dummy13 </CEP_DDL>
             <CEP_DDL> drop view Dummy12 </CEP_DDL>
             <CEP_DDL> drop view Dummy11 </CEP_DDL>
             <CEP_DDL> drop view Dummy10 </CEP_DDL>
             <CEP_DDL> drop view TotalFailedNonZeroByStatus12 </CEP_DDL>
             <CEP_DDL> drop view FailedTradesStatus </CEP_DDL>
             <CEP_DDL> drop view TotalFailuresByTypes </CEP_DDL>
             <CEP_DDL> drop view Dummy4 </CEP_DDL>
             <CEP_DDL> drop view TotalTrades </CEP_DDL>
             <CEP_DDL> drop view TotalFailedTrades </CEP_DDL>
             <CEP_DDL> drop view TotalFailedByType </CEP_DDL>
             <CEP_DDL> drop view Dummy9 </CEP_DDL>
             <CEP_DDL> drop view Dummy8 </CEP_DDL>
             <CEP_DDL> drop view Dummy7 </CEP_DDL>
             <CEP_DDL> drop view Dummy6 </CEP_DDL>
             <CEP_DDL> drop view TotalFailedByNonZeroType </CEP_DDL>
             <CEP_DDL> drop view TotalFineTrades </CEP_DDL>
             <CEP_DDL> drop view FailedTrades </CEP_DDL>
             <CEP_DDL> drop view Dummy2 </CEP_DDL>
             <CEP_DDL> drop view Dummy1 </CEP_DDL>
             <CEP_DDL> drop view FineTrades </CEP_DDL>
             <CEP_DDL> drop view CutOffTrades </CEP_DDL>
             <CEP_DDL> drop relation TradeStatusCodes </CEP_DDL>
             <CEP_DDL> drop relation TradeTypeCodes </CEP_DDL>
             <CEP_DDL> drop stream TradeInputs </CEP_DDL>
             <CEP_DDL> drop stream TradeUpdates </CEP_DDL>
             <CEP_DDL> drop stream TradeMatched </CEP_DDL>
   </CEP_UNLOAD>
</CEP>

