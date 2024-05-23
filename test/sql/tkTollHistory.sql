Rem
Rem $Header: tkTollHistory.sql 06-feb-2008.11:52:50 parujain Exp $
Rem
Rem tkTollHistory.sql
Rem
Rem Copyright (c) 2008, Oracle.  All rights reserved.  
Rem
Rem    NAME
Rem      tkTollHistory.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    parujain    01/22/08 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table LinRoad;

create table LinRoad(c1 integer, c2 integer, c3 integer, c4 integer);

create index linroad_idx on LinRoad (c1, c2, c3);

commit;

exit;
