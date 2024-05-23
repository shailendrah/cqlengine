<CEP xmlns="http://xmlns.oracle.com/cep">
<CEP_DDL> create stream tklog_S (c1 integer, c2 float) is application timestamped </CEP_DDL>
<CEP_DDL> create stream tklog_S1 (c1 integer) </CEP_DDL>
<CEP_DDL> create relation tklog_R (d1 integer, d2 char(10)) is silent </CEP_DDL>
<CEP_DDL> create relation tklog_R1 (c1 integer, c2 float) is silent </CEP_DDL>
<CEP_DDL> <![CDATA[ alter stream tklog_S add source "<EndPointReference><Address>file://@TEST_DATA@/inpS1.txt?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> <![CDATA[ alter stream tklog_S1 add source "<EndPointReference><Address>file://@TEST_DATA@/inpS1_data1.txt?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> <![CDATA[ alter relation tklog_R add source "<EndPointReference><Address>file://@TEST_DATA@/inpR.txt?convertTs=fase</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> <![CDATA[ alter relation tklog_R1 add source "<EndPointReference><Address>file://@TEST_DATA@/inpR-static.txt?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> create view tklog_v1 (c1 integer, c2 float) as select * from tklog_S[range 1000 nanoseconds] </CEP_DDL>
<CEP_DDL> create view tklog_v2 (c1 integer) as IStream(select * from tklog_S1[range 10000 nanoseconds slide 10000 nanoseconds]) </CEP_DDL>
<CEP_DDL> create query tklog_q1 as select * from tklog_R, tklog_S[range 1000 nanoseconds] where tklog_S.c1 = tklog_R.d1 </CEP_DDL>
<CEP_DDL> create query tklog_q2 as tklog_R1 union all tklog_v1 </CEP_DDL>
<CEP_DDL> create query tklog_q3 as tklog_R1 except tklog_v1 </CEP_DDL>
<CEP_DDL> <![CDATA[ alter query tklog_q1 add destination "<EndPointReference><Address>file://@TEST_OUTPUT@/tklog_outSR1-static.log?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> <![CDATA[ alter query tklog_q2 add destination "<EndPointReference><Address>file://@TEST_OUTPUT@/tklog_outSR1-union-static.log?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> <![CDATA[ alter query tklog_q3 add destination "<EndPointReference><Address>file://@TEST_OUTPUT@/tklog_outSR1-except-static.log?convertTs=false</Address></EndPointReference>"]]>  </CEP_DDL>
<CEP_DDL> alter query tklog_q1 start </CEP_DDL>
<CEP_DDL> alter query tklog_q2 start </CEP_DDL>
<CEP_DDL> alter query tklog_q3 start </CEP_DDL>
</CEP>
