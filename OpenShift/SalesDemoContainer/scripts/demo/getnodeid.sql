use system;
select current_time "TIME", hostname "CONNECTED TO HOST" from nodes where ID = getnodeid();
select number "JERSEY #" from hockey.hockey where name = 'MAX SUMMIT';
update hockey.hockey set number = number + 1 where name = 'MAX SUMMIT';