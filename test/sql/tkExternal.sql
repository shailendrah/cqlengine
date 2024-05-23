Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkExternal.sql /main/8 2015/11/02 17:02:25 sbishnoi Exp $
Rem
Rem tkExternal.sql
Rem
Rem Copyright (c) 2007, 2015, Oracle and/or its affiliates. 
Rem All rights reserved.
Rem
Rem    NAME
Rem      tkExternal.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    sbishnoi    06/03/09 - adding tables tkExtOuterJoin_R1 and
Rem                           tkExtOuterJoin_R2
Rem    sbishnoi    01/18/09 - adding table tkExternal_R2
Rem    hopark      06/27/08 - rename table
Rem    parujain    12/10/07 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table R;

drop table tkExternal_R1;

drop table tkExternal_R2;

drop table tkExternal_R3;

drop table tkExternal_R4;

drop table tkNegExternal_R3;

drop table R4;

drop table tkNegExternal_R5;

drop table tkExtOuterJoin_R1;

drop table tkExtOuterJoin_R2;

CREATE TABLE R (
 d1  integer,
 d2  float );

insert into R values (2, 3.4);

insert into R values (10, 2.1);

insert into R values (13, 2.5);

commit;

create table tkExternal_R1 (
  d1  timestamp with time zone,
  d2  interval day to second(5));

insert into tkExternal_R1 values ('07-AUG-04 11.13.48.000000 AM -07:00',interval '4 11:13:48.10000' DAY TO SECOND);

insert into tkExternal_R1 values ('07-AUG-06 11.13.48.000000 AM -07:00',interval '6 01:03:45.10000' DAY TO SECOND);

insert into tkExternal_R1 values ('19-FEB-04 09.00.00.000000 AM -07:00',interval '4 05:12:10.22200' DAY TO SECOND);

commit;

create table tkExternal_R2 (
  d1  integer,
  d2  number,
  d3  varchar2(100));

insert into tkExternal_R2 values (1, 10, 'Terry');
insert into tkExternal_R2 values (3, 15, 'Alex');
insert into tkExternal_R2 values (5, 20, 'Rahul');
insert into tkExternal_R2 values (7, 25, 'Paes');
insert into tkExternal_R2 values (9, 30, 'Saurabh');

commit;


create table tkExternal_R3 (
  d1  timestamp with time zone,
  d2  varchar2(100));

insert into tkExternal_R3 values ('07-AUG-06 11.13.48.000000 AM -07:00','abc');

insert into tkExternal_R3 values ('07-AUG-04 11.13.48.000000 AM -07:00','ab');

commit;

create table tkExternal_R4 (
  d1  varchar2(10),
  d2  varchar2(30));

insert into tkExternal_R4 values ('ORCL','Oracle Inc');

insert into tkExternal_R4 values ('MSFT','Microsoft Corp');

commit;

create table tkNegExternal_R3 (
  d1  timestamp with time zone,
  d2  varchar2(100));

insert into tkNegExternal_R3 values ('07-AUG-06 11.13.48.000000 AM -07:00','abc');

insert into tkNegExternal_R3 values ('07-AUG-04 11.13.48.000000 AM -07:00','ab');

commit;

create table R4 (
  c1  integer,
  c2  raw(10));

insert into R4 values(1, 'abc1');

insert into R4 values(3, 'abc3');

insert into R4 values(5, '1234');

commit;

create table tkNegExternal_R5 (
  d1  integer,
  d2  float);

insert into tkNegExternal_R5 values (null, 2.1);

insert into tkNegExternal_R5 values (2, null);

insert into tkNegExternal_R5 values (3, 3.1);

insert into tkNegExternal_R5 values (null, null);

commit;

create table tkExtOuterJoin_R1(
  c1 integer);

insert into tkExtOuterJoin_R1 values (5);

insert into tkExtOuterJoin_R1 values (6);

insert into tkExtOuterJoin_R1 values (4);

commit;

create table tkExtOuterJoin_R2(
  c1 integer,
  c2 integer);

insert into tkExtOuterJoin_R2 values (1,6);

insert into tkExtOuterJoin_R2 values (4,5);

insert into tkExtOuterJoin_R2 values (5,5);

insert into tkExtOuterJoin_R2 values (6,4);

insert into tkExtOuterJoin_R2 values (6,1);

commit;

create table tkExtTimeZone_R1(c1 timestamp with time zone, c2 varchar(20));

insert into tkExtTimeZone_R1 (c1,c2) select to_timestamp_tz('05-Oct-13 03.16.00.756000 PM  +0530','DD-MON-YY HH.MI.SS.FF6 AM TZH:TZM'),'withTZandTrailZeroes' from dual;

insert into tkExtTimeZone_R1 (c1,c2) select to_timestamp_tz('05-Oct-13 03.16.00.000756 PM  +0530','DD-MON-YY HH.MI.SS.FF6 AM TZH:TZM'),'withTZandFwdZeroes' from dual;

commit;

create table tkExtTimeZone_R2(c1 timestamp , c2 varchar(20));

insert into tkExtTimeZone_R2 (c1,c2) select to_timestamp_tz('05-Oct-13 03.16.00.756000 PM','DD-MON-YY HH.MI.SS.FF6 AM') ,'withoutTZandTrail0s' from dual;

insert into tkExtTimeZone_R2 (c1,c2) select to_timestamp_tz('05-Oct-13 03.16.00.000756 PM','DD-MON-YY HH.MI.SS.FF6 AM') ,'withoutTZandFwd0s' from dual;

commit;

exit;

