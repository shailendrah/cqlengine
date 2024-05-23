create stream rfidStream(epcisMsg xmltype);

alter stream rfidStream add source push;

create view FilterByPos (epcMsg xmltype) as SELECT * from rfidStream where XMLExists('for $i in /ObjectEvent where $i/bizStep = $x return $i' passing by value rfidStream.epcisMsg as ".", "urn:epcglobal:hls:bizstep:pos" as "x" RETURNING CONTENT) XMLData = true;

create view epcItem(epcItemURN char(128)) as select X.epcItemURN from FilterByPos XMLTable ('//epc' PASSING BY VALUE FilterByPos.epcMsg as "." COLUMNS epcItemURN char(128) PATH 'fn:data(.)') AS X;

create query q1 as select * from epcItem;
alter query q1 add destination "<EndPointReference><Address>file:///tmp/q1.txt</Address></EndPointReference>" ;
alter query q1 start ;


create view v1 (c1 integer) as select count(*) from epcItem[range 1];
create view v2 (epcItem char(128)) as select * from epcItem[range 1];
create view v3 (c2 integer) as select 1 from v1 where v1.c1 > 1; 
create view v4 (epcitem char(128)) as IStream(select epcItem from v2,v3); 

create query q2 as select count(*) from epcItem[range 1];
alter query q2 add destination "<EndPointReference><Address>file:///tmp/q2.txt</Address></EndPointReference>" ;
alter query q2 start ;

create query q3 as select * from v4; 
alter query q3 add destination "<EndPointReference><Address>file:///tmp/q3.txt</Address></EndPointReference>" ;
alter query q3 start ;

create query q4 as select * from v4; 
alter query q4 add destination "<EndPointReference><Address>jms:jms/ConnectionFactory:jms/demoTopic1</Address></EndPointReference>" ;
alter query q4 start;

alter system run duration=0;

