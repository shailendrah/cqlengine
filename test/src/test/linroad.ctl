-- This is a control file to write the data in a database for linear road
-- benchmark.
-- In order to read from a data file: CarLocStr3hours.dat
-- the followingg command should be issued:
-- sqlldr linroad/linroad control=linroad skip=1 data=/tmp/CarLocStr3hours
-- For creating the file, the following can be used:
-- sqlplus "/as sysdba"
--   drop user linroad cascade;
--   grant connect, resource, dba to linroad identified by linroad;
--   connect linroad/linroad
--   create table LinRoad(c1 integer, c2 integer, c3 integer, c4 integer, c5 integer, c6 integer, c7 integer);

LOAD DATA
   TRUNCATE INTO TABLE LINROAD
   (
   c1 INTEGER EXTERNAL TERMINATED BY " ",
   c2 INTEGER EXTERNAL TERMINATED BY ",",
   c3 INTEGER EXTERNAL TERMINATED BY ",",
   c4 INTEGER EXTERNAL TERMINATED BY ",",
   c5 INTEGER EXTERNAL TERMINATED BY ",",
   c6 INTEGER EXTERNAL TERMINATED BY ",",
   c7 INTEGER EXTERNAL TERMINATED BY ","
   )

