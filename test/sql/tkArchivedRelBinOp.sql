Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedRelBinOp.sql /main/3 2015/05/10 20:30:30 udeshmuk Exp $
Rem
Rem tkArchivedRelBinOp.sql
Rem
Rem Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
Rem All rights reserved.
Rem
Rem    NAME
Rem      tkArchivedRelBinOp.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    udeshmuk    05/10/15 - add details for binoprighttable4
Rem    udeshmuk    02/11/15 - add another table used in tkArchivedDimJoin5
Rem    udeshmuk    09/08/11 - DDL and DML for tkArchivedRelBinOp.cqlx
Rem    udeshmuk    09/08/11 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table BinOpLeftTestTable;
drop table BinOpRightTestTable;
drop table BinOpRightTestTable2;
drop table BinOpRightTestTable3;
drop table BinOpRightTestTable4;

create table BinOpLeftTestTable(c1 integer, c2 float, c3 number(19,0), c4 varchar2(10));
create table BinOpRightTestTable(c1 integer, c2 float,c3 number(19,0), c4 varchar2(10));
create table BinOpRightTestTable2(c3 number(19,0), c4 varchar2(10));
create table BinOpRightTestTable3(c1 integer, c3 number(19,0));
create table BinOpRightTestTable4(c1 integer, c11 float, c111 number(19,0), c1111 varchar2(10));

insert into BinOpLeftTestTable values (10, 1.5, 1, 'first');
insert into BinOpLeftTestTable values(20,3, 2, 'second');
insert into BinOpLeftTestTable values(30, 4.5, 3, 'third');
insert into BinOpLeftTestTable values(10, 6, 4, 'first');
insert into BinOpLeftTestTable values(20, 7.5, 5, 'fourth');
insert into BinOpLeftTestTable values(30,4.5, 6, 'third');

insert into BinOpRightTestTable values(10,1.5, 1, 'first');
insert into BinOpRightTestTable values(25,3, 2, 'fifth');
insert into BinOpRightTestTable values(30, 4.5, 3, 'third');
insert into BinOpRightTestTable values(20, 3, 4, 'second');
insert into BinOpRightTestTable values(20, 6, 5, 'fourth');

insert into BinOpRightTestTable2 values(1, 'first');
insert into BinOpRightTestTable2 values(2, 'second');

insert into BinOpRightTestTable3 values(1, 1);
insert into BinOpRightTestTable3 values(20, 2);
insert into BinOpRightTestTable3 values(10, 3);

insert into BinOpRightTestTable4 values(10,1.5, 1, 'first');
insert into BinOpRightTestTable4 values(25,3, 2, 'fifth');
insert into BinOpRightTestTable4 values(30, 4.5, 3, 'third');
insert into BinOpRightTestTable4 values(20, 3, 4, 'second');
insert into BinOpRightTestTable4 values(20, 6, 5, 'fourth');

commit;
