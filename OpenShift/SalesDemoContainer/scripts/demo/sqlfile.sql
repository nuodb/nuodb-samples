use system;

select * from storagegroups;
select * from roles;
select * from schemas;

select substr(SQLSTRING,1,30) "SQL Stmt",
   COUNT, RUNTIME, USER,  SCHEMA,  NUMPARAM,  PARAMS,  CONNID, NODEID
from connections;

select ID, PORT, ADDRESS, HOSTNAME, STATE, TYPE, CONNSTATE 
from nodes;


