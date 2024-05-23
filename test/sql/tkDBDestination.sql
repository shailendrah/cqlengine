Rem
Rem $Header: pcbpel/cep/test/sql/tkDBDestination.sql /main/2 2009/03/23 13:32:30 hopark Exp $
Rem
Rem tkDBDestination.sql
Rem
Rem Copyright (c) 2008, 2009, Oracle and/or its affiliates.
Rem All rights reserved. 
Rem
Rem    NAME
Rem      tkDBDestination.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    sbishnoi    03/24/08 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

DROP TABLE DBDESTINATION1;

DROP TABLE DBDESTINATION2;

DROP TABLE DBDESTINATION3;

DROP TABLE DBDESTINATION4;

CREATE TABLE DBDESTINATION1 (COL1 INTEGER);

CREATE TABLE DBDESTINATION2 (COL1 INTEGER, COL2 INTEGER, PRIMARY KEY(COL1));

CREATE TABLE DBDESTINATION3 (COL1 INTEGER, COL2 INTEGER, PRIMARY KEY(COL1));

CREATE TABLE DBDESTINATION4 (COL1 INTEGER, COL2 INTEGER, PRIMARY KEY(COL1,COL2));

exit;

