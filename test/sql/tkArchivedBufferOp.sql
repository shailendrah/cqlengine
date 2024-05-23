Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedBufferOp.sql /main/1 2012/07/16 08:14:06 udeshmuk Exp $
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

drop table BufferOpTable;

create table BufferOpTable(c1 integer, c2 float, eid number(19,0));

insert into BufferOpTable values(5, 4.5, 1);
insert into BufferOpTable values(10, 9.27, 2);
insert into BufferOpTable values(20, 3.12, 3);

commit;
