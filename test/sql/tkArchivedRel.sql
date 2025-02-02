Rem
Rem $Header: cep/wlevs_cql/modules/cqlengine/test/sql/tkArchivedRel.sql /main/4 2012/08/06 18:03:22 apiper Exp $
Rem
Rem tkArchivedRel.sql
Rem
Rem Copyright (c) 2011, 2012, Oracle and/or its affiliates. 
Rem All rights reserved. 
Rem
Rem    NAME
Rem      tkArchivedRel.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    sbishnoi    05/20/11 - fix DDL by placing ;
Rem    sbishnoi    05/12/11 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

DROP VIEW CALLCENTERFACT_DO_VIEW;
DROP TABLE FLEX_CALLCENTERFACT_DO;

CREATE TABLE FLEX_CALLCENTERFACT_DO
   (	"ID" NUMBER(19,0) NOT NULL ENABLE, 
	"OPTLOCK" NUMBER(19,0), 
	"HIERARCHY" VARCHAR2(255), 
	"STRING_01" VARCHAR2(255), 
	"STRING_02" VARCHAR2(255), 
	"STRING_03" VARCHAR2(255), 
	"STRING_04" VARCHAR2(255), 
	"STRING_05" VARCHAR2(255), 
	"STRING_06" VARCHAR2(255), 
	"STRING_07" VARCHAR2(255), 
	"STRING_08" VARCHAR2(255), 
	"STRING_09" VARCHAR2(255), 
	"STRING_10" VARCHAR2(255), 
	"STRING_11" VARCHAR2(255), 
	"STRING_12" VARCHAR2(255), 
	"STRING_13" VARCHAR2(255), 
	"STRING_14" VARCHAR2(255), 
	"STRING_15" VARCHAR2(255), 
	"STRING_16" VARCHAR2(255), 
	"STRING_17" VARCHAR2(255), 
	"STRING_18" VARCHAR2(255), 
	"STRING_19" VARCHAR2(255), 
	"STRING_20" VARCHAR2(255), 
	"LONG_01" NUMBER(19,0), 
	"LONG_02" NUMBER(19,0), 
	"LONG_03" NUMBER(19,0), 
	"LONG_04" NUMBER(19,0), 
	"LONG_05" NUMBER(19,0), 
	"LONG_06" NUMBER(19,0), 
	"LONG_07" NUMBER(19,0), 
	"LONG_08" NUMBER(19,0), 
	"LONG_09" NUMBER(19,0), 
	"LONG_10" NUMBER(19,0), 
	"LONG_11" NUMBER(19,0), 
	"LONG_12" NUMBER(19,0), 
	"LONG_13" NUMBER(19,0), 
	"LONG_14" NUMBER(19,0), 
	"LONG_15" NUMBER(19,0), 
	"LONG_16" NUMBER(19,0), 
	"LONG_17" NUMBER(19,0), 
	"LONG_18" NUMBER(19,0), 
	"LONG_19" NUMBER(19,0), 
	"LONG_20" NUMBER(19,0), 
	"DECIMAL_01" NUMBER(38,0), 
	"DECIMAL_02" NUMBER(38,0), 
	"DECIMAL_03" NUMBER(38,0), 
	"DECIMAL_04" NUMBER(38,0), 
	"DECIMAL_05" NUMBER(38,0), 
	"DECIMAL_06" NUMBER(38,0), 
	"DECIMAL_07" NUMBER(38,0), 
	"DECIMAL_08" NUMBER(38,0), 
	"DECIMAL_09" NUMBER(38,0), 
	"DECIMAL_10" NUMBER(38,0), 
	"DECIMAL_11" NUMBER(38,0), 
	"DECIMAL_12" NUMBER(38,0), 
	"DECIMAL_13" NUMBER(38,0), 
	"DECIMAL_14" NUMBER(38,0), 
	"DECIMAL_15" NUMBER(38,0), 
	"DECIMAL_16" NUMBER(38,0), 
	"DECIMAL_17" NUMBER(38,0), 
	"DECIMAL_18" NUMBER(38,0), 
	"DECIMAL_19" NUMBER(38,0), 
	"DECIMAL_20" NUMBER(38,0), 
	"DATE_01" TIMESTAMP (6), 
	"DATE_02" TIMESTAMP (6), 
	"DATE_03" TIMESTAMP (6), 
	"DATE_04" TIMESTAMP (6), 
	"DATE_05" TIMESTAMP (6), 
	"DATE_06" TIMESTAMP (6), 
	"DATE_07" TIMESTAMP (6), 
	"DATE_08" TIMESTAMP (6), 
	"DATE_09" TIMESTAMP (6), 
	"DATE_10" TIMESTAMP (6), 
	 PRIMARY KEY ("ID"));

CREATE OR REPLACE FORCE VIEW CALLCENTERFACT_DO_VIEW ("OPTLOCK", "ID", "HIERARCHY", "CUSTOMERLOCATIONID", "PRODUCTID", "CUSTOMERSTATUS", "CALLPRIORITY", "CALLWAITTIME", "CALLPROCESSINGTIME", "CALLSTATUS", "CALLCLOSEDTIME") AS 
  SELECT OPTLOCK, ID, HIERARCHY, STRING_01 AS customerLocationId, STRING_02 AS productId, STRING_03 AS customerStatus, LONG_01 AS callPriority, LONG_02 AS callWaitTime, LONG_03 AS callProcessingTime, STRING_04 AS callStatus, DATE_01 AS callClosedTime FROM FLEX_CallCenterFact_DO;
 

INSERT INTO FLEX_CALLCENTERFACT_DO (ID, STRING_04, LONG_03, DATE_01) VALUES (1, 'CLOSED', 100, '12-MAY-11 10.05.00.000000000 AM');
INSERT INTO FLEX_CALLCENTERFACT_DO (ID, STRING_04, LONG_03, DATE_01) VALUES (2, 'CLOSED', 150, '12-MAY-11 10.10.00.000000000 AM');
INSERT INTO FLEX_CALLCENTERFACT_DO (ID, STRING_04, LONG_03, DATE_01) VALUES (10, 'CLOSED', 75, '12-MAY-11 10.15.00.000000000 AM');
INSERT INTO FLEX_CALLCENTERFACT_DO (ID, STRING_04, LONG_03, DATE_01) VALUES (11, 'OPEN', null, '12-MAY-11 10.20.00.123000000 AM');
INSERT INTO FLEX_CALLCENTERFACT_DO (ID, STRING_04, LONG_03, DATE_01) VALUES (12, 'CLOSED', 120, '12-MAY-11 10.20.00.123000000 AM');

CREATE TABLE BEAM_TRANSACTION_CONTEXT(TRANSACTION_CID NUMBER(19,0), TRANSACTION_TID NUMBER(19,0));

INSERT INTO BEAM_TRANSACTION_CONTEXT VALUES(1,1); 
INSERT INTO BEAM_TRANSACTION_CONTEXT VALUES(2,3); 
INSERT INTO BEAM_TRANSACTION_CONTEXT VALUES(3,2);
COMMIT;

