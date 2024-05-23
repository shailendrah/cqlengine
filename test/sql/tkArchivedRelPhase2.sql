Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedRelPhase2.sql /main/3 2012/07/10 10:59:30 udeshmuk Exp $
Rem
Rem tkArchivedRelPhase2.sql
Rem
Rem Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
Rem All rights reserved. 
Rem
Rem    NAME
Rem      tkArchivedRelPhase2.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    udeshmuk    07/03/12 - add calculation do
Rem    udeshmuk    06/17/12 - add batch do
Rem    udeshmuk    09/08/11 - DDL and DML for tkArchivedRelPhase2.cqlx
Rem    udeshmuk    09/08/11 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table Phase2TestTable;
drop table batch_table;
drop table calculation_do;

create table Phase2TestTable(c1 integer, c2 float, c3 varchar2(10), eid number(19,0));
create table batch_table(BEAM_ID number(19,0), intc integer);
create table calculation_do(varc varchar(10), datc timestamp, intc integer, floatc float, beam_id number(19,0));

insert into Phase2TestTable values(10, 1.5, 'first', 1);
insert into Phase2TestTable values(20, 3.0, 'second', 2);
insert into Phase2TestTable values(30, 4.5 , 'third', 3);
insert into Phase2TestTable values(25, 3.25, 'first', 4);
insert into Phase2TestTable values (35, 5.75, 'third', 5);
insert into Phase2TestTable values( 30, 3.5, 'third', 6);
insert into Phase2TestTable values(25, 4.25, 'first', 7);
insert into Phase2TestTable values(20, 2.25, 'second', 8);
insert into Phase2TestTable values (10, null, 'first', 9); 

insert into batch_table values(1,1);
insert into batch_table values(2,2);

insert into calculation_Do values('Happy', TIMESTAMP '2012-12-30 01:22:45', 1, 2.5, 1);
insert into calculation_Do values('UnHappy', TIMESTAMP '2012-06-16 01:23:45', 2, 3.6, 2);

commit;
