SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100

drop table EmpTable;
drop table DeptTable;

create table EmpTable(beam_id number(19,0), EmpNo integer, EName varchar2(20), salary number(19,0), DeptNo integer);
create table DeptTable(beam_id number(19,0), DeptNo integer, DeptName varchar2(30));

insert into EmpTable values(101, 1001, 'emp1', 35000, 20);
insert into EmpTable values(102, 1002, 'emp2', 32000, 10);
insert into EmpTable values(103, 1003, 'emp3', 19000, 10);
insert into EmpTable values(104, 1004, 'emp4', 3000, 20);
insert into EmpTable values(105, 1005, 'emp5', 21000, 20);

insert into DeptTable values(1, 10, 'dep1');
insert into DeptTable values(2, 20, 'dep2');
commit;
