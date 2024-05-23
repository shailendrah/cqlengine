<CEP xmlns="http://xmlns.oracle.com/cep">
    <CEP_DDL>register stream SimpleEvent1(orderId integer, orderAmount float)</CEP_DDL>
    <CEP_DDL> <![CDATA[ alter stream SimpleEvent1 add source "<EndPointReference><Address>file://@TEST_DATA@/inpS4.txt</Address></EndPointReference>"]]> </CEP_DDL>
    <CEP_DDL> create function doubleOrderAmount(c1 float) return float as language java name "oracle.cep.test.userfunctions.TkUsrFunction" </CEP_DDL>
    <CEP_DDL> create function sumOrder(c1 float) return float aggregate using "oracle.cep.test.userfunctions.TkSum"</CEP_DDL>
    <CEP_DDL>register query q1 as select orderId as orderId, sumOrder(orderAmount) as totalOrderAmount  from SimpleEvent1[rows 1] group by orderId</CEP_DDL>
    <CEP_DDL>register query q2 as select orderId as orderId, doubleOrderAmount(orderAmount) from SimpleEvent1[rows 1]</CEP_DDL>
    <CEP_DDL>alter query q1 add destination &quot;&lt;EndPointReference&gt;&lt;Address&gt;file://@TEST_OUTPUT@/simple1.out&lt;/Address&gt;&lt;/EndPointReference&gt;&quot;</CEP_DDL>
    <CEP_DDL>alter query q2 add destination &quot;&lt;EndPointReference&gt;&lt;Address&gt;file://@TEST_OUTPUT@/simple2.out&lt;/Address&gt;&lt;/EndPointReference&gt;&quot;</CEP_DDL>
    <CEP_DDL>alter query q1 start</CEP_DDL>
    <CEP_DDL>alter query q2 start</CEP_DDL>
     <CEP_DDL>alter system run</CEP_DDL>
</CEP>
