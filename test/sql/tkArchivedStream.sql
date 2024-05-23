Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedStream.sql /main/1 2012/02/02 19:27:26 udeshmuk Exp $
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

drop table stream_do;

create table stream_do(c1 integer, c2 float, c3 number(19), c4 timestamp(6));

insert into stream_do values(1, 2.46, 700000000, '27-JAN-12 04.05.20.000000000 PM');
insert into stream_do values(2, 4.64, 1050000000, '23-JAN-12 09.32.53.000000000 AM');
insert into stream_do values(3, 24.75, 900000000, '26-JAN-12 11.15.45.000000000 AM');
insert into stream_do values(4, 70.5, 850000000, '26-JAN-12 08.05.16.000000000 AM');
insert into stream_do values(5, 1.05, 1200000000, '28-JAN-12 04.49.55.000000000 AM');
insert into stream_do values(6, 9.55, 1000000000, '21-JAN-12 03.08.13.000000000 AM');
insert into stream_do values(7, 5.25, 600000000, '27-JAN-12 06.27.57.000000000 AM');

commit;
