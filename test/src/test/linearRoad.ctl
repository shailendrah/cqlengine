-- This is a control file to write the data in a database for linear road
-- benchmark.
-- In order to read from a data file: historical-tolls.dat 
-- the following command should be issued:
-- sqlldr soainfra/soainfra control=linearRoad skip=1 data= $ADE_VIEW_ROOT/pcbpel/cep/test/data/historical-tolls.dat
-- For creating the file, the following can be used:
-- sqlplus "sys/welcome1 /as sysdba"
--   drop user soainfra cascade;
--   grant connect, resource, dba to soainfra identified by soainfra;
--   connect soainfra/soainfra
--   create table LinRoad(c1 integer, c2 integer, c3 integer, c4 integer);

LOAD DATA
   TRUNCATE INTO TABLE LINROAD
   (
   c1 INTEGER EXTERNAL TERMINATED BY ",",
   c2 INTEGER EXTERNAL TERMINATED BY ",",
   c3 INTEGER EXTERNAL TERMINATED BY ",",
   c4 INTEGER EXTERNAL TERMINATED BY WHITESPACE 
   )
