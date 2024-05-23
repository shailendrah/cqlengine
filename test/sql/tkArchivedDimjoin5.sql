Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedDimjoin5.sql /main/1 2015/07/10 20:13:20 sbishnoi Exp $
Rem
Rem tkArchivedDimjoin5.sql
Rem
Rem Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
Rem
Rem    NAME
Rem      tkArchivedDimjoin5.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    sbishnoi    07/10/15 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100
DROP TABLE BINOPRIGHTTESTTABLE2;
CREATE TABLE BINOPRIGHTTESTTABLE2(C3 NUMBER(19,0), C4 VARCHAR2(10));
insert into BINOPRIGHTTESTTABLE2 values (1,'first');
insert into BINOPRIGHTTESTTABLE2 values (2,'second');

DROP TABLE BINOPRIGHTTESTTABLE3;
CREATE TABLE BINOPRIGHTTESTTABLE3(C1 NUMBER(38,0), C3 NUMBER(19,0));
insert into BINOPRIGHTTESTTABLE3 values (1,1);
insert into BINOPRIGHTTESTTABLE3 values (20,2);
insert into BINOPRIGHTTESTTABLE3 values (10,3);

DROP TABLE BINOPRIGHTTESTTABLE4;
CREATE TABLE BINOPRIGHTTESTTABLE4(C1 NUMBER(38,0), C11 FLOAT, C111 NUMBER(19,0), C1111 VARCHAR2(10));
insert into BINOPRIGHTTESTTABLE4 values (10,1,1,'first');
insert into BINOPRIGHTTESTTABLE4 values (25,3,2,'fifth');
insert into BINOPRIGHTTESTTABLE4 values (30,4,3,'third');
insert into BINOPRIGHTTESTTABLE4 values (20,3,4,'second');
insert into BINOPRIGHTTESTTABLE4 values (20,6,5,'fourth');

COMMIT;
EXIT;
